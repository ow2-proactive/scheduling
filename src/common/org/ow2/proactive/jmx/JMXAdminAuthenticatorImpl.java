/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.jmx;

import java.util.Collections;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.jmx.naming.JMXProperties;


/**
 * Class to perform the admin authentication for the JMX MBean Server 
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class JMXAdminAuthenticatorImpl implements JMXAuthenticator {

    /** reference to the authentication Object */
    private AuthenticationImpl authentication;

    /**
     * Constructor to assign the values to the global variables 
     *
     * @param authentication the authentication object that is actually used 
     */
    public JMXAdminAuthenticatorImpl(AuthenticationImpl authentication) {
        this.authentication = authentication;
    }

    /**
     * This method is automatically called when a JMX Client tries to connect to the MBean Server referred
     * by the connector, it authenticates with the method logAsAdmin() of the Scheduler
     * 
     * @return Subject the principal JMX User Subject
     * @param credentials the JMX user credentials
     */
    public Subject authenticate(Object credentials) {
        // Verify that credentials is of type String[]
        if (!(credentials instanceof String[])) {
            // Special case for null so we get a more informative message
            if (credentials == null) {
                throw new SecurityException("Credentials required");
            }
            throw new SecurityException("Credentials should be String[]");
        }
        final String[] aCredentials = (String[]) credentials;
        // Perform authentication
        String username = (String) aCredentials[0];
        String password = (String) aCredentials[1];
        try {
            // Try to authenticate as Admin based on the authenticationType parameter
            authentication.loginAs(JMXProperties.JMX_ADMIN, new String[] { JMXProperties.JMX_ADMIN },
                    username, password);
        } catch (LoginException le) {
            throw new SecurityException("Invalid credentials for " + JMXProperties.JMX_ADMIN);
        }
        // Authentication as (Admin/User) successfully, return a new JMX Subject principal
        return new Subject(true, Collections.singleton(new JMXPrincipal((String) aCredentials[0])),
            Collections.EMPTY_SET, Collections.EMPTY_SET);
    }
}
