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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;


/**
 * Implementation of the policy according that :
 * <ul>
 * 	<li>We try to keep scheduling policy order while it's not leading to starvation
 * 	(if first tasks to be started are finally not started because of bad selection script, we must go on next tasks but
 *  this situation could lead to run lower priority before normal or high or even mislead FIFO order.)</li>
 * 	<li>A specified number of tasks can be returned by this policy (NB_TASKS_PER_LOOP setting)</li>
 *  <li>This policy returns groups of tasks sequentially taken in the whole queue.
 *  Tasks that have just been returned are not returned by the next call to the policy until a predefined
 *  number of calls is reached. (for example, if their are X tasks to schedule, the policy returns Y tasks, and there is Z nodes available,
 *  the policy will return X/Z groups of Y tasks AND THEN restart to get task from beginning of the queue.
 *  This will avoid starvation.</li>
 * </ul>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
class InternalPolicy {

    /** Maximum number of tasks returned by the policy in each loop */
    private int NB_TASKS_PER_LOOP = PASchedulerProperties.SCHEDULER_POLICY_NBTASKPERLOOP.getValueAsInt();

    private Set<TaskId> ids = new HashSet<>();
    private int previousFreeNodeNumber = 0;
    RMState RMState = null;

    /**
     * Filter tasks by splitting them into group of configurable number of tasks.
     * This method just controls what is provided by scheduling policy
     *
     * @param orderedTasks the list of ordered task provide by the scheduling policy
     * @return a filtered and splited list of task to be scheduled
     */
    public LinkedList<EligibleTaskDescriptor> filter(Vector<EligibleTaskDescriptor> orderedTasks) {
        //safety branch
        if (orderedTasks == null || orderedTasks.size() == 0) {
            return null;
        }

        //check number of free nodes
        int freeNodeNb;
        if (RMState == null) {
            freeNodeNb = NB_TASKS_PER_LOOP;
        } else {
            freeNodeNb = RMState.getFreeNodesNumber();
        }

        LinkedList<EligibleTaskDescriptor> toReturn = new LinkedList<>();

        //fill list of task to be returned by the policy
        //max number of returned tasks will be the number of tasks per loop
        int i = 0;
        for (EligibleTaskDescriptor etd : orderedTasks) {
            if (!ids.contains(etd.getTaskId())) {
                toReturn.add(etd);
                ids.add(etd.getTaskId());
                if (++i == NB_TASKS_PER_LOOP) {
                    break;
                }
            }
        }

        //clear ids list in some conditions
        if (toReturn.size() == 0 || freeNodeNb != previousFreeNodeNumber || freeNodeNb == 0) {
            ids.clear();
        }

        previousFreeNodeNumber = freeNodeNb;

        return toReturn;
    }

}
