/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import java.util.Collection;
import java.util.LinkedList;


/**
 * BoundedLinkedList is a list able to manage history.
 * Elements are added and removed from the head.
 * It ensures the size is never greater than the maximum size given in the constructor.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class BoundedLinkedList<E> extends LinkedList<E> {

    private int size;

    /**
     * Create a new instance of Bounded Linked List
     *
     * @param size the size of this bounded list
     */
    public BoundedLinkedList(int size) {
        this.size = size;
    }

    /**
     * Add the element at the first position (head).
     * Ensure that the size remains lower than or equal to the size given in the constructor.
     *
     * @see java.util.LinkedList#add(java.lang.Object)
     */
    @Override
    public boolean add(E o) {
        super.addFirst(o);
        if (this.size() > size) {
            super.removeLast();
        }
        return true;
    }

    /**
     * Add the element at the specified index.
     * Ensure that the size remains lower than or equal to the size given in the constructor.
     *
     * @see java.util.LinkedList#add(int, java.lang.Object)
     */
    @Override
    public void add(int index, E element) {
        if (index == size)
            throw new ArrayIndexOutOfBoundsException("Given index : " + index + ", max size : " + size);
        super.add(index, element);
        if (this.size() > size) {
            super.removeLast();
        }
    }

    /**
     * Add all the elements at the first position (head).
     * Ensure that the size remains lower than or equal to the size given in the constructor.
     *
     * @see java.util.LinkedList#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.addAll(0, c);
    }

    /**
     * Add all the elements at the specified position.
     * Ensure that the size remains lower than or equal to the size given in the constructor.
     *
     * @see java.util.LinkedList#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (index == size)
            throw new ArrayIndexOutOfBoundsException("Given index : " + index + ", max size : " + size);
        boolean ret = super.addAll(index, c);
        if (ret) {
            for (int i = 0; i < c.size(); i++) {
                if (this.size() > size) {
                    super.removeLast();
                } else {
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Get the maximum size supported by this linked list.
     *
     * @return the maximum size supported by this linked list.
     */
    public int getBound() {
        return this.size;
    }

    /**
     * Change the maximum size supported by this linked list.
     *
     * @param newSize the new size supported by this linked list.
     */
    public void setBound(int newSize) {
        this.size = newSize;
    }

}
