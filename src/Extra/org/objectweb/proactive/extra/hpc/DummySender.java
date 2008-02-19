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
package org.objectweb.proactive.extra.hpc;

import java.io.IOException;
import java.security.AccessControlException;
import java.security.PublicKey;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;


public class DummySender implements UniversalBody {

    //
    // ---- DUMMY SENDER ---
    //

    private static final long serialVersionUID = 1L;
    private static DummySender instance = new DummySender();

    public static DummySender getDummySender() {
        return instance;
    }

    public String getNodeURL() {
        return "Unknown";
    }

    //
    // ---- DUMMY SENDER ---
    //

    public void createShortcut(Shortcut shortcut) throws IOException {
        // TODO Auto-generated method stub

    }

    public void disableAC() throws IOException {
        // TODO Auto-generated method stub

    }

    public void enableAC() throws IOException {
        // TODO Auto-generated method stub

    }

    public UniqueID getID() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getReifiedClassName() {
        // TODO Auto-generated method stub
        return null;
    }

    public UniversalBody getRemoteAdapter() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object receiveFTMessage(FTMessage ev) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public GCResponse receiveGCMessage(GCMessage toSend) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public int receiveReply(Reply r) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int receiveRequest(Request request) throws IOException, RenegotiateSessionException {
        // TODO Auto-generated method stub
        return 0;
    }

    public void register(String url) throws IOException, UnknownProtocolException {
        // TODO Auto-generated method stub

    }

    public void setRegistered(boolean registered) throws IOException {
        // TODO Auto-generated method stub

    }

    public void updateLocation(UniqueID id, UniversalBody body) throws IOException {
        // TODO Auto-generated method stub

    }

    public String getJobID() {
        // TODO Auto-generated method stub
        return null;
    }

    public TypedCertificate getCertificate() throws SecurityNotAvailableException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Entities getEntities() throws SecurityNotAvailableException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public SecurityContext getPolicy(Entities local, Entities distant) throws SecurityNotAvailableException,
            IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
            throws SecurityNotAvailableException, AccessControlException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public PublicKey getPublicKey() throws SecurityNotAvailableException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] publicKeyExchange(long sessionID, byte[] signature) throws SecurityNotAvailableException,
            RenegotiateSessionException, KeyExchangeException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue) throws SecurityNotAvailableException,
            RenegotiateSessionException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey, byte[] encodedIVParameters,
            byte[] encodedClientMacKey, byte[] encodedLockData, byte[] parametersSignature)
            throws SecurityNotAvailableException, RenegotiateSessionException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setProActiveSecurityManager(Entity user, PolicyServer policyServer)
            throws SecurityNotAvailableException, AccessControlException, IOException {
        // TODO Auto-generated method stub

    }

    public long startNewSession(long distantSessionID, SecurityContext policy,
            TypedCertificate distantCertificate) throws SessionException, SecurityNotAvailableException,
            IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    public void terminateSession(long sessionID) throws SecurityNotAvailableException, IOException {
        // TODO Auto-generated method stub

    }
}
