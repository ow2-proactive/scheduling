/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.common.event;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Upper class for RM's event objects
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@MappedSuperclass
@Entity
@Table(name = "RMEvent")
public class RMEvent implements Serializable {

    /**  */
	private static final long serialVersionUID = 21L;
	@Id
    @GeneratedValue
    @SuppressWarnings("unused")
    protected long id;
    /** Resource manager URL */
    @Column(name = "rmurl")
    private String RMUrl = null;
    @Column(name = "type")
    protected RMEventType type;
    /** the resource manager client which initiates the event */
    @Column(name = "initiator")
    protected String initiator;
    @Column(name = "timeStamp")
    protected long timeStamp;

    /**
     * ProActive empty constructor
     */
    public RMEvent() {
    }

    /**
     * Creates the node event object.
     */
    public RMEvent(RMEventType type) {
        this.type = type;
        this.timeStamp = System.currentTimeMillis();
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

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.type + "[" + this.RMUrl + "]";
    }
}
