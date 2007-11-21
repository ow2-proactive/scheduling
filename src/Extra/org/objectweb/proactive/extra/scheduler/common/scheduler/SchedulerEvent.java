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
 * SchedulerEvent is an Enumeration of all different events that scheduler can invoke.
 *
 * @author jlscheef - ProActiveTeam
 * @date 18 oct. 07
 * @version 3.2
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
    PENDING_TO_RUNNING_JOB("jobPendingToRunningEvent"),
    JOB_RESUMED("jobResumedEvent"),
    NEW_PENDING_JOB("jobSubmittedEvent"),
    RUNNING_TO_FINISHED_JOB("jobRunningToFinishedEvent"),
    REMOVE_FINISHED_JOB("jobRemoveFinishedEvent"),
    PENDING_TO_RUNNING_TASK("taskPendingToRunningEvent"),
    RUNNING_TO_FINISHED_TASK("taskRunningToFinishedEvent"),
    CHANGE_JOB_PRIORITY("jobChangePriorityEvent"),
    PAUSED("schedulerPausedEvent");
    private String methodName;

    /**
     * Default constructor.
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
