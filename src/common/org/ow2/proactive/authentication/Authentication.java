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
package org.ow2.proactive.authentication;

import java.io.Serializable;
import java.security.PublicKey;

import javax.management.JMException;
import javax.security.auth.login.LoginException;


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
    public boolean isActivated();

    /**
     * Request this Authentication's public key for credentials encryption
     *
     * @return this Authentication's public key
     * @throws LoginException the key could not be retrieved
     */
    public PublicKey getPublicKey() throws LoginException;

    /**
     * Return the JMX connector server URL used to contact this instance.
     *
     * @return the string representation of the JMX connector server URL
     * @throws JMException if the JMX connector server could not be started
     */
    public String getJMXConnectorURL() throws JMException;

    /**
     * Return the URL of this Resource Manager.
     * This URL must be used to contact this Resource Manager.
     *
     * @return the URL of this Resource Manager.
     */
    public String getHostURL();

}
