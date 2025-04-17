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
import java.math.BigInteger;
import java.nio.file.*;
import java.security.KeyException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.core.properties.PASharedProperties;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;


@PublicAPI
public class UsersServiceImpl implements UsersService {

    public static String DOMAIN_SEP = "@";

    public static final String ENCRYPTED_DATA_SEP = " ";

    private static UsersServiceImpl instance;

    public static UsersServiceImpl getInstance() {
        if (instance == null) {
            instance = new UsersServiceImpl();
        }
        return instance;
    }

    private static final Logger logger = Logger.getLogger(UsersServiceImpl.class);

    private static final ReentrantReadWriteLock usersServiceLock = new ReentrantReadWriteLock();

    private static final ReentrantReadWriteLock.ReadLock readLock = usersServiceLock.readLock();

    private static final ReentrantReadWriteLock.WriteLock writeLock = usersServiceLock.writeLock();

    public static final String LOCK_FILE_NAME = "lock";

    /** file used to prevent concurrent modification of login.cfg or group.cfg **/
    public static File authenticationLockFile = new File(PASharedProperties.getAbsolutePath(PASharedProperties.AUTHENTICATION_DIR.getValueAsString()),
                                                         LOCK_FILE_NAME);

    /** The file where to store the allowed user//password */
    private String loginFile = null;

    /** The file where to store group management */
    private String groupFile = null;

    /** The file where to store tenant management **/
    private String tenantFile = null;

    private Map<String, InternalUserInfo> internalUserInfos = new HashMap<>();

    private Map<String, String> tenants = null;

    private boolean tenantsChanged = false;

    private boolean usersDeleted = false;

    private PublicKey publicKey = null;

    private PrivateKey privateKey = null;

    private String loginFileCheckSum = null;

    private String groupFileCheckSum = null;

    private String tenantFileCheckSum = null;

    private UsersServiceImpl() {
    }

    // Lifecycle methods

    @Override
    public void refresh() throws LoginException {
        try {
            writeLock.lock();
            if (loginFileCheckSum == null || groupFileCheckSum == null || tenantFileCheckSum == null) {
                internalRefresh();
                return;
            }
            String tmpLoginFileCheckSum = computeChecksum(loginFile);
            String tmpGroupFileCheckSum = computeChecksum(groupFile);
            String tmpTenantFileCheckSum = computeChecksum(tenantFile);
            if (!loginFileCheckSum.equals(tmpLoginFileCheckSum) || !groupFileCheckSum.equals(tmpGroupFileCheckSum) ||
                !tenantFileCheckSum.equals(tmpTenantFileCheckSum)) {
                internalRefresh();
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean usersChanged() {
        try {
            readLock.lock();
            return internalUserInfos.values().stream().filter(ui -> ui.isChanged()).findAny().isPresent();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void commit() throws LoginException {
        try {
            writeLock.lock();
            if (usersChanged() || usersDeleted) {
                storeLoginFile(getUserProperties());
                storeGroups(getUserGroups());
                internalUserInfos.values().stream().forEach(ui -> ui.setChanged(false));
                usersDeleted = false;
            }
            if (tenantsChanged) {
                storeTenants(getUserTenants());
                tenantsChanged = false;
            }

        } finally {
            writeLock.unlock();
        }
    }

    // Users service methods

    @Override
    public List<OutputUserInfo> listUsers() throws LoginException {
        try {
            readLock.lock();
            return listUsersInternal();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<OutputUserInfo> addUser(InputUserInfo userInfo) throws LoginException {
        try {
            writeLock.lock();
            if (internalUserInfos.containsKey(userInfo.getLogin())) {
                throw new IllegalArgumentException("User " + userInfo.getLogin() + " is already present");
            }
            String tenant = findTenant(userInfo.getGroups());
            try {
                if (PASharedProperties.PASSWORD_STRENGTH_ENABLE.getValueAsBoolean()) {
                    if (!userInfo.getPassword()
                                 .matches(PASharedProperties.PASSWORD_STRENGTH_REGEXP.getValueAsString())) {
                        throw new LoginException("Password of new user " + userInfo.getLogin() +
                                                 " does not satisfy strength requirements: " +
                                                 PASharedProperties.PASSWORD_STRENGTH_ERROR_MESSAGE.getValueAsString());
                    }
                }
                internalUserInfos.put(userInfo.getLogin(),
                                      new InternalUserInfo(userInfo.getLogin(),
                                                           encodePassword(userInfo.getPassword()),
                                                           tenant,
                                                           userInfo.getGroups(),
                                                           true));
            } catch (KeyException e) {
                logger.error("Error when encoding password: " + e.getMessage(), e);
                throw buildLoginException("Error when encoding password", e);
            }
            return listUsersInternal();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean userExists(String userName) {
        try {
            readLock.lock();
            return internalUserInfos.containsKey(userName);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public OutputUserInfo getUser(String userName) throws LoginException {
        try {
            readLock.lock();
            InternalUserInfo found = internalUserInfos.get(userName);
            if (found == null) {
                throw new IllegalArgumentException("Cannot find user " + userName);
            }
            return new OutputUserInfo(found.getLogin(), found.getTenant(), found.getGroups());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<OutputUserInfo> updateUser(InputUserInfo userInfo) throws LoginException {
        try {
            writeLock.lock();
            InternalUserInfo found = internalUserInfos.get(userInfo.getLogin());
            if (found == null) {
                throw new IllegalArgumentException("Cannot find user " + userInfo.getLogin());
            }
            if (userInfo.getPassword() != null) {
                try {
                    if (!checkPassword(userInfo.getLogin(), userInfo.getPassword())) {
                        // different password
                        if (PASharedProperties.PASSWORD_STRENGTH_ENABLE.getValueAsBoolean()) {
                            if (!userInfo.getPassword()
                                         .matches(PASharedProperties.PASSWORD_STRENGTH_REGEXP.getValueAsString())) {
                                throw new LoginException("Password of user " + userInfo.getLogin() +
                                                         " does not satisfy strength requirements: " +
                                                         PASharedProperties.PASSWORD_STRENGTH_ERROR_MESSAGE.getValueAsString());
                            }
                        }

                        String encodedPassword = encodePassword(userInfo.getPassword());
                        found.setPassword(encodedPassword);
                        // change password in credentials file
                        changePasswordInCredentials(userInfo.getLogin(), userInfo.getPassword());
                    }
                } catch (Exception e) {
                    logger.error("Error when updating user's password for user " + userInfo.getLogin(), e);
                    throw buildLoginException("Error when updating user's password for user " + userInfo.getLogin(), e);
                }
            }
            found.setGroups(userInfo.getGroups());
            found.setTenant(findTenant(found.getGroups()));
            return listUsersInternal();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<OutputUserInfo> deleteUser(String userName) throws LoginException {
        try {
            writeLock.lock();
            internalUserInfos.remove(userName);
            usersDeleted = true;
            return listUsersInternal();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Multimap<String, String> listTenants() {
        try {
            readLock.lock();
            return tenantsToMMap();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Multimap<String, String> addOrEditTenant(String tenant, Set<String> groups) {
        try {
            if (groups == null) {
                throw new IllegalArgumentException("groups cannot be null");
            }
            writeLock.lock();
            tenants.entrySet().removeIf(e -> e.getValue().equals(tenant));
            groups.forEach(g -> tenants.put(g, tenant));
            tenantsChanged = true;
            updateUsersTenant();
            return tenantsToMMap();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Multimap<String, String> removeTenant(String tenant) {
        try {
            writeLock.lock();
            tenantsChanged = tenants.entrySet().removeIf(e -> e.getValue().equals(tenant));
            updateUsersTenant();
            return tenantsToMMap();
        } finally {
            writeLock.unlock();
        }
    }

    // Login module methods

    @Override
    public boolean checkPassword(String userName, String password) throws LoginException {
        try {
            readLock.lock();
            if (!internalUserInfos.containsKey(userName)) {
                return false;
            }
            String encryptedPassword = internalUserInfos.get(userName).getPassword();
            try {
                return internalCheckPassword(encryptedPassword, password, privateKey);
            } catch (Exception e) {
                throw buildLoginException("Error when checking password for user " + userName, e);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<String> getGroups(String userName) throws LoginException {
        try {
            readLock.lock();
            if (!internalUserInfos.containsKey(userName)) {
                throw new LoginException("user " + userName + " not found");
            }
            InternalUserInfo userInfo = internalUserInfos.get(userName);
            return userInfo.getGroups();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getTenant(String userName) throws LoginException {
        try {
            readLock.lock();
            if (!internalUserInfos.containsKey(userName)) {
                throw new LoginException("user " + userName + " not found");
            }
            InternalUserInfo userInfo = internalUserInfos.get(userName);
            return userInfo.getTenant();
        } finally {
            readLock.unlock();
        }
    }

    public void saveCredentialsFile(String domain, String username, byte[] credentialBytes) {
        if (!PASharedProperties.CREATE_CREDENTIALS_WHEN_LOGIN.getValueAsBoolean()) {
            return;
        }
        try {
            writeLock.lock();
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
            logger.info("Stored credential file " + credentialsfile);
        } finally {
            writeLock.unlock();
        }
    }

    public byte[] readCredentialsFile(String domain, String username) throws LoginException {
        File credentialsfile = getCredentialsFile(domain, username);
        try {
            readLock.lock();
            return Credentials.getCredentials(credentialsfile.getAbsolutePath()).getBase64();
        } catch (Exception e) {
            throw new LoginException("Unable to decrypt credentials file: " + e.getMessage());
        } finally {
            readLock.unlock();
        }
    }

    public File getCredentialsFile(String domain, String username) {
        return new File(PASharedProperties.getAbsolutePath(PASharedProperties.AUTHENTICATION_DIR.getValueAsString() +
                                                           "/" + username +
                                                           (Strings.isNullOrEmpty(domain) ||
                                                            !PASharedProperties.USE_DOMAIN_IN_CREDENTIALS_FILE.getValueAsBoolean() ? ""
                                                                                                                                   : DOMAIN_SEP +
                                                                                                                                     domain) +
                                                           ".cred"));
    }

    public byte[] createCredentials(String username, String password, byte[] key) throws KeyException {

        Credentials cred = Credentials.createCredentials(new CredData(CredData.parseLogin(username),
                                                                      CredData.parseDomain(username),
                                                                      password,
                                                                      key),
                                                         publicKey);

        return cred.getBase64();
    }

    public byte[] addPrivateKeyToCredentials(String domain, String username, byte[] key) throws LoginException {
        File credentialsFile = getCredentialsFile(domain, username);
        try {
            writeLock.lock();
            Credentials originalCredentials = Credentials.getCredentials(credentialsFile.getAbsolutePath());
            CredData credData = originalCredentials.decrypt(privateKey);
            credData.setKey(key);

            byte[] credentialBytes = createCredentials(Strings.isNullOrEmpty(domain) ? username
                                                                                     : domain + "\\" + username,
                                                       credData.getPassword(),
                                                       key);

            saveCredentialsFile(domain, username, credentialBytes);
            return credentialBytes;

        } catch (Exception e) {
            throw new LoginException("Unable to decrypt credentials file: " + e.getMessage());
        } finally {
            writeLock.unlock();
        }
    }

    public void changePasswordInCredentials(String username, String newPassword) throws LoginException {
        File credentialsFile = getCredentialsFile(null, username);
        if (credentialsFile.exists()) {
            try {
                Credentials originalCredentials = Credentials.getCredentials(credentialsFile.getAbsolutePath());
                CredData credData = originalCredentials.decrypt(privateKey);
                credData.setPassword(newPassword);

                byte[] credentialBytes = createCredentials(Strings.isNullOrEmpty(credData.getDomain()) ? username
                                                                                                       : credData.getDomain() +
                                                                                                         "\\" +
                                                                                                         username,
                                                           credData.getPassword(),
                                                           credData.getKey());

                saveCredentialsFile(credData.getDomain(), username, credentialBytes);

            } catch (Exception e) {
                throw new LoginException("Unable to decrypt credentials file: " + e.getMessage());
            }
        }
    }

    // Internal methods

    String getLoginFilePath() {
        try {
            readLock.lock();
            if (loginFile != null) {
                return loginFile;
            }
            return JaasConfigUtils.getLoginFileName();
        } finally {
            readLock.unlock();
        }
    }

    String getGroupFilePath() {
        try {
            readLock.lock();
            if (groupFile != null) {
                return groupFile;
            }
            return JaasConfigUtils.getGroupFileName();
        } finally {
            readLock.unlock();
        }
    }

    String getTenantFilePath() {
        try {
            readLock.lock();
            if (tenantFile != null) {
                return tenantFile;
            }
            return JaasConfigUtils.getTenantFileName();
        } finally {
            readLock.unlock();
        }
    }

    void setLoginFilePath(String loginFilePath) {
        try {
            writeLock.lock();
            this.loginFile = loginFilePath;
            this.loginFileCheckSum = null;
        } finally {
            writeLock.unlock();
        }
    }

    void setGroupFilePath(String groupFilePath) {
        try {
            writeLock.lock();
            this.groupFile = groupFilePath;
            this.groupFileCheckSum = null;
        } finally {
            writeLock.unlock();
        }
    }

    void setTenantFilePath(String tenantFilePath) {
        try {
            writeLock.lock();
            this.tenantFile = tenantFilePath;
            this.tenantFileCheckSum = null;
        } finally {
            writeLock.unlock();
        }
    }

    void setPrivateKeyPath(String privateKeyPath) throws LoginException {
        try {
            writeLock.lock();
            privateKey = Credentials.getPrivateKey(PASharedProperties.getAbsolutePath(privateKeyPath));
        } catch (KeyException e) {
            throw buildLoginException("Could not read private key from " + privateKeyPath, e);
        } finally {
            writeLock.unlock();
        }
    }

    void setPublicKeyPath(String publicKeyPath) throws LoginException {
        try {
            writeLock.lock();
            publicKey = Credentials.getPublicKey(PASharedProperties.getAbsolutePath(publicKeyPath));
        } catch (KeyException e) {
            throw buildLoginException("Could not read private key from " + publicKeyPath, e);
        } finally {
            writeLock.unlock();
        }
    }

    private List<OutputUserInfo> listUsersInternal() {
        return internalUserInfos.values()
                                .stream()
                                .map(ui -> new OutputUserInfo(ui.getLogin(), ui.getTenant(), ui.getGroups()))
                                .collect(Collectors.toList());
    }

    private boolean sameCredentialsBytes(byte[] credentialBytes, File credentialsfile) {
        try (InputStream inputStream = new FileInputStream(credentialsfile)) {
            return Arrays.equals(credentialBytes, IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void updateUsersTenant() {
        internalUserInfos.values().stream().forEach(ui -> ui.setTenant(findTenant(ui.getGroups())));
    }

    private LoginException buildLoginException(String message, Exception e) {
        return new LoginException(message + " : " + e.getClass().getCanonicalName() + " " + e.getMessage());
    }

    private String computeChecksum(String filePath) throws LoginException {
        String checksum;
        try {
            byte[] data = Files.readAllBytes(Paths.get(filePath));
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            checksum = new BigInteger(1, hash).toString(16);
            return checksum;
        } catch (Exception e) {
            logger.error("Cannot compute checksum of file " + filePath, e);
            throw buildLoginException("Cannot compute checksum of file " + filePath, e);
        }
    }

    private void internalRefresh() throws LoginException {
        try {
            loginFile = getLoginFilePath();
            groupFile = getGroupFilePath();
            tenantFile = getTenantFilePath();
            publicKey = Credentials.getPublicKey(PASharedProperties.getAbsolutePath(PASharedProperties.AUTH_PUBKEY_PATH.getValueAsString()));
            privateKey = Credentials.getPrivateKey(PASharedProperties.getAbsolutePath(PASharedProperties.AUTH_PRIVKEY_PATH.getValueAsString()));
            tenants = readTenantsFromFile();
            internalUserInfos = fillInternalUsersInfo(readLoginFile(), readGroupsFromFile(), tenants);
            tenantsChanged = false;
            usersDeleted = false;
            loginFileCheckSum = computeChecksum(loginFile);
            groupFileCheckSum = computeChecksum(groupFile);
            tenantFileCheckSum = computeChecksum(tenantFile);
        } catch (Exception e) {
            logger.error("Exception while refreshing users", e);
            throw buildLoginException("Exception while refreshing users", e);
        }
    }

    private Multimap<String, String> tenantsToMMap() {
        Multimap<String, String> answer = TreeMultimap.create();
        tenants.entrySet().forEach(e -> answer.put(e.getValue(), e.getKey()));
        return answer;
    }

    private String findTenant(Set<String> groups) {
        String tenant = null;
        for (String group : groups) {
            if (tenants.containsKey(group)) {
                tenant = tenants.get(group);
                break;
            }
        }
        return tenant;
    }

    private String encodePassword(String clearPassword) throws KeyException {
        // different password
        String encodedPassword;
        if (PASharedProperties.LEGACY_ENCRYPTION.getValueAsBoolean()) {
            encodedPassword = HybridEncryptionUtil.encryptStringToBase64(clearPassword,
                                                                         publicKey,
                                                                         FileLoginModule.ENCRYPTED_DATA_SEP);
        } else {
            encodedPassword = HybridEncryptionUtil.hashPassword(clearPassword);
        }
        return encodedPassword;
    }

    private boolean internalCheckPassword(String encryptedPassword, String password, PrivateKey privateKey)
            throws KeyException {
        if (PASharedProperties.LEGACY_ENCRYPTION.getValueAsBoolean()) {
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

    private Properties getUserProperties() {
        Properties answer = new Properties();
        for (String userName : internalUserInfos.keySet().stream().sorted().collect(Collectors.toList())) {
            answer.setProperty(userName, internalUserInfos.get(userName).getPassword());
        }
        return answer;
    }

    private Multimap<String, String> getUserGroups() {
        Multimap<String, String> answer = TreeMultimap.create();
        for (String userName : internalUserInfos.keySet().stream().sorted().collect(Collectors.toList())) {
            InternalUserInfo userInfo = internalUserInfos.get(userName);
            for (String group : userInfo.getGroups())
                answer.put(userName, group);
        }
        return answer;
    }

    private Map<String, String> getUserTenants() {
        return new TreeMap<>(tenants);
    }

    private Properties readLoginFile() throws LoginException {
        Properties props = new Properties();
        try (FileInputStream stream = new FileInputStream(loginFile)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            props.load(reader);
        } catch (Exception e) {
            logger.error("Error reading login file " + loginFile, e);
            throw buildLoginException("Error reading login file " + loginFile, e);
        }
        return props;
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
        } catch (Exception e) {
            logger.error("Error reading group file " + groupFile, e);
            throw buildLoginException("Error reading group file " + groupFile, e);
        }
        return groupsMap;
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
        } catch (Exception e) {
            logger.error("Error reading tenant file " + tenantFile, e);
            throw buildLoginException("Error reading tenant file " + tenantFile, e);
        }
        return groupsToTenant;
    }

    private Map<String, InternalUserInfo> fillInternalUsersInfo(Properties users, Multimap<String, String> groups,
            Map<String, String> tenants) {
        Map<String, InternalUserInfo> answer = new TreeMap<>();
        for (String userName : users.stringPropertyNames()) {
            String userPassword = users.getProperty(userName);
            Collection<String> userGroups = groups.get(userName);
            String userTenant = null;
            for (String group : userGroups) {
                if (tenants.containsKey(group)) {
                    userTenant = tenants.get(group);
                    break;
                }
            }
            InternalUserInfo userInfo = new InternalUserInfo(userName, userPassword, userTenant, userGroups, false);
            answer.put(userInfo.getLogin(), userInfo);
        }
        return answer;
    }

    void storeLoginFile(Properties props) throws LoginException {

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
            logger.error("Error writing login file " + loginFile, e);
            throw buildLoginException("Error writing login file " + loginFile, e);
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
            logger.error("Error writing group file " + groupFile, e);
            throw buildLoginException("Error writing group file " + groupFile, e);
        } finally {
            authenticationLockFile.delete();
        }
    }

    private void storeTenants(Map<String, String> tenants) throws LoginException {
        try {
            while (authenticationLockFile.exists()) {
                Thread.sleep(50);
            }
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tenantFile)))) {
                for (Map.Entry<String, String> tenantEntry : getUserTenants().entrySet()) {
                    writer.println(tenantEntry.getKey() + ":" + tenantEntry.getValue());
                }
            }
        } catch (Exception e) {
            logger.error("Error writing tenant file " + tenantFile, e);
            throw buildLoginException("Error writing tenant file " + tenantFile, e);
        } finally {
            authenticationLockFile.delete();
        }
    }
}
