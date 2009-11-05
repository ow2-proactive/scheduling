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
import java.security.KeyException;
import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.login.LoginException;

import org.ow2.proactive.authentication.crypto.Credentials;


/**
 * Class to instantiate a Connector Client for an MBean Agent deployed at a given url 
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class PAAuthenticationConnectorClient {
    private MBeanServerConnection connection;
    private JMXConnector connector;
    private String url;

    public PAAuthenticationConnectorClient(String url) {
        this.url = url;
    }

    /**
     * Connect to the JMX Connector
     *  
     * @throws IOException 
     * @throws LoginException 
     */
    public void connect() throws LoginException, IOException {
        throw new LoginException("Anonymous connection is not accepted");
    }

    /**
     * Connect to the JMX Connector
     *  
     * @param username
     * @param password
     * @throws IOException
     * @throws LoginException 
     */
    public void connect(String username, String password) throws IOException, LoginException {
        try {
            Credentials creds = Credentials
                    .createCredentials(username, password, Credentials.getPubKeyPath());
            connect(creds, username);
        } catch (KeyException e) {
            throw new LoginException("Could not encrypt credentials, try to adjust the System property: " +
                Credentials.pubkeyPathProperty + "  " + e.getMessage());
        }
    }

    /**
     * Connect to the JMX Connector
     *  
     * @param cred encapsulates encrypted credentials
     * @param username optional username, execution will go on if null
     * @throws IOException
     * @throws LoginException 
     */
    public void connect(Credentials cred, String username) throws IOException, LoginException {
        /*  build the jMX URL */
        JMXServiceURL jmxUrl = new JMXServiceURL(url);
        // Create the enviroment Map
        HashMap<String, Object> env = new HashMap<String, Object>();

        Object[] creds = new Object[2];
        creds[0] = cred;
        creds[1] = username;

        env.put(JMXConnector.CREDENTIALS, creds);

        /* connect to the connector server, passing the enviroment  */
        this.connector = JMXConnectorFactory.connect(jmxUrl, env);
        /* retrieve the JMX Connection that will enable the remote calls onto the remote MBean server */
        this.connection = this.connector.getMBeanServerConnection();
    }

    /**
     * Returns the MBeanServerConnection that will enable the calls onto the remote MBean Server
     * @return the JMX Connection
     */
    public MBeanServerConnection getConnection() {
        return this.connection;
    }

    /**
     * Returns the JMX Remote connector that can be used to establish a connection to a connector server.
     * @return the JMX Connector Server
     */
    public JMXConnector getConnector() {
        return this.connector;
    }
}
