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
package org.objectweb.proactive.ic2d.gui.jobmonitor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeModel;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeNode;


public class JobMonitorTreeCellRenderer extends DefaultTreeCellRenderer
    implements JobMonitorConstants {
    private static Font highlightedFont = null;

    private void setAttributes(boolean highlighted, boolean deleted) {
        if (highlighted && (highlightedFont == null)) {
            highlightedFont = getFont().deriveFont(Font.BOLD);
        }

        setFont(highlighted ? highlightedFont : null);
        setForeground(deleted ? Color.RED : Color.BLACK);
    }

    private void addDeletedTime(DataTreeNode node) {
        BasicMonitoredObject object = node.getObject();
        if (!object.isDeleted()) {
            return;
        }

        DataTreeNode parent = (DataTreeNode) node.getParent();
        BasicMonitoredObject parentObject = parent.getObject();

        if (parentObject.isDeleted()) {
            return;
        }

        setText(node.toString() + " (Unresponding for " +
            object.getDeletedTime() + ")");
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
            row, hasFocus);
        DataTreeNode currentNode = (DataTreeNode) value;
        if (currentNode.getObject() == null) {
            return this;
        }

        int key = currentNode.getKey();
        Icon icon = Icons.getIconForKey(key);
        if (icon != null) {
            setIcon(icon);
        }

        DataTreeModel model = (DataTreeModel) tree.getModel();

        setAttributes(model.isHighlighted(key),
            currentNode.getObject().isDeleted());

        addDeletedTime(currentNode);
        return this;
    }
}
