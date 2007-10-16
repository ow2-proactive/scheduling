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
import java.util.HashMap;
import static org.objectweb.proactive.core.ssh.SSH.logger;
public class SSHConnectionCache {
    private static SSHConnectionCache singleton = null;
    private HashMap<String, SSHConnection> hash;

    static public SSHConnectionCache getSingleton() {
        if (singleton == null) {
            singleton = new SSHConnectionCache();
        }

        return singleton;
    }

    private SSHConnectionCache() {
        hash = new HashMap<String, SSHConnection>();
    }

    public SSHConnection getConnection(String username, String hostname,
        String port) throws IOException {
        String id = buildIdentifier(username, hostname, port);
        SSHConnection conn = hash.get(id);
        if (conn == null) {
            conn = new SSHConnection(username, hostname, port);
            hash.put(id, conn);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Connection to " + hostname + ":" + port +
                    " already opened");
            }
        }
        return conn;
    }

    private static String buildIdentifier(String user, String host, String port) {
        return port + user + host;
    }
}
