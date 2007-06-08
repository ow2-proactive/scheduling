package org.objectweb.proactive.core.remoteobject.rmissh;

import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.rmi.RmiRemoteObject;
import org.objectweb.proactive.core.remoteobject.rmi.RmiRemoteObjectFactory;
import org.objectweb.proactive.core.rmi.ClassServer;
import org.objectweb.proactive.core.rmi.ClassServerHelper;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIClientSocketFactory;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RmiSshRemoteObjectFactory extends RmiRemoteObjectFactory {

    static {
        createClassServer();
    }

    protected static synchronized void createClassServer() {
        try {
            if (classServerHelper == null) {
                classServerHelper = new ClassServerHelper();
            }
            String codebase = classServerHelper.initializeClassServer();

            codebase = UrlBuilder.buildUrl(UrlBuilder.getHostNameFromUrl(
                        codebase), "/", Constants.HTTPSSH_PROTOCOL_IDENTIFIER,
                    ClassServer.getServerSocketPort());

            codebase = addCodebase(codebase);
        } catch (Exception e) {
            ProActiveLogger.getLogger(Loggers.CLASS_SERVER)
                           .warn("Error with the ClassServer : " +
                e.getMessage());
        }
    }

    public RemoteRemoteObject newRemoteObject(RemoteObject target)
        throws ProActiveException {
        try {
            return new RmiSshRemoteObjectImpl(target);
        } catch (RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    public URI[] list(URI url) throws ProActiveException {
        try {
            Registry registry = LocateRegistry.getRegistry(url.getHost(),
                    url.getPort(), new SshRMIClientSocketFactory());

            String[] names = registry.list();
            if (names != null) {
                URI[] uris = new URI[names.length];
                for (int i = 0; i < names.length; i++) {
                    uris[i] = URIBuilder.buildURI(url.getHost(), names[i],
                            Constants.RMISSH_PROTOCOL_IDENTIFIER, url.getPort());
                }
                return uris;
            }
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
        return null;
    }

    @Override
    public RemoteObject lookup(URI url1) throws ProActiveException {
        String host;
        String url = url1.toString();
        host = UrlBuilder.getHostNameFromUrl(url);

        int port = UrlBuilder.getPortFromUrl(url);
        try {
            Registry registry = LocateRegistry.getRegistry(host, port,
                    new SshRMIClientSocketFactory());
            RmiRemoteObject objectStub = (RmiRemoteObject) registry.lookup(UrlBuilder.getNameFromUrl(
                        url));
            return new RemoteObjectAdapter(objectStub);
        } catch (java.rmi.NotBoundException e) {
            throw new ProActiveException("The url " + url +
                " is not bound to any known object");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
