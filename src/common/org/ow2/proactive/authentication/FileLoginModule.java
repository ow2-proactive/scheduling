/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 *
 */
public abstract class FileLoginModule implements Loggable, LoginModule {

    /** connection logger */
    private Logger logger = getLogger();

    /**
     *  JAAS call back handler used to get authentication request parameters 
     */
    private CallbackHandler callbackHandler;

    /** authentication status */
    private boolean succeeded = false;

    /** The file where to store the allowed user//password */
    private String loginFile = getLoginFileName();

    /** The file where to store group management */
    private String groupFile = getGroupFileName();

    /**
     * Defines login file name
     */
    protected abstract String getLoginFileName();

    /**
     * Defines group file name
     */
    protected abstract String getGroupFileName();
    
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {

        //test login file existence
        if (!(new File(this.loginFile).exists())) {
            throw new RuntimeException("The file " + this.loginFile + " has not been found \n" +
                "Scheduler is unable to perform user authentication by file method");
        }

        //test group file existence
        if (!(new File(this.groupFile).exists())) {
            throw new RuntimeException("The file " + this.groupFile + " has not been found \n" +
                "Scheduler is unable to perform user authentication by file method");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Using Login file at : " + this.loginFile);
            logger.debug("Using Group file at : " + this.groupFile);
        }
        this.callbackHandler = callbackHandler;
    }

	/**
     * @see javax.security.auth.spi.LoginModule#login()
     */
    public boolean login() throws LoginException {
        // prompt for a user name and password
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available "
                + "to garner authentication information from the user");
        }

        Callback[] callbacks = new Callback[] { new NoCallback() };

        String username = null;
        String password = null;
        String reqGroup = null;
        GroupHierarchy groupsHierarchy = null;
        String[] hierarchyArray = null;

        try {

            // gets the username, password, group Membership, and group Hierarchy from callback handler
            callbackHandler.handle(callbacks);
            Map<String, Object> params = ((NoCallback) callbacks[0]).get();
            username = (String) params.get("username");
            password = (String) params.get("pw");
            reqGroup = (String) params.get("group");
            hierarchyArray = (String[]) params.get("groupsHierarchy");
            groupsHierarchy = new GroupHierarchy(hierarchyArray);

            params.clear();
            ((NoCallback) callbacks[0]).clear();
        } catch (java.io.IOException ioe) {
            logger.error("",ioe);
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
		logger.error("",uce);
            throw new LoginException("Error: " + uce.getCallback().toString() +
                " not available to garner authentication information from the user");

        }

        if (logger.isDebugEnabled()) {
            logger.debug("File authentication requested for user : " + username);
            String hierarchyRepresentation = "";
            for (String s : hierarchyArray) {
                hierarchyRepresentation += (s + " ");
            }
            logger.debug("requested group : " + reqGroup + ", group hierarchy : " + hierarchyRepresentation);
        }

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
            succeeded = false;
            logger.info("Incorrect Username/Password");
            throw new FailedLoginException("Incorrect Username/Password");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("authentication succeeded, checking group");
        }

        if (reqGroup == null) {
            succeeded = false;
            logger.info("No group has been specified for authentication");
            throw new FailedLoginException("No group has been specified for authentication");
        }

        Properties groups = new Properties();

        try {
            groups.load(new FileInputStream(new File(groupFile)));
        } catch (FileNotFoundException e) {
            throw new LoginException(e.toString());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        }

        String group = (String) groups.get(username);

        if (group == null) {
            succeeded = false;
            logger.info("User doesn't belong to a group");
            throw new FailedLoginException("User doesn't belong to a group");
        }

        if (groupsHierarchy == null) {
            succeeded = false;
            logger.info("Groups hierarchy not found");
            throw new FailedLoginException("Groups hierarchy not found");
        }

        try {
            if (!groupsHierarchy.isAbove(group, reqGroup)) {
                succeeded = false;
                logger.info("User group not matching");
                throw new FailedLoginException("User group not matching");
            }
        } catch (GroupException e) {
		logger.error("",e);
            throw new FailedLoginException("Groups hierarchy not found");
        }

        logger.info("authentication succeeded for user '"+username+"' in group '"+group+"'");
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
}