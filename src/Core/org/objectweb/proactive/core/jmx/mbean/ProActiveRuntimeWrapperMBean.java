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

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 * This MBean represents a ProActiveRuntime.
 * Since the ProActiveRuntime is an abstraction of a Java virtual machine it may aggregate some
 * information from various MXBeans like {@link java.lang.management.ThreadMXBean} or {@link java.lang.management.MemoryMXBean}. 
 * 
 * @author The ProActive Team
 */
public interface ProActiveRuntimeWrapperMBean extends Serializable {

    /**
     * Returns the url of the ProActiveRuntime associated.
     * @return The url of the ProActiveRuntime associated.
     */
    public String getURL();

    /**
     * Returns a list of Object Name used by the MBeans of the nodes containing in the ProActive Runtime.
     * @return a list of Object Name used by the MBeans of the nodes containing in the ProActive Runtime.
     * @throws ProActiveException
     */
    public List<ObjectName> getNodes() throws ProActiveException;

    /**
     * Returns the number of all bodies registered on this runtime.
     * @return the number of all bodies registered on this runtime
     */
    public int getAllBodiesCount();

    /**
     * Returns the number of internal bodies registered on this runtime.
     * <p>
     * A body is considered internal if its reified object implements {@link org.objectweb.proactive.ProActiveInternalObject}.
     * 
     * @return the number of internal bodies registered on this runtime
     */
    public int getInternalBodiesCount();

    /**
     * Returns the number of user bodies registered on this runtime.
     * @return the number of user bodies registered on this runtime
     */
    public int getUserBodiesCount();

    /**
     * Returns the number of half-bodies registered on this runtime.
     * @return the number of half-bodies registered on this runtime.
     */
    public int getHalfBodiesCount();

    /**
     * Returns the current number of live threads including both 
     * daemon and non-daemon threads. This method calls {@link java.lang.management.ThreadMXBean#getThreadCount()}.     
     * @see java.lang.management.ThreadMXBean
     * @return the current number of live threads.
     */
    public int getThreadCount();

    /**
     * Returns the amount of used memory in bytes. This method calls
     * {@link java.lang.management.MemoryMXBean#getHeapMemoryUsage()} to get the used value from the snapshot of memory usage. 
     * @see java.lang.management.MemoryMXBean    
     * @return the amount of used memory in bytes.
     */
    public long getUsedHeapMemory();

    /**
     * Returns the number of classes that are currently loaded in the Java virtual machine.
     * This method calls {@link java.lang.management.ClassLoadingMXBean#getLoadedClassCount()}.
     * @see java.lang.management.ClassLoadingMXBean
     * @return the number of currently loaded classes.
     */
    public int getLoadedClassCount();

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
     * Kills this ProActiveRuntime.
     * @exception Exception if a problem occurs when killing this ProActiveRuntime
     */
    public void killRuntime() throws Exception;

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
