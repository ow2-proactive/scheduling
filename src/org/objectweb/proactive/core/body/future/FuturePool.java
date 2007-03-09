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
package org.objectweb.proactive.core.body.future;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyImpl;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.exceptions.body.BodyNonFunctionalException;
import org.objectweb.proactive.core.exceptions.body.SendReplyCommunicationException;
import org.objectweb.proactive.core.exceptions.manager.NFEManager;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;


public class FuturePool extends Object implements java.io.Serializable {
    protected boolean newState;

    // table of future and ACs
    private FutureMap futures;

    // ID of the body corresponding to this futurePool
    private UniqueID ownerBody;

    // Active queue of AC services
    private transient ActiveACQueue queueAC;

    // toggle for enabling or disabling automatic continuation 
    private boolean acEnabled;

    // table used for storing values which arrive in the futurePool BEFORE the registration
    // of its corresponding future.
    private java.util.HashMap<String, FutureResult> valuesForFutures;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public FuturePool() {
        futures = new FutureMap();
        valuesForFutures = new java.util.HashMap<String, FutureResult>();
        this.newState = false;
        if ("enable".equals(ProActiveConfiguration.getACState())) {
            this.acEnabled = true;
        } else {
            this.acEnabled = false;
        }
    }

    //
    // -- STATIC ------------------------------------------------------
    //
    // this table is used to register destination before sending.
    // So, a future could retreive its destination during serialization
    // this table indexed by the thread which perform the registration.
    static private java.util.Hashtable<Thread, UniversalBody> bodyDestination;

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

    // to register in the table
    static public void registerBodyDestination(UniversalBody dest) {
        bodyDestination.put(Thread.currentThread(), dest);
    }

    // to clear an entry in the table
    static public void removeBodyDestination() {
        bodyDestination.remove(Thread.currentThread());
    }

    // to get a destination
    static public UniversalBody getBodyDestination() {
        return (bodyDestination.get(Thread.currentThread()));
    }

    // this table is used to register deserialized futures after receive
    // So, futures to add in the local futurePool could be retreived
    static private java.util.Hashtable<Thread, ArrayList<Future>> incomingFutures;

    // to register an incoming future in the table  	
    public static void registerIncomingFuture(Future f) {
        java.util.ArrayList<Future> listOfFutures = incomingFutures.get(Thread.currentThread());
        if (listOfFutures != null) {
            listOfFutures.add(f);
        } else {
            java.util.ArrayList<Future> newListOfFutures = new java.util.ArrayList<Future>();
            newListOfFutures.add(f);
            incomingFutures.put(Thread.currentThread(), newListOfFutures);
        }
    }

    // to remove an entry from the table
    static public void removeIncomingFutures() {
        incomingFutures.remove(Thread.currentThread());
    }

    // to get a list of incomingFutures
    static public java.util.ArrayList getIncomingFutures() {
        return (incomingFutures.get(Thread.currentThread()));
    }

    static {
        bodyDestination = new java.util.Hashtable<Thread, UniversalBody>();
        incomingFutures = new java.util.Hashtable<Thread, ArrayList<Future>>();
        // A HashTable cannot contain null as value so we use a syncrhonized HashMap
        forwarderThreads = Collections.synchronizedMap(new HashMap<Thread, Object>());
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Setter of the ID of the body corresonding to this FuturePool
     * @param i ID of the owner body.
     */
    public void setOwnerBody(UniqueID i) {
        ownerBody = i;
    }

    /**
     * Getter of the ID of the body corresonding to this FuturePool
     */
    public UniqueID getOwnerBody() {
        return ownerBody;
    }

    /**
     * To enable the automatic continuation behaviour for all futures in
     * this FuturePool
     * */
    public void enableAC() {
        // queueAC is created in a lazy manner (see receiveFutureValue)
        this.acEnabled = true;
    }

    /**
     * To disable the automatic continuation behaviour for all futures in
     * this FuturePool
     * */
    public void disableAC() {
        this.acEnabled = false;
        if (this.queueAC != null) {
            this.queueAC.killMe();
            this.queueAC = null;
        }
    }

    /**
     * Method called when a reply is recevied, ie a value is available for a future.
     * This method perform local futures update, and put an ACService in the activeACqueue.
     * @param id sequence id of the future to update
     * @param creatorID ID of the body creator of the future to update
     * @param result value to update with the futures
     */
    public synchronized int receiveFutureValue(long id, UniqueID creatorID,
        FutureResult result, Reply reply) throws java.io.IOException {
        // get all aiwated futures
        java.util.ArrayList futuresToUpdate = futures.getFuturesToUpdate(id,
                creatorID);

        if (futuresToUpdate != null) {
            // FAULT-TOLERANCE
            int ftres = FTManager.NON_FT;
            if ((reply != null) && (reply.getFTManager() != null)) {
                ftres = reply.getFTManager().onDeliverReply(reply);
            }

            Future future = (Future) (futuresToUpdate.get(0));
            if (future != null) {
                future.receiveReply(result);
            }

            // if there are more than one future to update, we "give" deep copy
            // of the result to the other futures to respect ProActive model
            // We use here the migration tag to perform a simple serialization (ie 
            // without continuation side-effects)
            int numOfFuturesToUpdate = futuresToUpdate.size();
            if (numOfFuturesToUpdate > 1) {
                setMigrationTag();
                for (int i = 1; i < numOfFuturesToUpdate; i++) {
                    Future otherFuture = (Future) (futuresToUpdate.get(i));
                    otherFuture.receiveReply((FutureResult) Utils.makeDeepCopy(
                            result));
                }
                unsetMigrationTag();
            }
            stateChange();

            // 2) create and put ACservices
            if (acEnabled) {
                java.util.ArrayList bodiesToContinue = futures.getAutomaticContinuation(id,
                        creatorID);
                if ((bodiesToContinue != null) &&
                        (bodiesToContinue.size() != 0)) {
                    ProActiveSecurityManager psm = ((AbstractBody) ProActive.getBodyOnThis()).getProActiveSecurityManager();

                    // lazy creation of the AC thread
                    if (this.queueAC == null) {
                        this.queueAC = new ActiveACQueue();
                        this.queueAC.start();
                    }
                    //the added reply is a deep copy with the isAC tag set to true
                    queueAC.addACRequest(new ACService(bodiesToContinue,
                            new ReplyImpl(creatorID, id, null, result, psm, true)));
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
        futureObject.setSenderID(ownerBody);
        futures.receiveFuture(futureObject);
        long id = futureObject.getID();
        UniqueID creatorID = futureObject.getCreatorID();
        if (valuesForFutures.get("" + id + creatorID) != null) {
            try {
                this.receiveFutureValue(id, creatorID,
                    valuesForFutures.remove("" + id + creatorID), null);
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
    public void addAutomaticContinuation(long id, UniqueID creatorID,
        UniversalBody bodyDest) {
        futures.addAutomaticContinuation(id, creatorID, bodyDest);
    }

    public synchronized void waitForReply(long timeout)
        throws ProActiveException {
        this.newState = false;
        // variable used to know wether the timeout has expired or not
        int timeoutCounter = 1;
        while (!newState) {
            timeoutCounter--;
            // counter < 0 means that it is the second time we enter in the loop
            // while the state has not been changed, i.e timeout has expired
            if (timeoutCounter < 0) {
                throw new ProActiveException(
                    "Timeout expired while waiting for future update");
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
    public void registerDestination(UniversalBody dest) {
        if (acEnabled) {
            FuturePool.registerBodyDestination(dest);
        }
    }

    /**
     * To clear registred destination for the calling thread.
     */
    public void removeDestination() {
        if (acEnabled) {
            FuturePool.removeBodyDestination();
        }
    }

    public void setMigrationTag() {
        futures.setMigrationTag();
    }

    public void unsetMigrationTag() {
        futures.unsetMigrationTag();
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
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        setMigrationTag();
        out.defaultWriteObject();
        if (acEnabled) {
            // queue could not be created because of lazy creation
            if (this.queueAC == null) {
                // notify the reader that queueAC is null
                out.writeBoolean(false);
            } else {
                // notify the reader that queueAC has been created
                out.writeBoolean(true);
                // send the queue of AC requests
                out.writeObject(queueAC.getQueue());
                // stop the ActiveQueue thread if this is not a checkpointing serialization
                FTManager ftm = ((AbstractBody) (LocalBodyStore.getInstance()
                                                               .getLocalBody(this.ownerBody))).getFTManager();
                if (ftm != null) {
                    if (!ftm.isACheckpoint()) {
                        queueAC.killMe();
                    }
                } else {
                    queueAC.killMe();
                }
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        unsetMigrationTag();
        if (acEnabled) {
            // if queueExists is true, ACqueue has been created
            boolean queueExists = in.readBoolean();
            if (queueExists) {
                // create a new ActiveACQueue
                java.util.ArrayList<ACService> queue = (java.util.ArrayList<ACService>) (in.readObject());
                queueAC = new ActiveACQueue(queue);
                queueAC.start();
            }
        }
    }

    //--------------------------------INNER CLASS------------------------------------//

    /**
     * Active Queue for AC. This queue has his own thread to perform ACservices
     * available in the queue. This thread is compliant with migration by using
     * the threadStore of the body correponding to this FutureMap.
     * Note that the ACServices are served in FIFO manner.
     * @see ACservice
     */
    private class ActiveACQueue extends Thread {
        private java.util.ArrayList<ACService> queue;
        private int counter;
        private boolean kill;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public ActiveACQueue() {
            queue = new java.util.ArrayList<ACService>();
            counter = 0;
            kill = false;
            this.setName("Thread for AC");
        }

        public ActiveACQueue(java.util.ArrayList<ACService> queue) {
            this.queue = queue;
            counter = queue.size();
            kill = false;
            this.setName("Thread for AC");
        }

        //
        // -- PUBLIC METHODS -----------------------------------------------
        //

        /**
         * return the current queue of ACServices to perform
         */
        public java.util.ArrayList<ACService> getQueue() {
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
        public synchronized void killMe() {
            kill = true;
            notifyAll();
        }

        @Override
        public void run() {
            // get a reference on the owner body
            // try until it's not null because deserialization of the body 
            // may be not finished when we restart the thread.
            Body owner = null;

            while (true) {
                // if there is no AC to do, wait...
                waitForAC();
                // if body is dead, kill the thread
                if (kill) {
                    break;
                }
                while (owner == null) {
                    owner = LocalBodyStore.getInstance().getLocalBody(ownerBody);
                    // Associating the thred with the body
                    LocalBodyStore.getInstance().setCurrentThreadBody(owner);
                    // it's a halfbody...
                    if (owner == null) {
                        owner = LocalBodyStore.getInstance()
                                              .getLocalHalfBody(ownerBody);
                        LocalBodyStore.getInstance().setCurrentThreadBody(owner);
                    }
                }

                // there are ACs to do !
                try {
                    // enter in the threadStore 
                    owner.enterInThreadStore();

                    // if body has migrated, kill the thread
                    if (kill) {
                        break;
                    }

                    ACService toDo = this.removeACRequest();
                    if (toDo != null) {
                        toDo.doAutomaticContinuation();
                    }

                    // exit from the threadStore
                    owner.exitFromThreadStore();
                } catch (Exception e2) {
                    // to unblock active object
                    owner.exitFromThreadStore();
                    throw new ProActiveRuntimeException("Error while sending reply for AC ",
                        e2);
                }
            }
        }

        // synchronized wait on ACRequest queue
        private synchronized void waitForAC() {
            try {
                while ((counter == 0) && !kill) {
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
        private java.util.ArrayList dests;

        // reply to send
        private Reply reply;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public ACService(java.util.ArrayList dests, Reply reply) {
            this.dests = dests;
            this.reply = reply;
        }

        //
        // -- PUBLIC METHODS -----------------------------------------------
        //
        public void doAutomaticContinuation() throws java.io.IOException {
            if (dests != null) {
                for (int i = 0; i < dests.size(); i++) {
                    UniversalBody dest = (UniversalBody) (dests.get(i));
                    registerDestination(dest);
                    // FAULT-TOLERANCE
                    AbstractBody ownerBody = (AbstractBody) (LocalBodyStore.getInstance()
                                                                           .getLocalBody(FuturePool.this.ownerBody));
                    if (ownerBody == null) {
                        //this might be a halfbody !
                        ownerBody = (AbstractBody) (LocalBodyStore.getInstance()
                                                                  .getLocalHalfBody(FuturePool.this.ownerBody));
                    }
                    FTManager ftm = ownerBody.getFTManager();
                    try {
                        if (ftm != null) {
                            ftm.sendReply(reply, dest);
                        } else {
                            //System.out.println("ACService.doAutomaticContinuation() : sending reply");
                            reply.send(dest);
                        }
                    } catch (IOException ioe) {
                        BodyNonFunctionalException nfe = new SendReplyCommunicationException("Exception occured in while sending reply in AC",
                                ioe, ownerBody, dest.getID());

                        NFEManager.fireNFE(nfe, ownerBody);
                    }
                    removeDestination();
                }
            }
        }
    } //ACService
}
