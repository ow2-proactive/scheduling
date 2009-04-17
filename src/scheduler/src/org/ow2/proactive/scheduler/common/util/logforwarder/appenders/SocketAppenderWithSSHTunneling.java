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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util.logforwarder.appenders;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.net.SocketAppender;
import org.objectweb.proactive.core.ssh.SSHConnection;


/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 *  Initial developer(s):               Activeeon Team
 *                        http://www.activeeon.com
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
public class SocketAppenderWithSSHTunneling extends SocketAppender {

    // look for an open port from ...
    private static final int BASE_PORT_SEARCH = 1025;
    // stop looking for an open port after ...
    private static final int MAX_PORT_SEARCH_TRIES = 65535 - BASE_PORT_SEARCH;

    // connection to the log server
    SSHConnection sshConnection;

    /**
     * Create a new SocketAppenderWithSSHTunneling with the current username on remote ssh port 22.
     * @param remoteHost the host running the log server.
     * @param port the remote listening port.
     * @throws IOException
     */
    public SocketAppenderWithSSHTunneling(String remoteHost, int port) throws IOException {
        this(System.getProperty("user.name"), remoteHost, port, 22);
    }

    /**
     * Create a new SocketAppenderWithSSHTunneling with the current username.
     * @param remoteHost the host running the log server.
     * @param port the remote listening port.
     * @param remoteSSHPort the remote ssh port (usually 22).
     * @throws IOException
     */
    public SocketAppenderWithSSHTunneling(String remoteHost, int port, int remoteSSHPort) throws IOException {
        this(System.getProperty("user.name"), remoteHost, port, remoteSSHPort);
    }

    /**
     * Create a new SocketAppenderWithSSHTunneling.
     * @param username username for ssh connection.
     * @param remoteHost the host running the log server.
     * @param port the remote listening port.
     * @param remoteSSHPort the remote ssh port (usually 22).
     * @throws IOException
     */
    public SocketAppenderWithSSHTunneling(String username, String remoteHost, int port, int remoteSSHPort)
            throws IOException {
        super(remoteHost, port);

        InetAddress localhost = InetAddress.getLocalHost();
        sshConnection = new SSHConnection(username, remoteHost, remoteSSHPort);

        int localPort = BASE_PORT_SEARCH;
        int nbTries = 0;
        boolean stop = false;

        while (!stop && nbTries < MAX_PORT_SEARCH_TRIES) {
            try {
                sshConnection.createTunnel(localPort, remoteHost, port);
                stop = true;
            } catch (java.net.BindException e) {
                nbTries++;
                localPort++;
            }
        }
        this.setRemoteHost(localhost.getHostAddress());
        this.setPort(localPort);
    }

    public void close() {
        super.close();
        try {
            this.sshConnection.close(false);
        } catch (Exception e) {
            //well, if we are here it's difficult to log the error somewhere ...
            e.printStackTrace();
        }
    }
}