/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.Job;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.exceptions.Handlerizable;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.CommunicationForbiddenException;
import org.objectweb.proactive.ext.security.Policy;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 * An object implementing this interface provides the minimum service a body offers
 * remotely or locally. This interface is the generic version that is used remotely
 * and locally. A body accessed from the same JVM offers all services of this interface,
 * plus the services defined in the Body interface.
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 * @see org.objectweb.proactive.Body
 * @see org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter
 */
public interface UniversalBody extends Handlerizable, Job {

    /**
     * Receives a request for later processing. The call to this method is non blocking
     * unless the body cannot temporary receive the request.
     * @param request the request to process
     * @exception java.io.IOException if the request cannot be accepted
     */
    public void receiveRequest(Request request)
        throws java.io.IOException, RenegotiateSessionException;

    /**
     * Receives a reply in response to a former request.
     * @param r the reply received
     * @exception java.io.IOException if the reply cannot be accepted
     */
    public void receiveReply(Reply r) throws java.io.IOException;

    /**
     * Returns the url of the node this body is associated to
     * The url of the node can change if the active object migrates
     * @return the url of the node this body is associated to
     */
    public String getNodeURL();

    /**
     * Returns the UniqueID of this body
     * This identifier is unique accross all JVMs
     * @return the UniqueID of this body
     */
    public UniqueID getID();

    /**
     * Signals to this body that the body identified by id is now to a new
     * remote location. The body given in parameter is a new stub pointing
     * to this new location. This call is a way for a body to signal to his
     * peer that it has migrated to a new location
     * @param id the id of the body
     * @param body the stub to the new location
     */
    public void updateLocation(UniqueID id, UniversalBody body)
        throws java.io.IOException;

    /**
     * Returns the remote friendly version of this body
     * @return the remote friendly version of this body
     */
    public UniversalBody getRemoteAdapter();

    /**
     * Enables automatic continuation mechanism for this body
     */
    public void enableAC() throws java.io.IOException;

    /**
     * Disables automatic continuation mechanism for this body
     */
    public void disableAC() throws java.io.IOException;

    /**
     * For setting an immediate service for this body.
     * An immediate service is a method that will bw excecuted by the calling thread.
     */
    public void setImmediateService(String methodName)
        throws IOException;

    // SECURITY
    public void initiateSession(int type, UniversalBody body)
        throws java.io.IOException, CommunicationForbiddenException, 
            AuthenticationException, RenegotiateSessionException, 
            SecurityNotAvailableException;

    public void terminateSession(long sessionID)
        throws java.io.IOException, SecurityNotAvailableException;

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, java.io.IOException;

    public ProActiveSecurityManager getProActiveSecurityManager()
        throws SecurityNotAvailableException, java.io.IOException;

    public Policy getPolicyFrom(X509Certificate certificate)
        throws SecurityNotAvailableException, java.io.IOException;

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, java.io.IOException, 
            RenegotiateSessionException;

    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws SecurityNotAvailableException, KeyExchangeException, 
            java.io.IOException;

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, java.io.IOException;

    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, Exception;

    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
        byte[] sig_code)
        throws SecurityNotAvailableException, Exception, 
            RenegotiateSessionException;

    byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws SecurityNotAvailableException, Exception, 
            RenegotiateSessionException;

    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException, java.io.IOException;

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, java.io.IOException;

    /**
     * @return name of the virtual node where the object has been created
     */
    public String getVNName()
        throws SecurityNotAvailableException, java.io.IOException;

    /**
     * @return object's X509Certificate as byte array
     */
    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException;

    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException;    
}
