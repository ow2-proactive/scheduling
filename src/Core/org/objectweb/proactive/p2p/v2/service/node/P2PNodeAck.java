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
package org.objectweb.proactive.p2p.v2.service.node;

import java.io.Serializable;


/**
 *
 * @author Alexandre di Costanzo
 *
 * Created on May 19, 2005
 */
public class P2PNodeAck implements Serializable {
    private boolean bool = false;

    /**
     * The ProActive no arg constructor.
     */
    public P2PNodeAck() {
        // empty
    }

    /**
     * Construct a new P2PNodeAck with the specified value.
     * @param bool the boolean value.
     */
    public P2PNodeAck(boolean bool) {
        this.bool = bool;
    }

    /**
     * Returns the value of this <code>P2PNodeAck</code> object as a boolean
     * primitive.
     * @return the primitive <code>boolean</code> value of this object.
     */
    public boolean ackValue() {
        return this.bool;
    }
}
