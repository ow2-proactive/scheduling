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
package org.objectweb.proactive.core.remoteobject.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.http.message.HttpRegistryListRemoteObjectsMessage;
import org.objectweb.proactive.core.remoteobject.http.message.HttpRemoteObjectLookupMessage;
import org.objectweb.proactive.core.remoteobject.http.util.HTTPRegistry;
import org.objectweb.proactive.core.remoteobject.http.util.exceptions.HTTPRemoteException;
import org.objectweb.proactive.core.rmi.ClassServer;
import org.objectweb.proactive.core.rmi.ClassServerHelper;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class HTTPRemoteObjectFactory extends AbstractRemoteObjectFactory
    implements RemoteObjectFactory {
    protected String protocolIdentifier = Constants.XMLHTTP_PROTOCOL_IDENTIFIER;

    static {
        createClassServer();
    }

    protected static synchronized void createClassServer() {
        if (classServerHelper == null) {
            try {
                classServerHelper = new ClassServerHelper();
                String codebase = classServerHelper.initializeClassServer();

                PAProperties.PA_XMLHTTP_PORT.setValue(URIBuilder.getPortNumber(
                        codebase) + "");

                addCodebase(codebase);
            } catch (Exception e) {
                ProActiveLogger.getLogger(Loggers.CLASS_SERVER)
                               .warn("Error with the ClassServer : " +
                    e.getMessage());
            }
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#newRemoteObject(org.objectweb.proactive.core.remoteobject.RemoteObject)
     */
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target)
        throws ProActiveException {
        try {
            return new HttpRemoteObjectImpl(target, null);
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * Registers an remote object into the http registry
     * @param urn The urn of the body (in fact his url + his name)
     * @exception java.io.IOException if the remote body cannot be registered
     */

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#register(org.objectweb.proactive.core.remoteobject.RemoteObject, java.net.URI, boolean)
     */
    public RemoteRemoteObject register(InternalRemoteRemoteObject ro, URI url,
        boolean replacePrevious) throws ProActiveException {
        URL u = null;

        int port = ClassServer.getServerSocketPort();
        try {
            u = new URL(url.toString());
            port = u.getPort();
            if (port != ClassServer.getServerSocketPort()) {
                throw new ProActiveException("Bad registering port : " + port +
                    ". You have to register on the same port as the runtime " +
                    ClassServer.getServerSocketPort());
            }
            url = URI.create(u.toString());
        } catch (MalformedURLException e) {
            url = URI.create(ClassServer.getUrl() + url.toString());
        }

        HTTPRegistry.getInstance().bind(url.toString(), ro);

        HttpRemoteObjectImpl rro = new HttpRemoteObjectImpl(ro, url);

        ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                       .info("registering remote object  at endpoint " + url);
        return rro;
    }

    /**
     * Unregisters an active object previously registered into the bodys table
     * @param urn the urn under which the active object has been registered
     */
    public void unregister(URI urn) throws ProActiveException {
        HTTPRegistry.getInstance().unbind(urn.toString());
    }

    /**
     * Looks-up an active object previously registered in the bodys table .
     * @param urn the urn (in fact its url + name)  the remote Body is registered to
     * @return a UniversalBody
     */
    public RemoteObject lookup(URI url) throws ProActiveException {
        int port = url.getPort();

        if (port == -1) {
            port = Integer.parseInt(PAProperties.PA_XMLHTTP_PORT.getValue());
        }

        String urn = url.getPath();
        HttpRemoteObjectLookupMessage message = new HttpRemoteObjectLookupMessage(urn,
                url, port);
        try {
            message.send();
        } catch (HTTPRemoteException e) {
            throw new ProActiveException(e);
        }
        RemoteRemoteObject result = message.getReturnedObject();

        if (result == null) {
            throw new ProActiveException("The url " + url +
                " is not bound to any known object");
        } else {
            return new RemoteObjectAdapter(result);
        }
    }

    /**
     * Gets a remote object from an urn in the table that maps urns and remote objects
     * @param urn The urn of the remote Object
     * @return the remote Object that maps the urn
     */

    //    public static synchronized InternalRemoteRemoteObject getRemoteObjectFromUrn(
    //        String urn) {
    //        try {
    //            return new HTTPRemoteObjectFactory().newRemoteObject(HTTPRegistry.getInstance()
    //                                                                             .lookup(urn));
    //        } catch (ProActiveException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        }
    //        return null;
    //    }

    /**
     * List all active object previously registered in the registry
     * @param url the url of the host to scan, typically //machine_name
     * @return a list of Strings, representing the registered names, and {} if no registry
     * @exception java.io.IOException if scanning reported some problem (registry not found, or malformed Url)
     */

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.BodyAdapterImpl#list(java.lang.String)
     */
    public URI[] list(URI url) throws ProActiveException {
        ArrayList<Object> paramsList = new ArrayList<Object>();

        HttpRegistryListRemoteObjectsMessage req = new HttpRegistryListRemoteObjectsMessage(url);

        try {
            req.send();

            String[] tmpUrl = req.getReturnedObject();

            URI[] uris = new URI[tmpUrl.length];

            for (int i = 0; i < tmpUrl.length; i++) {
                uris[i] = URI.create(tmpUrl[i]);
            }

            return uris;
        } catch (HTTPRemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#getPort()
     */
    public int getPort() {
        return Integer.parseInt(PAProperties.PA_XMLHTTP_PORT.getValue());
    }


    public String getProtocolId() {
        return this.protocolIdentifier;
    }
}
