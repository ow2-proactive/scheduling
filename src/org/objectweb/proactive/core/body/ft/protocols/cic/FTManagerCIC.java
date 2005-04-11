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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.exception.ProtocolErrorException;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.ft.internalmsg.GlobalStateCompletion;
import org.objectweb.proactive.core.body.ft.internalmsg.Heartbeat;
import org.objectweb.proactive.core.body.ft.logging.MessageLog;
import org.objectweb.proactive.core.body.ft.logging.ReplyLog;
import org.objectweb.proactive.core.body.ft.logging.RequestLog;
import org.objectweb.proactive.core.body.ft.util.faultdetection.FaultDetector;
import org.objectweb.proactive.core.body.ft.util.recovery.RecoveryProcess;
import org.objectweb.proactive.core.body.future.FutureResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyImpl;
import org.objectweb.proactive.core.body.request.AwaitedRequest;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.BlockingRequestQueueImpl;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.util.CircularArrayList;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 * This class implements a Communication Induced Checkpointing protocol for ProActive.
 * This FTManager is linked to each fault-tolerant active object.
 * @author cdelbe
 * @since ProActive 2.2
 */
public class FTManagerCIC
    extends org.objectweb.proactive.core.body.ft.protocols.FTManager {

    /** Time to wait between a send and a resend in ms*/
    public static final long TIME_TO_RESEND = 3000;

    /** Value returned by an object if the recieved message must be send again */
    public static final int RESEND_MESSAGE = -3;

    /** Value returned by an object if the sender of the received message must recover asap */
    public static final int RECOVER = -4;

    //logger
    protected static Logger logger = Logger.getLogger(FTManagerCIC.class.getName());

    // MessageInfos coding
    // Infos piggybacked on messages are char[] for performance reasons.
    // We define here the access to differents values
    protected static final short INFOS_SIZE = 6;
    protected static final short CKPT_INDEX = 0;
    protected static final short HISTO_INDEX = 1;
    protected static final short INCARNATION = 2;
    protected static final short LAST_REC = 3;
    protected static final short IS_ORPHAN_FOR = 4;
    protected static final short FROM_HALF_BODY = 5;
    protected static final short IS_HALF = 1;
    protected static final short IS_ACTIVE = 2;

    // FT Values
    private int incarnation;
    private int lastRecovery; // index of the last ckpt used for recovery
    private int checkpointIndex; //index of the latest checkpoint
    private long checkpointTimer;
    private int nextMax;
    private int nextMin;
    private int historyIndex; // index of the latest closed history

    // logged messages
    private Hashtable requestToResend;
    private int latestRequestLog;
    private Hashtable replyToResend;
    private int latestReplyLog;

    // history
    private Vector history;
    private int cptHisto;

    // awaited request in owner request queue
    private Vector awaitedRequests;

    // infos of ckpt with opened history
    private Hashtable awaitedCheckpointInfo;

    // pool of MessageInfo
    private char[] forSentRequest;
    private char[] forSentReply;

    // DEBUG
    private String hostname;

    public int init(AbstractBody owner) throws ProActiveException {
        super.init(owner);
        this.incarnation = 1;
        this.checkpointIndex = 0;
        this.historyIndex = 0;
        this.nextMax = 1;
        this.nextMin = 0;
        this.lastRecovery = 0;
        this.checkpointTimer = System.currentTimeMillis();
        this.requestToResend = new Hashtable();
        this.latestRequestLog = 0;
        this.replyToResend = new Hashtable();
        this.latestReplyLog = 0;
        this.history = new Vector();
        this.awaitedRequests = new Vector();
        this.awaitedCheckpointInfo = new Hashtable();
        this.forSentRequest = new char[INFOS_SIZE];
        this.forSentReply = new char[INFOS_SIZE];

        // INITIAL REGISTRATION
        try {
            this.location.updateLocation(owner.getID(), owner.getRemoteAdapter());
        } catch (RemoteException e) {
            logger.error("**ERROR** Unable to register in location server");
            throw new ProActiveException("Unable to register in location server",
                e);
        }

        // for debug purpose
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        return 0;
    }

    public int onReceiveReply(Reply reply) {
        char[] mi = reply.getMessageInfo();
        if (mi == null) {
            //reply rcvd from an non ft object
            reply.setFTManager(this);
            return 0;
        }
        if (mi[FROM_HALF_BODY] == IS_ACTIVE) {
            int localInc = this.incarnation;
            int inc = mi[INCARNATION];
            if (inc > localInc) {
                reply.setIgnoreIt(true);
                //((AbstractBody)this.owner).terminate();
                return FTManagerCIC.RESEND_MESSAGE;
            } else if (inc < localInc) {
                reply.setIgnoreIt(true);
                return FTManagerCIC.RECOVER;
            }
        }
        reply.setFTManager(this);
        return 0; //This value is not returned to the sender
    }

    public synchronized int onDeliverReply(Reply reply) {
        int currentCheckpointIndex = this.checkpointIndex;
        char[] mi = reply.getMessageInfo();

        if (mi == null) {
            //request rcvd from an non ft object
            return 0;
        }
        if (mi[FROM_HALF_BODY] == IS_ACTIVE) {
            // history closure
            if (mi[HISTO_INDEX] > this.historyIndex) {
                try {
                    this.closeHistories(mi[HISTO_INDEX]);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            // udpate checkpoint index
            if (mi[CKPT_INDEX] > currentCheckpointIndex) {
                this.nextMax = Math.max(this.nextMax, mi[CKPT_INDEX]);
			}
        }
        return currentCheckpointIndex;
    }

    public int onReceiveRequest(Request request) {
        char[] mi = request.getMessageInfo();

        if (mi == null) {
            //request rcvd from an non ft object
            request.setFTManager(this);
            return 0; //This value is not returned to the sender
        }

        if (mi[FROM_HALF_BODY] == IS_ACTIVE) {
            int localInt = this.incarnation;
            int inc = mi[INCARNATION];
            if (inc > localInt) {
                request.setIgnoreIt(true);
                //((AbstractBody)this.owner).terminate();
                // this OA will be killed by the its new incarnation (see read() AbstractBody)
                return FTManagerCIC.RESEND_MESSAGE;
            } else if (inc < localInt) {
                // force the sender to recover and ignore this message
                request.setIgnoreIt(true);
                return FTManagerCIC.RECOVER;
            }
        } else {
            // from a halfbody, nothing to do
        }
        request.setFTManager(this);
        return 0; //This value is not returned to the sender
    }

    public synchronized int onDeliverRequest(Request request) {
        int currentCheckpointIndex = this.checkpointIndex;
        char[] mi = request.getMessageInfo();

        if (mi == null) {
            //request rcvd from an non ft object
            return 0; //This value is not returned to the sender
        }

        if (mi[FROM_HALF_BODY] == IS_ACTIVE) {
            // history closure
            if (mi[HISTO_INDEX] > this.historyIndex) {
                try {
                    this.closeHistories(mi[HISTO_INDEX]);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            //is there any corresponding awaited request ?	
            if (!(this.updateAwaitedRequests(request))) {
                //if no, an awaited request is added to history
                history.add(request.getSourceBodyID());
            } else {
                //this request must be then ignored..
                request.setIgnoreIt(true);
            }

            // udpate checkpoint index
            int ckptIndex = mi[CKPT_INDEX];
            if (ckptIndex > currentCheckpointIndex) {
                this.nextMax = Math.max(this.nextMax, ckptIndex);
                // mark the request that is orphan; it will be changed in awaited req in next ckpt
                // oprhan du indexCkpt+1 a mi.ckptIndex compris
                mi[IS_ORPHAN_FOR] = (char) ckptIndex;
            }
        }
        return currentCheckpointIndex;
    }

    public synchronized int onSendReplyBefore(Reply reply) {
        // set message info values
        this.forSentReply[CKPT_INDEX] = (char) this.checkpointIndex;
        this.forSentReply[HISTO_INDEX] = (char) this.historyIndex;
        this.forSentReply[INCARNATION] = (char) this.incarnation;
        this.forSentReply[LAST_REC] = (char) this.lastRecovery;
        this.forSentReply[IS_ORPHAN_FOR] = Character.MAX_VALUE;
        this.forSentReply[FROM_HALF_BODY] = (char) IS_ACTIVE;
        reply.setMessageInfo(this.forSentReply);
        return 0;
    }

    public synchronized int onSendReplyAfter(Reply reply, int rdvValue,
        UniversalBody destination) {
        // if return value is RESEDN, receiver have to recover --> resend the message
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
        int currentCheckpointIndex = this.checkpointIndex;

        // update checkpoint index
        if (rdvValue > currentCheckpointIndex) {
            this.nextMax = Math.max(this.nextMax, rdvValue);
            // log this in-transit message
            this.extendReplyLog(rdvValue);
            // must make a deep copy of result !
            try {
                Reply toLog = null;
                try {
                    toLog = new ReplyImpl(reply.getSourceBodyID(),
                            reply.getSequenceNumber(), reply.getMethodName(),
                            (FutureResult) Utils.makeDeepCopy(reply.getResult()),
                            owner.getProActiveSecurityManager());
                } catch (SecurityNotAvailableException e1) {
                    toLog = new ReplyImpl(reply.getSourceBodyID(),
                            reply.getSequenceNumber(), reply.getMethodName(),
                            (FutureResult) Utils.makeDeepCopy(reply.getResult()), null);
                }
                MessageLog log = new ReplyLog(toLog, destination.getRemoteAdapter());
                for (int i = currentCheckpointIndex + 1; i <= rdvValue; i++) {
                    ((Vector) (this.replyToResend.get(new Integer(i)))).add(log);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public synchronized int onSendRequestBefore(Request request) {
        // set message info values
        this.forSentRequest[CKPT_INDEX] = (char) this.checkpointIndex;
        this.forSentRequest[HISTO_INDEX] = (char) this.historyIndex;
        this.forSentRequest[INCARNATION] = (char) this.incarnation;
        this.forSentRequest[LAST_REC] = (char) this.lastRecovery;
        this.forSentRequest[IS_ORPHAN_FOR] = Character.MAX_VALUE;
        this.forSentRequest[FROM_HALF_BODY] = (char) IS_ACTIVE;
        request.setMessageInfo(this.forSentRequest);
        return 0;
    }

    public synchronized int onSendRequestAfter(Request request, int rdvValue,
        UniversalBody destination) throws RenegotiateSessionException {
        //	if return value is RESEDN, receiver have to recover --> resend the message
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

        int currentCheckpointIndex = this.checkpointIndex;

        // update checkpoint index
        if (rdvValue > currentCheckpointIndex) {
            this.nextMax = Math.max(this.nextMax, rdvValue);
            // log this in-transit message in the rdvValue-currentIndex next checkpoints
            this.extendRequestLog(rdvValue);
            try {
                //must make deep copy of paramteres
                request.getMethodCall().makeDeepCopyOfArguments();
                //must reset the send counter (this request has not been forwarded)
                request.resetSendCounter();
                MessageLog log = new RequestLog(request, destination.getRemoteAdapter());
                for (int i = currentCheckpointIndex + 1; i <= rdvValue; i++) {
                    ((Vector) (this.requestToResend.get(new Integer(i)))).add(log);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public int onServeRequestBefore(Request request) {
        while (this.haveToCheckpoint()) {            
            this.checkpoint(request);
        }
        return 0;
    }

    public int onServeRequestAfter(Request request) {
        return 0;
    }

    // Active Object is created but not started 
    public int beforeRestartAfterRecovery(CheckpointInfo ci, int inc) {
        CheckpointInfoCIC cic = (CheckpointInfoCIC) ci;
        BlockingRequestQueue queue = ((AbstractBody) owner).getRequestQueue();
        int index = cic.checkpointIndex;

        //	reinit ft values
        this.history = new Vector();
        this.awaitedRequests = new Vector();
        this.awaitedCheckpointInfo = new Hashtable();
        this.replyToResend = new Hashtable();
        this.requestToResend = new Hashtable();
        this.checkpointIndex = index;
        this.nextMax = index;
        this.checkpointTimer = System.currentTimeMillis();
        this.historyIndex = index;
        this.lastRecovery = index;
        this.incarnation = inc;

        //	add orphan-tagged requests in request queue
        //this requests are also added to this.awaitedRequests
        this.filterQueue(queue, cic);

        //	add pending request to reuqestQueue
        Request pendingRequest = cic.pendingRequest;
        // pending request could be null 
        if (pendingRequest != null) {
            char[] mic = pendingRequest.getMessageInfo();
            if ((mic != null) && (mic[IS_ORPHAN_FOR] <= this.checkpointIndex)) {
                if (this.owner.getID().equals(pendingRequest.getSourceBodyID())) {
                    throw new ProtocolErrorException(
                            "A self request is orphan for " + this.owner.getID());
                }
                pendingRequest = new AwaitedRequest(cic.pendingRequest.getSourceBodyID());
                this.awaitedRequests.add(pendingRequest);
            }
            queue.addToFront(pendingRequest);   
        }

        // building history
        Iterator itHistory = cic.history.iterator();
        while (itHistory.hasNext()) {
            Request currentAwaitedRequest = new AwaitedRequest((UniqueID) (itHistory.next()));
            queue.add(currentAwaitedRequest);
            this.awaitedRequests.add(currentAwaitedRequest);
        }

        //enable communication
        //System.out.println("[CIC] enable communication");
        ((AbstractBody) owner).acceptCommunication();

        try {
            // update servers
            this.location.updateLocation(owner.getID(), owner.getRemoteAdapter());
            this.recovery.updateState(owner.getID(), RecoveryProcess.RUNNING);
        } catch (RemoteException e) {
            System.err.println("Unable to connect with location server");
            e.printStackTrace();
        }

        //debug
        try {
            //debug
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        // resend all in-transit message
        this.sendLogs((CheckpointInfoCIC) ci);

        return 0;
    }

    /////////////////////
    // PRIVATE METHODS //
    /////////////////////
    //search for an awaited request from r.source
    // if any, unfreeze ar and remove it from awaitedRequests list
    private boolean updateAwaitedRequests(Request r) {
        AwaitedRequest ar = null;
        Iterator it = this.awaitedRequests.iterator();
        UniqueID sender = r.getSourceBodyID();

        while (it.hasNext()) {
            AwaitedRequest arq = (AwaitedRequest) (it.next());
            if ((arq.getAwaitedSender()).equals(r.getSourceBodyID())) {
                ar = arq;
                break;
            }
        }
        if (ar != null) {
            ar.setAwaitedRequest(r);
            this.awaitedRequests.remove(ar);
            return true;
        } else {
            return false;
        }
    }

    // return true if this ao have to checkpoint
    private boolean haveToCheckpoint() {
        int currentCheckpointIndex = this.checkpointIndex;
        int currentNextMax = this.nextMax;

        // checkpoint if next is greater than index
        if (currentNextMax > currentCheckpointIndex) {
            //System.out.println("[CIC] have to checkpoint because of cn=" + currentCheckpointIndex + "< nMax=" + currentNextMax);
            return true;
        }
        // checkpoint if TTC is elapsed
        else if ((this.checkpointTimer + this.ttc) < System.currentTimeMillis()) {
            //System.out.println("[CIC] have to checkpoint because of TTC");
            return true;
        } else {
            return false;
        }
    }

    // checkpoint with index = current + 1
    private Checkpoint checkpoint(Request pendingRequest) {
        Checkpoint c;
        long start;
        long end;
        try {
            start = System.currentTimeMillis();
            //	stop accepting communication
            ((AbstractBody) owner).blockCommunication();
            //System.out.println("BEGIN CHECKPOINT " + );
            synchronized (this) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[CIC] Checkpointing with index = " +
                        (this.checkpointIndex + 1));
                }

                // create infos for checkpoint
                CheckpointInfoCIC ci = new CheckpointInfoCIC();
                this.extendReplyLog(this.checkpointIndex + 1);
                this.extendRequestLog(this.checkpointIndex + 1);
                ci.replyToResend = (Vector) (this.replyToResend.get(new Integer(this.checkpointIndex +
                        1)));
                ci.requestToResend = (Vector) (this.requestToResend.get(new Integer(this.checkpointIndex +
                        1)));
                ci.pendingRequest = pendingRequest;
                ci.checkpointIndex = this.checkpointIndex + 1;

                // delete logs
                this.replyToResend.remove(new Integer(this.checkpointIndex + 1));
                this.requestToResend.remove(new Integer(this.checkpointIndex + 1));

                // inc checkpoint index
                this.checkpointIndex++;

                // store infos for further sending to the server
                this.awaitedCheckpointInfo.put(new Integer(this.checkpointIndex), ci);

                // current informations are not stored in the checkpoint 
                Hashtable awaitedCheckpointTMP = this.awaitedCheckpointInfo;
                this.awaitedCheckpointInfo = null;
                Hashtable requestToSendTMP = this.requestToResend;
                this.requestToResend = null;
                Hashtable replyToSendTMP = this.replyToResend;
                this.replyToResend = null;
                this.history = new Vector();
                
  
                
                // checkpoint the active object
                this.setCheckpointTag(true);
                c = new Checkpoint((Body) owner, this.checkpointIndex,
                        this.additionalCodebase);
                
                // send it to server
                int resStorage = this.storage.storeCheckpoint(c, this.incarnation);
                this.setCheckpointTag(false);

                // restore current informations               
                this.awaitedCheckpointInfo = awaitedCheckpointTMP;
                this.replyToResend = replyToSendTMP;
                this.requestToResend = requestToSendTMP;
                
                
                // reninit checkpoint values
                this.checkpointTimer = System.currentTimeMillis();
                
            }
            ((AbstractBody) owner).acceptCommunication();
            end = System.currentTimeMillis();
            //System.out.println("[BENCH] Cumulated Ckpt time at " + this.checkpointIndex + " : " + this.cumulatedCheckpointTime + " ms");// + System.currentTimeMillis() + "]");
            return c;
        } catch (RemoteException e) {
            logger.error("[CIC] Unable to send checkpoint to the server");
            e.printStackTrace();
        }
        return null;
    }

    //close histories st num<=to
    private synchronized void closeHistories(int to) throws RemoteException {
        //Thread.dumpStack();
        // latest open history = this.checkpointIndex
        if (to > this.checkpointIndex) {
            this.historyIndex = to;
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("[CIC] " + this.hostname +
                    " closing history up to " + to + "(local=" +
                    this.historyIndex + ")");
            }
            for (int i = this.historyIndex + 1; i <= to; i++) {
                CheckpointInfoCIC currentCi = (CheckpointInfoCIC) (this.awaitedCheckpointInfo.get(new Integer(i)));
                currentCi.history = this.history;
                this.storage.addInfoToCheckpoint(currentCi, this.owner.getID(),
                    i, this.incarnation);
            }

            this.historyIndex = to;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // send logged messages before recovery
    // !!! ALL FT-STATE VARAIBLES MUST BE SET !!!
    private void sendLogs(CheckpointInfoCIC ci) {
        //send replies
        //System.out.println("[CIC] Sending logged messages...");
        Vector replies = ci.replyToResend;
        Iterator itReplies = replies.iterator();
        while (itReplies.hasNext()) {
            //System.out.println( this.owner.getID() + "      SEND REPLY");
            UniversalBody destination = null;
            Reply r = null;
            ReplyLog rl = (ReplyLog) (itReplies.next());
            r = rl.getReply();
            destination = rl.getDestination();
            this.sendReply(r, destination);
        }

        //send requests
        Vector requests = ci.requestToResend;
        Iterator itRequests = requests.iterator();
        while (itRequests.hasNext()) {
            try {
                //System.out.println( this.owner.getID() + "      SEND REQUEST");
                UniversalBody destination = null;
                RequestLog lr = (RequestLog) (itRequests.next());
                Request loggedRequest = lr.getRequest();
                destination = lr.getDestination();
                // must create a new req : the sender must be this.owner
                Request r = new RequestImpl(loggedRequest.getMethodCall(),
                        this.owner.getRemoteAdapter(),
                        loggedRequest.isOneWay(),
                        loggedRequest.getSequenceNumber());
                this.sendRequest(r, destination);
            } catch (RenegotiateSessionException e) {
                e.printStackTrace();
            }
        }
    }

    // send reply until destination is available
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

    // send request until destination is available
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

    // replace request that are orphan for cic.checkpointIndex by awaitedRequest
    private void filterQueue(BlockingRequestQueue queue, CheckpointInfoCIC cic) {
        java.util.Iterator itQueue = queue.iterator();
        java.util.ArrayList toChange = new java.util.ArrayList();
        while (itQueue.hasNext()) {
            Request current = (Request) (itQueue.next());
            char[] mi = current.getMessageInfo();
            
            if (mi==null){
                // current is an awaited request that is not updated 
                this.awaitedRequests.add(current); 
            }else if (mi[IS_ORPHAN_FOR] <= cic.checkpointIndex) {
                // current is an orpahn request 
                toChange.add(current);
            }
        }

        // replace all in toChange by awaitedRequest
        java.util.Iterator itChange = toChange.iterator();
        CircularArrayList internalQueue = ((BlockingRequestQueueImpl) queue).getInternalQueue();
        while (itChange.hasNext()) {
            Request r = (Request) (itChange.next());
            int index = internalQueue.indexOf(r);
            internalQueue.remove(index);
            AwaitedRequest ar = new AwaitedRequest(r.getSourceBodyID());
            internalQueue.add(index, ar);
            this.awaitedRequests.add(ar);
        }
    }

    private void extendRequestLog(int size) {
        if (this.latestRequestLog < size) {
            //the log vector must grow
            for (int j = this.latestRequestLog + 1; j <= size; j++) {
                this.requestToResend.put(new Integer(j), new Vector());
            }
            this.latestRequestLog = size;
        }
    }

    private void extendReplyLog(int size) {
        if (this.latestReplyLog < size) {
            //the log vector must grow
            for (int j = this.latestReplyLog + 1; j <= size; j++) {
                this.replyToResend.put(new Integer(j), new Vector());
            }
            this.latestReplyLog = size;
        }
    }

    // DEBUGGING
    private void printDebug(String location) {
        System.out.println("[CIC] FTManager state (" + location + ")");
        System.out.println("      * Checkpoint index : " +
            this.checkpointIndex);
        System.out.println("      * Next max         : " + this.nextMax);
        System.out.println("      * History index    : " + this.historyIndex);
        System.out.println("      * Owner            : " + this.owner);
        System.out.println("      * Storage          : " + this.storage);
    }

    public String toString() {
        String ret = " Incarnation = ";
        ret += this.incarnation;
        return ret;
    }

    private long getSize(Serializable c) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // serialize the body
            oos.writeObject(c);
            // store the serialized form
            return baos.toByteArray().length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
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
                        Thread.sleep(TIME_TO_RESEND);                  
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    newLocation = this.location.searchObject(suspect,
                            suspectLocation.getRemoteAdapter(), this.owner.getID());
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

    //////////////////////////////////////////////////////////////////////////////////
    ///////// HANDLING EVENTS ////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////
    // Double Dispatch pattern
    public int handleFTMessage(FTMessage fte) {
        return fte.handleFTMessage(this);
    }

    /**
     * Closing history.
     * @param fte Message that contains the last complete global state
     * @return index of the last closed history
     */
    public int HandlingGSCEEvent(GlobalStateCompletion fte) {
        try {
            this.closeHistories(fte.getIndex());
        } catch (RemoteException e) {
            logger.error("[ERROR] Checkpoint server unreachable");
            e.printStackTrace();
        }
        return this.historyIndex;
    }

    /**
     * Heartbeat message. Send state value to the fault detector.
     * @param fte heartbeat message.
     * @return FaultDetector.OK if active object is alive, FaultDetector.IS_DEAD otherwise.
     */
    public int HandleHBEvent(Heartbeat fte) {
        if (this.owner.isAlive()){
            return FaultDetector.OK;
        } else {
            return FaultDetector.IS_DEAD;
        }
    }
}
