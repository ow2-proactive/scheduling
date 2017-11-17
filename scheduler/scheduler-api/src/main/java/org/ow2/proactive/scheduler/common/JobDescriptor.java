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
package org.ow2.proactive.scheduler.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.TaskId;


/**
 * This class represents a job for the policy.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface JobDescriptor extends Serializable, Comparable<JobDescriptor> {

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
    Collection<TaskDescriptor> getEligibleTasks();

    /**
     * Get the job id
     *
     * @return the job id
     */
    JobId getJobId();

    /**
     * Get the job priority
     *
     * @return the job priority
     */
    JobPriority getJobPriority();

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

    /**
     * Retrieve the persisted state of tasks and restore the state of tasks
     * that are found running.
     */
    void restoreRunningTasks();

}
