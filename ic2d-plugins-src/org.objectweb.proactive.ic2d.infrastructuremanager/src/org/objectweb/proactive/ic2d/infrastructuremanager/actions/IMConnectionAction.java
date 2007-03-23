package org.objectweb.proactive.ic2d.infrastructuremanager.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.infrastructuremanager.dialog.IMDialogConnection;

public class IMConnectionAction extends Action {

	private Display display;	
	public static final String RM_CONNECTION = "Infrastructure Manager Connection";
	
	public IMConnectionAction(Display display) {
		setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "connection.png"));
		this.display = display;
		setId(RM_CONNECTION);
		setText("Connect to a RM");
		setToolTipText("Connect to a RM");
	}
	
	@Override
	public void run() {
		new IMDialogConnection(display.getActiveShell());
	}
}
