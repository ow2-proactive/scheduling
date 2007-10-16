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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import static org.objectweb.proactive.core.ssh.SSH.logger;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.LocalPortForwarder;
import ch.ethz.ssh2.Session;


/**
 * A connection to a SSH-2 server
 *
 * This class is an interface between ganymed and ProActive. It encapsulates
 * a ch.ethz.ssh2.Connection and all tunnels and sessions related to this
 * connection
 */
public class SSHConnection {
    private Connection connection = null;
    private Collection<LocalPortForwarder> portForwarders = null;
    private Collection<Session> sessions = null;

    public SSHConnection(String username, String hostname, String port)
        throws IOException {
        this(username, hostname, Integer.parseInt(port));
    }

    /**
     * Create a SSH connection to a SSH-2 server
     *
     * Only public key authentication is supported. All public keys found
     * in SshParameters.getSshKeyDirectory() are tried. The private key
     * must not be password protected.
     *
     * @param username  A string holding the username
     * @param hostname the hostname of the SSH-2 server
     * @param port port on the server, normally 22
     * @throws IOException If the connection can't be established an IOException
     * is thrown
     * @see SSHKeys
     * @see SshParameters
     */
    public SSHConnection(String username, String hostname, int port)
        throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Create SSH Connection from" +
                InetAddress.getLocalHost() + " to " + hostname + ":" + port);
        }
        connection = new Connection(hostname, port);
        connection.connect();

        String[] keys = SSHKeys.getKeys();

        for (String key : keys) {
            connection.authenticateWithPublicKey(username, new File(key), null);
            if (connection.isAuthenticationComplete()) {
                connection.setTCPNoDelay(true);
                break;
            }
        }

        if (connection.isAuthenticationComplete()) {
            portForwarders = new ArrayList<LocalPortForwarder>();
            sessions = new ArrayList<Session>();
            return;
        }

        // Connection cannot be opened
        if (logger.isInfoEnabled()) {
            logger.info("Authentication failed for " + username + "@" +
                hostname + ":" + port);
            logger.info("Keys were:");
            for (String key : keys) {
                logger.info("\t" + key);
            }
        }

        throw new IOException("Authentication failed");
    }

    /**
     * Get the encapslutation Connection object
     * @return the Connection
     * @see Connection
     */
    protected Connection getConnection() {
        return connection;
    }

    /**
     * All SSH Sessions associated to this connection
     * @return all sessions handled by this connection
     * @see Session
     */
    protected Collection<Session> getSessions() {
        return sessions;
    }

    /**
     * All SSH Tunnels associated to this connection
     * @return all tunnels handled by this connection
     * @see LocalPortForwarder
     */
    protected Collection<LocalPortForwarder> getPortForwarders() {
        return portForwarders;
    }

    /**
     * Can this connection be closed ?
     *
     * A connection can be closed if no sessions are running and
     * no tunnels are opened.
     *
     * @return true is returned if the connection can be closed.
     */
    public boolean canBeClosed() {
        if (portForwarders == null) {
            return true;
        }

        return portForwarders.size() == 0;
    }

    /**
     * Try to close this connection
     *
     * If softly is true, this connection will
     * be closed if no sessions or tunnels
     * are running. Otherwise the connection will be
     * closed anyway.
     *
     * @param softly If false, the connection will be close even if sessions or tunnels are running
     */
    public void close(boolean softly) {
        if ((portForwarders == null) || (portForwarders.size() == 0)) {
            connection.close();
        } else {
            if (!softly) {
                for (LocalPortForwarder lpf : portForwarders) {
                    try {
                        lpf.close();
                    } catch (IOException e) {
                        // Do nothing
                    }
                }
                for (Session session : sessions) {
                    session.close();
                }
                connection.close();
            }
        }
    }

    /**
     * Create a SSH-2 tunnel
     *
     * @param localPort local TCP port
     * @param distantHost distant host
     * @param distantPort distant port
     * @return the tunnel
     * @throws IOException If the tunnel cannot been created, an IOException
     * is thrown
     * @see {@link LocalPortForwarder}
     */
    public LocalPortForwarder createTunnel(int localPort, String distantHost,
        int distantPort) throws IOException {
        LocalPortForwarder lpf = connection.createLocalPortForwarder(localPort,
                distantHost, distantPort);
        portForwarders.add(lpf);
        return lpf;
    }
}
