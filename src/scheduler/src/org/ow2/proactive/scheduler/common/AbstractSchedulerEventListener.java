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
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskEvent;


/**
 * AbstractSchedulerEventListener implements an empty SchedulerEventListener.
 * Just Override the events you want to receive.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
public class AbstractSchedulerEventListener implements SchedulerEventListener<Job> {

    public void jobChangePriorityEvent(JobEvent event) {
    }

    public void jobPausedEvent(JobEvent event) {
    }

    public void jobPendingToRunningEvent(JobEvent event) {
    }

    public void jobRemoveFinishedEvent(JobEvent event) {
    }

    public void jobResumedEvent(JobEvent event) {
    }

    public void jobRunningToFinishedEvent(JobEvent event) {
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

    public void taskPendingToRunningEvent(TaskEvent event) {
    }

    public void taskRunningToFinishedEvent(TaskEvent event) {
    }

    public void taskWaitingForRestart(TaskEvent event) {
    }

    public void usersUpdate(UserIdentification userIdentification) {
    }

    public void schedulerPolicyChangedEvent(String newPolicyName) {
    }

}
