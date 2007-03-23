
package org.objectweb.proactive.ic2d.infrastructuremanager.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

public class CollapseAllAction extends Action {
	
	private ExpandBar expandBar;

	public CollapseAllAction(ExpandBar bar) {
		super("Collapse All");
		expandBar = bar;
		setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "collapse.gif"));
		setToolTipText("Collapse All");
	}

	public void run() {
		for(ExpandItem item : expandBar.getItems()) {
			item.setExpanded(false);
		}
	}
	
}
