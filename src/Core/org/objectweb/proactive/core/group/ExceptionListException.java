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
package org.objectweb.proactive.core.group;

import java.util.Iterator;
import java.util.Vector;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * An exception that contains a list of the ExceptionInGroup occured in a group.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class ExceptionListException extends RuntimeException implements Iterable<ExceptionInGroup> {

    /** A vector implements the list */
    private Vector<ExceptionInGroup> list;

    /**
     * Builds a new empty list of exception
     */
    public ExceptionListException() {
        super("Exception list, only one cause in the stacktrace");
        this.list = new Vector<ExceptionInGroup>();
    }

    /**
     * Adds an exception into this list
     * @param exception - the exception to add
     */
    public synchronized void add(ExceptionInGroup exception) {
        if (getCause() == this) {
            initCause(exception);
        }
        this.list.add(exception);
    }

    /**
     * Removes all of the exceptions from this list.
     */
    public void clear() {
        this.list.clear();
    }

    /**
     * Returns an iterator over the exceptions in this list in proper sequence.
     * @return an iterator over the exceptions in this list in proper sequence.
     */
    public Iterator<ExceptionInGroup> iterator() {
        return this.list.iterator();
    }

    /**
     * Returns the number of exceptions in this list.
     * @return the number of exceptions in this list.
     */
    public int size() {
        return this.list.size();
    }

    /**
     * Tests if this ExceptionListException has no ExceptionInGroup.
     * @return <code>true</code> if and only if this list has no components, that is, its size is zero; <code>false otherwise.
     */
    public boolean isEmpty() {
        return this.list.isEmpty();
    }
}
