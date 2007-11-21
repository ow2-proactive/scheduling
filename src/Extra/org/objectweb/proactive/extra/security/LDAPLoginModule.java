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

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;


/**
 * <p>
 * This LDAPLoginModule authenticates users with a password.
 *
 * <p>
 * This LDAPLoginModule connects to a ldap server to check the username and
 * password. It will only work if the structure of the LDAP directory is the
 * same as the one described here
 * http://dsi.inria.fr/services_offerts/authentification/info_en_plus#2
 *
 * <p>
 * This module doesn't add any principal to the subject, it must only be used to
 * check if the login is succesful or not.
 *
 * <p>
 * This LDAPLoginModule recognizes the debug option. If set to true in the login
 * Configuration, debug messages will be output to the output stream,
 * System.out.
 */
public class LDAPLoginModule implements LoginModule {
    private CallbackHandler callbackHandler;

    // configurable option
    private boolean debug = false;

    // the authentication status
    private boolean succeeded = false;

    /**
     * Initialize this <code>LDAPLoginModule</code>.
     *
     * <p>
     *
     * @param subject
     *            the <code>Subject</code> not to be authenticated.
     *            <p>
     *
     * @param callbackHandler
     *            a <code>CallbackHandler</code> to get the credentials of the
     *            user, must work with <code>NoCallback</code> callbacks.
     *            <p>
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
        Map<String, ?> sharedState, Map<String, ?> options) {
        this.callbackHandler = callbackHandler;

        // initialize any configured options
        debug = "true".equalsIgnoreCase((String) options.get("debug"));
    }

    /**
     * Authenticate the user by getting the user name and password from the
     * CallbackHandler.
     *
     * <p>
     *
     * @return true in all cases since this <code>LDAPLoginModule</code>
     *         should not be ignored.
     *
     * @exception FailedLoginException
     *                if the authentication fails.
     *                <p>
     *
     * @exception LoginException
     *                if this <code>LDAPLoginModule</code> is unable to
     *                perform the authentication.
     */
    public boolean login() throws LoginException {
        // prompt for a user name and password
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available " +
                "to garner authentication information from the user");
        }

        Callback[] callbacks = new Callback[] { new NoCallback() };

        String username = null;
        String password = null;
        String urlLDAP = null;

        try {
            callbackHandler.handle(callbacks);

            Map<String, Object> params = ((NoCallback) callbacks[0]).get();

            // gets the username, password and url from the callback handler
            username = (String) params.get("username");
            password = (String) params.get("pw");
            urlLDAP = (String) params.get("url");

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

        // verify the username and password
        boolean usernameExists = false;
        boolean passwordMatch = false;

        // Get the UID of the user by an anonymous connection to the LDAP server
        // (null = not found)
        String userID = null;

        try {
            userID = getLDAPUserID(urlLDAP, username);
        } catch (NamingException e) {
            throw new FailedLoginException("Cannot connect to LDAP server");
        }

        if (userID != null) {
            usernameExists = true;
            // Check if the password match the username
            passwordMatch = checkLDAPPassword(urlLDAP, userID, password);
        }

        if (usernameExists && passwordMatch) {
            // authentication succeeded!!!
            if (debug) {
                System.out.println("\t\t[LDAPLoginModule] " +
                    "authentication succeeded");
            }

            succeeded = true;

            return true;
        } else {
            // authentication failed -- clean out state
            if (debug) {
                System.out.println("\t\t[LDAPLoginModule] " +
                    "authentication failed");
            }

            succeeded = false;
            username = null;

            if (!usernameExists) {
                throw new FailedLoginException("User Name Doesn't exists");
            } else {
                throw new FailedLoginException("Password Incorrect");
            }
        }
    }

    /**
     * <p>
     * This method is called if the LoginContext's overall authentication
     * succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * LoginModules succeeded).
     * <p>
     *
     * @exception LoginException
     *                if the commit fails.
     *
     * @return true if this LDAPLoginModule's own login and commit attempts
     *         succeeded, or false otherwise.
     */
    public boolean commit() throws LoginException {
        return succeeded;
    }

    /**
     * <p>
     * This method is called if the LoginContext's overall authentication
     * failed. (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * LoginModules did not succeed).
     *
     * <p>
     * If this LDAPLoginModule's own authentication attempt succeeded (checked
     * by retrieving the private state saved by the <code>login</code> and
     * <code>commit</code> methods), then this method cleans up any state that
     * was originally saved.
     *
     * <p>
     *
     * @exception LoginException
     *                if the abort fails.
     *
     * @return false if this LoginModule's own login and/or commit attempts
     *         failed, and true otherwise.
     */
    public boolean abort() throws LoginException {
        boolean result = succeeded;
        succeeded = false;

        return result;
    }

    /**
     * Logout the user.
     * <p>
     *
     * @exception LoginException
     *                if the logout fails.
     *
     * @return true in all cases since this <code>LoginModule</code> should
     *         not be ignored.
     */
    public boolean logout() throws LoginException {
        succeeded = false;

        return true;
    }

    /**
     * Connects anonymously to the LDAP server <code>url</code> and retreive
     * the UID of the user <code>username</code>
     *
     * <p>
     *
     * @return the String containing the UID of the user or null if the user is
     *         not found.
     */
    private String getLDAPUserID(String url, String username)
        throws NamingException {
        String userID = null;

        // Anonymous connection to get the uid and the group of the user
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");

        // url of the LDAP server
        env.put(Context.PROVIDER_URL, "ldap://" + url + "/");

        DirContext ctx = null;

        // Create the initial directory context
        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException ne) {
            throw ne;
        }

        // Set the attribute to match in the search
        Attributes matchAttrs = new BasicAttributes("inriaLocalLogin",
                username, true);

        // Specify the ids of the attributes to return
        String[] attrIDs = { "inriaLocalLogin", "uid" };

        String resultUsername = null;

        try {
            // Search for objects matching these attributes
            NamingEnumeration<SearchResult> answer = ctx.search("ou=People,dc=inria,dc=fr",
                    matchAttrs, attrIDs);

            if (answer.hasMoreElements()) {
                Attributes attr = answer.nextElement().getAttributes();
                resultUsername = new String(attr.get("inriaLocalLogin").get()
                                                .toString());

                if (username.equals(resultUsername)) {
                    userID = new String(attr.get("uid").get().toString());
                }
            }
        } catch (NamingException e) {
            System.err.println("Problem with the search in anonymous mode : " +
                e);
        }

        // Close the context when we're done
        try {
            ctx.close();
        } catch (NamingException e) {
            System.err.println("Problem closing anonymous connection : " + e);
        }

        return userID;
    }

    /**
     * Connect using SSL encryption to the LDAP server <code>url</code> and
     * tries to authenticate with the <code>UID</code> and
     * <code>password</code>.
     *
     * <p>
     *
     * @return true if the authentication is successful, false otherwise.
     */
    private boolean checkLDAPPassword(String url, String userID, String password) {
        String uid = "uid=" + userID + ",ou=people,dc=inria,dc=fr";

        // Secured connection to check the user password
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");

        // url of the LDAP server
        env.put(Context.PROVIDER_URL, "ldaps://" + url + "/");

        // secure connection that does not check certificates
        env.put("java.naming.ldap.factory.socket",
            DummySSLSocketFactory.class.getName());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, uid);
        env.put(Context.SECURITY_CREDENTIALS, password);

        DirContext ctx = null;

        try {
            // Create the initial directory context
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            System.err.println("Problem connecting securely to LDAP server : " +
                e);

            // Connexion failed, password is incorrect
            return false;
        }

        // Close the context when we're done
        try {
            ctx.close();
        } catch (NamingException e) {
            System.err.println("Problem closing secure connection : " + e);
        }

        return true;
    }
}
