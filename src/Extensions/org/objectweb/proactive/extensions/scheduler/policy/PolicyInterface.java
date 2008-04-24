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
package org.objectweb.proactive.extensions.scheduler.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.extensions.scheduler.job.JobDescriptor;
import org.objectweb.proactive.extensions.scheduler.resourcemanager.RMState;
import org.objectweb.proactive.extensions.scheduler.task.EligibleTaskDescriptor;


/**
 * Policy interface for the scheduler.
 * Must be implemented in order to be used as a policy in the scheduler core.
 *
 * @author The ProActive Team
 * @version 3.9, Jul 5, 2007
 * @since ProActive 3.9
 */
public abstract class PolicyInterface implements Serializable {

    /** 
     * Resources manager state. Can be used in an inherit policy to be aware
     * of resources informations like total nodes number, used nodes, etc. 
     */
    public RMState RMState = null;

    /**
     * Return the tasks that have to be scheduled.
     * The tasks must be in the desired scheduling order.
     * The first task to be schedule must be the first in the returned Vector.
     *
     * @param jobs the list of pending or running job descriptors.
     * @return a vector of every tasks that are ready to be schedule.
     */
    public abstract Vector<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs);
}
