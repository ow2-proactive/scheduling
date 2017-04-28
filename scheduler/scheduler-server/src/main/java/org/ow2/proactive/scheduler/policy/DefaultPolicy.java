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
package org.ow2.proactive.scheduler.policy;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;


/**
 * Implementation of the policy according that :
 * <ul>
 * 	<li>Implementation of the policy using FIFO priority ordering.</li>
 * 	<li>Relies on Job IDs for FIFO.</li>
 * </ul>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public class DefaultPolicy extends Policy {

    /**
     * {@inheritDoc}
     * Override reload to avoid reading config file
     * Attempting to read a non-existing file will fail policy changes or renewal.
     */
    @Override
    public boolean reloadConfig() {
        return true;
    }

    /**
     * This method return the tasks using FIFO policy according to the jobs priorities.
     *
     * @see org.ow2.proactive.scheduler.policy.Policy#getOrderedTasks(java.util.List)
     */
    @Override
    public LinkedList<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs) {
        LinkedList<EligibleTaskDescriptor> toReturn = new LinkedList<>();

        Collections.sort(jobs, FIFO_BY_PRIORITY_COMPARATOR);

        //add all sorted tasks to list of tasks
        for (JobDescriptor jd : jobs) {
            toReturn.addAll(jd.getEligibleTasks());
        }

        //return sorted list of tasks
        return toReturn;
    }

    public static final Comparator<JobDescriptor> FIFO_BY_PRIORITY_COMPARATOR = new Comparator<JobDescriptor>() {
        @Override
        public int compare(JobDescriptor job1, JobDescriptor job2) {
            JobPriority job1Priority = job1.getInternal().getPriority();
            JobPriority job2Priority = job2.getInternal().getPriority();
            if (job1Priority.equals(job2Priority)) {
                return job1.getJobId().compareTo(job2.getJobId());
            } else {
                return job2Priority.compareTo(job1Priority);
            }
        }
    };

}
