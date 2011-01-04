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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.gui.data.model;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;


/**
 * Deploying node representation for the client side
 */
public class DeployingNode extends Node {

    public DeployingNode(RMNodeEvent nodeEvent) {
        super(nodeEvent.getNodeUrl(), TreeElementType.PENDING_NODE);
        this.state = nodeEvent.getNodeState();
        this.stateChangeTime = nodeEvent.getTimeStampFormatted();
        this.provider = nodeEvent.getNodeProvider();
        this.owner = nodeEvent.getNodeOwner();
        this.description = nodeEvent.getNodeInfo();
    }

    /**
     * @param event the event associated with the state change
     */
    @Override
    public void setState(RMNodeEvent event) {
        NodeState ns = event.getNodeState();
        //we control the state that we affect to the pending node
        if (ns != NodeState.LOST && ns != NodeState.DEPLOYING) {
            return;
        }
        super.setState(event);
    }

    /**
     * To handle the removal of the pending node from the model
     * {@inheritDoc}
     */
    @Override
    public void removeFromModel(boolean preemp, boolean removeDownNodes) {
        //we don't care about preemptive removal
        //if we only want to remove lost (down) node, we return
        if (removeDownNodes && this.state == NodeState.DEPLOYING) {
            return;
        }
        try {
            RMStore.getInstance().getResourceManager().removeNode(this.getName(), true);
        } catch (RuntimeException e) {
            e.printStackTrace();
            String message = e.getMessage();
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot remove node", message);
        }
    }
}
