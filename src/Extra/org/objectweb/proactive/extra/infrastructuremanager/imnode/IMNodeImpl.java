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
package org.objectweb.proactive.extra.infrastructuremanager.imnode;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.IMNodeSource;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptHandler;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptLoader;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extra.scheduler.common.scripting.SelectionScript;


public class IMNodeImpl implements IMNode, Serializable {

    /**  */
    private static final long serialVersionUID = -7612176229370058091L;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.IM_DATARESOURCE);
    private HashMap<SelectionScript, Integer> scriptStatus;

    // Attributes
    private Node node;
    private String nodeURL;
    private String nodeName;
    private String vnodeName;
    private String padName;
    private String hostName;
    private String vmName;
    private boolean free = true;
    private boolean down = false;
    private ScriptHandler handler = null;
    private IMNodeSource nodeSource;

    // ----------------------------------------------------------------------//
    // CONSTRUCTOR
    public IMNodeImpl(Node node, String vnodeName, String padName,
        IMNodeSource nodeSource) {
        this.nodeSource = nodeSource;
        this.node = node;
        this.vnodeName = vnodeName;
        this.padName = padName;
        this.nodeName = node.getNodeInformation().getName();
        this.nodeURL = node.getNodeInformation().getURL();
        this.hostName = node.getNodeInformation().getVMInformation()
                            .getHostName();
        this.vmName = node.getNodeInformation().getVMInformation()
                          .getDescriptorVMName();
        this.scriptStatus = new HashMap<SelectionScript, Integer>();
    }

    // ----------------------------------------------------------------------//
    // GET
    public String getNodeURL() {
        return this.nodeURL;
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public Node getNode() throws NodeException {
        if (!isDown()) {
            return this.node;
        } else {
            throw new NodeException("The node is down");
        }
    }

    public NodeInformation getNodeInformation() throws NodeException {
        if (!isDown()) {
            return this.node.getNodeInformation();
        } else {
            throw new NodeException("The node is down");
        }
    }

    public String getVNodeName() {
        return this.vnodeName;
    }

    public String getPADName() {
        return this.padName;
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getDescriptorVMName() {
        return this.vmName;
    }

    // ----------------------------------------------------------------------//
    // IS
    public boolean isFree() throws NodeException {
        // TODO Enlever cette Exception ne servant pas a grand chose
        if (!isDown()) {
            return this.free;
        } else {
            throw new NodeException("The node is down");
        }
    }

    public boolean isDown() {
        return this.down;
    }

    // ----------------------------------------------------------------------//
    // SET
    public void setBusy() throws NodeException {
        if (!isDown()) {
            this.free = false;
        } else {
            throw new NodeException("The node is down");
        }
    }

    public void setFree() throws NodeException {
        if (!isDown()) {
            this.free = true;
        } else {
            throw new NodeException("The node is down");
        }
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    // OTHER SET in the case of the node can migrate.
    // For exemple if the node migrate from other jvm, you must change
    // the attribute Jvm, VNode, ...

    // ----------------------------------------------------------------------//
    // TOSTRING
    @Override
    public String toString() {
        String mes = "\n";

        try {
            mes += ("| Name of this Node  :  " + getNodeURL() + "\n");
            mes += "+-----------------------------------------------+\n";
            mes += ("| Node is free ?  	: " + free + "\n");
            mes += ("| Name of PAD	  	: " + padName + "\n");
            mes += ("| VNode 		  	: " + vnodeName + "\n");
            mes += ("| Host  		  	: " + getHostName() + "\n");
            mes += ("| Name of the VM 	: " +
            getNodeInformation().getVMInformation().getDescriptorVMName() +
            "\n");
            mes += "+-----------------------------------------------+\n";
        } catch (NodeException e) {
            mes += "Node is down \n";
        }

        return mes;
    }

    /**
     * If no script handler is define, create one, and execute the script.
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

    @Override
    public boolean equals(Object imnode) {
        if (imnode instanceof IMNode) {
            return this.nodeURL.equals(((IMNode) imnode).getNodeURL());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return nodeURL.hashCode();
    }

    @SuppressWarnings("unchecked")
    public HashMap<SelectionScript, Integer> getScriptStatus() {
        return scriptStatus;
    }

    public IMNodeSource getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(IMNodeSource nodeSource) {
        this.nodeSource = nodeSource;
    }

    public int compareTo(IMNode imnode) {
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
}
