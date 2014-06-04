/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
package org.ow2.proactive.scheduler.rest;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.rest.data.DataUtility;
import org.ow2.proactive.scheduler.rest.utils.EventCodecUtil;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventNotification;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventNotification.Action;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventSubscription;

import com.google.common.collect.Lists;

/**
 * Utility class to subscribe and receive scheduler events from REST server. It
 * utilizes Atmosphere 2.0 framework
 * 
 */
public class SchedulerEventReceiver {

    private static final String EVENTING_PATH = "scheduler/events";
    private static final Logger logger = Logger.getLogger(SchedulerEventReceiver.class);

    private String restServerUrl;
    private SchedulerEventListener eventListener;
    private boolean myEventsOnly;
    private SchedulerEvent[] events;
    private String sessionId;
    private Socket socket;

    private SchedulerEventReceiver() {
    }

    public void start() throws IOException {
        openAndReceive(eventListener, myEventsOnly, events);
    }

    @SuppressWarnings("rawtypes")
    private void openAndReceive(final SchedulerEventListener eventListener, boolean myEventsOnly,
            SchedulerEvent... events) throws IOException {
        Client client = ClientFactory.getDefault().newClient();
        socket = client.create();
        EventNotificationHandler notificationHandler = new EventNotificationHandler(eventListener);
        socket.on(Event.MESSAGE, notificationHandler);
        // initialize the connection
        RequestBuilder requestBuilder = client.newRequestBuilder();
        requestBuilder.method(Request.METHOD.GET);
        requestBuilder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        // authentication header
        requestBuilder.header("sessionid", sessionId);
        requestBuilder.uri(eventingUrl(restServerUrl));
        requestBuilder.encoder(new EventSubscriptionEncoder());
        requestBuilder.decoder(new EventNotificationDecoder());
        requestBuilder.transport(Request.TRANSPORT.WEBSOCKET);
        socket.open(requestBuilder.build());
        // submit subscription request
        EventSubscription eventSubscription = new EventSubscription(myEventsOnly, asStringArray(events));
        socket.fire(EventCodecUtil.toJsonString(eventSubscription));
    }

    public void stop() {
        if (socket != null) {
            socket.close();
        }
    }

    private static List<String> asStringArray(SchedulerEvent... schedulerEvents) {
        List<String> result;
        if (schedulerEvents == null) {
            return Collections.emptyList();
        }
        result = Lists.newArrayListWithCapacity(schedulerEvents.length);
        for (SchedulerEvent event : schedulerEvents) {
            result.add(event.name());
        }
        return result;
    }

    private static String eventingUrl(String restServerUrl) {
        return restServerUrl + (restServerUrl.endsWith("/") ? "" : "/") + EVENTING_PATH;
    }

    public static class Builder {
        private SchedulerEventReceiver schedulerEventReceiver = new SchedulerEventReceiver();

        public Builder restServerUrl(String restServerUrl) {
            this.schedulerEventReceiver.restServerUrl = restServerUrl;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.schedulerEventReceiver.sessionId = sessionId;
            return this;
        }

        public Builder schedulerEventListener(SchedulerEventListener eventListener) {
            this.schedulerEventReceiver.eventListener = eventListener;
            return this;
        }

        public Builder myEventsOnly(boolean myEventsOnly) {
            this.schedulerEventReceiver.myEventsOnly = myEventsOnly;
            return this;
        }

        public Builder selectedEvents(SchedulerEvent... events) {
            this.schedulerEventReceiver.events = events;
            return this;
        }

        public SchedulerEventReceiver build() {
            checkState(!isNullOrEmpty(this.schedulerEventReceiver.restServerUrl),
                    "REST Server URL cannot be null or empty");
            checkState(this.schedulerEventReceiver.eventListener != null,
                    "Scheduler Event Listener cannot be null.");
            checkState(!isNullOrEmpty(this.schedulerEventReceiver.sessionId),
                    "Sessiond id cannot be null or empty.");
            return this.schedulerEventReceiver;
        }
    }

    private static class EventNotificationDecoder implements Decoder<String, EventNotification> {

        @Override
        public EventNotification decode(Event event, String message) {
            EventNotification notification = null;
            if (Event.MESSAGE == event) {
                try {
                    notification = EventCodecUtil.fromJsonString(message, EventNotification.class);
                } catch (Exception e) {
                    logger.error(String.format("Cannot construct %s type object from: %n%s",
                            EventNotification.class.getName(), message), e);
                }
            }
            return notification;
        }
    }

    private static class EventSubscriptionEncoder implements Encoder<EventSubscription, String> {
        @Override
        public String encode(EventSubscription subscription) {
            try {
                return EventCodecUtil.toJsonString(subscription);
            } catch (Exception e) {
                throw new RuntimeException("Cannot construct a JSON string from the specified object.", e);
            }
        }
    }

    private static class EventNotificationHandler implements Function<EventNotification> {
        private SchedulerEventListener eventListener;

        EventNotificationHandler(SchedulerEventListener eventListener) {
            this.eventListener = eventListener;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void on(EventNotification eventData) {
            Action action = eventData.getAction();
            switch (action) {
            case NONE:
                break;
            case SCHEDULER_STATE_UPDATED:
                eventListener
                        .schedulerStateUpdatedEvent(SchedulerEvent.valueOf(eventData.getSchedulerEvent()));
                break;
            case JOB_SUBMITTED:
                eventListener.jobSubmittedEvent(DataUtility.toJobState((JobStateData) eventData.getData()));
                break;
            case JOB_STATE_UPDATED:
                eventListener.jobStateUpdatedEvent(new NotificationData<JobInfo>(SchedulerEvent
                        .valueOf(eventData.getSchedulerEvent()), DataUtility
                        .toJobInfo((JobInfoData) eventData.getData())));
            case TASK_STATE_UPDATED:
                eventListener.taskStateUpdatedEvent(new NotificationData<TaskInfo>(SchedulerEvent
                        .valueOf(eventData.getSchedulerEvent()), DataUtility
                        .taskInfo((TaskInfoData) eventData.getData())));
            case USERS_UPDATED:
                eventListener.usersUpdatedEvent((NotificationData<UserIdentification>) eventData.getData());
            default:
                throw new RuntimeException(String.format("Unknown action: %s", action));
            }
        }
    }
}
