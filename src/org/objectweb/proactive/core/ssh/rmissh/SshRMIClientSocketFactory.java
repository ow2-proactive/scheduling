package org.objectweb.proactive.core.ssh.rmissh;

import java.net.Socket;
import java.io.IOException;
import java.rmi.server.RMIClientSocketFactory;

/**
 * @author mlacage
 */
public class SshRMIClientSocketFactory implements RMIClientSocketFactory, java.io.Serializable {
	
	public SshRMIClientSocketFactory () {}
	
	public Socket createSocket(String host, int port) throws IOException {
		Socket socket = new SshSocket (host, port);
		return socket;
	}
}
