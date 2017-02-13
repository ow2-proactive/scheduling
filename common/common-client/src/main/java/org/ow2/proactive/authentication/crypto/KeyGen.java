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
package org.ow2.proactive.authentication.crypto;

import java.io.File;
import java.security.KeyException;


/**
 * KeyPair generation utility
 * 
 * @see org.ow2.proactive.authentication.crypto.KeyPairUtil
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 *
 */
public class KeyGen {

    /**
     * Entry point
     * 
     * @param args arguments, try '-h' for help
     * @throws KeyException keypair generation failure
     */
    public static void main(String[] args) throws KeyException {

        /**
         * Default values
         */
        String algo = "RSA"; // should work fine with default providers
        String pubKey = null;
        String privKey = null;
        int size = 1024;

        /**
         * Arguments handling
         */
        int index = 0;
        while (index < args.length) {
            if (args[index].equals("--size") || args[index].equals("-s")) {
                if (++index == args.length) {
                    printError("No value provided for option --size");
                    printUsage(algo, size);
                    return;
                }
                size = Integer.parseInt(args[index]);
            }
            if (args[index].equals("--private") || args[index].equals("-p")) {
                if (++index == args.length) {
                    printError("No value provided for argument --private");
                    printUsage(algo, size);
                    return;
                }
                privKey = args[index];

            }
            if (args[index].equals("--public") || args[index].equals("-P")) {
                if (++index == args.length) {
                    printError("No value provided for argument --public");
                    printUsage(algo, size);
                    return;
                }
                pubKey = args[index];
            }
            if (args[index].equals("--algo") || args[index].equals("-a")) {
                if (++index == args.length) {
                    printError("No value provided for option --algo");
                    printUsage(algo, size);
                    return;
                }
                algo = args[index];
            }
            if (args[index].equals("--help") || args[index].equals("-h")) {
                printUsage(algo, size);
                return;
            }
            index++;
        }

        if (privKey == null) {
            printError("--private argument is mandatory");
            printUsage(algo, size);
            return;
        }
        if (pubKey == null) {
            printError("--public argument is mandatory");
            printUsage(algo, size);
            return;
        }

        /**
         * Create directories if not existing
         */
        try {
            File f = new File(pubKey).getParentFile();
            if (f != null && !f.isDirectory()) {
                f.mkdirs();
            }
            f = new File(privKey).getParentFile();
            if (f != null && !f.isDirectory()) {
                f.mkdirs();
            }
        } catch (Exception e) {
            printError("Could not create directory: " + e.getMessage());
        }

        KeyPairUtil.generateKeyPair(algo, size, privKey, pubKey);

        System.out.println("Successfully stored generated keypair at:");
        System.out.println("\t" + privKey);
        System.out.println("\t" + pubKey);
    }

    private static void printError(String errorMessage) {
        System.err.println(errorMessage + "\n");
    }

    private static void printUsage(String algo, int size) {
        System.out.println("Usage:");
        System.out.println("\tjava " + KeyGen.class.getCanonicalName() + " arguments [options]");
        System.out.println("");
        System.out.println("Description:");
        System.out.println("\tGenerates a couple of public and private keys that will");
        System.out.println("\tbe used for Resource Manager and Scheduler authentication.");
        System.out.println("");
        System.out.println("Arguments:");
        System.out.println("\t-P, --public PATH");
        System.out.println("\t\tPath to the generated public key");
        System.out.println("\t-p, --private PATH");
        System.out.println("\t\tPath to the generated private key");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("\t-s, --size SIZE [=" + size + "]");
        System.out.println("\t\tSize of the key");
        System.out.println("\t-a, --algo ALGO [=" + algo + "]");
        System.out.println("\t\tKey generation algorithm");
    }

}
