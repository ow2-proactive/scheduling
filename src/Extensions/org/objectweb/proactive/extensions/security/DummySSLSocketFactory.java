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
package org.objectweb.proactive.extensions.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * DummySSLSocketFactory connects to an HTTPS connection without
 * verifying the server's certificate.
 *
 */
public class DummySSLSocketFactory extends SocketFactory {
    private static SocketFactory dummyFactory = null;

    static {
        // create a dummy trust manager
        TrustManager[] dummyTrustMan = new TrustManager[] {
                new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] c,
                            String a) {
                        }

                        public void checkServerTrusted(X509Certificate[] c,
                            String a) {
                        }
                    }
            };

        // create our "dummy" ssl socket factory with our lazy trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, dummyTrustMan, new java.security.SecureRandom());
            dummyFactory = sc.getSocketFactory();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see javax.net.SocketFactory#getDefault()
     */
    public static SocketFactory getDefault() {
        return new DummySSLSocketFactory();
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
     */
    @Override
    public Socket createSocket(String arg0, int arg1)
        throws IOException, UnknownHostException {
        return dummyFactory.createSocket(arg0, arg1);
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
     */
    @Override
    public Socket createSocket(InetAddress arg0, int arg1)
        throws IOException {
        return dummyFactory.createSocket(arg0, arg1);
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.lang.String, int,
     *      java.net.InetAddress, int)
     */
    @Override
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
        throws IOException, UnknownHostException {
        return dummyFactory.createSocket(arg0, arg1, arg2, arg3);
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int,
     *      java.net.InetAddress, int)
     */
    @Override
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
        int arg3) throws IOException {
        return dummyFactory.createSocket(arg0, arg1, arg2, arg3);
    }
}
