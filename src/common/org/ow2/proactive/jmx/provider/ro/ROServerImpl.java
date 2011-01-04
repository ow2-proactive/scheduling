/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.jmx.provider.ro;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.security.auth.Subject;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.util.URIBuilder;
import org.ow2.proactive.jmx.provider.JMXProviderUtils;


/**
 * <p>
 * A remote object representing a connector server. Remote clients can make
 * connections using the {@link #newClient(Object)} method. This method returns
 * a Remote Object representing the connection.
 * </p>
 * 
 * <p>
 * User code does not usually reference this class directly. Remote Object
 * connection servers are usually created with the class
 * {@link ROConnectorServer}. Remote clients usually create connections either
 * with {@link javax.management.remote.JMXConnectorFactory} or by instantiating
 * {@link ROConnector}.
 * </p>
 * 
 * @author The ProActive Team
 */
public class ROServerImpl implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(ROServerImpl.class);
    /** The current connection number */
    private static int connectionNumber;
    /** The reference on the MBeanServer */
    private transient MBeanServer mbeanServer;
    /** The environment containing the attributes */
    private final Map<String, ?> env;
    /** The access control context */
    private final AccessControlContext context;
    /** All known weakly referenced connection exposers identified by the connection id */
    private final Map<String, WeakReference<RemoteObjectExposer<ROConnection>>> connections;
    /** The remote object exposer of this server */
    private final RemoteObjectExposer<ROServerImpl> roe;

    /**
     * Empty constructor without arguments.
     */
    public ROServerImpl() {
        this.mbeanServer = null;
        this.env = null;
        this.context = null;
        this.connections = null;
        this.roe = null;
    }

    /**
     * Constructs a new <code>ROServerImpl</code>.
     * 
     * @param mbs
     *            the MBean server
     * @param env
     *            the environment containing attributes for the new
     *            <code>ROServerImpl</code>
     */
    public ROServerImpl(final MBeanServer mbs, final Map<String, ?> env) {
        this.mbeanServer = mbs;
        this.env = env;
        this.context = AccessController.getContext();
        this.connections = new ConcurrentHashMap<String, WeakReference<RemoteObjectExposer<ROConnection>>>();
        this.roe = new RemoteObjectExposer<ROServerImpl>(ROServerImpl.class.getName(), this);
    }

    /**
     * Returns a new ROConnection.
     * 
     * @return a RO Connection that will enables remote calls onto the remote
     *         MBean Server
     */
    public synchronized ROConnection newClient(final Object credentials) throws IOException {
        if (this.mbeanServer == null) {
            throw new IllegalStateException("Not attached to an MBean server");
        }
        // Authenticate to get the subject
        Subject subject = null;
        final JMXAuthenticator authenticator = (JMXAuthenticator) this.env
                .get(JMXConnectorServer.AUTHENTICATOR);
        if (authenticator != null) {
            try {
                subject = authenticator.authenticate(credentials);
            } catch (SecurityException e) {
                LOGGER.warn("Authentication failed", e);
                throw e;
            }
        }
        final int num = ++ROServerImpl.connectionNumber;
        // Create the id of the connection
        final String connectionId = ROServerImpl.createConnectionID(subject, num);
        try {
            final ROConnection connection = new ROConnection(this.mbeanServer, connectionId, this, subject,
                this.context);
            // Create a remote object exposer for this object			
            final RemoteObjectExposer<ROConnection> roe = new RemoteObjectExposer<ROConnection>(
                ROConnection.class.getName(), connection);
            // Use a weak reference and put it in the hash map  			
            this.connections.put(connectionId, new WeakReference<RemoteObjectExposer<ROConnection>>(roe));
            // Get the default base uri for all remote objects
            final URI baseURI = JMXProviderUtils.getBaseURI();
            // Generate the uri (default base uri + class simple name + connection number)
            final URI uri = URIBuilder.buildURI(baseURI, ROConnection.class.getSimpleName() + num);
            // Bind under the correct uri
            return PARemoteObject.bind(roe, uri);
        } catch (Exception e) {
            final String message = "Unable to create the client connection " + connectionId;
            LOGGER.error(message, e);
            throw JMXProviderUtils.newIOException(message, e);
        }
    }

    /**
     * Sets the MBean server attached to this connector.
     * 
     * @param mbs
     *            The MBean server bound to this connector
     */
    public synchronized void setMBeanServer(final MBeanServer mbs) {
        this.mbeanServer = mbs;
    }

    /**
     * Returns the MBean server bound with the connector.
     * 
     * @return the MBean server bound with the connector
     */
    public synchronized MBeanServer getMBeanServer() {
        return this.mbeanServer;
    }

    /**
     * Closes this server.
     * 
     * @throws IOException if the close operation failed
     */
    public synchronized void close() throws IOException {
        // First close the server
        IOException serverException = null;
        try {
            this.internalCloseRemoteObject(this.roe);
        } catch (ProActiveException e) {
            serverException = JMXProviderUtils.newIOException("Unable to close the server " +
                this.roe.getURL(), e);
        }

        // Even if the server was not closed properly
        // try to close all the connections
        IOException connectionCloseException = null;
        for (final Entry<String, WeakReference<RemoteObjectExposer<ROConnection>>> entry : this.connections
                .entrySet()) {
            String connectionId = entry.getKey();
            WeakReference<RemoteObjectExposer<ROConnection>> weakReference = entry.getValue();
            RemoteObjectExposer<ROConnection> roe = weakReference.get();
            if (roe == null) {
                this.connections.remove(connectionId);
            } else {
                try {
                    this.internalCloseRemoteObject(roe);
                } catch (ProActiveException e) {
                    if (connectionCloseException == null) {
                        connectionCloseException = JMXProviderUtils.newIOException(
                                "Unable to close the connection " + connectionId, e);
                    }
                }
            }
        }
        // If there was an exception when closing the server re-throw it once all connections
        // were closed (at least attempted)
        if (serverException != null) {
            throw serverException;
        }
        // If there was an exception when closing the connections re-throw it now (the first one) 
        if (connectionCloseException != null) {
            throw connectionCloseException;
        }
    }

    private void internalCloseRemoteObject(final RemoteObjectExposer<?> roe) throws ProActiveException {
        // First try to unregisterAll
        ProActiveException unregisterAllException = null;
        try {
            roe.unregisterAll();
        } catch (ProActiveException e) {
            // In case of exception keep it			
            unregisterAllException = e;
        }
        // Since the unexportAll method throws an exception it cannot be placed in a finally block
        // so try it even if there was a exception on unregisterAll 		
        ProActiveException unexportAllException = null;
        try {
            roe.unexportAll();
        } catch (ProActiveException e) {
            // In case of exception keep it
            unexportAllException = e;
        }
        // If there was an exception on unregisterAll report it 
        if (unregisterAllException != null) {
            throw unregisterAllException;
        }
        // If there was an exception on unexportAll report it
        if (unexportAllException != null) {
            throw unexportAllException;
        }
    }

    // -------------------------------------------------------------------------
    // PROTECTED METHODS
    // -------------------------------------------------------------------------

    protected void closeConnectionById(final String connectionId) throws IOException {
        // This method is executed in case of a closed server
        // or a closed connection. We must ensure the connection is in both cases removed from the list
        // of connections.

        final WeakReference<RemoteObjectExposer<ROConnection>> weak = this.connections.remove(connectionId);
        if (weak == null) {
            return;
        }
        final RemoteObjectExposer<ROConnection> roe = weak.get();
        if (roe == null) {
            return;
        }
        // Try to close the remote object
        try {
            this.internalCloseRemoteObject(roe);
        } catch (ProActiveException e) {
            throw JMXProviderUtils.newIOException("Unable to close the connection " + connectionId, e);
        }
    }

    /**
     * Returns the remote object exposer of this server.
     */
    protected RemoteObjectExposer<ROServerImpl> getRemoteObjectExposer() {
        return this.roe;
    }

    // -------------------------------------------------------------------------
    // PRIVATE STATIC METHODS
    // -------------------------------------------------------------------------

    /**
     * See JSR 160 specification at javax/management/remote/package-summary.html
     * The formal grammar is: ConnectionId: Protocol : ClientAddressopt Space
     * ClientIdopt Space ArbitraryText ClientAddress: // HostAddress
     * ClientPortopt ClientPort : HostPort
     */
    private static String createConnectionID(
    // final String clientAddress,
            // final int clientPort,
            final Subject subject, final int num) {
        // Start with Protocol
        final StringBuilder strBuilder = new StringBuilder(JMXProviderUtils.RO_PROTOCOL);
        strBuilder.append(':');
        // ClientAddress
        // if (clientAddress != null) {
        // strBuilder.append("//").append(clientAddress);
        // }
        // ClientPort
        // if (clientPort >= 0) {
        // strBuilder.append(':').append(clientPort);
        // }
        strBuilder.append(' ');
        // ClientId
        if (subject != null) {
            for (final Principal p : subject.getPrincipals()) {
                String name = p.getName();
                // Must not contain spaces
                name = name.replace(' ', '_');
                strBuilder.append(name);
            }
        }
        strBuilder.append(' ');
        // ArbitraryText
        strBuilder.append(num);
        return strBuilder.toString();
    }
}