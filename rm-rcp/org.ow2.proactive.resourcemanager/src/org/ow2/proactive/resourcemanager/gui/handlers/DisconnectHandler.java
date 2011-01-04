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
        //No mood to implements callbacks to static field of my handlers
        //to RMStore, regarding connected state, just do business code
        //and let RCP API manages buttons...
        if (previousState != RMStore.isConnected()) {
            previousState = RMStore.isConnected();
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
        return RMStore.isConnected();
    }

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
