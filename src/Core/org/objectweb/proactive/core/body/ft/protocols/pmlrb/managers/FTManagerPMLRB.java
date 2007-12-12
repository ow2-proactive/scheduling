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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.ft.protocols.pmlrb.infos.CheckpointInfoPMLRB;
import org.objectweb.proactive.core.body.ft.protocols.pmlrb.infos.MessageInfoPMLRB;
import org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.message.Message;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.MutableLong;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class defines the fault-tolerance manager for the Pessimistic Message Logging protocol,
 * Receiver Based approach.
 * @author cdelbe
 * @since 3.0
 */
public class FTManagerPMLRB extends FTManager {

    /**
         *
         */

    /** Incarantion is not used for PML. Set to a default value */
    public static final int INC_VALUE = Integer.MAX_VALUE;

    /** Value returned if the message is ignored */
    public static final int IGNORED_MSG = -1;

    //logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE_PML);

    // index of the latest received messae per senderID
    // UniqueID <-> MutableLong
    private Hashtable<UniqueID, MutableLong> latestReceivedIndex;

    // true if this oa is recovering
    private boolean isRecovering;

    // timer
    private long checkpointTimer;

    //sequence number of sending for any messages
    private char sendNumber;
    private MessageInfoPMLRB replyInfos;
    private MessageInfoPMLRB requestInfos;

    // After a recovery, the last message of the log could be resend by its sender:
    // if the failure has occured between the logging and the end of the RDV.
    // Identify possible duplicatas
    private transient UniqueID potentialDuplicataSender;
    private transient long potentialDuplicataSequence;

    /**
     * FTManager initialization.
     * @param owner the attached body.
     */
    @Override
    public int init(AbstractBody owner) throws ProActiveException {
        super.init(owner);
        this.latestReceivedIndex = new Hashtable<UniqueID, MutableLong>();
        this.isRecovering = false;
        //checkpoint timer init: a checkpoint must be taken before any request service
        this.checkpointTimer = 0;
        // this.checkpointTimer = System.currentTimeMillis();
        this.sendNumber = 0;
        this.replyInfos = new MessageInfoPMLRB();
        this.requestInfos = new MessageInfoPMLRB();
        this.potentialDuplicataSender = null;
        this.potentialDuplicataSequence = 0;
        logger.info(" PML fault-tolerance is enabled for body " + this.ownerID);
        return 0;
    }

    /**
     * Message can be ignored if its index is l.t. lastestReceivedIndex[sender]
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onReceiveReply(org.objectweb.proactive.core.body.reply.Reply)
     */
    @Override
    public int onReceiveReply(Reply reply) {
        // if the message is sent by a non ft object
        if (reply.getMessageInfo() == null) {
            reply.setFTManager(this);
            return 0;
        }

        // Automatic continuation ---> Replies are not in sequence
        // we thus cannot block ac replies
        if (!reply.isAutomaticContinuation()) {
            if (this.alreadyReceived(reply)) {
                // this message has already been received. Ignore it
                reply.setIgnoreIt(true);
                return IGNORED_MSG;
            }
        }
        reply.setFTManager(this);
        return 0;
    }

    /**
     * Message can be ignored if its index is l.t. lastestReceivedIndex[sender]
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onReceiveRequest(org.objectweb.proactive.core.body.request.Request)
     */
    @Override
    public int onReceiveRequest(Request request) {
        // if the message is sent from a non ft object
        if (request.getMessageInfo() == null) {
            request.setFTManager(this);
            return 0;
        }
        if (this.alreadyReceived(request)) {
            // this message has already been received. Ignore it
            request.setIgnoreIt(true);
            return IGNORED_MSG;
        }
        request.setFTManager(this);
        return 0;
    }

    /**
     * Message must be synchronously logged before being delivered.
     * The LatestRcvdIndex table is updated
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onDeliverReply(org.objectweb.proactive.core.body.reply.Reply)
     */
    @Override
    public int onDeliverReply(Reply reply) {
        // if the ao is recovering, message are not logged
        if (!this.isRecovering) {
            try {
                // log the message
                this.storage.storeReply(this.ownerID, reply);
                // update latestIndex table
                this.updateLatestRvdIndexTable(reply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * Message must be synchronously logged before being delivered.
     * The LatestRcvdIndex table is updated
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onReceiveRequest(org.objectweb.proactive.core.body.request.Request)
     */
    @Override
    public int onDeliverRequest(Request request) {
        // if the ao is recovering, message are not logged
        if (!this.isRecovering) {
            try {
                // log the message
                this.storage.storeRequest(this.ownerID, request);
                // update latestIndex table
                this.updateLatestRvdIndexTable(request);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /*
     * Set the value of m.sourceBody to m.seqNumber
     */
    private void updateLatestRvdIndexTable(Message m) {
        // the first message from this sender?
        MutableLong index = (this.latestReceivedIndex.get(m.getSourceBodyID()));
        MessageInfoPMLRB mi = (MessageInfoPMLRB) (m.getMessageInfo());
        if (mi == null) {
            // from a not ft
            return;
        }
        long msgIndex = mi.sentSequenceNumber;
        if (index != null) {
            index.setValue(msgIndex);
        } else {
            //first message
            this.latestReceivedIndex.put(m.getSourceBodyID(),
                new MutableLong(msgIndex));
        }
    }

    /*
     * Return true if this message has already been received
     */
    private boolean alreadyReceived(Message m) {
        if ((this.potentialDuplicataSender != null) &&
                (m.getSourceBodyID().equals(this.potentialDuplicataSender)) &&
                (m.getSequenceNumber() == this.potentialDuplicataSequence)) {
            // this message has been already logged just before the failure of this.
            // no more such message can appear...
            this.potentialDuplicataSender = null;
            return true;
        } else {
            long msgIndex = ((MessageInfoPMLRB) (m.getMessageInfo())).sentSequenceNumber;
            MutableLong index = (this.latestReceivedIndex.get(m.getSourceBodyID()));
            return (index != null) && (msgIndex <= index.getValue());
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onSendReplyBefore(org.objectweb.proactive.core.body.reply.Reply)
     */
    @Override
    public synchronized int onSendReplyBefore(Reply reply) {
        this.replyInfos.sentSequenceNumber = this.getNextSendNumber();
        reply.setMessageInfo(this.replyInfos);
        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onSendReplyAfter(org.objectweb.proactive.core.body.reply.Reply, int, org.objectweb.proactive.core.body.UniversalBody)
     */
    @Override
    public int onSendReplyAfter(Reply reply, int rdvValue,
        UniversalBody destination) {
        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onSendRequestBefore(org.objectweb.proactive.core.body.request.Request)
     */
    @Override
    public synchronized int onSendRequestBefore(Request request) {
        this.requestInfos.sentSequenceNumber = this.getNextSendNumber();
        request.setMessageInfo(this.requestInfos);
        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onSendRequestAfter(org.objectweb.proactive.core.body.request.Request, int, org.objectweb.proactive.core.body.UniversalBody)
     */
    @Override
    public int onSendRequestAfter(Request request, int rdvValue,
        UniversalBody destination) throws RenegotiateSessionException {
        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onServeRequestBefore(org.objectweb.proactive.core.body.request.Request)
     */
    @Override
    public int onServeRequestBefore(Request request) {
        if (this.haveToCheckpoint()) {
            this.checkpoint(request);
        }
        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#onServeRequestAfter(org.objectweb.proactive.core.body.request.Request)
     */
    @Override
    public int onServeRequestAfter(Request request) {
        return 0;
    }

    /**
     * Message logs are contained in the checkpoint info structure.
     */
    @Override
    public int beforeRestartAfterRecovery(CheckpointInfo ci, int inc) {
        // recovery mode: received message no longer logged
        this.isRecovering = true;

        //first must register incoming futures deserialized by the recovery thread
        this.owner.registerIncomingFutures();

        //get messages
        List<Reply> replies = ((CheckpointInfoPMLRB) ci).getReplyLog();
        List<Request> request = ((CheckpointInfoPMLRB) ci).getRequestLog();

        // deal with potential duplicata of request
        // duplicata of replies are not treated since they are automaticaly ignored.
        Request potentialDuplicata = request.get(request.size() - 1);
        this.potentialDuplicataSender = potentialDuplicata.getSourceBodyID();
        this.potentialDuplicataSequence = potentialDuplicata.getSequenceNumber();

        // add messages in the body context
        Iterator<Request> itRequest = request.iterator();
        BlockingRequestQueue queue = owner.getRequestQueue();

        while (itRequest.hasNext()) {
            queue.add((itRequest.next()));
        }

        // replies
        Iterator<Reply> itReplies = replies.iterator();
        FuturePool fp = owner.getFuturePool();
        try {
            while (itReplies.hasNext()) {
                Reply current = itReplies.next();
                fp.receiveFutureValue(current.getSequenceNumber(),
                    current.getSourceBodyID(), current.getResult(), current);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //	add pending request to reuqestQueue
        Request pendingRequest = ((CheckpointInfoPMLRB) ci).getPendingRequest();

        // pending request could be null
        if (pendingRequest != null) {
            queue.addToFront(pendingRequest);
        }

        // normal mode
        this.isRecovering = false;

        // enable communication
        this.owner.acceptCommunication();

        try {
            // update servers
            this.location.updateLocation(ownerID, owner.getRemoteAdapter());
            this.recovery.updateState(ownerID, RecoveryProcess.RUNNING);
        } catch (RemoteException e) {
            logger.error("Unable to connect with location server");
            e.printStackTrace();
        }

        this.checkpointTimer = System.currentTimeMillis();

        return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.protocols.FTManager#handleFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    @Override
    public Object handleFTMessage(FTMessage fte) {
        return fte.handleFTMessage(this);
    }

    /////////////////////
    // private methods //
    /////////////////////
    private boolean haveToCheckpoint() {
        return ((this.checkpointTimer + this.ttc) < System.currentTimeMillis());
    }

    private void checkpoint(Request pending) {
        //System.out.println("[PMLRB] Checkpointing...");
        owner.blockCommunication();
        // checkpoint the active object
        try {
            this.setCheckpointTag(true);
            // create a checkpoint
            Checkpoint c = new Checkpoint(owner, this.additionalCodebase);

            // create checkpoint info with the pending request
            CheckpointInfoPMLRB ci = new CheckpointInfoPMLRB(pending);

            // attach infos
            c.setCheckpointInfo(ci);
            // send it to server
            this.storage.storeCheckpoint(c, FTManager.DEFAULT_TTC_VALUE); // SEE INC VALUE !

            // reninit checkpoint values
            this.checkpointTimer = System.currentTimeMillis();

            this.setCheckpointTag(false);
        } catch (RemoteException e) {
            logger.error("[PMLRB] Unable to send checkpoint to the server");
            e.printStackTrace();
        }

        owner.acceptCommunication();
    }

    private synchronized char getNextSendNumber() {
        return ++sendNumber;
    }
}
