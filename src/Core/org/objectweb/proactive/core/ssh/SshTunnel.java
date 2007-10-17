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

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import static org.objectweb.proactive.core.ssh.SSH.logger;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.URIBuilder;

import ch.ethz.ssh2.LocalPortForwarder;


/**
 *
 * This class represent a SSH Tunnel.
 *
 * It's a wrapper around a LocalPortForwarder and a SSHConnection object.
 * When creating a tunnel {@link SSHConnectionCache} is used to reuse
 * already existing SSH-2 connections.
 *
 * @see SSHConnection
 * @see LocalPortForwarder
 */
public class SshTunnel {
    private SSHConnection connection = null;
    private LocalPortForwarder lpf = null;
    private int localPort;
    private int distantPort;
    private String distantHost;

    /**
     * Open a SSH Tunnel between localhost and distantHost.
     *
     * If no SSH Connection to distantHost exists; a new Connection is opened.
     * Otherwise the connection is reused.
     *
     * @param distantHost
     *            the name of the machine to which a tunnel must be established.
     * @param distantPort
     *            the port number on the distant machine to which a tunnel must
     *            be established
     * @throws IOException
     *             an exception is thrown if either the authentication or the
     *             tunnel establishment fails.
     */
    public SshTunnel(String distantHost, int distantPort)
        throws IOException {
        String username = SshParameters.getSshUsername(distantHost);
        String sshPort = SshParameters.getSshPort();

        this.distantHost = distantHost;
        this.distantPort = distantPort;

        SSHConnectionCache scc = SSHConnectionCache.getSingleton();
        try {
            connection = scc.getConnection(username, distantHost, sshPort);
        } catch (IOException e) {
            logger.info("Connection to " + distantHost + ":" + sshPort +
                "cannot be opened");
            throw e;
        }

        int initialPort = ProActiveRandom.nextInt(65536 - 1024) + 1024;

        for (localPort = (initialPort == 65535) ? 1024 : (initialPort + 1);
                localPort != initialPort;
                localPort = (localPort == 65535) ? 1024 : (localPort + 1)) {
            logger.debug("initialPort:" + initialPort + " localPort:" +
                localPort);
            try {
                lpf = connection.createTunnel(localPort, distantHost,
                        distantPort);
                return;
            } catch (BindException e) {
                // Try another port
                if (logger.isDebugEnabled()) {
                    logger.debug("The port " + localPort + " is not free");
                }
            }
        }

        // Looped all over the port range
        logger.error(
            "No free local port can be found to establish a new SSH-2 tunnel");
        throw new BindException("No free local port found");
    }

    public void realClose() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Closing tunnel from " +
                URIBuilder.getLocalAddress().getHostAddress() + ":" +
                localPort + "to " + distantHost + ":" + distantPort);
        }
        lpf.close();
        lpf = null;
        connection.close(true);
    }

    /**
     * This method returns the local port number which can be used to access
     * this tunnel. This method cannot fail.
     */
    public int getPort() {
        return localPort;
    }

    public InetAddress getInetAddress() throws java.net.UnknownHostException {
        return InetAddress.getByName(distantHost);
    }

    public String getDistantHost() {
        return distantHost;
    }

    public int getDistantPort() {
        return distantPort;
    }
}
