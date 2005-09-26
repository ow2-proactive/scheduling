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
package org.objectweb.proactive.core.body.ft.servers.recovery;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.body.ft.servers.util.ActiveQueue;
import org.objectweb.proactive.core.body.ft.servers.util.ActiveQueueJob;
import org.objectweb.proactive.core.body.ft.servers.util.JobBarrier;


/**
 * @author cdelbe
 * @since 2.2
 */
public abstract class RecoveryProcessImpl implements RecoveryProcess {

    /** Maximum number of active queues */
    public static final int MAX_ACTIVE_QUEUES = 50;

    //logger
    protected static Logger logger = Logger.getLogger(RecoveryProcess.class.getName());

    // global server
    protected FTServer server;

    // state table (bodyID --> state)
    protected Hashtable bodies;

    // internal pool of thread (ActiveQueue)
    private Vector activeQueuePool;
    private int activeQueuesCounter;

    public RecoveryProcessImpl(FTServer server) {
        this.server = server;
        this.bodies = new Hashtable();
        this.activeQueuePool = new Vector();
        this.activeQueuesCounter = 0;
    }

    /**
     * This method define the recovery behavior.
     * @param failed
     */
    protected abstract void recover(UniqueID failed);

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#register(org.objectweb.proactive.core.UniqueID)
     */
    public void register(UniqueID id) throws RemoteException {
        //register with RUNNING default state
        bodies.put(id, new Integer(RUNNING));

        //adapt active queues pool
        synchronized (this.activeQueuePool) {
            if (this.activeQueuePool.size() < RecoveryProcessImpl.MAX_ACTIVE_QUEUES) {
                ActiveQueue aq = new ActiveQueue("ActiveQueue");
                aq.start();
                this.activeQueuePool.add(aq);
            }
        }
        logger.info("[RECOVERY] Body " + id + " has registered");
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#unregister(org.objectweb.proactive.core.UniqueID)
     */
    public void unregister(UniqueID id) throws RemoteException {
        // remove from the register table
        bodies.remove(id);
        // remove from the location table
        this.server.updateLocation(id, null);
        logger.info("[RECOVERY] Body " + id + " has unregistered");
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#failureDetected(org.objectweb.proactive.core.UniqueID)
     */
    public void failureDetected(UniqueID id) throws RemoteException {
        // id is recovering ??
        int currentState = ((Integer) this.bodies.get(id)).intValue();
        if (currentState == RUNNING) {
            // we can suppose that id is failed
            logger.info("[RECOVERY] Failure is detected for " + id);
            this.bodies.put(id, new Integer(RECOVERING));
            this.recover(id);
        } else if (currentState == RECOVERING) {
            // id is recovering ...  do nothing  
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#updateState(org.objectweb.proactive.core.UniqueID, int)
     */
    public void updateState(UniqueID id, int state) throws RemoteException {
        logger.info("[RECOVERY]  " + id + " is updating its state : " + state);
        this.bodies.put(id, new Integer(state));
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#broadcastFTEvent(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public void broadcastFTEvent(FTMessage fte) throws RemoteException {
        //new BroadcastThread(fte).start();
    }

    /**
     * @see XXX
     */
    public void submitJob(ActiveQueueJob job) {
        synchronized (this.activeQueuePool) {
            ((ActiveQueue) (this.activeQueuePool.get(this.activeQueuesCounter))).addJob(job);
            this.activeQueuesCounter = (this.activeQueuesCounter + 1) % (this.activeQueuePool.size());
        }
    }

    public JobBarrier submitJobWithBarrier(ActiveQueueJob job) {
        synchronized (this.activeQueuePool) {
            JobBarrier b = ((ActiveQueue) (this.activeQueuePool.get(this.activeQueuesCounter))).addJobWithBarrier(job);
            this.activeQueuesCounter = (this.activeQueuesCounter + 1) % (this.activeQueuePool.size());
            return b;
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#getSystemSize()
     */
    public int getSystemSize() throws RemoteException {
        return this.bodies.size();
    }
}
