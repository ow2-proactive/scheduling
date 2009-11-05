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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.jmx.connector;

import java.io.IOException;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.jmx.JMXAdminAuthenticatorImpl;


/**
 * Class to instantiate a Connector Server for an MBean Agent 
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class PAAuthenticationConnectorServer {
    /** logger device */
    private Logger logger;
    /** reference to the Connector Server */
    private JMXConnectorServer cs;

    /**
     * Create a Connector Server with the authentication mechanism based on the name of the Server
     *
     * @param serverUrl the url where the server is deployed and the protocol used (inside the url)
     * @param serverName a given name of the Connector Server
     * @param mbs the MBean Server to connect to (for authorization mechanism)
     * @param authenticationMethod the authentication method (e.g. File based or LDAP)
     * @param logger the logger to use in this class
     * @param anonym Is the authentication anonym or not
     */
    public PAAuthenticationConnectorServer(String serverUrl, String serverName, MBeanServer mbs,
            AuthenticationImpl authentication, boolean anonym, Logger logger) {
        this.logger = logger;
        // Create the enviroment Map
        HashMap<String, Object> env = null;
        /* Switch for the different Views to call the correct Authentication mechanism
           if anonymous, no authentication required */
        if (!anonym) {
            env = new HashMap<String, Object>();
            env.put(JMXConnectorServer.AUTHENTICATOR, new JMXAdminAuthenticatorImpl(authentication));
        }
        // The url where is the MBean Server registry and the server name
        String url = serverUrl + serverName;
        try {
            JMXServiceURL jmxUrl = new JMXServiceURL(url);
            cs = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, env, mbs);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    /**
     * Starts the JMX connector server.
     */
    public void start() {
        try {
            this.cs.start();
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    /**
     * Stops the JMX connector server.
     */
    public void stop() {
        try {
            this.cs.stop();
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    /**
     * Returns the address of the connector server.
     * 
     * @return the address of this connector server, or null if it
     * does not have one or the connector server could not been started.
     */
    public JMXServiceURL getAddress() {
        if (this.cs == null) {
            return null;
        }
        return this.cs.getAddress();
    }
}
