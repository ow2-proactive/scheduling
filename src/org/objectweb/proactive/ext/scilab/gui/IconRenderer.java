package org.objectweb.proactive.ext.scilab.gui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class IconRenderer extends DefaultTableCellRenderer {
	JLabel res = new JLabel();

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
     
		res.setHorizontalAlignment(JLabel.CENTER);
		if (value instanceof ImageIcon) {
			ImageIcon icon = (ImageIcon) value;
			res.setIcon(icon);
		}
		return res;
	}
}