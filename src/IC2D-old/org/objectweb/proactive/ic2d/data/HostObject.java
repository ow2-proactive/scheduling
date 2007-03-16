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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.data;

import java.util.Iterator;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.event.HostObjectListener;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredHost;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredNode;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredObjectSet;
import org.objectweb.proactive.ic2d.util.HostRTFinder;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


/**
 * Holder class for the host data representation
 */
public class HostObject extends AbstractDataObject {

    /** Name of this Host  */
    protected String hostname;

    /** OS */
    protected String os;
    protected HostRTFinder nodeFinder;
    protected HostObjectListener listener;

    //following represents the nodes found on this host
    protected MonitoredObjectSet objectSet;
    protected BasicMonitoredObject monitoredHost;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public HostObject(WorldObject parent, BasicMonitoredObject monitoredHost,
        MonitoredObjectSet objectSet) {
        super(parent);
        this.objectSet = objectSet;

        //	Test if there is port defined, then remove it to see if hostname exists
        try {
            this.hostname = monitoredHost.getFullName();
            String shortHostname = UrlBuilder.getHostNameorIP(java.net.InetAddress.getByName(
                        UrlBuilder.removePortFromHost(hostname)));
            this.monitoredHost = monitoredHost;

            //controller.log("HostObject "+this.hostname+ " created");
        } catch (java.net.UnknownHostException e) {
            this.hostname = monitoredHost.getFullName();
            //controller.warn("Hostname " + hostname + " failed reverse lookup.");
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    @Override
    public String toString() {
        return "Host: " + hostname + "\n" + super.toString();
    }

    /**
     * @return Returns the protocol used to monitor this host
     */
    public String getMonitoredProtocol() {
        return ((MonitoredHost) monitoredHost).getMonitorProtocol();
    }

    public void createAllNodes() {
        //MonitoredObjectSet objectSet = asso.getValues(monitoredHost, 2, null);
        Iterator iter = objectSet.iterator();
        if (!iter.hasNext()) {
            //we check first if nodes were found
            controller.warn("No Node objects were found on host " + hostname +
                " !");
        }
        boolean hideP2PNode = new Boolean(System.getProperty(
                    P2PConstants.HIDE_P2PNODE_MONITORING)).booleanValue();

        while (iter.hasNext()) {
            MonitoredNode monitoredNode = (MonitoredNode) iter.next();
            Node node = monitoredNode.getNode();
            String nodeName = node.getNodeInformation().getName();

            if (hideP2PNode &&
                    (nodeName.compareTo(P2PConstants.P2P_NODE_NAME) == 0)) {
                continue;
            }

            VMObject vmObject = findVMObjectHavingExistingNode(nodeName);

            if (vmObject == null) {
                // new NodeObject
                addVMObject(node);
            } else {
                controller.log("The node " + nodeName +
                    " is already known by host " + hostname +
                    " look for new objects");
                vmObject.sendEventsForAllActiveObjects();
            }
        }
    }

    //
    // accessor methods
    //
    public String getHostName() {
        return hostname;
    }

    public String getOperatingSystem() {
        return os;
    }

    //
    // Event Listener
    //
    public void registerListener(HostObjectListener listener) {
        this.messageMonitoringListener = listener;
        this.listener = listener;
    }

    //
    // VM related methods
    //

    /**
     * Register the node
     */
    public VMObject addVMObject(Node node) {
        java.rmi.dgc.VMID vmid = node.getNodeInformation().getVMID();
        String protocolId = node.getNodeInformation().getCreationProtocolID();
        VMObject vmObject = getVMObject(vmid);

        if (vmObject != null) {
            controller.log("The node " + node.getNodeInformation().getURL() +
                " belongs to an already existing vm id=" + vmid);

            // add the node to the existing vm in case it doesn't exist
            vmObject.addNodeObject(node);

            // refresh ActiveObject for this vm
            vmObject.sendEventsForAllActiveObjects();

            return vmObject;
        }

        try {
            vmObject = new VMObject(this, vmid, node, protocolId);
            putChild(vmid, vmObject);
            controller.log("The node " + node.getNodeInformation().getURL() +
                " has been found on vm id=" + vmid);

            if (listener != null) {
                listener.vmObjectAdded(vmObject);
            }

            if (os == null) {
                os = vmObject.getSystemProperty("os.name");

                if (listener != null) {
                    listener.operatingSystemFound(os);
                }
            }

            return vmObject;
        } catch (ActiveObjectCreationException e) {
            controller.log("Cannot create the spy on host " + hostname +
                " on node " + node.getNodeInformation().getURL(), e, false);

            return null;
        } catch (NodeException e) {
            controller.log("Problem with the node " +
                node.getNodeInformation().getURL(), e, false);

            return null;
        }
    }

    public void removeVMObject(java.rmi.dgc.VMID id) {
        VMObject vmObject = (VMObject) removeChild(id);

        if ((vmObject != null) && (listener != null)) {
            listener.vmObjectRemoved(vmObject);
        }
    }

    public VMObject getVMObject(java.rmi.dgc.VMID id) {
        return (VMObject) getChild(id);
    }

    @Override
    public void destroyObject() {
        getTypedParent().removeHostObject(hostname);
    }

    public synchronized VMObject findVMObjectHavingExistingNode(String nodeName) {
        if (getChildObjectsCount() == 0) {
            return null;
        }

        java.util.Iterator iterator = childsIterator();

        while (iterator.hasNext()) {
            VMObject vmObject = (VMObject) iterator.next();

            if (vmObject.getNodeObject(nodeName) != null) {
                controller.log("Found that vm id=" + vmObject.getID() +
                    " own the node " + nodeName);

                return vmObject;
            }
        }

        return null;
    }

    //
    // -- PROTECTED METHOD -----------------------------------------------
    //
    protected WorldObject getTypedParent() {
        return (WorldObject) parent;
    }

    @Override
    protected synchronized boolean destroy() {
        // destroy all childs
        if (super.destroy()) {
            // remove ref on other object
            listener = null;
            nodeFinder = null;

            return true;
        } else {
            return false;
        }
    }
}
