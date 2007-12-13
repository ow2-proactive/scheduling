/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.event;

/**
 * <p>
 * Base class of all events occuring in ProActive. <code>ProActiveEvent</code>
 * provides a event type and a timestamp.
 * </p><p>
 * Should be subclassed to create more specific events.
 * </p>
 *
 * @see java.util.EventObject
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class ProActiveEvent extends java.util.EventObject implements java.io.Serializable {
    public static final int GENERIC_TYPE = -1;

    /** type of the message */
    protected int type;

    /** The timestamp */
    protected long timeStamp;

    /**
     * Creates a new <code>ProActiveEvent</code> based on the given object and type
     * @param obj the object originating of the event
     * @param type the type of the event
     */
    public ProActiveEvent(Object obj, int type) {
        super(obj);
        this.timeStamp = System.currentTimeMillis();
        this.type = type;
    }

    /**
     * Returns the time this event was created
     * @return the time this event was created
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns the type of this event
     * @return the type of this event
     */
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ProActiveEvent@" + timeStamp + " type=" + type;
    }
}
