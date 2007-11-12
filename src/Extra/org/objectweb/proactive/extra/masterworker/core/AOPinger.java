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
package org.objectweb.proactive.extra.masterworker.core;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.group.ExceptionInGroup;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.Worker;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.WorkerDeadListener;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.WorkerWatcher;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The Pinger Active Object is responsible for watching workers'activity. <br>
 * It reports workers failure to the Master<br>
 * @author fviale
 *
 */
public class AOPinger implements WorkerWatcher, RunActive, InitActive,
    Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -7489033564540496244L;

    /**
    * pinger log4j logger
    */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERWORKER_WORKERS);

    /**
     * Stub on the active object
     */
    protected AOPinger stubOnThis;

    /**
     * is this active object terminated
     */
    protected boolean terminated;

    /**
     * interval when workers are sent a ping message
     */
    protected long pingPeriod;

    /**
     * Who will be notified when workers are dead (in general : the master)
     */
    protected WorkerDeadListener listener;

    /**
     * Stub to worker group
     */
    protected Worker workerGroupStub;

    /**
     * Worker group
     */
    protected Group<Worker> workerGroup;

    /**
     * for internal use
     */
    private Thread localThread;

    /**
     * ProActive empty constructor
     */
    public AOPinger() {
    }

    /**
     * Creates a pinger with the given listener
     * @param listener object which will be notified when a worker is dead
     */
    public AOPinger(final WorkerDeadListener listener) {
        this.listener = listener;
        terminated = false;
        pingPeriod = Long.parseLong(PAProperties.PA_MASTERWORKER_PINGPERIOD.getValue());
    }

    /**
     * {@inheritDoc}
     */
    public void addWorkerToWatch(final Worker worker) {
        workerGroup.add(worker);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void initActivity(final Body body) {
        try {
            workerGroupStub = (Worker) ProGroup.newGroup(AOWorker.class.getName());
            workerGroup = ProGroup.getGroup(workerGroupStub);
            stubOnThis = (AOPinger) ProActiveObject.getStubOnThis();
            body.setImmediateService("terminate");
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeWorkerToWatch(final Worker worker) {
        workerGroup.remove(worker);
    }

    /**
     * {@inheritDoc}
     */
    public void runActivity(final Body body) {
        localThread = Thread.currentThread();
        Service service = new Service(body);
        while (!terminated) {
            // we serve everything
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }
            try {
                workerGroupStub.heartBeat();
            } catch (Exception e) {
                if (e instanceof ExceptionListException) {
                    ExceptionListException ele = (ExceptionListException) e;
                    synchronized (ele) {
                        Iterator<ExceptionInGroup> it = ele.iterator();
                        while (it.hasNext()) {
                            ExceptionInGroup eig = it.next();
                            stubOnThis.workerMissing((Worker) eig.getObject());
                        }
                    }
                }
            }
            try {
                Thread.sleep(pingPeriod);
            } catch (InterruptedException e) {
                // do not print message, pinger is terminating
            }
        }

        body.terminate();
    }

    /**
     * {@inheritDoc}
     */
    public void setPingPeriod(long periodMillis) {
        this.pingPeriod = periodMillis;
    }

    /**
     * Reports that a worker is missing
     * @param worker the missing worker
     */
    public void workerMissing(final Worker worker) {
        synchronized (workerGroup) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "A worker is missing...reporting back to the Master");
            }

            if (workerGroup.contains(worker)) {
                listener.isDead(worker);
                workerGroup.remove(worker);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper terminate() {
        this.terminated = true;
        localThread.interrupt();

        if (logger.isDebugEnabled()) {
            logger.debug("Pinger terminated...");
        }

        return new BooleanWrapper(true);
    }

    /*
     * TODO: handle exceptions with stubOnThis.workerMissing((Worker) eig.getObject()); for each
     * member in the ExceptionListException
     */
}
