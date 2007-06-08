package org.objectweb.proactive.core.remoteobject.ibis;

import java.io.IOException;
import java.net.URI;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.rmi.RegistryHelper;
import org.objectweb.proactive.core.util.IbisProperties;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import ibis.rmi.RemoteException;


public class IbisRemoteObjectFactory extends RemoteObjectFactory {
    protected static RegistryHelper registryHelper;

    static {
        IbisProperties.load();

        if ((System.getSecurityManager() == null) &&
                !("false".equals(ProActiveConfiguration.getInstance()
                                                           .getProperty("proactive.securitymanager")))) {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        }

        createClassServer();
        createRegistry();
    }

    private static synchronized void createRegistry() {
        if (registryHelper == null) {
            registryHelper = new RegistryHelper();
            try {
                registryHelper.initializeRegistry();
            } catch (java.rmi.RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public RemoteRemoteObject newRemoteObject(RemoteObject target)
        throws ProActiveException {
        try {
            return new IbisRemoteObjectImpl(target);
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public URI[] list(URI url) throws ProActiveException {
        try {
            String[] names = ibis.rmi.Naming.list(URIBuilder.removeProtocol(url)
                                                            .toString());

            if (names != null) {
                URI[] uris = new URI[names.length];
                for (int i = 0; i < names.length; i++) {
                    uris[i] = URIBuilder.setProtocol(URI.create(names[i]),
                            Constants.IBIS_PROTOCOL_IDENTIFIER);
                }
                return uris;
            }
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
        return null;
    }

    public RemoteRemoteObject register(RemoteObject target, URI url,
        boolean replacePreviousBinding) throws ProActiveException {
        IbisRemoteObject rro = null;
        try {
            rro = new IbisRemoteObjectImpl(target);
        } catch (RemoteException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            if (replacePreviousBinding) {
                ibis.rmi.Naming.rebind(UrlBuilder.removeProtocol(url.toString()),
                    rro);
            } else {
                ibis.rmi.Naming.bind(UrlBuilder.removeProtocol(url.toString()),
                    rro);
            }
            rro.setURI(url);
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .debug(" successfully bound in registry at " + url);
        } catch (ibis.rmi.AlreadyBoundException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .warn(url + " already bound in registry", e);
            throw new ProActiveException(e);
        } catch (java.net.MalformedURLException e) {
            throw new ProActiveException("cannot bind in registry at " + url, e);
        } catch (RemoteException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .debug(" cannot bind object at " + url);
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rro;
    }

    public void unregister(URI url) throws ProActiveException {
        try {
            ibis.rmi.Naming.unbind(UrlBuilder.removeProtocol(url.toString()));

            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .debug(url + " unbound in registry");
        } catch (ibis.rmi.NotBoundException e) {
            //No need to throw an exception if an object is already unregistered
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .warn(url + " is not bound in the registry ");
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public RemoteObject lookup(URI url1) throws ProActiveException {
        Object o = null;
        String url = UrlBuilder.removeProtocol(url1.toString());

        // Try if URL is the address of a RmiRemoteBody
        try {
            o = ibis.rmi.Naming.lookup(url);
        } catch (IOException e) {
            // connection failed, try to find a rmiregistry at proactive.rmi.port port
            String url2 = UrlBuilder.buildUrl(UrlBuilder.getHostNameFromUrl(url),
                    UrlBuilder.getNameFromUrl(url));
            try {
                o = ibis.rmi.Naming.lookup(url);
            } catch (Exception e1) {
                throw new ProActiveException(e);
            }
        } catch (ibis.rmi.NotBoundException e) {
            // there are one rmiregistry on target computer but nothing bound to this url isn t bound
            throw new ProActiveException("The url " + url +
                " is not bound to any known object");
        }

        if (o instanceof IbisRemoteObject) {
            return new RemoteObjectAdapter((IbisRemoteObject) o);
        }

        throw new ProActiveException(
            "The given url does exist but doesn't point to a remote object  url=" +
            url + " class found is " + o.getClass().getName());
    }
}
