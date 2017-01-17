/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.resourcemanager.rmnode;

import java.io.Serializable;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * This class stands for a node whose deployment has already been launched whereas the RMNode
 * has not been acquired by the RMCore yet. This purely informative.
 *
 */
public final class RMDeployingNode extends AbstractRMNode {
    static {
        InfrastructureManager.RMDeployingNodeAccessor.setDefault(new RMDeployingNodeAccessorImpl());
    }

    public static final String PROTOCOL_ID = "deploying";

    /** The command that was used to launch the node */
    private String commandLine;

    /** The description of this deploying node */
    private String description = "";

    /** URL of the node, considered as its unique ID */
    private String nodeURL;

    /** Name of the node */
    private String nodeName;

    /** {@link NodeSource} Stub of NodeSource that handle the RMNode */
    private NodeSource nodeSource;

    /** {@link NodeSource} NodeSource that handle the RMNode */
    private String nodeSourceName;

    /** State of the node */
    private NodeState state;

    /** Time stamp of the latest state change */
    private long stateChangeTime;

    /** The add event */
    private RMNodeEvent addEvent;

    /** The last event */
    private RMNodeEvent lastEvent;

    /** client registered the node in the resource manager */
    private Client provider;

    RMDeployingNode() {
    }

    RMDeployingNode(String name, NodeSource ns, String command, Client provider) {
        this.nodeName = name;
        this.nodeSource = ns;
        this.commandLine = command;
        this.nodeSourceName = ns.getName();
        this.nodeURL = this.buildNodeURL();
        this.state = NodeState.DEPLOYING;
        this.stateChangeTime = System.currentTimeMillis();
        this.provider = provider;
        this.addEvent = null;
        this.lastEvent = null;
    }

    RMDeployingNode(String name, NodeSource ns, String command, Client provider, String description) {
        this(name, ns, command, provider);
        this.description = description;
    }

    /**
     * @return the deploying node's description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param desc the new description of the deploying node
     */
    void setDescription(final String desc) {
        this.description = desc;
    }

    /**
     * @return this deploying node's name
     */
    public String getName() {
        return this.nodeName;
    }

    /**
     * @return this deploying node's command line
     */
    public String getCommandLine() {
        return this.commandLine;
    }

    /**
     * {@inheritDoc}
     */
    public void clean() throws NodeException {
        //implementation does nothing
    }

    /**
     * {@inheritDoc}
     */
    public <T> ScriptResult<T> executeScript(Script<T> script, Map<String, Serializable> bindings) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public RMNodeEvent getAddEvent() {
        return this.addEvent;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Permission getAdminPermission() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return empty string
     */
    public String getDescriptorVMName() {
        return "";
    }

    /**
     * @return empty string
     */
    public String getHostName() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public RMNodeEvent getLastEvent() {
        return this.lastEvent;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Node getNode() {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public NodeInformation getNodeInformation() throws NodeException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String getNodeName() {
        return this.nodeName;
    }

    /**
     * {@inheritDoc}
     */
    public NodeSource getNodeSource() {
        return this.nodeSource;
    }

    /**
     * {@inheritDoc}
     */
    public String getNodeURL() {
        return this.nodeURL;
    }

    /**
     * Always return null (a deploying node cannot be owned)
     */
    public Client getOwner() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Client getProvider() {
        return this.provider;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public HashMap<SelectionScript, Integer> getScriptStatus() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public NodeState getState() {
        return this.state;
    }

    /**
     * {@inheritDoc}
     */
    public long getStateChangeTime() {
        return this.stateChangeTime;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Permission getUserPermission() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return an empty string
     */
    public String getVNodeName() {
        return "";
    }

    /**
     * @return false;
     */
    public boolean isBusy() {
        return false;
    }

    /**
     * @return false;
     */
    public boolean isDown() {
        return false;
    }

    /**
     * @return false;
     */
    public boolean isFree() {
        return false;
    }

    /**
     * @return false;
     */
    public boolean isToRemove() {
        return false;
    }

    /**
     * @return true if the deploying node is lost, false otherwise
     */
    public boolean isLost() {
        return this.state == NodeState.LOST;
    }

    /**
     * Sets this deploying node's state to lost
     */
    public void setLost() {
        this.state = NodeState.LOST;
        this.stateChangeTime = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    public void setAddEvent(final RMNodeEvent addEvent) {
        this.addEvent = addEvent;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setBusy(Client owner) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setDown() {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setFree() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public void setLastEvent(final RMNodeEvent lastEvent) {
        this.lastEvent = lastEvent;
    }

    /**
     * {@inheritDoc}
     */
    public void setNodeSource(NodeSource nodeSource) {
        this.nodeSource = nodeSource;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setToRemove() {
        throw new UnsupportedOperationException();
    }

    public int compareTo(RMNode o) {
        return this.getNodeURL().compareTo(o.getNodeURL());
    }

    /**
     * {@inheritDoc}
     */
    public String getNodeSourceName() {
        return this.nodeSourceName;
    }

    /**
     * @return the url of this deploying node
     */
    private String buildNodeURL() {
        return RMDeployingNode.PROTOCOL_ID + "://" + this.nodeSourceName + "/" + this.nodeName;
    }

    /**
     * @return false
     */
    public boolean isConfiguring() {
        return false;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setConfiguring(Client owner) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return false
     */
    @Override
    public boolean isLocked() {
        return false;
    }

    /**
     * @return {@code -1}
     */
    @Override
    public long getLockTime() {
        return -1;
    }

    /**
     * @return {@code null}
     */
    @Override
    public Client getLockedBy() {
        return null;
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void lock(Client owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unlock(Client owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNodeInfo() {
        String newLine = System.lineSeparator();
        String nodeInfo = "Node " + this.getNodeName() + newLine;
        nodeInfo += "URL : " + this.getNodeURL() + newLine;
        nodeInfo += "Node source : " + this.getNodeSourceName() + newLine;
        nodeInfo += "Provider : " + this.getProvider().getName() + newLine;
        nodeInfo += "State : " + this.getState() + newLine;
        nodeInfo += "Description : " + this.getDescription() + newLine;
        nodeInfo += "Command : " + this.getCommandLine() + newLine;
        return nodeInfo;
    }

    @Override
    public void setJMXUrl(JMXTransportProtocol protocol, String address) {
    }

    @Override
    public String getJMXUrl(JMXTransportProtocol protocol) {
        return null;
    }

    @Override
    public boolean isProtectedByToken() {
        return false;
    }

    @Override
    public RMNodeEvent createNodeEvent(RMEventType eventType, NodeState previousNodeState, String initiator) {
        RMNodeEvent rmNodeEvent = new RMNodeEvent(toNodeDescriptor(), eventType, previousNodeState, initiator);
        // The rm node always keeps track on its last event, this is needed for rm node events logic
        if (eventType != null) {
            switch (eventType) {
                case NODE_ADDED:
                    this.setAddEvent(rmNodeEvent);
                    break;
            }
            this.setLastEvent(rmNodeEvent);
        }
        return rmNodeEvent;
    }

    @Override
    public RMNodeEvent createNodeEvent() {
        return createNodeEvent(null, null, null);
    }
}

/**
 * Implementation of {@link InfrastructureManager.RMDeployingNodeAccessor} to be
 * able to have fine tuned access rights.
 */
class RMDeployingNodeAccessorImpl extends InfrastructureManager.RMDeployingNodeAccessor {

    /** {@inheritDoc} */
    @Override
    protected RMDeployingNode newRMDeployingNode(String name, NodeSource ns, String commandLine,
            Client provider, String description) {
        return new RMDeployingNode(name, ns, commandLine, provider, description);
    }

    /** {@inheritDoc} */
    @Override
    protected void setDescription(RMDeployingNode pn, String newDescription) {
        pn.setDescription(newDescription);
    }

    /** {@inheritDoc} */
    @Override
    protected void setLost(RMDeployingNode pn) {
        pn.setLost();
    }
}