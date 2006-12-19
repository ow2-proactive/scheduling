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
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

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
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.ext.security.securityentity.Entity;


/**
 * An object implementing this interface provides the minimum service a body forwarder offers
 * remotely. This interface is extended by protocol-specific(RMI, RMI/SSH, IBIS, HTTP, JINI)
 * remote interfaces to allow the body to be accessed remotely.
 * @author ProActiveTeam
 * @see org.objectweb.proactive.core.body.UniversalBodyForwarder
 */
public interface RemoteBodyForwarder {
    public static Logger bodyLogger = ProActiveLogger.getLogger(Loggers.BODY);

    /**
     * @see RemoteBody#receiveRequest(Request)
     */
    public int receiveRequest(UniqueID id, Request r)
        throws java.io.IOException, RenegotiateSessionException;

    /**
     * @see RemoteBody#receiveReply(Reply)
     */
    public int receiveReply(UniqueID id, Reply r) throws java.io.IOException;

    /**
     * @see RemoteBody#terminate()
     */
    public void terminate(UniqueID id) throws java.io.IOException;

    /**
     * @see RemoteBody#getNodeURL()
     */
    public String getNodeURL(UniqueID id) throws java.io.IOException;

    /**
     * @see RemoteBody#getID()
     */
    public UniqueID getID(UniqueID id) throws java.io.IOException;

    /**
     * @see RemoteBody#getJobID()
     */
    public String getJobID(UniqueID id) throws java.io.IOException;

    /**
     * @see RemoteBody#updateLocation(UniqueID, UniversalBody)
     */
    public void updateLocation(UniqueID id, UniqueID uid, UniversalBody body)
        throws java.io.IOException;

    /**
     * @see RemoteBody#enableAC()
     */
    public void enableAC(UniqueID id) throws java.io.IOException;

    /**
     * @see RemoteBody#disableAC()
     */
    public void disableAC(UniqueID id) throws java.io.IOException;

    /**
     * @see RemoteBody#setImmediateService(String)
     */
    public void setImmediateService(UniqueID id, String methodName)
        throws java.io.IOException;

    /**
     * @see RemoteBody#setImmediateService(String, Class[])
     */
    public void setImmediateService(UniqueID id, String methodName,
        Class[] parametersTypes) throws IOException;

    /**
     * @see RemoteBody#removeImmediateService(String, Class[])
     */
    public void removeImmediateService(UniqueID id, String methodName,
        Class[] parametersTypes) throws IOException;

    //
    // -- Security
    //
    public X509Certificate getCertificate(UniqueID id)
        throws SecurityNotAvailableException, IOException;

    public long startNewSession(UniqueID id, Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException;

    public PublicKey getPublicKey(UniqueID id)
        throws SecurityNotAvailableException, IOException;

    public byte[] randomValue(UniqueID id, long sessionID,
        byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException;

    public byte[][] publicKeyExchange(UniqueID id, long sessionID,
        byte[] myPublicKey, byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            KeyExchangeException, IOException;

    public byte[][] secretKeyExchange(UniqueID id, long sessionID,
        byte[] encodedAESKey, byte[] encodedIVParameters,
        byte[] encodedClientMacKey, byte[] encodedLockData,
        byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException;

    public SecurityContext getPolicy(UniqueID id,
        SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException;

    public byte[] getCertificateEncoded(UniqueID id)
        throws SecurityNotAvailableException, IOException;

    public ArrayList<Entity> getEntities(UniqueID id)
        throws SecurityNotAvailableException, IOException;

    public void terminateSession(UniqueID id, long sessionID)
        throws SecurityNotAvailableException, IOException;

    public Object receiveFTMessage(UniqueID id, FTMessage fte)
        throws IOException;

    public void changeProxiedBody(UniqueID id, Body newBody)
        throws java.io.IOException;

    //
    // -- NFE
    //
    public void addNFEListener(UniqueID id, NFEListener listener)
        throws java.io.IOException;

    public void removeNFEListener(UniqueID id, NFEListener listener)
        throws java.io.IOException;

    public int fireNFE(UniqueID id, NonFunctionalException e)
        throws java.io.IOException;

    //
    // -- BodyAdapter
    //
    public UniversalBody lookup(UniqueID id, String url)
        throws java.io.IOException;

    public void unregister(UniqueID id, String url) throws java.io.IOException;

    public void register(UniqueID id, String url) throws java.io.IOException;

    public BodyAdapter getRemoteAdapter(UniqueID id) throws java.io.IOException;
}
