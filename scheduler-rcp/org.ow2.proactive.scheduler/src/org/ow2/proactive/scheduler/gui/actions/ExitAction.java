package org.ow2.proactive.scheduler.gui.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;


/**
 * Exits the application
 *
 */
public class ExitAction extends SchedulerGUIAction {

    private static boolean shuttingDown = false;

    public ExitAction() {
        this.setText("E&xit");
        this.setToolTipText("Exit the Scheduler client");
        this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Internal.IMG_EXIT));
    }

    @Override
    public void run() {
        boolean ret = MessageDialog.openConfirm(null, "Scheduler", "Do you really want do quit?");
        if (ret) {
            exit();
            PlatformUI.getWorkbench().close();
        }
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        setEnabled(true);
    }

    public static void exit() {
        if (shuttingDown)
            return;

        shuttingDown = true;

        // remove empty editor if it exists
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().setEditorAreaVisible(false);
        } catch (Throwable t) {
        }

        Activator.log(IStatus.INFO, "Shutting down...", null);

        try {
            if (ActionsManager.getInstance().isConnected()) {
                DisconnectAction.disconnection();
            }
        } catch (Throwable t) {
            // we're closing anyway, some graphical components may have been disposed
        }
    }
}