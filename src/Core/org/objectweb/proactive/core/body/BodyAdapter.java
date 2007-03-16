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
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;


/**
 * An adapter for a RemoteBody. The Adpater is the generic entry point for remote calls
 * to a RemoteBody using different protocols such as RMI, RMISSH, IBIS, HTTP, JINI.
 * This also allows to cache informations, and so to avoid crossing the network when calling some methods.
 * @author ProActiveTeam
 * @since ProActive 2.2
 * @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 */
public abstract class BodyAdapter implements UniversalBody, Serializable {

    /**
     * Cache the ID of the Body locally for speed
     */
    protected UniqueID bodyID;

    /**
     * Cache the jobID locally for speed
     */
    protected String jobID;

    /**
     * List all the Objects  (Runtimes, VN, Active Objects, Components...) registered in a registry.
     * @param url the url of the host
     * @return String [] containing the names of the Objects registered in the registry
     * @exception java.io.IOException if the given url does not harbour a registry
     */
    public abstract String[] list(String url) throws java.io.IOException;

    /**
     * Looks-up an active object previously registered in a registry. In fact it is the
     * remote version of the body of an active object that can be registered into the
     * Registry under a given URL.
     * @param url the url the remote Body is registered to
     * @return a UniversalBody
     * @exception java.io.IOException if the remote body cannot be found under the given url
     *      or if the object found is not of type RemoteBody
     */
    public abstract UniversalBody lookup(String url) throws java.io.IOException;

    /**
     * Registers an active object into protocol-specific registry. In fact it is the
     * remote version of the body of the active object that is registered into the
     * Registry under the given URL.
     * @param url the url under which the remote body is registered.
     * @exception java.io.IOException if the remote body cannot be registered
     */
    public abstract void register(String url) throws java.io.IOException;

    /**
     * Unregisters an active object previously registered into a registry.
     * @param url the url under which the active object is registered.
     * @exception java.io.IOException if the remote object cannot be removed from the registry
     */
    public abstract void unregister(String url) throws java.io.IOException;

    /**
     * Change the body referenced by this adapter.
     * @param newBody the body referenced after the call
     * @exception java.io.IOException if a pb occurs during this method call
     */
    public abstract void changeProxiedBody(Body newBody)
        throws IOException;
}
