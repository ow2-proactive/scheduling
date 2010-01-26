/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.ow2.proactive.resourcemanager.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;


/**
 * Heap simple implementation
 * @author The ProActive Team
 *
 * @param <E> a {@link Comparable} class
 */
public class Heap<E extends Comparable<? super E>> {
    private Object[] nodes_; // the tree nodes, packed into an array
    private int count_ = 0; // number of used slots
    private final Comparator<? super E> cmp_; // for ordering

    /**
     * Create a Heap with the given initial capacity and comparator
     * @param capacity initial capacity.
     * @param cmp a comparator object used to sort heap's elements.
     * @throws IllegalArgumentException if capacity less or equal to zero.
     */
    public Heap(int capacity, Comparator<? super E> cmp) throws IllegalArgumentException {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }

        nodes_ = new Object[capacity];
        cmp_ = cmp;
    }

    /**
     * Create a Heap with the given capacity,
     * and relying on natural ordering.
     * @param capacity
     */
    public Heap(int capacity) {
        this(capacity, null);
    }

    /** perform element comaprisons using comparator or natural ordering **/
    protected int compare(E a, E b) {
        if (cmp_ == null) {
            return a.compareTo(b);
        } else {
            return cmp_.compare(a, b);
        }
    }

    // indexes of heap parents and children
    private final int parent(int k) {
        return (k - 1) / 2;
    }

    private final int left(int k) {
        return (2 * k) + 1;
    }

    private final int right(int k) {
        return 2 * (k + 1);
    }

    /**
     * insert an element, resize if necessary
     * @param x Element to insert.
     */
    @SuppressWarnings("unchecked")
    public synchronized void insert(E x) {
        if (count_ >= nodes_.length) {
            int newcap = ((3 * nodes_.length) / 2) + 1;
            Object[] newnodes = new Object[newcap];
            System.arraycopy(nodes_, 0, newnodes, 0, nodes_.length);
            nodes_ = newnodes;
        }

        int k = count_;
        ++count_;

        while (k > 0) {
            int par = parent(k);

            if (compare(x, (E) nodes_[par]) < 0) {
                nodes_[k] = nodes_[par];
                k = par;
            } else {
                break;
            }
        }

        nodes_[k] = x;
    }

    /**
     * Return and remove least element, or null if empty
     * @return the extracted element.
     */
    @SuppressWarnings("unchecked")
    public synchronized E extract() {
        if (count_ < 1) {
            return null;
        }

        int k = 0; // take element at root;
        Object least = nodes_[k];
        --count_;

        Object x = nodes_[count_];
        nodes_[count_] = null;

        for (;;) {
            int l = left(k);

            if (l >= count_) {
                break;
            } else {
                int r = right(k);
                int child = ((r >= count_) || (compare((E) nodes_[l], (E) nodes_[r]) < 0)) ? l : r;

                if (compare((E) x, (E) nodes_[child]) > 0) {
                    nodes_[k] = nodes_[child];
                    k = child;
                } else {
                    break;
                }
            }
        }

        nodes_[k] = x;

        return (E) least;
    }

    /**
     * Return least element without removing it, or null if empty 
     * @return least element.
     */
    @SuppressWarnings("unchecked")
    public synchronized E peek() {
        if (count_ > 0) {
            return (E) nodes_[0];
        } else {
            return null;
        }
    }

    /**
     * Return number of elements
     * @return heap's size.
     */
    public synchronized int size() {
        return count_;
    }

    /** remove all elements **/
    public synchronized void clear() {
        for (int i = 0; i < count_; ++i)
            nodes_[i] = null;

        count_ = 0;
    }

    /**
     * Return a String representation of the heap.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        int nBlanks = 32;
        int itemsPerRow = 1;
        int column = 0;
        int currentIndex = 0;

        while (count_ > 0) {
            if (column == 0) {
                for (int k = 0; k < nBlanks; k++)
                    str.append(" ");
            }

            str.append(nodes_[currentIndex].toString());

            if (++currentIndex == count_) { // done?
                break;
            }

            if (++column == itemsPerRow) // end of row?
            {
                nBlanks /= 2;
                itemsPerRow *= 2;
                column = 0;
                str.append("\n");
            } else {
                for (int k = 0; k < ((nBlanks * 2) - 2); k++)
                    str.append(" "); // interim blanks
            }
        }

        return str.toString();
    }

    /**
     * Add an element to the heap
     * @param e element to add
     * @return true 
     */
    public boolean add(E e) {
        insert(e);

        return true;
    }

    /**
     * Add a Collection of element into the heap.
     * @param c collection to add
     * @return true;
     */
    public boolean addAll(Collection<? extends E> c) {
        for (E e : c)
            insert(e);

        return true;
    }

    /**
     * Check if an element is stored in the heap.
     * @param o element to check
     * @return true if the element is stored in the heap, false otherwise.
     */
    public boolean contains(Object o) {
        return Arrays.binarySearch(nodes_, o) >= 0;
    }

    /**
     * check if the heap contains all element of a collection.
     * @param c collection to check
     * @return true if the whole collection is stored in the heap.
     */
    public boolean containsAll(Collection<?> c) {
        for (Object o : c)
            if (!contains(o)) {
                return false;
            }

        return true;
    }

    /**
     * Check if the heap is empty.
     * @return true if the heap is empty, false otherwise.
     */
    public boolean isEmpty() {
        return count_ == 0;
    }
}
