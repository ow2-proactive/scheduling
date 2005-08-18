package org.objectweb.proactive.core.body.rmi;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIClientSocketFactory;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIServerSocketFactory;
import org.objectweb.proactive.core.util.UrlBuilder;


/**
 * @author mlacage
 *
 */
public class SshRmiBodyAdapter extends RmiBodyAdapter {
    
    public SshRmiBodyAdapter(){
        
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

    public UniversalBody lookup(String url) throws java.io.IOException {
        String host;
        try {
            host = UrlBuilder.getHostNameFromUrl(url);
        } catch (java.net.UnknownHostException e) {
            throw new IOException("Unknown host in: " + url + " " +
                e.toString());
        }
        int port = UrlBuilder.getPortFromUrl(url);
        try {
            Registry registry = LocateRegistry.getRegistry(host, port,
                    new SshRMIClientSocketFactory());
            RmiRemoteBody bodyStub = (RmiRemoteBody) registry.lookup(UrlBuilder.getNameFromUrl(
                        url));
            try {
                construct(bodyStub);
            } catch (ProActiveException e1) {
                throw new java.io.IOException(
                "The remote object with the given url is not accessible ");
            };
            return this;
        } catch (java.rmi.NotBoundException e) {
            throw new java.io.IOException("The url " + url +
                " is not bound to any known object");
        }
    }
}
