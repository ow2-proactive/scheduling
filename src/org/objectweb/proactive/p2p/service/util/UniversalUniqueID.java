/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.p2p.service.util;

import java.io.Serializable;
import java.util.Random;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Feb 7, 2005
 */
public class UniversalUniqueID implements Serializable {
    private static Random _RandomForSpace = new Random();
    private static Random _RandomForTime = new Random();
    private String uniqueId = null;

    /**
     * @param uniqueId
     */
    protected UniversalUniqueID(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * @return an universal unique ID.
     */
    public static UniversalUniqueID randomUUID() {
        String uniqueId = Long.toHexString(System.currentTimeMillis()) + "-" +
            Long.toHexString(_RandomForSpace.nextLong()) + "-" +
            Long.toHexString(_RandomForTime.nextLong());
        return new UniversalUniqueID(uniqueId);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof UniversalUniqueID) {
            return this.uniqueId.equals(((UniversalUniqueID) obj).uniqueId);
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.uniqueId.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.uniqueId;
    }
}
