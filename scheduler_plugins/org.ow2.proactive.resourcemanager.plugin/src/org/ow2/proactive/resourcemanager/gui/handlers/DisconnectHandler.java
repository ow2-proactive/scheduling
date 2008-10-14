package org.ow2.proactive.resourcemanager.gui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;


public class DisconnectHandler extends AbstractHandler implements IHandler {

    boolean previousState = true;

    @Override
    public boolean isEnabled() {
        //hack for toolbar menu (bug?), force event throwing if state changed.
        // Otherwise command stills disabled in toolbar menu
        //
        //No mood to implements callbacks to static field of my handlers
        //from RMStore, regarding connected state, just do business code  
        //and let RCP API manages buttons... 
        if (previousState != RMStore.isConnected()) {
            previousState = RMStore.isConnected();
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
        return RMStore.isConnected();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (MessageDialog.openConfirm(HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell(),
                "Confirm disconnection",
                "Are you sure you want to disconnect from the ProActive Resource Manager ?")) {
            RMStore.getInstance().disconnectionActions();
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
        return null;
    }
}
