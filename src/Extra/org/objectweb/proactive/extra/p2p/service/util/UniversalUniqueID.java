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
package org.objectweb.proactive.extra.p2p.service.util;

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
            Long.toHexString(_RandomForSpace.nextLong()) + "-" + Long.toHexString(_RandomForTime.nextLong());
        return new UniversalUniqueID(uniqueId);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
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
    @Override
    public int hashCode() {
        return this.uniqueId.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.uniqueId;
    }
}
