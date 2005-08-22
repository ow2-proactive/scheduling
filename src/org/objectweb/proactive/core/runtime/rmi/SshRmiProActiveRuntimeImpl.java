package org.objectweb.proactive.core.runtime.rmi;

import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import org.objectweb.proactive.core.Constants;


/**
 * @author mlacage
 *
 */
public class SshRmiProActiveRuntimeImpl extends RmiProActiveRuntimeImpl {
    public SshRmiProActiveRuntimeImpl(RMIClientSocketFactory csf,
        RMIServerSocketFactory ssf)
        throws java.rmi.RemoteException, java.rmi.AlreadyBoundException {
        super(csf, ssf);
    }

    protected String getProtocol() {
        return Constants.RMISSH_PROTOCOL_IDENTIFIER;
    }
}
