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
package org.objectweb.proactive.core.remoteobject.ibis;

import ibis.rmi.RemoteException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

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
import org.objectweb.proactive.core.util.IbisProperties;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class IbisRemoteObjectFactory extends AbstractRemoteObjectFactory implements RemoteObjectFactory {
    protected static RegistryHelper registryHelper;
    protected String protocolIdentifier = Constants.IBIS_PROTOCOL_IDENTIFIER;
    static final Logger LOGGER_RO = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);

    static {
        IbisProperties.load();

        if ((System.getSecurityManager() == null) && PAProperties.PA_SECURITYMANAGER.isTrue()) {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        }

        createClassServer();
        createRegistry();
    }

    /**
     * create the registry used by the ibis protocol
     */
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

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#newRemoteObject(org.objectweb.proactive.core.remoteobject.RemoteObject)
     */
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) throws ProActiveException {
        try {
            return new IbisRemoteObjectImpl(target);
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#list(java.net.URI)
     */
    public URI[] list(URI url) throws ProActiveException {
        try {
            String[] names = ibis.rmi.Naming.list(URIBuilder.removeProtocol(url).toString());

            if (names != null) {
                URI[] uris = new URI[names.length];
                for (int i = 0; i < names.length; i++) {
                    uris[i] = URIBuilder
                            .setProtocol(URI.create(names[i]), Constants.IBIS_PROTOCOL_IDENTIFIER);
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
    public RemoteRemoteObject register(InternalRemoteRemoteObject target, URI url,
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
                ibis.rmi.Naming.rebind(URIBuilder.removeProtocol(url).toString(), rro);
            } else {
                ibis.rmi.Naming.bind(URIBuilder.removeProtocol(url).toString(), rro);
            }
            //            rro.setURI(url);
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                    .debug(" successfully bound in registry at " + url);
        } catch (ibis.rmi.AlreadyBoundException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).warn(url + " already bound in registry", e);
            throw new ProActiveException(e);
        } catch (java.net.MalformedURLException e) {
            throw new ProActiveException("cannot bind in registry at " + url, e);
        } catch (RemoteException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).debug(" cannot bind object at " + url);
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rro;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#unregister(java.net.URI)
     */
    public void unregister(URI url) throws ProActiveException {
        try {
            ibis.rmi.Naming.unbind(URIBuilder.removeProtocol(url).toString());

            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).debug(url + " unbound in registry");
        } catch (ibis.rmi.NotBoundException e) {
            //No need to throw an exception if an object is already unregistered
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).warn(url + " is not bound in the registry ");
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#lookup(java.net.URI)
     */
    public RemoteObject lookup(URI uri) throws ProActiveException {
        Object o = null;

        URI modifiedURI = uri;
        if (uri.getPort() == -1) {
            LOGGER_RO.debug("No port specified, using the default one");
            modifiedURI = URIBuilder.buildURI(URIBuilder.getHostNameFromUrl(uri), URIBuilder
                    .getNameFromURI(uri));
            modifiedURI = RemoteObjectHelper.expandURI(modifiedURI);
        }

        // Try if URL is the address of a RmiRemoteBody
        try {
            o = ibis.rmi.Naming.lookup(URIBuilder.removeProtocol(modifiedURI).toString());
            LOGGER_RO.debug(modifiedURI.toString() + " looked up successfully");
        } catch (ibis.rmi.NotBoundException e) {
            // there are one rmiregistry on target computer but nothing bound to this url isn t bound
            throw new ProActiveException("The url " + modifiedURI + " is not bound to any known object", e);
        } catch (MalformedURLException e) {
            throw new ProActiveException(e);
        } catch (RemoteException e) {
            throw new ProActiveException("Registry could not be contacted, " + modifiedURI, e);
        }

        if (o instanceof IbisRemoteObject) {
            return new RemoteObjectAdapter((IbisRemoteObject) o);
        }

        throw new ProActiveException("The given url does exist but doesn't point to a remote object  url=" +
            modifiedURI + " class found is " + o.getClass().getName());
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
