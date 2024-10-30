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
package org.ow2.proactive.scheduler.authentication;

import static org.ow2.proactive.authentication.LDAPProperties.*;
import static org.ow2.proactive.core.properties.PropertyDecrypter.ENCRYPTION_PREFIX;

import java.io.*;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ow2.proactive.authentication.NoCallbackHandler;
import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.core.properties.PropertyDecrypter;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive.web.WebProperties;

import com.google.common.collect.ImmutableMap;


public class ChangeLDAPConfiguration {

    private static final String newline = System.lineSeparator();

    public static final String HELP_OPTION = "h";

    public static final String HELP_OPTION_NAME = "help";

    public static final String DEBUG_OPTION = "d";

    public static final String DEBUG_OPTION_NAME = "debug";

    public static final String LDAP_SERVER_URLS_OPTION = "u";

    public static final String LDAP_SERVER_URLS_OPTION_NAME = "url";

    public static final String LDAP_SERVER_ACTIVE_DIRECTORY_OPTION = "a";

    public static final String LDAP_SERVER_ACTIVE_DIRECTORY_OPTION_NAME = "active-directory";

    public static final String LDAP_START_TLS_ENABLED_OPTION = "s";

    public static final String LDAP_START_TLS_ENABLED_OPTION_NAME = "start.tls";

    public static final String LDAP_START_TLS_DISABLE_CHECK_OPTION_NAME = "start.tls.disable.check";

    public static final String LDAP_USER_SUBTREE_OPTION_NAME = "user.subtree";

    public static final String LDAP_GROUP_SUBTREE_OPTION_NAME = "group.subtree";

    public static final String LDAP_USER_FILTER_OPTION_NAME = "user.filter";

    public static final String LDAP_GROUP_FILTER_OPTION_NAME = "group.filter";

    public static final String LDAP_GROUP_FILTER_USE_UID_OPTION_NAME = "group.filter.use.uid";

    public static final String LDAP_GROUP_NAME_ATTR_OPTION_NAME = "group.attr";

    public static final String LDAP_TENANT_ATTRIBUTE_OPTION_NAME = "tenant.attribute";

    public static final String LDAP_AUTHENTICATION_DISABLE_OPTION = "a";

    public static final String LDAP_AUTHENTICATION_DISABLE_OPTION_NAME = "auth.disable";

    public static final String LDAP_AUTHENTICATION_LOGIN_OPTION = "l";

    public static final String LDAP_AUTHENTICATION_LOGIN_OPTION_NAME = "login";

    public static final String LDAP_AUTHENTICATION_PASSWORD_OPTION = "p";

    public static final String LDAP_AUTHENTICATION_PASSWORD_OPTION_NAME = "password";

    public static final String LDAP_TEST_LOGIN_OPTION_NAME = "test.user";

    public static final String LDAP_TEST_PWD_OPTION_NAME = "test.pwd";

    public static final String LDAP_CONFIGURATION_FILE_OPTION = "o";

    public static final String LDAP_CONFIGURATION_FILE_OPTION_NAME = "output.file";

    public static final String LDAP_TRUSTSTORE_PATH_OPTION_NAME = "truststore.path";

    public static final String LDAP_TRUSTSTORE_PASSWORD_OPTION_NAME = "truststore.pwd";

    public static final String DEFAULT_LDAP_CONFIGURATION_PATH = "config/authentication/ldap.cfg";

    public static final String DEFAULT_TRUST_STORE_PATH = "config/authentication/truststore";

    public static final String JAAS_CONFIG_PATH = "config/authentication/jaas.config";

    public static final String RM_SETTINGS_INI = "config/rm/settings.ini";

    public static final String SCHEDULER_SETTINGS_INI = "config/scheduler/settings.ini";

    private static final String LDAP_KEY = "LDAP";

    private static final String LDAP_WITH_USERID_KEY = "LDAPwithUID";

    private static final String AD_KEY = "AD";

    public static final String DEFAULT_TRUSTSTORE_PASSWORD = "activeeon";

    private static boolean isDebug = false;

    private static List<String> ldapServerUrls = null;

    private static String ldapServerType = LDAP_KEY;

    private static boolean startTlsEnabled = false;

    private static boolean startTlsDisableCheck = false;

    private static boolean authenticationDisable = false;

    private static String login;

    private static String password;

    private static String userSubtree = null;

    private static String groupSubtree = null;

    private static String userFilter = null;

    private static String groupFilter = null;

    private static boolean groupFilterUseUID = false;

    private static String groupNameAttribute = "cn";

    private static String tenantAttribute = null;

    private static String ldapConfigurationPath = null;

    private static File ldapConfigurationFile = null;

    private static File backupConfigurationFile = null;

    private static String trustStorePath = DEFAULT_TRUST_STORE_PATH;

    private static String trustStorePassword = DEFAULT_TRUSTSTORE_PASSWORD;

    private static String testUserId = null;

    private static String testUserPwd = null;

    private static boolean newCertificateInstalled = false;

    private static Map<String, String> defaultUserFilters = ImmutableMap.of(LDAP_KEY,
                                                                            "(&(objectclass=inetOrgPerson)(uid=%s))",
                                                                            AD_KEY,
                                                                            "(&(objectclass=user)(sAMAccountName=%s))");

    private static Map<String, String> defaultGroupFilters = ImmutableMap.of(LDAP_KEY,
                                                                             "(&(objectclass=groupOfUniqueNames)(uniqueMember=%s))",
                                                                             LDAP_WITH_USERID_KEY,
                                                                             "(&(objectclass=posixGroup)(memberUID=%s))",
                                                                             AD_KEY,
                                                                             "(&(objectclass=group)(member:1.2.840.113556.1.4.1941:=%s))");

    /**
     * Entry point
     *
     * @param args arguments, try '-h' for help
     * @see WebProperties
     */
    public static void main(String[] args) {

        try {
            if (readCommandArguments(args)) {
                checkTLSOrLDAPSCertificate();
                backupConfigurationFile();
                try {
                    changeLDAPConfiguration(ldapConfigurationFile);
                    validateLDAPConfiguration();
                    changeSettingsConfiguration(checkConfigurationFile(getSchedulerFile(RM_SETTINGS_INI)));
                    changeSettingsConfiguration(checkConfigurationFile(getSchedulerFile(SCHEDULER_SETTINGS_INI)));
                } catch (ChangeLDAPConfigurationException e) {
                    restoreConfigurationFile();
                    throw e;
                } catch (Exception e) {
                    restoreConfigurationFile();
                    throw e;
                }
            }
        } catch (ChangeLDAPConfigurationException e) {
            System.err.println("ERROR : " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println(e.getCause().getMessage());
            }
            if (e.getAdditionalInfo() != null) {
                log(e.getAdditionalInfo());
            }
            System.exit(1);
        } catch (Exception e) {
            System.err.println("ERROR : " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println(e.getCause().getMessage());
            }
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

    public static boolean readCommandArguments(String... args) throws ChangeLDAPConfigurationException {
        Options options = new Options();
        CommandLine cmd = getCommandLine(args, options);

        if (cmd.hasOption(HELP_OPTION_NAME) || cmd.getOptions().length == 0) {
            displayHelp(options);
            return false;
        }

        if (cmd.hasOption(DEBUG_OPTION_NAME)) {
            isDebug = true;
        }

        if (cmd.hasOption(LDAP_SERVER_URLS_OPTION_NAME)) {
            String[] urls = cmd.getOptionValue(LDAP_SERVER_URLS_OPTION_NAME).split(",");
            ldapServerUrls = Arrays.stream(urls).collect(Collectors.toList());
        } else {
            exitWithErrorMessage("--" + LDAP_SERVER_URLS_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(LDAP_SERVER_ACTIVE_DIRECTORY_OPTION_NAME)) {
            ldapServerType = AD_KEY;
        }

        if (cmd.hasOption(LDAP_START_TLS_ENABLED_OPTION_NAME)) {
            startTlsEnabled = true;
        }

        if (cmd.hasOption(LDAP_START_TLS_DISABLE_CHECK_OPTION_NAME)) {
            startTlsDisableCheck = true;
        }

        if (cmd.hasOption(LDAP_AUTHENTICATION_DISABLE_OPTION)) {
            authenticationDisable = true;
        }

        if (cmd.hasOption(LDAP_AUTHENTICATION_LOGIN_OPTION_NAME)) {
            login = cmd.getOptionValue(LDAP_AUTHENTICATION_LOGIN_OPTION_NAME);
        } else if (!authenticationDisable) {
            exitWithErrorMessage("--" + LDAP_AUTHENTICATION_LOGIN_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(LDAP_AUTHENTICATION_PASSWORD_OPTION_NAME)) {
            password = cmd.getOptionValue(LDAP_AUTHENTICATION_PASSWORD_OPTION_NAME).trim();
            if (!password.startsWith(ENCRYPTION_PREFIX)) {
                password = PropertyDecrypter.encryptData(password);
            }
        } else if (!authenticationDisable) {
            exitWithErrorMessage("--" + LDAP_AUTHENTICATION_PASSWORD_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(LDAP_USER_SUBTREE_OPTION_NAME)) {
            userSubtree = cmd.getOptionValue(LDAP_USER_SUBTREE_OPTION_NAME);
        } else {
            exitWithErrorMessage("--" + LDAP_USER_SUBTREE_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(LDAP_GROUP_SUBTREE_OPTION_NAME)) {
            groupSubtree = cmd.getOptionValue(LDAP_GROUP_SUBTREE_OPTION_NAME);
        } else {
            groupSubtree = userSubtree;
        }

        if (cmd.hasOption(LDAP_USER_FILTER_OPTION_NAME)) {
            userFilter = cmd.getOptionValue(LDAP_USER_FILTER_OPTION_NAME);
        } else {
            userFilter = defaultUserFilters.get(ldapServerType);
        }

        if (cmd.hasOption(LDAP_GROUP_FILTER_USE_UID_OPTION_NAME)) {
            groupFilterUseUID = true;
        }

        if (cmd.hasOption(LDAP_GROUP_FILTER_OPTION_NAME)) {
            groupFilter = cmd.getOptionValue(LDAP_GROUP_FILTER_OPTION_NAME);
        } else {
            if (groupFilterUseUID && ldapServerType.equals(AD_KEY)) {
                exitWithErrorMessage("--" + LDAP_GROUP_FILTER_USE_UID_OPTION_NAME + " cannot be used when --" +
                                     LDAP_SERVER_ACTIVE_DIRECTORY_OPTION_NAME + " is set and --" +
                                     LDAP_GROUP_FILTER_OPTION_NAME + " is not provided", null, null);
            } else if (groupFilterUseUID) {
                groupFilter = defaultGroupFilters.get(LDAP_WITH_USERID_KEY);
            } else {
                groupFilter = defaultGroupFilters.get(ldapServerType);
            }
        }

        if (cmd.hasOption(LDAP_GROUP_NAME_ATTR_OPTION_NAME)) {
            groupNameAttribute = cmd.getOptionValue(LDAP_GROUP_NAME_ATTR_OPTION_NAME);
        }

        if (cmd.hasOption(LDAP_TENANT_ATTRIBUTE_OPTION_NAME)) {
            tenantAttribute = cmd.getOptionValue(LDAP_TENANT_ATTRIBUTE_OPTION_NAME);
        }

        if (cmd.hasOption(LDAP_TEST_LOGIN_OPTION_NAME)) {
            testUserId = cmd.getOptionValue(LDAP_TEST_LOGIN_OPTION_NAME);
        } else {
            exitWithErrorMessage("--" + LDAP_TEST_LOGIN_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(LDAP_TEST_PWD_OPTION_NAME)) {
            testUserPwd = cmd.getOptionValue(LDAP_TEST_PWD_OPTION_NAME).trim();
            if (!testUserPwd.startsWith(ENCRYPTION_PREFIX)) {
                testUserPwd = PropertyDecrypter.encryptData(testUserPwd);
            }
        } else {
            exitWithErrorMessage("--" + LDAP_TEST_PWD_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(LDAP_CONFIGURATION_FILE_OPTION_NAME)) {
            ldapConfigurationPath = cmd.getOptionValue(LDAP_CONFIGURATION_FILE_OPTION_NAME);
        } else {
            ldapConfigurationPath = DEFAULT_LDAP_CONFIGURATION_PATH;
        }

        if (cmd.hasOption(LDAP_TRUSTSTORE_PATH_OPTION_NAME)) {
            trustStorePath = cmd.getOptionValue(LDAP_TRUSTSTORE_PATH_OPTION_NAME);
        }

        if (cmd.hasOption(LDAP_TRUSTSTORE_PASSWORD_OPTION_NAME)) {
            trustStorePassword = cmd.getOptionValue(LDAP_TRUSTSTORE_PASSWORD_OPTION_NAME);
        }

        return true;
    }

    /**
     * Build the command line options and parse
     */
    private static CommandLine getCommandLine(String[] args, Options options) throws ChangeLDAPConfigurationException {
        Option opt = new Option(HELP_OPTION, HELP_OPTION_NAME, false, "Display this help");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(DEBUG_OPTION, DEBUG_OPTION_NAME, false, "Debug mode (prints modified files and properties)");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(LDAP_SERVER_URLS_OPTION,
                         LDAP_SERVER_URLS_OPTION_NAME,
                         false,
                         "(required) Url(s) of the LDAP server(s). Multiple servers can be configured using a comma-separated list.");
        opt.setRequired(false);
        opt.setArgName(LDAP_SERVER_URLS_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(LDAP_SERVER_ACTIVE_DIRECTORY_OPTION,
                         LDAP_SERVER_ACTIVE_DIRECTORY_OPTION_NAME,
                         false,
                         "Use this option when the LDAP server is an Active Directory.");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(LDAP_START_TLS_ENABLED_OPTION,
                         LDAP_START_TLS_ENABLED_OPTION_NAME,
                         false,
                         "Enable StartTLS mode.");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_START_TLS_DISABLE_CHECK_OPTION_NAME,
                         false,
                         "If StartTLS mode is enable, disable certificate verification check.");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_USER_SUBTREE_OPTION_NAME,
                         false,
                         "(required) Scope in the LDAP tree where users can be found.");
        opt.setRequired(false);
        opt.setArgName(LDAP_USER_SUBTREE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_GROUP_SUBTREE_OPTION_NAME,
                         false,
                         "Scope in the LDAP tree where groups can be found. If not set, the value of option " +
                                LDAP_USER_SUBTREE_OPTION_NAME + " will be used.");
        opt.setRequired(false);
        opt.setArgName(LDAP_GROUP_SUBTREE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_USER_FILTER_OPTION_NAME,
                         false,
                         "LDAP filter executed when searching for a LDAP user entry which corresponds to a given user identifier. The default user filter, depending on the LDAP server type is : " +
                                defaultUserFilters);
        opt.setRequired(false);
        opt.setArgName(LDAP_USER_FILTER_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_GROUP_FILTER_OPTION_NAME,
                         false,
                         "LDAP filter executed when searching for all groups associated with a given user. By default, the search takes as parameter the user LDAP entry. Option " +
                                LDAP_GROUP_FILTER_USE_UID_OPTION_NAME +
                                " can be enabled to give as parameter the user identifier instead. The default group filter, depending on the LDAP server type and " +
                                LDAP_GROUP_FILTER_USE_UID_OPTION_NAME + " option is  : " + defaultGroupFilters);
        opt.setRequired(false);
        opt.setArgName(LDAP_GROUP_FILTER_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_GROUP_FILTER_USE_UID_OPTION_NAME,
                         false,
                         "If enabled, the group filter will use as parameter the user uid instead of its distinguished name.");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_GROUP_NAME_ATTR_OPTION_NAME,
                         false,
                         "The attribute in the group entry that matches the jaas' group name. Default is \"cn\".");
        opt.setRequired(false);
        opt.setArgName(LDAP_GROUP_NAME_ATTR_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_TENANT_ATTRIBUTE_OPTION_NAME,
                         false,
                         "An LDAP attribute such as \"department\" or \"project\" can be used to group categories of users in an abstract organization called \"Tenant\".");
        opt.setRequired(false);
        opt.setArgName(LDAP_TENANT_ATTRIBUTE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(LDAP_AUTHENTICATION_DISABLE_OPTION,
                         LDAP_AUTHENTICATION_DISABLE_OPTION_NAME,
                         false,
                         "Use this option to disable authentication to the LDAP server (only when the server supports anonymous connections).");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(LDAP_AUTHENTICATION_LOGIN_OPTION,
                         LDAP_AUTHENTICATION_LOGIN_OPTION_NAME,
                         false,
                         "login name used to connect (bind) to the ldap server when executing queries. The login is usually a LDAP distinguished name (e.g. uid=janedoe,ou=People,dc=activeeon,dc=com). ");
        opt.setRequired(false);
        opt.setArgName(LDAP_AUTHENTICATION_LOGIN_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(LDAP_AUTHENTICATION_PASSWORD_OPTION,
                         LDAP_AUTHENTICATION_PASSWORD_OPTION_NAME,
                         false,
                         "Password associated with the login name. Password can be provided already encrypted using the 'encypt' command or in clear text." +
                                "The password will always be encrypted when stored in the ldap configuration file.");
        opt.setRequired(false);
        opt.setArgName(LDAP_AUTHENTICATION_PASSWORD_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_TEST_LOGIN_OPTION_NAME,
                         false,
                         "(Required) Identifier of the user that will be searched using configured filters. This parameter is required to validate the configuration and guarantee that the connection and filters are working properly.");
        opt.setRequired(false);
        opt.setArgName(LDAP_TEST_LOGIN_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_TEST_PWD_OPTION_NAME,
                         false,
                         "(Required) Password of the user that will be searched using configured filters. This parameter is required to validate the configuration and guarantee that the connection and filters are working properly.");
        opt.setRequired(false);
        opt.setArgName(LDAP_TEST_PWD_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(LDAP_CONFIGURATION_FILE_OPTION,
                         LDAP_CONFIGURATION_FILE_OPTION_NAME,
                         false,
                         "Ldap configuration file that will be modified. It will be created by copying the default configuration file if it does not exist.");
        opt.setRequired(false);
        opt.setArgName(LDAP_CONFIGURATION_FILE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_TRUSTSTORE_PATH_OPTION_NAME,
                         false,
                         "Path to the truststore that will be used to store LDAPS certificates. If the file does not exist it will be created. The default value is config/authentication/truststore");
        opt.setRequired(false);
        opt.setArgName(LDAP_TRUSTSTORE_PATH_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         LDAP_TRUSTSTORE_PASSWORD_OPTION_NAME,
                         false,
                         "Password used to encrypt the truststore that will be created if the LDAP server is using LDAPS protocol. If not provided the default password will be \"activeeon\".");
        opt.setRequired(false);
        opt.setArgName(LDAP_TRUSTSTORE_PASSWORD_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
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

    private static void exitWithErrorMessage(String errorMessage, String infoMessage, Throwable e)
            throws ChangeLDAPConfigurationException {
        throw new ChangeLDAPConfigurationException(errorMessage, e, infoMessage);
    }

    private static void displayHelp(Options options) {
        String header = newline + "Configure ProActive server for LDAP authentication." + newline;
        header += "ProActive can either use an Active Directory or a classical LDAP server for authentication." +
                  newline + newline;

        String footer = newline + "Examples: " + newline;
        footer += "# Configure Ldap authentication for a ldaps server and default filters. Debug output to see all modifications." +
                  newline;
        footer += "configure-ldap --url ldaps://ldap-server1.activeeon.com --user.subtree \"ou=users,dc=activeeon,dc=org\" --group.subtree \"ou=groups,dc=activeeon,dc=org\" --login \"cn=admin,dc=activeeon,dc=org\" --password \"my_bind_pwd\" --test.user user1 --test.pwd \"my_test_pwd\" --debug" +
                  newline;

        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(135);
        hf.printHelp("configure-ldap" + Tools.shellExtension(), header, options, footer, true);
    }

    private static File getSchedulerFile(String path) {
        if (new File(path).isAbsolute()) {
            return new File(path);
        } else {
            File pathName = new File(PASchedulerProperties.SCHEDULER_HOME.getValueAsString(), path);
            try {
                return new File(pathName.getCanonicalPath());
            } catch (IOException e) {
                return new File(pathName.getAbsolutePath());
            }
        }
    }

    // check that the provided configuration file can be modified
    private static File checkConfigurationFile(File configurationFile) throws ChangeLDAPConfigurationException {
        if (configurationFile != null && configurationFile.exists() && configurationFile.isFile()) {
            if (!configurationFile.canWrite()) {
                throw new ChangeLDAPConfigurationException("File " + configurationFile + " cannot be modified");
            }
        }
        return configurationFile;
    }

    private static void backupConfigurationFile() throws ChangeLDAPConfigurationException, IOException {
        ldapConfigurationFile = getSchedulerFile(ldapConfigurationPath);
        if (!ldapConfigurationFile.exists()) {
            log("Created configuration file " + ldapConfigurationFile + " using default configuration.");
            FileUtils.copyFile(getSchedulerFile(DEFAULT_LDAP_CONFIGURATION_PATH), ldapConfigurationFile);
        }
        checkConfigurationFile(ldapConfigurationFile);
        backupConfigurationFile = new File(ldapConfigurationFile.getParentFile(),
                                           ldapConfigurationFile.getName() + ".bak");
        if (backupConfigurationFile.exists()) {
            backupConfigurationFile.delete();
        }
        FileUtils.copyFile(ldapConfigurationFile, backupConfigurationFile);
        log("Backup configuration file " + ldapConfigurationFile + " to " + backupConfigurationFile);
    }

    private static void restoreConfigurationFile() throws IOException {
        log("Restore original configuration file " + ldapConfigurationFile + " due to error.");
        FileUtils.copyFile(backupConfigurationFile, ldapConfigurationFile);
    }

    private static void loadJaasConfiguration() {
        File jaasFile = getSchedulerFile(JAAS_CONFIG_PATH);
        if (jaasFile.exists() && !jaasFile.isDirectory()) {
            System.setProperty("java.security.auth.login.config", jaasFile.getAbsolutePath());
        } else {
            throw new RuntimeException("Could not find Jaas configuration at: " + jaasFile);
        }
    }

    private static void checkTLSOrLDAPSCertificate() throws Exception {
        if (startTlsEnabled || ldapServerUrls.get(0).startsWith("ldaps")) {
            File trustStoreFile = getSchedulerFile(trustStorePath);
            usn.net.ssl.util.InstallCert installer = new usn.net.ssl.util.InstallCert();
            try {
                installer.addTrustStore(new File(System.getProperty("java.home"), "lib/security/cacerts"),
                                        "changeit".toCharArray());
                if (trustStoreFile.exists()) {
                    installer.addTrustStore(trustStoreFile, trustStorePassword.toCharArray());
                }
                URI serverUri = new URI(ldapServerUrls.get(0));
                String serverScheme = serverUri.getScheme();
                String serverHost = serverUri.getHost();
                int serverPort = serverUri.getPort();
                if (serverPort == -1 && serverScheme.equals("ldaps")) {
                    serverPort = 636;
                } else {
                    serverPort = 389;
                }
                Set<X509Certificate> untrustedCerts = installer.getCerts(serverHost, serverPort);
                if (untrustedCerts.size() == 0) {
                    log("No untrusted certificate found for " + ldapServerUrls.get(0));
                } else {
                    log("Found untrusted certificate found for " + ldapServerUrls.get(0));
                    newCertificateInstalled = true;
                    if (!trustStoreFile.exists()) {
                        createTrustStore(trustStoreFile);
                    }
                    installer.addTrustStore(trustStoreFile, trustStorePassword.toCharArray());
                    installer.applyChanges(untrustedCerts, serverHost);
                }
            } finally {
                installer.close();
            }
        }
    }

    private static void createTrustStore(File trustStore)
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        char[] password = trustStorePassword.toCharArray();
        ks.load(null, password);

        // Store away the keystore.
        try (FileOutputStream fos = new FileOutputStream(trustStore)) {
            ks.store(fos, password);
        }
    }

    private static void changeLDAPConfiguration(File ldapConfiguration)
            throws ChangeLDAPConfigurationException, IOException {
        if (isDebug) {
            log("DEBUG: Checking " + ldapConfiguration);
        }
        checkConfigurationFile(ldapConfiguration);
        List<String> inputLines;
        try (BufferedReader reader = new BufferedReader(new FileReader(ldapConfiguration))) {
            inputLines = IOUtils.readLines(reader);
        }
        List<String> outputLines = new ArrayList<>(inputLines.size());
        for (String line : inputLines) {
            outputLines.add(analyseLineInLdapConfiguration(line));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ldapConfiguration))) {
            IOUtils.writeLines(outputLines, null, writer);
        }
    }

    private static String analyseLineInLdapConfiguration(String line) {
        line = line.trim();
        if (line.startsWith(LDAP_URL)) {
            return answerAndLog(LDAP_URL + "=" + ldapServerUrls.stream().collect(Collectors.joining(" ")));
        } else if (line.startsWith(LDAP_USERS_SUBTREE)) {
            return answerAndLog(LDAP_USERS_SUBTREE + "=" + userSubtree);
        } else if (line.contains(LDAP_GROUPS_SUBTREE)) {
            return answerAndLog(LDAP_GROUPS_SUBTREE + "=" + groupSubtree);
        } else if (line.startsWith(LDAP_USER_FILTER)) {
            return answerAndLog(LDAP_USER_FILTER + "=" + userFilter);
        } else if (line.startsWith(LDAP_GROUPSEARCH_USE_UID)) {
            return answerAndLog(LDAP_GROUPSEARCH_USE_UID + "=" + groupFilterUseUID);
        } else if (line.startsWith(LDAP_GROUP_FILTER)) {
            return answerAndLog(LDAP_GROUP_FILTER + "=" + groupFilter);
        } else if (line.startsWith(LDAP_GROUPNAME_ATTR)) {
            return answerAndLog(LDAP_GROUPNAME_ATTR + "=" + groupNameAttribute);
        } else if (tenantAttribute != null && line.contains(LDAP_TENANT_ATTR)) {
            return answerAndLog(LDAP_TENANT_ATTR + "=" + tenantAttribute);
        } else if (line.startsWith(LDAP_AUTHENTICATION_METHOD)) {
            return answerAndLog(LDAP_AUTHENTICATION_METHOD + "=" + (authenticationDisable ? "none" : "simple"));
        } else if (line.startsWith(LDAP_START_TLS + "=")) {
            return answerAndLog(LDAP_START_TLS + "=" + startTlsEnabled);
        } else if (line.startsWith(LDAP_START_TLS_ANY_CERTIFICATE + "=")) {
            return answerAndLog(LDAP_START_TLS_ANY_CERTIFICATE + "=" + startTlsDisableCheck);
        } else if (line.startsWith(LDAP_START_TLS_ANY_HOSTNAME + "=")) {
            return answerAndLog(LDAP_START_TLS_ANY_HOSTNAME + "=" + startTlsDisableCheck);
        } else if (line.startsWith(LDAP_BIND_LOGIN)) {
            if (!authenticationDisable) {
                return answerAndLog(LDAP_BIND_LOGIN + "=" + login);
            }
        } else if (line.startsWith(LDAP_BIND_PASSWD)) {
            if (!authenticationDisable) {
                return answerAndLog(LDAP_BIND_PASSWD + "=" + password);
            }
        } else if (line.startsWith(LDAP_TRUSTSTORE_PATH)) {
            if (newCertificateInstalled) {
                return answerAndLog(LDAP_TRUSTSTORE_PATH + "=" + trustStorePath);
            }
        } else if (line.startsWith(LDAP_TRUSTSTORE_PASSWD)) {
            if (newCertificateInstalled) {
                return answerAndLog(LDAP_TRUSTSTORE_PASSWD + "=" + trustStorePassword);
            }
        } else if (line.startsWith(LDAP_CONNECTION_POOLING)) {
            if (startTlsEnabled) {
                return answerAndLog(LDAP_CONNECTION_POOLING + "=" + false);
            } else {
                return answerAndLog(LDAP_CONNECTION_POOLING + "=" + true);
            }
        } else if (line.startsWith(FALLBACK_USER_AUTH)) {
            return answerAndLog(FALLBACK_USER_AUTH + "=" + true);
        } else if (line.startsWith(FALLBACK_GROUP_MEMBERSHIP)) {
            return answerAndLog(FALLBACK_GROUP_MEMBERSHIP + "=" + true);
        } else if (line.startsWith(FALLBACK_TENANT_MEMBERSHIP)) {
            return answerAndLog(FALLBACK_TENANT_MEMBERSHIP + "=" + true);
        }
        return line;
    }

    private static void changeSettingsConfiguration(File settingsConfiguration)
            throws ChangeLDAPConfigurationException, IOException {
        if (isDebug) {
            log("DEBUG: Checking " + settingsConfiguration);
        }
        checkConfigurationFile(settingsConfiguration);
        List<String> inputLines;
        try (BufferedReader reader = new BufferedReader(new FileReader(settingsConfiguration))) {
            inputLines = IOUtils.readLines(reader);
        }
        List<String> outputLines = new ArrayList<>(inputLines.size());
        for (String line : inputLines) {
            outputLines.add(analyseLineInSettings(line));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(settingsConfiguration))) {
            IOUtils.writeLines(outputLines, null, writer);
        }
    }

    private static String analyseLineInSettings(String line) {
        line = line.trim();
        if (line.startsWith(PAResourceManagerProperties.RM_LOGIN_METHOD.getKey())) {
            return answerAndLog(PAResourceManagerProperties.RM_LOGIN_METHOD.getKey() + "=RMLDAPLoginMethod");
        } else if (line.startsWith(PASchedulerProperties.SCHEDULER_LOGIN_METHOD.getKey())) {
            return answerAndLog(PASchedulerProperties.SCHEDULER_LOGIN_METHOD.getKey() + "=SchedulerLDAPLoginMethod");
        }
        return line;
    }

    private static void validateLDAPConfiguration() throws LoginException, ChangeLDAPConfigurationException {
        loadJaasConfiguration();
        Map<String, Object> params = new HashMap<>(4);
        //user name to check
        params.put("username", testUserId);
        //password to check
        params.put("pw", PropertyDecrypter.decryptData(testUserPwd));
        params.put("domain", null);
        LoginContext lc = new LoginContext("SchedulerLDAPLoginMethod", new NoCallbackHandler(params));
        lc.login();
        log("User " + testUserId + " logged successfully");
        Subject subject = lc.getSubject();
        Set<GroupNamePrincipal> groups = subject.getPrincipals(GroupNamePrincipal.class);
        if (groups == null || groups.isEmpty()) {
            throw new ChangeLDAPConfigurationException("Could not find any groups associated with user " + testUserId);
        }
        log("User " + testUserId + " associated with groups:");
        for (GroupNamePrincipal group : groups) {
            log(group.getName());
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

    static class ChangeLDAPConfigurationException extends Exception {
        public String getAdditionalInfo() {
            return additionalInfo;
        }

        private String additionalInfo = null;

        public ChangeLDAPConfigurationException(String message) {
            super(message);
        }

        public ChangeLDAPConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ChangeLDAPConfigurationException(String message, Throwable cause, String additionalInfo) {
            super(message, cause);
            this.additionalInfo = additionalInfo;
        }
    }

}
