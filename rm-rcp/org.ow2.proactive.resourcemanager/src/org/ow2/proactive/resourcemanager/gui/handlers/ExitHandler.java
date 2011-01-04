/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;


public class ExitHandler extends AbstractHandler implements IHandler {

    private static boolean shutDown = false;

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        boolean ret = MessageDialog.openConfirm(
                HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell(), "Exit Resource Manager",
                "Do you really want to quit?");
        if (ret) {
            exit();
            PlatformUI.getWorkbench().close();
        }

        return null;
    }

    public static void exit() {
        if (shutDown)
            return;
        shutDown = true;

        // remove empty editor if it exists
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().setEditorAreaVisible(false);
        } catch (Throwable t) {
        }

        Activator.log(IStatus.INFO, "Shutting down...", null);

        if (RMStore.isConnected()) {
            RMStore.getInstance().disconnectionActions();
        }
    }
}
