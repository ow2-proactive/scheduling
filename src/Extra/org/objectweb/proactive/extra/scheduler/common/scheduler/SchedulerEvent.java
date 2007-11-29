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
package org.objectweb.proactive.extra.scheduler.common.scheduler;


/**
 * Enumeration of all different events that scheduler can invoke.<br>
 * This is also used to choose which events you want to received from the scheduler.
 * See {@link UserSchedulerInterface}.addSchedulerEventListener for more details.
 *
 * @author jlscheef - ProActiveTeam
 * @date 18 oct. 07
 * @version 3.2
 * @publicAPI
 */
public enum SchedulerEvent {
    IMMEDIATE_PAUSED("schedulerImmediatePausedEvent"),
    RESUMED("schedulerResumedEvent"),SHUTDOWN("schedulerShutDownEvent"),
    SHUTTING_DOWN("schedulerShuttingDownEvent"),
    STARTED("schedulerStartedEvent"),
    STOPPED("schedulerStoppedEvent"),
    KILLED("schedulerKilledEvent"),
    JOB_KILLED("jobKilledEvent"),
    JOB_PAUSED("jobPausedEvent"),
    JOB_PENDING_TO_RUNNING("jobPendingToRunningEvent"),
    JOB_RESUMED("jobResumedEvent"),
    JOB_SUBMITTED("jobSubmittedEvent"),
    JOB_RUNNING_TO_FINISHED("jobRunningToFinishedEvent"),
    JOB_REMOVE_FINISHED("jobRemoveFinishedEvent"),
    TASK_PENDING_TO_RUNNING("taskPendingToRunningEvent"),
    TASK_RUNNING_TO_FINISHED("taskRunningToFinishedEvent"),
    JOB_CHANGE_PRIORITY("jobChangePriorityEvent"),
    PAUSED("schedulerPausedEvent");
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
