package org.objectweb.proactive.core.runtime.rmi;

import org.objectweb.proactive.core.Constants;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
/**
 * @author mlacage
 *
 */
public class SshRemoteProActiveRuntimeImpl extends RemoteProActiveRuntimeImpl {
	public SshRemoteProActiveRuntimeImpl(RMIClientSocketFactory csf, 
			RMIServerSocketFactory ssf) 
			throws java.rmi.RemoteException, java.rmi.AlreadyBoundException {
		super (csf, ssf);
	}
	
	protected String getProtocol () {
   		return Constants.RMISSH_PROTOCOL_IDENTIFIER;
    }
}
