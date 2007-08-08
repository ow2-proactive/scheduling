package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.security.SecurityEntity;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


/**
 * A RemoteObject allows to turn a java object into a remotely accessible object.
 * According to the protocol selected, the remote object is going to register itself
 * on a registry.
 *
 */
public interface RemoteObject extends SecurityEntity {
    public Reply receiveMessage(Request message)
        throws ProActiveException, RenegotiateSessionException, IOException;

    public Object getObjectProxy() throws ProActiveException;

    public Object getObjectProxy(RemoteRemoteObject rro)
        throws ProActiveException;

    public String getClassName();

    public Class getTargetClass();

    public String getProxyName();

    public Class getAdapterClass();
}
