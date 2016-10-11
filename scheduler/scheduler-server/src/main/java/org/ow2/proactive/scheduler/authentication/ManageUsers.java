/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.authentication;

import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.objectweb.proactive.utils.SecurityManagerConfigurator;
import org.ow2.proactive.authentication.FileLoginModule;
import org.ow2.proactive.authentication.crypto.CreateCredentials;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.Tools;

import java.io.*;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;


/**
 * Manage user accounts in login.cfg and group.cfg files
 *
 * @author The ProActive Team
 * @see org.ow2.proactive.authentication.crypto.Credentials
 */
public class ManageUsers {

    private static final String newline = System.lineSeparator();

    public static final String CREATE_OPTION = "C";
    public static final String CREATE_OPTION_NAME = "create";
    public static final String UPDATE_OPTION = "U";
    public static final String UPDATE_OPTION_NAME = "update";
    public static final String DELETE_OPTION = "D";
    public static final String DELETE_OPTION_NAME = "delete";

    public static final String HELP_OPTION = "h";
    public static final String HELP_OPTION_NAME = "help";
    public static final String LOGIN_OPTION = "l";
    public static final String LOGIN_OPTION_NAME = "login";
    public static final String PWD_OPTION = "p";
    public static final String PWD_OPTION_NAME = "password";
    public static final String GROUPS_OPTION = "g";
    public static final String GROUPS_OPTION_NAME = "groups";
    public static final String KEYFILE_OPTION = "kf";
    public static final String KEYFILE_OPTION_NAME = "keyfile";
    public static final String LOGINFILE_OPTION = "lf";
    public static final String LOGINFILE_OPTION_NAME = "loginfile";
    public static final String GROUPFILE_OPTION = "gf";
    public static final String GROUPFILE_OPTION_NAME = "groupfile";


    /**
     * Entry point
     *
     * @param args arguments, try '-h' for help
     * @throws IOException
     * @throws ParseException
     * @see org.ow2.proactive.authentication.crypto.Credentials
     */
    public static void main(String[] args) {

        try {

            manageUsers(args);

        } catch (ManageUsersException e) {
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

    public static void manageUsers(String[] args) throws ManageUsersException {
        SecurityManagerConfigurator.configureSecurityManager(CreateCredentials.class.getResource(
                "/all-permissions.security.policy").toString());

        Console console = System.console();
        /**
         * default values
         */
        boolean nonInteractive = true;
        String pubKeyPath = null;
        PublicKey pubKey = null;
        UserInfo userInfo = new UserInfo();
        String loginFilePath = getLoginFilePath();
        String groupFilePath = getGroupFilePath();
        Action action = null;

        Options options = new Options();

        CommandLine cmd = getCommandLine(args, loginFilePath, groupFilePath, options);

        if (cmd.hasOption(HELP_OPTION_NAME)) {
            displayHelp(options);
            return;
        }

        action = Action.getAction(cmd);

        if (cmd.hasOption(LOGIN_OPTION_NAME)) {
            userInfo.setLogin(cmd.getOptionValue(LOGIN_OPTION_NAME));
        }
        if (cmd.hasOption(PWD_OPTION_NAME)) {
            userInfo.setPassword(cmd.getOptionValue(PWD_OPTION_NAME));
        }
        if (cmd.hasOption(GROUPS_OPTION_NAME)) {
            String groupString = cmd.getOptionValue(GROUPS_OPTION_NAME);
            userInfo.setGroups(groupString.split(","));
        }

        if (cmd.hasOption(LOGINFILE_OPTION_NAME)) {
            loginFilePath = cmd.getOptionValue(LOGINFILE_OPTION_NAME);
        }
        if (cmd.hasOption(GROUPFILE_OPTION_NAME)) {
            groupFilePath = cmd.getOptionValue(GROUPFILE_OPTION_NAME);
        }
        if (cmd.hasOption(KEYFILE_OPTION_NAME)) {
            pubKeyPath = cmd.getOptionValue(KEYFILE_OPTION_NAME);
        }

        if (pubKeyPath == null) {
            pubKeyPath = getPublicKeyFilePath();
        }
        try {
            pubKey = Credentials.getPublicKey(pubKeyPath);
        } catch (KeyException e) {
            exitWithErrorMessage("Could not retrieve public key from '" + pubKeyPath, null, e);
        }

        switch (action) {
            case CREATE:
                nonInteractive = (userInfo.getLogin() != null && userInfo.getPassword() != null && userInfo.getGroups() != null);
                break;
            case UPDATE:
                nonInteractive = (userInfo.getLogin() != null && userInfo.getPassword() != null) || (userInfo.getLogin() != null && userInfo.getGroups() != null);
                break;
            case DELETE:
                nonInteractive = userInfo.getLogin() != null;
                break;
        }

        if (!nonInteractive) {
            askInteractively(console, userInfo, action);
        }

        updateAccounts(pubKey, userInfo, loginFilePath, groupFilePath, action);
    }

    /**
     * Ask the user for parameters missing on the command line
     */
    private static void askInteractively(Console console, UserInfo userInfo, Action action) {
        System.out.println(action);
        System.out.print("login: ");
        if (userInfo.getLogin() == null) {
            userInfo.setLogin(console.readLine());
        } else {
            System.out.println(userInfo.getLogin());
        }
        System.out.print("password: ");
        if ((action.isCreate() && userInfo.getPassword() == null) || (action.isUpdate() && userInfo.getPassword() == null && userInfo.getGroups() == null)) {
            userInfo.setPassword(new String(console.readPassword()));
        } else {
            System.out.println("*******");
        }

        if (action.isCreate() && userInfo.getGroups() == null) {
            System.out.print("groups for user " + userInfo.getLogin() + ":");
            String groupString = console.readLine();
            userInfo.setGroups(groupString.split(","));
        }
    }

    /**
     * Update the accounts in the login and config files
     *
     * @throws ManageUsersException
     */
    private static void updateAccounts(PublicKey pubKey, UserInfo userInfo, String loginFilePath, String groupFilePath, Action action) throws ManageUsersException {
        try {
            Properties props = new Properties();
            try {
                FileInputStream stream = new FileInputStream(new File(loginFilePath));
                props.load(stream);
                stream.close();
            } catch (Exception e) {
                exitWithErrorMessage("could not read login file : " + loginFilePath, null, e);
            }

            Multimap<String, String> groupsMap = loadGroups(groupFilePath);

            switch (action) {
                case CREATE:
                    if (props.containsKey(userInfo.getLogin())) {
                        warnWithMessage("USER already exists in login file : " + loginFilePath + ", updating this user information.");
                    }
                    updateUserPassword(pubKey, userInfo.getLogin(), userInfo.getPassword(), props);
                    System.out.println("Created user " + userInfo.getLogin() + " in " + loginFilePath);
                    updateUserGroups(userInfo.getLogin(), userInfo.getGroups(), groupsMap);
                    break;
                case UPDATE:
                    if (!props.containsKey(userInfo.getLogin())) {
                        if (userInfo.getPassword() != null && userInfo.getGroups() != null) {
                            warnWithMessage("USER does not exist in login file : " + loginFilePath + ", create this user.");
                        } else {
                            exitWithErrorMessage("USER does not exist in login file : " + loginFilePath + " and not enough information were provided to create a new user.", null, null);
                        }
                    }
                    if (userInfo.getPassword() != null) {
                        updateUserPassword(pubKey, userInfo.getLogin(), userInfo.getPassword(), props);
                        System.out.println("Changed password for user " + userInfo.getLogin() + " in " + loginFilePath);
                    }
                    if (userInfo.getGroups() != null) {
                        updateUserGroups(userInfo.getLogin(), userInfo.getGroups(), groupsMap);
                    }
                    break;
                case DELETE:
                    if (!props.containsKey(userInfo.getLogin())) {
                        warnWithMessage("USER does not exist in login file : " + loginFilePath);
                    }
                    props.remove(userInfo.getLogin());
                    groupsMap.removeAll(userInfo.getLogin());
                    System.out.println("Deleted user " + userInfo.getLogin() + " in " + loginFilePath);
                    break;
            }

            storeLoginFile(loginFilePath, props);

            storeGroups(groupFilePath, groupsMap);
        } catch (Throwable t) {
            exitWithErrorMessage("Unexpected error", null, t);
        }
    }

    /**
     * Build the command line options and parse
     */
    private static CommandLine getCommandLine(String[] args, String loginFilePath, String groupFilePath, Options options) throws ManageUsersException {
        Option opt = new Option(HELP_OPTION, HELP_OPTION_NAME, false, "Display this help");
        opt.setRequired(false);
        options.addOption(opt);
        OptionGroup optionGroup = new OptionGroup();
        optionGroup.setRequired(false);

        opt = new Option(CREATE_OPTION, CREATE_OPTION_NAME, true,
                "Action to create a user");
        opt.setArgName(CREATE_OPTION_NAME.toUpperCase());
        opt.setArgs(0);
        opt.setRequired(false);
        optionGroup.addOption(opt);

        opt = new Option(UPDATE_OPTION, UPDATE_OPTION_NAME, true,
                "Action to update an existing user. Updating a user means to change the user's password or group membership.");
        opt.setArgName(UPDATE_OPTION_NAME.toUpperCase());
        opt.setArgs(0);
        opt.setRequired(false);
        optionGroup.addOption(opt);

        opt = new Option(DELETE_OPTION, DELETE_OPTION_NAME, true,
                "Action to delete an existing user");
        opt.setArgName(DELETE_OPTION_NAME.toUpperCase());
        opt.setArgs(0);
        opt.setRequired(false);
        optionGroup.addOption(opt);
        options.addOptionGroup(optionGroup);

        opt = new Option(LOGIN_OPTION, LOGIN_OPTION_NAME, true,
                "Generate credentials for this specific user, will be asked interactively if not specified");
        opt.setArgName(LOGIN_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(PWD_OPTION, PWD_OPTION_NAME, true,
                "Password of the user, if the user is created or updated, will be asked interactively if not specified");
        opt.setArgName(PWD_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(GROUPS_OPTION, GROUPS_OPTION_NAME, true,
                "A comma-separated list of groups the user must be member of. Can be used when the user is created or updated");
        opt.setArgName(GROUPS_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        optionGroup.setRequired(false);
        opt = new Option(KEYFILE_OPTION, KEYFILE_OPTION_NAME, true, "Public key path on the local filesystem [default:" +
                getPublicKeyFilePath() + "]");
        opt.setArgName(KEYFILE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(LOGINFILE_OPTION, LOGINFILE_OPTION_NAME, true,
                "Path to the login file in use [default:" + loginFilePath + "]");
        opt.setArgName(LOGINFILE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(GROUPFILE_OPTION, GROUPFILE_OPTION_NAME, true,
                "Path to the group file in use [default:" + groupFilePath + "]");
        opt.setArgName(GROUPFILE_OPTION_NAME.toUpperCase());
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

    private static void updateUserGroups(String login, String[] groups, Multimap<String, String> groupsMap) {
        groupsMap.removeAll(login);
        for (String group : groups) {
            groupsMap.put(login, group);
            System.out.println("Added group " + group + " to user " + login);
        }
    }

    private static void updateUserPassword(PublicKey pubKey, String login, String password, Properties props) throws KeyException {
        String encodedPassword;
        encodedPassword = HybridEncryptionUtil.encryptStringToBase64(password, pubKey, FileLoginModule.ENCRYPTED_DATA_SEP);
        props.put(login, encodedPassword);
    }

    private static Multimap<String, String> loadGroups(String groupFilePath) throws ManageUsersException {
        try {
            Multimap<String, String> groupsMap = TreeMultimap.create();
            String line = null;
            BufferedReader reader = new BufferedReader(new FileReader(groupFilePath));
            try {
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] u2g = line.split(":");
                        groupsMap.put(u2g[0].trim(), u2g[1].trim());
                    }
                }
            } finally {
                reader.close();
            }
            return groupsMap;
        } catch (IOException e) {
            exitWithErrorMessage("could not read group file : " + groupFilePath, null, e);
        }
        return null;
    }

    /**
     * Stores the logins into login.cfg
     */
    private static void storeLoginFile(String loginFilePath, Properties props) throws IOException {
        try (FileOutputStream stream = new FileOutputStream(new File(loginFilePath))) {
            props.store(stream, null);
        }
        List<String> lines = null;

        try (FileInputStream reader = new FileInputStream(loginFilePath)) {
            lines = IOUtils.readLines(reader, Charsets.UTF_8);
        }

        TreeMap<String, String> sortedUsers = new TreeMap<>();
        for (String line : lines) {
            if (!(line.isEmpty() || line.startsWith("#"))) {
                String[] loginAndPwd = line.split("=", 2);
                sortedUsers.put(loginAndPwd[0], loginAndPwd[1]);
            }
        }
        List<String> modifiedLines = new ArrayList<>(sortedUsers.size());
        for (Map.Entry entry : sortedUsers.entrySet()) {
            modifiedLines.add(entry.getKey() + ":" + entry.getValue());
        }
        try (FileOutputStream writer = new FileOutputStream(loginFilePath)) {
            IOUtils.writeLines(modifiedLines, System.getProperty("line.separator"), writer, Charsets.UTF_8);
        }
    }

    /**
     * Stores the groups in group.cfg
     */
    private static void storeGroups(String groupFilePath, Multimap<String, String> groups) throws ManageUsersException {
        try {
            try (PrintWriter writer = new PrintWriter(new FileWriter(groupFilePath))) {
                for (Map.Entry<String, String> userEntry : groups.entries()) {
                    writer.println(userEntry.getKey() + ":" + userEntry.getValue());
                }
            }

        } catch (IOException e) {
            exitWithErrorMessage("could not write group file : " + groupFilePath, null, e);
        }
    }

    private static void exitWithErrorMessage(String errorMessage, String infoMessage, Throwable e) throws ManageUsersException {
        throw new ManageUsersException(errorMessage, e, infoMessage);
    }

    private static void warnWithMessage(String warnMessage) throws ManageUsersException {
        System.err.println("WARN : " + warnMessage);
    }

    private static String getLoginFilePath() {
        return getSchedulerFile(PASchedulerProperties.SCHEDULER_LOGIN_FILENAME.getValueAsString());
    }

    private static String getGroupFilePath() {
        return getSchedulerFile(PASchedulerProperties.SCHEDULER_GROUP_FILENAME.getValueAsString());
    }

    private static String getPublicKeyFilePath() {
        return getSchedulerFile(PASchedulerProperties.SCHEDULER_AUTH_PUBKEY_PATH.getValueAsString());
    }

    private static String getSchedulerFile(String path) {
        String schedulerFile = path;
        if (!(new File(schedulerFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix Scheduler_Home constant
            schedulerFile = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + File.separator + path;
        }
        return schedulerFile;
    }

    private static void displayHelp(Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(135);
        hf.printHelp("proactive-users" + Tools.shellExtension(), "", options, "", true);
    }

    enum Action {
        CREATE("Creating User."),
        UPDATE("Updating existing user"),
        DELETE("Deleting user");
        final String message;

        Action(String message) {
            this.message = message;
        }

        public static Action getAction(CommandLine cmd) {
            if (cmd.hasOption(CREATE_OPTION_NAME)) {
                return Action.CREATE;
            } else if (cmd.hasOption(UPDATE_OPTION_NAME)) {
                return Action.UPDATE;
            } else if (cmd.hasOption(DELETE_OPTION_NAME)) {
                return Action.DELETE;
            } else {
                throw new IllegalArgumentException("Command line does not contain, create, update or delete action: " + cmd);
            }
        }

        public boolean isCreate() {
            return this == Action.CREATE;
        }

        public boolean isUpdate() {
            return this == Action.UPDATE;
        }

        public boolean isDelete() {
            return this == Action.DELETE;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    static class UserInfo {
        private String login;
        private String password;
        private String[] groups;

        public UserInfo() {
        }

        public UserInfo(String login, String password, String[] groups) {
            this.login = login;
            this.password = password;
            this.groups = groups;
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

        public String[] getGroups() {
            return groups;
        }

        public void setGroups(String[] groups) {
            this.groups = groups;
        }
    }

    static class ManageUsersException extends Exception {
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
