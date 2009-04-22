/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.authentication;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.objectweb.proactive.api.PAActiveObject;


/**
 * An active object responsible for authentication.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public abstract class AuthenticationImpl implements Authentication {

    /** Activation is used to control authentication during scheduling initialization */
    private boolean activated = false;

    /**
     * Defines login method
     * 
     * @return a string which represents the login method.
     */
    protected abstract String getLoginMethod();

    /**
     * Default constructor which loads jaas.config and stores it in global system property
     */
    public AuthenticationImpl() {
        URL jaasConfig = AuthenticationImpl.class.getResource("jaas.config");

        if (jaasConfig == null) {
            throw new RuntimeException("The file 'jaas.config' has not been found");
        }
        try {
            // is used when jaas.config is in classes directory
            String jaasConfigPath = jaasConfig.toURI().getPath();
            if (jaasConfigPath == null) {
                // is used when jaas.config is inside jar file
                jaasConfigPath = jaasConfig.toString();
            }
            System.setProperty("java.security.auth.login.config", jaasConfigPath);
        } catch (URISyntaxException e) {
            getLogger().error(e.getMessage(), e);
        }
    }

    /**
     * Performs login
     * 
     * @param role the role of the user to connect, can be admin or user
     * @param groups the group in which the user is
     * @param username the user name of the user
     * @param password the password of the user
     * @throws LoginException if username or password is incorrect.
     */
    protected void loginAs(String role, String[] groups, String username, String password)
            throws LoginException {

        if (activated == false) {
            throw new LoginException("Authentication active object is not activated.");
        }

        if (username == null | username.equals("")) {
            throw new LoginException("Bad user name (user is null or empty)");
        }

        try {
            // Verify that this user//password can connect to this existing scheduler
            getLogger().info(username + " is trying to connect as " + role);

            Map<String, Object> params = new HashMap<String, Object>(4);
            //user name to check
            params.put("username", username);
            //password to check
            params.put("pw", password);
            //minimal group membership : user must belong to group user or a group above
            params.put("group", role);
            //group hierarchy defined for this authentication/permission ( from lowest,
            params.put("groupsHierarchy", groups);

            //Load LoginContext according to login method defined in jaas.config
            LoginContext lc = new LoginContext(getLoginMethod(), new NoCallbackHandler(params));

            lc.login();
            getLogger().info("Logged successfull as a " + role + " : " + username);

        } catch (LoginException e) {
            getLogger().info(e.getMessage());
            //Nature of exception is hidden for user, we don't want to inform
            //user about the reason of non authentication
            throw new LoginException("Authentication failed");
        }
    }

    /**
     * @see org.ow2.proactive.authentication.Authentication#isActivated()
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Activates or desactivates authentication active object
     * 
     * @param activated the status of the desired activated state.
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Terminates the active object
     * 
     * @return true if the object has been terminated.
     */
    public boolean terminate() {
        PAActiveObject.terminateActiveObject(false);
        getLogger().info("Authentication service is now shutdown!");
        return true;
    }
}
