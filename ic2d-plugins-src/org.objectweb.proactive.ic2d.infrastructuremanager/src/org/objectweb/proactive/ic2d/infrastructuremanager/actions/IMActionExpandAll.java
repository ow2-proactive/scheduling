
package org.objectweb.proactive.ic2d.infrastructuremanager.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.objectweb.proactive.ic2d.infrastructuremanager.composite.IMCompositeDescriptor;
import org.objectweb.proactive.ic2d.infrastructuremanager.composite.IMCompositeVNode;

public class IMActionExpandAll extends Action {

	private ExpandBar expandBar;

	public IMActionExpandAll(ExpandBar bar) {
		super("Expand All");
		expandBar = bar;
		setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "expand.gif"));
		setToolTipText("Expand All");
	}

	public void run() {
		for(ExpandItem item : expandBar.getItems()) {
			item.setExpanded(true);
			for(IMCompositeVNode vnode : ((IMCompositeDescriptor) item.getControl()).getVnodes().values()) {
				vnode.setCollapse(false);
			}
		}
	}

}
