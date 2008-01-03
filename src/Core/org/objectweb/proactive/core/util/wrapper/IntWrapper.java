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

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <p>An reifiable object for wrapping the primitive Java type <code>int</code>.</p>
 * <p>Use this class as result for ProActive asynchronous method calls.</p>
 *
 * @author Alexandre di Costanzo
 *
 * Created on Jul 28, 2005
 */
@PublicAPI
public class IntWrapper implements Serializable {

    /**
     * The primitive value.
     */
    private int value;

    /**
     * The no arguments constructor for ProActive.
     */
    public IntWrapper() {
        // nothing to do
    }

    /**
     * Construct an reifiable object for a <code>int</code>.
     * @param value the primitive <code>int</code> value.
     */
    public IntWrapper(int value) {
        this.value = value;
    }

    /**
     * Return the value of the <code>int</code>.
     * @return the primitive value.
     */
    public int intValue() {
        return this.value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.value + "";
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof IntWrapper) {
            return ((IntWrapper) arg0).intValue() == this.value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new Integer(this.value).hashCode();
    }
}
