package org.objectweb.proactive.ext.benchsocket;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;


public class BenchSocketFactory extends BenchFactory implements RMIServerSocketFactory,
    RMIClientSocketFactory, Serializable {
    
	//protected ArrayList streamList;
	
	protected static boolean measure=true;
		
	public ServerSocket createServerSocket(int port) throws IOException {
        return new BenchServerSocket(port, this); //ServerSocket(port);
    }

    public Socket createSocket(String host, int port) throws IOException {
        return new BenchClientSocket(host, port, this);
    }
     
    public static void startMeasure() {
    	BenchSocketFactory.measure = true;
    }
    
    public static void endMeasure() {
    	BenchSocketFactory.measure = false;
    }
    
    
}
