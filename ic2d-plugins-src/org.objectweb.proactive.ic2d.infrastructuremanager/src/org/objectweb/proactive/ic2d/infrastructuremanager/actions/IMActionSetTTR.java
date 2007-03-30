package org.objectweb.proactive.ic2d.infrastructuremanager.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.infrastructuremanager.data.IMData;
import org.objectweb.proactive.ic2d.infrastructuremanager.dialog.IMDialogSetTTR;

public class IMActionSetTTR extends Action {

	public static final String SET_TTR = "Set ttr";
	
	private Display display;
	private IMData imData;
	
	public IMActionSetTTR(Display display, IMData data) {
		this.display = display;
		this.imData = data;
		this.setId(SET_TTR);
		this.setText("Set Time To Refresh...");
		setToolTipText("Set Time To Refresh");
		this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "ttr.gif"));
	}
		
	@Override
	public void run() {
		new IMDialogSetTTR(display.getActiveShell(), imData);
	}

}
