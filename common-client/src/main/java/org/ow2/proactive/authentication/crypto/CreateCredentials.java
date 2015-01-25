/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive.authentication.crypto;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.SecurityManagerConfigurator;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.authentication.Connection;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.Tools;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyException;
import java.security.PublicKey;


/**
 * Creates encrypted credentials for future non-interactive authentications
 *
 * @see  org.ow2.proactive.authentication.crypto.Credentials
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 * 
 */
public class CreateCredentials {

    private static final String newline = System.getProperty("line.separator");

    /**
     * Entry point
     * 
     * @see org.ow2.proactive.authentication.crypto.Credentials
     * @param args arguments, try '-h' for help
     * @throws IOException
     * @throws ParseException
     *
     */
    public static void main(String[] args) throws IOException, ParseException {

        SecurityManagerConfigurator.configureSecurityManager(CreateCredentials.class.getResource(
                "/all-permissions.security.policy").toString());

        Console console = System.console();
        /**
         * default values
         */
        boolean interactive = true;
        String pubKeyPath = null;
        PublicKey pubKey = null;
        String login = null;
        String pass = null;
        String keyfile = null;
        String cipher = "RSA/ECB/PKCS1Padding";
        String path = Credentials.getCredentialsPath();
        String rm = null;
        String scheduler = null;
        String url = null;

        Options options = new Options();

        Option opt = new Option("h", "help", false, "Display this help");
        opt.setRequired(false);
        options.addOption(opt);

        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        opt = new Option("F", "file", true, "Public key path on the local filesystem [default:" +
            Credentials.getPubKeyPath() + "]");
        opt.setArgName("PATH");
        opt.setArgs(1);
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("R", "rm", true, "Request the public key to the Resource Manager at URL");
        opt.setArgName("URL");
        opt.setArgs(1);
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("S", "scheduler", true, "Request the public key to the Scheduler at URL");
        opt.setArgName("URL");
        opt.setArgs(1);
        opt.setRequired(false);
        group.addOption(opt);
        options.addOptionGroup(group);

        opt = new Option("l", "login", true,
            "Generate credentials for this specific user, will be asked interactively if not specified");
        opt.setArgName("LOGIN");
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("p", "password", true,
            "Use this password, will be asked interactively if not specified");
        opt.setArgName("PWD");
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("k", "keyfile", true,
            "Use specified ssh private key, asked interactively if specified without PATH, not specified otherwise.");
        opt.setArgName("PATH");
        opt.setOptionalArg(true);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("o", "output", true,
            "Output the resulting credentials to the specified file [default:" + path + "]");
        opt.setArgName("PATH");
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "cipher", true,
            "Use specified cipher parameters, need to be compatible with the specified key [default:" +
                cipher + "]");
        opt.setArgName("PARAMS");
        opt.setArgs(1);
        opt.setRequired(false);
        options.addOption(opt);

        Parser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            System.err.println(newline + "ERROR : " + e.getMessage() + newline);
            System.out.println("type -h or --help to display help screen");
            System.exit(1);
        }

        if (cmd.hasOption("help")) {
            displayHelp(options);
        }

        if (cmd.hasOption("file")) {
            pubKeyPath = cmd.getOptionValue("file");
        }
        if (cmd.hasOption("rm")) {
            rm = cmd.getOptionValue("rm");
        }
        if (cmd.hasOption("scheduler")) {
            scheduler = cmd.getOptionValue("scheduler");
        }

        if (cmd.hasOption("login")) {
            login = cmd.getOptionValue("login");
        }
        if (cmd.hasOption("password")) {
            pass = cmd.getOptionValue("password");
        }
        if (cmd.hasOption("keyfile") && cmd.getOptionValues("keyfile") != null) {
            keyfile = cmd.getOptionValue("keyfile");
        }

        if (cmd.hasOption("output")) {
            path = cmd.getOptionValue("output");
        }
        if (cmd.hasOption("cipher")) {
            cipher = cmd.getOptionValue("cipher");
        }

        int acc = 0;
        if (pubKeyPath != null) {
            acc++;
        }
        if (scheduler != null) {
            url = Connection.normalize(scheduler) + "SCHEDULER";
            acc++;

        }
        if (rm != null) {
            url = Connection.normalize(rm) + "RMAUTHENTICATION";
            acc++;
        }
        if (acc > 1) {
            System.out.println("--rm, --scheduler and --file arguments cannot be combined.");
            System.out.println("try -h for help.");
            System.exit(1);
        }

        if (url != null) {
            try {
                Connection<AuthenticationImpl> conn = new Connection<AuthenticationImpl>(
                        AuthenticationImpl.class) {
                    public Logger getLogger() {
                        return Logger.getLogger("pa.scheduler.credentials");
                    }
                };
                AuthenticationImpl auth = conn.connect(url);
                pubKey = auth.getPublicKey();
            } catch (Exception e) {
                System.err.println("ERROR : Could not retrieve public key from '" + url + "'");
                e.printStackTrace();
                System.exit(3);
            }
            System.out.println("Successfully obtained public key from " + url + newline);
        } else if (pubKeyPath != null) {
            try {
                pubKey = Credentials.getPublicKey(pubKeyPath);
            } catch (KeyException e) {
                System.err.println("ERROR : Could not retrieve public key from '" + pubKeyPath +
                    "' (no such file)");
                System.exit(4);
            }
        } else {
            System.out.println("No public key specified, attempting to retrieve it from default location.");
            pubKeyPath = Credentials.getPubKeyPath();
            try {
                pubKey = Credentials.getPublicKey(pubKeyPath);
            } catch (KeyException e) {
                System.err.println("ERROR : Could not retrieve public key from '" + pubKeyPath +
                    "' (no such file)");
                System.exit(5);
            }
        }

        if (login != null && pass != null &&
            (!cmd.hasOption("keyfile") || cmd.getOptionValues("keyfile") != null)) {
            System.out.println("Running in non-interactive mode." + newline);
            interactive = false;
        } else {
            System.out.println("Running in interactive mode.");
        }

        if (interactive) {
            System.out.println("Please enter Scheduler credentials,");
            System.out.println("they will be stored encrypted on disk for future logins." + newline);
            System.out.print("login: ");
            if (login == null) {
                login = console.readLine();
            } else {
                System.out.println(login);
            }
            System.out.print("password: ");
            if (pass == null) {
                pass = new String(console.readPassword());
            } else {
                System.out.println("*******");
            }
            System.out.print("keyfile: ");
            if (!cmd.hasOption("keyfile")) {
                System.out.println("no key file specified");
            } else if (cmd.hasOption("keyfile") && cmd.getOptionValues("keyfile") != null) {
                System.out.println(keyfile);
            } else {
                keyfile = console.readLine();
            }
        }

        try {
            CredData credData;
            if (keyfile != null && keyfile.length() > 0) {
                byte[] keyfileContent = FileToBytesConverter.convertFileToByteArray(new File(keyfile));
                credData = new CredData(CredData.parseLogin(login), CredData.parseDomain(login), pass,
                    keyfileContent);
            } else {
                System.out.println("--> Ignoring keyfile, credential does not contain SSH key");
                credData = new CredData(CredData.parseLogin(login), CredData.parseDomain(login), pass);
            }
            Credentials cred = Credentials.createCredentials(credData, pubKey, cipher);
            cred.writeToDisk(path);
        } catch (FileNotFoundException e) {
            System.err.println("ERROR : Could not retrieve ssh private key from '" + keyfile +
                "' (no such file)");
            System.exit(6);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(7);
        }

        System.out.println("Successfully stored encrypted credentials on disk at :");
        System.out.println("\t" + path);

        System.exit(0);
    }

    private static void displayHelp(Options options) {
        System.out.println("");
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(135);
        hf.printHelp("create-cred" + Tools.shellExtension(), "", options, "", true);
        System.exit(2);
    }

}
