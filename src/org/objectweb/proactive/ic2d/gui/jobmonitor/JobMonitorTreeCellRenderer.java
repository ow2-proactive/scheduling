package org.objectweb.proactive.ic2d.gui.jobmonitor;

import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeModel;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeNode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


public class JobMonitorTreeCellRenderer extends DefaultTreeCellRenderer
    implements JobMonitorConstants {
    private static Font highlightedFont = null;

    private void setAttributes(boolean highlighted, boolean deleted) {
        if (highlighted && (highlightedFont == null)) {
            highlightedFont = getFont().deriveFont(Font.BOLD);
        }

        setFont(highlighted ? highlightedFont : null);
        setForeground(deleted ? Color.LIGHT_GRAY : Color.BLACK);
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
        return this;
    }
}
