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

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive.web.WebProperties;


public class CopyRBACRole {

    private static final String newline = System.lineSeparator();

    public static final String HELP_OPTION = "h";

    public static final String HELP_OPTION_NAME = "help";

    public static final String DEBUG_OPTION = "d";

    public static final String DEBUG_OPTION_NAME = "debug";

    public static final String RBAC_SOURCE_ROLE_OPTION = "S";

    public static final String RBAC_SOURCE_ROLE_OPTION_NAME = "source";

    public static final String RBAC_DESTINATION_ROLE_OPTION = "D";

    public static final String RBAC_DESTINATION_ROLE_OPTION_NAME = "destination";

    public static final String RBAC_OVERWRITE_OPTION = "y";

    public static final String RBAC_OVERWRITE_OPTION_NAME = "overwrite";

    public static final String JAVA_POLICY_CONFIG_PATH = "config/security.java.policy-server";

    private static boolean isDebug = false;

    private static String source = null;

    private static String destination = null;

    private static boolean overwrite = false;

    /**
     * Entry point
     *
     * @param args arguments, try '-h' for help
     * @see WebProperties
     */
    public static void main(String[] args) {

        try {
            if (readCommandArguments(args)) {
                copyRoleConfiguration(getSchedulerFile(JAVA_POLICY_CONFIG_PATH));
            }
        } catch (CopyRBACRoleException e) {
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

    public static boolean readCommandArguments(String... args) throws CopyRBACRoleException {
        Options options = new Options();
        CommandLine cmd = getCommandLine(args, options);

        if (cmd.hasOption(HELP_OPTION_NAME) || cmd.getOptions().length == 0) {
            displayHelp(options);
            return false;
        }

        if (cmd.hasOption(DEBUG_OPTION_NAME)) {
            isDebug = true;
        }

        if (cmd.hasOption(RBAC_SOURCE_ROLE_OPTION_NAME)) {
            source = cmd.getOptionValue(RBAC_SOURCE_ROLE_OPTION_NAME);
        } else {
            exitWithErrorMessage("--" + RBAC_SOURCE_ROLE_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(RBAC_DESTINATION_ROLE_OPTION_NAME)) {
            destination = cmd.getOptionValue(RBAC_DESTINATION_ROLE_OPTION_NAME);
        } else {
            exitWithErrorMessage("--" + RBAC_DESTINATION_ROLE_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(RBAC_OVERWRITE_OPTION_NAME)) {
            overwrite = true;
        }

        return true;
    }

    /**
     * Build the command line options and parse
     */
    private static CommandLine getCommandLine(String[] args, Options options) throws CopyRBACRoleException {
        Option opt = new Option(HELP_OPTION, HELP_OPTION_NAME, false, "Display this help");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(DEBUG_OPTION, DEBUG_OPTION_NAME, false, "Debug mode (prints modified files and properties)");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(RBAC_SOURCE_ROLE_OPTION,
                         RBAC_SOURCE_ROLE_OPTION_NAME,
                         false,
                         "(required) Name of the role (group) that will be copied.");
        opt.setRequired(false);
        opt.setArgName(RBAC_SOURCE_ROLE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(RBAC_DESTINATION_ROLE_OPTION,
                         RBAC_DESTINATION_ROLE_OPTION_NAME,
                         false,
                         "(required) Name of the destination role (group).");
        opt.setRequired(false);
        opt.setArgName(RBAC_DESTINATION_ROLE_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(RBAC_OVERWRITE_OPTION,
                         RBAC_OVERWRITE_OPTION_NAME,
                         false,
                         "Overwrite if the destination role already exists.");
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

    private static void exitWithErrorMessage(String errorMessage, String infoMessage, Throwable e)
            throws CopyRBACRoleException {
        throw new CopyRBACRoleException(errorMessage, e, infoMessage);
    }

    private static void displayHelp(Options options) {
        String header = newline + "Create a new JaaS role by copying an existing one." + newline;
        header += "JaaS roles are used to define authorization inside the ProActive server." + newline + newline;

        String footer = newline + "Examples: " + newline;
        footer += "# Copy existing 'server-admins' role into new role 'my-admin-group'. Debug output to see all modifications." +
                  newline;
        footer += "copy-role --source server-admins --destination my-admin-group --debug" + newline;

        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(135);
        hf.printHelp("copy-role" + Tools.shellExtension(), header, options, footer, true);
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
    private static File checkConfigurationFile(File configurationFile) throws CopyRBACRoleException {
        if (configurationFile != null && configurationFile.exists() && configurationFile.isFile()) {
            if (!configurationFile.canWrite()) {
                throw new CopyRBACRoleException("File " + configurationFile + " cannot be modified");
            }
        }
        return configurationFile;
    }

    private static void copyRoleConfiguration(File securityPolicyFile) throws CopyRBACRoleException, IOException {
        if (isDebug) {
            log("DEBUG: Checking " + securityPolicyFile);
        }
        checkConfigurationFile(securityPolicyFile);
        List<String> inputLines;
        try (BufferedReader reader = new BufferedReader(new FileReader(securityPolicyFile))) {
            inputLines = IOUtils.readLines(reader);
        }
        List<String> roleDefinition = findRoleDefinition(inputLines);

        if (roleDefinition.size() == 0) {
            throw new CopyRBACRoleException("Cannot find role " + source + " in " + securityPolicyFile);
        }

        List<String> outputLines = new ArrayList<>(inputLines.size());
        boolean roleOverwritten = appendOrOverwriteRole(inputLines, outputLines, roleDefinition);
        if (roleOverwritten && !overwrite) {
            throw new CopyRBACRoleException("Role " + destination +
                                            " already exists and overwrite mode is not enabled.");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(securityPolicyFile))) {
            IOUtils.writeLines(outputLines, null, writer);
        }
        if (roleOverwritten) {
            log("Existing role " + destination + " overwritten");
        } else {
            log("New role " + destination + " created");
        }
        debugRoleContents(roleDefinition);
    }

    private static void debugRoleContents(List<String> roleDefinition) {
        if (isDebug) {
            for (String line : roleDefinition) {
                log("DEBUG: " + line);
            }
        }
    }

    private static List<String> findRoleDefinition(List<String> inputLines) {
        List<String> roleDefinition = new ArrayList<>();
        boolean roleRead = false;
        for (String line : inputLines) {
            if (line.trim().startsWith(
                                       "grant principal org.ow2.proactive.authentication.principals.GroupNamePrincipal \"" +
                                       source + "\"")) {
                roleDefinition.add("grant principal org.ow2.proactive.authentication.principals.GroupNamePrincipal \"" +
                                   destination + "\" {");
            } else if (roleDefinition.size() > 0 && !roleRead && line.trim().startsWith("}")) {
                roleDefinition.add(line);
                roleRead = true;
            } else if (roleDefinition.size() > 0 && !roleRead) {
                roleDefinition.add(line);
            }
        }
        return roleDefinition;
    }

    private static boolean appendOrOverwriteRole(List<String> inputLines, List<String> outputLines,
            List<String> roleDefinition) {
        boolean isOverwriting = false;
        boolean roleOverwritten = false;
        for (String line : inputLines) {
            if (line.trim().startsWith(
                                       "grant principal org.ow2.proactive.authentication.principals.GroupNamePrincipal \"" +
                                       destination + "\"")) {
                isOverwriting = true;
            } else if (isOverwriting && line.trim().startsWith("}")) {
                outputLines.addAll(roleDefinition);
                isOverwriting = false;
                roleOverwritten = true;
            } else if (!isOverwriting) {
                outputLines.add(line);
            }
        }
        if (!roleOverwritten) {
            // if the role has not been overwritten, append the role at the end
            outputLines.add(newline);
            outputLines.addAll(roleDefinition);
        }
        return roleOverwritten;
    }

    private static void log(String line) {
        System.out.println(line);
    }

    static class CopyRBACRoleException extends Exception {
        public String getAdditionalInfo() {
            return additionalInfo;
        }

        private String additionalInfo = null;

        public CopyRBACRoleException(String message) {
            super(message);
        }

        public CopyRBACRoleException(String message, Throwable cause) {
            super(message, cause);
        }

        public CopyRBACRoleException(String message, Throwable cause, String additionalInfo) {
            super(message, cause);
            this.additionalInfo = additionalInfo;
        }
    }

}
