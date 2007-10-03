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
 *
 */
public interface RemoteObject extends SecurityEntity {

    /**
     * Send a message containing a reified method call to a remote object
     * @param message the reified method call
     * @return a reply containing the result of the method call
     * @throws ProActiveException
     * @throws RenegotiateSessionException if the security infrastructure needs to (re)initiate the session
     * @throws IOException if the message transfer has failed
     */
    public Reply receiveMessage(Request message)
        throws ProActiveException, RenegotiateSessionException, IOException;

    /**
     * @return return a couple stub + proxy pointing on the current remote object
     * @throws ProActiveException
     */
    public Object getObjectProxy() throws ProActiveException;

    /**
     *
     * @param rro
     * @return return a couple stub + proxy pointing on a reference on a remote object identified by rro
     * @throws ProActiveException
     */
    public Object getObjectProxy(RemoteRemoteObject rro)
        throws ProActiveException;

    /**
     * @return return the classname of the reified object
     */
    public String getClassName();

    /**
     * @return return the class of the reified object
     */
    public Class<?> getTargetClass();

    /**
     * @return return the proxy's classname of the reified object
     */
    public String getProxyName();

    /**
     * @see org.objectweb.proactive.core.remoteobject.adapter.Adapter
     * @return return the <code>class</code> of the adapter of this remote object
     */
    public Class<?> getAdapterClass();
}
