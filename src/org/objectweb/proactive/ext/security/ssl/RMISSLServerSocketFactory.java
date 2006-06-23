/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ext.security.ssl;


/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;


public class RMISSLServerSocketFactory implements RMIServerSocketFactory,
    Serializable {
    private KeyStore keyStore = null;

    public ServerSocket createServerSocket(int port, KeyStore keyStore)
        throws IOException {
        SSLServerSocketFactory ssf = null;
        try {
            // set up key manager to do server authentication
            SSLContext ctx;
            KeyManagerFactory kmf;

            //KeyStore ks;
            //char[] passphrase = "passphrase".toCharArray();
            ctx = SSLContext.getInstance("TLS");
            kmf = KeyManagerFactory.getInstance("SunX509");
            //ks = KeyStore.getInstance("JKS");
            //ks.load(new FileInputStream("testkeys"), passphrase);
            kmf.init(keyStore, null);
            ctx.init(kmf.getKeyManagers(), null, null);

            ssf = ctx.getServerSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssf.createServerSocket(port);
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return createServerSocket(port, this.keyStore);
    }

    /**
     * @return Returns the keyStore.
     */
    public KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     * @param keyStore The keyStore to set.
     */
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }
}
