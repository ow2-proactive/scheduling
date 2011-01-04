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
package org.ow2.proactive.jmx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.ow2.proactive.authentication.Authentication;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.jmx.provider.JMXProviderUtils;


/**
 * This helper class provides a way to establish a JMX connection over RMI.
 * If it fails this helper will try over RO (Remote Object).
 * It can be reused if each open connection is closed with {@link #disconnect()} method. 
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>
 */
public final class JMXClientHelper {
    /** The name of the java property that defines the {@link JMXTransportProtocol} used by JMX client */
    public static final String PA_JMX_CLIENT_PROTOCOL = "pa.jmx.client.protocol";
    /** The authentication interface */
    private final Authentication auth;
    /** The environment that may contains credentials */
    private final Map<String, Object> env;
    /** To know if this helper is connected */
    private boolean isConnected; // false by default
    /** The JMX connector that provides a JMX connection */
    private JMXConnector jmxConnector;
    /** The last happened exception*/
    private Exception lastException;

    /** 
     * Creates a new instance of this helper and puts the credentials in the environment 
     * used by the JMXConnector.
     * 
     * @param auth the authentication interface
     * @param creds the array that contains credentials
     */
    public JMXClientHelper(final Authentication auth, final Object[] creds) {
        this.auth = auth;
        final HashMap<String, Object> environment = new HashMap<String, Object>(2); // 2 because creds + portential RO fallback    	
        environment.put(JMXConnector.CREDENTIALS, creds);
        this.env = environment;
    }

    /** 
     * Creates a new instance of this helper.
     * 
     * @param auth the authentication interface
     * @param creds the environment used by the JMXConnector 
     */
    public JMXClientHelper(final Authentication auth, final Map<String, Object> env) {
        this.auth = auth;
        this.env = env;
    }

    /**
     * Establishes the connection to the connector server by calling {@link #connect(JMXTransportProtocol)} 
     * using the JMXTransportProtocol specified by the {@link #PA_JMX_CLIENT_PROTOCOL} property 
     * that can be {@link JMXTransportProtocol#RMI} or {@link JMXTransportProtocol#RO}.
     * If the property is not defined the {@link JMXTransportProtocol#RMI} protocol is used.
     * 
     * @return <code>true</code> if this helper is connected otherwise return <code>false</code>
     */
    public boolean connect() {
        JMXTransportProtocol protocol = JMXTransportProtocol.RMI;
        final String property = System.getProperty(JMXClientHelper.PA_JMX_CLIENT_PROTOCOL);
        if (property != null &&
            JMXTransportProtocol.valueOf(property.toUpperCase()) == JMXTransportProtocol.RO) {
            protocol = JMXTransportProtocol.RO;
        }
        return this.connect(protocol);
    }

    /**
     * Establishes the connection to the connector server.
     * Handles exception and returns boolean value if success or not.
     * 
     * @param protocol the {@link JMXTransportProtocol}
     * @return <code>true</code> if this helper is connected and <code>false</code> otherwise
     */
    public boolean connect(final JMXTransportProtocol protocol) {
        if (this.isConnected) {
            return true;
        }
        try {
            if (protocol == JMXTransportProtocol.RO) {
                this.jmxConnector = JMXClientHelper.tryJMXoverRO(auth, env);
            } else {
                this.jmxConnector = JMXClientHelper.tryJMXoverRMI(auth, env);
            }
            this.isConnected = true;
        } catch (Exception e) {
            this.lastException = e;
        }
        return this.isConnected;
    }

    /** 
     * Returns true if this client is connected.
     * 
     * @return <code>true</code> if this client helper is connected otherwise return <code>false</code>
     */
    public boolean isConnected() {
        return this.isConnected;
    }

    /**
     * Disconnects the connector of this helper.
     */
    public void disconnect() {
        try {
            this.isConnected = false;
            if (this.jmxConnector != null) {
                this.jmxConnector.close();
            }
            this.jmxConnector = null;
        } catch (Exception e) {
            this.lastException = e;
        } finally {
            this.env.remove(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES);
        }
    }

    /**
     * Returns the connected JMXConnector.
     * 
     * @return the connected connector (may be null) 
     */
    public JMXConnector getConnector() {
        return this.jmxConnector;
    }

    /**
     * Returns the last happened exception.
     * 
     * @return the last exception occurred during precedent operation
     */
    public Exception getLastException() {
        return this.lastException;
    }

    /**
     * Tries to create a a JMXConnector over RMI, with a fall back solution over RO.
     * 
     * @param auth the authentication interface
     * @param env the environment that may contains credentials 
     * @return a new connected JMXConnector
     */
    public static JMXConnector tryJMXoverRMI(final Authentication auth, final Map<String, Object> env) {
        JMXServiceURL jmxRmiServiceURL = null;
        try {
            final String url = auth.getJMXConnectorURL(JMXTransportProtocol.RMI);
            jmxRmiServiceURL = new JMXServiceURL(url);
        } catch (Exception e) {
            // At this point the JMX-RMI infrastructure was not started, try JMX over RO
            return tryJMXoverRO(auth, env);
        }
        // Try to connect using RMI
        try {
            return JMXConnectorFactory.connect(jmxRmiServiceURL, env);
        } catch (IOException e) {
            // At this point there was a communication problem, try to connect over RO
            return tryJMXoverRO(auth, env);
        }
    }

    /**
     * Tries to create a a JMXConnector over RO. Throws a RuntimeException in case of failure.
     * 
     * @param auth the authentication interface
     * @param env the environment that may contains credentials 
     * @return a new connected JMXConnector
     */
    public static JMXConnector tryJMXoverRO(final Authentication auth, final Map<String, Object> env) {
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, JMXProviderUtils.RO_PROVIDER_PKGS);
        JMXServiceURL jmxRoServiceURL = null;
        try {
            final String url = auth.getJMXConnectorURL(JMXTransportProtocol.RO);
            jmxRoServiceURL = new JMXServiceURL(url);
        } catch (Exception e) {
            // At this point the JMX-RO infrastructure was not started throw an Exception
            throw new RuntimeException("Unable to obtain the URL of the JMX-RO connector server due to " +
                e.getMessage());
        }
        try {
            return JMXConnectorFactory.connect(jmxRoServiceURL, env);
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to the JMX-RO connector server", e);
        }
    }
}
