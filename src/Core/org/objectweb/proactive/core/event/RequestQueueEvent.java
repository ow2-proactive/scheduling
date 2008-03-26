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

import org.objectweb.proactive.core.UniqueID;


/**
 * <p>
 * A <code>RequestQueueEvent</code> occurs when a <code>RequestQueue</code> get modified.
 * </p>
 *
 * @see org.objectweb.proactive.core.body.request.RequestQueue
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class RequestQueueEvent extends ProActiveEvent implements java.io.Serializable {
    public static final int ADD_REQUEST = 10;
    public static final int REMOVE_REQUEST = 40;
    public static final int WAIT_FOR_REQUEST = 60;

    /** id of the object owner of the Queue */
    protected UniqueID ownerID;

    /**
     * Creates a new <code>RequestQueueEvent</code> based on the given owner id and type
     * @param ownerID the id of the owner of the <code>RequestQueue</code> in which the event occured
     * @param type the type of the event that occured
     */
    public RequestQueueEvent(UniqueID ownerID, int type) {
        super(ownerID, type);
        this.ownerID = ownerID;
    }

    /**
     * Returns the id of the owner of the <code>RequestQueue</code> in which the event occured
     * @return the id of the owner of the <code>RequestQueue</code> in which the event occured
     */
    public UniqueID getOwnerID() {
        return ownerID;
    }
}
