/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package benchsocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;


public class BenchClientSocket extends Socket {
    private static int counter;
    private Socket realSocket;
    private BenchOutputStream output;
    private BenchInputStream input;
    private int number;
    private BenchFactoryInterface parent;

    public BenchClientSocket() throws IOException {
        synchronized (BenchClientSocket.class) {
            BenchClientSocket.counter++;
            this.number = BenchClientSocket.counter;
            this.realSocket = new Socket();
            this.createStreams();
        }
    }

    public BenchClientSocket(Socket s, BenchFactoryInterface parent)
        throws IOException {
        synchronized (BenchClientSocket.class) {
            BenchClientSocket.counter++;
            this.number = BenchClientSocket.counter;
            this.realSocket = s;
            this.parent = parent;
            this.createStreams();
        }
    }

    public BenchClientSocket(String host, int port, BenchFactoryInterface parent)
        throws IOException {
        synchronized (BenchClientSocket.class) {
            BenchClientSocket.counter++;
            this.number = BenchClientSocket.counter;
            this.realSocket = new Socket(host, port);
            this.parent = parent;
            this.createStreams();
        }
    }

    public BenchClientSocket(InetAddress address, int port,
        BenchFactoryInterface parent) throws IOException {
        synchronized (BenchClientSocket.class) {
            BenchClientSocket.counter++;
            this.number = BenchClientSocket.counter;
            this.realSocket = new Socket(address, port);
            this.parent = parent;
            this.createStreams();
        }
    }

    public void createStreams() throws IOException {
        synchronized (BenchClientSocket.class) {
            this.number = BenchClientSocket.counter;
        }

        this.output = new BenchOutputStream(realSocket.getOutputStream(),
                this.number, this);
        this.parent.addStream(this.output);

        this.input = new BenchInputStream(realSocket.getInputStream(),
                this.number, this);
        this.parent.addStream(this.input);
    }

    @Override
    public String toString() {
        return this.realSocket.toString();
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        this.realSocket.bind(bindpoint);
    }

    @Override
    public synchronized void close() throws IOException {
        if (this.input != null) {
            this.input.close();
            this.input = null;
        }
        if (this.output != null) {
            this.output.close();
            this.output = null;
        }
        if (this.realSocket != null) {
            this.realSocket.close();
            this.realSocket = null;
        }
    }

    /* (non-Javadoc)
     * @see java.net.Socket#connect(java.net.SocketAddress, int)
     */
    @Override
    public void connect(SocketAddress endpoint, int timeout)
        throws IOException {
        this.realSocket.connect(endpoint, timeout);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#connect(java.net.SocketAddress)
     */
    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        this.realSocket.connect(endpoint);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getChannel()
     */
    @Override
    public SocketChannel getChannel() {
        return this.realSocket.getChannel();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getInetAddress()
     */
    @Override
    public InetAddress getInetAddress() {
        return this.realSocket.getInetAddress();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        //    	System.out.println("getInputtStream()");
        //  return this.realSocket.getInputStream();
        return this.input;
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getKeepAlive()
     */
    @Override
    public boolean getKeepAlive() throws SocketException {
        return this.realSocket.getKeepAlive();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getLocalAddress()
     */
    @Override
    public InetAddress getLocalAddress() {
        return this.realSocket.getLocalAddress();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getLocalPort()
     */
    @Override
    public int getLocalPort() {
        return this.realSocket.getLocalPort();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getLocalSocketAddress()
     */
    @Override
    public SocketAddress getLocalSocketAddress() {
        return this.realSocket.getLocalSocketAddress();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getOOBInline()
     */
    @Override
    public boolean getOOBInline() throws SocketException {
        return this.realSocket.getOOBInline();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        //  return this.realSocket.getOutputStream();
        //    	System.out.println("getOutputStream()");
        return this.output;
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getPort()
     */
    @Override
    public int getPort() {
        return this.realSocket.getPort();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getReceiveBufferSize()
     */
    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return this.realSocket.getReceiveBufferSize();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getRemoteSocketAddress()
     */
    @Override
    public SocketAddress getRemoteSocketAddress() {
        return this.realSocket.getRemoteSocketAddress();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getReuseAddress()
     */
    @Override
    public boolean getReuseAddress() throws SocketException {
        return this.realSocket.getReuseAddress();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getSendBufferSize()
     */
    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return this.realSocket.getSendBufferSize();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getSoLinger()
     */
    @Override
    public int getSoLinger() throws SocketException {
        return this.realSocket.getSoLinger();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getSoTimeout()
     */
    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return this.realSocket.getSoTimeout();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getTcpNoDelay()
     */
    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return this.realSocket.getTcpNoDelay();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getTrafficClass()
     */
    @Override
    public int getTrafficClass() throws SocketException {
        return this.realSocket.getTrafficClass();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#isBound()
     */
    @Override
    public boolean isBound() {
        return this.realSocket.isBound();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#isClosed()
     */
    @Override
    public boolean isClosed() {
        return this.realSocket.isClosed();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#isConnected()
     */
    @Override
    public boolean isConnected() {
        return this.realSocket.isConnected();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#isInputShutdown()
     */
    @Override
    public boolean isInputShutdown() {
        return this.realSocket.isInputShutdown();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#isOutputShutdown()
     */
    @Override
    public boolean isOutputShutdown() {
        return this.realSocket.isOutputShutdown();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#sendUrgentData(int)
     */
    @Override
    public void sendUrgentData(int data) throws IOException {
        this.realSocket.sendUrgentData(data);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setKeepAlive(boolean)
     */
    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        this.realSocket.setKeepAlive(on);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setOOBInline(boolean)
     */
    @Override
    public void setOOBInline(boolean on) throws SocketException {
        this.realSocket.setOOBInline(on);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setReceiveBufferSize(int)
     */
    @Override
    public synchronized void setReceiveBufferSize(int size)
        throws SocketException {
        this.realSocket.setReceiveBufferSize(size);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setReuseAddress(boolean)
     */
    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        this.realSocket.setReuseAddress(on);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setSendBufferSize(int)
     */
    @Override
    public synchronized void setSendBufferSize(int size)
        throws SocketException {
        this.realSocket.setSendBufferSize(size);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setSoLinger(boolean, int)
     */
    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        this.realSocket.setSoLinger(on, linger);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setSoTimeout(int)
     */
    @Override
    public synchronized void setSoTimeout(int timeout)
        throws SocketException {
        this.realSocket.setSoTimeout(timeout);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setTcpNoDelay(boolean)
     */
    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        this.realSocket.setTcpNoDelay(on);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setTrafficClass(int)
     */
    @Override
    public void setTrafficClass(int tc) throws SocketException {
        this.realSocket.setTrafficClass(tc);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#shutdownInput()
     */
    @Override
    public void shutdownInput() throws IOException {
        this.realSocket.shutdownInput();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#shutdownOutput()
     */
    @Override
    public void shutdownOutput() throws IOException {
        this.realSocket.shutdownOutput();
    }
}
