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

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * NotificationData is used by the Scheduler Core to notify the front-end of any changes.
 * It is sent through notification methods in core interface.
 *
 * @param T the type of the data to be sent in the notification.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
public class NotificationData<T extends Object> implements Serializable {

    /** Event type of the update */
    private SchedulerEvent eventType;

    /** The data to be sent in the update */
    private T data;

    /**
     * Create a new instance of NotificationData.
     *
     * @param eventType the Type of the event.
     * @param data the data contained in the notification
     */
    public NotificationData(SchedulerEvent eventType, T data) {
        this.eventType = eventType;
        this.data = data;
    }

    /**
     * Get the Type of the event.
     *
     * @return the Type of the event.
     */
    public SchedulerEvent getEventType() {
        return eventType;
    }

    /**
     * Get the data.
     *
     * @return the data.
     */
    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "NotificationData{" + "eventType=" + eventType + ", data=" + data + '}';
    }
}
