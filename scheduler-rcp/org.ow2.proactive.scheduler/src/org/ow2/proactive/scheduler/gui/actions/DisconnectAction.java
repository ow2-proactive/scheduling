package org.ow2.proactive.scheduler.gui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.composite.StatusLabel;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.views.SeparatedJobView;


public class DisconnectAction extends SchedulerGUIAction {

    public DisconnectAction() {
        this.setText("&Disconnect");
        this.setToolTipText("Disconnect from the ProActive Scheduler");
        this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_DISCONNECT));
    }

    @Override
    public void run() {
        if (MessageDialog.openConfirm(getParent().getShell(), "Confirm disconnection",
                "Are you sure you want to disconnect from the ProActive Scheduler ?")) {
            disconnection();
        }
    }

    public static void disconnection() {
        StatusLabel.getInstance().disconnect();
        // stop log server
        try {
            Activator.terminateLoggerServer();
        } catch (LogForwardingException e) {
            e.printStackTrace();
        }

        // Disconnect the JMX client of ChartIt
        JMXActionsManager.getInstance().disconnectJMXClient();
        SchedulerProxy.getInstance().disconnect();
        PAActiveObject.terminateActiveObject(SchedulerProxy.getInstance(), false);
        SchedulerProxy.clearInstance();

        SeparatedJobView.clearOnDisconnection();
        ActionsManager.getInstance().update();
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus chedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        super.setEnabled(connected);
    }
}
