/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
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
import java.util.List;
import java.util.Vector;

import org.ow2.proactive.scheduler.job.JobDescriptor;
import org.ow2.proactive.scheduler.task.EligibleTaskDescriptor;


/**
 * Implementation of the policy using FIFO prio ordering.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class PriorityPolicy extends PolicyInterface {

    /**
     * This method return the tasks using FIFO policy according to the jobs priorities.
     *
     * @see org.ow2.proactive.scheduler.policy.PolicyInterface#getOrderedTasks(java.util.List)
     */
    public Vector<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs) {
        Vector<EligibleTaskDescriptor> toReturn = new Vector<EligibleTaskDescriptor>();
        //sort jobs by priority
        Collections.sort(jobs);

        for (JobDescriptor jd : jobs) {
            toReturn.addAll(jd.getEligibleTasks());
        }

        return toReturn;
    }
}
