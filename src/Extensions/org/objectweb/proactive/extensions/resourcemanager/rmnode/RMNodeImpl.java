/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.resourcemanager.rmnode;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.resourcemanager.common.NodeState;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extensions.scheduler.common.scripting.ScriptHandler;
import org.objectweb.proactive.extensions.scheduler.common.scripting.ScriptLoader;
import org.objectweb.proactive.extensions.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extensions.scheduler.common.scripting.SelectionScript;


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
 * @author ProActive team.
 *
 */
public class RMNodeImpl implements RMNode, Serializable {

    /** serial version UID */
    private static final long serialVersionUID = -7612176229370058091L;

    /** associated logger */
    private static Logger logger = ProActiveLogger.getLogger(Loggers.RM_DATARESOURCE);

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

    /** {@link ProActiveDescriptor} name of the node */
    private String padName;

    /** Host name of the node */
    private String hostName;

    /** Java virtual machine name of the node */
    private String vmName;

    /** Script handled, manage scripts launching and results recovering */
    private ScriptHandler handler = null;

    /** {@link NodeSource} Stub of NodeSource that handle the RMNode */
    private NodeSource nodeSource;

    /** State of the node */
    private NodeState status;

    /** Create an RMNode Object.
     * A Created node begins to be free.
     * @param node ProActive node deployed.
     * @param vnodeName {@link VirtualNode} name of the node.
     * @param padName {@link ProActiveDescriptor} name of the node.
     * @param nodeSource {@link NodeSource} Stub of NodeSource that handle the RMNode.
     */
    public RMNodeImpl(Node node, String vnodeName, String padName,
        NodeSource nodeSource) {
        this.node = node;
        this.nodeSource = nodeSource;
        this.vnodeName = vnodeName;
        this.padName = padName;
        this.nodeName = node.getNodeInformation().getName();
        this.nodeURL = node.getNodeInformation().getURL();
        this.hostName = node.getNodeInformation().getVMInformation()
                            .getHostName();
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
     * Returns the ProActive Node object of the RMNode.
     * @return the ProActive Node object of the RMNode.
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
     * Returns the {@link VirtualNode} name of the RMNode.
     * @return the Virtual node name  of the RMNode.
     */
    public String getPADName() {
        return this.padName;
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
        return this.nodeSource.getSourceId();
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
        mes += ("| Name of PAD	  	: " + padName + "\n");
        mes += ("| VNode 		  	: " + vnodeName + "\n");
        mes += ("| Host  		  	: " + getHostName() + "\n");
        mes += ("| Name of the VM 	: " +
        getNodeInformation().getVMInformation().getDescriptorVMName() + "\n");
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
    @SuppressWarnings("unchecked")
    public HashMap<SelectionScript, Integer> getScriptStatus() {
        return scriptStatus;
    }

    /**
     * Compare two RMNode objects.
     * @return 0 if two Nodes object are the representation of the same node.
     */
    public int compareTo(RMNode imnode) {
        if (this.getPADName().equals(imnode.getPADName())) {
            if (this.getVNodeName().equals(imnode.getVNodeName())) {
                if (this.getHostName().equals(imnode.getHostName())) {
                    if (this.getDescriptorVMName()
                                .equals(imnode.getDescriptorVMName())) {
                        return this.getNodeURL().compareTo(imnode.getNodeURL());
                    } else {
                        return this.getDescriptorVMName()
                                   .compareTo(imnode.getDescriptorVMName());
                    }
                } else {
                    return this.getHostName().compareTo(imnode.getHostName());
                }
            } else {
                return this.getVNodeName().compareTo(imnode.getVNodeName());
            }
        }

        return this.getPADName().compareTo(imnode.getPADName());
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
     * memorize a result of a selection script,
     * node verify conditions of the selection.
     * @param script script tested.
     */
    public void setVerifyingScript(SelectionScript script) {
        if (scriptStatus.containsKey(script)) {
            scriptStatus.remove(script);
        }
        scriptStatus.put(script, RMNode.VERIFIED_SCRIPT);
    }

    /**
     * memorize a result of a selection script,
     * node does not verify conditions of the selection.
     * @param script script tested.
     */
    public void setNotVerifyingScript(SelectionScript script) {
        if (scriptStatus.containsKey(script)) {
            int status = scriptStatus.remove(script);
            if (status == RMNode.NOT_VERIFIED_SCRIPT) {
                scriptStatus.put(script, RMNode.NOT_VERIFIED_SCRIPT);
            } else {
                scriptStatus.put(script, RMNode.NO_LONGER_VERIFIED_SCRIPT);
            }
        } else {
            scriptStatus.put(script, RMNode.NOT_VERIFIED_SCRIPT);
        }
    }

    /**
     *  build the RMNodeEvent object for the RMNode
     *  @return the RMNodeEvent object related to the RMNode.
    */
    public RMNodeEvent getNodeEvent() {
        return new RMNodeEvent(this.nodeURL, this.getNodeSourceId(),
            this.padName, this.vnodeName, this.hostName, this.vmName,
            this.status);
    }
}
