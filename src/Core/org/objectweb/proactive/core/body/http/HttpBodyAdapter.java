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
package org.objectweb.proactive.core.body.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.BodyAdapterImpl;
import org.objectweb.proactive.core.body.RemoteBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.http.util.HTTPRegistry;
import org.objectweb.proactive.core.body.http.util.exceptions.HTTPUnexpectedException;
import org.objectweb.proactive.core.body.http.util.messages.HttpLookupMessage;
import org.objectweb.proactive.core.rmi.ClassServer;


/**
 * An HTTP adapter for a RemoteBody. The Adpater is the generic entry point for remote calls
 * to a RemoteBody using HTTP.
 * This also allows to cache informations, and so to avoid crossing the network when calling some methods.
 * @author ProActiveTeam
 * @since ProActive 2.2
 * @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 */
public class HttpBodyAdapter extends BodyAdapterImpl {
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public HttpBodyAdapter() {
    }

    public HttpBodyAdapter(UniversalBody body) throws ProActiveException {
        RemoteBody remoteBody = new HttpRemoteBodyImpl(body);
        construct(remoteBody);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Registers an active object into the table of body.
     * @param urn The urn of the body (in fact his url + his name)
     * @exception java.io.IOException if the remote body cannot be registered
     */
    @Override
    public void register(String urn) throws java.io.IOException {
        URL u = null;
        String url = null;
        int port = ClassServer.getServerSocketPort();
        try {
            u = new URL(urn);
            port = u.getPort();
            if (port != ClassServer.getServerSocketPort()) {
                throw new IOException(
                    "Bad registering port. You have to register on the same port as the runtime");
            }
            url = u.toString();
            urn = u.getPath();
        } catch (MalformedURLException e) {
            url = ClassServer.getUrl() + urn;
        }
        HTTPRegistry.getInstance().bind(urn, this);
        //        urn = urn.substring(urn.lastIndexOf('/') + 1);
        if (bodyLogger.isInfoEnabled()) {
            bodyLogger.info("register object  at " + url);
        }
    }

    /**
     * Unregisters an active object previously registered into the bodys table
     * @param urn the urn under which the active object has been registered
     */
    @Override
    public void unregister(String urn) throws java.io.IOException {
        HTTPRegistry.getInstance().unbind(urn);
    }

    /**
     * Looks-up an active object previously registered in the bodys table .
     * @param urn the urn (in fact its url + name)  the remote Body is registered to
     * @return a UniversalBody
     */
    @Override
    public UniversalBody lookup(String urn) throws java.io.IOException {
        //        try {
        URL u = null;
        int port = 0;
        try {
            u = new URL(urn);
            port = u.getPort();
        } catch (MalformedURLException e) {
            if (!urn.startsWith("http://")) {
                urn = "http://" + urn;
                return lookup(urn);
            }
            throw e;
        }
        if (port == 0) {
            throw new HTTPUnexpectedException(
                "You have to specify a port where the runtime can be reached");
        }
        String url = u.toString();
        urn = u.getPath();
        HttpLookupMessage message = new HttpLookupMessage(urn, url, port);
        message.send();
        UniversalBody result = message.getReturnedObject();
        if (result == null) {
            throw new java.io.IOException("The url " + url +
                " is not bound to any known object");
        } else {
            return result;
        }

        //        	
        //        	String url;
        ////            int port = ClassServer.getServerSocketPort();
        //            url = urn;

        //            urn = urn.substring(urn.lastIndexOf('/') + 1);
        //
        //
        //       
        //
        //            //            message = (HttpLookupMessage) ProActiveXMLUtils.sendMessage(url,
        //            //                    port, message, ProActiveXMLUtils.MESSAGE);
        //            //UniversalBody result = (UniversalBody) message.processMessage();
        //
        //
        //            //System.out.println("result = " + result );
        //          
        //        } catch (Exception e) {
        //            throw new HTTPUnexpectedException("Unexpected exception", e);
        //        }
    }

    /**
     * Gets a body from an urn in the table that mps urns and bodies
     * @param urn The urn of the body
     * @return the body mapping the urn
     */
    public static synchronized UniversalBody getBodyFromUrn(String urn) {
        return HTTPRegistry.getInstance().lookup(urn);
    }

    /**
     * List all active object previously registered in the registry
     * @param url the url of the host to scan, typically //machine_name
     * @return a list of Strings, representing the registered names, and {} if no registry
     * @exception java.io.IOException if scanning reported some problem (registry not found, or malformed Url)
     */

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.BodyAdapterImpl#list(java.lang.String)
     */
    @Override
    public String[] list(String url) throws java.io.IOException {
        return HTTPRegistry.getInstance().list();
    }
}
