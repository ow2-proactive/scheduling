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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.rmi.RegistryHelper;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author acontes
 *        remote object Factory for the RMI protocol
 */
public class RmiRemoteObjectFactory extends AbstractRemoteObjectFactory
    implements RemoteObjectFactory {
    protected String protocolIdentifier = Constants.RMI_PROTOCOL_IDENTIFIER;
    protected static RegistryHelper registryHelper;
    static final Logger LOGGER_RO = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);

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
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target)
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
    public RemoteRemoteObject register(InternalRemoteRemoteObject target,
        URI url, boolean replacePreviousBinding) throws ProActiveException {
        RmiRemoteObject rro = null;
        try {
            rro = new RmiRemoteObjectImpl(target);
        } catch (RemoteException e1) {
            // Cannot be thrown by the constructor
            e1.printStackTrace();
        }

        try {
            Registry reg = LocateRegistry.getRegistry(url.getHost(),
                    url.getPort());
        } catch (Exception e) {
            LOGGER_RO.debug("creating new rmiregistry on port : " +
                url.getPort());
            try {
                LocateRegistry.createRegistry(url.getPort());
            } catch (RemoteException e1) {
                LOGGER_RO.warn("damn cannot start a rmiregistry on port " +
                    url.getPort());
                throw new ProActiveException(e1);
            }
        }

        try {
            if (replacePreviousBinding) {
                java.rmi.Naming.rebind(URIBuilder.removeProtocol(url).toString(),
                    rro);
            } else {
                java.rmi.Naming.bind(URIBuilder.removeProtocol(url).toString(),
                    rro);
            }
            LOGGER_RO.debug(" successfully bound in registry at " + url);
        } catch (java.rmi.AlreadyBoundException e) {
            LOGGER_RO.warn(url + " already bound in registry", e);
            throw new ProActiveException(e);
        } catch (java.net.MalformedURLException e) {
            throw new ProActiveException("cannot bind in registry at " + url, e);
        } catch (RemoteException e) {
            LOGGER_RO.debug(" cannot bind object at " + url);
            throw new ProActiveException(e);
        } catch (IOException e) {
            LOGGER_RO.warn(" cannot bind object at " + url +
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
            java.rmi.Naming.unbind(URIBuilder.removeProtocol(url).toString());
            LOGGER_RO.debug(url + " unbound in registry");
        } catch (IOException e) {
            //No need to throw an exception if an object is already unregistered
            LOGGER_RO.warn(url + " is not bound in the registry ");
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#lookup(java.net.URI)
     */
    public RemoteObject lookup(URI uri) throws ProActiveException {
        Object o = null;

        // Try if URL is the address of a RmiRemoteBody
        try {
            LOGGER_RO.debug("trying to acquire " + uri.toString());
            o = java.rmi.Naming.lookup(URIBuilder.removeProtocol(uri).toString());
            LOGGER_RO.debug(uri.toString() + " looked up successfully");
        } catch (IOException e) {
            // connection failed, try to find a rmiregistry at proactive.rmi.port port
            URI url2 = URIBuilder.buildURI(URIBuilder.getHostNameFromUrl(uri),
                    URIBuilder.getNameFromURI(uri));
            url2 = RemoteObjectHelper.expandURI(url2);
            LOGGER_RO.debug("Lookup of " + uri.toString() +
                " failed, failbacking on default port : " + url2.toString());
            try {
                o = java.rmi.Naming.lookup(URIBuilder.removeProtocol(url2)
                                                     .toString());
                LOGGER_RO.warn("Lookup of " + url2.toString() + " succeed");
            } catch (Exception e1) {
                LOGGER_RO.warn("Lookup of " + url2.toString() + " failed");
            }
        } catch (java.rmi.NotBoundException e) {
            // there are one rmiregistry on target computer but nothing bound to this url isn t bound
            throw new ProActiveException("The url " + uri +
                " is not bound to any known object");
        }

        if (o instanceof RmiRemoteObject) {
            return new RemoteObjectAdapter((RmiRemoteObject) o);
        }

        throw new ProActiveException(
            "The given url does exist but doesn't point to a remote object  url=" +
            uri + " class found is " + o.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#getPort()
     */
    public int getPort() {
        return Integer.parseInt(PAProperties.PA_RMI_PORT.getValue());
    }


    public String getProtocolId() {
        return this.protocolIdentifier;
    }
}
