package org.objectweb.proactive.ext.benchsocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import ibis.util.IbisSocketFactory;


public class BenchIbisSocketFactory extends IbisSocketFactory
    implements BenchFactoryInterface {
    protected static ArrayList streamList = new ArrayList();

    public void addStream(BenchStream s) {
        synchronized (streamList) {
            streamList.add(s);
        }
    }

    public static void dumpStreamIntermediateResults() {
        synchronized (streamList) {
            Iterator it = streamList.iterator();
            while (it.hasNext()) {
                ((BenchStream) it.next()).dumpIntermediateResults();
            }
        }
    }

    public Socket accept(ServerSocket a) throws IOException {
        Socket s = a.accept();
        return s;
    }

    public void close(InputStream in, OutputStream out, Socket s) {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.flush();
                out.close();
            }
            if (s != null) {
                s.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int allocLocalPort() {
        return 0;
    }

    public ServerSocket createServerSocket(int port, int backlog,
        InetAddress addr) throws IOException {
        return new BenchServerSocket(port, addr, this);
    }

    public Socket createSocket(InetAddress rAddr, int rPort)
        throws IOException {
        return new BenchClientSocket(rAddr, rPort, this);
    }

    public Socket createSocket(InetAddress dest, int port, InetAddress localIP,
        long timeoutMillis) throws IOException {
        return new BenchClientSocket(dest, port, this);
    }

    public ServerSocket createServerSocket(int port, InetAddress localAddress,
        boolean retry) throws IOException {
        return new BenchServerSocket(port, localAddress, this);
    }
}
