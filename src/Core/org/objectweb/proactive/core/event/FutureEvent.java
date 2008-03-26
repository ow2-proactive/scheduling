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
 * A <code>FutureEvent</code> occurs when a <code>FuturProxy</code>
 * blocks the executing Thread because the result is not yet available.
 * </p>
 *
 * @see org.objectweb.proactive.core.body.future.FutureProxy
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class FutureEvent extends ProActiveEvent implements java.io.Serializable {

    /** Created when a Thread is blocked. */
    public static final int WAIT_BY_NECESSITY = 10;

    /** Created when a Thread continues */
    public static final int RECEIVED_FUTURE_RESULT = 20;
    private UniqueID creatorID;

    /**
     * Creates a new <code>FutureEvent</code> based on the given FutureProxy
     * @param bodyID the <code>UniqueID</code> of the body that is waiting for the future result
     * @param creatorID the <code>UniqueID</code> of the body that created
     * the corresponding <code>Future</code>
     * @param type the type of the event that occured
     */
    public FutureEvent(UniqueID bodyID, UniqueID creatorID, int type) {
        super(bodyID, type);
        this.creatorID = creatorID;
    }

    /**
     * Returns the <code>UniqueID</code> of the body that created the corresponding <code>Future</code>
     * @return the <code>UniqueID</code> of the body that created the corresponding <code>Future</code>
     */
    public UniqueID getCreatorID() {
        return creatorID;
    }

    /**
     * Returns the <code>UniqueID</code> of the body that is waiting
     * @return the <code>UniqueID</code> of the body that is waiting
     */
    public UniqueID getBodyID() {
        return (UniqueID) getSource();
    }

    @Override
    public String toString() {
        return "FutureEvent bodyID=" + getBodyID() + " creatorID=" + getCreatorID();
    }
}
