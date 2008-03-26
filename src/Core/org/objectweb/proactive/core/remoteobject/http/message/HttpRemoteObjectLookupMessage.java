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
package org.objectweb.proactive.core.remoteobject.http.message;

import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.http.HTTPRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.http.HttpRemoteObjectImpl;
import org.objectweb.proactive.core.remoteobject.http.util.HTTPRegistry;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMessage;


/**
 * This classes represents a HTTPMessage. When processed, this message performs a lookup thanks to the urn.
 * @author The ProActive Team
 * @see HttpMessage
 */
public class HttpRemoteObjectLookupMessage extends HttpMessage implements Serializable {
    private String urn;

    //Caller Side

    /**
     * Constructs an HTTP Message
     * @param urn The urn of the Object (it can be an active object or a runtime).
     */
    public HttpRemoteObjectLookupMessage(String urn, URI url, int port) {
        super(url.toString());
        this.urn = urn;
        if (!this.urn.startsWith("/")) {
            this.urn = "/" + urn;
        }
    }

    /**
     * Get the returned object.
     * @return the returned object
     */
    public RemoteRemoteObject getReturnedObject() {
        return (RemoteRemoteObject) this.returnedObject;
    }

    //Callee side

    /**
     * Performs the lookup
     * @return The Object associated with the urn
     */
    @Override
    public Object processMessage() {
        if (this.urn != null) {
            InternalRemoteRemoteObject irro = HTTPRegistry.getInstance().lookup(url);

            //            System.out.println("HttpRemoteObjectLookupMessage.processMessage() ++ ro at " + url +" : " +ro) ;
            if (irro != null) {
                RemoteRemoteObject rro = null;
                try {
                    rro = new HTTPRemoteObjectFactory().newRemoteObject(irro);
                    ((HttpRemoteObjectImpl) rro).setURI(URI.create(url));
                } catch (ProActiveException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                this.returnedObject = rro;
            }
        }
        return this.returnedObject;
    }
}
