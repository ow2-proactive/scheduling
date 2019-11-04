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
package org.ow2.proactive.scheduler.util;

import java.io.Console;
import java.io.IOException;

import org.apache.commons.cli.*;
import org.ow2.proactive.core.properties.PropertyDecrypter;
import org.ow2.proactive.utils.Tools;


public class EncryptCommand {

    private static final String newline = System.lineSeparator();

    public static final String HELP_OPTION = "h";

    public static final String HELP_OPTION_NAME = "help";

    public static final String DATA_OPTION = "d";

    public static final String DATA_OPTION_NAME = "data";

    public static final String INTERACTIVE_OPTION = "i";

    public static final String INTERACTIVE_OPTION_OPTION_NAME = "interactive";

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
            encryptCommand(args);
        } catch (EncryptCommandException e) {
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

    public static void encryptCommand(String... args) throws EncryptCommandException {
        Console console = System.console();
        Options options = new Options();
        CommandLine cmd = getCommandLine(args, options);

        String dataToEncrypt = null;

        if (cmd.hasOption(HELP_OPTION_NAME) || cmd.getOptions().length == 0) {
            displayHelp(options);
            return;
        }
        if (cmd.hasOption(DATA_OPTION_NAME)) {
            dataToEncrypt = cmd.getOptionValue(DATA_OPTION_NAME);
        }
        if (dataToEncrypt == null && cmd.hasOption(INTERACTIVE_OPTION_OPTION_NAME)) {
            System.out.print("Data to encrypt: ");
            dataToEncrypt = new String(console.readPassword());
        }
        if (dataToEncrypt == null || dataToEncrypt.isEmpty()) {
            exitWithErrorMessage("No data to encrypt", null, null);
        }
        System.out.println("Encrypted value (to use in configuration files):");
        System.out.println("ENC(" + PropertyDecrypter.getDefaultEncryptor().encrypt(dataToEncrypt.trim()) + ")");
    }

    /**
     * Build the command line options and parse
     */
    private static CommandLine getCommandLine(String[] args, Options options) throws EncryptCommandException {
        Option opt = new Option(HELP_OPTION, HELP_OPTION_NAME, false, "Display this help");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(INTERACTIVE_OPTION, INTERACTIVE_OPTION_OPTION_NAME, false, "Interactive mode");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(DATA_OPTION, DATA_OPTION_NAME, true, "Data to encrypt");
        opt.setRequired(false);
        opt.setArgName(DATA_OPTION_NAME.toUpperCase());
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
            throws EncryptCommandException {
        throw new EncryptCommandException(errorMessage, e, infoMessage);
    }

    private static void displayHelp(Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(135);
        hf.printHelp("encrypt" + Tools.shellExtension(), "", options, "", true);
    }

    static class EncryptCommandException extends Exception {
        public String getAdditionalInfo() {
            return additionalInfo;
        }

        private String additionalInfo = null;

        public EncryptCommandException(String message) {
            super(message);
        }

        public EncryptCommandException(String message, Throwable cause) {
            super(message, cause);
        }

        public EncryptCommandException(String message, Throwable cause, String additionalInfo) {
            super(message, cause);
            this.additionalInfo = additionalInfo;
        }
    }
}
