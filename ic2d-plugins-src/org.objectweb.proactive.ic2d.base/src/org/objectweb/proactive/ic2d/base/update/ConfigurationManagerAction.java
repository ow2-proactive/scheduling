package org.objectweb.proactive.ic2d.base.update;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.update.ui.UpdateManagerUI;


/**
 * Action to invoke the Update configuration manager.
 *
 * @since 3.0
 */
public class ConfigurationManagerAction
    implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;

    /**
     * The constructor.
     */
    public ConfigurationManagerAction() {
        // do nothing
    }

    /**
     * Runs the action when selected
     */
    public void run(IAction action) {
        BusyIndicator.showWhile(window.getShell().getDisplay(),
            new Runnable() {
                public void run() {
                    UpdateManagerUI.openConfigurationManager(window.getShell());
                }
            });
    }

    /**
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

    /**
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose() {
        // do nothing
    }

    /**
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
}
