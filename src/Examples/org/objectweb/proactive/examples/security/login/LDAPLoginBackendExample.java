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
package org.objectweb.proactive.examples.security.login;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.extensions.security.loginmodule.Login;


/**
 * <p>
 * This Sample application attempts to authenticate a user.
 *
 * <p>
 * If the user successfully authenticates itself, the message "Login successful"
 * is displayed, "login failed" otherwise.
 */
public class LDAPLoginBackendExample {

    /**
     * Attempt to authenticate the user.
     *
     * <p>
     *
     * @param args
     *            input arguments for this application. These are ignored.
     */
    public static void main(String[] args) {
        // the "java.security.auth.login.config" system property must be set to
        // designate a configuration file (see jaas-cfg/jaas.config for an
        // example) with the jvm parameter
        // -Djava.security.auth.login.config==jaas.config or like this
        // :
        System.setProperty("java.security.auth.login.config",
            Login.class.getResource("jaas.config").getFile());

        Map<String, Object> params = new HashMap<String, Object>(3);

        params.put("username", args[0]);
        params.put("pw", args[1]);

        params.put("url", "ildap-sop.inria.fr");

        try {
            Login.login(params, "LDAPLoginMethod");
            System.out.println("Login successful");
        } catch (LoginException e) {
            e.printStackTrace();
            System.out.println("Login failed");
            System.exit(-1);
        }
    }
}
