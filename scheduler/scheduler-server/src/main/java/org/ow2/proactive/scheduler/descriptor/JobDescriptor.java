/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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

import java.util.Collection;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * This class represents a job for the policy.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface JobDescriptor extends Comparable<JobDescriptor> {

    /**
     * Return true if the task represented by the given taskId has children, false if not.
     *
     * @param taskId the id representing the real task.
     * @return true if the task represented by the given taskId has children, false if not.
     */
    boolean hasChildren(TaskId taskId);

    /**
     * To get the tasks.
     *
     * @return the tasks.
     */
    Collection<EligibleTaskDescriptor> getEligibleTasks();

    /**
     * Get the job id
     *
     * @return the job id
     */
    JobId getJobId();

    /**
     * Return the internal representation of the job.
     * To be used carefully.
     *
     * @return the internal representation of the job.
     */
    InternalJob getInternal();

    /**
     * Return the list of running tasks
     *
     * @return the list of running tasks
     */
    Map<TaskId, TaskDescriptor> getRunningTasks();

    /**
     * Return the list of paused tasks
     *
     * @return the list of paused tasks
     */
    Map<TaskId, ? extends TaskDescriptor> getPausedTasks();

    void restoreInErrorTasks();

}
