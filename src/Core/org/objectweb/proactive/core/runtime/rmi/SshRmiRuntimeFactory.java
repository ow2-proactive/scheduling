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
package org.objectweb.proactive.core.runtime.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterForwarderImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterImpl;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntime;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIClientSocketFactory;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIServerSocketFactory;
import org.objectweb.proactive.core.util.UrlBuilder;


/**
 * @author mlacage
 */
public class SshRmiRuntimeFactory extends RmiRuntimeFactory {
    //    private static RegistryHelper registryHelper = new RegistryHelper();
    //    private static ClassServerHelper classServerHelper = new ClassServerHelper();
    //    private static ProActiveRuntime defaultRmiRuntime = null;
    public SshRmiRuntimeFactory() throws java.io.IOException {
        if ((System.getSecurityManager() == null) &&
                !("false".equals(System.getProperty("proactive.securitymanager")))) {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        }
    }

    // return a Runtime object "skeleton" (ie: a server)
    //    protected ProActiveRuntime getProtocolSpecificRuntimeImpl()
    //        throws ProActiveException {
    //        if (defaultRmiRuntime == null) {
    //            try {
    //                registryHelper.initializeRegistry();
    //            } catch (java.rmi.RemoteException e) {
    //                e.printStackTrace();
    //            }
    //
    //            defaultRmiRuntime = createRuntimeAdapter();
    //        }
    //        return defaultRmiRuntime;
    //    }
    // return a Runtime object "stub" (ie: a client)
    @Override
    protected ProActiveRuntime getRemoteRuntimeImpl(String s)
        throws ProActiveException {
        String host;
        try {
            host = UrlBuilder.getHostNameFromUrl(s);
        } catch (java.net.UnknownHostException e) {
            throw new ProActiveException("Unknown host: " + s, e);
        }
        int port = UrlBuilder.getPortFromUrl(s);
        try {
            Registry registry = LocateRegistry.getRegistry(host, port,
                    new SshRMIClientSocketFactory());
            RemoteProActiveRuntime runtimeStub = (RemoteProActiveRuntime) registry.lookup(UrlBuilder.getNameFromUrl(
                        s));
            return createRuntimeAdapter(runtimeStub);
        } catch (java.rmi.RemoteException e) {
            throw new ProActiveException("Remote", e);
        } catch (java.rmi.NotBoundException e) {
            throw new ProActiveException("NotBound", e);
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.RuntimeFactory#createRuntimeAdapter()
     */
    @Override
    protected ProActiveRuntimeAdapterImpl createRuntimeAdapter()
        throws ProActiveException {
        RmiProActiveRuntimeImpl impl;
        try {
            impl = new SshRmiProActiveRuntimeImpl(new SshRMIClientSocketFactory(),
                    new SshRMIServerSocketFactory());
        } catch (java.rmi.RemoteException e) {
            throw new ProActiveException("Cannot create the RemoteProActiveRuntimeImpl",
                e);
        } catch (java.rmi.AlreadyBoundException e) {
            throw new ProActiveException("Cannot bind remoteProactiveRuntime", e);
        }
        return new ProActiveRuntimeAdapterImpl(impl);
    }

    @Override
    protected ProActiveRuntimeAdapterForwarderImpl createRuntimeAdapterForwarder()
        throws ProActiveException {
        RmiSshProActiveRuntimeForwarder impl;
        try {
            impl = new RmiSshProActiveRuntimeForwarder(new SshRMIClientSocketFactory(),
                    new SshRMIServerSocketFactory());
        } catch (java.rmi.RemoteException e) {
            throw new ProActiveException("Cannot create the RemoteProActiveRuntimeImpl",
                e);
        } catch (java.rmi.AlreadyBoundException e) {
            throw new ProActiveException("Cannot bind remoteProactiveRuntime", e);
        }
        return new ProActiveRuntimeAdapterForwarderImpl(impl);
    }
}
