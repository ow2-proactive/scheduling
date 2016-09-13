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
package org.ow2.proactive.scheduler.common;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Enumeration of all different events that scheduler can invoke.
 * <p>
 * This is also used to choose which events you want to received from the scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 * @see Scheduler#addEventListener for more details.
 *
 * $Id$
 */
@PublicAPI
public enum SchedulerEvent {

    // WARNING: New State must be added at the end of the existing one in order to keep associated ordinal number.

    /** The scheduler has just been frozen, this pause will stop every process except the running one. */
    FROZEN("Frozen"), /** The scheduler has just been resumed. */
    RESUMED("Resumed"), /** The scheduler has just been shutdown. */
    SHUTDOWN("Shutdown"), /** The scheduler is shutting down. */
    SHUTTING_DOWN("Shutting down"), /** The scheduler has just been started. */
    STARTED("Started"), /** The scheduler has just been stopped. Every jobs will be stopped and running tasks will finished. */
    STOPPED("Stopped"), /** The scheduler has just been killed. */
    KILLED("Killed"), /** A job has just been paused. It will finished the running task. */
    JOB_PAUSED("Job paused"), /** A job has just been scheduled. At least one of its task is running. */
    JOB_PENDING_TO_RUNNING("Job pending to running"), /** A job has just been resumed. */
    JOB_RESUMED("Job resumed"), /** A job has just been submitted. */
    JOB_SUBMITTED("Job submitted"), /** A job has just finished. All tasks are finished. */
    JOB_RUNNING_TO_FINISHED("Job running to finished"), /** A job has just been removed from scheduler. */
    JOB_REMOVE_FINISHED("Job remove finished"), /** A task has just been scheduled. It is now running. */
    TASK_PENDING_TO_RUNNING("Task pending to running"), /** A task has just finished. */
    TASK_RUNNING_TO_FINISHED(
            "Task running to finished"), /** A task has just had an error, it will wait for restart. */
    TASK_IN_ERROR_TO_FINISHED(
                    "Task In-Error to finished"), /** A task has just had an error, was marked as finished. */
    TASK_WAITING_FOR_RESTART("Task waiting for restart"), /** The priority of a job has just been change. */
    JOB_CHANGE_PRIORITY(
            "Job change piority"), /** The scheduler has just been paused. Every running job will be finished. */
    PAUSED("Paused"), /** The Resource Manager is no more available. */
    RM_DOWN("RM down"), /** The Resource Manager is re-available . */
    RM_UP("RM up"), /** A user has just connect the scheduler or submit a job. */
    USERS_UPDATE("Users updated"), /** The scheduling policy has been changed. */
    POLICY_CHANGED(
            "Policy changed"), /** A job has been terminated from pending queue. All tasks are finished. */
    JOB_PENDING_TO_FINISHED(
            "Job pending to finished"), /** A Control Flow Action led to the replication of a task */
    TASK_REPLICATED("Task replicated"), /** A Control Flow Action (branching) led to a task being skipped */
    TASK_SKIPPED("Task skipped"), /** A new task progress value is available */
    TASK_PROGRESS("Task progress"), /** The database is no more available. */
    DB_DOWN("DB down"), /**
                         * A job has just been paused on error. It means that job execution can no longer progress
                         * due to one or more tasks which are in paused on error state.
                         */
    JOB_IN_ERROR("Job In-Error"), /** A task has just been paused due to an error while executing */
    TASK_IN_ERROR("Task In-Error"), /** A job has just been restarted from error. */
    JOB_RESTARTED_FROM_ERROR("Job restarted from error"), /** A job has just been updated.*/
    JOB_UPDATED("job updated");

    /** Name of the method */
    private String methodName;

    /**
     * Default implicit constructor.
     *
     * @param method the method to call as a string.
     */
    SchedulerEvent(String method) {
        methodName = method;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return methodName;
    }
}
