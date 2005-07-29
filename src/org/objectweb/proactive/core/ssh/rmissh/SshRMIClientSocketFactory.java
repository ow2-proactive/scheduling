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

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        HostsInfos.setUserName(hostname,username);
    }
}
