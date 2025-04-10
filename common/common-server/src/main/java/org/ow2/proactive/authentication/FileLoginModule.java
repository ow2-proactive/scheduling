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

import java.io.*;
import java.security.KeyException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.principals.*;
import org.ow2.proactive.core.properties.PASharedProperties;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


/**
 * Authentication based on user and group file.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public abstract class FileLoginModule implements Loggable, LoginModule {

    /** connection logger */
    private Logger logger = getLogger();

    public static String DOMAIN_SEP = "@";

    public static final String ENCRYPTED_DATA_SEP = " ";

    private static Multimap<String, Long> failedAttempts = ArrayListMultimap.create();

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

    /** The file where to store tenant management **/
    protected String tenantFile = getTenantFileName();

    public static final String LOCK_FILE_NAME = "lock";

    protected Subject subject;

    protected UsersServiceImpl usersService;

    /**
     * Defines login file name
     * 
     * @return the login file name
     */
    protected String getLoginFileName() {
        return JaasConfigUtils.getLoginFileName();
    }

    /**
     * Defines group file name
     * 
     * @return the group file name
     */
    protected String getGroupFileName() {
        return JaasConfigUtils.getGroupFileName();
    }

    /**
     * Defines tenant file name
     *
     * @return the tenant file name
     */
    protected String getTenantFileName() {
        return JaasConfigUtils.getTenantFileName();
    }

    protected Set<String> getConfiguredDomains() {
        return JaasConfigUtils.getConfiguredDomains();
    }

    /**
     * Defines private key
     *
     * @return private key in use
     */
    protected PrivateKey getPrivateKey() throws KeyException {
        return Credentials.getPrivateKey(PASharedProperties.getAbsolutePath(PASharedProperties.AUTH_PRIVKEY_PATH.getValueAsString()));
    }

    /**
     * Defines public key
     *
     * @return public key in use
     */
    protected PublicKey getPublicKey() throws KeyException {
        return Credentials.getPublicKey(PASharedProperties.getAbsolutePath(PASharedProperties.AUTH_PUBKEY_PATH.getValueAsString()));
    }

    /**
     * Returns true if legacy password encryption is used (hybrid symetric key).
     * Returns false if newer encryption is used (hash/salt based)
     * @return
     */
    protected boolean isLegacyPasswordEncryption() {
        return PASharedProperties.LEGACY_ENCRYPTION.getValueAsBoolean();
    }

    /**
     * 
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {

        this.subject = subject;
        checkLoginFile();
        checkGroupFile();
        checkTenantFile();

        if (logger.isDebugEnabled()) {
            logger.debug("Using Login file at : " + this.loginFile);
            logger.debug("Using Group file at : " + this.groupFile);
            logger.debug("Using Tenant file at : " + this.tenantFile);
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

    protected void checkTenantFile() {
        //test tenant file existence
        if (!(new File(this.tenantFile).exists())) {
            throw new RuntimeException("The file " + this.tenantFile + " has not been found \n" +
                                       "Unable to perform user authentication by file method");
        }
    }

    /**
     * 
     * @see javax.security.auth.spi.LoginModule#login()
     * @throws LoginException if userName of password are not correct
     */
    @Override
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
            String domain = (String) params.get("domain");
            byte[] key = (byte[]) params.get("key");
            if (domain != null) {
                domain = domain.toLowerCase();
            }

            params.clear();
            ((NoCallback) callbacks[0]).clear();

            if (username == null) {
                logger.info("No username has been specified for authentication");
                throw new FailedLoginException("No username has been specified for authentication");
            }

            succeeded = logUser(username, password, key, domain, true);
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
     * @param isNotFallbackAuthentication true if this method is not called inside a fallback mechanism
     * @return true user login and password are correct, and requested group is authorized for the user
     * @throws LoginException if authentication or group membership fails.
     */
    protected boolean logUser(String username, String password, byte[] key, String domain,
            boolean isNotFallbackAuthentication) throws LoginException {
        usersService = UsersServiceImpl.getInstance();
        usersService.refresh();

        if (isNotFallbackAuthentication) {
            removeOldFailedAttempts(username);

            if (tooManyFailedAttempts(username)) {
                String message = "Too many failed login/attempts, please try again in " +
                                 retryInHowManyMinutes(username) + " minutes.";
                logger.warn("[" + FileLoginModule.class.getSimpleName() + "] " + message);
                throw new FailedLoginException(message);
            }
        }

        if (!authenticateUser(domain, username, password)) {
            String message = "[" + FileLoginModule.class.getSimpleName() + "] Incorrect Username/Password";
            if (isNotFallbackAuthentication) {
                logger.info(message);
            } else {
                logger.debug(message);
            }
            if (isNotFallbackAuthentication) {
                storeFailedAttempt(username);
            }
            throw new FailedLoginException("Incorrect Username/Password");
        } else {
            resetFailedAttempt(username);
            if (isNotFallbackAuthentication) {
                createAndStoreCredentialFile(domain, username, password, key, false);
            }
        }

        subject.getPrincipals().add(new UserNamePrincipal(username));
        if (domain != null) {
            if (!getConfiguredDomains().contains(domain.toLowerCase())) {
                throw new FailedLoginException("Invalid domain used: " + domain.toLowerCase() + " is not a member of " +
                                               getConfiguredDomains());
            }
            subject.getPrincipals().add(new DomainNamePrincipal(domain));
        }
        groupMembership(domain, username);
        tenantMembership(domain, username);
        logger.debug("authentication succeeded for user '" + username + "'");
        return true;
    }

    protected void storeFailedAttempt(String username) {
        failedAttempts.put(username, (new Date()).getTime());
    }

    protected void resetFailedAttempt(String username) {
        failedAttempts.removeAll(username);
    }

    protected void removeOldFailedAttempts(String username) {
        if (failedAttempts.containsKey(username)) {
            failedAttempts.get(username)
                          .removeIf(time -> time < now() -
                                                   (PASharedProperties.FAILED_LOGIN_RENEW_MINUTES.getValueAsInt() *
                                                    60000L));
        }
    }

    protected boolean tooManyFailedAttempts(String username) {
        if (PASharedProperties.FAILED_LOGIN_MAX_ATTEMPTS.getValueAsInt() > 0 && failedAttempts.containsKey(username) &&
            failedAttempts.get(username).size() >= PASharedProperties.FAILED_LOGIN_MAX_ATTEMPTS.getValueAsInt()) {
            return true;
        }
        return false;
    }

    protected int retryInHowManyMinutes(String username) {
        if (tooManyFailedAttempts(username)) {
            return PASharedProperties.FAILED_LOGIN_RENEW_MINUTES.getValueAsInt() -
                   (int) ((now() - failedAttempts.get(username).stream().min(Long::compare).get()) / 60000);
        }
        return 0;
    }

    private long now() {
        return (new Date()).getTime();
    }

    /**
     * Check user and password from login file.
     * @param domain user's domain
     * @param username user's login
     * @param password user's password
     * @return true if user is found in login file and its password is correct, falser otherwise
     * @throws LoginException if login file is not found or unreadable.
     */
    private boolean authenticateUser(String domain, String username, String password) throws LoginException {

        PrivateKey privateKey = null;
        try {
            privateKey = getPrivateKey();
        } catch (KeyException e) {
            throw new LoginException(e.toString());
        }

        String key = username;
        if (!Strings.isNullOrEmpty(domain)) {
            if (usersService.userExists(username + DOMAIN_SEP + domain)) {
                key = username + DOMAIN_SEP + domain;
            }
        }

        try {
            return usersService.checkPassword(key, password);
        } catch (Exception e) {
            throw new LoginException(e.toString());
        }
    }

    /**
     * Return corresponding group for a user from the group file.
     * @param username user's login
     * @throws LoginException if group file is not found or unreadable.
     */
    protected void groupMembership(String domain, String username) throws LoginException {
        String key = username;
        if (!Strings.isNullOrEmpty(domain)) {
            if (usersService.userExists(username + DOMAIN_SEP + domain)) {
                key = username + DOMAIN_SEP + domain;
            }
        }

        for (String group : usersService.getUser(key).getGroups()) {
            subject.getPrincipals().add(new GroupNamePrincipal(group));
            logger.debug("adding group principal '" + group + "' for user '" + key + "'");
        }
    }

    /**
     * Return corresponding tenant for a user from the tenant file.
     * @param username user's login
     * @throws LoginException if tenant file is not found or unreadable.
     */
    protected void tenantMembership(String domain, String username) throws LoginException {

        String key = username;
        if (!Strings.isNullOrEmpty(domain)) {
            if (usersService.userExists(username + DOMAIN_SEP + domain)) {
                key = username + DOMAIN_SEP + domain;
            }
        }
        OutputUserInfo userInfo = usersService.getUser(key);
        if (userInfo.getTenant() != null) {
            subject.getPrincipals().add(new TenantPrincipal(userInfo.getTenant()));
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

    protected String generateRandomPassword() {
        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(4, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);
        String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
        String totalChars = RandomStringUtils.randomAlphanumeric(2);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                                               .concat(numbers)
                                               .concat(specialChar)
                                               .concat(totalChars);
        List<Character> pwdChars = combinedChars.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        return pwdChars.stream().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

    protected void addShadowAccount(String domain, String username, byte[] key) throws LoginException {
        UserLoginInfo userLoginInfo = new UserLoginInfo();
        userLoginInfo.setLogin(username);
        userLoginInfo.setDomain(domain);
        String rndPassword = generateRandomPassword();
        userLoginInfo.setPassword(rndPassword);
        userLoginInfo.setKey(key);
        Set<String> groups = new HashSet<>();
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof GroupNamePrincipal) {
                groups.add(principal.getName());
            }
        }
        userLoginInfo.setGroups(groups);
        createOrUpdateShadowAccount(userLoginInfo);
    }

    protected boolean createOrUpdateShadowAccount(UserLoginInfo userLoginInfo) throws LoginException {
        if (!userLoginInfo.isLoginSet()) {
            throw new LoginException("Login name not set");
        }
        if (!userLoginInfo.isPasswordSet()) {
            throw new LoginException("Password not set");
        }
        if (!userLoginInfo.isGroupSet()) {
            throw new LoginException("Groups not set");
        }
        String key = userLoginInfo.getLogin() +
                     (Strings.isNullOrEmpty(userLoginInfo.getDomain()) ? "" : DOMAIN_SEP + userLoginInfo.getDomain());
        boolean newUser = false;
        boolean userChanged;
        if (!usersService.userExists(key)) {
            usersService.addUser(new InputUserInfo(key, userLoginInfo.getPassword(), userLoginInfo.getGroups()));
            newUser = true;
        } else {
            usersService.updateUser(new InputUserInfo(key, null, userLoginInfo.getGroups()));
        }
        userChanged = usersService.usersChanged();
        usersService.commit();
        if (newUser) {
            // if a new account is created, store the account credential file, this will add the shadow credentials to the subject
            // (this will be used by rm, scheduler or rest server to update the account credentials)
            createAndStoreCredentialFile(userLoginInfo.getDomain(),
                                         userLoginInfo.getLogin(),
                                         userLoginInfo.getPassword(),
                                         userLoginInfo.getKey(),
                                         true);
        } else if (userLoginInfo.getKey() != null) {
            // if the login info contains a ssh private key, update the credentials file
            byte[] newCredentials = usersService.addPrivateKeyToCredentials(userLoginInfo.getDomain(),
                                                                            userLoginInfo.getLogin(),
                                                                            userLoginInfo.getKey());
            subject.getPrincipals().add(new ShadowCredentialsPrincipal(userLoginInfo.getLogin(), newCredentials));
        } else {
            // if an existing shadow account is reused, add shadow credentials to the subject
            // (this will be used by rm, scheduler or rest server to update the account credentials)
            subject.getPrincipals()
                   .add(new ShadowCredentialsPrincipal(userLoginInfo.getLogin(),
                                                       usersService.readCredentialsFile(userLoginInfo.getDomain(),
                                                                                        userLoginInfo.getLogin())));
        }
        if (userChanged) {
            getLogger().info("Created/Updated shadow user " + userLoginInfo.getLogin());
        }
        return newUser;
    }

    protected void createAndStoreCredentialFile(String domain, String username, String password, byte[] key,
            boolean isShadowAccount) {
        if (!PASharedProperties.CREATE_CREDENTIALS_WHEN_LOGIN.getValueAsBoolean()) {
            return;
        }
        try {
            byte[] credentialBytes = usersService.createCredentials(Strings.isNullOrEmpty(domain) ? username
                                                                                                  : domain + "\\" +
                                                                                                    username,
                                                                    password,
                                                                    key);

            if (isShadowAccount) {
                subject.getPrincipals().add(new ShadowCredentialsPrincipal(username, credentialBytes));
            }
            usersService.saveCredentialsFile(domain, username, credentialBytes);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static class UserLoginInfo {
        private String login;

        private String password;

        private String domain;

        private byte[] key;

        private Collection<String> groups = Collections.emptyList();

        public UserLoginInfo() {
        }

        public boolean isLoginSet() {
            return !Strings.isNullOrEmpty(login);
        }

        public boolean isPasswordSet() {
            return !Strings.isNullOrEmpty(password);
        }

        public boolean isGroupSet() {
            return groups != null && !groups.isEmpty();
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public byte[] getKey() {
            return key;
        }

        public void setKey(byte[] key) {
            this.key = key;
        }

        public Collection<String> getGroups() {
            return groups;
        }

        public void setGroups(Collection<String> groups) {
            this.groups = groups;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }

    public static class ManageUsersException extends Exception {
        public String getAdditionalInfo() {
            return additionalInfo;
        }

        private String additionalInfo = null;

        public ManageUsersException(String message) {
            super(message);
        }

        public ManageUsersException(String message, Throwable cause) {
            super(message, cause);
        }

        public ManageUsersException(String message, Throwable cause, String additionalInfo) {
            super(message, cause);
            this.additionalInfo = additionalInfo;
        }
    }
}
