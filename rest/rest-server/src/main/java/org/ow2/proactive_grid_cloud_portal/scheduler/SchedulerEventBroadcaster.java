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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.apache.log4j.Logger;
import org.atmosphere.cpr.BroadcasterFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventNotification;

import com.google.common.base.Throwables;

/**
 * Broadcasts the scheduler events which it receives to the target REST client.
 */
public class SchedulerEventBroadcaster implements SchedulerEventListener {
    private static final Logger log = Logger.getLogger(SchedulerEventBroadcaster.class);
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector());
        mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
    }

    private String broadcasterUuid;

    public SchedulerEventBroadcaster() {
    }

    public SchedulerEventBroadcaster(String broadcasterUuid) {
        this.broadcasterUuid = broadcasterUuid;
    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        broadcast(new EventNotification(EventNotification.Action.JOB_STATE_UPDATED,
                eventTypeName(notification), notification.getData()));
    }

    @Override
    public void jobSubmittedEvent(JobState jobState) {
        broadcast(new EventNotification(EventNotification.Action.JOB_SUBMITTED,
                SchedulerEvent.JOB_SUBMITTED.name(), jobState));
    }

    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent schedulerEvent) {
        broadcast(new EventNotification(EventNotification.Action.SCHEDULER_STATE_UPDATED,
                schedulerEvent.name(), null));
    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        broadcast(new EventNotification(EventNotification.Action.TASK_STATE_UPDATED,
                eventTypeName(notification), notification.getData()));
    }

    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        broadcast(new EventNotification(EventNotification.Action.USERS_UPDATED, eventTypeName(notification),
                notification.getData()));
    }

    private void broadcast(EventNotification eventNotification) {
        try {
            BroadcasterFactory.getDefault().lookup(broadcasterUuid)
                    .broadcast(mapper.writeValueAsString(eventNotification));
        } catch (Exception e) {
            log.error("Cannot broadcast event notification.", e);
            Throwables.propagate(e);
        }
    }

    private <T> String eventTypeName(NotificationData<T> notification) {
        return notification.getEventType().name();
    }
}
