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
package org.objectweb.proactive.extensions.security.loginmodule;

import java.util.Map;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;


/**
 *
 * @author nhouillo
 *
 */
public class Login {

    /**
     * Standard interface to check credentials. The system property
     * "java.security.auth.login.config" must be set to designate a
     * configuration file, see
     * http://java.sun.com/javase/6/docs/technotes/guides/security/jaas/JAASRefGuide.html#AppendixB
     * for more information.
     *
     * @param params
     *            The parameters to be given to the login modules found in the
     *            entry <code>loginMethod</code> of the configuratino file.
     * @param loginMethod
     *            The entry of the configuration file containig the list of
     *            login modules to use to authenticate a user.
     * @return always true.
     * @throws LoginException
     *             If the authentication fails (incorrect credentials) or if
     *             there is an error during the attempt to verify them.
     */
    public static Boolean login(Map<String, Object> params, String loginMethod) throws LoginException {
        LoginContext lc = new LoginContext(loginMethod, new NoCallbackHandler(params));

        lc.login();

        return true;
    }
}
