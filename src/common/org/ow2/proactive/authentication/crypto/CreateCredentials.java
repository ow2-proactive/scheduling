/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.authentication.crypto;

import java.io.File;
import java.io.IOException;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.passwordhandler.PasswordField;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.authentication.Connection;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 * Creates encrypted credentials for future non-interactive authentications
 *
 * @see  org.ow2.proactive.authentication.crypto.Credentials
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 * 
 */
public class CreateCredentials {

    /**
     * Entry point
     * 
     * @see org.ow2.proactive.authentication.crypto.Credentials
     * @param args arguments, try '-h' for help
     *
     */
    public static void main(String[] args) {

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

        if (args.length == 0) {
            displayHelp(path, cipher);
            return;
        }

        /**
         * Arguments handling
         */
        int index = 0;
        while (index < args.length) {
            if (args[index].equals("--file") || args[index].equals("-F")) {
                if (++index == args.length) {
                    System.out.println("No value provided for argument --public");
                    return;
                }
                pubKeyPath = args[index];
            }
            if (args[index].equals("--rm") || args[index].equals("-R")) {
                if (++index == args.length) {
                    System.out.println("No value provided for argument --rm");
                    return;
                }
                rm = args[index];
            }
            if (args[index].equals("--scheduler") || args[index].equals("-S")) {
                if (++index == args.length) {
                    System.out.println("No value provided for argument --scheduler");
                    return;
                }
                scheduler = args[index];
            }
            if (args[index].equals("--login") || args[index].equals("-l")) {
                if (++index == args.length) {
                    System.out.println("No value provided for option --login");
                    return;
                }
                login = args[index];
            }
            if (args[index].equals("--password") || args[index].equals("-p")) {
                if (++index == args.length) {
                    System.out.println("No value provided for option --password");
                    return;
                }
                pass = args[index];
            }
            if (args[index].equals("--keyfile") || args[index].equals("-k")) {
                if (++index == args.length) {
                    System.out.println("No value provided for option --keyfile");
                    return;
                }
                keyfile = args[index];
            }
            if (args[index].equals("--output") || args[index].equals("-o")) {
                if (++index == args.length) {
                    System.out.println("No value provided for option --output");
                    return;
                }
                path = args[index];
            }
            if (args[index].equals("--cipher") || args[index].equals("-c")) {
                if (++index == args.length) {
                    System.out.println("No value provided for option --cipher");
                    return;
                }
                cipher = args[index];
            }

            if (args[index].equals("--help") || args[index].equals("-h")) {
                displayHelp(path, cipher);
            }
            index++;
        }

        int acc = 0;
        if (scheduler != null) {
            url = Connection.normalize(scheduler) + "SCHEDULER";
            acc++;

        }
        if (rm != null) {
            url = Connection.normalize(rm) + "RMAUTHENTICATION";
            acc++;
        }
        if (pubKeyPath != null) {
            acc++;
        }
        if (acc > 1) {
            System.out.println("--rm, --scheduler and --file arguments cannot be combined.");
            System.out.println("try -h for help.");
            return;
        }

        if (url != null) {
            try {
                Connection<AuthenticationImpl> conn = new Connection<AuthenticationImpl>(
                        AuthenticationImpl.class) {
                    public Logger getLogger() {
                        return ProActiveLogger.getLogger("pa.scheduler.credentials");
                    }
                };
                AuthenticationImpl auth = conn.connect(url);
                pubKey = auth.getPublicKey();
            } catch (Exception e) {
                System.out.println("Could not retrieve public key from " + url + ": ");
                e.printStackTrace();
                System.exit(0);
            }
            System.out.println("Successfully obtained public key from " + url + ".\n");
        } else if (pubKeyPath != null) {
            try {
                pubKey = Credentials.getPublicKey(pubKeyPath);
            } catch (KeyException e) {
                System.out.println("Could not retrieve public key from " + pubKeyPath + ":");
                e.printStackTrace();
            }
        } else {
            System.out.println("No public key specified, attempting to retrieve it from default location.");
            pubKeyPath = Credentials.getPubKeyPath();
            try {
                pubKey = Credentials.getPublicKey(pubKeyPath);
            } catch (KeyException e) {
                System.out.println("Could not retrieve public key from " + pubKeyPath + ":");
                e.printStackTrace();
                return;
            }
        }

        if (login != null && pass != null) {
            System.out.println("Running in non-interactive mode.\n");
            interactive = false;
        } else if (pass != null) {
            System.out.println("Ignoring argument --password: used without --login.");
        }

        if (interactive) {
            System.out.println("Please enter Scheduler credentials,");
            System.out.println("they will be stored encrypted on disk for future logins.\n");

            System.out.print("login: ");
            if (login == null) {
                Scanner scanner = new Scanner(System.in);
                login = scanner.nextLine();
            } else {
                System.out.println(login);
            }
            char[] pw;
            try {
                pw = PasswordField.getPassword(System.in, "password: ");
                if (pw == null) {
                    pass = "";
                } else {
                    pass = String.valueOf(pw);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return;
            }
            System.out.println();
        }

        byte[] keyfileContent = null;
        //load keyfile if specified
        if (keyfile != null) {
            try {
                keyfileContent = FileToBytesConverter.convertFileToByteArray(new File(keyfile));
            } catch (Throwable t) {
                t.printStackTrace();
                return;
            }
        }
        Credentials cred = null;
        try {
            cred = Credentials.createCredentials(new CredData(login, pass, keyfileContent), pubKey, cipher);
            cred.writeToDisk(path);
        } catch (KeyException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Successfully stored encrypted credentials on disk at :");
        System.out.println("\t" + path);

        System.exit(0);
    }

    private static void displayHelp(String path, String cipher) {
        System.out.println("Usage:");
        System.out.println("\tjava " + CreateCredentials.class.getCanonicalName() + " pubkey [options]");
        System.out.println("");
        System.out.println("Description:");
        System.out.println("\tCreates encrypted credentials for");
        System.out.println("\tfuture non-interactive authentications.");
        System.out.println("");
        System.out.println("Public key must be one of:");
        System.out.println("\t-F, --file PATH [=" + Credentials.getPubKeyPath() + "]");
        System.out.println("\t\tPath to the public key on the local filesystem.");
        System.out.println("\t\tIf no pubkey argument is specified, default location will be used.");
        System.out.println("\t-R, --rm URL");
        System.out.println("\t\tRequest the public key to the Resource Manager at URL.");
        System.out.println("\t-S, --scheduler URL");
        System.out.println("\t\tRequest the public key to the Scheduler at URL.");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("\t-l, --login LOGIN");
        System.out.println("\t\tGenerate credentials for a specific user,");
        System.out.println("\t\twill be asked interactively if not specified.");
        System.out.println("\t-p, --password PASS\t");
        System.out.println("\t\tUse specified password and runs in non-interactive");
        System.out.println("\t\tmode. Ignored if used without the --login switch.");
        System.out.println("\t-k, --keyfile KEY\t");
        System.out.println("\t\tUse specified ssh private key");
        System.out.println("\t\tThis option is not mandatory");
        System.out.println("\t-o, --output PATH [=" + path + "]\t");
        System.out.println("\t\tOutput the resulting credentials to the specified file.");
        System.out.println("\t-c, --cipher PARAMS [=" + cipher + "]");
        System.out.println("\t\tUse specified cipher parameters, need to be compatible");
        System.out.println("\t\twith the specified key.");
    }

}
