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
package org.objectweb.proactive.core.body.ft.protocols.cic;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;


/**
 * This class implements a Communication Induced Checkpointing protocol for ProActive.
 * This FTManager is linked non active object communicating with fault-tolerant active objects.
 * @author cdelbe
 * @since ProActive 2.2
 */
public class HalfFTManagerCIC extends FTManager {
    // error message
    private static final String HALF_BODY_EXCEPTION_MESSAGE = "Cannot perform this call on a FTManager of a HalfBody";

    // message infos
    private char[] forSentMessage;

    //logger
    protected static Logger logger = Logger.getLogger(HalfFTManagerCIC.class.getName());

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#init(org.objectweb.proactive.core.body.AbstractBody)
     */
    public int init(AbstractBody owner) throws ProActiveException {
        super.init(owner);
        this.forSentMessage = new char[FTManagerCIC.INFOS_SIZE];
        this.forSentMessage[FTManagerCIC.FROM_HALF_BODY] = (char) FTManagerCIC.IS_HALF;
        return 0;
    }

    public int onReceiveReply(Reply reply) {
        reply.setFTManager(this);
        return 0;
    }

    public int onDeliverReply(Reply reply) {
        return FTManager.NON_FT;
    }

    public int onSendRequestBefore(Request request) {
        request.setMessageInfo(this.forSentMessage);
        return 0;
    }

    public int onSendReplyBefore(Reply reply) {
        reply.setMessageInfo(this.forSentMessage);
        return 0;
    }

    public int onSendRequestAfter(Request request, int rdvValue,
        UniversalBody destination) throws RenegotiateSessionException {
        if (rdvValue == FTManagerCIC.RESEND_MESSAGE) {
            try {
                request.resetSendCounter();
                request.setIgnoreIt(false);
                Thread.sleep(FTManagerCIC.TIME_TO_RESEND);
                int rdvValueBis = sendRequest(request, destination);
                return this.onSendRequestAfter(request, rdvValueBis, destination);
            } catch (RenegotiateSessionException e1) {
                throw e1;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public synchronized int onSendReplyAfter(Reply reply, int rdvValue,
        UniversalBody destination) {
        if (rdvValue == FTManagerCIC.RESEND_MESSAGE) {
            try {
                reply.setIgnoreIt(false);
                Thread.sleep(FTManagerCIC.TIME_TO_RESEND);
                int rdvValueBis = sendReply(reply, destination);
                return this.onSendReplyAfter(reply, rdvValueBis, destination);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public int sendRequest(Request r, UniversalBody destination)
        throws RenegotiateSessionException {
        try {
            this.onSendRequestBefore(r);
            int res;
            res = r.send(destination);
            this.onSendRequestAfter(r, res, destination);
            return res;
        } catch (IOException e) {
            logger.info("[FAULT] " + this.owner.getID() + " : FAILURE OF " +
                destination.getID() + " SUSPECTED ON REQUEST SENDING : " +
                e.getMessage());
            UniversalBody newDestination = this.communicationFailed(destination.getID(),
                    destination, e);
            return this.sendRequest(r, newDestination);
        } catch (RenegotiateSessionException e1) {
            throw e1;
        }
    }

    public int sendReply(Reply r, UniversalBody destination) {
        try {
            this.onSendReplyBefore(r);
            int res = r.send(destination);
            this.onSendReplyAfter(r, res, destination);
            return res;
        } catch (IOException e) {
            logger.info("[FAULT] " + this.owner.getID() + " : FAILURE OF " +
                destination.getID() + " SUSPECTED ON REPLY SENDING : " +
                e.getMessage());
            UniversalBody newDestination = this.communicationFailed(destination.getID(),
                    destination, e);
            return this.sendReply(r, newDestination);
        }
    }

    public UniversalBody communicationFailed(UniqueID suspect,
        UniversalBody suspectLocation, Exception e) {
        try {
            // send an adapter to suspectLocation: the suspected body could be local
            UniversalBody newLocation = this.location.searchObject(suspect,
                    suspectLocation.getRemoteAdapter(), this.owner.getID());
            if (newLocation == null) {
                while (newLocation == null) {
                    try {
                        // suspected is failed or is recovering
                        if (logger.isDebugEnabled()) {
                            logger.debug("[CIC] Waiting for recovery of " +
                                suspect);
                        }
                        Thread.sleep(FTManagerCIC.TIME_TO_RESEND);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    newLocation = this.location.searchObject(suspect,
                            suspectLocation.getRemoteAdapter(),
                            this.owner.getID());
                }
                return newLocation;
            } else {
                // newLocation is the new location of suspect
                return newLocation;
            }
        } catch (RemoteException e1) {
            logger.error("**ERROR** Location server unreachable");
            e1.printStackTrace();
            return null;
        }
    }

    ////////////////////////
    // UNCALLABLE METHODS //
    ////////////////////////
    public int onReceiveRequest(Request request) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    public int onDeliverRequest(Request request) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    public int onServeRequestBefore(Request request) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    public int onServeRequestAfter(Request request) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    public int beforeRestartAfterRecovery(CheckpointInfo ci, int inc) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    public int getIncarnation() {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    public int handleFTMessage(FTMessage fte) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }
}
