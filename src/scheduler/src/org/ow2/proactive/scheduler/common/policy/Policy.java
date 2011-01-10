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
package org.ow2.proactive.scheduler.common.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.scheduler.common.job.JobDescriptor;
import org.ow2.proactive.scheduler.common.task.EligibleTaskDescriptor;


/**
 * Policy interface for the scheduler.
 * Must be implemented in order to be used as a policy in the scheduler core.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public abstract class Policy implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 30L;
    /**
     * Resources manager state. Can be used in an inherit policy to be aware
     * of resources informations like total nodes number, used nodes, etc.
     * Can be null the first time the {@link #getOrderedTasks(List)} method is called.
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
