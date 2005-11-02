/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 *
 * @author ProActive Team
 *
 */
public interface UniversalBodyForwarder {
    public static Logger bodyLogger = ProActiveLogger.getLogger(Loggers.BODY);

    public int receiveRequest(UniqueID id, Request request)
        throws java.io.IOException, RenegotiateSessionException;

    public int receiveReply(UniqueID id, Reply r) throws java.io.IOException;

    public String getNodeURL(UniqueID id);

    public UniqueID getID(UniqueID id);

    public void updateLocation(UniqueID id, UniqueID uid, UniversalBody body)
        throws java.io.IOException;

    public void createShortcut(UniqueID id, Shortcut shortcut)
        throws java.io.IOException;

    public BodyAdapter getRemoteAdapter(UniqueID id);

    public void terminate(UniqueID id) throws java.io.IOException;

    public void enableAC(UniqueID id) throws java.io.IOException;

    public void disableAC(UniqueID id) throws java.io.IOException;

    public void setImmediateService(UniqueID id, String methodName)
        throws IOException;

    public void setImmediateService(UniqueID id, String methodName,
        Class[] parametersTypes) throws IOException;

    public void removeImmediateService(UniqueID id, String methodName,
        Class[] parametersTypes) throws IOException;

    // 
    // -- SECURITY
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

    public ArrayList getEntities(UniqueID id)
        throws SecurityNotAvailableException, IOException;

    public void terminateSession(UniqueID id, long sessionID)
        throws SecurityNotAvailableException, IOException;

    // FAULT TOLERANCE
    public Object receiveFTMessage(UniqueID id, FTMessage ev)
        throws IOException;

    public String getJobID(UniqueID id) throws IOException;

    // Following methods are not in UniversalBody but are needed
    public void addNFEListener(UniqueID id, NFEListener listener)
        throws java.io.IOException;

    public void removeNFEListener(UniqueID id, NFEListener listener)
        throws java.io.IOException;

    public int fireNFE(UniqueID id, NonFunctionalException e)
        throws java.io.IOException;

    // These four ones are needed because call is tramsited to the 
    // original body *adapter* on the forwarder    
    public void changeProxiedBody(UniqueID id, Body newBody)
        throws IOException;

    public abstract UniversalBody lookup(UniqueID id, String url)
        throws java.io.IOException;

    public abstract void register(UniqueID id, String url)
        throws java.io.IOException;

    public abstract void unregister(UniqueID id, String url)
        throws java.io.IOException;
}
