package org.objectweb.proactive.ext.benchsocket;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

public class BenchSocketFactory implements RMIServerSocketFactory,
RMIClientSocketFactory, Serializable { 


	public ServerSocket createServerSocket(int port) throws IOException {	
		return new ServerSocket(port);
	}

	
	public Socket createSocket(String host, int port) throws IOException {	
	System.out.println("================= Creating BenchClientSocket =================");
	 return new BenchClientSocket(host, port);
	//	return new Socket(host, port);
	}

}
