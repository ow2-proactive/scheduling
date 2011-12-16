/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive.scheduler.gui.data;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * Active object listening for Scheduler events.
 *  
 * @author The ProActive Team
 *
 */
public class SchedulerEventListenerImpl implements SchedulerEventListener {

    private volatile boolean stopListenEvents;

    private JobsController jobController;

    public static SchedulerEventListenerImpl createActiveObjectListener(JobsController jobController)
            throws Exception {
        SchedulerEventListenerImpl listener = new SchedulerEventListenerImpl();
        listener.jobController = jobController;
        return PAActiveObject.turnActive(listener);
    }

    public SchedulerEventListenerImpl() {
    }

    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        if (stopListenEvents) {
            return;
        }
        jobController.schedulerStateUpdatedEvent(eventType);
    }

    @Override
    public void jobSubmittedEvent(JobState job) {
        if (stopListenEvents) {
            return;
        }
        jobController.jobSubmittedEvent(job);
    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        if (stopListenEvents) {
            return;
        }
        jobController.jobStateUpdatedEvent(notification);
    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        if (stopListenEvents) {
            return;
        }
        jobController.taskStateUpdatedEvent(notification);
    }

    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        if (stopListenEvents) {
            return;
        }
        jobController.usersUpdatedEvent(notification);
    }

    @ImmediateService
    public void stopListenEvents() {
        stopListenEvents = true;
    }

}
