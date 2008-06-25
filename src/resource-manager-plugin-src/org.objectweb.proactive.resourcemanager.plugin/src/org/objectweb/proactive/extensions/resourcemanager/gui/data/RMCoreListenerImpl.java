package org.objectweb.proactive.extensions.resourcemanager.gui.data;

import java.io.Serializable;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.extensions.resourcemanager.gui.actions.ConnectDeconnectResourceManagerAction;
import org.objectweb.proactive.extensions.resourcemanager.gui.interfaces.RMCoreEventListener;


public class RMCoreListenerImpl implements RMCoreEventListener, Serializable {

    private Shell shell = null;

    public RMCoreListenerImpl(Shell s) {
        shell = s;
    }

    public void imKilledEvent() {
        // TODO Auto-generated method stub

    }

    public void imShutDownEvent() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openInformation(shell, "shutdown", "Resource manager " +
                    RMStore.getInstance().getURL() + " has been shutdown");
                ConnectDeconnectResourceManagerAction.getInstance().run();
            }
        });
    }

    public void imShuttingDownEvent() {
        // TODO Auto-generated method stub	
    }

    public void imStartedEvent() {
        // TODO Auto-generated method stub

    }
}