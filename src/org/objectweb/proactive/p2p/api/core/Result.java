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
package org.objectweb.proactive.p2p.api.core;

import org.objectweb.proactive.p2p.api.exception.NoResultsException;

import java.io.Serializable;


/**
 * @author Alexandre di Costanzo
 *
 * Created on May 2, 2005
 */
public class Result implements Serializable {
    private Object theResult = null;
    private Exception exception = null;

    /**
     * Construct an empty result.
     */
    public Result() {
    }

    /**
     * Construct a new result with an attached value. The value must implement Comparable.
     * @param theResult the value of the result.
     */
    public Result(Object theResult) {
        assert theResult instanceof Comparable;
        this.theResult = theResult;
    }

    public Result(Exception e) {
        this.exception = e;
    }

    /**
     * @return the value of the result or <code>null</code> if no value is
     * attached.
     */
    public Object getResult() throws NoResultsException {
        return this.theResult;
    }

    public Exception getException() {
        return this.exception;
    }

    /**
     * Attach a value to this result. The value must implement Comparable.
     * @param theResult the value.
     */
    public void setResult(Object theResult) {
        assert theResult instanceof Comparable;
        this.theResult = theResult;
    }

    /**
     * Compare 2 results and return which is the best. Use compareTo from java.lang.Comparable.
     * The least is returned.
     * @param other the other result.
     * @return the best result.
     */
    public Result returnTheBest(Result other) {
        if (((Comparable) this.theResult).compareTo(other.theResult) == -1) {
            return this;
        } else {
            return other;
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.theResult.toString();
    }

    /**
     * Compare 2 results.
     * @param other the other result.
     * @return <code>true</code> this is better than the other, else returns <code>false</code>.
     */
    public boolean isBetterThan(Result other) {
        if (((Comparable) this.theResult).compareTo(other.theResult) == -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return <code>true</code> if this result contains an exception, else <code>false</code>.
     */
    public boolean isAnException() {
        return this.exception != null;
    }
}
