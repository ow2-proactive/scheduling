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
package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.security.SecurityEntity;


/**
 * An InternalRemoteRemoteObject is a generic object owns by any remote remote object.
 * It seats between the protocol dependant part of the remote object on the server
 * side (the XXXRemoteObjectImpl) and the remote object implementation.
 * It handles all the requests related to a given communication protocol on a remote object.
 * Whereas it can be seen as a protocol dependant object, its behaviour is the same
 * for all the implementation of any protocol. This is why it is an internal remote remote
 * object, hidden to the level on the protocol dependant part of the remote object it represents
 * that only provides a transport layer
 */
public interface InternalRemoteRemoteObject extends RemoteRemoteObject,
    SecurityEntity {


	/**
	 * @return returns the URI where its protocol dependant remote object is bound
	 * @throws ProActiveException
	 * @throws IOException
	 */
	public URI getURI() throws ProActiveException, IOException;

    /**
     * @param uri sets the URI where  its protocol dependant remote object is bound
     * @throws ProActiveException
     * @throws IOException
     */
    public void setURI(URI uri) throws ProActiveException, IOException;

    /**
     * @return returns the protocol specific remote object this internal remote remote
     * object represents a.k.a.  the remote remote object
     */
    public RemoteRemoteObject getRemoteRemoteObject();

    /**
     * @param remoteRemoteObject set the protocol specific remote object this internal
     * remote remote object represents a.k.a. the remote remote object
     */
    public void setRemoteRemoteObject(RemoteRemoteObject remoteRemoteObject);

    /**
     * @return returns the remote object this internal remote remote represents
     */
    public RemoteObject getRemoteObject();

    /**
     * @param remoteObject sets  the remote object this internal remote remote represents
     */
    public void setRemoteObject(RemoteObject remoteObject);

    /**
     * @return returns a proxy to the remote object. The type of the the proxy is a
     * subclass of the reified remote object. The proxy will point to the remote remote
     * object that owns this internal remote remote object.
     */
    public Object getObjectProxy();
}
