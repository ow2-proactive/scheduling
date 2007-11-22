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
package org.objectweb.proactive.core.remoteobject.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


/**
 * RMI implementation of the remote remote object interface
 *
 *
 */
public class RmiRemoteObjectImpl extends UnicastRemoteObject
    implements RmiRemoteObject {
    protected InternalRemoteRemoteObject internalrrObject;

    public RmiRemoteObjectImpl() throws java.rmi.RemoteException {
    }

    public RmiRemoteObjectImpl(InternalRemoteRemoteObject target)
        throws java.rmi.RemoteException {
        this.internalrrObject = target;
    }

    public RmiRemoteObjectImpl(InternalRemoteRemoteObject target,
        RMIServerSocketFactory sf, RMIClientSocketFactory cf)
        throws java.rmi.RemoteException {
        super(0, cf, sf);
        this.internalrrObject = target;
    }

    public Reply receiveMessage(Request message)
        throws RemoteException, RenegotiateSessionException, ProActiveException,
            IOException {
        if (message.isOneWay()) {
            this.internalrrObject.receiveMessage(message);
            return null;
        }

        return this.internalrrObject.receiveMessage(message);
    }
}
