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
    public Reply receiveMessage(Request message)
        throws ProActiveException, IOException, RenegotiateSessionException;

    public Object getObjectProxy() throws ProActiveException, IOException;

    public void setObjectProxy(Object stub)
        throws ProActiveException, IOException;

    public URI getURI() throws ProActiveException, IOException;

    public void setURI(URI uri) throws ProActiveException, IOException;
}
