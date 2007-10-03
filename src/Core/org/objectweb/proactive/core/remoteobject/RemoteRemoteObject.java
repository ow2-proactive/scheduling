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
package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.security.SecurityEntity;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


/**
 *
 *
 * Remote interface for a remote object.
 *
 *
 */
public interface RemoteRemoteObject extends SecurityEntity {

    /**
     * Send a message containing a reified method call to a remote object. the target of the message
     * could be either the reified object or the remote object itself
     * @param message the reified method call
     * @return a reply containing the result of the method call
     * @throws ProActiveException
     * @throws RenegotiateSessionException if the security infrastructure needs to (re)initiate the session
     * @throws IOException if the message transfer has failed
     */
    public Reply receiveMessage(Request message)
        throws ProActiveException, IOException, RenegotiateSessionException;

    public Object getObjectProxy() throws ProActiveException, IOException;

    public void setObjectProxy(Object stub)
        throws ProActiveException, IOException;

    public URI getURI() throws ProActiveException, IOException;

    public void setURI(URI uri) throws ProActiveException, IOException;

    public String getClassName() throws ProActiveException, IOException;

    public String getProxyName() throws ProActiveException, IOException;

    public Class<?> getTargetClass() throws ProActiveException, IOException;

    public Class<?> getAdapterClass() throws ProActiveException, IOException;
}
