package org.objectweb.proactive.core.ssh.rmissh;

import java.io.IOException;

import java.net.ServerSocket;

import java.rmi.server.RMIServerSocketFactory;


public class SshRMIServerSocketFactory implements RMIServerSocketFactory,
    java.io.Serializable {
    public SshRMIServerSocketFactory() {
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
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
