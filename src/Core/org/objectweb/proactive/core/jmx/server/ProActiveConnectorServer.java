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
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.ProActiveJMXConstants;
import org.objectweb.proactive.core.node.NodeException;


/**
 * This connector server is attached to an MBean server.
 * It listens for client connection
 * requests and creates a connection for each one.</p>
 *
 * @author ProActive Team
 */
public class ProActiveConnectorServer extends JMXConnectorServer {
    private JMXServiceURL address;
    private ProActiveServerImpl paServer;
    private Map<String, Object> attributes;
    private static final int CREATED = 0;
    private static final int STARTED = 1;
    private static final int STOPPED = 2;
    private int state = CREATED;
    private UniqueID id;

    /**
     * Creates a ProActiveConnectorServer
     * @param url The connector url
     * @param environment the connector environnement, i.e., the package location of the ServerProvider
     * @throws IOException
     */
    public ProActiveConnectorServer(JMXServiceURL url,
        Map<String, ?> environment) throws IOException {
        this(url, environment, (MBeanServer) null);
    }

    /**
     * Creates a ProActiveConnectorServer
     * @param url The connector url
     * @param  environment the connector environnement, i.e., the package location of the ServerProvider
     * @param mbeanServer the MBean server bound with the connector
     * @throws IOException
     */
    public ProActiveConnectorServer(JMXServiceURL url,
        Map<String, ?> environment, MBeanServer mbeanServer)
        throws IOException {
        this(url, environment, (ProActiveServerImpl) null, mbeanServer);
    }

    /**
     * Creates a ProActiveConnectorServer
     * @param url The connector url
     * @param  environment the connector environnement, i.e., the package location of the ServerProvider
     * @param paServer the proActive JMX Server that
     * @param mbeanServer the MBean server bound with the connector
     * @throws IOException
     */
    public ProActiveConnectorServer(JMXServiceURL url,
        Map<String, ?> environment, ProActiveServerImpl paServer,
        MBeanServer mbeanServer) throws IOException {
        super(mbeanServer);
        if (url == null) {
            throw new IllegalArgumentException("Null JMXService URL");
        }
        if (paServer == null) {
            final String prt = url.getProtocol();
            if ((prt == null) || !(prt.equals("proactive"))) {
                final String msg = "Invalid protocol type :" + prt;
                throw new MalformedURLException(msg);
            }

            final String urlPath = url.getURLPath();
            if (environment == null) {
                this.attributes = new HashMap<String, Object>();
            } else {
                this.attributes = Collections.unmodifiableMap(environment);
            }

            this.address = url;
        }
    }

    /**
     * Activates the connector server, that is starts listening for client connections.
     * Calling this method when the connector server is already active has no effect.
     * Calling this method when the connector server has been stopped will generate an IOException.
     * The behaviour of this method when called for the first time depends on the parameters that were supplied at construction, as described below.
     * First, an object of a subclass of ProActiveServerImpl is required, to export the connector server through ProActive:
     * If an ProActiveServerImpl was supplied to the constructor, it is used.
     */

    //exposes the active object
    public synchronized void start() throws IOException {
        if (this.state == STARTED) {
            return;
        } else if (this.state == STOPPED) {
            throw new IOException("The Server has been stopped");
        }
        MBeanServer mbs = getMBeanServer();

        if (mbs == null) {
            throw new IllegalStateException(
                "This connector is not attached with a mbean server");
        }

        try {
            paServer = new ProActiveServerImpl();
            paServer.setMBeanServer(mbs);
            paServer = (ProActiveServerImpl) ProActiveObject.turnActive(paServer);
            id = paServer.getUniqueID();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        //Server registrations
        //        String url = ClassServer.getUrl();
        //        String url = "";
        //        System.out.println("url = " + url);
        //        String url = ProActiveJMXConstants.SERVER_REGISTERED_NAME;
        String path = this.address.getURLPath();
        int index = path.indexOf(ProActiveJMXConstants.SERVER_REGISTERED_NAME);
        String url = path.substring(index);
        ProActiveObject.register(this.paServer, url);
        state = STARTED;
    }

    /**
     * Deactivates the connector server, that is, stops listening for client connections.
     * Calling this method will also close all client connections that were made by this server.
     * After this method returns, whether normally or with an exception, the connector server will not create any new client connections.
     * Once a connector server has been stopped, it cannot be started again.
     * Calling this method when the connector server has already been stopped has no effect.
     * Calling this method when the connector server has not yet been started will disable the connector server object permanently.
     *  If closing a client connection produces an exception, that exception is not thrown from this method.
     *  A JMXConnectionNotification is emitted from this MBean with the connection ID of the connection that could not be closed.
     *  Closing a connector server is a potentially slow operation. For example, if a client machine with an open connection has crashed, the close operation might have to wait for a network protocol timeout. Callers that do not want to block in a close operation should do it in a separate thread.
     */
    public void stop() {
        try {
            String path = this.address.getURLPath();
            int index = path.indexOf(ProActiveJMXConstants.SERVER_REGISTERED_NAME);
            String url = path.substring(index);
            ProActiveObject.unregister(url);
        } catch (IOException e) {
            System.out.println(
                "Could not unregister ProActiveServerImpl from the registry. " +
                e.getMessage());
        }

        this.paServer = null;
        this.state = STOPPED;
    }

    /**
     * Determines whether the connector server is active.
     * A connector server starts being active when its start method returns successfully and remains active
     * until either its stop method is called or the connector server fails.
     */
    public boolean isActive() {
        return this.state == STARTED;
    }

    /**
     * The address of this connector server.
     */
    public JMXServiceURL getAddress() {
        return this.address;
    }

    /**
     * Returns the attributes of this connector
     */
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public UniqueID getUniqueID() {
        return this.id;
    }
}
