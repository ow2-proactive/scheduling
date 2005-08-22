package org.objectweb.proactive.core.ssh.httpssh;


/**
 * @author mlacage
 *
 */
public class Handler extends java.net.URLStreamHandler {
    protected java.net.URLConnection openConnection(java.net.URL u)
        throws java.io.IOException {
        HttpSshUrlConnection connection = new HttpSshUrlConnection(u);
        return connection;
    }
}
