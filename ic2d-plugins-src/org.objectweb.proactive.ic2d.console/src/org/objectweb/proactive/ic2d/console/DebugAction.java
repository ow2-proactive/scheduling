package org.objectweb.proactive.ic2d.console;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;


public class DebugAction implements IViewActionDelegate {
    public void init(IViewPart view) {
        // TODO Auto-generated method stub
    }

    public void run(IAction action) {
        Console.debug = !Console.debug;
        if (action.getToolTipText().compareTo("Debug mode") == 0) {
            action.setToolTipText("Normal mode");
        } else { //action.getToolTipText().compareTo("Normal mode") == 0
            action.setToolTipText("Debug mode");
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub
    }
}
