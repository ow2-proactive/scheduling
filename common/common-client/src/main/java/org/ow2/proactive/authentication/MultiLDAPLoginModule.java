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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
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
import org.ow2.proactive.authentication.principals.TenantPrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;

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
public abstract class MultiLDAPLoginModule extends FileLoginModule implements Loggable {

    /** connection logger */
    private final Logger logger = getLogger();

    private final Map<String, LDAPDomainConfiguration> ldapDomainConfigurations;

    private final static String FAKE_PASSWORD = "Frth481d";

    public static final String ANONYMOUS_LDAP_CONNECTION = "none";

    /** authentication status */
    private boolean succeeded = false;

    /**
     * Creates a new instance of LDAPLoginModule
     */
    public MultiLDAPLoginModule() {

        ldapDomainConfigurations = new HashMap<>();

        Map<String, String> ldapConfigurationPaths = getMultiLDAPConfig();
        for (Map.Entry<String, String> entry : ldapConfigurationPaths.entrySet()) {
            ldapDomainConfigurations.put(entry.getKey(),
                                         new LDAPDomainConfiguration(new LDAPProperties(entry.getValue()), logger));
        }

        checkLoginFile();
        checkGroupFile();
        checkTenantFile();
        logger.debug("Using Login file for fall back authentication at: " + loginFile);
        logger.debug("Using Group file for fall back group membership at: " + groupFile);
        logger.debug("Using Tenant file for fall back tenant membership at: " + tenantFile);
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
            String domain = (String) params.get("domain");
            String username = (String) params.get("username");
            String password = (String) params.get("pw");

            params.clear();
            ((NoCallback) callbacks[0]).clear();

            if (username == null) {
                logger.info("No username has been specified for authentication");
                throw new FailedLoginException("No username has been specified for authentication");
            }

            succeeded = logUser(domain, username, password);
            return succeeded;

        } catch (IOException ioe) {
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
    protected boolean logUser(String domain, String username, String password) throws LoginException {
        if (domain == null) {
            return super.logUser(username, password, false);
        } else {
            if (ldapDomainConfigurations.containsKey(domain)) {
                return internalLogUser(domain, username, password);
            } else {
                throw new FailedLoginException("Cannot login as " + domain + "\\" + username + ". LDAP domain " +
                                               domain + " is not configured");
            }
        }
    }

    private boolean internalLogUser(String domain, String username, String password) throws LoginException {

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
            userDN = getLDAPUserDN(domain, username);
        } catch (NamingException e) {
            logger.error("Cannot connect to LDAP server", e);
            throw new FailedLoginException("Cannot connect to LDAP server");
        }

        if (userDN == null) {
            logger.info("user entry not found in " + domain + " subtree " +
                        ldapDomainConfigurations.get(domain).getUsersDn() + " for user " + username);
            storeFailedAttempt(username);
            throw new FailedLoginException("User name doesn't exists");
        } else {
            // Check if the password match the user name
            passwordMatch = checkLDAPPassword(domain, userDN, password);
        }

        if (passwordMatch) {
            if (logger.isDebugEnabled()) {
                logger.debug("authentication succeeded, checking group");
            }
            resetFailedAttempt(username);

            if (ldapDomainConfigurations.get(domain).isFallbackGroupMembership()) {
                super.groupMembershipFromFile(username);
            }
            if (ldapDomainConfigurations.get(domain).isFallbackTenantMembership()) {
                super.tenantMembershipFromFile(username);
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
    private boolean checkLDAPPassword(String domain, String userDN, String password) {
        if (logger.isDebugEnabled()) {
            logger.debug("check password for user: " + userDN);
        }

        if (password == null || password.isEmpty()) {
            // Some LDAP server can allow connection with an empty password. This is not acceptable when we try to verify some user credentuals
            // So we use a fake password instead
            password = FAKE_PASSWORD;
        }

        ContextHandler handler = createLdapContext(domain, userDN, password, true);
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

    private ContextHandler createLdapContext(String domain, String user, String password,
            boolean requireAuthentication) {
        LdapContext ctx = null;
        StartTlsResponse tls = null;

        Hashtable<String, String> env = createBasicEnvForInitalContext(domain);
        try {
            if (!ldapDomainConfigurations.get(domain).isStartTls()) {
                if (requireAuthentication ||
                    !ldapDomainConfigurations.get(domain).getAuthenticationMethod().equals(ANONYMOUS_LDAP_CONNECTION)) {
                    if (requireAuthentication) {
                        // In case of anonymous bind, when we need to check some user credentials, we must force authentication to be simple
                        env.put(Context.SECURITY_AUTHENTICATION,
                                ANONYMOUS_LDAP_CONNECTION.equals(ldapDomainConfigurations.get(domain)
                                                                                         .getAuthenticationMethod()) ? "simple"
                                                                                                                     : ldapDomainConfigurations.get(domain)
                                                                                                                                               .getAuthenticationMethod());
                    } else {
                        env.put(Context.SECURITY_AUTHENTICATION,
                                ldapDomainConfigurations.get(domain).getAuthenticationMethod());
                    }
                    env.put(Context.SECURITY_PRINCIPAL, user);
                    env.put(Context.SECURITY_CREDENTIALS, password);
                }
            }
            // Create the initial directory context
            ctx = new InitialLdapContext(env, null);

            if (ldapDomainConfigurations.get(domain).isStartTls()) {
                // Start TLS
                tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
                if (ldapDomainConfigurations.get(domain).isAnyHostname()) {
                    tls.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                }
                if (ldapDomainConfigurations.get(domain).isAnyCertificate()) {
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
                if (requireAuthentication ||
                    !ldapDomainConfigurations.get(domain).getAuthenticationMethod().equals(ANONYMOUS_LDAP_CONNECTION)) {
                    if (requireAuthentication) {
                        // In case of anonymous bind, when we need to check some user credentials, we must force authentication to be simple
                        ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION,
                                             ANONYMOUS_LDAP_CONNECTION.equals(ldapDomainConfigurations.get(domain)
                                                                                                      .getAuthenticationMethod()) ? "simple"
                                                                                                                                  : ldapDomainConfigurations.get(domain)
                                                                                                                                                            .getAuthenticationMethod());
                    } else {
                        ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION,
                                             ldapDomainConfigurations.get(domain).getAuthenticationMethod());
                    }
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
    private String getLDAPUserDN(String domain, String username) throws NamingException {

        String userDN = null;
        ContextHandler ctx = null;
        LDAPDomainConfiguration ldapDomainConfiguration = ldapDomainConfigurations.get(domain);
        try {

            // Create the initial directory context
            ctx = this.connectAndGetContext(domain);
            if (ctx != null) {
                SearchControls sControl = new SearchControls();
                sControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
                String filter = String.format(ldapDomainConfiguration.getLdapUserFilter(), username);
                // looking for the user dn (distinguish name)
                NamingEnumeration<SearchResult> answer = ctx.getDirContext().search(
                                                                                    new LdapName(ldapDomainConfiguration.getUsersDn()),
                                                                                    filter,
                                                                                    sControl);
                if (answer.hasMoreElements()) {
                    SearchResult result = (SearchResult) answer.next();
                    userDN = result.getNameInNamespace();
                    if (logger.isDebugEnabled()) {
                        logger.debug("User " + username + " has LDAP entry " + userDN);
                    }
                    subject.getPrincipals().add(new UserNamePrincipal(username));

                    if (!Strings.isNullOrEmpty(ldapDomainConfiguration.getTenantAttribute())) {
                        Attribute tenantAttr = result.getAttributes().get(ldapDomainConfiguration.getTenantAttribute());
                        if (tenantAttr != null && tenantAttr.get() != null && !tenantAttr.get().toString().isEmpty()) {
                            subject.getPrincipals().add(new TenantPrincipal(tenantAttr.get().toString()));
                        }
                    } else {
                        // if a tenant attribute is not specified, the domain is used as tenant
                        subject.getPrincipals().add(new TenantPrincipal(domain));
                    }

                    // looking for the user groups
                    String groupFilter = String.format(ldapDomainConfiguration.getLdapGroupFilter(),
                                                       ldapDomainConfiguration.isUseUidInGroupSearch() ? username
                                                                                                       : userDN);

                    NamingEnumeration<SearchResult> groupResults = ctx.getDirContext().search(
                                                                                              new LdapName(ldapDomainConfiguration.getGroupsDn()),
                                                                                              groupFilter,
                                                                                              sControl);
                    while (groupResults.hasMoreElements()) {
                        SearchResult res = (SearchResult) groupResults.next();
                        Attribute attr = res.getAttributes().get(ldapDomainConfiguration.getGroupNameAttribute());
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
            logger.error("Problem with the search in mode: " + ldapDomainConfiguration.getAuthenticationMethod() + e);
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
    private ContextHandler connectAndGetContext(String domain) throws NamingException {
        // Create the initial directory context
        return createLdapContext(domain,
                                 ldapDomainConfigurations.get(domain).getBindLogin(),
                                 ldapDomainConfigurations.get(domain).getBindPasswd(),
                                 false);
    }

    /**
     * Retrieves Multi LDAP configuration.
     *
     * @return a map of (domain_name,configuration_file)
     */
    protected abstract Map<String, String> getMultiLDAPConfig();

    private Hashtable<String, String> createBasicEnvForInitalContext(String domain) {
        Hashtable<String, String> env = new Hashtable<>(6, 1f);
        env.put("com.sun.jndi.ldap.connect.pool", ldapDomainConfigurations.get(domain).getLdapConnectionPooling());
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapDomainConfigurations.get(domain).getLdapUrl());

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
