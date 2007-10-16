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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class Main {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    public static void main(String[] args) {
        Main theMainActiveObject = null;

        // Creates an active instance of this class
        ProActiveConfiguration.load();
        try {
            theMainActiveObject = (Main) org.objectweb.proactive.api.ProActiveObject.newActive(Main.class.getName(),
                    null);
        } catch (Exception e) {
            logger.error(e);
            System.exit(1);
        }

        // Asks it to perform the test
        theMainActiveObject.doStuff();
        return;
    }

    public void doStuff() {
        BinaryTree myTree = null;

        // This is the code for instanciating a passive version of the binary tree
        //        myTree = new BinaryTree ();
        // This is the code for instanciating an active version of the binary tree
        // If you want to test the pasive version of this test program, simply comment out
        // the next line and comment in the line of code above
        //
        // * The first parameter means that we want to get an active instance of class org.objectweb.proactive.examples.binarytree.ActiveBinaryTree
        // * The second parameter ('null') means we instancate this object through its empty (no-arg) constructor
        //  'null' is actually a convenience for 'new Object [0]'
        // * The last parameter 'null' means we want to instanciate this object in the current virtual machine
        try {
            //          Object o = new org.objectweb.proactive.examples.binarytree.ActiveBinaryTree ();
            myTree = (BinaryTree) org.objectweb.proactive.api.ProActiveObject.newActive(ActiveBinaryTree.class.getName(),
                    null);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }

        // Now we insert 4 elements in the tree
        // Note that this code is the same for the passive or active version of the tree
        myTree.put(1, "one");
        myTree.put(2, "two");
        myTree.put(3, "three");
        myTree.put(4, "four");
        // Now we get all these 4 elements out of the tree
        // method get in class BinaryTree returns a future object if
        // myTree is an active object, but as System.out actually calls toString()
        // on the future, the execution of each of the following 4 calls to System.out
        // blocks until the future object is available.
        ObjectWrapper tmp1 = myTree.get(3);
        ObjectWrapper tmp2 = myTree.get(4);
        ObjectWrapper tmp3 = myTree.get(2);
        ObjectWrapper tmp4 = myTree.get(1);
        logger.info("Value associated to key 1 is " + tmp4);
        logger.info("Value associated to key 2 is " + tmp3);
        logger.info("Value associated to key 3 is " + tmp1);
        logger.info("Value associated to key 4 is " + tmp2);
        logger.info("Use CTRL+C to stop the program");
    }
}
