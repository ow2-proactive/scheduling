/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extra.scheduler.policy;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.extra.scheduler.job.JobDescriptor;
import org.objectweb.proactive.extra.scheduler.task.EligibleTaskDescriptor;


/**
 * Implementation of the policy using FIFO prio ordering.
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Jul 5, 2007
 * @since ProActive 3.2
 */
public class PriorityPolicy implements PolicyInterface {

    /** Serial version UID */
    private static final long serialVersionUID = -5882465083001537486L;

    /**
     * This method return the tasks using FIFO policy according to the jobs priorities.
     *
     * @see org.objectweb.proactive.extra.scheduler.policy.PolicyInterface#getReadyTasks(java.util.List)
     */
    public Vector<EligibleTaskDescriptor> getOrderedTasks(
        List<JobDescriptor> jobs) {
        Vector<EligibleTaskDescriptor> toReturn = new Vector<EligibleTaskDescriptor>();
        //sort jobs by priority
        Collections.sort(jobs);

        for (JobDescriptor lj : jobs) {
            toReturn.addAll(lj.getEligibleTasks());
        }

        return toReturn;
    }
}
