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
package org.objectweb.proactive.examples.binarytree;

import org.objectweb.proactive.api.ProActiveObject;


public class Tree {
    private String key;
    private String value;
    private Tree left;
    private Tree right;
    private TreeDisplay display;
    private Integer graphicDepth;

    public Tree() {
    }

    public Tree(String key, String value, TreeDisplay display) {
        this.left = null;
        this.right = null;
        this.key = key;
        this.value = value;
        this.display = display;
        display.displayMessage("[" + key + "] Created with value " + value,
            java.awt.Color.blue);
    }

    public void insert(String key, String value, boolean AC) {
        int res = key.compareTo(this.key);
        if (res == 0) {
            // Same key --> Modify the current value
            display.displayMessage("[" + key + "] Replacing " + this.value +
                " with " + value);
            this.value = value;
        } else if (res < 0) {
            display.displayMessage("[" + key + "] trying left");
            // key < this.key --> store left
            if (left != null) {
                left.insert(key, value, AC);
            } else {
                display.displayMessage("[" + key + "] Creating left");
                // Create the new node
                try {
                    left = (Tree) org.objectweb.proactive.api.ProActiveObject.newActive(this.getClass()
                                                                                  .getName(),
                            new Object[] { key, value, display });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Enabled Automatic Continuations
                if (AC) {
                    try {
                        org.objectweb.proactive.api.ProActiveObject.enableAC(org.objectweb.proactive.api.ProActiveObject.getStubOnThis());
                    } catch (java.io.IOException e) {
                        display.displayMessage("Automatic Continuations error!!!",
                            java.awt.Color.red);
                    }
                }
            }
        } else {
            display.displayMessage("[" + key + "] trying right");
            if (right != null) {
                right.insert(key, value, AC);
            } else {
                display.displayMessage("[" + key + "] Creating right");
                try {
                    right = (Tree) org.objectweb.proactive.api.ProActiveObject.newActive(this.getClass()
                                                                                   .getName(),
                            new Object[] { key, value, display });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Enabled Automatic Continuations
                if (AC) {
                    try {
                        org.objectweb.proactive.api.ProActiveObject.enableAC(org.objectweb.proactive.api.ProActiveObject.getStubOnThis());
                    } catch (java.io.IOException e) {
                        display.displayMessage("Automatic Continuations error!!!",
                            java.awt.Color.red);
                    }
                }
            }
        }
    }

    public ObjectWrapper search(String key) {
        display.displayMessage("[" + this.key + "] Searching for " + key);
        if (key == null) {
            return new ObjectWrapper("null");
        }
        ;

        int res = key.compareTo(this.key);
        if (res == 0) {
            display.displayMessage("[" + this.key + "] Found " + key);
            return new ObjectWrapper(value);
        }
        if (res < 0) {
            return (left != null) ? left.search(key) : new ObjectWrapper("null");
        } else {
            return (right != null) ? right.search(key) : new ObjectWrapper(
                "null");
        }
    }

    public void delete() {
        if (right != null) {
            right.delete();
        }
        if (left != null) {
            left.delete();
        }
        ProActiveObject.terminateActiveObject(true);
    }

    public String getKey() {
        return key;
    }

    public java.util.ArrayList<String> getKeys() {
        java.util.ArrayList<String> keys = new java.util.ArrayList<String>();
        if (key != null) {
            keys.add(key);
        }
        if (right != null) {
            keys.addAll(right.getKeys());
        }
        if (left != null) {
            keys.addAll(left.getKeys());
        }
        return keys;
    }

    public String getValue() {
        return value;
    }

    public Tree getLeft() {
        return left;
    }

    public Tree getRight() {
        return right;
    }

    public int depth() {
        int rightDepth = 0;
        int leftDepth = 0;
        if (right != null) {
            rightDepth = right.depth();
        }
        if (left != null) {
            leftDepth = left.depth();
        }
        if (leftDepth < rightDepth) {
            return ++rightDepth;
        }
        return ++leftDepth;
    }

    // Change Automatic Continuations state
    public void enableAC() {
        try {
            org.objectweb.proactive.api.ProActiveObject.enableAC(org.objectweb.proactive.api.ProActiveObject.getStubOnThis());
            if (right != null) {
                right.enableAC();
            }
            if (left != null) {
                left.enableAC();
            }
        } catch (java.io.IOException e) {
            display.displayMessage("Automatic Continuations error!!!",
                java.awt.Color.red);
        }
    }

    public void disableAC() {
        try {
            org.objectweb.proactive.api.ProActiveObject.disableAC(org.objectweb.proactive.api.ProActiveObject.getStubOnThis());
            if (right != null) {
                right.disableAC();
            }
            if (left != null) {
                left.disableAC();
            }
        } catch (java.io.IOException e) {
            display.displayMessage("Automatic Continuations error!!!",
                java.awt.Color.red);
        }
    }
}
