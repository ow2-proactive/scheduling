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
package org.ow2.proactive.scheduler.common;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * Wrapper class for Scheduler Event Listener.
 * The goal of this class is to overcome the classloading problem (i.e. the user defined
 * scheduler listener stub have to be loaded on Scheduler core side).
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public class UniversalSchedulerListener implements SchedulerEventListener, Serializable {

    // wrapped listener
    private final SchedulerEventListener internalListener;

    /**
     * Create a wrapper for user defined listener.
     * @param internalListener the wrapped listener.
     */
    public UniversalSchedulerListener(SchedulerEventListener internalListener) {
        this.internalListener = internalListener;
    }

    // For active object creation;
    public UniversalSchedulerListener() {
        this.internalListener = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.
     * proactive.scheduler.common.NotificationData)
     */
    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        this.internalListener.jobStateUpdatedEvent(notification);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.
     * proactive.scheduler.common.NotificationData)
     */
    @Override
    public void jobUpdatedFullDataEvent(JobState job) {
        this.internalListener.jobUpdatedFullDataEvent(job);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive
     * .scheduler.common.job.JobState)
     */
    @Override
    public void jobSubmittedEvent(JobState job) {
        this.internalListener.jobSubmittedEvent(job);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.
     * proactive.scheduler.common.SchedulerEvent)
     */
    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        this.internalListener.schedulerStateUpdatedEvent(eventType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskStateUpdatedEvent(org.ow2.
     * proactive.scheduler.common.NotificationData)
     */
    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        this.internalListener.taskStateUpdatedEvent(notification);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive
     * .scheduler.common.NotificationData)
     */
    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        this.internalListener.usersUpdatedEvent(notification);
    }

}
