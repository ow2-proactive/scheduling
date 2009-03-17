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

import java.io.Serializable;

import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;


/**
 * NotificationData is used by the Scheduler Core to notify the front-end of any changes.
 * It is sent through the method in {@link SchedulerStateUpdate} interface.
 *
 * @param T the type of the data to be sent in the notification.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class NotificationData<T extends Object> implements Serializable {

    /** Event type of the update */
    private SchedulerEvent eventType;
    /** The data to be sent in the update */
    private T data;

    /**
     * Create a new instance of NotificationData.
     *
     * @param eventType
     * @param data
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

}
