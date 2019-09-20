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
package org.ow2.proactive.resourcemanager.common.event;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Upper class for RM's event objects
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class RMEvent implements Serializable, Cloneable, Comparable<RMEvent>, SortedUniqueSet.Unique {

    /** Resource manager URL */
    private String RMUrl = null;

    protected RMEventType type;

    /** the resource manager client which initiates the event */
    protected String initiator;

    protected long timeStamp;

    /** event count sent to this client during the session */
    protected long counter;

    /** smallest counter among the set of events with equal nodeUrl or nodeSourceName
     * smallest possible counter is zero so that is why we initilize it like this
     * */
    protected long firstCounter = 0;

    /**
     * ProActive empty constructor
     */
    public RMEvent() {
    }

    public RMEvent(long counter) {
        this.counter = counter;
    }

    /**
     * Creates the node event object.
     */
    public RMEvent(RMEventType type) {
        this.type = type;
        this.timeStamp = System.currentTimeMillis();
    }

    void updateFirstCounter(RMEvent another) {
        this.firstCounter = Math.min(this.firstCounter, another.firstCounter);
    }

    public long getFirstCounter() {
        return firstCounter;
    }

    /**
     * Returns the RM's URL of the event.
     * @return node source type of the event.
     */
    public String getRMUrl() {
        return this.RMUrl;
    }

    /**
     * Set the RM's URL of the event.
     * @param RMURL URL of the RM to set
     */
    public void setRMUrl(String RMURL) {
        this.RMUrl = RMURL;
    }

    /**
     * Gets the type of event @see RMEventType
     * @return the type of event
     */
    public RMEventType getEventType() {
        return type;
    }

    /**
     * Gets the time of event creation
     * @return the event creation time
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Gets the formatted time of event creation
     * @return the formatted event creation time
     */
    public String getTimeStampFormatted() {
        return new SimpleDateFormat().format(new Date(timeStamp));
    }

    @Override
    public String getKey() {
        return Long.toString(getCounter());
    }

    /**
     * Gets the number of events sent to a client during the current session.
     * @return the number of events sent to a client during the current session
     */
    public long getCounter() {
        return counter;
    }

    /**
     * Sets the number of events sent to a client during the current session.
     * @param counter to set
     */
    public void setCounter(long counter) {
        this.counter = counter;
        this.firstCounter = counter;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.type + ((counter > 0) ? " counter: " + counter + " " : "") + "[" + this.RMUrl + "]";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RMEvent rmEvent = (RMEvent) o;
        return counter == rmEvent.counter;
    }

    @Override
    public int hashCode() {
        return (int) (counter ^ (counter >>> 32));
    }

    @Override
    public int compareTo(RMEvent event) {
        return Long.compare(this.counter, event.counter);
    }

}
