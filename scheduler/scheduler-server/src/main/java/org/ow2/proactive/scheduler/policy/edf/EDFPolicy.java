/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.policy.edf;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;


/**
 * Early Deadline First Policy sorts job based on job priorities,
 * and then among jobs of the same priorities policy sorts them
 * based on Job::getDeadline(). If job deadline is not set then
 * this policy considers the job as job with infinite deadline.
 * Job deadline is set by the user.  *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 8.3
 */
public class EDFPolicy extends ExtendedSchedulerPolicy {

    @Override
    public LinkedList<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs) {
        return jobs.stream()
                   .sorted(FIFO_BY_PRIORITY_AND_DEADLINE_COMPARATOR)
                   .flatMap(jobDescriptor -> jobDescriptor.getEligibleTasks().stream())
                   .map(taskDescriptors -> (EligibleTaskDescriptor) taskDescriptors)
                   .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Compare by priority, then by deadline
     */
    private static final Comparator<JobDescriptor> FIFO_BY_PRIORITY_AND_DEADLINE_COMPARATOR = (job1, job2) -> {
        final InternalJob internalJob1 = ((JobDescriptorImpl) job1).getInternal();
        final InternalJob internalJob2 = ((JobDescriptorImpl) job2).getInternal();
        JobPriority job1Priority = internalJob1.getPriority();
        JobPriority job2Priority = internalJob2.getPriority();
        if (job1Priority.equals(job2Priority)) {
            return getDeadLineFromJob(internalJob1).compareTo(getDeadLineFromJob(internalJob2));
        } else {
            return job2Priority.compareTo(job1Priority);
        }
    };

    /**
     *
     * @param internalJob
     * @return deadline of the job if existed, otherwise returns biggest date possible
     * (this is how this policy treats absence of deadline)
     */
    private static Date getDeadLineFromJob(InternalJob internalJob) {
        return internalJob.getJobDeadline().orElse(new Date(Long.MAX_VALUE));
    }
}
