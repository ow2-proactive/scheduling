/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import javax.swing.tree.DefaultTreeModel;

import org.objectweb.proactive.ic2d.gui.jobmonitor.*;


/*
 * The TreeModel is built on top of the DataAssociation and feeded with a JTree
 * to java which will display it.
 * The 4 instances of the model (4 views) are always the same, they are just rebuilt
 * when the underlying data changes.
 * The model contains a DataTreeNode which is the root and itself contains DataTreeNode which
 * are its children ...
 */
public class DataTreeModel extends DefaultTreeModel
    implements JobMonitorConstants {
    private DataAssociation asso;
    private DataModelTraversal traversal;

    public DataTreeModel(DataAssociation _asso, DataModelTraversal _traversal) {
        super(new DataTreeNode(_traversal));
        asso = _asso;
        traversal = _traversal;
    }

    public DataTreeNode root() {
        return (DataTreeNode) getRoot();
    }

    public void rebuild() {
        rebuild(root());
    }

    public void rebuild(DataTreeNode node) {
        node.setAllRemovedStates();
        node.rebuild(this, node.getObject(), node.makeConstraints());
    }

    public DataModelTraversal getTraversal() {
        return traversal;
    }

    public DataAssociation getAssociations() {
        return asso;
    }

    public void setHighlighted(int key, boolean highlight) {
        traversal.setHighlighted(key, highlight);
        root().keyDisplayChanged(this, key);
    }

    public boolean isHighlighted(int key) {
        return traversal.isHighlighted(key);
    }

    public void setHidden(int key, boolean hide) {
        traversal.setHidden(key, hide);
        rebuild();
    }

    public boolean isHidden(int key) {
        return traversal.isHidden(key);
    }

    public void exchange(int fromKey, int toKey) {
        traversal.exchange(fromKey, toKey);
        rebuild();
    }

    public int getNbKey() {
        return traversal.getNbKey();
    }

    public Branch getBranch(int index) {
        return traversal.getBranch(index);
    }

    public int indexOfKey(int key) {
        return traversal.indexOf(key);
    }
}
