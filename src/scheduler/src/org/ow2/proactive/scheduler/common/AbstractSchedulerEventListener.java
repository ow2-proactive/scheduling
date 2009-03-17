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
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.common;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * AbstractSchedulerEventListener implements an empty SchedulerEventListener.
 * Just Override the events you want to receive.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public class AbstractSchedulerEventListener implements SchedulerEventListener {

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobChangePriorityEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobChangePriorityEvent(JobInfo info) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobPausedEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobPausedEvent(JobInfo info) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobPendingToRunningEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobPendingToRunningEvent(JobInfo info) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobRemoveFinishedEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobRemoveFinishedEvent(JobInfo info) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobResumedEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobResumedEvent(JobInfo info) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobRunningToFinishedEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobRunningToFinishedEvent(JobInfo info) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.JobState)
     */
    public void jobSubmittedEvent(JobState job) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerFrozenEvent()
     */
    public void schedulerFrozenEvent() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerKilledEvent()
     */
    public void schedulerKilledEvent() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerPausedEvent()
     */
    public void schedulerPausedEvent() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerRMDownEvent()
     */
    public void schedulerRMDownEvent() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerRMUpEvent()
     */
    public void schedulerRMUpEvent() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerResumedEvent()
     */
    public void schedulerResumedEvent() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerShutDownEvent()
     */
    public void schedulerShutDownEvent() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerShuttingDownEvent()
     */
    public void schedulerShuttingDownEvent() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStartedEvent()
     */
    public void schedulerStartedEvent() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStoppedEvent()
     */
    public void schedulerStoppedEvent() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskPendingToRunningEvent(org.ow2.proactive.scheduler.common.task.TaskInfo)
     */
    public void taskPendingToRunningEvent(TaskInfo info) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskRunningToFinishedEvent(org.ow2.proactive.scheduler.common.task.TaskInfo)
     */
    public void taskRunningToFinishedEvent(TaskInfo info) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskWaitingForRestart(org.ow2.proactive.scheduler.common.task.TaskInfo)
     */
    public void taskWaitingForRestart(TaskInfo info) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdate(org.ow2.proactive.scheduler.common.job.UserIdentification)
     */
    public void usersUpdate(UserIdentification userIdentification) {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerPolicyChangedEvent(java.lang.String)
     */
    public void schedulerPolicyChangedEvent(String newPolicyName) {
    }

}
