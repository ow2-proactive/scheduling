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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.ssh.rmissh;

import java.io.*;
import java.net.*;
import java.nio.channels.*;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ssh.SshParameters;
import org.objectweb.proactive.core.ssh.SshTunnel;
import org.objectweb.proactive.core.ssh.SshTunnelFactory;
import org.objectweb.proactive.core.ssh.TryCache;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author mlacage
 * We need to create a SshSocket class to associate the SshTunnel
 * and the Socket which uses the Tunnel. This allows the Java runtime
 * to automagically close the SshTunnels whenever the Socket is closed.
 */
public class SshSocket extends Socket {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SSH);
    private SshTunnel _tunnel;
    private Socket _socket;
    static private TryCache _tryCache = null;

    static private TryCache getTryCache() {
        if (_tryCache == null) {
            _tryCache = new TryCache();
        }
        return _tryCache;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        logger.debug("finalizing SshSocket for tunnel " + _tunnel.getPort());
        SshTunnelFactory.reportUnusedTunnel(_tunnel);
        _tunnel = null;
        _socket.close();
        _socket = null;
    }

    public SshSocket(String host, int port) throws IOException {
        logger.debug("try socket to " + host + ":" + port);
        if (SshParameters.getTryNormalFirst() &&
                getTryCache().needToTry(host, port)) {
            try {
                InetSocketAddress address = new InetSocketAddress(host, port);
                _socket = new Socket();
                _socket.connect(address, SshParameters.getConnectTimeout());
                getTryCache().recordTrySuccess(host, port);
                logger.debug("success normal socket to " + host + ":" + port);
                return;
            } catch (Exception e) {
                logger.debug("failure normal socket to " + host + ":" + port);
                getTryCache().recordTryFailure(host, port);
                _socket = null;
            }
        }
        logger.debug("try ssh socket to " + host + ":" + port);
        _tunnel = SshTunnelFactory.createTunnel(host, port);
        InetSocketAddress address = new InetSocketAddress("127.0.0.1",
                _tunnel.getPort());
        _socket = new Socket();
        _socket.connect(address, SshParameters.getConnectTimeout());
        logger.debug("Opened TCP connection 127.0.0.1:" + _tunnel.getPort() +
            " -> " + host + ":" + port);
    }

    public void connect() throws IOException {
        //assert false;
    }

    public void connect(SocketAddress endpoint, int timeout)
        throws IOException {
        // assert false;
    }

    public void bind(SocketAddress bindpoint) throws IOException {
        // assert false;
    }

    public InetAddress getInetAddress() {
        try {
            return _tunnel.getInetAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public InetAddress getLocalAddress() {
        return _socket.getLocalAddress();
    }

    public int getPort() {
        return _tunnel.getPort();
    }

    public int getLocalPort() {
        return _socket.getLocalPort();
    }

    public SocketAddress getRemoteSocketAddress() {
        return new InetSocketAddress(getInetAddress(), getPort());
    }

    public SocketAddress getLocalSocketAddress() {
        return new InetSocketAddress(getLocalAddress(), getLocalPort());
    }

    public SocketChannel getChannel() {
        return _socket.getChannel();
    }

    public InputStream getInputStream() throws IOException {
        return _socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return _socket.getOutputStream();
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        _socket.setTcpNoDelay(on);
    }

    public boolean getTcpNoDelay() throws SocketException {
        return _socket.getTcpNoDelay();
    }

    public void setSoLinger(boolean on, int linger) throws SocketException {
        _socket.setSoLinger(on, linger);
    }

    public int getSoLinger() throws SocketException {
        return _socket.getSoLinger();
    }

    public void sendUrgentData(int data) throws IOException {
        _socket.sendUrgentData(data);
    }

    public void setOOBInline(boolean on) throws SocketException {
        _socket.setOOBInline(on);
    }

    public boolean getOOBInline() throws SocketException {
        return _socket.getOOBInline();
    }

    public synchronized void setSoTimeout(int timeout)
        throws SocketException {
        _socket.setSoTimeout(timeout);
    }

    public synchronized int getSoTimeout() throws SocketException {
        return _socket.getSoTimeout();
    }

    public synchronized void setSendBufferSize(int size)
        throws SocketException {
        _socket.setSendBufferSize(size);
    }

    public synchronized int getSendBufferSize() throws SocketException {
        return _socket.getSendBufferSize();
    }

    public synchronized void setReceiveBufferSize(int size)
        throws SocketException {
        _socket.setReceiveBufferSize(size);
    }

    public synchronized int getReceiveBufferSize() throws SocketException {
        return _socket.getReceiveBufferSize();
    }

    public void setKeepAlive(boolean on) throws SocketException {
        _socket.setKeepAlive(on);
    }

    public boolean getKeepAlive() throws SocketException {
        return _socket.getKeepAlive();
    }

    public void setTrafficClass(int tc) throws SocketException {
        _socket.setTrafficClass(tc);
    }

    public int getTrafficClass() throws SocketException {
        return _socket.getTrafficClass();
    }

    public void setReuseAddress(boolean on) throws SocketException {
        _socket.setReuseAddress(on);
    }

    public boolean getReuseAddress() throws SocketException {
        return _socket.getReuseAddress();
    }

    public synchronized void close() throws IOException {
        _socket.close();
        try {
            SshTunnelFactory.reportUnusedTunnel(_tunnel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        _tunnel = null;
    }

    public void shutdownInput() throws IOException {
        _socket.shutdownInput();
    }

    public void shutdownOutput() throws IOException {
        _socket.shutdownOutput();
    }

    public String toString() {
        return _socket.toString();
    }

    public boolean isConnected() {
        return _socket.isConnected();
    }

    public boolean isBound() {
        return _socket.isBound();
    }

    public boolean isClosed() {
        return _socket.isClosed();
    }

    public boolean isInputShutdown() {
        return _socket.isInputShutdown();
    }

    public boolean isOutputShutdown() {
        return _socket.isOutputShutdown();
    }
}
