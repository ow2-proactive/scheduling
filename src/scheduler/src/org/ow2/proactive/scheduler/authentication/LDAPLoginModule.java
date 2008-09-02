package org.ow2.proactive.scheduler.authentication;

import java.net.MalformedURLException;
import java.net.URL;
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
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;


/**
 * @author gsigety
 *
 */
public class LDAPLoginModule implements LoginModule {

    /** connection logger */
    private static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CONNECTION);

    /** default value for Context.SECURITY_AUTHENTICATION 
     * that correspond to anonymous connection
    */
    private static final String ANONYMOUS_LDAP_CONNECTION = "none";

    /** name of key store path java property */
    private static final String SSL_KEYSTORE_PATH_PROPERTY = "javax.net.ssl.keyStore";

    /** name of key store password java property */
    private static final String SSL_KEYSTORE_PASSWD_PROPERTY = "javax.net.ssl.keyStorePassword";

    /** name of trust store password java property */
    private static final String SSL_TRUSTSTORE_PATH_PROPERTY = "javax.net.ssl.trustStore";

    /** name of trust store password java property */
    private static final String SSL_TRUSTSTORE_PASSWD_PROPERTY = "javax.net.ssl.trustStorePassword";

    /** LDAP used to perform authentication */
    private static final String LDAP_URL = PASchedulerProperties.SCHEDULER_LDAP_URL.getValueAsString();

    /** LDAP Subtree wherein users entries are searched*/
    private static final String USER_DN = PASchedulerProperties.SCHEDULER_LDAP_USERS_SUBTREE
            .getValueAsString();

    /** attribute name in a LDAP user entry that corresponds to user login name*/
    private static final String USER_LOGIN_ATTR_NAME = PASchedulerProperties.SCHEDULER_LDAP_USER_LOGIN_ATTR
            .getValueAsString();

    /** DN of an entry of type groupOfUniqueNames, that contains users DN that have user permissions */
    private static final String USERS_GROUP_DN = PASchedulerProperties.SCHEDULER_LDAP_USERS_GROUP_DN
            .getValueAsString();

    /** DN of an entry of type groupOfUniqueNames, that contains users DN that have admin permissions */
    private static final String ADMINS_GROUP_DN = PASchedulerProperties.SCHEDULER_LDAP_ADMINS_GROUP_DN
            .getValueAsString();

    /**
     * Authentication method used to bind to LDAP : none, simple, 
     * or one of the SASL authentication methods
     */
    private static final String AUTHENTICATION_METHOD = PASchedulerProperties.SCHEDULER_LDAP_AUTHENTICATION_METHOD
            .getValueAsString();

    /** user name used to bind to LDAP (if authentication method is different from none) */
    private static final String BIND_LOGIN = PASchedulerProperties.SCHEDULER_LDAP_BIND_LOGIN
            .getValueAsString();

    /** user password used to bind to LDAP (if authentication method is different from none) */
    private final String BIND_PASSWD = PASchedulerProperties.SCHEDULER_LDAP_BIND_PASSWD.getValueAsString();

    /**
     * JAAS call back handler used to get authentication request parameters 
     */
    private CallbackHandler callbackHandler;

    /** authentication status */
    private boolean succeeded = false;

    /** map defining  a group name and its relative DN in LDAP */
    private Map<String, String> groupDNMap;

    static {
        //initialize system properties for SSL/TLS connection
        String keyStore = PASchedulerProperties.SCHEDULER_LDAP_KEYSTORE_PATH.getValueAsString();
        if (keyStore != null && !"".equals(keyStore)) {
            System.setProperty(SSL_KEYSTORE_PATH_PROPERTY, keyStore);
            System.setProperty(SSL_KEYSTORE_PASSWD_PROPERTY,
                    PASchedulerProperties.SCHEDULER_LDAP_KEYSTORE_PASSWD.getValueAsString());
        }

        String trustStore = PASchedulerProperties.SCHEDULER_LDAP_TRUSTSTORE_PATH.getValueAsString();
        if (trustStore != null && !"".equals(trustStore)) {
            System.setProperty(SSL_TRUSTSTORE_PATH_PROPERTY, trustStore);
            System.setProperty(SSL_TRUSTSTORE_PASSWD_PROPERTY,
                    PASchedulerProperties.SCHEDULER_LDAP_TRUSTSTORE_PASSWD.getValueAsString());
        }
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
     */
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
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available "
                + "to garner authentication information from the user");
        }

        Callback[] callbacks = new Callback[] { new NoCallback() };
        String username = null;
        String password = null;
        String reqGroup = null;
        GroupHierarchy groupsHierarchy = null;
        String userDN = null;
        String[] hierarchyArray = null;

        boolean passwordMatch = false;
        try {

            // gets the user name, password, group Membership, and group Hierarchy from call back handler
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
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                " not available to garner authentication information " + "from the user");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("LDAP authentication requested for user : " + username);
            String hierarchyRepresentation = "";
            for (String s : hierarchyArray) {
                hierarchyRepresentation += (s + " ");
            }
            logger.debug("requested group : " + reqGroup + ", group hierarchy : " + hierarchyRepresentation);
        }

        // check the user name, get the RDN of the user
        // (null = not found)
        try {
            userDN = getLDAPUserDN(username);
        } catch (NamingException e) {
            e.printStackTrace();
            succeeded = false;
            throw new FailedLoginException("Cannot connect to LDAP server");
        }

        if (userDN == null) {
            succeeded = false;
            logger.info("user entry not found in subtree " + USER_DN + " for login " + username);
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
            logger.info("password verification failed");
            succeeded = false;
            throw new FailedLoginException("Password Incorrect");
        }

        if (reqGroup == null) {
            succeeded = false;
            throw new FailedLoginException("No group has been specified for authentication");
        }

        boolean groupOk = checkLDAPGroupMemberShip(userDN, reqGroup, groupsHierarchy);

        if (!groupOk) {
            logger.info("group membership verification failed");
            throw new FailedLoginException("User doesn't belong to a group");
        } else {
            succeeded = true;
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
     * Connect using SSL encryption to the LDAP server <code>url</code> and
     * tries to authenticate with the <code>UID</code> and
     * <code>password</code>.
     *
     * <p>
     *
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
        } finally {
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                e.printStackTrace();
                logger.error("Problem closing LDAP connection : " + e.getMessage());
            }
        }

        return userDN;
    }

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
                e.printStackTrace();
                logger.error("Problem closing LDAP connection : " + e.getMessage());
            }
        }
        return groupMemberShip;
    }

    /**
     * Performs connection to LDAP with appropriate security parameters
     * @return
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
}