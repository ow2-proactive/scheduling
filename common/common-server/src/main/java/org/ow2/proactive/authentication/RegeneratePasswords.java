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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.objectweb.proactive.utils.SecurityManagerConfigurator;
import org.ow2.proactive.authentication.crypto.CreateCredentials;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.core.properties.PASharedProperties;
import org.ow2.proactive.utils.Tools;

import com.google.common.base.Strings;


/**
 * This command can:
 *   - Regenerate system accounts (rm, scheduler, watcher) using random passwords. This will also update the associated credentials files.
 *   - Encrypt passwords in login.cfg using a source login file containing plain text passwords.
 *   If an associated credential file exist with the account name, it will also be updated.
 *
 *  If a login file is not provided, only system accounts will be regenerated. It is also possible to provide a login file and not regenerate system accounts.
 *
 * @author The ProActive Team
 * @see Credentials
 */
public class RegeneratePasswords {

    private static final String newline = System.lineSeparator();

    public static final String DEFAULT_AUTH_FOLDER = "config/authentication/";

    public static final String DEFAULT_WEB_SETTINGS_PATH = "config/web/settings.ini";

    public static final String DEFAULT_NOTIFICATION_SETTINGS_PATH = "dist/war/notification-service/WEB-INF/classes/application.properties";

    public static final String SCHEDULER_CACHE_PASSWORD_CONF = "scheduler.cache.password";

    public static final String SCHEDULER_CACHE_CREDENTIAL_CONF = "scheduler.cache.credential";

    public static final String RM_CACHE_PASSWORD_CONF = "rm.cache.password";

    public static final String RM_CACHE_CREDENTIAL_CONF = "rm.cache.credential";

    public static final String NOTIFICATION_SERVICE_LISTENERS_PWD = "listeners.pwd";

    public static final String HELP_OPTION = "h";

    public static final String HELP_OPTION_NAME = "help";

    public static final String DEBUG_OPTION = "d";

    public static final String DEBUG_OPTION_NAME = "debug";

    public static final String PRIVATE_KEYFILE_OPTION = "prk";

    public static final String PRIVATE_KEYFILE_OPTION_NAME = "privatekeyfile";

    public static final String PUBLIC_KEYFILE_OPTION = "puk";

    public static final String PUBLIC_KEYFILE_OPTION_NAME = "publickeyfile";

    public static final String LOGINFILE_OPTION = "lf";

    public static final String LOGINFILE_OPTION_NAME = "loginfile";

    public static final String NO_SYSTEM_ACCOUNTS_REGENERATION = "ns";

    public static final String NO_SYSTEM_ACCOUNTS_REGENERATION_OPTION_NAME = "nosystem";

    public static final String CONVERT_LEGACY_TO_NEW_OPTION = "ltn";

    public static final String CONVERT_LEGACY_TO_NEW_OPTION_NAME = "legacytonew";

    public static final String SOURCE_LOGINFILE_OPTION = "slf";

    public static final String SOURCE_LOGINFILE_OPTION_NAME = "sourceloginfile";

    public static final String[] SYSTEM_ACCOUNTS = { "rm", "scheduler", "watcher" };

    public static final String IS_EMPTY_SKIPPING = " is empty, skipping.";

    public static final String PROVIDED_USERNAME = "Provided username ";

    public static final String USER_HEADER = "USER ";

    public static final String UPDATING_THIS_USER_INFORMATION = ", updating this user information.";

    public static final String DOES_NOT_EXIST_IN_LOGIN_FILE = " does not exist in login file : ";

    public static final String ENCRYPTED_DATA_SEP = " ";

    private static boolean isDebug = false;

    private static boolean convertLegacyToNew = false;

    private static boolean regenerateSystemAccounts = false;

    private static PublicKey pubKey = null;

    private static PrivateKey privKey = null;

    private static String loginFilePath = null;

    /**
     * Entry point
     *
     * @param args arguments, try '-h' for help
     * @see Credentials
     */
    public static void main(String[] args) {

        try {

            regeneratePasswords(args);

        } catch (UpdatePasswordsException e) {
            System.err.println("ERROR : " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println(e.getCause().getMessage());
            }
            if (e.getAdditionalInfo() != null) {
                System.out.println(e.getAdditionalInfo());
            }
            System.exit(1);
        }

        System.exit(0);
    }

    public static void regeneratePasswords(String... args) throws UpdatePasswordsException {
        SecurityManagerConfigurator.configureSecurityManager(CreateCredentials.class.getResource("/all-permissions.security.policy")
                                                                                    .toString());

        Console console = System.console();
        /**
         * default values
         */
        String pubKeyPath = getPublicKeyFilePath();
        String privKeyPath = getPrivateKeyFilePath();
        loginFilePath = getLoginFilePath();
        String sourceLoginFilePath = null;

        Options options = new Options();

        CommandLine cmd = getCommandLine(args, loginFilePath, options);

        if (cmd.hasOption(HELP_OPTION_NAME) || cmd.getOptions().length == 0) {
            displayHelp(options);
            return;
        }

        if (cmd.hasOption(DEBUG_OPTION_NAME)) {
            isDebug = true;
        }

        if (!cmd.hasOption(SOURCE_LOGINFILE_OPTION_NAME) &&
            cmd.hasOption(NO_SYSTEM_ACCOUNTS_REGENERATION_OPTION_NAME) &&
            !cmd.hasOption(CONVERT_LEGACY_TO_NEW_OPTION_NAME)) {
            exitWithErrorMessage("Option " + NO_SYSTEM_ACCOUNTS_REGENERATION_OPTION_NAME + " is used, " +
                                 CONVERT_LEGACY_TO_NEW_OPTION_NAME + " is not used and " +
                                 SOURCE_LOGINFILE_OPTION_NAME + " is not provided. Nothing to do.", null, null);
        }

        if (cmd.hasOption(LOGINFILE_OPTION_NAME)) {
            loginFilePath = cmd.getOptionValue(LOGINFILE_OPTION_NAME);
        }
        if (cmd.hasOption(PUBLIC_KEYFILE_OPTION_NAME)) {
            pubKeyPath = cmd.getOptionValue(PUBLIC_KEYFILE_OPTION_NAME);
        }
        if (cmd.hasOption(PRIVATE_KEYFILE_OPTION_NAME)) {
            privKeyPath = cmd.getOptionValue(PRIVATE_KEYFILE_OPTION_NAME);
        }

        if (cmd.hasOption(SOURCE_LOGINFILE_OPTION_NAME)) {
            sourceLoginFilePath = cmd.getOptionValue(SOURCE_LOGINFILE_OPTION_NAME);
        }

        try {
            pubKey = Credentials.getPublicKey(pubKeyPath);
        } catch (KeyException e) {
            exitWithErrorMessage("Could not retrieve public key from '" + pubKeyPath, null, e);
        }

        try {
            privKey = Credentials.getPrivateKey(privKeyPath);
        } catch (KeyException e) {
            exitWithErrorMessage("Could not retrieve private key from '" + privKeyPath, null, e);
        }

        regenerateSystemAccounts = !cmd.hasOption(NO_SYSTEM_ACCOUNTS_REGENERATION_OPTION_NAME);

        convertLegacyToNew = cmd.hasOption(CONVERT_LEGACY_TO_NEW_OPTION_NAME);

        updateAccounts(sourceLoginFilePath);
    }

    /**
     * Update the accounts in the login and config files
     *
     * @throws UpdatePasswordsException
     */
    private static void updateAccounts(final String sourceLoginFile) throws UpdatePasswordsException {
        Properties destinationLoginProps = new Properties();
        try {
            try (InputStreamReader stream = new InputStreamReader(new FileInputStream(loginFilePath))) {
                destinationLoginProps.load(stream);
            } catch (Exception e) {
                exitWithErrorMessage("could not read login file : " + loginFilePath, null, e);
            }

            Properties sourceLoginProps = new Properties();

            if (convertLegacyToNew) {
                for (String key : destinationLoginProps.stringPropertyNames()) {
                    try {
                        String passwordWithLegacyEncryption = destinationLoginProps.getProperty(key);
                        String oldPassword = HybridEncryptionUtil.decryptBase64String(passwordWithLegacyEncryption,
                                                                                      privKey,
                                                                                      ENCRYPTED_DATA_SEP);
                        //log("Converting " + key + ":" + oldPassword + " to new format");
                        sourceLoginProps.setProperty(key, oldPassword);
                    } catch (Exception e) {
                        System.err.println("ERROR: Cannot decrypt password of user " + key + " : " + e.getMessage());
                    }
                }
            }

            if (regenerateSystemAccounts) {
                createRandomPasswordsForSystemAccounts(sourceLoginProps);
            }

            if (sourceLoginFile != null) {
                try (InputStreamReader stream = new InputStreamReader(new FileInputStream(sourceLoginFile))) {
                    sourceLoginProps.load(stream);
                } catch (Exception e) {
                    exitWithErrorMessage("could not read source login file : " + sourceLoginFile, null, e);
                }
            }

            Collection<String> sourceLoginNames = sourceLoginProps.stringPropertyNames();

            if (sourceLoginNames.size() == 0) {
                exitWithErrorMessage("no users to update found, nothing to do.", null, null);
            }

            for (String user : sourceLoginNames) {
                UserInfo sourceUserInfo = new UserInfo();
                sourceUserInfo.setLogin(user);
                sourceUserInfo.setPassword((String) sourceLoginProps.get(user));

                updateAccountPassword(sourceUserInfo, destinationLoginProps);
            }

            storeLoginFile(destinationLoginProps);

            if (regenerateSystemAccounts) {
                updateConfigurationFilesForWatcherAccount(sourceLoginProps.getProperty("watcher"));
            }

        } catch (Throwable t) {
            exitWithErrorMessage("Unexpected error", null, t);
        }
    }

    private static String generateCommonLangPassword() {
        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);
        String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
        String totalChars = RandomStringUtils.randomAlphanumeric(2);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                                               .concat(numbers)
                                               .concat(specialChar)
                                               .concat(totalChars);
        List<Character> pwdChars = combinedChars.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        String password = pwdChars.stream()
                                  .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                                  .toString();
        return password;
    }

    private static void createRandomPasswordsForSystemAccounts(Properties sourceLoginProps) {
        for (String account : SYSTEM_ACCOUNTS) {
            sourceLoginProps.put(account, generateCommonLangPassword());
        }
    }

    private static void updateAccountPassword(UserInfo userInfo, Properties props)
            throws UpdatePasswordsException, KeyException {
        if (!userInfo.isLoginSet()) {
            warnWithMessage(PROVIDED_USERNAME + IS_EMPTY_SKIPPING);
            return;
        }
        if (!props.containsKey(userInfo.getLogin())) {
            String userDoesNotExistInLoginFileMessage = USER_HEADER + userInfo.getLogin() +
                                                        DOES_NOT_EXIST_IN_LOGIN_FILE + loginFilePath;
            if (!userInfo.isPasswordSet()) {
                warnWithMessage(userDoesNotExistInLoginFileMessage + ", skipping.");
                return;
            } else {
                warnWithMessage(userDoesNotExistInLoginFileMessage + ", skipping.");
            }
        }
        if (userInfo.isPasswordSet()) {
            updateUserPassword(userInfo.getLogin(), userInfo.getPassword(), props);
            System.out.println("Changed password for user " + userInfo.getLogin());
            updateUserCredentials(pubKey, userInfo.getLogin(), userInfo.getPassword(), props);
            if ("admin".equals(userInfo.getLogin())) {
                updateUserCredentials(pubKey, userInfo.getLogin() + "-user", userInfo.getPassword(), props);
            }
        }
    }

    private static void log(String line) {
        System.out.println(line);
    }

    private static String answerAndLog(String line) {
        if (isDebug) {
            log("DEBUG:  -> Set " + line);
        }
        return line;
    }

    private static void updateConfigurationFilesForWatcherAccount(String password) throws IOException {
        updateConfigurationFileForWatcherAccount(password, DEFAULT_WEB_SETTINGS_PATH);
        updateConfigurationFileForWatcherAccount(password, DEFAULT_NOTIFICATION_SETTINGS_PATH);
    }

    private static void updateConfigurationFileForWatcherAccount(String password, String configurationFilePath)
            throws FileNotFoundException, IOException {
        List<String> inputLines;
        File configurationFile = new File(getSchedulerFile(configurationFilePath));
        try (BufferedReader reader = new BufferedReader(new FileReader(configurationFile))) {
            inputLines = IOUtils.readLines(reader);
        }
        List<String> outputLines = new ArrayList<>(inputLines.size());
        for (String line : inputLines) {
            outputLines.add(analyseLineInLdapConfiguration(line, password));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configurationFile))) {
            IOUtils.writeLines(outputLines, null, writer);
        }

    }

    private static String analyseLineInLdapConfiguration(String line, String password) {
        line = line.trim();
        if (line.contains(SCHEDULER_CACHE_PASSWORD_CONF)) {
            return answerAndLog(SCHEDULER_CACHE_PASSWORD_CONF + "=");
        } else if (line.contains(SCHEDULER_CACHE_CREDENTIAL_CONF)) {
            return answerAndLog(SCHEDULER_CACHE_CREDENTIAL_CONF + "=config/authentication/watcher.cred");
        } else if (line.contains(RM_CACHE_PASSWORD_CONF)) {
            return answerAndLog(RM_CACHE_PASSWORD_CONF + "=");
        } else if (line.contains(RM_CACHE_CREDENTIAL_CONF)) {
            return answerAndLog(RM_CACHE_CREDENTIAL_CONF + "=config/authentication/watcher.cred");
        } else if (line.contains(NOTIFICATION_SERVICE_LISTENERS_PWD)) {
            return answerAndLog(NOTIFICATION_SERVICE_LISTENERS_PWD + "=" + password);
        } else {
            return line;
        }
    }

    /**
     * Build the command line options and parse
     */
    private static CommandLine getCommandLine(String[] args, String loginFilePath, Options options)
            throws UpdatePasswordsException {
        Option opt = new Option(HELP_OPTION, HELP_OPTION_NAME, false, "Display this help");
        opt.setRequired(false);
        options.addOption(opt);
        OptionGroup optionGroup = new OptionGroup();
        optionGroup.setRequired(false);

        opt = new Option(DEBUG_OPTION, DEBUG_OPTION_NAME, false, "Debug mode (prints modified files and properties)");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(NO_SYSTEM_ACCOUNTS_REGENERATION,
                         NO_SYSTEM_ACCOUNTS_REGENERATION_OPTION_NAME,
                         true,
                         "Do not regenerate system accounts");
        opt.setArgs(0);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(CONVERT_LEGACY_TO_NEW_OPTION,
                         CONVERT_LEGACY_TO_NEW_OPTION_NAME,
                         true,
                         "Automatically convert stored passwords in the legacy format to the new hash format. Note that this operation not reversible.");
        opt.setArgs(0);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(PUBLIC_KEYFILE_OPTION,
                         PUBLIC_KEYFILE_OPTION_NAME,
                         true,
                         "Public key path on the local filesystem [default:" + getPublicKeyFilePath() + "]");
        opt.setArgName(PUBLIC_KEYFILE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(PRIVATE_KEYFILE_OPTION,
                         PRIVATE_KEYFILE_OPTION_NAME,
                         true,
                         "Private key path on the local filesystem [default:" + getPrivateKeyFilePath() + "]");
        opt.setArgName(PRIVATE_KEYFILE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(LOGINFILE_OPTION,
                         LOGINFILE_OPTION_NAME,
                         true,
                         "Path to the login file in use [default:" + loginFilePath + "]");
        opt.setArgName(LOGINFILE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(SOURCE_LOGINFILE_OPTION,
                         SOURCE_LOGINFILE_OPTION_NAME,
                         true,
                         "Path to a source login file, used to update passwords of non-system accounts. The source login file must contain clear text passwords in the format username:password");
        opt.setArgName(SOURCE_LOGINFILE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            exitWithErrorMessage(newline + e.getMessage() + newline, "type -h or --help to display help screen", null);
        }
        return cmd;
    }

    private static void updateUserPassword(String login, String password, Properties props) {
        String encodedPassword;
        encodedPassword = HybridEncryptionUtil.hashPassword(password);
        props.put(login, encodedPassword);
    }

    private static void updateUserCredentials(PublicKey pubKey, String login, String password, Properties props)
            throws KeyException {
        String credentialFilePath = getSchedulerFile(DEFAULT_AUTH_FOLDER + login + ".cred");
        File credentialFile = new File(credentialFilePath);
        if (credentialFile.exists() && credentialFile.canWrite()) {
            CredData credData = new CredData(CredData.parseLogin(login), CredData.parseDomain(login), password);
            Credentials cred = Credentials.createCredentials(credData, pubKey, "RSA/ECB/PKCS1Padding");
            cred.writeToDisk(credentialFile.getAbsolutePath());
            System.out.println("Updated credentials file in " + credentialFilePath);
        }
    }

    /**
     * Stores the logins into login.cfg
     */
    private static void storeLoginFile(Properties props) throws LoginException {
        UsersServiceImpl usersService = UsersServiceImpl.getInstance();
        usersService.setLoginFilePath(loginFilePath);
        usersService.storeLoginFile(props);
        System.out.println("Stored login file in " + loginFilePath);
    }

    private static void exitWithErrorMessage(String errorMessage, String infoMessage, Throwable e)
            throws UpdatePasswordsException {
        throw new UpdatePasswordsException(errorMessage, e, infoMessage);
    }

    private static void warnWithMessage(String warnMessage) {
        System.err.println("WARN : " + warnMessage);
    }

    private static String getLoginFilePath() {
        return getSchedulerFile(PASharedProperties.LOGIN_FILENAME.getValueAsString());
    }

    private static String getPublicKeyFilePath() {
        return getSchedulerFile(PASharedProperties.AUTH_PUBKEY_PATH.getValueAsString());
    }

    private static String getPrivateKeyFilePath() {
        return getSchedulerFile(PASharedProperties.AUTH_PRIVKEY_PATH.getValueAsString());
    }

    private static String getSchedulerFile(String path) {
        String schedulerFile = path;
        if (!(new File(schedulerFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix Scheduler_Home constant
            schedulerFile = PASharedProperties.SHARED_HOME.getValueAsString() + File.separator + path;
        }
        return schedulerFile;
    }

    private static void displayHelp(Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(135);
        hf.printHelp("regenerate-passwords" + Tools.shellExtension(),
                     "Regenerate passwords stored in config/authentication/login.cfg.",
                     options,
                     "",
                     true);
    }

    static class UserInfo {
        private String login;

        private String password;

        public UserInfo() {
        }

        public boolean isLoginSet() {
            return !Strings.isNullOrEmpty(login);
        }

        public boolean isPasswordSet() {
            return !Strings.isNullOrEmpty(password);
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
    }

    static class UpdatePasswordsException extends Exception {
        public String getAdditionalInfo() {
            return additionalInfo;
        }

        private String additionalInfo = null;

        public UpdatePasswordsException(String message) {
            super(message);
        }

        public UpdatePasswordsException(String message, Throwable cause) {
            super(message, cause);
        }

        public UpdatePasswordsException(String message, Throwable cause, String additionalInfo) {
            super(message, cause);
            this.additionalInfo = additionalInfo;
        }
    }

}
