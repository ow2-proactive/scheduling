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
