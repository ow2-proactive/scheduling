package org.objectweb.proactive.core.remoteobject.rmissh;

import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.rmi.RmiRemoteObjectImpl;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIClientSocketFactory;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIServerSocketFactory;


public class RmiSshRemoteObjectImpl extends RmiRemoteObjectImpl {
    public RmiSshRemoteObjectImpl(RemoteObject target)
        throws java.rmi.RemoteException {
        super(target, new SshRMIServerSocketFactory(),
            new SshRMIClientSocketFactory());
    }
}
