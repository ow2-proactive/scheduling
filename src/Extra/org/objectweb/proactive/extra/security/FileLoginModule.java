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
package org.objectweb.proactive.extra.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;


public class FileLoginModule implements LoginModule {
    private CallbackHandler callbackHandler;

    // configurable option
    private boolean debug = false;

    // the authentication status
    private boolean succeeded = false;

    public void initialize(Subject subject, CallbackHandler callbackHandler,
        Map<String, ?> sharedState, Map<String, ?> options) {
        this.callbackHandler = callbackHandler;

        // initialize any configured options
        debug = "true".equalsIgnoreCase((String) options.get("debug"));
    }

    public boolean login() throws LoginException {
        // prompt for a user name and password
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available " +
                "to garner authentication information from the user");
        }

        Callback[] callbacks = new Callback[] { new NoCallback() };

        String username = null;
        String password = null;
        String filePath = null;
        String reqGroup = null;
        String groupsFilePath = null;
        GroupHierarchy groupsHierarchy = null;

        try {
            callbackHandler.handle(callbacks);

            Map<String, Object> params = ((NoCallback) callbacks[0]).get();

            // gets the username, password and url from the callback handler
            username = (String) params.get("username");
            password = (String) params.get("pw");
            filePath = (String) params.get("path");
            reqGroup = (String) params.get("group");
            groupsFilePath = (String) params.get("groupsFilePath");
            groupsHierarchy = new GroupHierarchy((String[]) params.get(
                        "groupsHierarchy"));

            params.clear();
            ((NoCallback) callbacks[0]).clear();
        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                " not available to garner authentication information " +
                "from the user");
        }

        // print debugging information
        if (debug) {
            System.out.println("\t\t[LDAPLoginModule] " +
                "user entered user name: " + username);
            System.out.println("\t\t[LDAPLoginModule] " +
                "user entered password: " + password);
        }

        Properties props = new Properties();

        try {
            props.load(new FileInputStream(new File(filePath)));
        } catch (FileNotFoundException e) {
            throw new LoginException(e.toString());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        }

        // verify the username and password
        if (!props.containsKey(username) ||
                !props.get(username).equals(password)) {
            throw new FailedLoginException("Incorrect Username/Password");
        }

        if (reqGroup != null) {
            Properties groups = new Properties();

            try {
                groups.load(new FileInputStream(new File(groupsFilePath)));
            } catch (FileNotFoundException e) {
                throw new LoginException(e.toString());
            } catch (IOException e) {
                throw new LoginException(e.toString());
            }

            String group = (String) groups.get(username);

            if (group == null) {
                throw new FailedLoginException("User doesn't belong to a group");
            }

            if (groupsHierarchy == null) {
                throw new FailedLoginException("Groups hierarchy not found");
            }

            if (!groupsHierarchy.isAbove(group, reqGroup)) {
                throw new FailedLoginException("User group not matching");
            }
        }

        if (debug) {
            System.out.println("\t\t[FileLoginModule] " +
                "authentication succeeded");
        }

        succeeded = true;

        return true;
    }

    public boolean commit() throws LoginException {
        return succeeded;
    }

    public boolean abort() throws LoginException {
        boolean result = succeeded;
        succeeded = false;

        return result;
    }

    public boolean logout() throws LoginException {
        succeeded = false;

        return true;
    }

    private class GroupHierarchy {
        private String[] hierarchy;

        public GroupHierarchy(String[] hierarchy) {
            this.hierarchy = hierarchy;
        }

        public boolean isAbove(String trueGroup, String reqGroup)
            throws FailedLoginException {
            int trueGroupLevel = groupLevel(trueGroup);

            if (trueGroupLevel == -1) {
                throw new FailedLoginException(
                    "User group is not in groups hierarchy");
            }

            int reqGroupLevel = groupLevel(reqGroup);

            if (reqGroupLevel == -1) {
                throw new FailedLoginException(
                    "Required group is not in groups hierarchy");
            }

            return trueGroupLevel >= reqGroupLevel;
        }

        private int groupLevel(String group) {
            for (int i = hierarchy.length - 1; i > -1; i--) {
                if (hierarchy[i].equals(group)) {
                    return i;
                }
            }

            return -1;
        }
    }
}
