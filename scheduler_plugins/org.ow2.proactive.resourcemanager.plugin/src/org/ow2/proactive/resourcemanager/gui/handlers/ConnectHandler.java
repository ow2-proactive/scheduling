package org.ow2.proactive.resourcemanager.gui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.dialog.SelectResourceManagerDialog;
import org.ow2.proactive.resourcemanager.gui.dialog.SelectResourceManagerDialogResult;


public class ConnectHandler extends AbstractHandler implements IHandler {

    boolean previousState = true;

    @Override
    public boolean isEnabled() {
        //hack for toolbar menu, throws event if state changed
        //otherwise action stays disabled
        if (previousState != RMStore.isConnected()) {
            previousState = RMStore.isConnected();
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
        return !RMStore.isConnected();
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        SelectResourceManagerDialogResult dialogResult = SelectResourceManagerDialog.showDialog(HandlerUtil
                .getActiveWorkbenchWindowChecked(event).getShell());
        if (dialogResult != null) {
            try {
                RMStore.newInstance(dialogResult.getUrl(), dialogResult.getLogin(), dialogResult
                        .getPassword(), dialogResult.isLogAsAdmin());
            } catch (Throwable t) {
                MessageDialog.openError(Display.getDefault().getActiveShell(),
                        "Couldn't connect to resource manager", t.getMessage());
                t.printStackTrace();
            }
        }
        fireHandlerChanged(new HandlerEvent(this, true, false));
        return null;
    }
}
