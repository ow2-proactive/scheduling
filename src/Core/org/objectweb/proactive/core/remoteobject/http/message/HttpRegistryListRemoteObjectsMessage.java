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

import org.objectweb.proactive.core.remoteobject.http.util.HTTPRegistry;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMessage;


/**
 * This classes represents a HTTPMessage. When processed, this message performs a lookup thanks to the urn.
 * @author vlegrand
 * @see HttpMessage
 */
public class HttpRegistryListRemoteObjectsMessage extends HttpMessage implements Serializable {
    private String urn;

    //Caller Side

    /**
     * Constructs an HTTP Message
     * @param urn The urn of the Object (it can be an active object or a runtime).
     */
    public HttpRegistryListRemoteObjectsMessage(URI url) {
        super(url.toString());
    }

    /**
     * Get the returned object.
     * @return the returned object
     */
    public String[] getReturnedObject() {
        return (String[]) this.returnedObject;
    }

    //Callee side

    /**
     * Performs the lookup
     * @return The Object associated with the urn
     */
    @Override
    public Object processMessage() {
        String[] uri = HTTPRegistry.getInstance().list();
        //            System.out.println("HttpRemoteObjectLookupMessage.processMessage() ++ ro at " + url +" : " +ro) ;
        this.returnedObject = uri;
        return this.returnedObject;
    }
}
