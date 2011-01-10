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
import java.net.URI;
import java.util.Map;
import java.util.Vector;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.ow2.proactive.jmx.provider.JMXProviderUtils;


/**
 * The ROConnector thats exposes the active object responsible of creating the ROConnections.
 * 
 * @author The ProActive Team 
 */
public class ROConnector implements JMXConnector, NotificationListener, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 30L;
    private static final Logger LOGGER = Logger.getLogger(ROConnector.class);
    /** To know if this connector is closed (false by default) */
    private transient boolean closed;
    /** To know if this connector is already connected  (false by default) */
    private transient boolean connected;
    /** The stub on the RO server */
    private transient ROServerImpl roServerStub;
    /** The connection obtained from the RO server */
    private transient ROConnection connection;
    /** The cached connection id */
    private transient String connectiodId;
    /** The JMXServiceURL of the RO JMX Connector server to which this client connector will be connected */
    private final JMXServiceURL jmxServiceURL;
    /** The list of listeners */
    private final Vector<NotificationListener> listeners;

    /**
     * Empty constructor without arguments (needed by ProActive).
     */
    public ROConnector() {
        this.jmxServiceURL = null;
        this.listeners = null;
    }

    private ROConnector(ROServerImpl roServer, JMXServiceURL address, Map<String, ?> environment) {
        if ((roServer == null) && (address == null)) {
            throw new IllegalArgumentException("roServer jmxServiceURL both null");
        }
        this.roServerStub = roServer;
        this.jmxServiceURL = address;
        this.listeners = new Vector<NotificationListener>();
    }

    /**
     * <p>Constructs an <code>ROConnector</code> that will connect the RO
     * connector server with the given address.</p>
     *
     * @param url
     *            the address of the RO connector server.	 
     * @param environment
     *            additional attributes specifying how to make the connection.
     *            This parameter can be null, which is equivalent to an empty Map.
     * @exception IllegalArgumentException
     *                if <code>url</code> is null.
     */
    public ROConnector(final JMXServiceURL url, final Map<String, ?> environment) {
        this(null, url, environment);
    }

    /**
     * <p>Constructs an <code>ROConnector</code> using the given RO server stub.
     *
     * @param roServer a RO stub representing the RO connector server.
     * @param environment additional attributes specifying how to make
     * the connection.  This parameter can be null, which is equivalent to an empty Map.     
     * @exception IllegalArgumentException if <code>roServer</code> is null.
     */
    public ROConnector(final ROServerImpl roServer, final Map<String, ?> environment) {
        this(roServer, null, environment);
    }

    //--------------------------------------------------------------------
    // implements JMXConnector interface
    //--------------------------------------------------------------------

    /**
     * @see javax.management.remote.JMXConnector#connect()
     */
    public void connect() throws IOException {
        this.connect(null);
    }

    /**
     * @see javax.management.remote.JMXConnector#connect(java.util.Map<String,?>)
     */
    public synchronized void connect(final Map<String, ?> env) throws IOException {
        // Check if already connected
        if (this.connected) {
            return;
        }
        // Check if closed
        if (this.closed) {
            throw new IOException("Connector closed");
        }
        try {
            final URI uri = JMXProviderUtils.extractURI(this.jmxServiceURL);
            // Obtain the correct factory from the protocol (uri's scheme)
            final RemoteObjectFactory factory = AbstractRemoteObjectFactory.getRemoteObjectFactory(uri
                    .getScheme());
            // Use the factory to lookup the remote object
            final RemoteObject<ROServerImpl> remoteObject = factory.lookup(uri);
            // Get the proxy from the remote object
            final ROServerImpl roServerStubProxy = remoteObject.getObjectProxy();
            // Get the CREDENTIALS (null is admitted) from the environment 
            final Object credentials = env.get(JMXConnector.CREDENTIALS);
            // Call newClient on the RO Server stub to create a new connection
            this.connection = roServerStubProxy.newClient(credentials);
            this.connected = true;
            // Cache the id of the connection
            this.connectiodId = this.connection.getConnectionId();
        } catch (Exception e) {
            final String message = "Unable to establish a connection with the Remote Object JMX server at " +
                this.jmxServiceURL;
            // Log the exception
            LOGGER.error(message, e);
            // Throw the IOException
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw JMXProviderUtils.newIOException(message, e);
        }
    }

    /**
     * @see javax.management.remote.JMXConnector#getMBeanServerConnection()
     */
    public MBeanServerConnection getMBeanServerConnection() throws IOException {
        return this.getMBeanServerConnection(null);
    }

    /**
     * @see javax.management.remote.JMXConnector#getMBeanServerConnection(javax.security.auth.Subject)
     */
    public synchronized MBeanServerConnection getMBeanServerConnection(Subject delegationSubject)
            throws IOException {
        if (this.closed) {
            throw new IOException("Connection closed");
        }
        if (!this.connected) {
            throw new IOException("Not connected");
        }
        return this.connection;
    }

    /**
     * @see javax.management.remote.JMXConnector#close()
     */
    public synchronized void close() throws IOException {
        if (this.closed) {
            return;
        }
        Exception connectionCloseException = null;
        // Here we should stop the notification handler
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (Exception e) {
                connectionCloseException = e;
            }
        }
        this.connection = null;
        this.roServerStub = null;
        // Set closed state
        this.connected = false;
        this.closed = true;
        // Report the connection close exception
        if (connectionCloseException != null) {
            if (connectionCloseException instanceof IOException) {
                throw (IOException) connectionCloseException;
            }
            if (connectionCloseException instanceof RuntimeException) {
                throw (RuntimeException) connectionCloseException;
            }
            throw JMXProviderUtils.newIOException("Failed to close the connection", connectionCloseException);
        }
    }

    /**
     * @see javax.management.remote.JMXConnector#addConnectionNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    public void addConnectionNotificationListener(NotificationListener listener, NotificationFilter filter,
            Object handback) {
        this.listeners.addElement(listener);
    }

    /**
     * @see javax.management.remote.JMXConnector#removeConnectionNotificationListener(javax.management.NotificationListener)
     */
    public void removeConnectionNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException {
        this.listeners.remove(listener);
    }

    /**
     * @see javax.management.remote.JMXConnector#removeConnectionNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    public void removeConnectionNotificationListener(NotificationListener listener,
            NotificationFilter filter, Object handback) throws ListenerNotFoundException {
        this.listeners.remove(listener);
    }

    /**
     * @see javax.management.remote.JMXConnector#getConnectionId()
     */
    public synchronized String getConnectionId() throws IOException {
        if (this.closed || !this.connected) {
            throw new IOException("Not connected");
        }
        return this.connectiodId;
    }

    //--------------------------------------------------------------------
    // implements NotificationListener interface
    //--------------------------------------------------------------------

    /**
     * @see javax.management.NotificationListener#handleNotification(Notification, Object)
     */
    public void handleNotification(final Notification notification, final Object handback) {
        for (final NotificationListener listener : this.listeners) {
            listener.handleNotification(notification, handback);
        }
    }

    /**
     * <p>The address of this connector.</p>
     *
     * @return the address of this connector, or null if it
     * does not have one.
     */
    public JMXServiceURL getAddress() {
        return this.jmxServiceURL;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder b = new StringBuilder(this.getClass().getName());
        b.append(":");
        if (this.roServerStub != null) {
            b.append(" roServer=").append(this.roServerStub.toString());
        }
        if (this.jmxServiceURL != null) {
            if (this.roServerStub != null) {
                b.append(",");
            }
            b.append(" jmxServiceURL=").append(this.jmxServiceURL.toString());
        }
        return b.toString();
    }
}