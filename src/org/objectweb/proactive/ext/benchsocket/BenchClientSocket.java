package org.objectweb.proactive.ext.benchsocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import java.nio.channels.SocketChannel;


/**
 *
 * @author fabrice
 *
 * A wrapper to a real socket
 * to measure the size of data sent
 */
public class BenchClientSocket extends Socket {
    private static int counter;
    private Socket realSocket;
    private BenchOutputStream output;
    private BenchInputStream input;
    private int number;

    public BenchClientSocket() throws IOException {
        this.realSocket = new Socket();
        //        this.output = this.createOutputStream();
        //        this.input = this.createInputStream();
        this.createStreams();
    }

    public BenchClientSocket(Socket s) throws IOException {
    	this.realSocket =s;
    	//        this.output = this.createOutputStream();
    	//        this.input = this.createInputStream();
    	this.createStreams();
    }
    
    public BenchClientSocket(String host, int port) throws IOException {
        this.realSocket = new Socket(host, port);
        //        this.output = this.createOutputStream();
        //        this.input = this.createInputStream();
        this.createStreams();
    }

    protected BenchOutputStream createOutputStream() throws IOException {
        synchronized (BenchClientSocket.class) {
            this.number = BenchClientSocket.counter;
            BenchClientSocket.counter++;
        }
        return new BenchOutputStream(realSocket.getOutputStream(), this.number);
    }

    protected BenchInputStream createInputStream() throws IOException {
        synchronized (BenchClientSocket.class) {
            this.number = BenchClientSocket.counter;
            BenchClientSocket.counter++;
        }
        return new BenchInputStream(realSocket.getInputStream(), this.number);
    }

    public void createStreams() throws IOException {
        synchronized (BenchClientSocket.class) {
            this.number = BenchClientSocket.counter;
            BenchClientSocket.counter++;
        }
        this.output = new BenchOutputStream(realSocket.getOutputStream(),
                this.number);
        ;
        this.input = new BenchInputStream(realSocket.getInputStream(),
                this.number);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#bind(java.net.SocketAddress)
     */
    public void bind(SocketAddress bindpoint) throws IOException {
        //  Auto-generated method stub
        this.realSocket.bind(bindpoint);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#close()
     */
    public synchronized void close() throws IOException {
        //  Auto-generated method stub
        this.realSocket.close();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#connect(java.net.SocketAddress, int)
     */
    public void connect(SocketAddress endpoint, int timeout)
        throws IOException {
        this.realSocket.connect(endpoint, timeout);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#connect(java.net.SocketAddress)
     */
    public void connect(SocketAddress endpoint) throws IOException {
        this.realSocket.connect(endpoint);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getChannel()
     */
    public SocketChannel getChannel() {
        return this.realSocket.getChannel();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getInetAddress()
     */
    public InetAddress getInetAddress() {
        return this.realSocket.getInetAddress();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        //    	System.out.println("getInputtStream()");
        //  return this.realSocket.getInputStream();
        return this.input;
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getKeepAlive()
     */
    public boolean getKeepAlive() throws SocketException {
        return this.realSocket.getKeepAlive();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getLocalAddress()
     */
    public InetAddress getLocalAddress() {
        return this.realSocket.getLocalAddress();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getLocalPort()
     */
    public int getLocalPort() {
        return this.realSocket.getLocalPort();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getLocalSocketAddress()
     */
    public SocketAddress getLocalSocketAddress() {
        return this.realSocket.getLocalSocketAddress();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getOOBInline()
     */
    public boolean getOOBInline() throws SocketException {
        return this.realSocket.getOOBInline();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {
        //  return this.realSocket.getOutputStream();
        //    	System.out.println("getOutputStream()");
        return this.output;
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getPort()
     */
    public int getPort() {
        return this.realSocket.getPort();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getReceiveBufferSize()
     */
    public synchronized int getReceiveBufferSize() throws SocketException {
        return this.realSocket.getReceiveBufferSize();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getRemoteSocketAddress()
     */
    public SocketAddress getRemoteSocketAddress() {
        return this.realSocket.getRemoteSocketAddress();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getReuseAddress()
     */
    public boolean getReuseAddress() throws SocketException {
        return this.realSocket.getReuseAddress();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getSendBufferSize()
     */
    public synchronized int getSendBufferSize() throws SocketException {
        return this.realSocket.getSendBufferSize();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getSoLinger()
     */
    public int getSoLinger() throws SocketException {
        return this.realSocket.getSoLinger();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getSoTimeout()
     */
    public synchronized int getSoTimeout() throws SocketException {
        return this.realSocket.getSoTimeout();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getTcpNoDelay()
     */
    public boolean getTcpNoDelay() throws SocketException {
        return this.realSocket.getTcpNoDelay();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#getTrafficClass()
     */
    public int getTrafficClass() throws SocketException {
        return this.realSocket.getTrafficClass();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#isBound()
     */
    public boolean isBound() {
        return this.realSocket.isBound();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#isClosed()
     */
    public boolean isClosed() {
        return this.realSocket.isClosed();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#isConnected()
     */
    public boolean isConnected() {
        return this.realSocket.isConnected();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#isInputShutdown()
     */
    public boolean isInputShutdown() {
        return this.realSocket.isInputShutdown();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#isOutputShutdown()
     */
    public boolean isOutputShutdown() {
        return this.realSocket.isOutputShutdown();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#sendUrgentData(int)
     */
    public void sendUrgentData(int data) throws IOException {
        this.realSocket.sendUrgentData(data);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setKeepAlive(boolean)
     */
    public void setKeepAlive(boolean on) throws SocketException {
        this.realSocket.setKeepAlive(on);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setOOBInline(boolean)
     */
    public void setOOBInline(boolean on) throws SocketException {
        this.realSocket.setOOBInline(on);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setReceiveBufferSize(int)
     */
    public synchronized void setReceiveBufferSize(int size)
        throws SocketException {
        this.realSocket.setReceiveBufferSize(size);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setReuseAddress(boolean)
     */
    public void setReuseAddress(boolean on) throws SocketException {
        this.realSocket.setReuseAddress(on);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setSendBufferSize(int)
     */
    public synchronized void setSendBufferSize(int size)
        throws SocketException {
        this.realSocket.setSendBufferSize(size);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setSoLinger(boolean, int)
     */
    public void setSoLinger(boolean on, int linger) throws SocketException {
        this.realSocket.setSoLinger(on, linger);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setSoTimeout(int)
     */
    public synchronized void setSoTimeout(int timeout)
        throws SocketException {
        this.realSocket.setSoTimeout(timeout);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setTcpNoDelay(boolean)
     */
    public void setTcpNoDelay(boolean on) throws SocketException {
        this.realSocket.setTcpNoDelay(on);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#setTrafficClass(int)
     */
    public void setTrafficClass(int tc) throws SocketException {
        this.realSocket.setTrafficClass(tc);
    }

    /* (non-Javadoc)
     * @see java.net.Socket#shutdownInput()
     */
    public void shutdownInput() throws IOException {
        this.realSocket.shutdownInput();
    }

    /* (non-Javadoc)
     * @see java.net.Socket#shutdownOutput()
     */
    public void shutdownOutput() throws IOException {
        this.realSocket.shutdownOutput();
    }
}
