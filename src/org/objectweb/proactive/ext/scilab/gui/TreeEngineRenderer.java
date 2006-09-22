package org.objectweb.proactive.ext.scilab.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeEngineRenderer extends DefaultTreeCellRenderer {
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
		      boolean selected, boolean expanded, boolean leaf, int row,
		      boolean hasFocus) {

		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    	
		    if (value instanceof TreeEngineNode) {
		    	
		    	TreeEngineNode nodeEngine = ((TreeEngineNode) value);
		    	renderer.setText(nodeEngine.toString());
		    	if(!nodeEngine.isLeaf() && !nodeEngine.isRoot() && nodeEngine.getState() == TreeEngineNode.SUSPECT){
		    		renderer.setForeground(Color.RED);
		    	}else{
		    		renderer.setForeground(Color.BLACK);
		    	}
		    }
		    
		    return renderer;
	}
}
