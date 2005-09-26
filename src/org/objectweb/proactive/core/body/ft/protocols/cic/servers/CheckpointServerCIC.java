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
package org.objectweb.proactive.core.body.ft.protocols.cic.servers;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

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


/**
 * @author cdelbe
 * @since 2.2
 */
public class CheckpointServerCIC extends CheckpointServerImpl {

    /** Period of the checkpoints garbage collection (ms) */
    public static final int DEFAULT_GC_PERIOD = 40000;

    //monitoring latest global state
    private Hashtable stateMonitor; //ckpt index -> number of stored checkpoint
    private int lastGlobalState;
    private int lastRegisteredCkpt;
    private int lastUpdatedCkpt;

    // current incarnation 
    private int globalIncarnation;

    // monitoring recovery line
    private Hashtable greatestCommitedHistory; // ids <-> index of the greatest commited histo
    private Hashtable recoveryLineMonitor; // ckpt index <-> number of completed checkpoints
    private int recoveryLine;

    // handling histories
    private Hashtable histories;

    // profiling
    private boolean displayCkptSize;

    /**
     *
     */
    public CheckpointServerCIC(FTServer server) {
        super(server);

        this.stateMonitor = new Hashtable();
        this.lastGlobalState = 0;
        this.greatestCommitedHistory = new Hashtable();
        this.recoveryLineMonitor = new Hashtable();
        this.recoveryLine = 0;
        this.lastRegisteredCkpt = 0;
        this.globalIncarnation = 1;
        this.histories = new Hashtable();

        // garbage collection
        ActiveQueue gc = new ActiveQueue("ActiveQueue: GC");
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

        ArrayList ckptList = (ArrayList) checkpointStorage.get(c.getBodyID());

        // the first checkpoint ...
        if (ckptList == null) {
            // new storage slot
            ArrayList checkpoints = new ArrayList();

            //dummy first checkpoint
            checkpoints.add(new Object());
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
        int index = c.getIndex();
        if (index > this.lastRegisteredCkpt) {
            this.lastRegisteredCkpt = index;
        }
        MutableInteger currentGlobalState = (MutableInteger) (this.stateMonitor.get(new MutableInteger(
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
            " from body " + c.getBodyID()); // + "[" + System.currentTimeMillis() + "]");
        if (displayCkptSize) {
            logger.info("[CKPT] Size of ckpt " + index + " before addInfo : " +
                this.getSize(c) + " bytes");
        }

        // broadcast history closure if a new globalState is built
        if (this.checkLastGlobalState()) {
            //		    try {
            //		        System.out.println("Broadcasting GSCE !!");
            //		        //this.server.broadcastFTEvent(new GlobalStateCompletion(this.lastGlobalState));
            //		        
            //		    } catch (RemoteException e) {
            //		        // an active object seems to be failed ...
            //		        this.server.forceDetection();
            //		    }
            // send a GSC message to all
            Enumeration all = this.checkpointStorage.keys();
            while (all.hasMoreElements()) {
                UniqueID callee = (UniqueID) (all.nextElement());

                //		        ActiveQueueJob job = new GSCESender(this.server,callee,new GlobalStateCompletion(this.lastGlobalState));
                //		        ((ActiveQueue)(this.activeQueuePool.get(counter))).addJob(job);
                //		        counter = ((counter+1)%nbActiveQueues);
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
        return (Checkpoint) ((ArrayList) (checkpointStorage.get(id))).get(sequenceNumber);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getLastCheckpoint(org.objectweb.proactive.core.UniqueID)
     */
    public Checkpoint getLastCheckpoint(UniqueID id) throws RemoteException {
        ArrayList checkpoints = (java.util.ArrayList) (checkpointStorage.get(id));
        int size = checkpoints.size();
        return (Checkpoint) (checkpoints.get(size - 1));
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#addInfoToCheckpoint(org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo, org.objectweb.proactive.core.UniqueID, int, int)
     */
    public synchronized void addInfoToCheckpoint(CheckpointInfo ci,
        UniqueID id, int sequenceNumber, int incarnation)
        throws RemoteException {
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#commitHistory(int, long, long, java.util.List)
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
            ReceptionHistory ih = (ReceptionHistory) this.histories.get(rh.owner);
            ih.updateHistory(rh);
        }

        // update the recovery line monitoring
        MutableInteger greatestIndexSent = ((MutableInteger) (this.greatestCommitedHistory.get(rh.owner)));
        if (greatestIndexSent.getValue() < rh.checkpointIndex) {
            // must update rc monitoring
            greatestIndexSent.setValue(rh.checkpointIndex);
            // inc the rcv counter for the index indexOfCheckpoint
            MutableInteger counter = (MutableInteger) (this.recoveryLineMonitor.get(greatestIndexSent));
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
                int numRegistered = ((MutableInteger) (this.stateMonitor.get(mi))).getValue();
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
    // Recovery increase only 1 by 1 ... TO DO
    private boolean checkRecoveryLine() {
        System.out.println("CheckpointServerCIC.checkRecoveryLine()");
        try {
            int systemSize = this.server.getSystemSize();
            int lastRecoveryLine = this.recoveryLine;
            MutableInteger nextPossible = (MutableInteger) (this.recoveryLineMonitor.get(new MutableInteger(this.recoveryLine +
                        1)));

            System.out.println("NextPossible = " + nextPossible);
            if (nextPossible == null) {
                Thread.dumpStack();
            }

            // THIS PART MUST BE ATOMIC
            if ((nextPossible != null) &&
                    (nextPossible.getValue() == systemSize)) {
                // a new recovery line has been created
                // update histories
                Enumeration itKey = this.histories.keys();
                while (itKey.hasMoreElements()) {
                    UniqueID key = (UniqueID) (itKey.nextElement());
                    ReceptionHistory cur = (ReceptionHistory) (this.histories.get(key));
                    long nextBase = ((CheckpointInfoCIC) (this.getCheckpoint(key,
                            this.recoveryLine + 1).getCheckpointInfo())).lastRcvdRequestIndex +
                        1;
                    cur.goToNextBase(key, nextBase);
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
            Enumeration itBodies = null;
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
                this.stateMonitor = new Hashtable();
                this.recoveryLineMonitor = new Hashtable();

                // delete unusable checkpoints
                Iterator it = this.checkpointStorage.values().iterator();
                while (it.hasNext()) {
                    ArrayList ckpts = ((ArrayList) (it.next()));
                    while (ckpts.size() > (globalState + 1)) {
                        ckpts.remove(globalState + 1);
                    }
                }

                // set all the system in recovery state
                while (itBodies.hasMoreElements()) {
                    UniqueID current = (UniqueID) (itBodies.nextElement());
                    this.server.updateState(current, RecoveryProcess.RECOVERING);
                }

                //reinit the iterator
                itBodies = this.checkpointStorage.keys();

                // reinit hisotries; delete not recoverable parts of histories
                Enumeration itHistories = this.histories.elements();
                while (itHistories.hasMoreElements()) {
                    ((ReceptionHistory) (itHistories.nextElement())).compactHistory();
                }
            } // end synchronize

            // for waiting the end of the recovery
            Vector barriers = new Vector();

            // send checkpoints
            while (itBodies.hasMoreElements()) {
                UniqueID current = (UniqueID) (itBodies.nextElement());

                //Checkpoint toSend = this.server.getCheckpoint(current,globalState); 
                Checkpoint toSend = this.getCheckpoint(current, globalState);

                // update history of toSend
                CheckpointInfoCIC cic = (CheckpointInfoCIC) (toSend.getCheckpointInfo());
                ReceptionHistory histo = ((ReceptionHistory) (this.histories.get(current)));
                cic.history = (Vector) histo.getRecoverableHistory();
                System.out.println("Histo SIZE = " +
                    ((CheckpointInfoCIC) (toSend.getCheckpointInfo())).history.size());
                // set the last commited index
                cic.lastCommitedIndex = histo.getLastRecoverable();

                if (current.equals(failed)) {
                    //look for a new Runtime for this oa
                    Node node = this.server.getFreeNode();
                    barriers.add(this.server.submitJobWithBarrier(
                            new RecoveryJob(toSend, this.globalIncarnation, node)));
                } else {
                    UniversalBody toRecover = (UniversalBody) (this.server.getLocation(current));

                    // test current OA so as to handle mutliple failures
                    boolean isDead = false;
                    try {
                        isDead = this.server.isUnreachable(toRecover);
                    } catch (Exception e) {
                    }
                    if (isDead) {
                        Node node = this.server.getFreeNode();
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
            System.out.println("BEFORE BARRIER");
            Iterator itBarriers = barriers.iterator();
            while (itBarriers.hasNext()) {
                ((JobBarrier) (itBarriers.next())).waitForJobCompletion();
            }
            System.out.println("AFTER BARRIER");
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

    // SYNCHRONY OF HISTORY ACCESSES
    public synchronized void outputCommit(MessageInfo mi)
        throws RemoteException {
        Hashtable vectorClock = ((MessageInfoCIC) mi).vectorClock;

        // must store at least each histo up to vectorClock[id]       
        Enumeration enum = vectorClock.keys();

        // THIS PART MUST BE ATOMIC AND THREADED !
        // <ATOMIC>
        while (enum.hasMoreElements()) {
            UniqueID id = (UniqueID) (enum.nextElement());
            MutableLong ml = (MutableLong) vectorClock.get(id);
            ReceptionHistory ih = (ReceptionHistory) (this.histories.get(id));

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
        //  DEADLOCK => FTM is locked by beforeSending, and histo retreive need the lock
        // ==> lock sur l'hisot uniquement 
        // here we can commit alteration on histories
        Enumeration allHisto = this.histories.elements();
        while (allHisto.hasMoreElements()) {
            ReceptionHistory element = (ReceptionHistory) allHisto.nextElement();
            element.confirmLastUpdate();
        }
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
            synchronized (server) {
                int recLine = server.recoveryLine;
                int lastGS = server.lastGlobalState;
                Iterator it = server.checkpointStorage.values().iterator();
                while (it.hasNext()) {
                    ArrayList ckpts = ((ArrayList) (it.next()));
                    for (int i = 0; i < recLine; i++) {
                        if (ckpts.get(i) != null) {
                            ckpts.remove(i);
                            ckpts.add(i, null);
                        }
                    }
                }
                Runtime r = Runtime.getRuntime();
                long usedMem = (r.totalMemory() - r.freeMemory()) / 1024;
                System.out.println("[CKPT] Used memory : " + usedMem + " Kb");
            }
        }
    }

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

    private static class OCSender implements ActiveQueueJob {
        private FTServer server;
        private UniqueID callee;
        private OutputCommit toSend;

        public void doTheJob() {
        }
    }

    //    /*
    //	 * This class defines the element of this.histories. We just add the historized index of 
    //	 * the first and the last element of the vector.
    //	 * @author cdelbe
    //	 */
    //	private static class IndexedHistory implements Serializable{
    //	    
    //	    // the elements of the history
    //	    private List elements;
    //	    
    //	    // the historized index of the last element 
    //	    private long lastCommited; 
    //	    
    //	    // the historized index of the first element
    //	    private long base;
    //
    //	    // the last usable elements : the list elements can be longer that needed
    //	    // if this histo has been updated but not commited
    //	    private long lastRecoverable;
    //	    
    //	    /**
    //	     * Constructor
    //	     */
    //	    public IndexedHistory (){
    //	        this.elements = new Vector();
    //	        this.lastCommited = -1;
    //	        this.lastRecoverable = -1;
    //	        this.base = 0;
    //	    }
    //	    
    //	    /**
    //	     * Update this history up to last;
    //	     * @param base the historized index of the first element of elts
    //	     * @param last the historized index of the last element of elts
    //	     * @param elts the elements to add to the history
    //	     */
    //	    public void updateHistory(long toAddBase, long toAddLast, List toAdd){
    //
    //	        //System.out.println("update histo toAddBase=" + toAddBase + " ; toAddLast=" + toAddLast + " ; this.lastCommited=" + this.lastCommited);
    //	        
    //	        // if there is a gap between lastCommited and toAddBase, we can
    //	        // suppose that this gap is commited. The current history is then 
    //	        // replaced by toAdd
    //	        if (toAddBase>this.lastCommited+1){
    //	            // history is not contigue
    //	            System.out.println("HISTORIC IS NOT CONTIGUE ??");
    //	            this.elements = toAdd;
    //	            this.base = toAddBase;
    //	            this.lastCommited = toAddLast;
    //	        } else if (this.lastCommited<toAddLast){
    //	            // history is contigue 
    //	            Iterator it=toAdd.iterator();
    //	            // shift in elts up to this.lastCommited+1
    //	            for (long i=toAddBase;i<=this.lastCommited;i++){
    //	                it.next();
    //	            }
    //	            // add the rest to this.elements
    //	            while (it.hasNext()){
    //	                this.elements.add(it.next());
    //	            }
    //	            this.lastCommited = toAddLast;
    //	        }
    //	    }
    //	    
    //	    /**
    //	     * This method is called when elements between base and nextBase are no more
    //	     * usefull : there a included in the state represented by the last recovery line.
    //	     */
    //	    // UNIQUEID FOR DEBUGGING
    //	    public void goToNextBase(UniqueID id, long nextBase){
    //	        if (nextBase<this.base){
    //	            throw new ProtocolErrorException("nextBase is lower than current base !");
    //	        }
    //	        
    //	        System.out.println(""+ id + "goToNextBase from " + this.base + " to "+ nextBase + " with size of histo = " + this.elements.size());
    //	            
    //	        
    //	        int shift = (int)(nextBase - this.base);
    //	        // particular case : shift==histo_size
    //	        // minimal histo is empty for this checkpoint
    //	        if ( shift == (this.elements.size()+1)) {
    //	            System.out.println(""+id+ " histo is empty !");
    //	            this.elements.clear();
    //	            this.base = nextBase;
    //	        } else {      
    //	            this.elements.subList(0,shift).clear();	        
    //	            this.base= nextBase;
    //	        }
    //	        System.out.println("After gnb : histo size = " + this.elements.size());
    //	    }
    //	 
    //	    
    //	    public void confirmLastUpdate(){
    //	        this.lastRecoverable=this.lastCommited;
    //	    }
    //	    
    //	    public long getLastCommited(){
    //	        return this.lastCommited;
    //	    }
    //
    //	    /**  
    //	     * Called only on recovery of the system. If recoverable hisotry is different from
    //	     * stored history, stored history is replaced by recoverable history.
    //	     * @return the recoverable hisotry;
    //	     */
    //	    public List getRecoverableHistory(){
    //	        if (this.lastCommited == this.lastRecoverable){
    //	            return this.elements;
    //	        }else{
    //	            System.out.println("OKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
    //	            Vector toRet = new Vector();
    //	            int histoSize = this.elements.size();
    //	            for (int i = 0; i<=(this.lastRecoverable-this.base);i++){
    //	                toRet.add(this.elements.get(i));
    //	            }
    //	            // DELETE FROM LASTREC TO LASTCOMMITED !!!
    //	            this.elements = toRet;
    //	            return toRet;
    //	        }
    //	    }
    //	    
    //	    
    //	    // delete hisotry from LastRec to LastCommited
    //	    public void compactHistory(){
    //	        if (this.lastCommited>this.lastRecoverable){
    //	            this.elements.subList((int)(this.lastRecoverable+1-this.base),(int)(this.lastCommited+1-this.base));
    //	            this.lastCommited = this.lastRecoverable;
    //	        }
    //	    }
    //	}
}
