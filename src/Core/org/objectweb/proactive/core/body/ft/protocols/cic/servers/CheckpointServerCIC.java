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
package org.objectweb.proactive.core.body.ft.protocols.cic.servers;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.exception.NotImplementedException;
import org.objectweb.proactive.core.body.ft.internalmsg.GlobalStateCompletion;
import org.objectweb.proactive.core.body.ft.internalmsg.OutputCommit;
import org.objectweb.proactive.core.body.ft.message.HistoryUpdater;
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.ft.message.ReceptionHistory;
import org.objectweb.proactive.core.body.ft.protocols.cic.infos.CheckpointInfoCIC;
import org.objectweb.proactive.core.body.ft.protocols.cic.infos.MessageInfoCIC;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryJob;
import org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess;
import org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServerImpl;
import org.objectweb.proactive.core.body.ft.servers.util.ActiveQueue;
import org.objectweb.proactive.core.body.ft.servers.util.ActiveQueueJob;
import org.objectweb.proactive.core.body.ft.servers.util.JobBarrier;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.MutableInteger;
import org.objectweb.proactive.core.util.MutableLong;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class defines a checkpoint server for the CIC protocol.
 * @author cdelbe
 * @since 2.2
 */
public class CheckpointServerCIC extends CheckpointServerImpl {

    /** Period of the checkpoints garbage collection (ms) */
    public static final int DEFAULT_GC_PERIOD = 40000;

    //logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE_CIC);

    //monitoring latest global state
    private Hashtable<MutableInteger, MutableInteger> stateMonitor; //ckpt index -> number of stored checkpoint
    private int lastGlobalState;
    private int lastRegisteredCkpt;

    // current incarnation
    private int globalIncarnation;

    // monitoring recovery line
    private Hashtable<UniqueID, MutableInteger> greatestCommitedHistory; // ids <-> index of the greatest commited histo
    private Hashtable<MutableInteger, MutableInteger> recoveryLineMonitor; // ckpt index <-> number of completed checkpoints
    private int recoveryLine;

    // handling histories
    private Hashtable<UniqueID, ReceptionHistory> histories;

    // garbage collection
    private ActiveQueue gc;

    // profiling
    private boolean displayCkptSize;

    public CheckpointServerCIC(FTServer server) {
        super(server);

        this.stateMonitor = new Hashtable<MutableInteger, MutableInteger>();
        this.lastGlobalState = 0;
        this.greatestCommitedHistory = new Hashtable<UniqueID, MutableInteger>();
        this.recoveryLineMonitor = new Hashtable<MutableInteger, MutableInteger>();
        this.recoveryLine = 0;
        this.lastRegisteredCkpt = 0;
        this.globalIncarnation = 1;
        this.histories = new Hashtable<UniqueID, ReceptionHistory>();

        // garbage collection
        this.gc = new ActiveQueue("ActiveQueue: GC");
        gc.start();
        gc.addJob(new GarbageCollectionJob(this, DEFAULT_GC_PERIOD));

        this.displayCkptSize = false; // debugging stuff
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#storeCheckpoint(org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint, int)
     */
    public synchronized int storeCheckpoint(Checkpoint c, int incarnation)
        throws RemoteException {
        if (incarnation < this.globalIncarnation) {
            logger.warn("** WARNING ** : Object with incarnation " +
                incarnation + " is trying to store checkpoint");
            return 0;
        }

        List<Checkpoint> ckptList = checkpointStorage.get(c.getBodyID());

        // the first checkpoint ...
        if (ckptList == null) {
            // new storage slot
            List<Checkpoint> checkpoints = new ArrayList<Checkpoint>();

            //dummy first checkpoint
            checkpoints.add(new Checkpoint());
            UniqueID id = c.getBodyID();
            checkpointStorage.put(id, checkpoints);
            checkpoints.add(c);
            // new history slot
            this.histories.put(c.getBodyID(), new ReceptionHistory());
            // new greatestHisto slot
            this.greatestCommitedHistory.put(c.getBodyID(),
                new MutableInteger(0));
        } else {
            //add checkpoint
            ckptList.add(c);
        }

        // updating monitoring
        int index = ((CheckpointInfoCIC) (c.getCheckpointInfo())).checkpointIndex;
        if (index > this.lastRegisteredCkpt) {
            this.lastRegisteredCkpt = index;
        }
        MutableInteger currentGlobalState = (this.stateMonitor.get(new MutableInteger(
                    index)));
        if (currentGlobalState == null) {
            // this is the first checkpoint store for the global state index
            this.stateMonitor.put(new MutableInteger(index),
                new MutableInteger(1));
        } else {
            currentGlobalState.add(1);
        }

        //this.checkLastGlobalState();
        logger.info("[CKPT] Receive checkpoint indexed " + index +
            " from body " + c.getBodyID() + " (used memory = " +
            this.getUsedMem() + " Kb)"); // + "[" + System.currentTimeMillis() + "]");

        if (displayCkptSize) {
            logger.info("[CKPT] Size of ckpt " + index + " before addInfo : " +
                this.getSize(c) + " bytes");
        }

        // broadcast history closure if a new globalState is built
        if (this.checkLastGlobalState()) {
            // send a GSC message to all
            Enumeration<UniqueID> all = this.checkpointStorage.keys();
            while (all.hasMoreElements()) {
                UniqueID callee = all.nextElement();
                this.server.submitJob(new GSCESender(this.server, callee,
                        new GlobalStateCompletion(this.lastGlobalState)));
            }
        }
        return this.lastGlobalState;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getCheckpoint(org.objectweb.proactive.core.UniqueID, int)
     */
    public Checkpoint getCheckpoint(UniqueID id, int sequenceNumber)
        throws RemoteException {
        // TODO : checkpoints with multiple index ??
        return checkpointStorage.get(id).get(sequenceNumber);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getLastCheckpoint(org.objectweb.proactive.core.UniqueID)
     */
    public Checkpoint getLastCheckpoint(UniqueID id) throws RemoteException {
        List<Checkpoint> checkpoints = checkpointStorage.get(id);
        int size = checkpoints.size();
        return (checkpoints.get(size - 1));
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#addInfoToCheckpoint(org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo, org.objectweb.proactive.core.UniqueID, int, int)
     */
    public synchronized void addInfoToCheckpoint(CheckpointInfo ci,
        UniqueID id, int sequenceNumber, int incarnation)
        throws RemoteException {
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#commitHistory(org.objectweb.proactive.core.body.ft.message.HistoryUpdater)
     **/
    public synchronized void commitHistory(HistoryUpdater rh)
        throws RemoteException {
        if (rh.incarnation < this.globalIncarnation) {
            logger.warn("** WARNING ** : Object with incarnation " +
                rh.incarnation +
                " is trying to store checkpoint infos (Current inc = " +
                this.globalIncarnation + ")");
            return;
        }

        // update the histo if needed
        if (rh.elements != null) {
            ReceptionHistory ih = this.histories.get(rh.owner);
            ih.updateHistory(rh);
        }

        // update the recovery line monitoring
        MutableInteger greatestIndexSent = ((this.greatestCommitedHistory.get(rh.owner)));
        if (greatestIndexSent.getValue() < rh.checkpointIndex) {
            // must update rc monitoring
            greatestIndexSent.setValue(rh.checkpointIndex);
            // inc the rcv counter for the index indexOfCheckpoint
            MutableInteger counter = (this.recoveryLineMonitor.get(greatestIndexSent));
            if (counter == null) {
                // this is the first histo commit with index indexOfCkpt
                this.recoveryLineMonitor.put(new MutableInteger(
                        rh.checkpointIndex), new MutableInteger(1));
            } else {
                counter.add(1);
            }

            // test if a new recovery line has been created
            // update histories if any
            this.checkRecoveryLine();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getInfoFromCheckpoint(org.objectweb.proactive.core.UniqueID, int)
     */
    public CheckpointInfo getInfoFromCheckpoint(UniqueID id, int sequenceNumber)
        throws RemoteException {
        throw new NotImplementedException();
    }

    /**
     * Not implemented for the CIC protocol
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#storeRequest(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.request.Request)
     */
    public void storeRequest(UniqueID receiverId, Request request)
        throws RemoteException {
        throw new NotImplementedException();
    }

    /**
     * Not implemented for the CIC protocol
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#storeReply(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.reply.Reply)
     */
    public void storeReply(UniqueID receiverID, Reply reply)
        throws RemoteException {
        throw new NotImplementedException();
    }

    // return true if a new globalState is found
    private boolean checkLastGlobalState() {
        try {
            //logger.info("[CKPT] Checking last global state...");
            int systemSize = this.server.getSystemSize();
            int lastGB = this.lastGlobalState;
            int lastCkpt = this.lastRegisteredCkpt;
            MutableInteger mi = new MutableInteger(lastCkpt);
            for (int i = lastCkpt; i > lastGB; i--, mi.add(-1)) {
                int numRegistered = ((this.stateMonitor.get(mi))).getValue();
                if (numRegistered == systemSize) {
                    this.lastGlobalState = i;
                    return true;
                }
            }
            return false;
        } catch (RemoteException e) {
            logger.error("**ERROR** Cannot contact recoveryProcess");
            e.printStackTrace();
            return false;
        }
    }

    //return true if the recoveryline has changed
    private boolean checkRecoveryLine() {
        try {
            int systemSize = this.server.getSystemSize();
            MutableInteger nextPossible = (this.recoveryLineMonitor.get(new MutableInteger(this.recoveryLine +
                        1)));

            // THIS PART MUST BE ATOMIC
            if ((nextPossible != null) &&
                    (nextPossible.getValue() == systemSize)) {
                // a new recovery line has been created
                // update histories
                Enumeration<UniqueID> itKey = this.histories.keys();
                while (itKey.hasMoreElements()) {
                    UniqueID key = itKey.nextElement();
                    ReceptionHistory cur = (this.histories.get(key));
                    long nextBase = ((CheckpointInfoCIC) (this.getCheckpoint(key,
                            this.recoveryLine + 1).getCheckpointInfo())).lastRcvdRequestIndex +
                        1;
                    cur.goToNextBase(nextBase);
                    cur.confirmLastUpdate();
                }

                // a new rec line has been created
                this.recoveryLine = this.recoveryLine + 1;
                logger.info("[CKPT] Recovery line is " + this.recoveryLine);
                return true;
            }
        } catch (RemoteException e) {
            logger.error("[ERROR] The FT server is not reachable");
            e.printStackTrace();
        }
        return false;
    }

    // protected accessors
    protected void internalRecover(UniqueID failed) {
        try {
            Enumeration<UniqueID> itBodies = null;
            int globalState = 0;
            synchronized (this) {
                globalState = this.recoveryLine;
                this.globalIncarnation++;
                logger.info("[RECOVERY] Recovering system from " + globalState +
                    " with incarnation " + this.globalIncarnation);

                itBodies = this.checkpointStorage.keys();
                this.lastGlobalState = globalState;
                this.lastRegisteredCkpt = globalState;
                this.recoveryLine = globalState;
                this.stateMonitor = new Hashtable<MutableInteger, MutableInteger>();
                this.recoveryLineMonitor = new Hashtable<MutableInteger, MutableInteger>();

                // delete unusable checkpoints
                Iterator<List<Checkpoint>> it = this.checkpointStorage.values()
                                                                      .iterator();
                while (it.hasNext()) {
                    List<Checkpoint> ckpts = it.next();
                    while (ckpts.size() > (globalState + 1)) {
                        ckpts.remove(globalState + 1);
                    }
                }

                // set all the system in recovery state
                while (itBodies.hasMoreElements()) {
                    UniqueID current = (itBodies.nextElement());
                    this.server.updateState(current, RecoveryProcess.RECOVERING);
                }

                //reinit the iterator
                itBodies = this.checkpointStorage.keys();

                // reinit hisotries; delete not recoverable parts of histories
                Enumeration<ReceptionHistory> itHistories = this.histories.elements();
                while (itHistories.hasMoreElements()) {
                    itHistories.nextElement().compactHistory();
                }
            } // end synchronize

            // for waiting the end of the recovery
            Vector<JobBarrier> barriers = new Vector<JobBarrier>();

            // send checkpoints
            while (itBodies.hasMoreElements()) {
                UniqueID current = (itBodies.nextElement());

                //Checkpoint toSend = this.server.getCheckpoint(current,globalState);
                Checkpoint toSend = this.getCheckpoint(current, globalState);

                // update history of toSend
                CheckpointInfoCIC cic = (CheckpointInfoCIC) (toSend.getCheckpointInfo());
                ReceptionHistory histo = ((this.histories.get(current)));
                cic.history = histo.getRecoverableHistory();
                // set the last commited index
                cic.lastCommitedIndex = histo.getLastRecoverable();

                if (current.equals(failed)) {
                    //look for a new Runtime for this oa
                    Node node = this.server.getFreeNode();

                    //if (node==null)return;
                    barriers.add(this.server.submitJobWithBarrier(
                            new RecoveryJob(toSend, this.globalIncarnation, node)));
                } else {
                    UniversalBody toRecover = (this.server.getLocation(current));

                    // test current OA so as to handle mutliple failures
                    boolean isDead = false;
                    try {
                        isDead = this.server.isUnreachable(toRecover);
                    } catch (Exception e) {
                    }
                    if (isDead) {
                        Node node = this.server.getFreeNode();

                        //if (node==null)return;
                        barriers.add(this.server.submitJobWithBarrier(
                                new RecoveryJob(toSend, this.globalIncarnation,
                                    node)));
                    } else {
                        String nodeURL = toRecover.getNodeURL();
                        Node node = NodeFactory.getNode(nodeURL);
                        barriers.add(this.server.submitJobWithBarrier(
                                new RecoveryJob(toSend, this.globalIncarnation,
                                    node)));
                    }
                }
            }

            // MUST WAIT THE TERMINAISON OF THE RECOVERY !
            // FaultDetection thread wait for the completion of the recovery
            // If a failure occurs during rec, it will be detected by an active object
            Iterator<JobBarrier> itBarriers = barriers.iterator();
            while (itBarriers.hasNext()) {
                (itBarriers.next()).waitForJobCompletion();
            }
        } catch (NodeException e) {
            logger.error(
                "[RECOVERY] **ERROR** Unable to send checkpoint for recovery");
            e.printStackTrace();
        } catch (RemoteException e) {
            logger.error(
                "[RECOVERY] **ERROR** Cannot contact checkpoint server");
            e.printStackTrace();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#outputCommit(org.objectweb.proactive.core.body.ft.message.MessageInfo)
     */
    public synchronized void outputCommit(MessageInfo mi)
        throws RemoteException {
        Hashtable<UniqueID, MutableLong> vectorClock = ((MessageInfoCIC) mi).vectorClock;

        // must store at least each histo up to vectorClock[id]
        Enumeration<UniqueID> enumClocks = vectorClock.keys();

        // <ATOMIC>
        while (enumClocks.hasMoreElements()) {
            UniqueID id = (enumClocks.nextElement());
            MutableLong ml = vectorClock.get(id);
            ReceptionHistory ih = (this.histories.get(id));

            // first test if a history retreiving is necessary
            // i.e. if vc[id]<=histories[id].lastCommited
            long lastCommited = ih.getLastCommited();
            long index = ml.getValue();
            if (lastCommited < index) {
                try {
                    UniversalBody target = this.server.getLocation(id);
                    HistoryUpdater rh = (HistoryUpdater) (target.receiveFTMessage(new OutputCommit(lastCommited +
                                1, index)));
                    ih.updateHistory(rh);
                } catch (RemoteException e) {
                    logger.error("**ERROR** Unable to retreive history of " +
                        id);
                    e.printStackTrace();
                } catch (IOException e) {
                    logger.error("**ERROR** Unable to retreive history of " +
                        id);
                    e.printStackTrace();
                }
            }
        }

        // </ATOMIC>
        // wait for completion of histo retreiving
        // here we can commit alteration on histories
        Enumeration<ReceptionHistory> allHisto = this.histories.elements();
        while (allHisto.hasMoreElements()) {
            ReceptionHistory element = allHisto.nextElement();
            element.confirmLastUpdate();
        }
    }

    /**
     * Reintialize the server.
     */
    @Override
    public void initialize() throws RemoteException {
        super.initialize();
        this.stateMonitor = new Hashtable<MutableInteger, MutableInteger>();
        this.lastGlobalState = 0;
        this.greatestCommitedHistory = new Hashtable<UniqueID, MutableInteger>();
        this.recoveryLineMonitor = new Hashtable<MutableInteger, MutableInteger>();
        this.recoveryLine = 0;
        this.lastRegisteredCkpt = 0;
        this.globalIncarnation = 1;
        this.histories = new Hashtable<UniqueID, ReceptionHistory>();
        // kill GC thread
        gc.killMe();
        gc = new ActiveQueue("ActiveQueue: GC");
        gc.start();
        gc.addJob(new GarbageCollectionJob(this, DEFAULT_GC_PERIOD));
    }

    //////////////////////////////////////////
    ////// JOBS FOR ACTIVE QUEUE /////////////
    //////////////////////////////////////////
    private static class GarbageCollectionJob implements ActiveQueueJob {
        // this job is CIC specific
        private CheckpointServerCIC server;

        // period of garbage collection
        private int period;

        // constructor
        protected GarbageCollectionJob(CheckpointServerCIC server, int period) {
            this.server = server;
            this.period = period;
        }

        /**
         * Perform garbage collection : Delete unsable checkpoints,
         * i.e. index < currentRecoveryLine.
         * NOTE : this job is an infinite job.
         */
        public void doTheJob() {
            while (true) {
                try {
                    Thread.sleep(period);
                    CheckpointServerCIC.logger.info(
                        "[CKPT] Performing Garbage Collection...");
                    this.garbageCollection();
                    CheckpointServerCIC.logger.info(
                        "[CKPT] Garbage Collection done.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Delete unsable checkpoints, i.e. index < currentRecoveryLine
        protected void garbageCollection() {
            boolean hasGarbaged = false;
            synchronized (server) {
                int recLine = server.recoveryLine;
                Iterator<List<Checkpoint>> it = server.checkpointStorage.values()
                                                                        .iterator();
                while (it.hasNext()) {
                    List<Checkpoint> ckpts = it.next();
                    for (int i = 0; i < recLine; i++) {
                        if (ckpts.get(i) != null) {
                            hasGarbaged = true;
                            ckpts.remove(i);
                            ckpts.add(i, null);
                        }
                    }
                }
            }
            if (hasGarbaged) {
                System.gc();
            }
        }
    }

    /*
     * This class define a job for sending a global state completion notification.
     */
    private static class GSCESender implements ActiveQueueJob {
        private FTServer server;
        private UniqueID callee;
        private GlobalStateCompletion toSend;

        public GSCESender(FTServer s, UniqueID c, GlobalStateCompletion ts) {
            this.server = s;
            this.callee = c;
            this.toSend = ts;
        }

        public void doTheJob() {
            try {
                UniversalBody destination = server.getLocation(callee);

                // THIS CALL MUST BE FT !!!!
                HistoryUpdater histo = (HistoryUpdater) (destination.receiveFTMessage(toSend));

                // histo could be null : nothing to commit
                if (histo != null) {
                    server.commitHistory(histo);
                }
            } catch (IOException e) {
                try {
                    server.forceDetection();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
