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
package org.objectweb.proactive.examples.jmx.remote.management.client.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.ProActiveJMXConstants;


public class FrameworkConnection {
    private ProActiveConnection connection;
    private String url;
    private boolean connected;
    private static HashMap<String, ProActiveConnection> connectionsMap = new HashMap<String, ProActiveConnection>();

    /**
     * @return the connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @return the connection
     */
    public ProActiveConnection getConnection() {
        return connection;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    public FrameworkConnection() {
    }

    public FrameworkConnection(String url) {
        this.url = url;
    }

    public void connect() throws IOException {
        this.connection = connectionsMap.get(this.url);
        if (this.connection == null) {
            String url = "service:jmx:proactive://" + this.url + "///" +
                ProActiveJMXConstants.SERVER_REGISTERED_NAME;
            JMXServiceURL jmxUrl;
            try {
                jmxUrl = new JMXServiceURL(url);
                JMXConnector conn = JMXConnectorFactory.connect(jmxUrl,
                        ProActiveJMXConstants.PROACTIVE_JMX_ENV);
                this.connection = (ProActiveConnection) conn.getMBeanServerConnection();
                this.connected = true;
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
                throw new IOException(e1.getMessage());
            } catch (IOException e1) {
                e1.printStackTrace();
                throw new IOException(e1.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
                throw new IOException(e1.getMessage());
            }
        }
    }

    public void close() throws IOException {
        try {
            if (this.connected) {
                connectionsMap.remove(this.url);
                this.connected = false;
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
