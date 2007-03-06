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
package org.objectweb.proactive.core.body.ft.protocols.cic.managers;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.ft.protocols.cic.infos.MessageInfoCIC;
import org.objectweb.proactive.core.body.ft.servers.location.LocationServer;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;


/**
 * This class implements a Communication Induced Checkpointing protocol for ProActive.
 * This FTManager is linked non active object communicating with fault-tolerant active objects.
 * @author cdelbe
 * @since ProActive 2.2
 */
public class HalfFTManagerCIC extends FTManager {
    // message infos
    private MessageInfoCIC forSentMessage;

    //logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE_PML);

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#init(org.objectweb.proactive.core.body.AbstractBody)
     */
    public int init(AbstractBody owner) throws ProActiveException {
        //super.init(owner);
        // a half body need only a location server...
        try {
            String urlGlobal = ProActiveConfiguration.getGlobalFTServer();
            if (urlGlobal != null) {
                this.location = (LocationServer) (Naming.lookup(urlGlobal));
            } else {
                String urlLocation = ProActiveConfiguration.getLocationServer();
                if (urlLocation != null) {
                    this.location = (LocationServer) (Naming.lookup(urlLocation));
                } else {
                    throw new ProActiveException(
                        "Unable to init HalfFTManager : servers are not correctly set");
                }
            }
            this.storage = null;
            this.recovery = null;
        } catch (MalformedURLException e) {
            throw new ProActiveException("Unable to init HalfFTManager : FT is disable.",
                e);
        } catch (RemoteException e) {
            throw new ProActiveException("Unable to init HalfFTManager : FT is disable.",
                e);
        } catch (NotBoundException e) {
            throw new ProActiveException("Unable to init HalfFTManager : FT is disable.",
                e);
        }
        this.forSentMessage = new MessageInfoCIC();
        this.forSentMessage.fromHalfBody = true;
        logger.info(" CIC fault-tolerance is enabled for half body " +
            this.ownerID);
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

    public Object handleFTMessage(FTMessage fte) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }
}
