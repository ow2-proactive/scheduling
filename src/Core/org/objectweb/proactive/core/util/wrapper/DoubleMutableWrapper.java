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
package org.objectweb.proactive.core.util.wrapper;

import java.io.Serializable;


/**
 * <p>An reifiable object for wrapping the primitive Java type <code>double</code>.</p>
 * <p>Use this class as result for ProActive asynchronous method calls.</p>
 *
 * @author Alexandre di Costanzo
 *
 * Created on Jul 28, 2005
 */
public class DoubleMutableWrapper implements Serializable {

    /**
     * The primitive value.
     */
    private double value;

    /**
     * The no arguments constructor for ProActive.
     */
    public DoubleMutableWrapper() {
        // nothing to do
    }

    /**
     * Construct an reifiable object for a <code>double</code>.
     * @param value the primitive <code>double</code> value.
     */
    public DoubleMutableWrapper(double value) {
        this.value = value;
    }

    /**
     * Return the value of the <code>double</code>.
     * @return the primitive value.
     */
    public double doubleValue() {
        return this.value;
    }

    /**
     * Set the value with a new one.
     * @param value the new value.
     */
    public void setDoubleValue(double value) {
        this.value = value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.value + "";
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof DoubleMutableWrapper) {
            return ((DoubleMutableWrapper) arg0).doubleValue() == this.value;
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new Double(this.value).hashCode();
    }
}
