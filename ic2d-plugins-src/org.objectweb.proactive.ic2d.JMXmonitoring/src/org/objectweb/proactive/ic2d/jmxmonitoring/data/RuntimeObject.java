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
package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.mbean.NodeWrapperMBean;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


/**
 * Represents a Runtime in the IC2D model.
 * @author ProActive Team
 */
public class RuntimeObject extends AbstractData {

    /**
     * All the method names used to notify the observers
     */
    public enum methodName {RUNTIME_KILLED,
        RUNTIME_NOT_RESPONDING,
        RUNTIME_NOT_MONITORED;
    }
    ;
    private HostObject parent;
    private String url;

    //private ProActiveConnection connection;
    private String hostUrlServer;
    private String serverName;
    private ProActiveRuntimeWrapperMBean proxyMBean;

    public RuntimeObject(HostObject parent, String runtimeUrl,
        ObjectName objectName, String hostUrl, String serverName) {
        super(objectName);
        this.parent = parent;

        this.url = FactoryName.getCompleteUrl(runtimeUrl);

        this.hostUrlServer = hostUrl;
        this.serverName = serverName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HostObject getParent() {
        return this.parent;
    }

    @Override
    public void explore() {
        findNodes();
    }

    @Override
    public String getKey() {
        return this.url;
    }

    @Override
    public String getType() {
        return "runtime object";
    }

    @Override
    protected String getHostUrlServer() {
        return this.hostUrlServer;
    }

    @Override
    protected String getServerName() {
        return this.serverName;
    }

    /**
     * Returns the url of this object.
     * @return An url.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Kill this runtime.
     */
    public void killRuntime() {
        new Thread() {
                @Override
                public void run() {
                    Object[] params = {  };
                    String[] signature = {  };
                    invokeAsynchronous("killRuntime", params, signature);
                    runtimeKilled();
                }
            }.start();
    }

    public void runtimeKilled() {
        setChanged();
        notifyObservers(methodName.RUNTIME_KILLED);
        new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    RuntimeObject.this.destroy();
                }
            }.start();
    }

    /**
     * Finds all nodes of this Runtime.
     */
    @SuppressWarnings("unchecked")
    private void findNodes() {
        proxyMBean = MBeanServerInvocationHandler.newProxyInstance(getConnection(),
                getObjectName(), ProActiveRuntimeWrapperMBean.class, false);
        try {
            if (!(getConnection().isRegistered(getObjectName()))) {
                return;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<ObjectName> nodeNames = null;
        try {
            nodeNames = nodeNames = proxyMBean.getNodes();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Map<String, AbstractData> childrenToRemoved = this.getMonitoredChildrenAsMap();

        for (ObjectName name : nodeNames) {
            // Search if the node is a P2P node
            String nodeName = name.getKeyProperty(FactoryName.NODE_NAME_PROPERTY);
            if (nodeName.startsWith(P2PConstants.P2P_NODE_NAME) &&
                    getWorldObject().isP2PHidden()) {
                // We have to skeep this node because it is a P2PNode
                continue;
            }

            NodeWrapperMBean proxyNodeMBean = MBeanServerInvocationHandler.newProxyInstance(getConnection(),
                    name, NodeWrapperMBean.class, false);
            String url = proxyNodeMBean.getURL();

            // We need to have a complete url protocol://host:port/name
            url = FactoryName.getCompleteUrl(url);

            // If this child is a NOT monitored child.
            if (containsChildInNOTMonitoredChildren(url)) {
                continue;
            }

            NodeObject child = (NodeObject) this.getMonitoredChild(url);

            // If this child is not monitored.
            if (child == null) {
                child = new NodeObject(this, url, name);
                String virtualNodeName = child.getVirtualNodeName();
                VNObject vn = getWorldObject().getVirtualNode(virtualNodeName);

                // this virtual node is not monitored
                if (vn == null) {
                    vn = new VNObject(virtualNodeName, child.getJobId(),
                            getWorldObject());
                    getWorldObject().addVirtualNode(vn);
                }
                // Set to the node the parent virtual node.
                child.setVirtualNode(vn);
                vn.addChild(child);
                addChild(child);
            }
            // This child is already monitored, but this child maybe contains some not monitord objects.
            else {
                child.explore();
            }
            // Removes from the model the not monitored or termined nodes.
            childrenToRemoved.remove(child.getKey());
        }

        // Some child have to be removed
        for (Iterator<AbstractData> iter = childrenToRemoved.values().iterator();
                iter.hasNext();) {
            NodeObject child = (NodeObject) iter.next();
            child.destroy();
        }
    }

    @Override
    public String getName() {
        return URIBuilder.getNameFromURI(getUrl());
    }

    @Override
    public ProActiveConnection getConnection() {
        return JMXNotificationManager.getInstance().getConnection(getUrl());
    }

    @Override
    public String toString() {
        return "Runtime: " + getUrl();
    }
}
