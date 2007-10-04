package org.objectweb.proactive.ic2d.p2PMonitoring.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.objectweb.proactive.ic2d.p2PMonitoring.views.P2PView;


public class DumpFileChooserAction implements IWorkbenchWindowActionDelegate {
    private Display display;

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
        this.display = window.getShell().getDisplay();
    }

    public void run(IAction action) {
        //	new MonitorNewHostDialog(display.getActiveShell(), Protocol.RMI);
        //new LoadFileDialog(display.getActiveShell(), Protocol.RMI);
        FileDialog fd = new FileDialog(display.getActiveShell());
        fd.setText("Open P2P dump file");
        String filename = fd.open();
        if (filename != null) {
            // loadImage(filename);
            //         String currentDir = fd.getFilterPath();
            System.out.println("Opening " + filename);
            P2PView.loadDumpFile(filename);
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }
}
