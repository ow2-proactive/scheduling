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
package org.objectweb.proactive.core.body.ft.protocols.pmlrb.managers;

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
import org.objectweb.proactive.core.body.ft.protocols.pmlrb.infos.MessageInfoPMLRB;
import org.objectweb.proactive.core.body.ft.servers.location.LocationServer;
import org.objectweb.proactive.core.body.ft.service.FaultToleranceTechnicalService;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class implements a Pessimistic Message Logging protocol for ProActive.
 * This FTManager is linked non active object communicating with fault-tolerant active objects.
 * @author The ProActive Team
 * @since 3.0
 */
public class HalfFTManagerPMLRB extends FTManager {

    /**
     *
     */

    //logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE_PML);

    //sequence number of sending for any messages
    private char sendNumber;
    private MessageInfoPMLRB requestInfos;

    /**
     * FTManager initialization.
     * @param owner the attached body.
     */
    @Override
    public int init(AbstractBody owner) throws ProActiveException {
        // a half body need only a location server...
        Node node = NodeFactory.getNode(owner.getNodeURL());
        try {
            String urlGlobal = node.getProperty(FaultToleranceTechnicalService.GLOBAL_SERVER);
            if (urlGlobal != null) {
                this.location = (LocationServer) (Naming.lookup(urlGlobal));
            } else {
                String urlLocation = node.getProperty(FaultToleranceTechnicalService.LOCATION_SERVER);
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
            throw new ProActiveException("Unable to init HalfFTManager : FT is disable.", e);
        } catch (RemoteException e) {
            throw new ProActiveException("Unable to init HalfFTManager : FT is disable.", e);
        } catch (NotBoundException e) {
            throw new ProActiveException("Unable to init HalfFTManager : FT is disable.", e);
        }
        this.sendNumber = 0;
        this.requestInfos = new MessageInfoPMLRB();
        logger.info(" PML fault-tolerance is enabled for half body " + this.ownerID);
        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onReceiveReply(org.objectweb.proactive.core.body.reply.Reply)
     */
    @Override
    public int onReceiveReply(Reply reply) {
        reply.setFTManager(this);
        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onDeliverReply(org.objectweb.proactive.core.body.reply.Reply)
     */
    @Override
    public int onDeliverReply(Reply reply) {
        return FTManager.NON_FT;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onSendReplyBefore(org.objectweb.proactive.core.body.reply.Reply)
     */
    @Override
    public int onSendReplyBefore(Reply reply) {
        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onSendReplyAfter(org.objectweb.proactive.core.body.reply.Reply, int, org.objectweb.proactive.core.body.UniversalBody)
     */
    @Override
    public int onSendReplyAfter(Reply reply, int rdvValue, UniversalBody destination) {
        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onSendRequestBefore(org.objectweb.proactive.core.body.request.Request)
     */
    @Override
    public int onSendRequestBefore(Request request) {
        this.requestInfos.sentSequenceNumber = this.getNextSendNumber();
        request.setMessageInfo(this.requestInfos);
        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onSendRequestAfter(org.objectweb.proactive.core.body.request.Request, int, org.objectweb.proactive.core.body.UniversalBody)
     */
    @Override
    public int onSendRequestAfter(Request request, int rdvValue, UniversalBody destination)
            throws RenegotiateSessionException {
        return 0;
    }

    private synchronized char getNextSendNumber() {
        return ++sendNumber;
    }

    ////////////////////////
    // UNCALLABLE METHODS //
    ////////////////////////
    @Override
    public int onReceiveRequest(Request request) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    @Override
    public int onDeliverRequest(Request request) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    @Override
    public int onServeRequestBefore(Request request) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    @Override
    public int onServeRequestAfter(Request request) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    @Override
    public int beforeRestartAfterRecovery(CheckpointInfo ci, int inc) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    public int getIncarnation() {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    @Override
    public Object handleFTMessage(FTMessage fte) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }
}
