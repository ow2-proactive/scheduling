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

import java.io.Serializable;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;


/**
 *
 *
 */
public class SynchronousProxy implements Proxy, Serializable {
    protected RemoteObject remoteObject;

    public SynchronousProxy(ConstructorCall contructorCall, Object[] params)
        throws ProActiveException {
        Object p0 = params[0];

        if (p0 instanceof RemoteObject) {
            this.remoteObject = (RemoteObject) p0;
        } else if (p0 instanceof RemoteRemoteObject) {
            this.remoteObject = new RemoteObjectAdapter((RemoteRemoteObject) p0);
        }
    }

    public Object reify(MethodCall c) throws Throwable {
        Request r = new RequestImpl(c,
                c.getReifiedMethod().getReturnType().equals(java.lang.Void.TYPE));

        SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(r);

        if (reply != null) {
            if (reply.getSynchResult() instanceof Throwable) {
                throw (Throwable) reply.getSynchResult();
            }

            return reply.getSynchResult();
        }

        return null;
    }

    public Object receiveMessage(Request m) throws Throwable {
        SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(m);

        if (reply != null) {
            if (reply.getSynchResult() instanceof Throwable) {
                throw (Throwable) reply.getSynchResult();
            }

            return reply.getSynchResult();
        }

        return null;
    }

    public void setRemoteObject(RemoteRemoteObject rro)
        throws ProActiveException {
        this.remoteObject = new RemoteObjectAdapter(rro);
    }

    public void setRemoteObject(RemoteObject ro) {
        this.remoteObject = ro;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SynchronousProxy) {
            return this.remoteObject.equals(((SynchronousProxy) o).remoteObject);
        }

        return false;
    }
}
