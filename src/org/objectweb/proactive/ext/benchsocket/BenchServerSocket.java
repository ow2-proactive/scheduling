package org.objectweb.proactive.ext.benchsocket;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class BenchServerSocket extends ServerSocket {
	private BenchFactory parent; 
	
    public BenchServerSocket() throws IOException {
        super();
    }

    public BenchServerSocket(int port, BenchFactory parent) throws IOException {
        super(port);
        this.parent = parent;
    }

    public BenchServerSocket(int port, InetAddress localAddress, BenchFactory parent)
        throws IOException {
        super(port, -1, localAddress);
        this.parent = parent; 
    }

    public Socket accept() throws IOException {
        Socket s = new Socket();
        implAccept(s);
        return new BenchClientSocket(s, parent);
    }
}
