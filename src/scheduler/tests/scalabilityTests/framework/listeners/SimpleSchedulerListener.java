/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package scalabilityTests.framework.listeners;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.annotation.RemoteObject;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * This is a (simple) scheduler listener.
 * It listens to all the Scheduler events and just prints them to the logfile.
 * 
 * @author fabratu
 * 
 */
@RemoteObject
public class SimpleSchedulerListener implements SchedulerEventListener, Serializable {

    /**  */
	private static final long serialVersionUID = 21L;
	protected static final Logger logger = Logger.getLogger(SimpleSchedulerListener.class);

    public SimpleSchedulerListener() {
    }

    public void jobSubmittedEvent(JobState jobInfo) {
        logger.info("New job + " + jobInfo.getName() + " + has been started by " + jobInfo.getOwner());
    }

    public void jobStateUpdatedEvent(NotificationData<JobInfo> jobNotification) {
        logger.info("Job " + jobNotification.getData().getJobId() + " has changed its state to " +
            jobNotification.getEventType());
    }

    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        logger.info("Scheduler state changed to:" + eventType);
    }

    public void taskStateUpdatedEvent(NotificationData<TaskInfo> taskNotification) {
        logger.info("Task " + taskNotification.getData().getTaskId() + " has changed its state to " +
            taskNotification.getEventType());
    }

    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        logger.info("User info changed for:" + notification.getData().getUsername());
    }

}
