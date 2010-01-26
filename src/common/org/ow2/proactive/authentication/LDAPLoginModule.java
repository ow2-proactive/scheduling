/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.authentication;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;


/**
 * Authentication based on LDAP system.
 *
 * This class extends FileLoginModule in order to use authentication fall back mechanism :
 *
 * If user entry is not found in LDAP and file authentication fall back is activated
 * {@link org.ow2.proactive.authentication.LDAPProperties#FALLBACK_USER_AUTH}, it tries authentication (login/password and group membership
 * with file login module.
 *
 * If no group is found in LDAP and group membership file checking fall back is activated
 * {@link org.ow2.proactive.authentication.LDAPProperties#FALLBACK_GROUP_MEMBERSHIP}, its group membership is checked with file login module.
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
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

    /** LDAP used to perform authentication */
    private final String LDAP_URL = ldapProperties.getProperty(LDAPProperties.LDAP_URL);

    /** LDAP Subtree wherein users entries are searched*/
    private final String USER_DN = ldapProperties.getProperty(LDAPProperties.LDAP_USERS_SUBTREE);

    /** attribute name in a LDAP user entry that corresponds to user login name*/
    private final String USER_LOGIN_ATTR_NAME = ldapProperties
            .getProperty(LDAPProperties.LDAP_USER_LOGIN_ATTR);

    /** DN of an entry of type groupOfUniqueNames, that contains users DN that have user permissions */
    private final String USERS_GROUP_DN = ldapProperties.getProperty(LDAPProperties.LDAP_USERS_GROUP_DN);

    /** DN of an entry of type groupOfUniqueNames, that contains users DN that have admin permissions */
    private final String ADMINS_GROUP_DN = ldapProperties.getProperty(LDAPProperties.LDAP_ADMINS_GROUP_DN);

    /**
     * Authentication method used to bind to LDAP : none, simple, 
     * or one of the SASL authentication methods
     */
    private final String AUTHENTICATION_METHOD = ldapProperties
            .getProperty(LDAPProperties.LDAP_AUTHENTICATION_METHOD);

    /** user name used to bind to LDAP (if authentication method is different from none) */
    private final String BIND_LOGIN = ldapProperties.getProperty(LDAPProperties.LDAP_BIND_LOGIN);

    /** user password used to bind to LDAP (if authentication method is different from none) */
    private String BIND_PASSWD = ldapProperties.getProperty(LDAPProperties.LDAP_BIND_PASSWD);

    /**fall back property, check user/password and group in files if user in not found in LDAP */
    private boolean fallbackUserAuth = Boolean.valueOf(ldapProperties
            .getProperty(LDAPProperties.FALLBACK_USER_AUTH));

    /**group fall back property, check user group membership group file if user in not found in corresponding LDAP group*/
    private boolean fallbackGroupMembership = Boolean.valueOf(ldapProperties
            .getProperty(LDAPProperties.FALLBACK_GROUP_MEMBERSHIP));

    /** authentication status */
    private boolean succeeded = false;

    /** map defining  a group name and its relative DN in LDAP */
    private Map<String, String> groupDNMap;

    /**
     * Creates a new instance of LDAPLoginModule
     */
    public LDAPLoginModule() {
        if (fallbackUserAuth) {
            checkLoginFile();
            checkGroupFile();
            logger.info("Using Login file for fall back authentication at : " + loginFile);
            logger.info("Using Group file for fall back group membership at : " + groupFile);
        } else if (fallbackGroupMembership) {
            checkGroupFile();
            logger.info("Using Group file for fall back group membership at : " + groupFile);
        }

        //initialize system properties for SSL/TLS connection
        String keyStore = ldapProperties.getProperty(LDAPProperties.LDAP_KEYSTORE_PATH);
        if (!alreadyDefined(SSL_KEYSTORE_PATH_PROPERTY, keyStore)) {
            System.setProperty(SSL_KEYSTORE_PATH_PROPERTY, keyStore);
            System.setProperty(SSL_KEYSTORE_PASSWD_PROPERTY, ldapProperties
                    .getProperty(LDAPProperties.LDAP_KEYSTORE_PASSWD));
        }

        String trustStore = ldapProperties.getProperty(LDAPProperties.LDAP_TRUSTSTORE_PATH);
        if (!alreadyDefined(SSL_TRUSTSTORE_PATH_PROPERTY, trustStore)) {
            System.setProperty(SSL_TRUSTSTORE_PATH_PROPERTY, trustStore);
            System.setProperty(SSL_TRUSTSTORE_PASSWD_PROPERTY, ldapProperties
                    .getProperty(LDAPProperties.LDAP_TRUSTSTORE_PASSWD));
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
                logger.warn("Property " + propertyName + " is already defined");
                logger.warn("Using old value " + propertyValue);
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
        this.callbackHandler = callbackHandler;

        groupDNMap = new HashMap<String, String>(2);
        groupDNMap.put("admin", ADMINS_GROUP_DN);
        groupDNMap.put("user", USERS_GROUP_DN);

        if (logger.isDebugEnabled()) {
            logger.debug("Using LDAP : " + LDAP_URL);
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
            throw new LoginException("Error: no CallbackHandler available "
                + "to garner authentication information from the user");
        }

        try {

            Callback[] callbacks = new Callback[] { new NoCallback() };

            // gets the user name, password, group Membership, and group Hierarchy from call back handler
            callbackHandler.handle(callbacks);
            Map<String, Object> params = ((NoCallback) callbacks[0]).get();
            String username = (String) params.get("username");
            String password = (String) params.get("pw");
            String reqGroup = (String) params.get("group");
            String[] hierarchyArray = (String[]) params.get("groupsHierarchy");
            GroupHierarchy groupsHierarchy = new GroupHierarchy(hierarchyArray);

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

            if (logger.isDebugEnabled()) {
                logger.debug("LDAP authentication requested for user : " + username);
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
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                " not available to garner authentication information " + "from the user");
        }
    }

    /**
     * Check user and password from LDAP file. If user is authenticated,
     * check group membership in LDAP groups.
     *
     * If user entry is not found in LDAP and file authentication fall back is activated
     * {@link org.ow2.proactive.authentication.LDAPProperties#FALLBACK_USER_AUTH}, it tries authentication (login/password and group membership)
     * with file login module.
     *
     * If no group is found in LDAP and group membership fall back is activated
     * {@link org.ow2.proactive.authentication.LDAPProperties#FALLBACK_GROUP_MEMBERSHIP}, its group membership is checked with file login module.
     *
     * @param username user's login
     * @param password user's password
     * @param reqGroup requested level
     * @param groupsHierarchy Group hierarchy used for authentication.
     * @return true user login and password are correct, and requested group is authorized for the user
     * @throws LoginException if authentication and group membership fails.
     */
    @Override
    protected boolean logUser(String username, String password, String reqGroup,
            GroupHierarchy groupsHierarchy) throws LoginException {

        // check the user name, get the RDN of the user
        // (null = not found)
        String userDN = null;
        boolean passwordMatch = false;
        try {
            userDN = getLDAPUserDN(username);
        } catch (NamingException e) {
            logger.error("", e);
            throw new FailedLoginException("Cannot connect to LDAP server");
        }

        if (userDN == null) {
            logger.info("user entry not found in subtree " + USER_DN + " for user " + username);
            if (fallbackUserAuth) {
                logger.info("fall back to file authentication for user : " + username);
                return super.logUser(username, password, reqGroup, groupsHierarchy);
            } else
                throw new FailedLoginException("User name doesn't exists");
        } else {
            // Check if the password match the user name
            passwordMatch = checkLDAPPassword(userDN, password);
        }

        if (passwordMatch) {
            if (logger.isDebugEnabled()) {
                logger.debug("authentication succeeded, checking group");
            }
        } else {
            // authentication failed
            logger.info("password verification failed for user : " + username);
            throw new FailedLoginException("Password Incorrect");
        }

        boolean groupOk = checkLDAPGroupMemberShip(userDN, reqGroup, groupsHierarchy);

        if (!groupOk) {
            logger.info("LDAP group membership verification failed for user " + username);
            if (this.fallbackGroupMembership) {
                logger.info("fall back to file group membership checking for user : " + username +
                    ", requested group : " + reqGroup);
                String group = super.checkGroupMemberShip(username, reqGroup, groupsHierarchy);
                logger.info("authentication succeeded for user '" + username + "' in group '" + group + "'");
                return true;
            } else
                throw new FailedLoginException("User doesn't belong to a group");
        } else {
            return true;
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
        // Secured connection to check the user password
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        if (logger.isDebugEnabled()) {
            logger.debug("check password for user : " + userDN);
        }

        env.put(Context.PROVIDER_URL, LDAP_URL);
        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.SECURITY_CREDENTIALS, password);

        DirContext ctx = null;
        try {
            // Create the initial directory context
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            logger.error("Problem checkin user password, user password may be wrong : " + e);
            return false;
        }

        // Close the context when we're done
        try {
            ctx.close();
        } catch (NamingException e) {
            logger.error("Problem closing secure connection : " + e);
        }
        return true;
    }

    /**
     * Connects anonymously to the LDAP server <code>url</code> and retreive
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
        DirContext ctx = null;
        try {

            // Create the initial directory context
            ctx = this.connectAndGetContext();
            SearchControls sControl = new SearchControls();
            sControl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String filter = "(&(objectclass=*)(" + USER_LOGIN_ATTR_NAME + "=" + username + "))";

            NamingEnumeration<SearchResult> answer = ctx.search(USER_DN, filter, sControl);
            if (answer.hasMoreElements()) {
                SearchResult result = (SearchResult) answer.next();
                userDN = result.getNameInNamespace();
                if (logger.isDebugEnabled()) {
                    logger.debug("User DN found : " + userDN);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("User DN not found");
                }
            }
        } catch (NamingException e) {
            logger.error("Problem with the search in mode : " + AUTHENTICATION_METHOD + e);
            throw e;
        } finally {
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                logger.error("", e);
                logger.error("Problem closing LDAP connection : " + e.getMessage());
            }
        }

        return userDN;
    }

    /**
     * Checks is user belongs to the group
     *
     * @param userDN user name
     * @param reqGroup group to check
     * @param hierarchy group hierarchy
     *
     * @return true if user is a member of this group according to hierarchy
     */
    private boolean checkLDAPGroupMemberShip(String userDN, String reqGroup, GroupHierarchy hierarchy) {
        boolean groupMemberShip = false;

        // Create the initial directory context
        DirContext ctx = null;
        try {
            ctx = this.connectAndGetContext();
            String filter = "(&(objectclass=groupOfUniqueNames)(uniqueMember=" + userDN + "))";
            SearchControls sControl = new SearchControls();
            sControl.setSearchScope(SearchControls.SUBTREE_SCOPE);

            //try to find group membership in LDAP's defined groups
            for (Entry<String, String> ldapGroupEntry : this.groupDNMap.entrySet()) {
                String groupName = ldapGroupEntry.getKey();

                if (logger.isDebugEnabled()) {
                    logger.debug("test Group : " + groupName);
                }

                //check first if LDAP group to test is above or at same level of requested group
                //if not, useless to test user membership
                if (hierarchy.isGroupInHierarchy(groupName) && hierarchy.isAbove(groupName, reqGroup)) {
                    String groupDN = ldapGroupEntry.getValue();
                    if (logger.isDebugEnabled()) {
                        logger.debug("checking group : " + groupDN + " membership for user dn : " + userDN);
                    }
                    //perform LDAP search on a LDAP's group
                    NamingEnumeration<SearchResult> answer = ctx.search(groupDN, filter, sControl);
                    //check if user is member of this group
                    if (answer.hasMoreElements()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(userDN + " is member of  group : " + groupDN);
                        }
                        //user is member of group and group level is above or equal of requested level
                        groupMemberShip = true;
                        break;
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug(userDN + " is not member of  group : " + groupDN);
                        }
                    }
                }
            }
        } catch (NamingException e) {
            logger.error("Problem with the search in mode : " + AUTHENTICATION_METHOD + e);
            return false;
        } catch (GroupException e) {
            logger.error("Problem with group hierarchy: " + e.getMessage());
            return false;
        } finally {
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                logger.error("", e);
                logger.error("Problem closing LDAP connection : " + e.getMessage());
            }
        }
        return groupMemberShip;
    }

    /**
     * Performs connection to LDAP with appropriate security parameters
     * @return directory service interface.
     * @throws NamingException
     */
    private DirContext connectAndGetContext() throws NamingException {

        // Secured connection to check the user password
        Hashtable<String, String> env = new Hashtable<String, String>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_METHOD);

        // LDAP server URL
        env.put(Context.PROVIDER_URL, LDAP_URL);
        if (AUTHENTICATION_METHOD.equals(ANONYMOUS_LDAP_CONNECTION)) {
            env.put(Context.SECURITY_PRINCIPAL, BIND_LOGIN);
            env.put(Context.SECURITY_CREDENTIALS, BIND_PASSWD);
        }
        // Create the initial directory context
        return new InitialDirContext(env);
    }

    /**
     * Retrieves LDAP configuration file name.
     *
     * @return name of the file with LDAP configuration.
     */
    protected abstract String getLDAPConfigFileName();

}
