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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.util.ServletContextFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventNotification;

import com.google.common.base.Throwables;


/**
 * Broadcasts the scheduler events which it receives to the target REST client.
 */
public class SchedulerEventBroadcaster implements SchedulerEventListener {

    private static final Logger log = Logger.getLogger(SchedulerEventBroadcaster.class);

    private static final Mapper dozerMapper = DozerBeanMapperSingletonWrapper.getInstance();

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector());
        mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
    }

    private String broadcasterUUID;

    public SchedulerEventBroadcaster() {
        super();
    }

    public SchedulerEventBroadcaster(String broadcasterUuid) {
        this();
        this.broadcasterUUID = broadcasterUuid;
    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        broadcast(new EventNotification(EventNotification.Action.JOB_STATE_UPDATED,
                                        eventTypeName(notification),
                                        notification.getData()));
    }

    @Override
    public void jobUpdatedFullDataEvent(JobState jobState) {
        broadcast(new EventNotification(EventNotification.Action.JOB_FULL_DATA_UPDATED,
                                        SchedulerEvent.JOB_UPDATED.name(),
                                        jobState));
    }

    @Override
    public void jobSubmittedEvent(JobState jobState) {
        broadcast(new EventNotification(EventNotification.Action.JOB_SUBMITTED,
                                        SchedulerEvent.JOB_SUBMITTED.name(),
                                        jobState));
    }

    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent schedulerEvent) {
        broadcast(new EventNotification(EventNotification.Action.SCHEDULER_STATE_UPDATED, schedulerEvent.name(), null));
    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        TaskInfoData taskInfoData = dozerMapper.map(notification.getData(), TaskInfoData.class);

        broadcast(new EventNotification(EventNotification.Action.TASK_STATE_UPDATED,
                                        eventTypeName(notification),
                                        taskInfoData));
    }

    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        broadcast(new EventNotification(EventNotification.Action.USERS_UPDATED,
                                        eventTypeName(notification),
                                        notification.getData()));
    }

    private void broadcast(EventNotification eventNotification) {
        try {
            ServletContext servletContext = ServletContextFactory.getDefault().getServletContext();

            ((BroadcasterFactory) servletContext.getAttribute(BroadcasterFactory.class.getName())).lookup(broadcasterUUID)
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
