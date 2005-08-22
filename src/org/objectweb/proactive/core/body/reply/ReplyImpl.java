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
package org.objectweb.proactive.core.body.reply;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.FutureResult;
import org.objectweb.proactive.core.body.message.MessageImpl;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.ext.security.CommunicationForbiddenException;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


public class ReplyImpl extends MessageImpl implements Reply,
    java.io.Serializable {

    /**
     * The hypothetic result
     */
    protected FutureResult result;

    //security  features

    /**
     * the encrypted result
     */
    protected byte[][] encryptedResult;

    /*
     * the session ID used to find the key and decrypt the reply
     */
    protected long sessionID;
    protected transient ProActiveSecurityManager psm = null;

    public ReplyImpl(UniqueID senderID, long sequenceNumber, String methodName,
        FutureResult result, ProActiveSecurityManager psm) {
        super(senderID, sequenceNumber, true, methodName);
        this.result = result;
        this.psm = psm;
    }

    public FutureResult getResult() {
        return result;
    }

    public int send(UniversalBody destinationBody) throws java.io.IOException {
        // if destination body is on the same VM that the sender, we must perform 
        // a deep copy of result in order to preserve ProActive model.
        UniqueID destinationID = destinationBody.getID();
        boolean isLocal = ((LocalBodyStore.getInstance().getLocalBody(destinationID) != null) ||
            (LocalBodyStore.getInstance().getLocalHalfBody(destinationID) != null));

        if (isLocal) {
            result = (FutureResult) Utils.makeDeepCopy(result);
        }

        // security issue	
        //	System.out.println("ReplyImpl send : Current Thread " + Thread.currentThread().getName() + " result : " + result + "!");
        if (!ciphered) {
            long sessionID = 0;

            try {
                sessionID = psm.getSessionIDTo(destinationBody.getCertificate());

                if (sessionID == 0) {
                    psm.initiateSession(SecurityContext.COMMUNICATION_SEND_REPLY_TO,
                        destinationBody);
                    sessionID = psm.getSessionIDTo(destinationBody.getCertificate());
                }

                if (sessionID != 0) {
                    encryptedResult = psm.encrypt(sessionID, result);

                    ciphered = true;
                    this.sessionID = sessionID;
                }

                //   result = null;
            } catch (SecurityNotAvailableException e) {
                // do nothing       
            } catch (IOException e) {
                // throws exception 
                throw e;
            } catch (CommunicationForbiddenException e) {
                e.printStackTrace();
            } catch (AuthenticationException e) {
                e.printStackTrace();
            } catch (RenegotiateSessionException e) {
                e.printStackTrace();
            }
        }

        // end security
        // fault-tolerance returned value
        int ftres = destinationBody.receiveReply(this);
        return ftres;
    }

    // security issue
    public boolean isCiphered() {
        return ciphered;
    }

    public boolean decrypt(ProActiveSecurityManager psm)
        throws RenegotiateSessionException {
        if ((sessionID != 0) && ciphered) {
            byte[] decryptedMethodCall = psm.decrypt(sessionID, encryptedResult);
            try {
                ByteArrayInputStream bin = new ByteArrayInputStream(decryptedMethodCall);
                ObjectInputStream in = new ObjectInputStream(bin);
                result = (FutureResult) in.readObject();
                in.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.reply.Reply#getSessionId()
     */
    public long getSessionId() {
        return sessionID;
    }
}
