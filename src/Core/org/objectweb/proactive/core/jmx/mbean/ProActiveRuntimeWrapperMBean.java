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


/**
 * MBean representing a ProActiveRuntime.
 * @author ProActiveRuntime
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
}
