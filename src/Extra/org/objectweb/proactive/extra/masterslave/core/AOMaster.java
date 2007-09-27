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
package org.objectweb.proactive.extra.masterslave.core;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProException;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.masterslave.TaskAlreadySubmittedException;
import org.objectweb.proactive.extra.masterslave.TaskException;
import org.objectweb.proactive.extra.masterslave.interfaces.Master;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.MasterIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveDeadListener;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManager;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveWatcher;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskProvider;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskRepository;
import org.objectweb.proactive.extra.masterslave.util.HashSetQueue;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * Main Active Object of the Master/Slave API <br>
 * Literally : the entity to which an user can submit tasks to be solved<br>
 * @author fviale
 */
public class AOMaster implements Serializable, TaskProvider<Serializable>,
    InitActive, RunActive, MasterIntern, SlaveDeadListener {

    /**
         *
         */
    private static final long serialVersionUID = -5623997488806806210L;

    /**
    * log4j logger for the master
    */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERSLAVE);

    // Global variables

    /**
     * stub on this active object
     */
    protected Object stubOnThis;

    /**
     * is the master terminated
     */
    protected boolean terminated; // is the master terminated

    // Active objects references
    /**
     * Slave manager entity (deploy slaves)
     */
    protected SlaveManager smanager;

    /**
     * Pinger (checks that slaves are alive)
     */
    protected SlaveWatcher pinger;

    /**
     * The repository where to locate tasks
     */
    protected TaskRepository<Task<Serializable>> repository;

    // Slaves resources
    /**
     * stub to access group of slaves
     */
    protected Slave slaveGroupStub;

    /**
     * Group of slaves
     */
    protected Group<Slave> slaveGroup;

    /**
     * Initial memory of the slaves
     */
    protected Map<String, Object> initialMemory;

    // Sleeping slaves (we might want to wake them up)
    /**
     * Stub to group of sleeping slaves
     */
    protected Slave sleepingGroupStub;

    /**
     * Group of sleeping slaves
     */
    protected Group<Slave> sleepingGroup;

    /**
     * Associations of slaves and slaves names
     */
    protected HashMap<String, Slave> slavesByName;

    /**
     * Reverse associations of slaves and slaves names
     */
    protected HashMap<Slave, String> slavesByNameRev;

    /**
     * Activity of slaves, which slaves is doing which task
     */
    protected HashMap<String, Long> slavesActivity;

    // Task Queues :

    /**
     * tasks that wait for an available slave
     */
    protected HashSetQueue<Long> pendingTasks;

    /**
     * tasks that are currently processing
     */
    protected HashSetQueue<Long> launchedTasks;

    /**
     * tasks that are completed
     */
    protected ResultQueue<Serializable> resultQueue;

    /**
     * if there is a pending request from the client
     */
    protected Request pendingRequest;

    /**
     * Proactive empty no arg constructor
     */
    public AOMaster() {
        // do nothing
    }

    /**
     * Creates the master with the initial memory of the slaves
     * @param repository repository where the tasks can be found
     * @param initialMemory initial memory of the slaves
     */
    public AOMaster(final TaskRepository<Task<Serializable>> repository,
        final Map<String, Object> initialMemory) {
        this.initialMemory = initialMemory;
        this.repository = repository;
        this.pendingRequest = null;
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(Collection<Node> nodes) {
        ((SlaveManager) smanager).addResources(nodes);
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(final URL descriptorURL) {
        ((SlaveManager) smanager).addResources(descriptorURL);
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(final URL descriptorURL,
        final String virtualNodeName) {
        ((SlaveManager) smanager).addResources(descriptorURL, virtualNodeName);
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(final VirtualNode virtualnode) {
        ((SlaveManager) smanager).addResources(virtualnode);
    }

    /**
     * {@inheritDoc}
     */
    public int countAvailableResults() {
        return resultQueue.countAvailableResults();
    }

    /**
     * Tells if the master has some activity
     * @return master activity
     */
    protected boolean emptyPending() {
        return pendingTasks.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public TaskIntern<Serializable> getTask(final Slave slave,
        final String slaveName) {
        // if we don't know him, we record the slave in our system
        if (!slavesByName.containsKey(slaveName)) {
            recordSlave(slave, slaveName);
        }

        if (emptyPending()) {
            slavesActivity.put(slaveName, TaskIntern.NULL_TASK_ID);
            sleepingGroup.add(slave);
            // we return the null task, this will cause the slave to sleep for a while
            return new TaskWrapperImpl();
        } else {
            if (sleepingGroup.contains(slave)) {
                sleepingGroup.remove(slave);
            }

            Iterator<Long> it = pendingTasks.iterator();
            long taskId = it.next();
            // We remove the task from the pending list
            it.remove();
            // We add the task inside the launched list
            launchedTasks.add(taskId);
            slavesActivity.put(slaveName, taskId);
            TaskIntern<Serializable> taskfuture = (TaskIntern<Serializable>) repository.getTask(taskId);
            TaskIntern<Serializable> realTask = (TaskIntern<Serializable>) ProFuture.getFutureValue(taskfuture);
            repository.saveTask(taskId);

            return realTask;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void initActivity(final Body body) {
        stubOnThis = ProActiveObject.getStubOnThis();
        // General initializations
        terminated = false;
        // Queues
        pendingTasks = new HashSetQueue<Long>();
        launchedTasks = new HashSetQueue<Long>();
        resultQueue = new ResultQueue<Serializable>(Master.OrderingMode.CompletionOrder);

        // Ignore NFEs occurring on ourself (send reply exceptions on dead slaves)
        ProActiveObject.getBodyOnThis().addNFEListener(NFEListener.NOOP_LISTENER);

        // Slaves
        try {
            // Slave Group
            slaveGroupStub = (Slave) ProGroup.newGroup(AOSlave.class.getName());
            slaveGroup = ProGroup.getGroup(slaveGroupStub);
            // we ignore NFE on this group (the pinger is responsible for it)
            ProException.addNFEListenerOnGroup(slaveGroupStub,
                NFEListener.NOOP_LISTENER);
            // Group of sleeping slaves
            sleepingGroupStub = (Slave) ProGroup.newGroup(AOSlave.class.getName());
            sleepingGroup = ProGroup.getGroup(sleepingGroupStub);
            // we ignore NFE on this group (the pinger is responsible for it)
            ProException.addNFEListenerOnGroup(sleepingGroupStub,
                NFEListener.NOOP_LISTENER);
            slavesActivity = new HashMap<String, Long>();
            slavesByName = new HashMap<String, Slave>();
            slavesByNameRev = new HashMap<Slave, String>();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            // The resource manager
            smanager = (AOSlaveManager) ProActiveObject.newActive(AOSlaveManager.class.getName(),
                    new Object[] { stubOnThis, initialMemory });

            // The slave pinger
            pinger = (SlaveWatcher) ProActiveObject.newActive(AOPinger.class.getName(),
                    new Object[] { stubOnThis });
        } catch (ActiveObjectCreationException e1) {
            e1.printStackTrace();
        } catch (NodeException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void isDead(final Slave slave) {
        String slaveName = slavesByNameRev.get(slave);
        if (logger.isInfoEnabled()) {
            logger.info(slaveName + " reported missing... removing it");
        }

        // we remove the slave from our lists
        if (slaveGroup.contains(slave)) {
            slaveGroup.remove(slave);
            if (sleepingGroup.contains(slave)) {
                sleepingGroup.remove(slave);
            }

            slavesByNameRev.remove(slave);
            slavesByName.remove(slaveName);
            // if the slave was handling a task we put the task back to the pending queue
            Long taskId = slavesActivity.get(slaveName);
            if ((taskId != TaskIntern.NULL_TASK_ID) &&
                    launchedTasks.contains(taskId)) {
                launchedTasks.remove(taskId);
                if (pendingTasks.isEmpty()) {
                    // if the queue was empty before the task is rescheduled, we wake-up all sleeping slaves
                    if (sleepingGroup.size() > 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Waking up sleeping slaves...");
                        }

                        // We wake up the sleeping guys
                        sleepingGroupStub.wakeup();
                    }

                    pendingTasks.add(taskId);
                } else {
                    pendingTasks.add(taskId);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return resultQueue.isEmpty();
    }

    /**
     * Record the given slave in our system
     * @param slave the slave to record
     * @param slaveName the name of the slave
     */
    public void recordSlave(final Slave slave, final String slaveName) {
        // We record the slave in our system
        slavesByName.put(slaveName, slave);
        slavesByNameRev.put(slave, slaveName);
        slaveGroup.add(slave);

        // We tell the pinger to watch for this new slave
        pinger.addSlaveToWatch(slave);
    }

    /**
     * {@inheritDoc}
     */
    public void runActivity(final Body body) {
        Service service = new Service(body);
        while (!terminated) {
            service.waitForRequest();
            // We detect a waitXXX request in the request queue
            Request waitRequest = service.getOldest(new FindWaitFilter());
            if (waitRequest != null) {
                if (pendingRequest == null) {
                    // if there is one and there was none previously found we remove it and store it for later
                    pendingRequest = waitRequest;
                    service.blockingRemoveOldest(new FindWaitFilter());
                } else {
                    // if there is one and there was another one pending, we serve it immediately (it's an error)
                    service.serveOldest(new FindWaitFilter());
                }
            }

            // we serve directly every methods from the slaves
            service.serveAll("getTask");
            service.serveAll("sendResultAndGetTask");
            service.serveAll("isDead");

            // we serve everything else which is not a waitXXX method
            // Careful, the order is very important here, we need to serve the solve method before the waitXXX
            service.serveAll(new FindNotWaitFilter());

            // we maybe serve the pending waitXXX method if there is one and if the necessary results are collected
            maybeServePending();
        }

        // we clear the service to avoid dirty pending requests 
        service.flushAll();
        // we block the communications because a getTask request might still be coming from a slave created just before the master termination
        body.blockCommunication();
        // we finally terminate the master
        body.terminate();
    }

    /**
     * {@inheritDoc}
     */
    public TaskIntern<Serializable> sendResultAndGetTask(
        final ResultIntern<Serializable> result, final String originatorName) {
        long taskId = result.getId();
        if (launchedTasks.contains(taskId)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Result of task " + taskId + " received.");
            }

            launchedTasks.remove(taskId);
            // We add the result in the result queue
            resultQueue.addCompletedTask(result);
            // We remove the task from the repository (it won't be needed anymore)
            repository.removeTask(taskId);
        }

        // We assign a new task to the slave
        TaskIntern<Serializable> newTask = getTask(slavesByName.get(
                    originatorName), originatorName);
        return newTask;
    }

    /**
     * If there is a pending waitXXX method, we serve it if the necessary results are collected
     */
    protected void maybeServePending() {
        if (pendingRequest != null) {
            if (pendingRequest.getMethodName().equals("waitOneResult") &&
                    resultQueue.isOneResultAvailable()) {
                servePending();
            } else if (pendingRequest.getMethodName().equals("waitAllResults") &&
                    resultQueue.areAllResultsAvailable()) {
                servePending();
            } else if (pendingRequest.getMethodName().equals("waitKResults")) {
                int k = (Integer) pendingRequest.getParameter(0);
                if (((resultQueue.countPendingResults() +
                        resultQueue.countAvailableResults()) < k) || (k <= 0)) {
                    servePending();
                } else if (resultQueue.countAvailableResults() >= k) {
                    servePending();
                }
            }
        }
    }

    /**
     * Serve the pending waitXXX method
     */
    protected void servePending() {
        Body body = ProActiveObject.getBodyOnThis();
        Request req = pendingRequest;
        pendingRequest = null;
        body.serve(req);
    }

    /**
     * {@inheritDoc}
     */
    public void setPingPeriod(long periodMillis) {
        pinger.setPingPeriod(periodMillis);
    }

    /**
     * {@inheritDoc}
     */
    public void setResultReceptionOrder(final Master.OrderingMode mode) {
        resultQueue.setMode(mode);
    }

    /**
     * {@inheritDoc}
     */
    public int slavepoolSize() {
        return slaveGroup.size();
    }

    /**
     * {@inheritDoc}
     */
    public void solveIds(final List<Long> taskIds) {
        logger.debug("Adding " + taskIds.size() + " tasks by " +
            Thread.currentThread() + " and body is " +
            ProActiveObject.getContext().getBody());

        for (Long taskId : taskIds) {
            solve(taskId);
        }
    }

    /**
     * Adds a task to solve
     * @param taskId id of the task to solve
     * @throws IllegalArgumentException
     */
    protected void solve(final Long taskId) {
        resultQueue.addPendingTask(taskId);

        if (emptyPending()) {
            pendingTasks.add(taskId);
            if (sleepingGroup.size() > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Waking up sleeping slaves...");
                }

                // We wake up the sleeping guys
                sleepingGroupStub.wakeup();
            }
        } else {
            pendingTasks.add(taskId);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void terminate(final boolean freeResources) {
        terminateIntern(freeResources);
    }

    /**
     * Synchronous version of terminate
     * @param freeResources do we free as well deployed resources
     * @return true if completed successfully
     */
    public boolean terminateIntern(final boolean freeResources) {
        // We empty pending queues
        pendingTasks.clear();

        if (logger.isDebugEnabled()) {
            logger.debug("Terminating Master...");
        }

        // We terminate the pinger
        ProFuture.waitFor(pinger.terminate());
        // We terminate the slave manager
        ProFuture.waitFor(smanager.terminate(freeResources));
        if (logger.isDebugEnabled()) {
            logger.debug("Master terminated...");
        }

        launchedTasks.clear();

        slavesActivity.clear();
        slavesByName.clear();
        slavesByNameRev.clear();

        terminated = true;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public List<ResultIntern<Serializable>> waitAllResults()
        throws TaskException {
        if (pendingRequest != null) {
            throw new IllegalStateException(
                "Already waiting for a wait request");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("All results received by the user.");
        }

        return resultQueue.getAll();
    }

    /**
     * {@inheritDoc}
     */
    public List<ResultIntern<Serializable>> waitKResults(final int k)
        throws TaskException {
        if (pendingRequest != null) {
            throw new IllegalStateException(
                "Already waiting for a wait request");
        }

        if ((resultQueue.countPendingResults() +
                resultQueue.countAvailableResults()) < k) {
            throw new IllegalArgumentException("" + k + " is too big");
        } else if (k <= 0) {
            throw new IllegalArgumentException("Wrong value : " + k);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("" + k + " results received by the user.");
        }

        return resultQueue.getNextK(k);
    }

    /**
     * {@inheritDoc}
     */
    public ResultIntern<Serializable> waitOneResult() throws TaskException {
        if (pendingRequest != null) {
            throw new IllegalStateException(
                "Already waiting for a wait request");
        }

        ResultIntern<Serializable> res = resultQueue.getNext();

        if (logger.isDebugEnabled()) {
            logger.debug("Result of task " + res.getId() +
                " received by the user.");
        }

        return res;
    }

    /**
     * @author fviale
     * Internal class for filtering requests in the queue
     */
    protected class FindWaitFilter implements RequestFilter {

        /**
                 *
                 */
        private static final long serialVersionUID = -9077989348627519335L;

        /**
        * Creates a filter
        */
        public FindWaitFilter() {
        }

        /**
         * {@inheritDoc}
         */
        public boolean acceptRequest(final Request request) {
            // We find all the requests that are not servable yet
            String name = request.getMethodName();
            if (name.equals("waitOneResult")) {
                return true;
            } else if (name.equals("waitAllResults")) {
                return true;
            } else {
                return name.equals("waitKResults");
            }
        }
    }

    /**
     * @author fviale
     * Internal class for filtering requests in the queue
     */
    protected class FindNotWaitFilter implements RequestFilter {

        /**
                 *
                 */
        private static final long serialVersionUID = -1650163314641695052L;

        /**
        * Creates the filter
        */
        public FindNotWaitFilter() {
        }

        /**
         * {@inheritDoc}
         */
        public boolean acceptRequest(final Request request) {
            // We find all the requests which can't be served yet
            String name = request.getMethodName();
            if (name.equals("waitOneResult")) {
                return false;
            } else if (name.equals("waitAllResults")) {
                return false;
            } else {
                return !(name.equals("waitKResults"));
            }
        }
    }

    public void solve(List<TaskIntern<ResultIntern<Serializable>>> tasks)
        throws TaskAlreadySubmittedException {
        throw new UnsupportedOperationException("Illegal call");
    }
}
