/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.core.remoteobject.rmi;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.rmi.RegistryHelper;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author acontes
 *        remote object Factory for the RMI protocol
 */
public class RmiRemoteObjectFactory extends AbstractRemoteObjectFactory
    implements RemoteObjectFactory {
    protected static RegistryHelper registryHelper;

    static {
        if ((System.getSecurityManager() == null) &&
                PAProperties.PA_SECURITYMANAGER.isTrue()) {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        }

        createClassServer();
        createRegistry();
    }

    /**
     *  create the RMI registry
     */
    private static synchronized void createRegistry() {
        if (registryHelper == null) {
            registryHelper = new RegistryHelper();
            try {
                registryHelper.initializeRegistry();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#newRemoteObject(org.objectweb.proactive.core.remoteobject.RemoteObject)
     */
    public RemoteRemoteObject newRemoteObject(RemoteObject target)
        throws ProActiveException {
        try {
            return new RmiRemoteObjectImpl(target);
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#list(java.net.URI)
     */
    public URI[] list(URI url) throws ProActiveException {
        try {
            String[] names = java.rmi.Naming.list(URIBuilder.removeProtocol(url)
                                                            .toString());

            if (names != null) {
                URI[] uris = new URI[names.length];
                for (int i = 0; i < names.length; i++) {
                    uris[i] = URIBuilder.setProtocol(URI.create(names[i]),
                            Constants.RMI_PROTOCOL_IDENTIFIER);
                }
                return uris;
            }
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#register(org.objectweb.proactive.core.remoteobject.RemoteObject, java.net.URI, boolean)
     */
    public RemoteRemoteObject register(RemoteObject target, URI url,
        boolean replacePreviousBinding) throws ProActiveException {
        RmiRemoteObject rro = null;

        try {
            rro = new RmiRemoteObjectImpl(target);
        } catch (RemoteException e1) {
            // Cannot be thrown by the constructor
            e1.printStackTrace();
        }

        try {
            Registry reg = LocateRegistry.getRegistry(url.getPort());
        } catch (Exception e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .debug("creating new rmiregistry on port : " +
                url.getPort());
            try {
                LocateRegistry.createRegistry(url.getPort());
            } catch (RemoteException e1) {
                ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                               .warn("damn cannot start a rmiregistry on port " +
                    url.getPort());
                throw new ProActiveException(e1);
            }
        }

        try {
            if (replacePreviousBinding) {
                java.rmi.Naming.rebind(UrlBuilder.removeProtocol(url.toString()),
                    rro);
            } else {
                java.rmi.Naming.bind(UrlBuilder.removeProtocol(url.toString()),
                    rro);
            }
            rro.setURI(url);
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .debug(" successfully bound in registry at " + url);
        } catch (java.rmi.AlreadyBoundException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .warn(url + " already bound in registry", e);
            throw new ProActiveException(e);
        } catch (java.net.MalformedURLException e) {
            throw new ProActiveException("cannot bind in registry at " + url, e);
        } catch (RemoteException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .debug(" cannot bind object at " + url);
            throw new ProActiveException(e);
        } catch (IOException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .warn(" cannot bind object at " + url +
                " \n reason is : " + e.getMessage());
            throw new ProActiveException(e);
        }
        return rro;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#unregister(java.net.URI)
     */
    public void unregister(URI url) throws ProActiveException {
        try {
            java.rmi.Naming.unbind(UrlBuilder.removeProtocol(url.toString()));

            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .debug(url + " unbound in registry");
        } catch (IOException e) {
            //No need to throw an exception if an object is already unregistered
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .warn(url + " is not bound in the registry ");
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#lookup(java.net.URI)
     */
    public RemoteObject lookup(URI url1) throws ProActiveException {
        Object o = null;
        String url = url1.toString();

        // Try if URL is the address of a RmiRemoteBody
        try {
            o = java.rmi.Naming.lookup(URIBuilder.removeProtocol(url1).toString());
        } catch (IOException e) {
            // connection failed, try to find a rmiregistry at proactive.rmi.port port
            String url2 = UrlBuilder.buildUrl(UrlBuilder.getHostNameFromUrl(url),
                    UrlBuilder.getNameFromUrl(url));
            try {
                o = java.rmi.Naming.lookup(url);
            } catch (Exception e1) {
                throw new ProActiveException(e);
            }
        } catch (java.rmi.NotBoundException e) {
            // there are one rmiregistry on target computer but nothing bound to this url isn t bound
            throw new ProActiveException("The url " + url +
                " is not bound to any known object");
        }

        if (o instanceof RmiRemoteObject) {
            return new RemoteObjectAdapter((RmiRemoteObject) o);
        }

        throw new ProActiveException(
            "The given url does exist but doesn't point to a remote object  url=" +
            url + " class found is " + o.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#getPort()
     */
    public int getPort() {
        return Integer.parseInt(PAProperties.PA_RMI_PORT.getValue());
    }
}
