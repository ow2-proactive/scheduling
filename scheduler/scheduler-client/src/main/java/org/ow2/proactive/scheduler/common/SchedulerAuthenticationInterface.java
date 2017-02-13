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
package org.ow2.proactive.scheduler.common;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.authentication.Authentication;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;


/**
 * Scheduler Authentication Interface provides method to connect to the scheduler.
 * <p>
 * Before using the scheduler communication interface, you have to connect it using a login/password using this interface.
 * You can get this interface by using the scheduler connection {@link SchedulerConnection}.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface SchedulerAuthenticationInterface extends Authentication {

    /**
     * Try to login a client to the scheduler sending login and password in an encrypted state.<br>
     * If the login or/and password do not match an allowed one, it will throw an LoginException.<br>
     * If the authentication succeed, it will return a new scheduler API.<br>
     * This authentication just requires the client to be known by the System.<br>
     * Note that you can use the provided
     * {@link SchedulerAuthenticationGUIHelper} class to display a graphical
     * interface that will ask the URL, login and password.
     *
     * @param cred Object encapsulating encrypted credentials, and information on how to decrypt them
     * @return The {@link Scheduler} interface if this client is allowed to access the scheduler.
     * @throws LoginException thrown if this user/password does not match any entries.
     * @throws AlreadyConnectedException thrown if this client is already connected to the Scheduler.
     */
    Scheduler login(Credentials cred) throws LoginException, AlreadyConnectedException;
}
