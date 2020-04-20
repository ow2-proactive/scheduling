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

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.core.properties.PASharedProperties;

import com.google.common.base.Strings;


/**
 * Authentication based on LDAP system.
 *
 * Improved version of @see{LDAPLoginModule}
 *
 * support custom filters for username and group
 *
 *
 * @author The ActiveEon Team
 * @since ProActive Scheduling 2.1.1
 */
public abstract class LDAPLoginModule extends FileLoginModule implements Loggable {

    /** connection logger */
    private final Logger logger = getLogger();

    /** LDAP configuration properties */
    private LDAPProperties ldapProperties = new LDAPProperties(getLDAPConfigFileName());

    /** default value for Context.SECURITY_AUTHENTICATION
     * that correspond to anonymous connection
    */
    private final String ANONYMOUS_LDAP_CONNECTION = "none";

    /** name of key store path java property */
    private final String SSL_KEYSTORE_PATH_PROPERTY = "javax.net.ssl.keyStore";

    /** name of key store password java property */
    private final String SSL_KEYSTORE_PASSWD_PROPERTY = "javax.net.ssl.keyStorePassword";

    /** name of trust store password java property */
    private final String SSL_TRUSTSTORE_PATH_PROPERTY = "javax.net.ssl.trustStore";

    /** name of trust store password java property */
    private final String SSL_TRUSTSTORE_PASSWD_PROPERTY = "javax.net.ssl.trustStorePassword";

    /** boolean defining whether connection polling has to be used */
    private final String LDAP_CONNECTION_POOLING = ldapProperties.getProperty(LDAPProperties.LDAP_CONNECTION_POOLING);

    /** LDAP used to perform authentication */
    private final String LDAP_URL = ldapProperties.getProperty(LDAPProperties.LDAP_URL);

    /** LDAP Subtree wherein users entries are searched */
    private final String USERS_DN = ldapProperties.getProperty(LDAPProperties.LDAP_USERS_SUBTREE);

    /**
     * LDAP Subtree wherein groups entries are searched
     * If empty, then USERS_DN is used instead
     */
    private String GROUPS_DN = ldapProperties.getProperty(LDAPProperties.LDAP_GROUPS_SUBTREE);

    /**
     * Authentication method used to bind to LDAP: none, simple,
     * or one of the SASL authentication methods
     */
    private final String AUTHENTICATION_METHOD = ldapProperties.getProperty(LDAPProperties.LDAP_AUTHENTICATION_METHOD);

    private final boolean START_TLS = Boolean.parseBoolean(ldapProperties.getProperty(LDAPProperties.LDAP_START_TLS,
                                                                                      "false"));

    private final boolean ANY_CERTIFICATE = Boolean.parseBoolean(ldapProperties.getProperty(LDAPProperties.LDAP_START_TLS_ANY_CERTIFICATE,
                                                                                            "false"));

    private final boolean ANY_HOSTNAME = Boolean.parseBoolean(ldapProperties.getProperty(LDAPProperties.LDAP_START_TLS_ANY_HOSTNAME,
                                                                                         "false"));

    /** user name used to bind to LDAP (if authentication method is different from none) */
    private final String BIND_LOGIN = ldapProperties.getProperty(LDAPProperties.LDAP_BIND_LOGIN);

    /** user password used to bind to LDAP (if authentication method is different from none) */
    private String BIND_PASSWD = ldapProperties.getProperty(LDAPProperties.LDAP_BIND_PASSWD);

    /**fall back property, check user/password and group in files if user in not found in LDAP */
    private boolean fallbackUserAuth = Boolean.valueOf(ldapProperties.getProperty(LDAPProperties.FALLBACK_USER_AUTH));

    /**group fall back property, check user group membership group file if user in not found in corresponding LDAP group*/
    private boolean fallbackGroupMembership = Boolean.valueOf(ldapProperties.getProperty(LDAPProperties.FALLBACK_GROUP_MEMBERSHIP));

    /** authentication status */
    private boolean succeeded = false;

    /**
     * Creates a new instance of LDAPLoginModule
     */
    public LDAPLoginModule() {
        if (GROUPS_DN == null) {
            GROUPS_DN = USERS_DN;
        }

        if (fallbackUserAuth) {
            checkLoginFile();
            checkGroupFile();
            logger.debug("Using Login file for fall back authentication at: " + loginFile);
            logger.debug("Using Group file for fall back group membership at: " + groupFile);
        } else if (fallbackGroupMembership) {
            checkGroupFile();
            logger.debug("Using Group file for fall back group membership at: " + groupFile);
        }

        //initialize system properties for SSL/TLS connection
        String keyStore = ldapProperties.getProperty(LDAPProperties.LDAP_KEYSTORE_PATH);
        if ((!Strings.isNullOrEmpty(keyStore)) && (!alreadyDefined(SSL_KEYSTORE_PATH_PROPERTY, keyStore))) {
            keyStore = PASharedProperties.getAbsolutePath(keyStore);
            System.setProperty(SSL_KEYSTORE_PATH_PROPERTY, keyStore);
            System.setProperty(SSL_KEYSTORE_PASSWD_PROPERTY,
                               ldapProperties.getProperty(LDAPProperties.LDAP_KEYSTORE_PASSWD));
        }

        String trustStore = ldapProperties.getProperty(LDAPProperties.LDAP_TRUSTSTORE_PATH);
        if ((!Strings.isNullOrEmpty(trustStore)) && (!alreadyDefined(SSL_TRUSTSTORE_PATH_PROPERTY, trustStore))) {
            trustStore = PASharedProperties.getAbsolutePath(trustStore);
            System.setProperty(SSL_TRUSTSTORE_PATH_PROPERTY, trustStore);
            System.setProperty(SSL_TRUSTSTORE_PASSWD_PROPERTY,
                               ldapProperties.getProperty(LDAPProperties.LDAP_TRUSTSTORE_PASSWD));
        }
    }

    /**
     * Checks if property is already defined.
     *
     * @param propertyName name of the property
     * @param propertyValue value of the property
     * @return true id the property is defined and its value equals the specified value.
     */
    private boolean alreadyDefined(String propertyName, String propertyValue) {

        if (propertyName != null && propertyName.length() != 0) {
            String definedPropertyValue = System.getProperty(propertyName);

            if (System.getProperty(propertyName) != null && !definedPropertyValue.equals(propertyValue)) {
                logger.debug("Property " + propertyName + " is already defined");
                logger.debug("Using old value " + propertyValue);
                return true;
            }
        }

        return false;
    }

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
     * @param sharedState state shared with other configured LoginModules. <p>
     *
     * @param options options specified in the login
     *			<code>Configuration</code> for this particular
     *			<code>LDAPLoginModule</code>.
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;

        if (logger.isDebugEnabled()) {
            logger.debug("Using LDAP: " + LDAP_URL);
        }
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
     * Check user and password from file, or authenticate with ldap.
     *
     * @param username user's login
     * @param password user's password
     * @return true user login and password are correct, and requested group is authorized for the user
     * @throws LoginException if authentication and group membership fails.
     */
    protected boolean logUser(String username, String password) throws LoginException {
        if (fallbackUserAuth) {
            try {
                return super.logUser(username, password, false);
            } catch (LoginException ex) {
                return internalLogUser(username, password);
            }
        } else {
            return internalLogUser(username, password);
        }
    }

    private boolean internalLogUser(String username, String password) throws LoginException {

        removeOldFailedAttempts(username);
        if (tooManyFailedAttempts(username)) {
            String message = "Too many failed login/attempts, please try again in " + retryInHowManyMinutes(username) +
                             " minutes.";
            logger.warn(message);
            throw new FailedLoginException(message);
        }
        // check the user name, get the RDN of the user
        // (null = not found)
        String userDN = null;
        boolean passwordMatch = false;
        try {
            userDN = getLDAPUserDN(username);
        } catch (NamingException e) {
            logger.error("Cannot connect to LDAP server", e);
            storeFailedAttempt(username);
            throw new FailedLoginException("Cannot connect to LDAP server");
        }

        if (userDN == null) {
            logger.info("user entry not found in subtree " + USERS_DN + " for user " + username);
            storeFailedAttempt(username);
            throw new FailedLoginException("User name doesn't exists");
        } else {
            // Check if the password match the user name
            passwordMatch = checkLDAPPassword(userDN, password);
        }

        if (passwordMatch) {
            if (logger.isDebugEnabled()) {
                logger.debug("authentication succeeded, checking group");
            }

            if (fallbackGroupMembership) {
                super.groupMembershipFromFile(username);
            }
        } else {
            // authentication failed
            logger.info("password verification failed for user: " + username);
            storeFailedAttempt(username);
            throw new FailedLoginException("Password Incorrect");
        }

        return true;
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
    @Override
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
     * @exception LoginException
     *                if the logout fails.
     *
     * @return true in all cases since this <code>LoginModule</code> should
     *         not be ignored.
     */
    @Override
    public boolean logout() throws LoginException {
        succeeded = false;
        return true;
    }

    /**
     * Connect using SSL encryption to the LDAP server <code>url</code> and
     * tries to authenticate with the <code>UID</code> and
     * <code>password</code>.
     *
     * <p>
     *
     * @param userDN user name
     * <p>
     * @param password user password
     * <p>
     * @return true if the authentication is successful, false otherwise.
     */
    private boolean checkLDAPPassword(String userDN, String password) {
        if (logger.isDebugEnabled()) {
            logger.debug("check password for user: " + userDN);
        }

        ContextHandler handler = createLdapContext(userDN, password);
        closeContext(handler);
        return handler != null;
    }

    private void closeContext(ContextHandler handler) {
        if (handler != null) {
            // Close the context when we're done
            if (handler.getTlsResponse() != null) {
                try {
                    handler.getTlsResponse().close();
                } catch (IOException e) {
                    logger.error("Problem closing tls session: " + e.getMessage(), e);

                }
            }
            try {
                handler.getDirContext().close();
            } catch (NamingException e) {
                logger.error("Problem closing connection: " + e.getMessage(), e);
            }
        }
    }

    private ContextHandler createLdapContext(String user, String password) {
        LdapContext ctx = null;
        StartTlsResponse tls = null;

        Hashtable<String, String> env = createBasicEnvForInitalContext();
        try {
            if (!START_TLS) {
                if (!AUTHENTICATION_METHOD.equals(ANONYMOUS_LDAP_CONNECTION)) {
                    env.put(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_METHOD);
                    env.put(Context.SECURITY_PRINCIPAL, user);
                    env.put(Context.SECURITY_CREDENTIALS, password);
                }
            }
            // Create the initial directory context
            ctx = new InitialLdapContext(env, null);

            if (START_TLS) {
                // Start TLS
                tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
                if (ANY_HOSTNAME) {
                    tls.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                }
                if (ANY_CERTIFICATE) {
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, new X509TrustManager[] { new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        public void checkServerTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    } }, new SecureRandom());
                    tls.negotiate(context.getSocketFactory());
                } else {
                    tls.negotiate();
                }
                if (!AUTHENTICATION_METHOD.equals(ANONYMOUS_LDAP_CONNECTION)) {
                    ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_METHOD);
                    ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, user);
                    ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
                }
            }
            return new ContextHandler(ctx, tls);
        } catch (NamingException e) {
            logger.error("Problem checking user password, user password may be wrong: " + e);
            return null;
        } catch (Exception e) {
            logger.error("Problem when creating the ldap context", e);
            return null;
        }
    }

    /**
     * Connects anonymously to the LDAP server <code>url</code> and retrieve
     * DN of the user <code>username</code>
     *
     * <p>
     * @exception NamingException
     *                if a naming exception is encountered.
     * <p>
     *
     * @return the String containing the UID of the user or null if the user is
     *         not found.
     */
    private String getLDAPUserDN(String username) throws NamingException {

        String userDN = null;
        ContextHandler ctx = null;
        try {

            // Create the initial directory context
            ctx = this.connectAndGetContext();
            if (ctx != null) {
                SearchControls sControl = new SearchControls();
                sControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
                String filter = String.format(ldapProperties.getProperty(LDAPProperties.LDAP_USER_FILTER), username);
                // looking for the user dn (distinguish name)
                NamingEnumeration<SearchResult> answer = ctx.getDirContext().search(new LdapName(USERS_DN),
                                                                                    filter,
                                                                                    sControl);
                if (answer.hasMoreElements()) {
                    SearchResult result = (SearchResult) answer.next();
                    userDN = result.getNameInNamespace();
                    if (logger.isDebugEnabled()) {
                        logger.debug("User " + username + " has LDAP entry " + userDN);
                    }
                    subject.getPrincipals().add(new UserNamePrincipal(username));

                    // looking for the user groups
                    String groupFilter = String.format(ldapProperties.getProperty(LDAPProperties.LDAP_GROUP_FILTER),
                                                       userDN);

                    NamingEnumeration<SearchResult> groupResults = ctx.getDirContext().search(new LdapName(GROUPS_DN),
                                                                                              groupFilter,
                                                                                              sControl);
                    while (groupResults.hasMoreElements()) {
                        SearchResult res = (SearchResult) groupResults.next();
                        Attribute attr = res.getAttributes()
                                            .get(ldapProperties.getProperty(LDAPProperties.LDAP_GROUPNAME_ATTR));
                        if (attr != null) {
                            String groupName = attr.get().toString();
                            subject.getPrincipals().add(new GroupNamePrincipal(groupName));
                            if (logger.isDebugEnabled()) {
                                logger.debug("User " + username + " is a member of group " + groupName);
                            }
                        }
                    }

                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("User DN not found");
                    }
                }
            }
        } catch (NamingException e) {
            logger.error("Problem with the search in mode: " + AUTHENTICATION_METHOD + e);
            throw e;
        } finally {
            closeContext(ctx);
        }

        return userDN;
    }

    /**
     * Performs connection to LDAP with appropriate security parameters
     * @return directory service interface.
     * @throws NamingException
     */
    private ContextHandler connectAndGetContext() throws NamingException {
        // Create the initial directory context
        return createLdapContext(BIND_LOGIN, BIND_PASSWD);
    }

    /**
     * Retrieves LDAP configuration file name.
     *
     * @return name of the file with LDAP configuration.
     */
    protected abstract String getLDAPConfigFileName();

    private Hashtable<String, String> createBasicEnvForInitalContext() {
        Hashtable<String, String> env = new Hashtable<>(6, 1f);
        env.put("com.sun.jndi.ldap.connect.pool", LDAP_CONNECTION_POOLING);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, LDAP_URL);

        return env;
    }

    private class ContextHandler {

        private DirContext dirContext;

        private StartTlsResponse tlsResponse;

        public ContextHandler(DirContext dirContext, StartTlsResponse tlsResponse) {
            this.dirContext = dirContext;
            this.tlsResponse = tlsResponse;
        }

        public DirContext getDirContext() {
            return dirContext;
        }

        public StartTlsResponse getTlsResponse() {
            return tlsResponse;
        }
    }

}
