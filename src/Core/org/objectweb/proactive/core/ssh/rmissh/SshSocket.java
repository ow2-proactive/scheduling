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
package org.objectweb.proactive.core.ssh.rmissh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import org.objectweb.proactive.core.config.PAProperties;
import static org.objectweb.proactive.core.ssh.SSH.logger;
import org.objectweb.proactive.core.ssh.SshParameters;
import org.objectweb.proactive.core.ssh.SshTunnel;
import org.objectweb.proactive.core.ssh.SshTunnelFactory;
import org.objectweb.proactive.core.ssh.TryCache;


/**
 * @author mlacage
 * We need to create a SshSocket class to associate the SshTunnel
 * and the Socket which uses the Tunnel. This allows the Java runtime
 * to automagically close the SshTunnels whenever the Socket is closed.
 */
public class SshSocket extends Socket {
    private SshTunnel _tunnel;
    private Socket _socket;
    static private TryCache _tryCache = null;

    static private TryCache getTryCache() {
        if (_tryCache == null) {
            _tryCache = new TryCache();
        }
        return _tryCache;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        logger.debug("finalizing SshSocket for tunnel " + _tunnel.getPort());
        SshTunnelFactory.reportUnusedTunnel(_tunnel);
        _tunnel = null;
        _socket.close();
        _socket = null;
    }

    public SshSocket(String host, int port) throws IOException {
        if (PAProperties.PA_SSH_TUNNELING_TRY_NORMAL_FIRST.isTrue() && getTryCache().needToTry(host, port)) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("try socket to " + host + ":" + port);
                }
                InetSocketAddress address = new InetSocketAddress(host, port);
                _socket = new Socket();
                _socket.connect(address, SshParameters.getConnectTimeout());
                getTryCache().recordTrySuccess(host, port);
                if (logger.isDebugEnabled()) {
                    logger.debug("success normal socket to " + host + ":" + port);
                }
                return;
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("failure normal socket to " + host + ":" + port);
                }
                getTryCache().recordTryFailure(host, port);
                _socket = null;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("try ssh socket to " + host + ":" + port);
        }
        _tunnel = SshTunnelFactory.createTunnel(host, port);
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", _tunnel.getPort());
        _socket = new Socket();
        _socket.connect(address, SshParameters.getConnectTimeout());
        if (logger.isDebugEnabled()) {
            logger.debug("Opened TCP connection 127.0.0.1:" + _tunnel.getPort() + " -> " + host + ":" + port);
        }
    }

    public void connect() throws IOException {
        //assert false;
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        // assert false;
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        // assert false;
    }

    @Override
    public InetAddress getInetAddress() {
        try {
            return _tunnel.getInetAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    @Override
    public InetAddress getLocalAddress() {
        return _socket.getLocalAddress();
    }

    @Override
    public int getPort() {
        return _tunnel.getPort();
    }

    @Override
    public int getLocalPort() {
        return _socket.getLocalPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return new InetSocketAddress(getInetAddress(), getPort());
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return new InetSocketAddress(getLocalAddress(), getLocalPort());
    }

    @Override
    public SocketChannel getChannel() {
        return _socket.getChannel();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return _socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return _socket.getOutputStream();
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        _socket.setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return _socket.getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        _socket.setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return _socket.getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        _socket.sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        _socket.setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return _socket.getOOBInline();
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        _socket.setSoTimeout(timeout);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return _socket.getSoTimeout();
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        _socket.setSendBufferSize(size);
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return _socket.getSendBufferSize();
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        _socket.setReceiveBufferSize(size);
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return _socket.getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        _socket.setKeepAlive(on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return _socket.getKeepAlive();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        _socket.setTrafficClass(tc);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return _socket.getTrafficClass();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        _socket.setReuseAddress(on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return _socket.getReuseAddress();
    }

    @Override
    public synchronized void close() throws IOException {
        _socket.close();
        try {
            SshTunnelFactory.reportUnusedTunnel(_tunnel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        _tunnel = null;
    }

    @Override
    public void shutdownInput() throws IOException {
        _socket.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        _socket.shutdownOutput();
    }

    @Override
    public String toString() {
        return _socket.toString();
    }

    @Override
    public boolean isConnected() {
        return _socket.isConnected();
    }

    @Override
    public boolean isBound() {
        return _socket.isBound();
    }

    @Override
    public boolean isClosed() {
        return _socket.isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return _socket.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return _socket.isOutputShutdown();
    }
}
