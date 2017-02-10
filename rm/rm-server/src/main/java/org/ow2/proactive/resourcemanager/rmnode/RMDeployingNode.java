/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.rmnode;

import java.io.Serializable;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * This class stands for a node whose deployment has already been launched whereas the RMNode
 * has not been acquired by the RMCore yet. This is purely informative.
 */
public final class RMDeployingNode extends AbstractRMNode {

    static {
        InfrastructureManager.RMDeployingNodeAccessor.setDefault(new RMDeployingNodeAccessorImpl());
    }

    public static final String PROTOCOL_ID = "deploying";

    /** The command that was used to launch the node */
    private final String commandLine;

    /** The description of this deploying node */
    private String description = "";

    /**
     * Required by ProActive Programming.
     */
    public RMDeployingNode() {
        this.commandLine = "";
    }

    public RMDeployingNode(String name, NodeSource nodeSource, String command, Client provider) {
        super(nodeSource, name, null, provider);

        changeState(NodeState.DEPLOYING);

        this.commandLine = command;
    }

    public RMDeployingNode(String name, NodeSource ns, String command, Client provider, String description) {
        this(name, ns, command, provider);
        this.description = description;
    }

    /**
     * Returns the deploying node's description.
     *
     * @return the deploying node's description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the new description of the deploying node.
     *
     * @param desc the new description of the deploying node.
     */
    void setDescription(final String desc) {
        this.description = desc;
    }

    /**
     * Returns the command line of the node that is deploying.
     *
     * @return the command line of the node that is deploying.
     */
    public String getCommandLine() {
        return this.commandLine;
    }

    /**
     * The behaviour is to do nothing.
     */
    @Override
    public void clean() throws NodeException {
        // implementation does nothing
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException under all conditions.
     */
    @Override
    public <T> ScriptResult<T> executeScript(Script<T> script, Map<String, Serializable> bindings) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException under all conditions.
     */
    @Override
    public Permission getAdminPermission() {
        throw new UnsupportedOperationException();
    }

    /**
     * A deploying node has no VM descriptor.
     *
     * @return an empty String.
     */
    @Override
    public String getDescriptorVMName() {
        return "";
    }

    /**
     * A deploying node has no hostname assigned yet.
     *
     * @return an empty String.
     */
    @Override
    public String getHostName() {
        return "";
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException under all conditions.
     */
    @Override
    public Node getNode() {
        throw new UnsupportedOperationException();
    }

    /**
     * Always return null (a deploying node cannot be owned)
     */
    @Override
    public Client getOwner() {
        return null;
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException under all conditions.
     */
    @Override
    public HashMap<SelectionScript, Integer> getScriptStatus() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException under all conditions.
     */
    @Override
    public Permission getUserPermission() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns {@code false} under all conditions.
     *
     * @return {@code false} under all conditions.
     */
    @Override
    public String getVNodeName() {
        return "";
    }

    /**
     * Returns {@code false} under all conditions.
     *
     * @return {@code false} under all conditions.
     */
    @Override
    public boolean isBusy() {
        return false;
    }

    /**
     * Returns {@code false} under all conditions.
     *
     * @return {@code false} under all conditions.
     */
    @Override
    public boolean isDown() {
        return false;
    }

    /**
     * Returns {@code false} under all conditions.
     *
     * @return {@code false} under all conditions.
     */
    @Override
    public boolean isFree() {
        return false;
    }

    /**
     * Returns {@code false} under all conditions.
     *
     * @return {@code false} under all conditions.
     */
    @Override
    public boolean isToRemove() {
        return false;
    }

    /**
     * @return true if the deploying node is lost, false otherwise
     */
    public boolean isLost() {
        return this.state == NodeState.LOST;
    }

    @Override
    public boolean isDeploying() {
        return true;
    }

    /**
     * Sets this deploying node's state to lost
     */
    public void setLost() {
        changeState(NodeState.LOST);
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException under all conditions.
     */
    @Override
    public void setBusy(Client owner) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException under all conditions.
     */
    @Override
    public void setDown() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException under all conditions.
     */
    @Override
    public void setFree() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException under all conditions.
     */
    @Override
    public void setToRemove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(RMNode rmNode) {
        return this.getNodeURL().compareTo(rmNode.getNodeURL());
    }

    @Override
    public String getNodeURL() {
        return RMDeployingNode.PROTOCOL_ID + "://" + super.nodeSourceName + "/" + super.nodeName;
    }

    /**
     * Returns {@code false} under all conditions.
     *
     * @return {@code false} under all conditions.
     */
    @Override
    public boolean isConfiguring() {
        return false;
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException under all conditions.
     */
    @Override
    public void setConfiguring(Client owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNodeInfo() {
        String newLine = System.lineSeparator();
        String nodeInfo = "Node " + this.getNodeName() + newLine;
        nodeInfo += "URL: " + this.getNodeURL() + newLine;
        nodeInfo += "Node source: " + this.getNodeSourceName() + newLine;
        nodeInfo += "Provider: " + this.getProvider().getName() + newLine;
        nodeInfo += "State: " + this.getState() + newLine;
        nodeInfo += getLockStatus();
        nodeInfo += "Description: " + this.getDescription() + newLine;
        nodeInfo += "Command: " + this.getCommandLine() + newLine;
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

    public RMDeployingNode updateOnNodeSource() {
        return nodeSource.update(this);
    }

}

/**
 * Implementation of {@link InfrastructureManager.RMDeployingNodeAccessor} to be
 * able to have fine tuned access rights.
 */
class RMDeployingNodeAccessorImpl extends InfrastructureManager.RMDeployingNodeAccessor {

    /** {@inheritDoc} */
    @Override
    protected RMDeployingNode newRMDeployingNode(String name, NodeSource ns, String commandLine, Client provider,
            String description) {
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
