/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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

import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.event.WorldObjectListener;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredObjectSet;


/**
 * Holder class for all hosts
 */
public class WorldObject extends AbstractDataObject {
    protected WorldObjectListener listener;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public WorldObject(IC2DObject parent) {
        super(parent);
        controller.log("WorldObject created");
    }

    //
    // -- PUBLIC METHOD -----------------------------------------------
    //
    //
    // Event Listener
    //
    public void registerListener(WorldObjectListener listener) {
        this.messageMonitoringListener = listener;
        this.listener = listener;
    }

    //
    // Host related methods
    //
    //    public HostObject addHostObject(BasicMonitoredObject monitoredHost, MonitoredObjectSet objectSet)
    //        throws java.rmi.RemoteException {
    //        return addHostObject(monitoredHost, objectSet, null);
    //    }
    public HostObject addHostObject(BasicMonitoredObject monitoredHost,
        MonitoredObjectSet objectSet) throws java.rmi.RemoteException {
        String shortHostname = null;
        String hostname = monitoredHost.getFullName();

        //System.out.println("name of added host : " + hostname);
        try {
            shortHostname = UrlBuilder.getHostNameorIP(java.net.InetAddress.getByName(
                        UrlBuilder.removePortFromHost(hostname)));
        } catch (java.net.UnknownHostException e) {
            controller.warn("Hostname " + shortHostname +
                " failed reverse lookup.");
            return null;
        }
        HostObject host = getHostObject(hostname);
        if (host == null) {
            host = new HostObject(this, monitoredHost, objectSet);
            putChild(hostname, host);
            if (listener != null) {
                listener.hostObjectAdded(host);
            }
        } else {
            controller.log("Hostname " + hostname +
                " already monitored, check for new nodes.");
            host.objectSet = objectSet;
        }
        host.createAllNodes();
        //        if (nodeName == null) {
        //            host.createAllNodes();
        //        } else {
        //            host.createOneNode(nodeName);
        //        }
        return host;
    }

    //    public void addHosts() {
    //        RunnableProcessor.getInstance().processRunnable("Create Jini nodes",
    //            new CreateJiniNodeTask(this), controller);
    //    }
    //    public void addHosts(String host) {
    //        RunnableProcessor.getInstance().processRunnable("Create Jini nodes",
    //            new CreateJiniNodeTask(this, host), controller);
    //    }
    public void addHostsObject(HostObject host) {
        if (listener != null) {
            listener.hostObjectAdded(host);
        }
    }

    public void removeHostObject(String hostname) {
        HostObject host = (HostObject) removeChild(hostname);
        if (host != null) {
            if (listener != null) {
                listener.hostObjectRemoved(host);
            }
        }
    }

    public HostObject getHostObject(String hostname) {
        return (HostObject) getChild(hostname);
    }

    public void destroyObject() {
        destroy();
    }

    //
    // -- PROTECTED METHOD -----------------------------------------------
    //
    protected IC2DObject getTypedParent() {
        return (IC2DObject) parent;
    }

    /**
     * destroy this object
     */
    protected synchronized boolean destroy() {
        if (super.destroy()) {
            listener = null;
            return true;
        } else {
            return false;
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
