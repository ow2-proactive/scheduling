/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
import org.eclipse.ui.handlers.HandlerUtil;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.dialog.AddNodeDialog;


public class AddNodesHandler extends AbstractHandler implements IHandler {

    boolean previousState = true;

    @Override
    public boolean isEnabled() {
        //hack for toolbar menu (bug?), force event throwing if state changed.
        // Otherwise command stills disabled in toolbar menu
        //No mood to implements callbacks to static field of my handlers
        //to RMStore, just do business code
        //and let RCP API manages buttons...
        if (previousState != RMStore.isConnected()) {
            previousState = RMStore.isConnected();
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
        return RMStore.isConnected();
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        AddNodeDialog.showDialog(HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell(), null);
        return null;
    }
}
