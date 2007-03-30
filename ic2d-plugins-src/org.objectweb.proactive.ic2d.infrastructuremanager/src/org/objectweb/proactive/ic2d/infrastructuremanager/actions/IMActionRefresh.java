package org.objectweb.proactive.ic2d.infrastructuremanager.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class IMActionRefresh extends Action {
	
	public static final String REFRESH = "Refresh";
	private Thread thread;
	
	public IMActionRefresh(Thread t) {
		thread = t;
		this.setId(REFRESH);
		this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "refresh.gif"));
		this.setText("Refresh");
		this.setToolTipText("Refresh");
	}
	
	@Override
	public void run() {
		thread.interrupt();
	}
}