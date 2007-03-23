package org.objectweb.proactive.ic2d.infrastructuremanager.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.infrastructuremanager.dialog.IMDialogConnection;
import org.objectweb.proactive.ic2d.infrastructuremanager.views.IMViewAdministration;
import org.objectweb.proactive.ic2d.infrastructuremanager.views.IMViewInfrastructure;


public class NewViewAction  extends Action implements IWorkbenchWindowActionDelegate {

	private static int index = 0;
	public static final String NEW_VIEW = "NewInfrastructureView";

	public NewViewAction() {
		this.setId(NEW_VIEW);
		this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "newview.gif"));
		this.setText("New Infrastructure View");
		this.setToolTipText("New Infrastructure View");
	}

	//
	// -- PUBLICS METHODS -----------------------------------------------
	//

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	public void run(IAction action) {
		this.run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		try {
			IMViewInfrastructure view = (IMViewInfrastructure) PlatformUI.getWorkbench().
			getActiveWorkbenchWindow().getActivePage().showView(IMViewInfrastructure.ID, 
					IMViewInfrastructure.ID+"#"+(++index), IWorkbenchPage.VIEW_ACTIVATE);			
			if(view.getPartName().equals("Infrastructure")) {
				PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getActivePage().hideView(view);
			}
			
		} 
		catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}











