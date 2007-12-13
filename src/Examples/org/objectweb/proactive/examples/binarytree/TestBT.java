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
package org.objectweb.proactive.examples.binarytree;

public class TestBT {
    public static void main(String[] args) {
        BinaryTree myTree = new BinaryTree(); // Instantiating a standard version

        // To get an active version, just comment the line above,
        // and comment out the line below
        // myTree = (BinaryTree) Proactive.newActive("ActiveBinaryTree", null, null);
        // * First parameter:   get an active instance of class ActiveBTree
        // * Second ('null'):   instantiates with empty (no-arg) constructor
        //                     'null' is a convenience for 'new Object [0]'
        // * Last ('null'):     instantiates this object on the current host,
        //                     within the current virtual machine
        // Use either standard or active versions through polymorphism
        TestBT.useBTree(myTree);
    }

    // Note that this code is the same for the passive or active version of the tree
    protected static void useBTree(BinaryTree bt) {
        ObjectWrapper s1;
        ObjectWrapper s2;
        bt.put(1, "one"); // We insert 4 elements in the tree, non-blocking
        bt.put(2, "two");
        bt.put(3, "three");
        bt.put(4, "four");
        // Now we get all these 4 elements out of the tree
        // method get in class BinaryTree returns a future object if
        // bt is an active object, but as System.out actually calls toString()
        // on the future, the execution of each of the following 4 calls
        // to System.out blocks until the future object is available.
        System.out.println("Value associated to key 2 is " + bt.get(2));
        System.out.println("Value associated to key 1 is " + bt.get(1));
        System.out.println("Value associated to key 3 is " + bt.get(3));
        System.out.println("Value associated to key 4 is " + bt.get(4));
        // When using variables, all the instructions are non-blocking
        bt.put(2, "twoBis");
        s2 = bt.get(2); // non-blocking
        bt.put(2, "twoTer");
        s2 = bt.get(2); // non-blocking
        s1 = bt.get(1); // non-blocking
        // Blocking operations

        System.out.println("Value associated to key 2 is " + s2); // prints "twoTer"
        System.out.println("Value associated to key 1 is " + s1); // prints "one"
    }
}
