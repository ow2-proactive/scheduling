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

import java.io.IOException;
import java.io.Serializable;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.permissions.PrincipalPermission;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * Implementation of the RMNode Interface.
 * An RMNode is a ProActive node able to execute schedulers tasks.
 * So an RMNode is a representation of a ProActive node object with its associated {@link NodeSource},
 * and its state in the Resource Manager :<BR>
 * -free : node is ready to perform a task.<BR>
 * -busy : node is executing a task.<BR>
 * -to be released : node is busy and have to be removed at the end of the its current task.<BR>
 * -down : node is broken, and not anymore able to perform tasks.<BR><BR>
 *
 * Resource Manager can select nodes that verify criteria. this selection is implemented with
 * {@link SelectionScript} objects. Each node memorize results of executed scripts, in order to
 * answer faster to a selection already asked.
 *
 * @see NodeSource
 * @see SelectionScript
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class RMNodeImpl extends AbstractRMNode {

    private final static Logger logger = Logger.getLogger(RMNodeImpl.class);

    /** HashMap associates a selection Script to its result on the node */
    private HashMap<SelectionScript, Integer> scriptStatus;

    /** ProActive Node Object of the RMNode */
    private Node node;

    /** {@link VirtualNode} name of the node */
    private String vnodeName = "";

    /** Host name of the node */
    private String hostName;

    /** JVM name of the node */
    private String jvmName;

    /** Script handled, manage scripts launching and results recovering */
    private ScriptHandler handler = null;

    /** client taken the node for computations */
    private Client owner;

    /** Node access permission*/
    private Permission nodeAccessPermission;

    private String[] jmxUrls;

    /** true if node is protected with token */
    private boolean protectedByToken = false;

    /**
     * Constructs a new instance. Initial state is set to {@link NodeState#FREE}.
     *
     * @param node ProActive node deployed.
     * @param nodeSource {@link NodeSource} Stub of NodeSource that handles the Node.
     * @param provider the client who deployed the Node.
     * @param nodeAccessPermission the permissions associated with the Node.
     */
    public RMNodeImpl(Node node, NodeSource nodeSource, Client provider, Permission nodeAccessPermission) {
        this(node, nodeSource, provider, nodeAccessPermission, NodeState.FREE);
    }

    public RMNodeImpl(Node node, NodeSource nodeSource, Client provider, Permission nodeAccessPermission,
            NodeState state) {
        super(nodeSource, node.getNodeInformation().getName(), node.getNodeInformation().getURL(), provider);

        changeState(state);

        this.hostName = node.getNodeInformation().getVMInformation().getHostName();
        this.jmxUrls = new String[JMXTransportProtocol.values().length];
        this.jvmName = node.getProActiveRuntime().getURL();
        this.node = node;
        this.nodeAccessPermission = nodeAccessPermission;
        this.scriptStatus = new HashMap<>();
    }

    /**
     * A constructor of {@link RMNodeImpl} that already has all the
     * configuration information.
     */
    public RMNodeImpl(Node node, NodeSource nodeSource, String nodeName, String nodeUrl, Client provider,
            String hostName, String[] jmxUrls, String jvmName, Permission nodeAccessPermission, NodeState state) {
        super(nodeSource, nodeName, nodeUrl, provider);

        changeState(state);

        this.hostName = hostName;
        this.jmxUrls = jmxUrls;
        this.jvmName = jvmName;
        this.node = node;
        this.nodeAccessPermission = nodeAccessPermission;
        this.scriptStatus = new HashMap<>();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.rmnode.RMNode#getNode()
     */
    @Override
    public Node getNode() {
        return this.node;
    }

    /**
     * Returns the Virtual node name of the RMNode.
     * @return the Virtual node name  of the RMNode.
     */
    @Override
    public String getVNodeName() {
        return this.vnodeName;
    }

    /**
     * Returns the host name of the RMNode.
     * @return the host name of the RMNode.
     */
    @Override
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Returns the java virtual machine name of the RMNode.
     * @return the java virtual machine name of the RMNode.
     */
    @Override
    public String getDescriptorVMName() {
        return this.jvmName;
    }

    /**
     * Changes the state of this node to {@link NodeState#BUSY}.
     */
    @Override
    public void setBusy(Client owner) {
        changeState(NodeState.BUSY);
        this.owner = owner;
    }

    /**
     * Changes the state of this node to {@link NodeState#FREE}.
     */
    @Override
    public void setFree() {
        changeState(NodeState.FREE);
        this.owner = null;
    }

    /**
     * Changes the state of this node to {@link NodeState#CONFIGURING}
     */
    @Override
    public void setConfiguring(Client owner) {
        if (!this.isDown()) {
            changeState(NodeState.CONFIGURING);
        }
    }

    /**
     * Changes the state of this node to {@link NodeState#DOWN}.
     */
    @Override
    public void setDown() {
        changeState(NodeState.DOWN);
    }

    /**
     * Changes the state of this node to {@link NodeState#TO_BE_REMOVED}.
     */
    @Override
    public void setToRemove() {
        changeState(NodeState.TO_BE_REMOVED);
    }

    /**
     * @return true if the node is free, false otherwise.
     */
    @Override
    public boolean isFree() {
        return this.state == NodeState.FREE;
    }

    /**
     * @return true if the node is busy, false otherwise.
     */
    @Override
    public boolean isBusy() {
        return this.state == NodeState.BUSY;
    }

    /**
     * @return true if the node is down, false otherwise.
     */
    @Override
    public boolean isDown() {
        return this.state == NodeState.DOWN;
    }

    /**
     * @return true if the node is 'to be released', false otherwise.
     */
    @Override
    public boolean isToRemove() {
        return this.state == NodeState.TO_BE_REMOVED;
    }

    /**
     * @return true if the node is 'configuring', false otherwise.
     */
    @Override
    public boolean isConfiguring() {
        return this.state == NodeState.CONFIGURING;
    }

    /**
     * @return a String showing information about the node.
     */
    @Override
    public String getNodeInfo() {
        String newLine = System.lineSeparator();
        String nodeInfo = "Node " + nodeName + newLine;
        nodeInfo += "URL: " + getNodeURL() + newLine;
        nodeInfo += "Node source: " + nodeSourceName + newLine;
        nodeInfo += "Provider: " + provider.getName() + newLine;
        nodeInfo += "Used by: " + (owner == null ? "nobody" : owner.getName()) + newLine;
        nodeInfo += "State: " + state + newLine;
        nodeInfo += getLockStatus();
        nodeInfo += "JMX RMI: " + getJMXUrl(JMXTransportProtocol.RMI) + newLine;
        nodeInfo += "JMX RO: " + getJMXUrl(JMXTransportProtocol.RO) + newLine;
        return nodeInfo;
    }

    private void initHandler() throws NodeException {
        if (this.handler == null) {
            try {
                this.handler = ScriptLoader.createHandler(this.node);
            } catch (Exception e) {
                throw new NodeException("Unable to create Script Handler on node ", e);
            }
        }
    }

    /**
     * Returns the ProActive stub on the remote script handler.
     * @return the ProActive stub on the script handler.
     */
    public ScriptHandler getHandler() throws NodeException {
        this.initHandler();
        return this.handler;
    }

    /**
     * Execute a selection Script in order to test the Node.
     * If no script handler is defined, create one, and execute the script.
     * @param script Selection script to execute
     * @param bindings bindings to use to execute the selection scripts
     * @return Result of the test.
     */
    @Override
    public <T> ScriptResult<T> executeScript(Script<T> script, Map<String, Serializable> bindings) {
        try {
            this.initHandler();
        } catch (NodeException e) {
            return new ScriptResult<>(e);
        }
        if (bindings != null) {
            for (String key : bindings.keySet()) {
                this.handler.addBinding(key, bindings.get(key));
            }
        }
        return this.handler.handle(script);
    }

    /**
     * Clean the node.
     * kill all active objects on the node.
     * @throws NodeException
     */
    @Override
    public synchronized void clean() throws NodeException {
        handler = null;
        try {
            logger.debug(getNodeURL() + " : cleaning");
            node.killAllActiveObjects();
        } catch (IOException e) {
            throw new NodeException("Node is down");
        }

        // Wait until all active objects are terminated
        waitUntilNodeIsCleaned();
    }

    private void waitUntilNodeIsCleaned() throws NodeException {
        long timeout = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT.getValueAsLong();
        int sleepTime = 100;
        int maximumNumberOfWait = Math.round(((float) timeout) / sleepTime);
        try {
            int numberOfActiveObjects = node.getNumberOfActiveObjects();
            int numberOfWait = 0;
            while (numberOfActiveObjects > 0 && numberOfWait < maximumNumberOfWait) {
                Thread.sleep(sleepTime);
                numberOfWait++;
                numberOfActiveObjects = node.getNumberOfActiveObjects();
            }
            if (numberOfWait >= maximumNumberOfWait) {
                logger.error("Node " + nodeName + "could not be cleaned after " + timeout + " ms.");
            }
        } catch (InterruptedException e) {
            logger.warn("Interrupted while cleaning node " + nodeName, e);
        }
    }

    /**
     * Gives the HashMap of all scripts tested with corresponding results.
     * @return the HashMap of all scripts tested with corresponding results.
     */
    @Override
    public HashMap<SelectionScript, Integer> getScriptStatus() {
        return this.scriptStatus;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param rmnode the RMNode object to compare
     * @return an integer
     */
    @Override
    public int compareTo(RMNode rmnode) {
        int vNodeNameComparison = this.getVNodeName().compareTo(rmnode.getVNodeName());

        if (vNodeNameComparison == 0) {
            int hostNameComparison = this.getHostName().compareTo(rmnode.getHostName());

            if (hostNameComparison == 0) {
                int descriptorVmNameComparison = this.getDescriptorVMName().compareTo(rmnode.getDescriptorVMName());

                if (descriptorVmNameComparison == 0) {
                    return this.getNodeURL().compareTo(rmnode.getNodeURL());
                } else {
                    return descriptorVmNameComparison;
                }
            } else {
                return hostNameComparison;
            }
        } else {
            return vNodeNameComparison;
        }
    }

    public void setState(NodeState nodeState) {
        this.state = nodeState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client getOwner() {
        return owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Permission getUserPermission() {
        return nodeAccessPermission;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Permission getAdminPermission() {
        return new PrincipalPermission(provider.getName(),
                                       provider.getSubject().getPrincipals(UserNamePrincipal.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMXUrl(JMXTransportProtocol protocol, String address) {
        jmxUrls[protocol.ordinal()] = address;
    }

    @Override
    public String getJMXUrl(JMXTransportProtocol protocol) {
        return jmxUrls[protocol.ordinal()];
    }

    @Override
    public String[] getJmxUrls() {
        return jmxUrls;
    }

    @Override
    public boolean isProtectedByToken() {
        return protectedByToken;
    }

    public void setProtectedByToken(boolean protectedByToken) {
        this.protectedByToken = protectedByToken;
    }

    @Override
    public boolean isDeploying() {
        return false;
    }

}
