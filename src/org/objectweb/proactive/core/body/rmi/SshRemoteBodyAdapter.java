package org.objectweb.proactive.core.body.rmi;

import org.objectweb.proactive.core.ssh.rmissh.SshRMIClientSocketFactory;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIServerSocketFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.ProActiveException;

/**
 * @author mlacage
 *
 */
public class SshRemoteBodyAdapter extends RemoteBodyAdapter {
	   public SshRemoteBodyAdapter(UniversalBody body) throws ProActiveException {
        try {
        		RemoteBody remoteBody = new RemoteBodyImpl(body,
        				new SshRMIServerSocketFactory (),
        				new SshRMIClientSocketFactory ());
                super.construct (remoteBody);
        } catch (java.rmi.RemoteException e) {
            throw new ProActiveException(e);
        }
    }
}
