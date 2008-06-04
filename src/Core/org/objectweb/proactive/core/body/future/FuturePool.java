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
package org.objectweb.proactive.core.body.future;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyImpl;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;


public class FuturePool extends Object implements java.io.Serializable {
    protected boolean newState;

    // table of future and ACs
    // this map is rebuilt on deserailisation of the object
    public transient FutureMap futures;

    // body corresponding to this futurePool
    private Body ownerBody;

    // Active queue of AC services
    private transient ActiveACQueue queueAC;

    // toggles for enabling or disabling automatic continuation
    // outgoing ACs has to be registred if true
    private boolean registerACs;

    // incoming replies can be sent by ACs
    private boolean sendACs;

    // table used for storing values which arrive in the futurePool BEFORE the registration
    // of its corresponding future.
    private java.util.HashMap<String, MethodCallResult> valuesForFutures;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public FuturePool() {
        futures = new FutureMap();
        valuesForFutures = new java.util.HashMap<String, MethodCallResult>();
        this.newState = false;
        if (PAProperties.PA_FUTURE_AC.isTrue()) {
            this.registerACs = true;
            this.sendACs = true;
            this.queueAC = new ActiveACQueue();
        } else {
            this.registerACs = false;
            this.sendACs = false;
        }
    }

    //
    // -- STATIC ------------------------------------------------------
    //

    // Automatic continuation

    // this table is used to register destination before sending.
    // So, a future could retreive its destination during serialization
    // this table indexed by the thread which perform the registration.
    static private ThreadLocal<ArrayList<UniversalBody>> bodiesDestination;

    // to register in the table
    static public void registerBodiesDestination(ArrayList<UniversalBody> dests) {
        bodiesDestination.set(dests);
    }

    // to clear an entry in the table
    static public void removeBodiesDestination() {
        bodiesDestination.remove();
    }

    // to get a destination
    static public ArrayList<UniversalBody> getBodiesDestination() {
        return bodiesDestination.get();
    }

    // this table is used to register deserialized futures after receive
    // So, futures to add in the local futurePool could be retreived
    static private ThreadLocal<ArrayList<Future>> incomingFutures;

    // to register an incoming future in the table
    public static void registerIncomingFuture(Future f) {
        incomingFutures.get().add(f);
    }

    // to remove an entry from the table
    static public void removeIncomingFutures() {
        incomingFutures.remove();
    }

    // to get a list of incomingFutures
    static public ArrayList<Future> getIncomingFutures() {
        return (incomingFutures.get());
    }

    // body forwarders

    // map of threads that are running a body forwarder
    static private Map<Thread, Object> forwarderThreads;

    // Add the current thread as a body forwarder
    static public void addMeAsBodyForwarder() {
        forwarderThreads.put(Thread.currentThread(), null);
    }

    // Remove the current thread from the list of body forwarders
    static public void removeMeFromBodyForwarders() {
        forwarderThreads.remove(Thread.currentThread());
    }

    // Return true if the current thread is executing a body forwarder
    static public boolean isInsideABodyForwarder() {
        return forwarderThreads.containsKey(Thread.currentThread());
    }

    static {
        bodiesDestination = new ThreadLocal<ArrayList<UniversalBody>>();
        incomingFutures = new ThreadLocal<ArrayList<Future>>() {
            @Override
            protected synchronized ArrayList<Future> initialValue() {
                return new ArrayList<Future>();
            }
        };
        // A HashTable cannot contain null as value so we use a syncrhonized HashMap
        forwarderThreads = Collections.synchronizedMap(new HashMap<Thread, Object>());
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Setter of the body corresonding to this FuturePool
     * @param b the owner body.
     */
    public void setOwnerBody(Body b) {
        ownerBody = b;
    }

    /**
     * Getter of the body corresonding to this FuturePool
     */
    public Body getOwnerBody() {
        return ownerBody;
    }

    /**
     * To enable the automatic continuation behaviour for all futures in
     * this FuturePool
     * */
    public void enableAC() {
        // queueAC is started in a lazy manner (see receiveFutureValue)
        this.queueAC = new ActiveACQueue();
        this.registerACs = true;
        this.sendACs = true;
    }

    /**
     * To disable the automatic continuation behaviour for all futures in
     * this FuturePool
     * */
    public void disableAC() {
        this.registerACs = false;
        // this.sendACs is still true to send remaining ACs
        this.queueAC.killMe(true);
    }

    /**
     * To terminate the AC thread.
     * @param completeACs if true, the thread will be terminated as soon as no automatic continuation
     * remain in the futurepool. Otherwise, the thread is killed.
     */
    public void terminateAC(boolean completeACs) {
        if (!completeACs) {
            // kill ACqueue now
            this.registerACs = false;
            this.sendACs = false;
            if (this.queueAC != null) {
                this.queueAC.killMe(false);
                this.queueAC = null;
            }
        } else {
            // ACqueue status become KILL_AFTER_COMPLETION
            // ACs can still occur
            this.queueAC.killMe(true);
        }
    }

    /**
     * Return true if some ACs are remaining is this futurepool.
     * @return true if some ACs are remaining is this futurepool, false otherwise.
     */
    public synchronized boolean remainingAC() {
        return this.futures.remainingAC();
    }

    /**
     * Method called when a reply is received, ie a value is available for a future.
     * This method perform local futures update, and put an ACService in the activeACqueue.
     * @param id sequence id of the future to update
     * @param creatorID ID of the body creator of the future to update
     * @param result value to update with the futures
     */
    public synchronized int receiveFutureValue(long id, UniqueID creatorID, MethodCallResult result,
            Reply reply) throws java.io.IOException {
        // get all aiwated futures
        ArrayList<Future> futuresToUpdate = futures.getFuturesToUpdate(id, creatorID);

        if (futuresToUpdate != null) {
            // FAULT-TOLERANCE
            int ftres = FTManager.NON_FT;
            if ((reply != null) && (reply.getFTManager() != null)) {
                ftres = reply.getFTManager().onDeliverReply(reply);
            }

            Future future = (futuresToUpdate.get(0));
            if (future != null) {
                future.receiveReply(result);
            }

            // if there are more than one future to update, we "give" deep copy
            // of the result to the other futures to respect ProActive model
            // We use here the migration tag to perform a simple serialization (ie
            // without continuation side-effects)
            int numOfFuturesToUpdate = futuresToUpdate.size();
            if (numOfFuturesToUpdate > 1) {
                setCopyMode(true);
                for (int i = 1; i < numOfFuturesToUpdate; i++) {
                    Future otherFuture = (futuresToUpdate.get(i));
                    otherFuture.receiveReply((MethodCallResult) Utils.makeDeepCopy(result));
                }
                setCopyMode(false);
                // register futures potentially generated during the copy of result
                ((AbstractBody) ownerBody).registerIncomingFutures();
            }
            stateChange();

            // 2) create and put ACservices
            if (this.registerACs) {
                ArrayList<UniversalBody> bodiesToContinue = (ArrayList<UniversalBody>) (futures
                        .getAutomaticContinuation(id, creatorID).clone());
                if ((bodiesToContinue != null) && (bodiesToContinue.size() != 0)) {
                    ProActiveSecurityManager psm = ((AbstractBody) PAActiveObject.getBodyOnThis())
                            .getProActiveSecurityManager();

                    // lazy starting of the AC thread
                    if (!this.queueAC.isAlive()) {
                        this.queueAC.start();
                    }

                    // the added reply is a deep copy (concurrent modification of result)
                    // ACs are registred during this deep copy (no copy mode)
                    // Warn : this copy does not avoid the copy for local communications !
                    this.registerDestinations(bodiesToContinue);
                    MethodCallResult newResult = (MethodCallResult) Utils.makeDeepCopy(result);

                    // the created futures should be set in copyMode to avoid AC registration
                    // during the effective sending by the AC thread
                    ArrayList<Future> incFutures = FuturePool.getIncomingFutures();
                    if (incFutures != null) {
                        for (Future f : incFutures) {
                            f.setCopyMode(true);
                        }
                        FuturePool.removeIncomingFutures();
                    }
                    this.removeDestinations();

                    // add the deepcopied AC
                    queueAC.addACRequest(new ACService(bodiesToContinue, new ReplyImpl(creatorID, id, null,
                        newResult, psm, true)));
                }
            }
            // 3) Remove futures from the futureMap
            futures.removeFutures(id, creatorID);
            return ftres;
        } else {
            // we have to store the result received by AC until future arrive
            this.valuesForFutures.put("" + id + creatorID, result);
            // OR this reply might be an orphan reply (return value is ignored if not)
            return FTManager.ORPHAN_REPLY;
        }
    }

    /**
     * To put a future in the FutureMap
     * @param futureObject future to register
     */
    public synchronized void receiveFuture(Future futureObject) {
        futureObject.setSenderID(ownerBody.getID());
        futures.receiveFuture(futureObject);
        long id = futureObject.getID();
        UniqueID creatorID = futureObject.getCreatorID();
        if (valuesForFutures.get("" + id + creatorID) != null) {
            try {
                this.receiveFutureValue(id, creatorID, valuesForFutures.remove("" + id + creatorID), null);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * To add an automatic contiunation, ie a destination body, for a particular future.
     * @param id sequence id of the corresponding future
     * @param creatorID UniqueID of the body which creates futureObject
     * @param bodyDest body destination of this continuation
     */
    public void addAutomaticContinuation(FutureID id, UniversalBody bodyDest) {
        futures.addAutomaticContinuation(id.getID(), id.getCreatorID(), bodyDest);
    }

    public synchronized void waitForReply(long timeout) throws ProActiveException {
        this.newState = false;
        // variable used to know wether the timeout has expired or not
        int timeoutCounter = 1;
        while (!newState) {
            timeoutCounter--;
            // counter < 0 means that it is the second time we enter in the loop
            // while the state has not been changed, i.e timeout has expired
            if (timeoutCounter < 0) {
                throw new ProActiveException("Timeout expired while waiting for future update");
            }
            try {
                wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * To register a destination before sending a reques or a reply
     * Registration key is the calling thread.
     */
    public void registerDestinations(ArrayList<UniversalBody> dests) {
        if (registerACs) {
            FuturePool.registerBodiesDestination(dests);
        }
    }

    /**
     * To clear registred destination for the calling thread.
     */
    public void removeDestinations() {
        if (registerACs) {
            FuturePool.removeBodiesDestination();
        }
    }

    public void setCopyMode(boolean mode) {
        futures.setCopyMode(mode);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void stateChange() {
        this.newState = true;
        notifyAll();
    }

    //
    // -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
        if (this.sendACs) {
            // queue could not be created because of lazy creation
            if (!this.queueAC.isAlive()) {
                // notify the reader that queueAC is null
                out.writeBoolean(false);
            } else {
                // notify the reader that queueAC has been created
                out.writeBoolean(true);
                // send the queue of AC requests
                out.writeObject(queueAC.getQueue());
                // stop the ActiveQueue thread if this is not a checkpointing serialization
                FTManager ftm = ((AbstractBody) ownerBody).getFTManager();
                if (ftm != null) {
                    if (!ftm.isACheckpoint()) {
                        queueAC.killMe(false);
                    }
                } else {
                    queueAC.killMe(false);
                }
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // futuremap is empty
        // futures are registred in FutureProxy.read()
        this.futures = new FutureMap();
        if (this.sendACs) {
            // if queueExists is true, ACqueue has been created
            boolean queueStarted = in.readBoolean();
            if (queueStarted) {
                // create a new ActiveACQueue
                ArrayList<ACService> queue = (ArrayList<ACService>) (in.readObject());
                queueAC = new ActiveACQueue(queue);
                queueAC.start();
            } else {
                queueAC = new ActiveACQueue();
            }
        }
    }

    //--------------------------------INNER CLASS------------------------------------//

    /**
     * Active queue status :
     * ALIVE is the normal state,
     * if state is KILL_NOW, the queue must be killed even if there is some ACs to do in the futurepool.
     * if state is KILL_AFTER_COMPLETION, the queue will be killed when no more ACs remain in the futurepool.
     */
    private enum KillStatus {
        ALIVE, KILL_NOW, KILL_AFTER_COMPLETION;
    }

    /**
     * Active Queue for AC. This queue has his own thread to perform ACservices
     * available in the queue. This thread is compliant with migration by using
     * the threadStore of the body correponding to this FutureMap.
     * Note that the ACServices are served in FIFO manner.
     * @see ACservice
     */
    private class ActiveACQueue extends Thread {
        private ArrayList<ACService> queue;
        private int counter;
        private KillStatus status;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public ActiveACQueue() {
            queue = new ArrayList<ACService>();
            counter = 0;
            status = KillStatus.ALIVE;
            this.setName("Thread for AC");
        }

        public ActiveACQueue(ArrayList<ACService> queue) {
            this.queue = queue;
            counter = queue.size();
            status = KillStatus.ALIVE;
            this.setName("Thread for AC");
        }

        //
        // -- PUBLIC METHODS -----------------------------------------------
        //

        /**
         * return the current queue of ACServices to perform
         */
        public ArrayList<ACService> getQueue() {
            return queue;
        }

        /**
         * Add a ACservice in the active queue.
         */
        public synchronized void addACRequest(ACService r) {
            queue.add(r);
            counter++;
            notifyAll();
        }

        /**
         * Return the oldest request in queue and remove it from the queue
         */
        public synchronized ACService removeACRequest() {
            counter--;
            return (queue.remove(0));
        }

        /**
         * To stop the thread.
         */
        public synchronized void killMe(boolean completeACs) {
            status = completeACs ? KillStatus.KILL_AFTER_COMPLETION : KillStatus.KILL_NOW;
            notifyAll();
        }

        @Override
        public void run() {
            // push an initial context for this thread : associate this thread to the owner body
            LocalBodyStore.getInstance().pushContext(new Context(FuturePool.this.getOwnerBody(), null));

            while (true) {
                // if there is no AC to do, wait...
                waitForAC();
                // if body is dead, kill the thread
                if (status == KillStatus.KILL_NOW) {
                    break;
                }

                // there are ACs to do !
                try {
                    // enter in the threadStore
                    FuturePool.this.getOwnerBody().enterInThreadStore();

                    // if body has migrated, kill the thread
                    if (status == KillStatus.KILL_NOW) {
                        break;
                    }

                    ACService toDo = this.removeACRequest();
                    if (toDo != null) {
                        toDo.doAutomaticContinuation();
                    }

                    // exit from the threadStore
                    FuturePool.this.getOwnerBody().exitFromThreadStore();
                } catch (Exception e2) {
                    // to unblock active object
                    e2.printStackTrace();
                    FuturePool.this.getOwnerBody().exitFromThreadStore();
                    throw new ProActiveRuntimeException("Error while sending reply for AC ", e2);
                }

                // kill it after completion of the remaining ACs...
                if ((status == KillStatus.KILL_AFTER_COMPLETION) &&
                    (!FuturePool.this.getOwnerBody().getFuturePool().remainingAC())) {
                    // if the body is not active, the ACthread has been killed by a call to terminateAC().
                    // Then complete the termination.
                    if (!FuturePool.this.getOwnerBody().isActive()) {
                        FuturePool.this.getOwnerBody().terminate(false);
                    }
                    // else the ACthread has been killed by a call to disableAC().
                    // no need to terminate the body.
                    status = KillStatus.KILL_NOW;
                    break;
                }
            }
        }

        // synchronized wait on ACRequest queue
        private synchronized void waitForAC() {
            try {
                while ((counter == 0) && (status != KillStatus.KILL_NOW)) {
                    wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A simple object for a request for an automatic continuation
     * @see ActiveACQueue
     */
    private class ACService implements java.io.Serializable {
        // bodies that have to be updated
        private ArrayList<UniversalBody> dests;

        // reply to send
        private Reply reply;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public ACService(ArrayList<UniversalBody> dests, Reply reply) {
            this.dests = dests;
            this.reply = reply;
        }

        //
        // -- PUBLIC METHODS -----------------------------------------------
        //
        public void doAutomaticContinuation() throws java.io.IOException {
            if (dests != null) {
                // for several *local* destinations, the deepcopy of the result in the reply object
                // would unset copymode for the future contained inside this result. A new reply is
                // created to avoid the alteration of the original result.
                int remainingSends = dests.size();
                Reply toSend = null;
                ProActiveSecurityManager psm = (remainingSends > 1) ? (((AbstractBody) PAActiveObject
                        .getBodyOnThis()).getProActiveSecurityManager()) : null;

                for (int i = 0; i < dests.size(); i++) {
                    UniversalBody dest = (dests.get(i));

                    // FAULT-TOLERANCE
                    FTManager ftm = ((AbstractBody) FuturePool.this.getOwnerBody()).getFTManager();

                    if (remainingSends > 1) {
                        // create a new reply to keep the original copy unchanged for next sending ...
                        toSend = new ReplyImpl(reply.getSourceBodyID(), reply.getSequenceNumber(), null,
                            reply.getResult(), psm, true);
                    } else {
                        // last sending : the orignal can ben sent
                        toSend = reply;
                    }

                    // send the reply
                    if (ftm != null) {
                        ftm.sendReply(toSend, dest);
                    } else {
                        try {
                            toSend.send(dest);
                        } catch (IOException ioe) {
                            UniversalBody.sendReplyExceptionsLogger.error(ioe, ioe);
                        }
                    }
                    remainingSends--;
                }
            }
        }
    } //ACService
}
