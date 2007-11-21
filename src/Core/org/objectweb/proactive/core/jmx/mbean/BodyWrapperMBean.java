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
import java.util.Collection;

import javax.management.ObjectName;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 * MBean representing an active object.
 * @author ProActive Team
 */
public interface BodyWrapperMBean extends Serializable {

    /**
     * Returns the unique id.
     * @return The unique id of this active object.
     */
    public UniqueID getID();

    /**
     * Returns the name of the body of the active object that can be used for displaying information
     * @return the name of the body of the active object
     */
    public String getName();

    /**
     * Returns the url of the node containing the active object.
     * @return Returns the url of the node containing the active object
     */
    public String getNodeUrl();

    /**
     * Send a new notification.
     * @param type The type of the notification. See {@link NotificationType}
     */
    public void sendNotification(String type);

    /**
     * Send a new notification.
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
     * Returns an array of timers.
     * @return an array of timers
     */
    public Object[] getTimersSnapshotFromBody() throws Exception;

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

    /**
     * Migrate the body to the given node.
     * @param nodeUrl
     * @throws MigrationException
     */
    public void migrateTo(String nodeUrl) throws MigrationException;

    /**
     *  returns a list of outgoing active object references.
     */
    public Collection<UniqueID> getReferenceList();

    public String getDgcState();
}
