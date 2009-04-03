/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.rmnode;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
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
 *
 */
public class RMNodeImpl implements RMNode, Serializable {

    /** associated logger */
    private static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMNODE);

    /** HashMap associates a selection Script to its result on the node */
    private HashMap<SelectionScript, Integer> scriptStatus;

    /** ProActive Node Object of the RMNode */
    private Node node;

    /** URL of the node, considered as its unique ID */
    private String nodeURL;

    /** Name of the node */
    private String nodeName;

    /** {@link VirtualNode} name of the node */
    private String vnodeName;

    /** Host name of the node */
    private String hostName;

    /** Java virtual machine name of the node */
    private String vmName;

    /** Script handled, manage scripts launching and results recovering */
    private ScriptHandler handler = null;

    /** {@link NodeSource} Stub of NodeSource that handle the RMNode */
    private NodeSource nodeSource;

    /** {@link NodeSource} NodeSource ID that handle the RMNode */
    private String nodeSourceID;

    /** State of the node */
    private NodeState status;

    /** Create an RMNode Object.
     * A Created node begins to be free.
     * @param node ProActive node deployed.
     * @param vnodeName {@link VirtualNode} name of the node.
     * @param nodeSource {@link NodeSource} Stub of NodeSource that handle the RMNode.
     */
    public RMNodeImpl(Node node, String vnodeName, NodeSource nodeSource) {
        this.node = node;
        this.nodeSource = nodeSource;
        this.nodeSourceID = nodeSource.getName();
        this.vnodeName = vnodeName;
        this.nodeName = node.getNodeInformation().getName();
        this.nodeURL = node.getNodeInformation().getURL();
        this.hostName = node.getNodeInformation().getVMInformation().getHostName();
        this.vmName = node.getNodeInformation().getVMInformation().getName();
        this.scriptStatus = new HashMap<SelectionScript, Integer>();
        this.status = NodeState.FREE;
    }

    /**
     * Returns the name of the node.
     * @return the name of the node.
     */
    public String getNodeName() {
        return this.nodeName;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.rmnode.RMNode#getNode()
     */
    public Node getNode() throws NodeException {
        if (this.status != NodeState.DOWN) {
            return this.node;
        } else {
            throw new NodeException("The node is down");
        }
    }

    /**
     * Returns the NodeInformation object of the RMNode.
     * @return the NodeInformation object of the RMNode.
     */
    public NodeInformation getNodeInformation() {
        return this.node.getNodeInformation();
    }

    /**
     * Returns the Virtual node name of the RMNode.
     * @return the Virtual node name  of the RMNode.
     */
    public String getVNodeName() {
        return this.vnodeName;
    }

    /**
     * Returns the host name of the RMNode.
     * @return the host name of the RMNode.
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Returns the java virtual machine name of the RMNode.
     * @return the java virtual machine name of the RMNode.
     */
    public String getDescriptorVMName() {
        return this.vmName;
    }

    /**
     * Returns the NodeSource name of the RMNode.
     * @return {@link NodeSource} name of the RMNode.
     */
    public String getNodeSourceId() {
        return this.nodeSourceID;
    }

    /**
     * Returns the unique id of the RMNode.
     * @return the unique id of the RMNode represented by its URL.
     */
    public String getNodeURL() {
        return this.node.getNodeInformation().getURL();
    }

    /**
     * change the node's status to busy.
     * @throws NodeException if the node is down.
     */
    public void setBusy() throws NodeException {
        if (this.status != NodeState.DOWN) {
            this.status = NodeState.BUSY;
        } else {
            throw new NodeException("The node is down");
        }
    }

    /**
     * change the node's status to free.
     * @throws NodeException if the node is down.
     */
    public void setFree() throws NodeException {
        if (this.status != NodeState.DOWN) {
            this.status = NodeState.FREE;
        } else {
            throw new NodeException("The node is down");
        }
    }

    /**
     * change the node's status to down.
     */
    public void setDown() {
        this.status = NodeState.DOWN;
    }

    /**
     * change the node's status to 'to be released'.
     * @throws NodeException if the node is down.
     */
    public void setToRelease() throws NodeException {
        if (this.status != NodeState.DOWN) {
            this.status = NodeState.TO_BE_RELEASED;
        } else {
            throw new NodeException("The node is down");
        }
    }

    /**
     * @return true if the node is free, false otherwise.
     */
    public boolean isFree() {
        if (this.status == NodeState.FREE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if the node is busy, false otherwise.
     */
    public boolean isBusy() {
        if (this.status == NodeState.BUSY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if the node is down, false otherwise.
     */
    public boolean isDown() {
        if (this.status == NodeState.DOWN) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if the node is 'to be released', false otherwise.
     */
    public boolean isToRelease() {
        if (this.status == NodeState.TO_BE_RELEASED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return a String shwowing informations about the node.
     */
    @Override
    public String toString() {
        String mes = "\n";

        mes += ("| Name of this Node  :  " + getNodeURL() + "\n");
        mes += "+-----------------------------------------------+\n";
        mes += ("| Node is free ?  	: " + this.isFree() + "\n");
        mes += ("| VNode 		  	: " + vnodeName + "\n");
        mes += ("| Host  		  	: " + getHostName() + "\n");
        mes += ("| Name of the VM 	: " + getNodeInformation().getVMInformation().getDescriptorVMName() + "\n");
        mes += "+-----------------------------------------------+\n";
        return mes;
    }

    /**
     * Execute a selection Script in order to test the Node.
     * If no script handler is defined, create one, and execute the script.
     * @param script Selection script to execute
     * @return Result of the test.
     *
     */
    @SuppressWarnings("unchecked")
    public ScriptResult<Boolean> executeScript(SelectionScript script) {
        if (handler == null) {
            try {
                handler = ScriptLoader.createHandler(this.node);
            } catch (Exception e) {
                return new ScriptResult<Boolean>(new NodeException(
                    "Unable to create Script Handler on node ", e));
            }
        }

        return handler.handle(script);
    }

    /**
     * Clean the node.
     * kill all active objects on the node.
     */
    public synchronized void clean() {
        handler = null;

        try {
            node.killAllActiveObjects();
        } catch (Exception e) {
            logger.error("Error while cleaning the Node", e);
        }
    }

    /**
     * Compare two RMNode objects
     * @return true if the two RMNode objects represent the same Node.
     */
    @Override
    public boolean equals(Object imnode) {
        if (imnode instanceof RMNode) {
            return this.nodeURL.equals(((RMNode) imnode).getNodeURL());
        }

        return false;
    }

    /**
     * @return HashCode of node's ID,
     * i.e. the hashCode of the node's URL.
     */
    @Override
    public int hashCode() {
        return nodeURL.hashCode();
    }

    /**
     * Gives the HashMap of all scripts tested with corresponding results.
     * @return the HashMap of all scripts tested with corresponding results.
     */
    public HashMap<SelectionScript, Integer> getScriptStatus() {
        return scriptStatus;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param rmnode the RMNode object to compare
     * @return an integer
     */
    public int compareTo(RMNode rmnode) {
        if (this.getVNodeName().equals(rmnode.getVNodeName())) {
            if (this.getHostName().equals(rmnode.getHostName())) {
                if (this.getDescriptorVMName().equals(rmnode.getDescriptorVMName())) {
                    return this.getNodeURL().compareTo(rmnode.getNodeURL());
                } else {
                    return this.getDescriptorVMName().compareTo(rmnode.getDescriptorVMName());
                }
            } else {
                return this.getHostName().compareTo(rmnode.getHostName());
            }
        } else {
            return this.getVNodeName().compareTo(rmnode.getVNodeName());
        }
    }

    /**
     * @return the stub of the {@link NodeSource} that handle the RMNode.
     */
    public NodeSource getNodeSource() {
        return this.nodeSource;
    }

    /**
     * Set the NodeSource stub to the RMNode.
     * @param ns Stub of the NodeSource that handle the IMNode.
     */
    public void setNodeSource(NodeSource ns) {
        this.nodeSource = ns;
    }

    /**
     *  build the RMNodeEvent object for the RMNode
     *  @return the RMNodeEvent object related to the RMNode.
     */
    public RMNodeEvent getNodeEvent() {
        return new RMNodeEvent(this.nodeURL, this.getNodeSourceId(), "", this.vnodeName, this.hostName,
            this.vmName, this.status);
    }
}
