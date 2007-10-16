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
package org.objectweb.proactive.core.ssh.httpssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.ssh.SshParameters;
import org.objectweb.proactive.core.ssh.SshTunnel;
import org.objectweb.proactive.core.ssh.SshTunnelFactory;
import org.objectweb.proactive.core.ssh.TryCache;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author mlacage
 */
public class HttpSshUrlConnection extends java.net.HttpURLConnection {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SSH);
    private SshTunnel _tunnel;
    private HttpURLConnection _httpConnection;
    private java.util.Hashtable<String, List<String>> _properties;
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
        SshTunnelFactory.reportUnusedTunnel(_tunnel);
    }

    public HttpSshUrlConnection(java.net.URL u) throws IOException {
        super(u);
        _properties = new Hashtable<String, List<String>>();
    }

    private void checkNotConnected() throws IllegalStateException {
        if (connected) {
            throw new IllegalStateException("already connected");
        }
    }

    private void checkNullKey(String str) throws NullPointerException {
        if (str == null) {
            throw new NullPointerException("null key");
        }
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        checkNotConnected();
        return _properties;
    }

    @Override
    public String getRequestProperty(String key) {
        checkNotConnected();
        if (key == null) {
            return null;
        }
        ArrayList list = (ArrayList) _properties.get(key);
        String retval = null;
        if (list != null) {
            retval = (String) list.get(0);
        }
        return retval;
    }

    @Override
    public void setRequestProperty(String key, String value) {
        checkNotConnected();
        checkNullKey(key);
        ArrayList<String> list = new ArrayList<String>();
        list.add(value);
        _properties.put(key, list);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        checkNotConnected();
        checkNullKey(key);
        List<String> list = _properties.get(key);
        if (list == null) {
            list = new ArrayList<String>();
        }
        list.add(value);
        _properties.put(key, list);
    }

    @Override
    public void setInstanceFollowRedirects(boolean followRedirects) {
        super.setInstanceFollowRedirects(followRedirects);
        if (_httpConnection != null) {
            _httpConnection.setInstanceFollowRedirects(followRedirects);
        }
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        super.setRequestMethod(method);
        if (_httpConnection != null) {
            _httpConnection.setRequestMethod(method);
        }
    }

    @Override
    public int getResponseCode() throws IOException {
        ensureTunnel();
        return _httpConnection.getResponseCode();
    }

    @Override
    public void connect() throws IOException {
        ensureTunnel();
        _httpConnection.connect();
        connected = true;
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public boolean usingProxy() {
        if (_httpConnection != null) {
            return _httpConnection.usingProxy();
        } else {
            return false;
        }
    }

    @Override
    public InputStream getErrorStream() {
        if (!connected) {
            return null;
        } else {
            // if we are connected, _httpConnection is a valid field.
            return _httpConnection.getErrorStream();
        }
    }

    @Override
    public String getHeaderField(String name) {
        try {
            ensureTunnel();
            return _httpConnection.getHeaderField(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getHeaderField(int n) {
        try {
            ensureTunnel();
            return _httpConnection.getHeaderField(n);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getHeaderFieldKey(int n) {
        try {
            ensureTunnel();
            return _httpConnection.getHeaderFieldKey(n);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        try {
            ensureTunnel();
            return _httpConnection.getHeaderFields();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        ensureTunnel();
        return _httpConnection.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        ensureTunnel();
        return _httpConnection.getOutputStream();
    }

    @Override
    public String toString() {
        return HttpSshUrlConnection.class.getName() + ":" + url.toString();
    }

    private void ensureSetup(HttpURLConnection connection) {
        connection.setDoInput(getDoInput());
        connection.setDoOutput(getDoOutput());
        connection.setAllowUserInteraction(getAllowUserInteraction());
        connection.setIfModifiedSince(getIfModifiedSince());
        connection.setUseCaches(getUseCaches());

        connection.setInstanceFollowRedirects(getInstanceFollowRedirects());
        try {
            connection.setRequestMethod(getRequestMethod());
        } catch (Exception e) {
            e.printStackTrace();
        }

        java.util.Set set = _properties.entrySet();
        for (java.util.Iterator i = set.iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            ArrayList values = (ArrayList) entry.getValue();
            for (java.util.Iterator j = values.iterator(); j.hasNext();) {
                String val = (String) j.next();
                try {
                    connection.addRequestProperty(key, val);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void ensureTunnel() throws IOException {
        if (_httpConnection != null) {
            return;
        }
        java.net.URL u = getURL();
        logger.debug("create http " + url.toString());
        String host = u.getHost();
        int port = u.getPort();
        String path = u.getPath();
        if (PAProperties.PA_SSH_TUNNELING_TRY_NORMAL_FIRST.isTrue() &&
                getTryCache().needToTry(host, port)) {
            if (!getTryCache().everTried(host, port)) {
                try {
                    logger.debug("try http socket probing");
                    InetSocketAddress address = new InetSocketAddress(host, port);
                    Socket socket = new Socket();
                    socket.connect(address, SshParameters.getConnectTimeout());
                    socket.close();
                    logger.debug("success http socket probing");
                } catch (Exception e) {
                    logger.debug("failure http socket probing");
                    getTryCache().recordTryFailure(host, port);
                }
            }
            if (getTryCache().needToTry(host, port)) {
                try {
                    java.net.URL httpURL = new java.net.URL("http://" + host +
                            ":" +
                            port // uncomment the following line and comment the one above to make sure
                                 // you test that connections fallback to tunneling if the main
                                 // connection fails.
                                 //+ 1 
                             +path);
                    logger.debug("try http not tunneled");
                    _httpConnection = (HttpURLConnection) httpURL.openConnection();
                    ensureSetup(_httpConnection);
                    _httpConnection.connect();
                    logger.debug("success http not tunneled ");
                    connected = true;
                    getTryCache().recordTrySuccess(host, port);
                    return;
                } catch (Exception e) {
                    getTryCache().recordTryFailure(host, port);
                    logger.debug("failure http not tunneled ");
                }
            }
        }
        logger.debug("try http ssh tunneled");
        _tunnel = SshTunnelFactory.createTunnel(host, port);
        java.net.URL httpURL = new java.net.URL("http://127.0.0.1:" +
                _tunnel.getPort() + path);
        _httpConnection = (HttpURLConnection) httpURL.openConnection();
        ensureSetup(_httpConnection);
        logger.debug("Opened http connection through tunnel 127.0.0.1:" +
            _tunnel.getPort() + " -> " + host + ":" + port + " ressource " +
            path + " -- " + _httpConnection.toString());
    }
}
