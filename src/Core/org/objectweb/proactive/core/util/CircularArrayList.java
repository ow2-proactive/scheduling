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
package org.objectweb.proactive.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import static junit.framework.Assert.assertTrue;

/**
 * <p>
 * Originally written by Dr. Heinz Kabutz in the very excellent
 * <a href="http://www.smotricz.com/kabutz/">The Java Specialists Newsletter</a>
 * </p><p>
 * Cleaned from many infamous bugs and completed.
 * </p>
 *
 * @author  Heinz Kabutz
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class CircularArrayList extends java.util.AbstractList implements java.util.List,
    java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.UTIL);
    private static final int DEFAULT_SIZE = 5;
    protected Object[] array;

    // head points to the first logical element in the array, and
    // tail points to the element following the last.  This means
    // that the list is empty when head == tail.  It also means
    // that the array array has to have an extra space in it.
    protected int head = 0;

    // head points to the first logical element in the array, and
    // tail points to the element following the last.  This means
    // that the list is empty when head == tail.  It also means
    // that the array array has to have an extra space in it.
    protected int tail = 0;

    // Strictly speaking, we don't need to keep a handle to size,
    // as it can be calculated programmatically, but keeping it
    // makes the algorithms faster.
    protected int size = 0;

    public CircularArrayList() {
        this(DEFAULT_SIZE);
    }

    public CircularArrayList(int size) {
        array = new Object[size];
    }

    public CircularArrayList(java.util.Collection c) {
        size = c.size();
        tail = c.size();
        array = new Object[c.size()];
        c.toArray(array);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CircularArray size=");
        sb.append(size);
        sb.append("\n");
        for (int i = 0; i < size; i++) {
            sb.append("[");
            sb.append(convert(i));
            sb.append("]=>");
            sb.append(array[convert(i)]);
            sb.append(", ");
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public boolean isEmpty() {
        return head == tail; // or size == 0
    }

    // We use this method to ensure that the capacity of the
    // list will suffice for the number of elements we want to
    // insert.  If it is too small, we make a new, bigger array
    // and copy the old elements in.
    public void ensureCapacity(int minCapacity) {
        int oldCapacity = array.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = ((oldCapacity * 3) / 2) + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            Object[] newData = new Object[newCapacity];
            toArray(newData);
            tail = size;
            head = 0;
            array = newData;
        }
    }

    @Override
    public int size() {
        // the size can also be worked out each time as:
        // (tail + array.length - head) % array.length
        return size;
    }

    @Override
    public boolean contains(Object elem) {
        return indexOf(elem) >= 0;
    }

    @Override
    public int indexOf(Object elem) {
        if (elem == null) {
            for (int i = 0; i < size; i++)
                if (array[convert(i)] == null) {
                    return i;
                }
        } else {
            for (int i = 0; i < size; i++)
                if (elem.equals(array[convert(i)])) {
                    return i;
                }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object elem) {
        if (elem == null) {
            for (int i = size - 1; i >= 0; i--)
                if (array[convert(i)] == null) {
                    return i;
                }
        } else {
            for (int i = size - 1; i >= 0; i--)
                if (elem.equals(array[convert(i)])) {
                    return i;
                }
        }
        return -1;
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[size]);
    }

    @Override
    public Object[] toArray(Object[] a) {
        //System.out.println("head="+head+" tail="+tail+" size="+size);
        if (size == 0) {
            return a;
        }
        if (a.length < size) {
            a = (Object[]) java.lang.reflect.Array.newInstance(a.getClass()
                                                                .getComponentType(),
                    size);
        }
        if (head < tail) {
            System.arraycopy(array, head, a, 0, tail - head);
        } else {
            System.arraycopy(array, head, a, 0, array.length - head);
            System.arraycopy(array, 0, a, array.length - head, tail);
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public Object get(int index) {
        rangeCheck(index);
        return array[convert(index)];
    }

    @Override
    public Object set(int index, Object element) {
        modCount++;
        rangeCheck(index);
        int convertedIndex = convert(index);
        Object oldValue = array[convertedIndex];
        array[convertedIndex] = element;
        return oldValue;
    }

    @Override
    public boolean add(Object o) {
        modCount++;
        // We have to have at least one empty space
        ensureCapacity(size + 1 + 1);
        array[tail] = o;
        tail = (tail + 1) % array.length;
        size++;
        return true;
    }

    // This method is the main reason we re-wrote the class.
    // It is optimized for removing first and last elements
    // but also allows you to remove in the middle of the list.
    @Override
    public Object remove(int index) {
        modCount++;
        rangeCheck(index);
        int pos = convert(index);

        // an interesting application of try/finally is to avoid
        // having to use local variables
        try {
            return array[pos];
        } finally {
            array[pos] = null; // Let gc do its work
                               // optimized for FIFO access, i.e. adding to back and
                               // removing from front

            if (pos == head) {
                head = (head + 1) % array.length;
            } else if (pos == tail) {
                tail = (tail - 1 + array.length) % array.length;
            } else {
                if ((pos > head) && (pos > tail)) { // tail/head/pos
                    System.arraycopy(array, head, array, head + 1, pos - head);
                    head = (head + 1) % array.length;
                } else {
                    System.arraycopy(array, pos + 1, array, pos, tail - pos -
                        1);
                    tail = (tail - 1 + array.length) % array.length;
                }
            }
            size--;
        }
    }

    @Override
    public void clear() {
        modCount++;
        // Let gc do its work
        for (int i = 0; i != size; i++) {
            array[convert(i)] = null;
        }
        head = tail = size = 0;
    }

    @Override
    public boolean addAll(java.util.Collection c) {
        modCount++;
        int numNew = c.size();

        // We have to have at least one empty space
        ensureCapacity(size + numNew + 1);
        java.util.Iterator e = c.iterator();
        for (int i = 0; i < numNew; i++) {
            array[tail] = e.next();
            tail = (tail + 1) % array.length;
            size++;
        }
        return numNew != 0;
    }

    @Override
    public void add(int index, Object element) {
        if (index == size) {
            add(element);
            return;
        }
        modCount++;
        rangeCheck(index);
        // We have to have at least one empty space
        ensureCapacity(size + 1 + 1);
        int pos = convert(index);
        if (pos == head) {
            head = (head - 1 + array.length) % array.length;
            array[head] = element;
        } else if (pos == tail) {
            array[tail] = element;
            tail = (tail + 1) % array.length;
        } else {
            if ((pos > head) && (pos > tail)) { // tail/head/pos
                System.arraycopy(array, pos, array, head - 1, pos - head + 1);
                head = (head - 1 + array.length) % array.length;
            } else { // head/pos/tail
                System.arraycopy(array, pos, array, pos + 1, tail - pos);
                tail = (tail + 1) % array.length;
            }
            array[pos] = element;
        }
        size++;
    }

    @Override
    public boolean addAll(int index, java.util.Collection c) {
        boolean result = true;
        Iterator it = c.iterator();
        while (it.hasNext()) {
            result &= this.add(it.next());
        }
        return result;
    }

    // The convert() method takes a logical index (as if head was
    // always 0) and calculates the index within array
    private int convert(int index) {
        return (index + head) % array.length;
    }

    private void rangeCheck(int index) {
        if ((index >= size) || (index < 0)) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " +
                size);
        }
    }

    @SuppressWarnings("unused")
    static public class UnitTestCircularArrayList {
        private CircularArrayList cal;

        @Before
        public void setUp() {
            cal = new CircularArrayList();
        }

        /**
             * Add and remove 50 elements and check that size() is ok
             */
        @Test
        public void addAndRemove() {
            int nbElem = 50;

            for (int i = 0; i < nbElem; i++)
                cal.add(i);
            assertTrue(cal.size() == nbElem);

            for (int i = 0; i < nbElem; i++)
                cal.remove(0);
            assertTrue(cal.size() == 0);
        }

        /**
             * Remove() on an empty list must thrown an {@link IndexOutOfBoundsException} exception
             */
        @Test(expected = IndexOutOfBoundsException.class)
        public void removeTooManyElems() {
            cal.remove(0);
        }

        /**
             * Serialization
             * @throws IOException
             */
        @Test
        public void serialization() throws IOException {
            int nbElem = 50;

            for (int i = 0; i < nbElem; i++)
                cal.add(i);

            CircularArrayList r = (CircularArrayList) Utils.makeDeepCopy(cal);
            assertTrue(r.equals(cal));
        }

        @Test
        public void collectionAsParameter() {
            Collection<Integer> col = new ArrayList<Integer>();
            for (int i = 0; i < 50; i++)
                col.add(i);

            CircularArrayList o = new CircularArrayList(col);

            assertTrue(col.equals(o));

            assertTrue(o.size() == col.size());
        }
    }
}
