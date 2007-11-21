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
import java.util.List;

import javax.management.ObjectName;

import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 * MBean representing a Node.
 * @author ProActive Team
 */
public interface NodeWrapperMBean extends Serializable {

    /**
     * Returns the url of the node.
     * @return The url of the node.
     */
    public String getURL();

    /**
     * Returns a list of Object Name used by the MBeans of the active objects containing in the Node.
     * @return The list of ObjectName of MBeans representing the active objects of this node.
     */
    public List<ObjectName> getActiveObjects();

    /**
     * Sends a new notification.
     * @param type The type of the notification. See {@link NotificationType}
     */
    public void sendNotification(String type);

    /**
     * Sends a new notification.
     * @param type Type of the notification. See {@link NotificationType}
     * @param userData The user data.
     */
    public void sendNotification(String type, Object userData);

    /**
     * Returns the object name used for this MBean.
     * @return The object name used for this MBean.
     */
    public ObjectName getObjectName();

    /**
     * Returns the name of the virtual node by which the node
     * has been instancied if any.
     * @return the name of the virtual node.
     */
    public String getVirtualNodeName();

    /**
     * Returns the jobId.
     * @return The jobId.
     */
    public String getJobId();

    /**
     * Returns the security manager.
     * @param user
     * @return the security manager
     */
    public ProActiveSecurityManager getSecurityManager(Entity user);

    /**
     * Modify the security manager.
     * @param user
     * @param policyServer
     */
    public void setSecurityManager(Entity user, PolicyServer policyServer);
}
