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

public class ActiveBinaryTree extends BinaryTree {

    /*
       Unlike the 'like Current' construction of the Eiffel language,
       it is difficult in Java to create an object of the
       same type as the current object without using heavy
       mechanisms of the Reflection API.
       This is why we cannot completely reuse class BinaryTree because it contains
       an explicit call to the constructor of class BinaryTree, then creating a passive
       object where we want an active one
       This is why we here override the createChildren method
       in order to ensure that all the nodes
       of the binary tree are active objects
       This of course is a bulky implementation, and we could provide
       a more subtle scheme, such as creating active objects only
       for a given depth n of the tree (so that there cannot
       be more that 2**n active objects)
     */
    @Override
    protected void createChildren() {
        try {
            String s = this.getClass().getName();
            this.leftTree = (BinaryTree) org.objectweb.proactive.api.ProActiveObject.newActive(s,
                    null);
            this.rightTree = (BinaryTree) org.objectweb.proactive.api.ProActiveObject.newActive(s,
                    null);
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
