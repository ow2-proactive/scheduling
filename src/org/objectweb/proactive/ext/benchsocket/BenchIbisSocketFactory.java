package org.objectweb.proactive.ext.benchsocket;

import ibis.util.IbisSocketFactoryInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class BenchIbisSocketFactory extends BenchFactory
    implements IbisSocketFactoryInterface {
    public Socket createSocket(InetAddress dest, int port, InetAddress localIP,
        long timeoutMillis) throws IOException {
        return new BenchClientSocket(dest, port, this);
    }

    public ServerSocket createServerSocket(int port, InetAddress localAddress,
        boolean retry) throws IOException {
        return new BenchServerSocket(port, localAddress, this);
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
}
