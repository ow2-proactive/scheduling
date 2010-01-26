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
                    System.out.println("No value provided for option --size");
                    return;
                }
                size = Integer.parseInt(args[index]);
            }
            if (args[index].equals("--private") || args[index].equals("-p")) {
                if (++index == args.length) {
                    System.out.println("No value provided for argument --private");
                    return;
                }
                privKey = args[index];

            }
            if (args[index].equals("--public") || args[index].equals("-P")) {
                if (++index == args.length) {
                    System.out.println("No value provided for argument --public");
                    return;
                }
                pubKey = args[index];
            }
            if (args[index].equals("--algo") || args[index].equals("-a")) {
                if (++index == args.length) {
                    System.out.println("No value provided for option --algo");
                    return;
                }
                algo = args[index];
            }
            if (args[index].equals("--help") || args[index].equals("-h")) {
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
                return;
            }
            index++;
        }

        if (privKey == null) {
            System.out.println("--private argument is mandatory.");
            System.out.println("Use -h for help.");
            return;
        }
        if (pubKey == null) {
            System.out.println("--public argument is mandatory.");
            System.out.println("Use -h for help.");
            return;
        }

        /**
         * Create directories if not existing
         */
        try {
            File f = new File(pubKey).getParentFile();
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            f = new File(privKey).getParentFile();
            if (!f.isDirectory()) {
                f.mkdirs();
            }
        } catch (Exception e) {
            System.out.println("Could not create directory: " + e.getMessage());
        }

        KeyPairUtil.generateKeyPair(algo, size, privKey, pubKey);

        System.out.println("Successfully stored generated keypair at:");
        System.out.println("\t" + privKey);
        System.out.println("\t" + pubKey);
    }
}
