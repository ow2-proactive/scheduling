package org.objectweb.proactive.ic2d.gui.jobmonitor;

import javax.swing.Icon;

public class Branch implements JobMonitorConstants {
	private int key;
	private boolean hidden;
	private boolean highlighted;
	
	public Branch(int key) {
		this.key = key;
		hidden = false;
		highlighted = false;
	}
	
	public int getKey() {
		return key;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	public boolean isHighlighted() {
		return highlighted;
	}
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}
	
	public String getName() {
		return NAMES[KEY2INDEX[key]];
	}
	
	public Icon getIcon() {
		return Icons.getIconForKey(key);
	}
}
