package org.objectweb.proactive.ic2d.gui.jobmonitor;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeNode;

public class JobMonitorTreeCellRenderer extends DefaultTreeCellRenderer implements JobMonitorConstants
{
	public Component getTreeCellRendererComponent (JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,boolean hasFocus)
	{
		super.getTreeCellRendererComponent (tree, value, sel, expanded, leaf, row, hasFocus);
		
		DataTreeNode currentNode = (DataTreeNode) value;
		int key = currentNode.getKey();
		Icon icon = Icons.getIconForKey(key);		
		if (icon != null)
			setIcon (icon);
		
		return this;
	}
}
