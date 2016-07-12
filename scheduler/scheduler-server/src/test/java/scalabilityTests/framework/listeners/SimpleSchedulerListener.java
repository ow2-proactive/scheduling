/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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

    protected static final Logger logger = Logger.getLogger(SimpleSchedulerListener.class);

    public SimpleSchedulerListener() {
    }

    public void jobSubmittedEvent(JobState jobState) {
        logger.info("New job + " + jobState.getName() + " + has been started by " + jobState.getOwner());
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

    @Override
    public void jobUpdatedFullDataEvent(JobState jobState) {
        logger.info("New job + " + jobState.getName() + " + has been started by " + jobState.getOwner());

    }

}
