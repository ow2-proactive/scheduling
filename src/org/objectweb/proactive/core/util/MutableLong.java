/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.core.util;

import java.io.Serializable;


/**
 * Defines a mutable, serializable and not final Long.
 * @author cdelbe
 */
public class MutableLong implements Serializable {
    private long value;

    public MutableLong() {
    }

    public MutableLong(long value) {
        this.value = value;
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long add(long toAdd) {
        this.value += toAdd;
        return this.value;
    }

    public boolean isLessThan(MutableLong l) {
        return this.value < l.getValue();
    }

    public int hashCode() {
        return (int) this.value;
    }

    public boolean equals(Object mi) {
        if (mi instanceof MutableLong) {
            return this.value == ((MutableLong) mi).getValue();
        } else {
            return false;
        }
    }
}
