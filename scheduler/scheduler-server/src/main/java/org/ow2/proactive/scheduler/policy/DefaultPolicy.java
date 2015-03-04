/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.policy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

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
    public Vector<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs) {
        Vector<EligibleTaskDescriptor> toReturn = new Vector<EligibleTaskDescriptor>();

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
