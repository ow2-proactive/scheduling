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
package org.objectweb.proactive.core.util.wrapper;

import java.io.Serializable;


/**
 * <p>An reifiable object for wrapping the Java type <code>String</code>.</p>
 * <p>Use this class as result for ProActive asynchronous method calls.</p>
 *
 * @author Alexandre di Costanzo
 *
 * Created on Jul 28, 2005
 */
public class StringMutableWrapper implements Serializable {

    /**
     * The not reifiable value.
     */
    private String value;

    /**
     * The no arguments constructor for ProActive.
     */
    public StringMutableWrapper() {
        // nothing to do
    }

    /**
     * Construct an reifiable object for a <code>String</code>.
     * @param value the class <code>String</code> value.
     */
    public StringMutableWrapper(String value) {
        this.value = value;
    }

    /**
     * Return the value of the <code>String</code>.
     * @return the none reifiable value.
     */
    public String stringValue() {
        return this.value;
    }

    /**
     * Set the value with a new one.
     * @param value the new value.
     */
    public void setStringValue(String value) {
        this.value = value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.value;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        if (arg0 instanceof StringMutableWrapper) {
            return this.value.equals(((StringMutableWrapper) arg0).stringValue());
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.value.hashCode();
    }
}
