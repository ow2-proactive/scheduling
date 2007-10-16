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
package org.objectweb.proactive.extensions.branchnbound.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.objectweb.proactive.extensions.branchnbound.core.exception.NoResultsException;


/**
 * A wrapper for a solution.
 *
 * @author Alexandre di Costanzo
 *
 * Created on May 2, 2005
 */
public class Result implements Serializable {
    private Object theSolution = null;
    private Exception exception = null;

    /**
     * Construct an empty result.
     */
    public Result() {
    }

    /**
     * Construct a new result with an attached value. The value must implement Comparable.
     * @param theSolution the value of the result.
     */
    public Result(Object theSolution) {
        assert theSolution instanceof Comparable;
        this.theSolution = theSolution;
    }

    /**
     *
     * Construct a new result with an excpetion.
     * @param e the exception.
     */
    public Result(Exception e) {
        this.exception = e;
    }

    /**
     * @return the value of the result or <code>null</code> if no value is
     * attached.
     */
    public Object getSolution() {
        return this.theSolution;
    }

    /**
     * @return the attached exception or <code>null</code> else.
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * Attach a value to this result. The value must implement Comparable.
     * @param theSolution the value.
     */
    public void setSolution(Object theSolution) {
        assert theSolution instanceof Comparable;
        this.theSolution = theSolution;
    }

    /**
     * Compare 2 results and return which is the best. Use compareTo from java.lang.Comparable.
     * The least is returned.
     * @param other the other result.
     * @return the best result.
     */
    public Result returnTheBest(Result other) {
        if (((Comparable) this.theSolution).compareTo(other.getSolution()) <= 0) {
            return this;
        }
        return other;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.theSolution.toString();
    }

    /**
     * Compare 2 results.
     * @param other the other result.
     * @return <code>true</code> this is better than the other, else returns <code>false</code>.
     */
    public boolean isBetterThan(Result other) {
        if (((Comparable) this.theSolution).compareTo(other.theSolution) < 0) {
            return true;
        }
        return false;
    }

    /**
     * @return <code>true</code> if this result contains an exception, else <code>false</code>.
     */
    public boolean isAnException() {
        return this.exception != null;
    }

    // Serialization ----------------------------------------------------------
    private static final String NORESULT = "--No result--";

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (this.theSolution instanceof NoResultsException) {
            out.write(NORESULT.getBytes());
        } else {
            out.writeObject(this.theSolution);
        }
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        Object readObject = in.readObject();
        if (readObject instanceof String &&
                (((String) readObject).compareTo(NORESULT) == 0)) {
            this.theSolution = new NoResultsException();
        } else {
            this.theSolution = readObject;
        }
    }
}
