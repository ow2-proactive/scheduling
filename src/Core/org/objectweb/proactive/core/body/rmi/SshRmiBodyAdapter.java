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
package org.objectweb.proactive.core.body.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIClientSocketFactory;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIServerSocketFactory;
import org.objectweb.proactive.core.util.UrlBuilder;


/**
 * An RMISSH adapter for a RemoteBody. The Adpater is the generic entry point for remote calls
 * to a RemoteBody using RMISSH.
 * This also allows to cache informations, and so to avoid crossing the network when calling some methods.
 * @author ProActiveTeam
 * @since ProActive 2.2
 * @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 */
public class SshRmiBodyAdapter extends RmiBodyAdapter {
    public SshRmiBodyAdapter() {
    }

    public SshRmiBodyAdapter(UniversalBody body) throws ProActiveException {
        try {
            RmiRemoteBody remoteBody = new RmiRemoteBodyImpl(body,
                    new SshRMIServerSocketFactory(),
                    new SshRMIClientSocketFactory());
            construct(remoteBody);
        } catch (java.rmi.RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    @Override
    public UniversalBody lookup(String url) throws java.io.IOException {
        String host;

        host = UrlBuilder.getHostNameFromUrl(url);

        int port = UrlBuilder.getPortFromUrl(url);
        try {
            Registry registry = LocateRegistry.getRegistry(host, port,
                    new SshRMIClientSocketFactory());
            RmiRemoteBody bodyStub = (RmiRemoteBody) registry.lookup(UrlBuilder.getNameFromUrl(
                        url));
            try {
                construct(bodyStub);
            } catch (ProActiveException e1) {
                throw new java.io.IOException("The remote object at " + url +
                    " is not accessible ");
            }
            return this;
        } catch (java.rmi.NotBoundException e) {
            throw new java.io.IOException("The url " + url +
                " is not bound to any known object");
        }
    }
}
