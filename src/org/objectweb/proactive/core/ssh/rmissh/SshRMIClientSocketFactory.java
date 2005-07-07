package org.objectweb.proactive.core.ssh.rmissh;

import java.io.IOException;

import java.net.Socket;

import java.rmi.server.RMIClientSocketFactory;


/**
 * @author mlacage
 */
public class SshRMIClientSocketFactory implements RMIClientSocketFactory,
    java.io.Serializable {
    public SshRMIClientSocketFactory() {
    }

    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = new SshSocket(host, port);
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
}
