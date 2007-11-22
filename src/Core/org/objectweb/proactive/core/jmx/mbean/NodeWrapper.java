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
package org.objectweb.proactive.core.jmx.mbean;

import java.io.Serializable;
import java.net.URI;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.filter.ProActiveInternalObjectFilter;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.runtime.LocalNode;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of a NodeWrapper MBean.
 * @author ProActive Team
 */
public class NodeWrapper extends NotificationBroadcasterSupport
    implements Serializable, NodeWrapperMBean {

    /** JMX Logger */
    private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);

    /** ObjectName of this MBean */
    private transient ObjectName objectName;

    /** The Node wrapped in this MBean */
    private LocalNode localNode;

    /** The url of the LocalNode */
    private String nodeUrl;

    /** The url of the ProActive Runtime */
    private String runtimeUrl;

    /** Used by the JMX notifications */
    private long counter = 1;

    public NodeWrapper() {

        /* Empty Constructor required by JMX */
    }

    /**
     * Creates a new NodeWrapper MBean, representing a Local Node.
     * @param objectName
     * @param localNode
     * @param runtimeUrl
     */
    public NodeWrapper(ObjectName objectName, LocalNode localNode,
        String runtimeUrl) {
        this.objectName = objectName;
        this.localNode = localNode;
        this.runtimeUrl = runtimeUrl;

        URI runtimeURI = URI.create(runtimeUrl);
        String host = runtimeURI.getHost();
        String protocol = URIBuilder.getProtocol(runtimeURI);
        int port = runtimeURI.getPort();

        this.nodeUrl = URIBuilder.buildURI(host, localNode.getName(), protocol,
                port).toString();
    }

    public String getURL() {
        return this.nodeUrl;
    }

    public List<ObjectName> getActiveObjects() {
        List<UniversalBody> activeObjects = this.localNode.getActiveObjects(new ProActiveInternalObjectFilter());

        List<ObjectName> onames = new ArrayList<ObjectName>();
        for (UniversalBody ub : activeObjects) {
            UniqueID id = ub.getID();

            //System.out.println("NodeWrapper.getActiveObjects() " + id);
            ObjectName name = FactoryName.createActiveObjectName(id);
            onames.add(name);
        }
        return onames;
    }

    public ObjectName getObjectName() {
        return this.objectName;
    }

    public String getVirtualNodeName() {
        return localNode.getVirtualNodeName();
    }

    public String getJobId() {
        return localNode.getJobId();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.NodeWrapperMBean#getJobIdAndVirtualNodeName()
     */
    public String[] getJobIdAndVirtualNodeName() {
        return new String[] {
            this.localNode.getJobId(), this.localNode.getVirtualNodeName()
        };
    }

    public void sendNotification(String type) {
        sendNotification(type, null);
    }

    public void sendNotification(String type, Object userData) {
        ObjectName source = getObjectName();

        if (logger.isDebugEnabled()) {
            logger.debug("[" + type +
                "]#[NodeWrapper.sendNotification] source=" + source +
                ", userData=" + userData);
        }
        Notification notification = new Notification(type, source, counter++);
        notification.setUserData(userData);
        sendNotification(notification);
    }

    public ProActiveSecurityManager getSecurityManager(Entity user) {
        try {
            return this.localNode.getProActiveSecurityManager(user);
        } catch (AccessControlException e) {
            e.printStackTrace();
            return null;
        } catch (SecurityNotAvailableException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setSecurityManager(Entity user, PolicyServer policyServer) {
        try {
            this.localNode.setProActiveSecurityManager(user, policyServer);
        } catch (AccessControlException e) {
            e.printStackTrace();
        } catch (SecurityNotAvailableException e) {
            e.printStackTrace();
        }
    }
}
