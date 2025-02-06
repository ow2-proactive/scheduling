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
import java.nio.charset.Charset;
import java.security.KeyException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.authentication.principals.*;
import org.ow2.proactive.core.properties.PASharedProperties;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;


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

    private static final ReentrantLock createUsersLock = new ReentrantLock();

    private Properties users = null;

    private Multimap<String, String> groups = null;

    private Map<String, String> tenants = null;

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

    /** file used to prevent concurrent modification of login.cfg or group.cfg **/
    public static File authenticationLockFile = new File(PASharedProperties.getAbsolutePath(PASharedProperties.AUTHENTICATION_DIR.getValueAsString()),
                                                         LOCK_FILE_NAME);

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
     * Defines tenant file name
     *
     * @return the tenant file name
     */
    protected abstract String getTenantFileName();

    protected abstract Set<String> getConfiguredDomains();

    /**
     * Defines private key
     *
     * @return private key in use
     */
    protected abstract PrivateKey getPrivateKey() throws KeyException;

    /**
     * Defines public key
     *
     * @return public key in use
     */
    protected abstract PublicKey getPublicKey() throws KeyException;

    /**
     * Returns true if legacy password encryption is used (hybrid symetric key).
     * Returns false if newer encryption is used (hash/salt based)
     * @return
     */
    protected abstract boolean isLegacyPasswordEncryption();

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

    private void updateCache() throws LoginException {
        try {
            createUsersLock.lock();
            users = readLoginFile();
            groups = readGroupsFromFile();
            tenants = readTenantsFromFile();
        } finally {
            createUsersLock.unlock();
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
        updateCache();
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
            if (users.containsKey(username + DOMAIN_SEP + domain)) {
                key = username + DOMAIN_SEP + domain;
            }
        }

        String encryptedPassword = (String) users.get(key);

        // verify the username and password
        if (encryptedPassword == null) {
            return false;
        } else {
            try {
                return checkPassword(encryptedPassword, password, privateKey);
            } catch (Exception e) {
                throw new LoginException(e.toString());
            }
        }
    }

    private Properties readLoginFile() throws LoginException {
        Properties props = new Properties();
        try (FileInputStream stream = new FileInputStream(loginFile)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            props.load(reader);
        } catch (IOException e) {
            throw new LoginException(e.toString());
        }
        return props;
    }

    private boolean checkPassword(String encryptedPassword, String password, PrivateKey privateKey)
            throws KeyException {
        if (isLegacyPasswordEncryption()) {
            if (!HybridEncryptionUtil.decryptBase64String(encryptedPassword, privateKey, ENCRYPTED_DATA_SEP)
                                     .equals(password)) {
                return false;
            } else {
                return true;
            }
        } else {
            return HybridEncryptionUtil.verifyPassword(password, encryptedPassword);
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
            if (groups.containsKey(username + DOMAIN_SEP + domain)) {
                key = username + DOMAIN_SEP + domain;
            }
        }

        for (String group : groups.get(key)) {
            subject.getPrincipals().add(new GroupNamePrincipal(group));
            logger.debug("adding group principal '" + group + "' for user '" + key + "'");
        }
    }

    private Multimap<String, String> readGroupsFromFile() throws LoginException {
        Multimap<String, String> groupsMap = TreeMultimap.create();

        String line;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(groupFile)))) {
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    String[] u2g = line.split(":");
                    if (u2g.length == 2) {
                        groupsMap.put(u2g[0].trim(), u2g[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            throw new LoginException("could not read group file " + groupFile + " : " + e.getMessage());
        }
        return groupsMap;
    }

    /**
     * Return corresponding tenant for a user from the tenant file.
     * @param username user's login
     * @throws LoginException if tenant file is not found or unreadable.
     */
    protected void tenantMembership(String domain, String username) throws LoginException {

        Set<String> groupNames = subject.getPrincipals()
                                        .stream()
                                        .filter(principal -> principal instanceof GroupNamePrincipal)
                                        .map(principal -> principal.getName())
                                        .collect(Collectors.toSet());
        boolean tenantDefined = false;
        for (String groupName : groupNames) {
            // only one tenant should be defined per user (the first tenant found)
            if (tenants.containsKey(groupName) && !tenantDefined) {
                String tenant = tenants.get(groupName);
                logger.debug("adding tenant principal '" + tenant + "' for user '" + username + "'");
                subject.getPrincipals().add(new TenantPrincipal(tenant));
                tenantDefined = true;
            }
        }
    }

    private Map<String, String> readTenantsFromFile() throws LoginException {
        Map<String, String> groupsToTenant = new TreeMap<>();
        try (FileInputStream stream = new FileInputStream(tenantFile)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    String[] u2g = line.split(":");
                    groupsToTenant.putIfAbsent(u2g[0].trim(), u2g[1].trim());
                }
            }
        } catch (FileNotFoundException e) {
            throw new LoginException(e.toString());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        }
        return groupsToTenant;
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
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin(username);
        userInfo.setDomain(domain);
        String rndPassword = generateRandomPassword();
        userInfo.setPassword(rndPassword);
        userInfo.setKey(key);
        Set<String> groups = new HashSet<>();
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof GroupNamePrincipal) {
                groups.add(principal.getName());
            }
        }
        userInfo.setGroups(groups);
        createOrUpdateShadowAccount(userInfo);
    }

    protected boolean createOrUpdateShadowAccount(UserInfo userInfo) throws LoginException {
        try {
            createUsersLock.lock();
            if (!userInfo.isLoginSet()) {
                throw new LoginException("Login name not set");
            }
            if (!userInfo.isPasswordSet()) {
                throw new LoginException("Password not set");
            }
            if (!userInfo.isGroupSet()) {
                throw new LoginException("Groups not set");
            }
            String key = userInfo.getLogin() +
                         (Strings.isNullOrEmpty(userInfo.getDomain()) ? "" : DOMAIN_SEP + userInfo.getDomain());
            boolean userUpdated = false;
            boolean groupsUpdated = false;
            Properties existingUsers = users;
            // check if the shadow account already exists, if not, a new account will be created
            if (!existingUsers.containsKey(key)) {
                try {
                    updateUserPassword(getPublicKey(), key, userInfo.getPassword(), existingUsers);
                    userUpdated = true;
                } catch (KeyException e) {
                    throw new LoginException("Key Error when updating user password : " + e.getMessage());
                }
            }
            Multimap<String, String> existingAllUsersGroups = groups;
            Set<String> existingUserGroups = new HashSet(existingAllUsersGroups.get(key));
            Set<String> newUserGroups = new HashSet<>(userInfo.getGroups());

            // track if the user groups have been modified, if yes, update the shadow account
            // for a new account, this test will always return true as existingUserGroups is empty
            if (!Sets.symmetricDifference(existingUserGroups, newUserGroups).isEmpty()) {
                updateUserGroups(key, userInfo.getGroups(), existingAllUsersGroups);
                groupsUpdated = true;
            }
            if (userUpdated) {
                // if a new account is created store the updated login.cfg file
                storeLoginFile(existingUsers);
                // store as well the account credential file, this will add the shadow credentials to the subject
                // (this will be used by rm, scheduler or rest server to update the account credentials)
                createAndStoreCredentialFile(userInfo.getDomain(),
                                             userInfo.getLogin(),
                                             userInfo.getPassword(),
                                             userInfo.getKey(),
                                             true);
            } else if (userInfo.getKey() != null) {
                byte[] newCredentials = addPrivateKeyToCredentials(userInfo.getDomain(),
                                                                   userInfo.getLogin(),
                                                                   userInfo.getKey());
                subject.getPrincipals().add(new ShadowCredentialsPrincipal(userInfo.getLogin(), newCredentials));
            } else {
                // if an existing shadow account is reused, add shadow credentials to the subject
                // (this will be used by rm, scheduler or rest server to update the account credentials)
                subject.getPrincipals()
                       .add(new ShadowCredentialsPrincipal(userInfo.getLogin(),
                                                           readCredentialsFile(userInfo.getDomain(),
                                                                               userInfo.getLogin())));
            }
            if (groupsUpdated) {
                // if the user groups have been modified store the updated group.cfg file
                storeGroups(existingAllUsersGroups);
            }
            if (userUpdated || groupsUpdated) {
                getLogger().info("Created/Updated shadow user " + userInfo.getLogin());
            }
            return userUpdated;
        } finally {
            createUsersLock.unlock();
        }
    }

    private void storeLoginFile(Properties props) throws LoginException {

        try {
            while (authenticationLockFile.exists()) {
                Thread.sleep(50);
            }
            authenticationLockFile.createNewFile();
            TreeMap<String, String> orderedProperties = new TreeMap<>();
            for (String key : props.stringPropertyNames()) {
                orderedProperties.put(key, props.getProperty(key));
            }

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(loginFile)))) {
                for (Map.Entry<String, String> entry : orderedProperties.entrySet()) {
                    writer.write(entry.getKey() + ":" + entry.getValue());
                    writer.newLine();
                }
            }
        } catch (Exception e) {
            throw new LoginException("could not write login file " + loginFile + " : " + e.getMessage());
        } finally {
            authenticationLockFile.delete();
        }
    }

    private void storeGroups(Multimap<String, String> groups) throws LoginException {
        try {
            while (authenticationLockFile.exists()) {
                Thread.sleep(50);
            }
            authenticationLockFile.createNewFile();
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(groupFile)))) {
                for (Map.Entry<String, String> userEntry : groups.entries()) {
                    writer.println(userEntry.getKey() + ":" + userEntry.getValue());
                }
            }

        } catch (Exception e) {
            throw new LoginException("could not write group file " + groupFile + " : " + e.getMessage());
        } finally {
            authenticationLockFile.delete();
        }
    }

    protected void updateUserPassword(PublicKey pubKey, String login, String password, Properties props)
            throws KeyException {
        String encodedPassword;
        if (isLegacyPasswordEncryption()) {
            encodedPassword = HybridEncryptionUtil.encryptStringToBase64(password,
                                                                         pubKey,
                                                                         FileLoginModule.ENCRYPTED_DATA_SEP);
        } else {
            encodedPassword = HybridEncryptionUtil.hashPassword(password);
        }
        props.put(login, encodedPassword);

    }

    protected void createAndStoreCredentialFile(String domain, String username, String password, byte[] key,
            boolean isShadowAccount) {
        if (!PASharedProperties.CREATE_CREDENTIALS_WHEN_LOGIN.getValueAsBoolean()) {
            return;
        }
        try {
            createUsersLock.lock();
            try {
                byte[] credentialBytes = createCredentials(Strings.isNullOrEmpty(domain) ? username
                                                                                         : domain + "\\" + username,
                                                           password,
                                                           key);

                if (isShadowAccount) {
                    subject.getPrincipals().add(new ShadowCredentialsPrincipal(username, credentialBytes));
                }
                saveCredentialsFile(domain, username, credentialBytes);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            createUsersLock.unlock();
        }
    }

    private byte[] readCredentialsFile(String domain, String username) throws LoginException {
        File credentialsfile = getCredentialsFile(domain, username);
        try {
            return Credentials.getCredentials(credentialsfile.getAbsolutePath()).getBase64();
        } catch (Exception e) {
            throw new LoginException("Unable to decrypt credentials file: " + e.getMessage());
        }
    }

    private byte[] addPrivateKeyToCredentials(String domain, String username, byte[] key) throws LoginException {
        File credentialsFile = getCredentialsFile(domain, username);
        try {
            Credentials originalCredentials = Credentials.getCredentials(credentialsFile.getAbsolutePath());
            CredData credData = originalCredentials.decrypt(getPrivateKey());
            credData.setKey(key);

            byte[] credentialBytes = createCredentials(Strings.isNullOrEmpty(domain) ? username
                                                                                     : domain + "\\" + username,
                                                       credData.getPassword(),
                                                       key);

            saveCredentialsFile(domain, username, credentialBytes);
            return credentialBytes;

        } catch (Exception e) {
            throw new LoginException("Unable to decrypt credentials file: " + e.getMessage());
        }
    }

    private File getCredentialsFile(String domain, String username) {
        return new File(PASharedProperties.SHARED_HOME.getValueAsString() + "/config/authentication/" + username +
                        (Strings.isNullOrEmpty(domain) ||
                         !PASharedProperties.USE_DOMAIN_IN_CREDENTIALS_FILE.getValueAsBoolean() ? ""
                                                                                                : DOMAIN_SEP + domain) +
                        ".cred");
    }

    private void saveCredentialsFile(String domain, String username, byte[] credentialBytes) {
        if (!PASharedProperties.CREATE_CREDENTIALS_WHEN_LOGIN.getValueAsBoolean()) {
            return;
        }
        File credentialsfile = getCredentialsFile(domain, username);

        if (credentialsfile.exists() && sameCredentialsBytes(credentialBytes, credentialsfile)) {
            return;
        }

        FileOutputStream fos = null;
        try {
            credentialsfile.delete();
            credentialsfile.createNewFile();
            fos = new FileOutputStream(credentialsfile);
            fos.write(credentialBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                }

            }
        }
    }

    private boolean sameCredentialsBytes(byte[] credentialBytes, File credentialsfile) {
        try (InputStream inputStream = new FileInputStream(credentialsfile)) {
            return Arrays.equals(credentialBytes, IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] createCredentials(String username, String password, byte[] key) throws KeyException {
        PublicKey pubKey = getPublicKey();

        Credentials cred = Credentials.createCredentials(new CredData(CredData.parseLogin(username),
                                                                      CredData.parseDomain(username),
                                                                      password,
                                                                      key),
                                                         pubKey);

        return cred.getBase64();
    }

    protected void updateUserGroups(String login, Collection<String> groups, Multimap<String, String> groupsMap) {
        if (!groups.isEmpty()) {
            groupsMap.replaceValues(login, groups);
        }
    }

    public static class UserInfo {
        private String login;

        private String password;

        private String domain;

        private byte[] key;

        private Collection<String> groups = Collections.emptyList();

        public UserInfo() {
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
