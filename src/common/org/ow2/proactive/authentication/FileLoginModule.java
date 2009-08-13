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

import org.apache.log4j.Logger;


/**
 * Authentication based on user and group file.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public abstract class FileLoginModule implements Loggable, LoginModule {

    /** connection logger */
    private Logger logger = getLogger();

    /**
     *  JAAS call back handler used to get authentication request parameters 
     */
    protected CallbackHandler callbackHandler;

    /** authentication status */
    private boolean succeeded = false;

    /** The file where to store the allowed user//password */
    protected String loginFile = getLoginFileName();

    /** The file where to store group management */
    protected String groupFile = getGroupFileName();

    /**
     * Defines login file name
     * 
     * @return the login file name
     */
    protected abstract String getLoginFileName();

    /**
     * Defines group file name
     * 
     * @return the group file name
     */
    protected abstract String getGroupFileName();

    /**
     * 
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        checkLoginFile();
        checkGroupFile();
        if (logger.isDebugEnabled()) {
            logger.debug("Using Login file at : " + this.loginFile);
            logger.debug("Using Group file at : " + this.groupFile);
        }
        this.callbackHandler = callbackHandler;
    }

    protected void checkLoginFile() {
        //test login file existence
        if (!(new File(this.loginFile).exists())) {
            throw new RuntimeException("The file " + this.loginFile + " has not been found \n" +
                "Unable to perform user authentication by file method");
        }
    }

    protected void checkGroupFile() {
        //test group file existence
        if (!(new File(this.groupFile).exists())) {
            throw new RuntimeException("The file " + this.groupFile + " has not been found \n" +
                "Unable to perform user authentication by file method");
        }
    }

    /**
     * 
     * @see javax.security.auth.spi.LoginModule#login()
     * @throws LoginException if userName of password are not correct
     */
    public boolean login() throws LoginException {
        succeeded = false;
        // prompt for a user name and password
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available "
                + "to garner authentication information from the user");
        }
        try {
            Callback[] callbacks = new Callback[] { new NoCallback() };

            // gets the username, password, group Membership, and group Hierarchy from callback handler
            callbackHandler.handle(callbacks);
            Map<String, Object> params = ((NoCallback) callbacks[0]).get();
            String username = (String) params.get("username");
            String password = (String) params.get("pw");
            String reqGroup = (String) params.get("group");
            String[] hierarchyArray = (String[]) params.get("groupsHierarchy");

            params.clear();
            ((NoCallback) callbacks[0]).clear();

            if (username == null) {
                logger.info("No username has been specified for authentication");
                throw new FailedLoginException("No username has been specified for authentication");
            }

            if (hierarchyArray == null) {
                logger.info("No group hierarchy has been specified for authentication");
                throw new FailedLoginException("No group hierarchy has been specified for authentication");
            }

            if (hierarchyArray.length == 0) {
                logger.info("No group hierarchy has been specified for authentication");
                throw new FailedLoginException("No group hierarchy has been specified for authentication");
            }

            if (reqGroup == null) {
                logger.info("No group has been specified for authentication");
                throw new FailedLoginException("No group has been specified for authentication");
            }

            GroupHierarchy groupsHierarchy = new GroupHierarchy(hierarchyArray);

            if (logger.isDebugEnabled()) {
                logger.debug("File authentication requested for user : " + username);
                String hierarchyRepresentation = "";
                for (String s : hierarchyArray) {
                    hierarchyRepresentation += (s + " ");
                }
                logger.debug("requested group : " + reqGroup + ", group hierarchy : " +
                    hierarchyRepresentation);
            }

            succeeded = logUser(username, password, reqGroup, groupsHierarchy);
            return succeeded;

        } catch (java.io.IOException ioe) {
            logger.error("", ioe);
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            logger.error("", uce);
            throw new LoginException("Error: " + uce.getCallback().toString() +
                " not available to garner authentication information from the user");
        }
    }

    /**
     * First Check user and password from login file. If user is authenticated,
     * check group membership from group file.
     * @param username user's login
     * @param password user's password
     * @param reqGroup requested level
     * @param groupsHierarchy Group hierarchy used for authentication.
     * @return true user login and password are correct, and requested group is authorized for the user
     * @throws LoginException if authentication or group membership fails.
     */
    protected boolean logUser(String username, String password, String reqGroup,
            GroupHierarchy groupsHierarchy) throws LoginException {

        if (!authenticateUserFromFile(username, password)) {
            logger.info("Incorrect Username/Password");
            throw new FailedLoginException("Incorrect Username/Password");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("authentication succeeded, checking group");
        }

        String group = checkGroupMemberShip(username, reqGroup, groupsHierarchy);
        logger.info("authentication succeeded for user '" + username + "' in group '" + group + "'");
        return true;
    }

    /**
     * Check user and password from login file.
     * @param username user's login
     * @param password user's password
     * @return true if user is found in login file and its password is correct, falser otherwise
     * @throws LoginException if login file is not found or unreadable.
     */
    private boolean authenticateUserFromFile(String username, String password) throws LoginException {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File(loginFile)));
        } catch (FileNotFoundException e) {
            throw new LoginException(e.toString());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        }

        // verify the username and password
        if (!props.containsKey(username) || !props.get(username).equals(password)) {
            return false;
        } else
            return true;
    }

    /**
     * Check group membership from group file. If a group is found for the user,
     * and this group is above or equals to the requested group for logging,
     * group membership succeed, otherwise it fails.
     * @param username user's login
     * @param reqGroup user's requested group
     * @param groupsHierarchy group hierarchy used for authentication.
     * @return a String representing user's group.
     * @throws LoginException if user's group is not found in group file, or user's group is below than requested group,
     * or group file is not found or unreadable.
     */
    protected String checkGroupMemberShip(String username, String reqGroup, GroupHierarchy groupsHierarchy)
            throws LoginException {
        String group = getGroupMembershipFromFile(username);
        if (group == null) {
            logger.info("User doesn't belong to a group");
            throw new FailedLoginException("User doesn't belong to a group");
        }

        if (groupsHierarchy == null) {
            logger.info("Groups hierarchy not found");
            throw new FailedLoginException("Groups hierarchy not found");
        }

        try {
            if (!groupsHierarchy.isAbove(group, reqGroup)) {
                logger.info("User group not matching");
                throw new FailedLoginException("User group not matching");
            }
        } catch (GroupException e) {
            logger.debug("", e);
            logger.info(e.getMessage());
            throw new FailedLoginException(e.getMessage());
        }
        return group;
    }

    /**
     * Return corresponding group for an user from the group file.
     * @param username user's login
     * @return a String representing user's group, or null if no group is found for the user.
     * @throws LoginException if group file is not found or unreadable.
     */
    private String getGroupMembershipFromFile(String username) throws LoginException {
        Properties groups = new Properties();
        try {
            groups.load(new FileInputStream(new File(groupFile)));
        } catch (FileNotFoundException e) {
            throw new LoginException(e.toString());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        }
        return (String) groups.get(username);
    }

    /**
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    public boolean commit() throws LoginException {
        return succeeded;
    }

    /**
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    public boolean abort() throws LoginException {
        boolean result = succeeded;
        succeeded = false;
        return result;
    }

    /**
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    public boolean logout() throws LoginException {
        succeeded = false;
        return true;
    }
}