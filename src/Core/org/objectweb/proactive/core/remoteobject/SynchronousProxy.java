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
                ((Throwable) reply.getSynchResult()).printStackTrace();
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
