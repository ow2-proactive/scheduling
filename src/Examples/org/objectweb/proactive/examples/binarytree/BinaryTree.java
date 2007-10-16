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
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A standard implementation of a node in a binary tree.
 */
public class BinaryTree {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    protected int key; // The key for accessing the value contained in this node
    protected Object value; // The actual value contained in this node

    // Convenience instance variable. One could prove this class invariant:
    // (this.isLeaf == ((this.leftTree==null) && (this.rightTree==null)))
    protected boolean isLeaf;

    // The two subtrees
    protected BinaryTree leftTree;
    protected BinaryTree rightTree;

    /**
     *   Creates an empty node
     */
    public BinaryTree() {
        this.isLeaf = true; // On creation, the node does not yet have any child
    }

    /**
     *   Inserts a (key, value) pair in the subtree that has this node as its root
     *   If this node is a leaf
     */
    public void put(int key, Object value) {
        // This node is empty, let's use it
        if (this.isLeaf) {
            this.key = key;
            this.value = value;
            this.isLeaf = false;
            this.createChildren();
        } else if (key == this.key) {
            // Replaces the current value with a new one
            this.value = value;
        } else if (key < this.key) {
            // smaller keys are on the left,
            this.leftTree.put(key, value);
        } else {
            // greater keys on the right,
            this.rightTree.put(key, value);
        }
    }

    public ObjectWrapper get(int key) {
        //System.out.println("Get of " + key + " in node " + this.key);
        if (this.isLeaf) {
            // We have reached a leaf of the tree and no key matching the parameter 'key'
            // has been found. This is a miss.
            return new ObjectWrapper("null");
        }
        if (key == this.key) {
            // We have found the node that contains the value we're looking for
            return new ObjectWrapper(this.value);
        }
        if (key < this.key) {
            // The current key is greater than the search key, let's continue on the left
            ObjectWrapper res = this.leftTree.get(key);
            return res;
        }

        // The current key is smaller than the search key, let's continue on the right
        ObjectWrapper res = this.rightTree.get(key);
        return res;
    }

    /**
     *   Creates two empty leaves as children
     */
    protected void createChildren() {
        this.leftTree = new BinaryTree();
        this.rightTree = new BinaryTree();
    }
}
