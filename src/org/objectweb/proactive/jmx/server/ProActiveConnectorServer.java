/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.jmx.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.rmi.ClassServer;
import org.objectweb.proactive.jmx.ProActiveJMXConstants;

import com.sun.jmx.remote.util.EnvHelp;


/**
 * @author Virginie Legrand - INRIA
 */
public class ProActiveConnectorServer extends JMXConnectorServer {
    private JMXServiceURL address;
    private ProActiveServerImpl paServer;
    private Map attributes;
    private static final int CREATED = 0;
    private static final int STARTED = 1;
    private static final int STOPPED = 2;
    private int state = CREATED;
    private final static Set openedServers = new HashSet();

    /**
     *
     * @param url
     * @param environment
     * @throws IOException
     */
    public ProActiveConnectorServer(JMXServiceURL url, Map environment)
        throws IOException {
        this(url, environment, (MBeanServer) null);
    }

    /**
     *
     * @param url
     * @param environment
     * @param mbeanServer
     * @throws IOException
     */
    public ProActiveConnectorServer(JMXServiceURL url, Map environment,
        MBeanServer mbeanServer) throws IOException {
        this(url, environment, (ProActiveServerImpl) null, mbeanServer);
    }

    /**
     *
     * @param url
     * @param environment
     * @param paServer
     * @param mbeanServer
     * @throws IOException
     */
    public ProActiveConnectorServer(JMXServiceURL url, Map environment,
        ProActiveServerImpl paServer, MBeanServer mbeanServer)
        throws IOException {
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
                this.attributes = Collections.EMPTY_MAP;
            } else {
                this.attributes = Collections.unmodifiableMap(environment);
                EnvHelp.checkAttributes(this.attributes);
            }

            this.address = url;
        }
    }

    /**
     * Activates the connector server, that is starts listening for client connections.
     * Calling this method when the connector server is already active has no effect.
     *  Calling this method when the connector server has been stopped will generate an IOException.
     *  The behaviour of this method when called for the first time depends on the parameters that were supplied at construction, as described below.
                 First, an object of a subclass of ProActiveServerImpl is required, to export the connector server through ProActive:
                 If an ProActiveServerImpl was supplied to the constructor, it is used.
     */

    //C'est lui qui expose l'objet actif  qui va gerer les connexions
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
            paServer = (ProActiveServerImpl) ProActive.turnActive(paServer);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        System.out.println(
            "Enregistrement du Server JMX pour acces a distance ...");

        //enregistrement de l'objet
        String url = ClassServer.getUrl();
        System.out.println("url = " + url);
        url += ProActiveJMXConstants.SERVER_REGISTERED_NAME;
        ProActive.register(this.paServer, url);
        state = STARTED;
    }

    /**
     * Deactivates the connector server, that is, stops listening for client connections.
     * Calling this method will also close all client connections that were made by this server.
     * After this method returns, whether normally or with an exception, the connector server will not create any new client connections.
             Once a connector server has been stopped, it cannot be started again.
             Calling this method when the connector server has already been stopped has no effect.
             Calling this method when the connector server has not yet been started will disable the connector server object permanently.
             If closing a client connection produces an exception, that exception is not thrown from this method.
             A JMXConnectionNotification is emitted from this MBean with the connection ID of the connection that could not be closed.
             Closing a connector server is a potentially slow operation. For example, if a client machine with an open connection has crashed, the close operation might have to wait for a network protocol timeout. Callers that do not want to block in a close operation should do it in a separate thread.
             This method calls the method close on the connector server's RMIServerImpl object.
     */
    public void stop() throws IOException {
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

    public Map getAttributes() {
        return this.attributes;
    }
}
