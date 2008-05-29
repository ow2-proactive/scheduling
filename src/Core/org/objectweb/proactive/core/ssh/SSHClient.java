/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.ssh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;


/**
 * A minimalistic SSH Client
 *
 * Args: [-p password | -i identity_file] [-d passphrase] [-l username] [-v] hostname "cmd"
 *
 * Pubkey and Password authentications are supported. By default 'id_dsa', 'id_rsa'
 * and 'identity' files are tried.
 *
 * If a password is specified then pubkey authentication is disabled
 */
public class SSHClient {
    static final private String OPT_PASSWORD = "p";
    static final private String OPT_USERNAME = "l";
    static final private String OPT_IDENTITY = "i";
    static final private String OPT_IDENTITY_PASSWORD = "d";
    static final private String OPT_HELP = "h";
    static final private String OPT_VERBOSE = "v";
    static private boolean verbose = false;

    private static String buildCmdLine(List<String> args) {
        StringBuilder cmd = new StringBuilder();
        for (String s : args) {
            cmd.append(" ");
            cmd.append(s);
        }

        return cmd.toString();
    }

    private static void info(String str) {
        if (verbose) {
            System.out.println(str);
        }
    }

    public static void printHelp(boolean exit) {
        System.out.println("Options:");
        System.out.println("\t-" + OPT_USERNAME + "\tusername");
        System.out.println("\t-" + OPT_IDENTITY + "\tprivate key");
        System.out.println("\t-" + OPT_IDENTITY_PASSWORD + "\tpassword to decrypt the private key");
        System.out.println("\t-" + OPT_PASSWORD + "\tpassword to perform password authentication");
        System.out.println("\t-" + OPT_VERBOSE + "\tverbose mode");

        if (exit) {
            System.exit(2);
        }
    }

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(OPT_PASSWORD, true, "Password for password authentication");
        options.addOption(OPT_USERNAME, true, "Username");
        options.addOption(OPT_IDENTITY, true, "Identity file");
        options.addOption(OPT_IDENTITY_PASSWORD, true, "Password for identity file");
        options.addOption(OPT_HELP, false, "Help");
        options.addOption(OPT_VERBOSE, false, "Verbose");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);

        String username = System.getProperty("user.name");
        String password = null;
        File identity = null;
        String identityPassword = null;
        String hostname = null;

        if (cmd.hasOption(OPT_HELP)) {
            printHelp(true);
        }

        if (cmd.hasOption(OPT_USERNAME)) {
            username = cmd.getOptionValue(OPT_USERNAME);
        }

        if (cmd.hasOption(OPT_PASSWORD)) {
            password = cmd.getOptionValue(OPT_PASSWORD);
        }

        if (cmd.hasOption(OPT_IDENTITY)) {
            identity = new File(cmd.getOptionValue(OPT_IDENTITY));
            if (!identity.exists()) {
                System.err.println("[E] specified identity file," + identity + ", does not exist");
                System.exit(3);
            }
            if (!identity.isFile()) {
                System.err.println("[E] specified identity file" + identity + " is not a file");
                System.exit(3);
            }
            if (!identity.canRead()) {
                System.err.println("[E] specified identity file" + identity + " is not readable");
                System.exit(3);
            }
        }

        if (cmd.hasOption(OPT_IDENTITY_PASSWORD)) {
            identityPassword = cmd.getOptionValue(OPT_IDENTITY_PASSWORD);
        }

        if (cmd.hasOption(OPT_VERBOSE)) {
            verbose = true;
        }

        List<String> remArgs = cmd.getArgList();
        if (remArgs.size() == 0) {
            System.err.println("[E] You must specify an hostname");
            printHelp(true);
        }

        hostname = remArgs.remove(0);

        try {
            Connection conn = new Connection(hostname);
            conn.connect();

            boolean isAuthenticated = false;

            // 1. Password authentication requested
            if (password != null) {
                isAuthenticated = conn.authenticateWithPassword(username, password);
                if (isAuthenticated) {
                    info("Password authentication succeeded");
                } else {
                    info("Password authentication failed");
                }
            } else {
                // 2. Pubkey authentication

                // 2.1 An identity file is specified use it 
                if (identity != null) {
                    isAuthenticated = conn.authenticateWithPublicKey(username, identity, identityPassword);
                    if (isAuthenticated) {
                        info("Pubkey authentication succeeded with " + identity);
                    } else {
                        info("Pubkey authentication failed with " + identity);
                    }
                } else {
                    // 2.2 Try to find identity files automagically
                    for (String id : SSHKeys.getKeys()) {
                        File f = new File(id);
                        if (!(f.exists() && f.isFile() && f.canRead())) {
                            continue;
                        }

                        isAuthenticated = conn.authenticateWithPublicKey(username, f, identityPassword);
                        info("Pubkey authentication succeeded with " + f);
                        if (isAuthenticated) {
                            break;
                        }
                    }
                }
            }

            if (!isAuthenticated) {
                System.err.println("[E] Authentication failed");
                System.exit(2);
            }

            conn.setTCPNoDelay(true);
            Session sess = conn.openSession();

            sess.execCommand(buildCmdLine(remArgs));

            InputStream stdout = sess.getStdout();
            InputStream stderr = sess.getStderr();

            byte[] buffer = new byte[8192];

            while (true) {
                if ((stdout.available() == 0) && (stderr.available() == 0)) {

                    /* Even though currently there is no data available, it may be that new data arrives
                     * and the session's underlying channel is closed before we call waitForCondition().
                     * This means that EOF and STDOUT_DATA (or STDERR_DATA, or both) may
                     * be set together.
                     */
                    int conditions = sess.waitForCondition(ChannelCondition.STDOUT_DATA |
                        ChannelCondition.STDERR_DATA | ChannelCondition.EOF, 0);

                    /* Wait no longer than 2 seconds (= 2000 milliseconds) */
                    if ((conditions & ChannelCondition.TIMEOUT) != 0) {

                        /* A timeout occured. */
                        throw new IOException("Timeout while waiting for data from peer.");
                    }

                    /* Here we do not need to check separately for CLOSED, since CLOSED implies EOF */
                    if ((conditions & ChannelCondition.EOF) != 0) {

                        /* The remote side won't send us further data... */
                        if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0) {

                            /* ... and we have consumed all data in the local arrival window. */
                            break;
                        }
                    }

                    /* OK, either STDOUT_DATA or STDERR_DATA (or both) is set. */

                    // You can be paranoid and check that the library is not going nuts:
                    // if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0)
                    //	throw new IllegalStateException("Unexpected condition result (" + conditions + ")");
                }

                /* If you below replace "while" with "if", then the way the output appears on the local
                 * stdout and stder streams is more "balanced". Addtionally reducing the buffer size
                 * will also improve the interleaving, but performance will slightly suffer.
                 * OKOK, that all matters only if you get HUGE amounts of stdout and stderr data =)
                 */
                while (stdout.available() > 0) {
                    int len = stdout.read(buffer);
                    if (len > 0) { // this check is somewhat paranoid
                        System.out.write(buffer, 0, len);
                    }
                }

                while (stderr.available() > 0) {
                    int len = stderr.read(buffer);
                    if (len > 0) { // this check is somewhat paranoid
                        System.err.write(buffer, 0, len);
                    }
                }
            }

            sess.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(2);
        }
        System.exit(0);
    }
}
