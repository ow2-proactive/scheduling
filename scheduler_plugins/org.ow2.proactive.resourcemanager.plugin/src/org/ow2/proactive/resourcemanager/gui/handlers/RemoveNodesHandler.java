package org.ow2.proactive.resourcemanager.gui.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.dialog.RemoveNodeDialog;


public class RemoveNodesHandler extends AbstractHandler implements IHandler {

    private static RemoveNodesHandler instance;

    private boolean previousState = true;
    private ArrayList<String> selectedNodes = null;

    public RemoveNodesHandler() {
        super();
        instance = this;
    }

    public static RemoveNodesHandler getInstance() {
        return instance;
    }

    @Override
    public boolean isEnabled() {
        boolean enabled;
        if (RMStore.isConnected() && selectedNodes != null && selectedNodes.size() != 0) {
            enabled = true;
        } else {
            enabled = false;
        }
        //hack for toolbar menu (bug?), force event throwing if state changed.
        // Otherwise command stills disabled in toolbar menu.
        //No mood to implement callbacks to static field of my handler
        //to RMStore, just do business code  
        //and let RCP API manages buttons... 
        if (previousState != enabled) {
            previousState = enabled;
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
        return enabled;
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        RemoveNodeDialog.showDialog(HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell(),
                selectedNodes);
        return null;
    }

    public void setSelectedNodes(ArrayList<String> selectedNodes) {
        this.selectedNodes = selectedNodes;
        if (!previousState && selectedNodes.size() > 0) {
            fireHandlerChanged(new HandlerEvent(this, true, false));
        } else if (previousState && selectedNodes.size() == 0) {
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
    }
}
