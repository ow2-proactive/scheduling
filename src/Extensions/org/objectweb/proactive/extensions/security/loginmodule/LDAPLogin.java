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

import javax.security.auth.login.LoginException;


/**
 *
 * @author nhouillo
 *
 */
public abstract class LDAPLogin {

    /**
     * LDAP implementation of the Login standard interface. Uses the
     * <code>LDAPLoginMethod</code> from the configuration file designated by
     * the "java.security.auth.login.config" system property, which must be set
     * to use the <code>LDAPLoginModule</code> in this package. It will only
     * work if the structure of the LDAP directory is the same as the one
     * described here
     * http://dsi.inria.fr/services_offerts/authentification/info_en_plus#2
     *
     * @param params
     *            It must contain 3 <code>String</code>s : "username"
     *            corresponding to the "inriaLocalLogin" in the directory, "pw"
     *            being the password, and "url" the url of the LDAP directory.
     * @return always true
     * @throws LoginException
     *             If the authentication fails (incorrect credentials) or if
     *             there is an error during the attempt to verify them.
     */
    public static Boolean login(Map<String, Object> params) throws LoginException {
        return Login.login(params, "LDAPLoginMethod");
    }
}
