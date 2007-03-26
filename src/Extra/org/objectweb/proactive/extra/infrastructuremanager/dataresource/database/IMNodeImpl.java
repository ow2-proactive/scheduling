package org.objectweb.proactive.extra.infrastructuremanager.dataresource.database;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;


public class IMNodeImpl implements IMNode, Serializable {
    // Attributes
    private Node node;
    private String nodeName;
    private String vnodeName;
    private String padName;
    private String hostName;
    private String vmName;
    private boolean free = true;
    private boolean down = false;

    // ----------------------------------------------------------------------//
    // CONSTRUCTOR
    public IMNodeImpl(Node node, String vnodeName, String padName) {
        this.node = node;
        this.vnodeName = vnodeName;
        this.padName = padName;
        this.nodeName = node.getNodeInformation().getName();
        this.hostName = node.getNodeInformation().getHostName();
        this.vmName = node.getNodeInformation().getDescriptorVMName();
    }

    // ----------------------------------------------------------------------//
    // GET
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
    public String toString() {
        String mes = "\n";
        try {
            mes += ("| Name of this Node  :  " + getNodeName() + "\n");
            mes += "+-----------------------------------------------+\n";
            mes += ("| Node is free ?  	: " + free + "\n");
            mes += ("| Name of PAD	  	: " + padName + "\n");
            mes += ("| VNode 		  	: " + vnodeName + "\n");
            mes += ("| Host  		  	: " + getHostName() + "\n");
            mes += ("| Name of the VM 	: " +
            getNodeInformation().getDescriptorVMName() + "\n");
            mes += "+-----------------------------------------------+\n";
        } catch (NodeException e) {
            mes += "Node is down \n";
        }
        return mes;
    }
}
