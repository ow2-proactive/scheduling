package org.ow2.proactive.resourcemanager.gui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.dialog.ShutdownDialog;


public class ShutdownHandler extends AbstractHandler implements IHandler {

    boolean previousState = true;

    @Override
    public boolean isEnabled() {
        //hack for toolbar menu (bug?), force event throwing if state changed.
        // Otherwise command stills disabled in toolbar menu
        //No mood to implement callbacks to static field of my handlers
        //to RMStore, just do business code  
        //and let RCP API manages buttons...
        if (previousState != RMStore.isConnected()) {
            previousState = RMStore.isConnected();
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
        return RMStore.isConnected();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ShutdownDialog.showDialog(HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell());
        fireHandlerChanged(new HandlerEvent(this, true, false));
        return null;
    }
}
