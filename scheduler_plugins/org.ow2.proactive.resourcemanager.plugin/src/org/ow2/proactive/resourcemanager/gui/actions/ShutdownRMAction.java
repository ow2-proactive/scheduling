package org.ow2.proactive.resourcemanager.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.ow2.proactive.resourcemanager.gui.dialog.ShutdownDialog;


public class ShutdownRMAction extends Action {

    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static ShutdownRMAction instance = null;
    private TreeViewer viewer = null;
    private Composite parent = null;

    private ShutdownRMAction(Composite parent, TreeViewer viewer) {
        this.parent = parent;
        this.viewer = viewer;
        this.setText("Shutdown Resource Manager");
        this.setToolTipText("shutdown RM");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/rm_shutdown.png"));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    @Override
    public void run() {
        ShutdownDialog.showDialog(parent.getShell());
    }

    public static ShutdownRMAction newInstance(Composite parent, TreeViewer viewer) {
        instance = new ShutdownRMAction(parent, viewer);
        return instance;
    }

    public static ShutdownRMAction getInstance() {
        return instance;
    }

}
