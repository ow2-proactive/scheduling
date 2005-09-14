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
package org.objectweb.proactive.core.runtime.ibis;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.rmi.ClassServerHelper;
import org.objectweb.proactive.core.rmi.RegistryHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeForwarder;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.IbisProperties;
import org.objectweb.proactive.core.util.UrlBuilder;

import ibis.rmi.AlreadyBoundException;
import ibis.rmi.RemoteException;


public class IbisRuntimeFactory extends RuntimeFactory {

    static {
        IbisProperties.load();
    }

    //protected final static int MAX_RETRY = 5;
    //protected java.util.Random random;
    protected static RegistryHelper registryHelper = new RegistryHelper();
    protected static ClassServerHelper classServerHelper = new ClassServerHelper();
    private static ProActiveRuntime defaultRmiRuntime = null;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public IbisRuntimeFactory() throws java.io.IOException {
        if ((System.getSecurityManager() == null) &&
                !("false".equals(System.getProperty("proactive.securitymanager")))) {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        }

        registryHelper.initializeRegistry();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public static void setClassServerClasspath(String v) {
        classServerHelper.setClasspath(v);
    }

    public static void setShouldCreateClassServer(boolean v) {
        classServerHelper.setShouldCreateClassServer(v);
    }

    public static void setRegistryPortNumber(int v) {
        registryHelper.setRegistryPortNumber(v);
    }

    public static void setShouldCreateRegistry(boolean v) {
        registryHelper.setShouldCreateRegistry(v);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    //  protected ProActiveRuntime createRemoteRuntimeImpl(String s, boolean replacePreviousBinding) throws ProActiveException {
    //    return createRuntimeAdapter(s, replacePreviousBinding);
    //  }
    protected synchronized ProActiveRuntime getProtocolSpecificRuntimeImpl()
        throws ProActiveException {
        //return createRuntimeAdapter(s,false);
        if (defaultRmiRuntime == null) {
            defaultRmiRuntime = createRuntimeAdapter();
        }

        return defaultRmiRuntime;
    }

    protected ProActiveRuntime getRemoteRuntimeImpl(String s)
        throws ProActiveException {
        //if (s == null) return null;
        if (runtimeLogger.isDebugEnabled()) {
            runtimeLogger.debug("looking for " + s);
        }

        try {
            RemoteProActiveRuntime remoteProActiveRuntime = (RemoteProActiveRuntime) ibis.rmi.Naming.lookup(UrlBuilder.removeProtocol(
                        s, "ibis:"));

            if (runtimeLogger.isDebugEnabled()) {
                runtimeLogger.debug(remoteProActiveRuntime.getClass().getName());
            }

            return createRuntimeAdapter(remoteProActiveRuntime);
        } catch (ibis.rmi.RemoteException e) {
            throw new ProActiveException("Remote", e);
        } catch (ibis.rmi.NotBoundException e) {
            throw new ProActiveException("NotBound", e);
        } catch (java.net.MalformedURLException e) {
            throw new ProActiveException("Malformed URL:" + s, e);
        }
    }

    protected ProActiveRuntimeAdapterImpl createRuntimeAdapter()
        throws ProActiveException {
        IbisProActiveRuntimeImpl impl;

        try {
            impl = new IbisProActiveRuntimeImpl();
        } catch (RemoteException e) {
            throw new ProActiveException("Cannot create the RemoteProActiveRuntimeImpl",
                e);
        } catch (AlreadyBoundException e) {
            throw new ProActiveException("Cannot bind remoteProactiveRuntime", e);
        }

        return new ProActiveRuntimeAdapterImpl(impl);
    }

    protected static RegistryHelper getRegistryHelper() {
        return registryHelper;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
