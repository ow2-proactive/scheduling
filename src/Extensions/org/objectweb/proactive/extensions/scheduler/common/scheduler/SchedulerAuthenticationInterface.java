/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.scheduler.common.scheduler;

import java.io.Serializable;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;


/**
 * Scheduler Authentication Interface provides method to connect to the scheduler.<br>
 * Before using the scheduler communication interface {@link UserSchedulerInterface}, you have to connect it using a login/password using this interface.<br>
 * You can get this interface by using the scheduler connection {@link SchedulerConnection}
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jul 24, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public interface SchedulerAuthenticationInterface extends Serializable {

    /**
     * Connect the user interface to a scheduler with the given scheduler URL.<br>
     * If the login or/and password do not match an allowed one,
     * it will throw an LoginException.<br>
     * If the authentication succeed, it will return a new scheduler user API to managed job.
     *
     * @param user the user name of the user to connect.
     * @param password the password of the user to connect.
     * @return The {@link userScheduler} interface if this user can access to the scheduler.
     * @throws LoginException thrown if this user/password does not match any entries.
     * @throws SchedulerException thrown if the connection to the scheduler cannot be established.
     */
    public UserSchedulerInterface logAsUser(String user, String password) throws LoginException,
            SchedulerException;

    /**
     * Connect the administrator interface to a scheduler with the given scheduler URL.<br>
     * If the login or/and password do not match an allowed one,
     * it will throw an LoginException.<br>
     * If the authentication succeed, it will return a new scheduler administrator API.<br>
     * This authentication requires that the user has administrator rights.
     *
     * @param user the user name of the user to connect.
     * @param password the password of the user to connect.
     * @return The {@link userScheduler} interface if this user can access to the scheduler.
     * @throws LoginException thrown if this user/password does not match any entries.
     * @throws SchedulerException thrown if the connection to the scheduler cannot be established.
     */
    public AdminSchedulerInterface logAsAdmin(String user, String password) throws LoginException,
            SchedulerException;
}
