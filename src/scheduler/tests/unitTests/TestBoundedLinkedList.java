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
package unitTests;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.utils.BoundedLinkedList;


/**
 * Test the bounded linked list class.
 * The list is a circular bounded linked list. It ensure that the size will never be greater than
 * the size given as parameter. The elements are added and removed at the head.
 * If the list is full, the next element is added to the head and the last element is removed.
 * Feel free to add more test or behavior.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class TestBoundedLinkedList {

    @Test
    public void run() throws Throwable {
        log("Test with fixed values");
        BoundedLinkedList<Integer> list = new BoundedLinkedList<Integer>(5);
        Assert.assertEquals(0, list.size());
        list.add(10);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(10, list.element());
        Assert.assertEquals(10, list.getFirst());
        Assert.assertEquals(10, list.peek());
        list.add(20);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(20, list.element());
        Assert.assertEquals(20, list.getFirst());
        Assert.assertEquals(20, list.peek());
        list.add(30);
        list.add(40);
        list.add(50);
        Assert.assertEquals(5, list.size());
        Assert.assertEquals(50, list.element());
        Assert.assertEquals(50, list.getFirst());
        Assert.assertEquals(50, list.peek());
        list.remove();
        Assert.assertEquals(4, list.size());
        Assert.assertEquals(40, list.element());
        Assert.assertEquals(40, list.getFirst());
        Assert.assertEquals(40, list.peek());
        Assert.assertEquals(40, list.poll());//also remove element
        list.remove();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(20, list.element());
        Assert.assertEquals(10, list.getLast());
        list.add(60);
        list.add(70);
        list.add(80);
        list.add(90);
        list.add(100);
        Assert.assertEquals(5, list.size());
        Assert.assertEquals(100, list.element());
        Assert.assertEquals(60, list.getLast());
        list.addAll(1, list);
        Assert.assertEquals(5, list.size());
        Assert.assertEquals(100, list.getFirst());
        Assert.assertEquals(70, list.getLast());
        try {
            list.add(5, 12);//must throw the exception
            Assert.assertTrue(false);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            list.addAll(5, list);//must throw the exception
            Assert.assertTrue(false);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        Assert.assertEquals(5, list.getBound());
        list.setBound(7);
        Assert.assertEquals(7, list.getBound());
        Assert.assertEquals(5, list.size());
        list.add(111);
        Assert.assertEquals(111, list.element());
        Assert.assertEquals(70, list.getLast());
        Assert.assertEquals(6, list.size());
        list.add(222);
        list.add(333);
        Assert.assertEquals(333, list.element());
        Assert.assertEquals(80, list.getLast());
        Assert.assertEquals(7, list.size());
    }

    private void log(String s) {
        System.out.println("------------------------------ " + s);
    }

}
