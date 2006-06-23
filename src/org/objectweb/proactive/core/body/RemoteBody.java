/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.SecurityEntity;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;


/**
 * An object implementing this interface provides the minimum service a body offers
 * remotely. This interface is extended by protocol-specific(RMI, RMI/SSH, IBIS, HTTP, JINI)
 * remote interfaces to allow the body to be accessed remotely.
 * @author ProActiveTeam
 * @version 1.0
 * @since ProActive 2.2
 * @see org.objectweb.proactive.core.body.UniversalBody
 * @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public interface RemoteBody extends Serializable, SecurityEntity {
    public static Logger bodyLogger = ProActiveLogger.getLogger(Loggers.BODY);

    /**
     * Receives a request for later processing. The call to this method is non blocking
     * unless the body cannot temporary receive the request.
     * @param r the request to process
     * @exception java.io.IOException if the request cannot be accepted
     * @return value for fault-tolerance protocol
     */
    public int receiveRequest(Request r)
        throws java.io.IOException, RenegotiateSessionException;

    /**
     * Receives a reply in response to a former request.
     * @param r the reply received
     * @exception java.io.IOException if the reply cannot be accepted
     * @return value for fault-tolerance protocol
     */
    public int receiveReply(Reply r) throws java.io.IOException;

    /**
     * Terminate the body. After this call the body is no more alive and no more active
     * although the active thread is not interrupted. The body is unuseable after this call.
     * @exception java.io.IOException if an exception occured during the remote communication
     */
    public void terminate() throws java.io.IOException;

    /**
     * Returns the url of the node this body is associated to
     * The url of the node can change if the active object migrates
     * @return the url of the node this body is associated to
     * @exception java.io.IOException if an exception occured during the remote communication
     */
    public String getNodeURL() throws java.io.IOException;

    /**
     * Returns the UniqueID of this body
     * This identifier is unique accross all JVMs
     * @return the UniqueID of this body
     * @exception java.io.IOException if an exception occured during the remote communication
     */
    public UniqueID getID() throws java.io.IOException;

    /**
     * @return the JobID of the remote body
     * @exception java.io.IOException if an exception occured during the remote communication
     */
    public String getJobID() throws java.io.IOException;

    /**
     * Signals to this body that the body identified by id is now to a new
     * remote location. The body given in parameter is a new stub pointing
     * to this new location. This call is a way for a body to signal to his
     * peer that it has migrated to a new location
     * @param id the id of the body
     * @param body the stub to the new location
     * @exception java.io.IOException if an exception occured during the remote communication
     */
    public void updateLocation(UniqueID id, UniversalBody body)
        throws java.io.IOException;

    /**
     * Enables automatic continuation mechanism for this body
     * @exception java.io.IOException if an exception occured during the remote communication
     */
    public void enableAC() throws java.io.IOException;

    /**
     * Disables automatic continuation mechanism for this body
     * @exception java.io.IOException if an exception occured during the remote communication
     */
    public void disableAC() throws java.io.IOException;

    /**
     * For setting an immediate service for this body.
     * An immediate service is a method that will bw excecuted by the calling thread.
     * @exception java.io.IOException if an exception occured during the remote communication
     */
    public void setImmediateService(String methodName)
        throws java.io.IOException;

    public void setImmediateService(String methodName, Class[] parametersTypes)
        throws IOException;

    public void removeImmediateService(String methodName,
        Class[] parametersTypes) throws IOException;

    /**
     * For sending a message to the FTManager linked to this object
     * @param fte the message
     * @return depends on the message meaning
     * @exception java.io.IOException if an exception occured during the remote communication
     */
    public Object receiveFTMessage(FTMessage fte) throws IOException;

    /**
     * Change the body referenced by this adapter
     * @param newBody the body referenced after the call
     * @exception java.io.IOException if an exception occured during the remote communication
     */
    public void changeProxiedBody(Body newBody) throws java.io.IOException;

    public void addNFEListener(NFEListener listener) throws java.io.IOException;

    public void removeNFEListener(NFEListener listener)
        throws java.io.IOException;

    public int fireNFE(NonFunctionalException e) throws java.io.IOException;
}
