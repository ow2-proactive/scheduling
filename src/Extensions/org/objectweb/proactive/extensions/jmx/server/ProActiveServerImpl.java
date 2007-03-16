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
package org.objectweb.proactive.extensions.jmx.server;

import java.io.Serializable;

import javax.management.MBeanServer;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.jmx.ProActiveConnection;
import org.objectweb.proactive.extensions.jmx.ProActiveJMXConstants;


/**
 * The active object representing the connector. This object is responsible of creating ProActive JMX Connections
 * @author vlegrand
 */

/**
 * @author vlegrand
 *
 */

/**
 * @author vlegrand
 *
 */
public class ProActiveServerImpl implements Serializable {
    private static final long serialVersionUID = -5189383875728195134L;
    private transient MBeanServer mbeanServer;

    /**
     *  The ProActive Connector version
     * @return
     */
    public String getVersion() {
        return ProActiveJMXConstants.VERSION;
    }

    /**
     * Returns a new ProActive Connection
     * @return a ProActive Connection that will enables remote calls onto the remote MBean Server
     */
    public ProActiveConnection newClient() {
        ProActiveConnection client = null;
        try {
            client = new ProActiveConnection(this.mbeanServer);
            client = (ProActiveConnection) ProActive.turnActive(client);
            return client;
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        return client;
    }

    /**
     * Sets the MBean server attached to the connector
     * @param mbs The MBean Server bounfd with the connector
     */
    public synchronized void setMBeanServer(MBeanServer mbs) {
        this.mbeanServer = mbs;
    }

    /**
     * @return the Mbean Server bound with  the connector
     */
    public synchronized MBeanServer getMBeanServer() {
        return mbeanServer;
    }
}
