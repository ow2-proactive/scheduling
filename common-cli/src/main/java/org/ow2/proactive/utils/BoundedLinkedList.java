/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import java.io.Serializable;
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
public class BoundedLinkedList<E> implements Serializable {

    private static final long serialVersionUID = 60L;

    private LinkedList<E> list;
    private int size;

    /**
     * Create a new instance of Bounded Linked List
     *
     * @param size the size of this bounded list
     */
    public BoundedLinkedList(int size) {
        this.size = size;
        list = new LinkedList<E>();
    }

    /**
     * Add the element at the first position (head).
     * Ensure that the size remains lower than or equal to the size given in the constructor.
     *
     * @see java.util.LinkedList#add(java.lang.Object)
     */
    public boolean add(E o) {
        list.addFirst(o);
        if (list.size() > size) {
            list.removeLast();
        }
        return true;
    }

    /**
     * Add the element at the specified index.
     * Ensure that the size remains lower than or equal to the size given in the constructor.
     *
     * @see java.util.LinkedList#add(int, java.lang.Object)
     */
    public void add(int index, E element) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("Given index : " + index + ", max size : " + size);
        }
        list.add(index, element);
        if (list.size() > size) {
            list.removeLast();
        }
    }

    /**
     * Add all the elements at the first position (head).
     * Ensure that the size remains lower than or equal to the size given in the constructor.
     *
     * @see java.util.LinkedList#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends E> c) {
        return this.addAll(0, c);
    }

    /**
     * Add all the elements at the specified position.
     * Ensure that the size remains lower than or equal to the size given in the constructor.
     *
     * @see java.util.LinkedList#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("Given index : " + index + ", max size : " + size);
        }
        boolean ret = list.addAll(index, c);
        if (ret) {
            for (int i = 0; i < c.size(); i++) {
                if (list.size() > size) {
                    list.removeLast();
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

    /**
     * @see java.util.LinkedList#size()
     */
    public int size() {
        return list.size();
    }

    /**
     * @see java.util.Queue#element()
     */
    public E element() {
        return list.element();
    }

    /**
     * @see java.util.LinkedList#getFirst()
     */
    public E getFirst() {
        return list.getFirst();
    }

    /**
     * @see java.util.LinkedList#peek()
     */
    public E peek() {
        return list.peek();
    }

    /**
     * @see java.util.LinkedList#remove()
     */
    public E remove() {
        return list.remove();
    }

    /**
     * @see java.util.LinkedList#poll()
     */
    public E poll() {
        return list.poll();
    }

    /**
     * @see java.util.LinkedList#getLast()
     */
    public E getLast() {
        return list.getLast();
    }

    /**
     * @see java.util.LinkedList#get(int)
     */
    public E get(int n) {
        return list.get(n);
    }

    /**
     * Convert the current Bounded list into a collection
     * 
     * @return the collection representing this bounded linked list.
     */
    public Collection<E> toCollection() {
        return list;
    }
}
