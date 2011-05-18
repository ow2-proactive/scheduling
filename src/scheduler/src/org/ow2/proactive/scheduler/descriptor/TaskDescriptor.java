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
package org.ow2.proactive.scheduler.descriptor;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * This class represents a task for the policy.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public interface TaskDescriptor extends Serializable {

    /**
     * To get the children
     *
     * @return the children
     */
    public Vector<TaskDescriptor> getChildren();

    /**
     * Return the internal representation of the task.
     * To be used carefully.
     *
     * @return the internal representation of the task.
     */
    public InternalTask getInternal();

    /**
     * To get the id of the corresponding task
     *
     * @return the id of the corresponding task
     */
    public TaskId getTaskId();

    /**
     * To get the parents
     *
     * @return the parents
     */
    public Vector<TaskDescriptor> getParents();

    /**
     * To get the jobId
     *
     * @return the jobId
     */
    public JobId getJobId();

    /**
     * Return the number of children remaining.
     *
     * @return the number of children remaining.
     */
    public int getChildrenCount();

    /**
     * Get the number of attempt the core has made to start this task.
     *
     * @return the number of attempt the core has made to start this task.
     */
    public int getAttempt();

}
