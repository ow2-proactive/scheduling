/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting.helper.filetransfer.driver;

import static org.objectweb.proactive.core.ssh.SSH.logger;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.core.ssh.SshConfig;
import org.objectweb.proactive.core.util.ProActiveInet;

import com.trilead.ssh2.Connection;


public class ConnectionTools {

    public static Connection authentificateWithKeys(String username, String hostname, int port)
            throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Create SSH Connection from" + ProActiveInet.getInstance().getInetAddress() +
                " to " + hostname + ":" + port);
        }
        Connection connection = null;
        // get keys through ProActive's ssh config
        SshConfig config = new SshConfig();

        for (String key : config.getPrivateKeys(hostname)) {
            connection = new Connection(hostname, port);
            connection.connect();

            try {
                System.out.println("Trying " + key);
                connection.authenticateWithPublicKey(username, new File(key), null);
                if (connection.isAuthenticationComplete()) {
                    break;
                }
            } catch (IOException e) {
                // Gracefully handle password protected private key
                boolean isPasswordProtected = false;

                Throwable t = e;
                while (t != null || !isPasswordProtected) {
                    if (t.getMessage().contains("PEM is encrypted, but no password was specified")) {
                        isPasswordProtected = true;
                    }
                    t = t.getCause();
                }

                if (isPasswordProtected) {
                    logger.warn(key + " is password protected. Ignore it !");
                    connection.close();
                } else {
                    throw e;
                }
            }
        }

        if (connection.isAuthenticationComplete()) {
            connection.setTCPNoDelay(true);
            return connection;
        } else {
            // Connection cannot be opened
            if (logger.isInfoEnabled()) {
                logger.info("Authentication failed for " + username + "@" + hostname + ":" + port);
                logger.info("Keys were:");
                for (String key : config.getPrivateKeys(hostname)) {
                    logger.info("\t" + key);
                }
            }

            throw new IOException("Authentication failed");
        }
    }

}
