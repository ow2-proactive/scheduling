/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.dialog.SelectResourceManagerDialog;
import org.ow2.proactive.resourcemanager.gui.dialog.SelectResourceManagerDialogResult;


public class ConnectHandler extends AbstractHandler implements IHandler {

    private boolean previousState = true;
    /** A boolean value to know if the dialog box is already open */
    private boolean isDialogOpen;
    private static ConnectHandler thisHandler;

    public ConnectHandler() {
        thisHandler = this;
    }

    public static ConnectHandler getHandler() {
        return thisHandler;
    }

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

    public synchronized Object execute(ExecutionEvent event) throws ExecutionException {
        return execute(HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell());
    }

    public synchronized Object execute(Shell parent) {
        if (this.isDialogOpen || RMStore.isConnected()) {
            return null;
        }
        this.isDialogOpen = true;
        SelectResourceManagerDialogResult dialogResult = SelectResourceManagerDialog.showDialog(parent);
        if (dialogResult != null && !dialogResult.isCanceled()) {
            try {
                RMStore.newInstance(dialogResult.getUrl(), dialogResult.getLogin(), dialogResult
                        .getPassword());
            } catch (Throwable t) {
                MessageDialog.openError(Display.getDefault().getActiveShell(),
                        "Couldn't connect to resource manager", t.getMessage());
                Activator.log(IStatus.ERROR, "Could not connect to the Resource Manager ", t);
                t.printStackTrace();

                try {
                    // trying to disconnect in any case
                    RMStore.getInstance().getResourceManager().disconnect();
                } catch (Throwable thr) {
                }
            }
        }
        fireHandlerChanged(new HandlerEvent(this, true, false));
        this.isDialogOpen = false;
        return null;
    }
}
