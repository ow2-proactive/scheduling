package org.objectweb.proactive.core.ssh.rmissh;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

public class SshRMIServerSocketFactory implements RMIServerSocketFactory, java.io.Serializable {
	
	public SshRMIServerSocketFactory () {}

	public ServerSocket createServerSocket(int port) throws IOException {
		return new ServerSocket (port);
	}

}
