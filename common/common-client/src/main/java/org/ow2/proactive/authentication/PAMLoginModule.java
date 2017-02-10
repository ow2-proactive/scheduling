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

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;

import net.sf.jpam.Pam;
import net.sf.jpam.PamReturnValue;


/**
 * Authentication based on user and group file.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public abstract class PAMLoginModule extends FileLoginModule implements Loggable {

    /**
     * connection logger
     */
    private final Logger logger = getLogger();

    /**
     * PAM module name to be installed in the pam configuration
     **/
    public static final String PAM_MODULE_NAME = "proactive-jpam";

    /**
     * authentication status
     */
    private boolean succeeded = false;

    private final Pam pam;

    public PAMLoginModule() {
        pam = new Pam(PAM_MODULE_NAME);
    }

    /**
     * @see LoginModule#initialize(Subject, CallbackHandler, Map, Map)
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;

    }

    /**
     * Authenticate the user by getting the user name and password from the
     * CallbackHandler.
     * <p>
     * <p>
     *
     * @return true in all cases since this <code>PAMLoginModule</code>
     * should not be ignored.
     * @throws FailedLoginException if the authentication fails.
     *                              <p>
     * @throws LoginException       if this <code>LDAPLoginModule</code> is unable to
     *                              perform the authentication.
     */
    @Override
    public boolean login() throws LoginException {
        succeeded = false;
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available " +
                                     "to garner authentication information from the user");
        }

        try {

            Callback[] callbacks = new Callback[] { new NoCallback() };

            // gets the user name, password, group Membership, and group Hierarchy from call back handler
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

            succeeded = logUser(username, password);
            return succeeded;

        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                                     " not available to garner authentication information " + "from the user");
        }
    }

    /**
     * Check user and password from file, or authenticate with PAM.
     *
     * @param username user's login
     * @param password user's password
     * @return true user login and password are correct, and requested group is authorized for the user
     * @throws LoginException if authentication and group membership fails.
     */
    protected boolean logUser(String username, String password) throws LoginException {
        try {
            return super.logUser(username, password, false);
        } catch (LoginException ex) {
            return pamLogUser(username, password);
        }
    }

    private boolean pamLogUser(String username, String password) throws LoginException {
        logger.debug("Authenticating user " + username + " with PAM.");
        PamReturnValue answer = pam.authenticate(username, password);
        if (answer.equals(PamReturnValue.PAM_SUCCESS)) {
            subject.getPrincipals().add(new UserNamePrincipal(username));
            super.groupMembershipFromFile(username);
            return true;
        } else {
            logger.info("PAM authentication failed for user " + username + ": " + answer);
            throw new FailedLoginException(answer.toString());
        }
    }

    /**
     * <p>
     * This method is called if the LoginContext's overall authentication
     * succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * LoginModules succeeded).
     * <p>
     *
     * @return true if this LDAPLoginModule's own login and commit attempts
     * succeeded, or false otherwise.
     * @throws LoginException if the commit fails.
     */
    @Override
    public boolean commit() throws LoginException {
        return succeeded;
    }

    /**
     * <p>
     * This method is called if the LoginContext's overall authentication
     * failed. (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * LoginModules did not succeed).
     * <p>
     * <p>
     * If this LDAPLoginModule's own authentication attempt succeeded (checked
     * by retrieving the private state saved by the <code>login</code> and
     * <code>commit</code> methods), then this method cleans up any state that
     * was originally saved.
     * <p>
     * <p>
     *
     * @return false if this LoginModule's own login and/or commit attempts
     * failed, and true otherwise.
     * @throws LoginException if the abort fails.
     */
    @Override
    public boolean abort() throws LoginException {
        boolean result = succeeded;
        succeeded = false;
        return result;
    }

    /**
     * Logout the user.
     * <p>
     *
     * @return true in all cases since this <code>LoginModule</code> should
     * not be ignored.
     * @throws LoginException if the logout fails.
     */
    @Override
    public boolean logout() throws LoginException {
        succeeded = false;
        return true;
    }
}
