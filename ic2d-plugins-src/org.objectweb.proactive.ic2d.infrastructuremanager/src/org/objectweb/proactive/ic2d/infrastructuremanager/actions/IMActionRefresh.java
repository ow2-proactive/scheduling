package org.objectweb.proactive.ic2d.infrastructuremanager.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.infrastructuremanager.Activator;
import org.objectweb.proactive.ic2d.monitoring.data.MonitorThread;

public class IMActionRefresh extends Action {
	
	public static final String REFRESH = "Refresh";
	
	private MonitorThread monitorThread;
	
	public IMActionRefresh(Shell s) {
		this.setId(REFRESH);
		this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "refresh.gif"));
		this.setText("Refresh");
		this.setToolTipText("Refresh");
	}
	
	@Override
	public void run() {
		monitorThread.forceRefresh();
		Console.getInstance(Activator.CONSOLE_NAME).debug("Manual refresh");
	}
}