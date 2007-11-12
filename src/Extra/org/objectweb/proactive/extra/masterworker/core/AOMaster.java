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
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.masterworker.TaskAlreadySubmittedException;
import org.objectweb.proactive.extra.masterworker.TaskException;
import org.objectweb.proactive.extra.masterworker.interfaces.Master;
import org.objectweb.proactive.extra.masterworker.interfaces.Task;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.MasterIntern;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.TaskProvider;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.TaskRepository;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.Worker;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.WorkerDeadListener;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.WorkerManager;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.WorkerWatcher;
import org.objectweb.proactive.extra.masterworker.util.HashSetQueue;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * Main Active Object of the Master/Worker API <br>
 * Literally : the entity to which an user can submit tasks to be solved<br>
 * @author fviale
 */
public class AOMaster implements Serializable, TaskProvider<Serializable>,
    InitActive, RunActive, MasterIntern, WorkerDeadListener {

    /**
         *
         */
    private static final long serialVersionUID = -5623997488806806210L;

    /**
    * log4j logger for the master
    */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERWORKER);

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
     * Worker manager entity (deploy workers)
     */
    protected WorkerManager smanager;

    /**
     * Pinger (checks that workers are alive)
     */
    protected WorkerWatcher pinger;

    /**
     * The repository where to locate tasks
     */
    protected TaskRepository<Task<Serializable>> repository;

    // Slaves resources
    /**
     * stub to access group of workers
     */
    protected Worker workerGroupStub;

    /**
     * Group of workers
     */
    protected Group<Worker> workerGroup;

    /**
     * Initial memory of the workers
     */
    protected Map<String, Object> initialMemory;

    // Sleeping workers (we might want to wake them up)
    /**
     * Stub to group of sleeping workers
     */
    protected Worker sleepingGroupStub;

    /**
     * Group of sleeping workers
     */
    protected Group<Worker> sleepingGroup;

    /**
     * Associations of workers and workers names
     */
    protected HashMap<String, Worker> workersByName;

    /**
     * Reverse associations of workers and workers names
     */
    protected HashMap<Worker, String> workersByNameRev;

    /**
     * Activity of workers, which workers is doing which task
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
     * Creates the master with the initial memory of the workers
     * @param repository repository where the tasks can be found
     * @param initialMemory initial memory of the workers
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
        ((WorkerManager) smanager).addResources(nodes);
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(final URL descriptorURL) {
        ((WorkerManager) smanager).addResources(descriptorURL);
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(final URL descriptorURL,
        final String virtualNodeName) {
        ((WorkerManager) smanager).addResources(descriptorURL, virtualNodeName);
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(final VirtualNode virtualnode) {
        ((WorkerManager) smanager).addResources(virtualnode);
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
    public TaskIntern<Serializable> getTask(final Worker worker,
        final String slaveName) {
        // if we don't know him, we record the slave in our system
        if (!workersByName.containsKey(slaveName)) {
            recordWorker(worker, slaveName);
        }

        if (emptyPending()) {
            slavesActivity.put(slaveName, TaskIntern.NULL_TASK_ID);
            sleepingGroup.add(worker);
            // we return the null task, this will cause the slave to sleep for a while
            return new TaskWrapperImpl();
        } else {
            if (sleepingGroup.contains(worker)) {
                sleepingGroup.remove(worker);
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

        // Slaves
        try {
            // Worker Group
            workerGroupStub = (Worker) ProGroup.newGroup(AOWorker.class.getName());
            workerGroup = ProGroup.getGroup(workerGroupStub);
            // Group of sleeping workers
            sleepingGroupStub = (Worker) ProGroup.newGroup(AOWorker.class.getName());
            sleepingGroup = ProGroup.getGroup(sleepingGroupStub);
            slavesActivity = new HashMap<String, Long>();
            workersByName = new HashMap<String, Worker>();
            workersByNameRev = new HashMap<Worker, String>();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            // The resource manager
            smanager = (AOWorkerManager) ProActiveObject.newActive(AOWorkerManager.class.getName(),
                    new Object[] { stubOnThis, initialMemory });

            // The slave pinger
            pinger = (WorkerWatcher) ProActiveObject.newActive(AOPinger.class.getName(),
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
    public void isDead(final Worker worker) {
        String slaveName = workersByNameRev.get(worker);
        if (logger.isInfoEnabled()) {
            logger.info(slaveName + " reported missing... removing it");
        }

        // we remove the slave from our lists
        if (workerGroup.contains(worker)) {
            workerGroup.remove(worker);
            if (sleepingGroup.contains(worker)) {
                sleepingGroup.remove(worker);
            }

            workersByNameRev.remove(worker);
            workersByName.remove(slaveName);
            // if the slave was handling a task we put the task back to the pending queue
            Long taskId = slavesActivity.get(slaveName);
            if ((taskId != TaskIntern.NULL_TASK_ID) &&
                    launchedTasks.contains(taskId)) {
                launchedTasks.remove(taskId);
                if (pendingTasks.isEmpty()) {
                    // if the queue was empty before the task is rescheduled, we wake-up all sleeping workers
                    if (sleepingGroup.size() > 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Waking up sleeping workers...");
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
     * @param worker the slave to record
     * @param slaveName the name of the slave
     */
    public void recordWorker(final Worker worker, final String slaveName) {
        // We record the slave in our system
        workersByName.put(slaveName, worker);
        workersByNameRev.put(worker, slaveName);
        workerGroup.add(worker);

        // We tell the pinger to watch for this new slave
        pinger.addWorkerToWatch(worker);
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

            // we serve directly every methods from the workers
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
        TaskIntern<Serializable> newTask = getTask(workersByName.get(
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
    public int workerpoolSize() {
        return workerGroup.size();
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
                    logger.debug("Waking up sleeping workers...");
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
        workersByName.clear();
        workersByNameRev.clear();

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
