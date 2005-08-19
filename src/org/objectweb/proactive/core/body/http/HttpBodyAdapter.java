/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.BodyAdapter;
import org.objectweb.proactive.core.body.RemoteBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.http.util.exceptions.HTTPUnexpectedException;
import org.objectweb.proactive.core.body.http.util.messages.HttpLookupMessage;
import org.objectweb.proactive.core.rmi.ClassServer;
import org.objectweb.proactive.core.util.UrlBuilder;

import java.io.IOException;

import java.util.Hashtable;
/**
 * An HTTP adapter for a RemoteBody. The Adpater is the generic entry point for remote calls
 * to a RemoteBody using HTTP.
 * This also allows to cache informations, and so to avoid crossing the network when calling some methods.
 * @author ProActiveTeam
 * @since ProActive 2.2
 * @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 */

public class HttpBodyAdapter extends BodyAdapter {

    /**
     * an Hashtable containing all the http  adapters registered. They can be retrieved
     * thanks to the ProActive.lookupActive method
     */
    protected static transient Hashtable urnBodys = new Hashtable();
    

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
     * @param paBody the body of the active object to register.
     * @param urn The urn of the body (in fact his url + his name)
     * @exception java.io.IOException if the remote body cannot be registered
     */
    public void register(String urn)
        throws java.io.IOException {
        int port = UrlBuilder.getPortFromUrl(urn);

        //        System.out.println("port = " + port);
        //        System.out.println("port config = " + ClassServer.getServerSocketPort());
        if (port != ClassServer.getServerSocketPort()) {
            throw new IOException(
                "Bad registering port. You have to register on the same port as the runtime");
        }

        urn = urn.substring(urn.lastIndexOf('/') + 1);

        urnBodys.put(urn, this);

        if (bodyLogger.isInfoEnabled()) {
            bodyLogger.info("register object  at " + urn);
            bodyLogger.info(urnBodys);
        }
    }

    /**
     * Unregisters an active object previously registered into the bodys table
     * @param urn the urn under which the active object has been registered
     */
    public void unregister(String urn) throws java.io.IOException {
        urnBodys.put(urn, null);
    }

    /**
     * Looks-up an active object previously registered in the bodys table .
     * @param urn the urn (in fact its url + name)  the remote Body is registered to
     * @return a UniversalBody
     */
    public UniversalBody lookup(String urn) throws java.io.IOException {
        try {
            String url;
            int port = ClassServer.getServerSocketPort();
            url = urn;
            if (urn.lastIndexOf(":") > 4) {
                port = UrlBuilder.getPortFromUrl(urn);

                port = Integer.parseInt(urn.substring(urn.lastIndexOf(':') + 1,
                            urn.lastIndexOf(':') + 5));
            }

            urn = urn.substring(urn.lastIndexOf('/') + 1);

            HttpLookupMessage message = new HttpLookupMessage(urn, url, port);
            message.send();
            //            message = (HttpLookupMessage) ProActiveXMLUtils.sendMessage(url,
            //                    port, message, ProActiveXMLUtils.MESSAGE);
            //UniversalBody result = (UniversalBody) message.processMessage();
            UniversalBody result = message.getReturnedObject();

            //System.out.println("result = " + result );
            if (result == null) {
                throw new java.io.IOException("The url " + url +
                    " is not bound to any known object");
            } else {
                return result;
            }
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    /**
     * Gets a body from an urn in the table that mps urns and bodies
     * @param urn The urn of the body
     * @return the body mapping the urn
     */
    public static synchronized UniversalBody getBodyFromUrn(String urn) {
        return (UniversalBody) urnBodys.get(urn);
    }
}
