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
package org.ow2.proactive.resourcemanager.gui.data.model;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;


/**
 * @author The ProActive Team
 */
public class Node extends TreeLeafElement implements Removable, Describable {
    protected NodeState state = null;
    protected String stateChangeTime;
    protected String provider;
    protected String owner;
    protected String description;

    protected Node(String name, TreeElementType type) {
        super(name, type);
    }

    public Node(RMNodeEvent nodeEvent) {
        super(nodeEvent.getNodeUrl(), TreeElementType.NODE);
        this.state = nodeEvent.getNodeState();
        this.stateChangeTime = nodeEvent.getTimeStampFormatted();
        this.provider = nodeEvent.getNodeProvider();
        this.owner = nodeEvent.getNodeOwner();
        this.description = nodeEvent.getNodeInfo();
    }

    /**
     * To get the state
     *
     * @return the state
     */
    public NodeState getState() {
        return state;
    }

    /**
     * To set the state
     *
     * @param state the state to set
     */
    public void setState(RMNodeEvent event) {
        this.state = event.getNodeState();
        this.stateChangeTime = event.getTimeStampFormatted();
        this.owner = event.getNodeOwner();
    }

    /**
     * Returns a description of the node
     * @return the description of the node
     */
    public String getDescription() {
        return this.description;
    }

    public String getStateChangeTime() {
        return stateChangeTime;
    }

    public String getProvider() {
        return provider;
    }

    public String getOwner() {
        return owner;
    }

    /**
     * @param description the new description of the node
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Handles the removal of the node from the model
     * {@inheritDoc}
     */
    public void removeFromModel(boolean preemptive, boolean removeDownNodes) {
        if (removeDownNodes && this.getState() != NodeState.DOWN) {
            return;
        }
        try {
            RMStore.getInstance().getResourceManager().removeNode(this.getName(), preemptive)
                    .getBooleanValue();
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
