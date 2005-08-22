package org.objectweb.proactive.core.runtime.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapter;
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
    protected ProActiveRuntimeAdapter createRuntimeAdapter()
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
        return new ProActiveRuntimeAdapter(impl);
    }
}
