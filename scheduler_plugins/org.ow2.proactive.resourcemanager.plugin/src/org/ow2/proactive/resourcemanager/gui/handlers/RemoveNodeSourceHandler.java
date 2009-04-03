package org.ow2.proactive.resourcemanager.gui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.dialog.RemoveSourceDialog;


public class RemoveNodeSourceHandler extends AbstractHandler implements IHandler {

    private static RemoveNodeSourceHandler instance;
    boolean previousState = true;

    public RemoveNodeSourceHandler() {
        super();
        instance = this;
    }

    public static RemoveNodeSourceHandler getInstance() {
        return instance;
    }

    @Override
    public boolean isEnabled() {
        boolean state;
        if (RMStore.isConnected() && RMStore.getInstance().getModel().getSourcesNames(false).length > 0) {
            state = true;
        } else
            state = false;

        //hack for toolbar menu (bug?), force event throwing if state changed.
        // Otherwise command stills disabled in toolbar menu
        //No mood to implement callbacks to static field of my handlers
        //to RMStore, just do business code  
        //and let RCP API manages buttons... 
        if (previousState != state) {
            previousState = state;
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
        return state;
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        RemoveSourceDialog.showDialog(HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell());
        return null;
    }
}
