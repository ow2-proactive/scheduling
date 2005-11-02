/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.ssh.rmissh;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.server.RMIClientSocketFactory;

import org.objectweb.proactive.core.util.HostsInfos;


/**
 * @author mlacage
 */
public class SshRMIClientSocketFactory implements RMIClientSocketFactory,
    java.io.Serializable {
    String username;
    String hostname;

    public SshRMIClientSocketFactory() {
        this.username = System.getProperty("user.name");
        try {
            this.hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            this.hostname = "locahost";
        }
    }

    public Socket createSocket(String host, int port) throws IOException {
        String realName = HostsInfos.getSecondaryName(host);
        Socket socket = new SshSocket(realName, port);
        return socket;
    }

    public boolean equals(Object obj) {
        // the equals method is class based, since all instances are functionally equivalent.
        // We could if needed compare on an instance basic for instance with the host and port
        // Same for hashCode
        return this.getClass().equals(obj.getClass());
    }

    public int hashCode() {
        return this.getClass().hashCode();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        HostsInfos.setUserName(hostname, username);
    }
}
