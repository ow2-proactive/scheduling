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
package org.ow2.proactive.authentication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyException;
import java.security.PrivateKey;
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
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;


/**
 * Authentication based on user and group file.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public abstract class FileLoginModule implements Loggable, LoginModule {

    /** connection logger */
    private Logger logger = getLogger();

    public static final String ENCRYPTED_DATA_SEP = " ";

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

    protected Subject subject;

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
     * Defines private key
     *
     * @return private key in use
     */
    protected abstract PrivateKey getPrivateKey() throws KeyException;

    /**
     * 
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
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
            throw new LoginException("Error: no CallbackHandler available " +
                                     "to garner authentication information from the user");
        }
        try {
            Callback[] callbacks = new Callback[] { new NoCallback() };

            // gets the username, password, group Membership, and group Hierarchy from callback handler
            callbackHandler.handle(callbacks);
            Map<String, Object> params = ((NoCallback) callbacks[0]).get();
            String username = (String) params.get("username");
            String password = (String) params.get("pw");

            params.clear();
            ((NoCallback) callbacks[0]).clear();

            if (username == null) {
                logger.info("No username has been specified for authentication");
                throw new FailedLoginException("No username has been specified for authentication");
            }

            succeeded = logUser(username, password, true);
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
     * @param printErrorMessage if a message should be printed if the password is incorrect.
     * @return true user login and password are correct, and requested group is authorized for the user
     * @throws LoginException if authentication or group membership fails.
     */
    protected boolean logUser(String username, String password, boolean printErrorMessage) throws LoginException {

        if (!authenticateUserFromFile(username, password)) {
            String message = "[" + FileLoginModule.class.getSimpleName() + "] Incorrect Username/Password";
            if (printErrorMessage) {
                logger.info(message);
            } else {
                logger.debug(message);
            }
            throw new FailedLoginException("Incorrect Username/Password");
        }

        subject.getPrincipals().add(new UserNamePrincipal(username));
        groupMembershipFromFile(username);
        logger.debug("authentication succeeded for user '" + username + "'");
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
        PrivateKey privateKey = null;
        try {
            privateKey = getPrivateKey();
        } catch (KeyException e) {
            throw new LoginException(e.toString());
        }

        try (FileInputStream stream = new FileInputStream(loginFile)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            props.load(reader);
        } catch (FileNotFoundException e) {
            throw new LoginException(e.toString());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        }

        // verify the username and password
        if (!props.containsKey(username)) {
            return false;
        } else {
            String encryptedPassword = (String) props.get(username);
            try {
                if (!HybridEncryptionUtil.decryptBase64String(encryptedPassword, privateKey, ENCRYPTED_DATA_SEP)
                                         .equals(password)) {
                    return false;
                }
            } catch (KeyException e) {
                throw new LoginException(e.toString());
            }
            return true;
        }
    }

    /**
     * Return corresponding group for an user from the group file.
     * @param username user's login
     * @throws LoginException if group file is not found or unreadable.
     */
    protected void groupMembershipFromFile(String username) throws LoginException {

        try (FileInputStream stream = new FileInputStream(groupFile)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] u2g = line.split(":");
                if (u2g[0].trim().equals(username)) {
                    subject.getPrincipals().add(new GroupNamePrincipal(u2g[1]));
                    logger.debug("adding group principal '" + u2g[1] + "' for user '" + username + "'");
                }
            }
        } catch (FileNotFoundException e) {
            throw new LoginException(e.toString());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        }
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
