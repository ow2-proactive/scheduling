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
package org.objectweb.proactive.extensions.scheduler.common.scheduler;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Enumeration of all different events that scheduler can invoke.<br>
 * This is also used to choose which events you want to received from the scheduler.
 * See {@link UserSchedulerInterface}.addSchedulerEventListener for more details.
 *
 * @author The ProActive Team
 * @date 18 oct. 07
 * @version 3.9
 * @since ProActive 3.9
 */
@PublicAPI
public enum SchedulerEvent {

    //WARNING : State must be followed by a string representing the name of the method associated in
    //the schedulerEventListener class.

    /** The scheduler has just been frozen, this pause will stop every process except the running one. */
    FROZEN("schedulerFrozenEvent"),

    /** The scheduler has just been resumed. */
    RESUMED("schedulerResumedEvent"), SHUTDOWN("schedulerShutDownEvent"),

    /** The scheduler is shutting down. */
    SHUTTING_DOWN("schedulerShuttingDownEvent"),

    /** The scheduler has just been started. */
    STARTED("schedulerStartedEvent"),
    /** The scheduler has just been stopped. Every jobs will be stopped and running tasks will finished. */
    STOPPED("schedulerStoppedEvent"),
    /** The scheduler has just been killed. */
    KILLED("schedulerKilledEvent"),
    /** A job has just been killed. */
    JOB_KILLED("jobKilledEvent"),
    /** A job has just been paused. It will finished the running task. */
    JOB_PAUSED("jobPausedEvent"),

    /** A job has just been scheduled. At least one of its task is running. */
    JOB_PENDING_TO_RUNNING("jobPendingToRunningEvent"),

    /** A job has just been resumed. */
    JOB_RESUMED("jobResumedEvent"),
    /** A job has just been submitted. */
    JOB_SUBMITTED("jobSubmittedEvent"),

    /** A job has just finished. All tasks are finished. */
    JOB_RUNNING_TO_FINISHED("jobRunningToFinishedEvent"),

    /** A job has just been removed from scheduler. */
    JOB_REMOVE_FINISHED("jobRemoveFinishedEvent"),

    /** A task has just been scheduled. It is now running. */
    TASK_PENDING_TO_RUNNING("taskPendingToRunningEvent"),

    /** A task has just finished. */
    TASK_RUNNING_TO_FINISHED("taskRunningToFinishedEvent"),

    /** A task has just had an error, it will wait for restart. */
    TASK_WAITING_FOR_RESTART("taskWaitingForRestart"),

    /** The priority of a job has just been change. */
    JOB_CHANGE_PRIORITY("jobChangePriorityEvent"),

    /** The scheduler has just been paused. Every running job will be finished. */
    PAUSED("schedulerPausedEvent"),

    /** The Resource Manager is no more available. */
    RM_DOWN("schedulerRMDownEvent"),

    /** The Resource Manager is re-available . */
    RM_UP("schedulerRMUpEvent"),

    /** A user has just connect the scheduler or submit a job. */
    USERS_UPDATE("usersUpdate");

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
