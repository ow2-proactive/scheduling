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
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * AbstractSchedulerEventListener implements an empty SchedulerEventListener.
 * Just Override the events you want to receive.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
public class AbstractSchedulerEventListener implements SchedulerEventListener {

    public void jobChangePriorityEvent(JobInfo info) {
    }

    public void jobPausedEvent(JobInfo info) {
    }

    public void jobPendingToRunningEvent(JobInfo info) {
    }

    public void jobRemoveFinishedEvent(JobInfo info) {
    }

    public void jobResumedEvent(JobInfo info) {
    }

    public void jobRunningToFinishedEvent(JobInfo info) {
    }

    public void jobSubmittedEvent(Job job) {
    }

    public void schedulerFrozenEvent() {
    }

    public void schedulerKilledEvent() {
    }

    public void schedulerPausedEvent() {
    }

    public void schedulerRMDownEvent() {
    }

    public void schedulerRMUpEvent() {
    }

    public void schedulerResumedEvent() {
    }

    public void schedulerShutDownEvent() {
    }

    public void schedulerShuttingDownEvent() {
    }

    public void schedulerStartedEvent() {
    }

    public void schedulerStoppedEvent() {
    }

    public void taskPendingToRunningEvent(TaskInfo info) {
    }

    public void taskRunningToFinishedEvent(TaskInfo info) {
    }

    public void taskWaitingForRestart(TaskInfo info) {
    }

    public void usersUpdate(UserIdentification userIdentification) {
    }

    public void schedulerPolicyChangedEvent(String newPolicyName) {
    }

}
