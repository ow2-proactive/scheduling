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
package org.objectweb.proactive.core.body.ibis;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.BodyAdapterImpl;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.util.UrlBuilder;


/**
 * An IBIS adapter for a RemoteBody. The Adpater is the generic entry point for remote calls
 * to a RemoteBody using IBIS.
 * This also allows to cache informations, and so to avoid crossing the network when calling some methods.
 * @author ProActiveTeam
 * @since ProActive 2.2
 * @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 */
public class IbisBodyAdapter extends BodyAdapterImpl {
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public IbisBodyAdapter() {
    }

    public IbisBodyAdapter(IbisRemoteBody remoteBody) throws ProActiveException {
        construct(remoteBody);
    }

    public IbisBodyAdapter(UniversalBody body) throws ProActiveException {
        try {
            IbisRemoteBody remoteBody = new IbisRemoteBodyImpl(body);
            construct(remoteBody);
        } catch (ibis.rmi.RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Registers an active object into a RMI registry. In fact it is the
     * remote version of the body of the active object that is registered into the
     * RMI Registry under the given URL.
     * @param url the url under which the remote body is registered.
     * @exception java.io.IOException if the remote body cannot be registered
     */
    @Override
    public void register(String url) throws java.io.IOException {
        ibis.rmi.Naming.rebind(url, (IbisRemoteBody) proxiedRemoteBody);
    }

    /**
     * Unregisters an active object previously registered into a RMI registry.
     * @param url the url under which the active object is registered.
     * @exception java.io.IOException if the remote object cannot be removed from the registry
     */
    @Override
    public void unregister(String url) throws java.io.IOException {
        try {
            ibis.rmi.Naming.unbind(url);
        } catch (ibis.rmi.NotBoundException e) {
            throw new java.io.IOException(
                "No object is bound to the given url : " + url);
        }
    }

    /**
     * Looks-up an active object previously registered in a RMI registry. In fact it is the
     * remote version of the body of an active object that can be registered into the
     * RMI Registry under a given URL.
     * @param url the url the remote Body is registered to
     * @return a UniversalBody
     * @exception java.io.IOException if the remote body cannot be found under the given url
     *      or if the object found is not of type IbisRemoteBody
     */
    @Override
    public UniversalBody lookup(String url) throws java.io.IOException {
        Object o = null;

        // Try if URL is the address of a IbisRemoteBody
        try {
            o = ibis.rmi.Naming.lookup(UrlBuilder.removeProtocol(url));
        } catch (ibis.rmi.NotBoundException e) {
            throw new java.io.IOException("The url " + url +
                " is not bound to any known object");
        }

        if (o instanceof IbisRemoteBody) {
            try {
                construct((IbisRemoteBody) o);
            } catch (ProActiveException e1) {
                throw new java.io.IOException("The remote object at " + url +
                    " is not accessible ");
            }

            return this;
        } else {
            throw new java.io.IOException(
                "The given url does exist but doesn't point to a remote body  url=" +
                url + " class found is " + o.getClass().getName());
        }
    }
}
