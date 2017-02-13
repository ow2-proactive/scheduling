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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

public enum TaskStatusData {

    /**
     * The task has just been submitted by the user.
     */
    SUBMITTED,
    /**
     * The task is in the scheduler pending queue.
     */
    PENDING,
    /**
     * The task is paused.
     */
    PAUSED,
    /**
     * The task is executing.
     */
    RUNNING,
    /**
     * The task is waiting for restart after an error. (ie:native code != 0 or exception)
     */
    WAITING_ON_ERROR,
    /**
     * The task is waiting for restart after a failure. (ie:node down)
     */
    WAITING_ON_FAILURE,
    /**
     * The task is failed
     * (only if max execution time has been reached and the node on which it was started is down).
     */
    FAILED,
    /**
     * The task could not be started.<br>
     * It means that the task could not be started due to
     * dependences failure.
     */
    NOT_STARTED,
    /**
     * The task could not be restarted.<br>
     * It means that the task could not be restarted after an error
     * during the previous execution
     */
    NOT_RESTARTED,
    /**
     * The task has been aborted by an exception on an other task while the task is running. (job is cancelOnError=true)
     * Can be also in this status if the job is killed while the concerned task was running.
     */
    ABORTED,
    /**
     * The task has finished execution with error code (!=0) or exception.
     */
    FAULTY,
    /**
     * The task has finished execution.
     */
    FINISHED,
    /**
     * The task was not executed: it was the non-selected branch of an IF/ELSE control flow action
     */
    SKIPPED,
    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskStatus#IN_ERROR
     */
    IN_ERROR;

}
