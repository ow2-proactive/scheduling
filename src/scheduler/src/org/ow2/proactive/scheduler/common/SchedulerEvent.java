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
package org.ow2.proactive.scheduler.common;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Enumeration of all different events that scheduler can invoke.<br>
 * This is also used to choose which events you want to received from the scheduler.
 * See {@link UserSchedulerInterface}.addSchedulerEventListener for more details.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 * $Id$
 */
@PublicAPI
public enum SchedulerEvent {

    //WARNING : New State must be added at the end of the existing one in order to keep associated ordinal number.

    /** The scheduler has just been frozen, this pause will stop every process except the running one. */
    FROZEN("Frozen"),
    /** The scheduler has just been resumed. */
    RESUMED("Resumed"),
    /** The scheduler has just been shutdown. */
    SHUTDOWN("Shutdown"),
    /** The scheduler is shutting down. */
    SHUTTING_DOWN("Shutting down"),
    /** The scheduler has just been started. */
    STARTED("Started"),
    /** The scheduler has just been stopped. Every jobs will be stopped and running tasks will finished. */
    STOPPED("Stopped"),
    /** The scheduler has just been killed. */
    KILLED("Killed"),
    /** A job has just been paused. It will finished the running task. */
    JOB_PAUSED("Job paused"),
    /** A job has just been scheduled. At least one of its task is running. */
    JOB_PENDING_TO_RUNNING("Job pending to running"),
    /** A job has just been resumed. */
    JOB_RESUMED("Job resumed"),
    /** A job has just been submitted. */
    JOB_SUBMITTED("Job submitted"),
    /** A job has just finished. All tasks are finished. */
    JOB_RUNNING_TO_FINISHED("Job running to finished"),
    /** A job has just been removed from scheduler. */
    JOB_REMOVE_FINISHED("Job remove finished"),
    /** A task has just been scheduled. It is now running. */
    TASK_PENDING_TO_RUNNING("Task pending to running"),
    /** A task has just finished. */
    TASK_RUNNING_TO_FINISHED("Task running to finished"),
    /** A task has just had an error, it will wait for restart. */
    TASK_WAITING_FOR_RESTART("Task waiting for restart"),
    /** The priority of a job has just been change. */
    JOB_CHANGE_PRIORITY("Job change piority"),
    /** The scheduler has just been paused. Every running job will be finished. */
    PAUSED("Paused"),
    /** The Resource Manager is no more available. */
    RM_DOWN("RM down"),
    /** The Resource Manager is re-available . */
    RM_UP("RM up"),
    /** A user has just connect the scheduler or submit a job. */
    USERS_UPDATE("Users updated"),
    /** The scheduling policy has been changed. */
    POLICY_CHANGED("Policy changed");

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
