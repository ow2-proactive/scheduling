/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.authentication;

import java.io.Serializable;
import java.security.PublicKey;

import javax.management.JMException;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;


/**
 * 
 * Authentication interface.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public interface Authentication extends Loggable, Serializable {

    /**
     * Checks whether an authentication is activated or in other words is ready to authenticate users.
     * 
     * @return true if it is activated.
     */
    boolean isActivated();

    /**
     * Request this Authentication's public key for credentials encryption
     *
     * @return this Authentication's public key
     * @throws LoginException the key could not be retrieved
     */
    PublicKey getPublicKey() throws LoginException;

    /**
     * Return the JMX connector server URL used to contact this instance.
     *
     * @return the string representation of the JMX connector server URL
     * @throws JMException if the JMX connector server could not be started
     */
    String getJMXConnectorURL() throws JMException;

    /**
     * Returns the address of the JMX connector server depending on the specified protocol.
     * 
     * @param protocol the JMX transport protocol
     * @return the address of the anonymous connector server
     * @throws JMException in case of boot sequence failure
     */
    String getJMXConnectorURL(final JMXTransportProtocol protocol) throws JMException;

    /**
     * Return the URL of this Instance.
     * This URL must be used to contact this Instance.
     *
     * @return the URL of this Instance.
     */
    String getHostURL();

    /**
     * Performs login.
     * 
     * @param cred encrypted username and password
     * @return the name of the user logged
     * @throws LoginException if username or password is incorrect.
     */
    Subject authenticate(Credentials cred) throws LoginException;

}
