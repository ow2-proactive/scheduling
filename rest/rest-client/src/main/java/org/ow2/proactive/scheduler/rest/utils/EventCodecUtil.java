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
package org.ow2.proactive.scheduler.rest.utils;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.node.ObjectNode;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.SchedulerUserData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventNotification;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventNotification.Action;


public class EventCodecUtil {
    private static final ObjectMapper mapper;

    static {
        SimpleModule module = new SimpleModule("JsonUtilsMapper", new Version(1, 0, 0, null));
        EventNotificationDeserializer deserializer = new EventNotificationDeserializer();
        module.addDeserializer(EventNotification.class, deserializer);
        mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private EventCodecUtil() {
    }

    public static <T> T fromJsonString(String jsonString, Class<T> valueType) {
        try {
            return mapper.readValue(jsonString, valueType);
        } catch (Exception e) {
            throw new RuntimeException("Parser error for jsonString : " + jsonString, e);
        }
    }

    public static String toJsonString(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("Parser error.", e);
        }
    }

    private static class EventNotificationDeserializer extends StdDeserializer<EventNotification> {

        protected EventNotificationDeserializer() {
            super(EventNotification.class);
        }

        @Override
        public EventNotification deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            EventNotification notification = new EventNotification();
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            ObjectNode root = (ObjectNode) mapper.readTree(jp);
            String actionString = root.get("action").getTextValue();
            notification.setAction(Action.valueOf(actionString));
            JsonNode data = root.get("data");
            switch (notification.getAction()) {
                case JOB_SUBMITTED:
                    notification.setData(mapper.readValue(data, JobStateData.class));
                    break;
                case JOB_STATE_UPDATED:
                    notification.setData(mapper.readValue(data, JobInfoData.class));
                    break;
                case JOB_FULL_DATA_UPDATED:
                    notification.setData(mapper.readValue(data, JobStateData.class));
                    break;
                case TASK_STATE_UPDATED:
                    notification.setData(mapper.readValue(data, TaskInfoData.class));
                    break;
                case USERS_UPDATED:
                    notification.setData(mapper.readValue(data, SchedulerUserData.class));
                    break;
                default:
                    break;
            }
            notification.setSchedulerEvent(root.get("schedulerEvent").asText());
            return notification;

        }

    }
}
