package org.objectweb.proactive.core.runtime.rmi;

import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.rmi.ClassServerHelper;
import org.objectweb.proactive.core.rmi.RegistryHelper;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIClientSocketFactory;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIServerSocketFactory;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

/**
 * @author mlacage
 */
public class SshRemoteRuntimeFactory extends RuntimeFactory {
	private static RegistryHelper registryHelper = new RegistryHelper();
	private static ClassServerHelper classServerHelper = new ClassServerHelper();
    private static ProActiveRuntime defaultRmiRuntime = null;
	
	public SshRemoteRuntimeFactory() throws java.io.IOException {
		if ((System.getSecurityManager() == null) &&
				!("false".equals(System.getProperty("proactive.securitymanager")))) {
			System.setSecurityManager(new java.rmi.RMISecurityManager());
		}
	}
    
	// return a Runtime object "skeleton" (ie: a server)
    protected ProActiveRuntime getProtocolSpecificRuntimeImpl()
    	throws ProActiveException {
        if (defaultRmiRuntime == null) {
			try {
				registryHelper.initializeRegistry();
			} catch (java.rmi.RemoteException e) {
				e.printStackTrace();
			}
	    	RemoteProActiveRuntimeImpl impl;
	        try {
	        	impl = new SshRemoteProActiveRuntimeImpl (new SshRMIClientSocketFactory (), 
	        			new SshRMIServerSocketFactory ());
	        } catch (java.rmi.RemoteException e) {
	            throw new ProActiveException("Cannot create the RemoteProActiveRuntimeImpl", e);
	        } catch (java.rmi.AlreadyBoundException e) {
	            throw new ProActiveException(
	                "Cannot bind remoteProactiveRuntime", e);
	        }
	        defaultRmiRuntime = new RemoteProActiveRuntimeAdapter (impl);	
        }
        return defaultRmiRuntime;
    }

    // return a Runtime object "stub" (ie: a client)
    protected ProActiveRuntime getRemoteRuntimeImpl(String s)
    		throws ProActiveException {
        String host;
        try {
        	host = UrlBuilder.getHostNameFromUrl (s);
        } catch (java.net.UnknownHostException e) {
        	throw new ProActiveException ("Unknown host: " + s, e);
        }
        int port = UrlBuilder.getPortFromUrl (s);
        try {
        	Registry registry = LocateRegistry.getRegistry (host, port, 
        		new SshRMIClientSocketFactory ());
        	 RemoteProActiveRuntime runtimeStub = (RemoteProActiveRuntime) 
				registry.lookup (UrlBuilder.getNameFromUrl (s));
            return new RemoteProActiveRuntimeAdapter(runtimeStub);
        } catch (java.rmi.RemoteException e) {
            throw new ProActiveException("Remote", e);
        } catch (java.rmi.NotBoundException e) {
            throw new ProActiveException("NotBound", e);
        }    	
    }
}
