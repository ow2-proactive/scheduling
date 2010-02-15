/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.jmx.provider.ro;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.ow2.proactive.jmx.provider.JMXProviderUtils;


/**
 * <p>A JMX API connector server that creates Remote Object based connections
 * from remote clients. Usually, such connector servers are made
 * using {@link javax.management.remote.JMXConnectorServerFactory
 * JMXConnectorServerFactory}. However, specialized applications can
 * use this class directly, for example with an {@link ROServerImpl}
 * object.</p>
 *
 * @author The ProActive Team
 */
public final class ROConnectorServer extends JMXConnectorServer {
    private static final int CREATED = 0;
    private static final int STARTED = 1;
    private static final int STOPPED = 2;
    private int state = CREATED;
    private final JMXServiceURL address;
    private final Map<String, Object> env;
    private ROServerImpl roServerLocalRef;

    /**
     * Creates a ROConnectorServer
     * @param url The connector URL
     * @param environment the connector environment, i.e., the package location of the ServerProvider
     * @throws IOException
     */
    public ROConnectorServer(final JMXServiceURL url, final Map<String, ?> environment) throws IOException {
        this(url, environment, (MBeanServer) null);
    }

    /**
     * Creates a ROConnectorServer
     * @param url The connector URL
     * @param  environment the connector environment, i.e., the package location of the ServerProvider
     * @param mbeanServer the MBean server bound with the connector
     * @throws IOException
     */
    public ROConnectorServer(final JMXServiceURL url, final Map<String, ?> environment,
            final MBeanServer mbeanServer) throws IOException {
        this(url, environment, (ROServerImpl) null, mbeanServer);
    }

    /**
     * Creates a ROConnectorServer
     * @param url The connector URL
     * @param  environment the connector environment, i.e., the package location of the ServerProvider
     * @param roServer the Remote Object JMX Server
     * @param mbeanServer the MBean server bound with the connector
     * @throws IOException
     */
    public ROConnectorServer(final JMXServiceURL url, final Map<String, ?> environment,
            final ROServerImpl roServer, final MBeanServer mbeanServer) throws IOException {
        super(mbeanServer);
        if (url == null) {
            throw new IllegalArgumentException("Null JMXService URL");
        }
        this.address = url;
        if (environment == null) {
            this.env = Collections.emptyMap();
        } else {
            this.env = Collections.unmodifiableMap(environment);
        }
        if (roServer == null) {
            final String prt = url.getProtocol();
            if ((prt == null) || !(prt.equals(JMXProviderUtils.RO_PROTOCOL))) {
                throw new MalformedURLException("Invalid protocol type :" + prt);
            }
        }
        this.roServerLocalRef = roServer;
    }

    /**
     * Activates the connector server, that is starts listening for client connections.
     * Calling this method when the connector server is already active has no effect.
     * Calling this method when the connector server has been stopped will generate an IOException.
     * The behavior of this method when called for the first time depends on the parameters that were supplied at construction, as described below.
     * First, an object of a subclass of ROServerImpl is required, to export the connector server through Remote Objects:
     * If an ROServerImpl was supplied to the constructor, it is used.
     */
    public synchronized void start() throws IOException {
        if (this.state == STARTED) {
            return;
        } else if (this.state == STOPPED) {
            throw new IOException("This connector server has been stopped");
        }
        // Check if the connector server is attached to an MBean server 
        final MBeanServer mbs = getMBeanServer();
        if (mbs == null) {
            throw new IllegalStateException("This connector server is not attached with a mbean server");
        }
        // Create an instance the server        
        if (this.roServerLocalRef == null) {
            this.roServerLocalRef = new ROServerImpl(mbs, this.env);
        }
        // Extract the URI from the current address
        final URI uri = JMXProviderUtils.extractURI(this.address);
        try {
            // Use the exposer of the created server to expose it as a remote object
            final RemoteObjectExposer<ROServerImpl> roe = this.roServerLocalRef.getRemoteObjectExposer();
            // Bind under the correct uri
            PARemoteObject.bind(roe, uri);
        } catch (ProActiveException e) {
            throw JMXProviderUtils.newIOException("Failed to create the Remote Object JMX Server " +
                this.address.getURLPath(), e);
        }
        this.state = STARTED;
    }

    /**
     * Deactivates the connector server, that is, stops listening for client connections.
     * Calling this method will also close all client connections that were made by this server.
     * After this method returns, whether normally or with an exception, the connector server will not create any new client connections.
     * Once a connector server has been stopped, it cannot be started again.
     * Calling this method when the connector server has already been stopped has no effect.
     * Calling this method when the connector server has not yet been started will disable the connector server object permanently.
     * If closing a client connection produces an exception, that exception is not thrown from this method.
     * A JMXConnectionNotification is emitted from this MBean with the connection ID of the connection that could not be closed.
     * Closing a connector server is a potentially slow operation. For example, if a client machine with an open connection has crashed, the close operation might have to wait for a network protocol timeout.
     * Callers that do not want to block in a close operation should do it in a separate thread.
     */
    public synchronized void stop() throws IOException {
        if (this.state == STOPPED) {
            // Already stopped
            return;
        }
        this.state = STOPPED;
        // Close the server
        if (this.roServerLocalRef != null) {
            this.roServerLocalRef.close(); // TODO: Make the close method to emit close notif
            this.roServerLocalRef = null;
        }
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
     * Returns the attributes of this connector.
     */
    public Map<String, Object> getAttributes() {
        return this.env;
    }
}