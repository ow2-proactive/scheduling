package org.objectweb.proactive.ext.benchsocket;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;


public class BenchServerSocket extends ServerSocket {
    public BenchServerSocket() throws IOException {
        super();
    }

    public BenchServerSocket(int port) throws IOException {
        super(port);
    }

    public Socket accept() throws IOException {
        Socket s = new Socket();
        implAccept(s);
        return new BenchClientSocket(s); 
    }
}
