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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.Selectable;


public class UnlockNodesHandler extends AbstractHandler implements IHandler {

    private static UnlockNodesHandler instance;

    private boolean previousState = true;
    private Set<String> selectedNodes = null;

    public UnlockNodesHandler() {
        super();
        instance = this;
    }

    public static UnlockNodesHandler getInstance() {
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
        ResourceManager resourceManager = RMStore.getInstance().getResourceManager();
        try {
            BooleanWrapper status = resourceManager.unlockNodes(selectedNodes);
            if (status.getBooleanValue()) {
                // success
            } else {
                MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot unlock nodes",
                        "Unknown reason");
            }
        } catch (Exception e) {
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot unlock nodes", e
                    .getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public void setSelectedNodes(List<Selectable> selectedNodes) {
        this.selectedNodes = new HashSet<String>();
        for (Selectable selected : selectedNodes) {
            if (selected instanceof org.ow2.proactive.resourcemanager.gui.data.model.Node) {
                this.selectedNodes.add(((org.ow2.proactive.resourcemanager.gui.data.model.Node) selected)
                        .getName());
            }
        }
        if (!previousState && selectedNodes.size() > 0) {
            fireHandlerChanged(new HandlerEvent(this, true, false));
        } else if (previousState && selectedNodes.size() == 0) {
            fireHandlerChanged(new HandlerEvent(this, true, false));
        }
    }
}
