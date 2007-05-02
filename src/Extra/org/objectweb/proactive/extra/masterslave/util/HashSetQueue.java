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
package org.objectweb.proactive.extra.masterslave.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Queue;


/**
 * This collection provides both Set and Queue functionnalities <br/>
 * @author fviale
 *
 * @param <T> type of the elements contained in the Queue
 */
public class HashSetQueue<T> extends LinkedHashSet<T> implements Queue<T> {

    /* (non-Javadoc)
     * @see java.util.Queue#element()
     */
    public T element() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        Iterator<T> it = iterator();
        return it.next();
    }

    /* (non-Javadoc)
     * @see java.util.Queue#offer(java.lang.Object)
     */
    public boolean offer(T o) {
        return add(o);
    }

    /* (non-Javadoc)
     * @see java.util.Queue#peek()
     */
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        Iterator<T> it = iterator();
        return it.next();
    }

    /* (non-Javadoc)
     * @see java.util.Queue#poll()
     */
    public T poll() {
        if (isEmpty()) {
            return null;
        }
        Iterator<T> it = iterator();
        T t = it.next();
        it.remove();
        return t;
    }

    /* (non-Javadoc)
     * @see java.util.Queue#remove()
     */
    public T remove() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        Iterator<T> it = iterator();
        T t = it.next();
        it.remove();
        return t;
    }
}
