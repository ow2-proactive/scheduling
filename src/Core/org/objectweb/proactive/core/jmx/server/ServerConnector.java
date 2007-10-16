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
package org.objectweb.proactive.core.jmx.server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.ProActiveJMXConstants;


/**
 * Creates and register a ProActive JMX Connector Server
 * @author ProActive Team
 *
 */
public class ServerConnector {
    private MBeanServer mbs;
    private JMXConnectorServer cs;
    private String serverName;

    /**
     * Creates and register a ProActive JMX Connector attached to the platform MBean Server.
     *
     */
    public ServerConnector() {
        /*Retrieve the Platform MBean Server */
        this("serverName");
    }

    public ServerConnector(String serverName) {
        this.mbs = ManagementFactory.getPlatformMBeanServer();
        this.serverName = serverName;

        String url = "service:jmx:proactive:///jndi/proactive://localhost/" +
            ProActiveJMXConstants.SERVER_REGISTERED_NAME + "_" +
            this.serverName;
        JMXServiceURL jmxUrl;
        try {
            jmxUrl = new JMXServiceURL(url);
            Thread.currentThread()
                  .setContextClassLoader(ServerConnector.class.getClassLoader());
            cs = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl,
                    ProActiveJMXConstants.PROACTIVE_JMX_ENV, this.mbs);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the JMX Connector
     * @throws IOException
     */
    public void start() throws IOException {
        this.cs.start();
    }

    public UniqueID getUniqueID() {
        return ((ProActiveConnectorServer) cs).getUniqueID();
    }
}
