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
package org.objectweb.proactive.extensions.masterworker.core;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.*;
import org.objectweb.proactive.extensions.masterworker.util.TaskID;
import org.objectweb.proactive.extensions.masterworker.util.TaskQueue;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

import java.io.Serializable;
import java.net.URL;
import java.util.*;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * Main Active Object of the Master/Worker API <br>
 * Literally : the entity to which an user can submit tasks to be solved<br>
 *
 * @author The ProActive Team
 */
public class AOMaster implements Serializable, WorkerMaster, InitActive, RunActive, MasterIntern,
        WorkerDeadListener {

    /** log4j logger for the master */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.MASTERWORKER);
    private static final boolean debug = logger.isDebugEnabled();

    /** How many tasks do we initially send to each worker, default value */
    private static final int DEFAULT_INITIAL_TASK_FLOODING = 2;

    /** How many tasks do we initially send to each worker */
    private int initial_task_flooding = DEFAULT_INITIAL_TASK_FLOODING;

    // Global variables

    /** stub on this active object */
    private AOMaster stubOnThis;

    /** is the master terminated */
    private boolean terminated; // is the master terminated

    // Active objects references :

    /** Worker manager entity (deploy workers) */
    private WorkerManager smanager;

    /** Pinger (checks that workers are alive) */
    private WorkerWatcher pinger;

    /** The repository where to locate tasks */
    private TaskRepository repository;

    // Workers resources :

    /** stub to access group of workers */
    private Worker workerGroupStub;

    /** Group of workers */
    private Group<Worker> workerGroup;

    /** Initial memory of the workers */
    private Map<String, Serializable> initialMemory;

    /** Stub to group of sleeping workers */
    private Worker sleepingGroupStub;

    /** Group of sleeping workers */
    private Group<Worker> sleepingGroup;

    /** Associations of workers and workers names */
    private HashMap<String, Worker> workersByName;

    /** Reverse associations of workers and workers names */
    private HashMap<Worker, String> workersByNameRev;

    /** Activity of workers, which workers is doing which task */
    private HashMap<String, Set<Long>> workersActivity;

    // Task Queues :

    /** main tasks (submitted by the main client) and that wait for an available worker */
    // private HashSetQueue<Long> pendingTasks;
    /** subtasks (submitted by a task itself), these tasks are prioritary over the standard ones */
    //  private HashMap<String, HashSetQueue<Long>> pendingSubTasks;
    private TaskQueue pendingTasks;

    /** tasks that are currently processing */
    private HashMap<Long, String> launchedTasks;

    /** main tasks that are completed */
    private ResultQueue<Serializable> resultQueue;

    /** sub tasks that are completed */
    private HashMap<String, ResultQueue<Serializable>> subResultQueues;

    /** if there is a pending request from the client */
    private Request pendingRequest;

    /** if there is a pending request from the sub clients (the workers) */
    private HashMap<String, Request> pendingSubRequests;

    // For Remote Master, deployment has been initiated on the client :

    /** descriptor used to deploy the master (if any) */
    private URL masterDescriptorURL;

    /** GCMapplication used to deploy the master (if any) */
    private GCMApplication applicationUsed;

    /** VN Name of the master (if any) */
    private String masterVNNAme;

    /** Proactive empty no arg constructor */
    @Deprecated
    public AOMaster() {
        /* do nothing */
    }

    /**
     * Creates the master with the initial memory of the workers
     *
     * @param initialMemory       initial memory of the workers
     * @param masterDescriptorURL descriptor used to deploy the master (if any)
     * @param applicationUsed     GCMapplication used to deploy the master (if any)
     * @param masterVNNAme        VN Name of the master (if any)
     */
    public AOMaster(final Map<String, Serializable> initialMemory, final URL masterDescriptorURL,
            final GCMApplication applicationUsed, final String masterVNNAme) {
        this.initialMemory = initialMemory;
        this.masterDescriptorURL = masterDescriptorURL;
        this.applicationUsed = applicationUsed;
        this.masterVNNAme = masterVNNAme;
        try {
            this.repository = (AOTaskRepository) PAActiveObject.newActive(AOTaskRepository.class.getName(),
                    new Object[] {});
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        this.pendingRequest = null;
    }

    /** {@inheritDoc} */
    public void addResources(final Collection<Node> nodes) {
        (smanager).addResources(nodes);
    }

    /** {@inheritDoc} */
    public void addResources(final URL descriptorURL) throws ProActiveException {
        (smanager).addResources(descriptorURL);
    }

    /** {@inheritDoc} */
    public void addResources(final URL descriptorURL, final String virtualNodeName) throws ProActiveException {
        (smanager).addResources(descriptorURL, virtualNodeName);
    }

    /** {@inheritDoc} */
    public void addResources(final String schedulerURL, final String user, final String password)
            throws ProActiveException {
        (smanager).addResources(schedulerURL, user, password);

    }

    /** {@inheritDoc} */
    public int countAvailableResults(String originatorName) {
        if (originatorName == null) {
            return resultQueue.countAvailableResults();
        } else {
            if (subResultQueues.containsKey(originatorName)) {
                return subResultQueues.get(originatorName).countAvailableResults();
            } else {
                throw new IllegalArgumentException("Unknown originator: " + originatorName);
            }

        }
    }

    /**
     * Tells if the master has some activity
     *
     * @return master activity
     */
    private boolean emptyPending() {
        return pendingTasks.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Queue<TaskIntern<Serializable>> getTasksInternal(final Worker worker, final String workerName,
            boolean flooding) {
        // if we don't know him, we record the worker in our system
        if (!workersByName.containsKey(workerName)) {
            if (debug) {
                logger.debug("new worker " + workerName + " recorded by the master");
            }
            recordWorker(worker, workerName);
        }

        if (emptyPending()) {
            // We say that the worker is sleeping if we don't know it yet or if it's not doing a task
            if (workersActivity.containsKey(workerName)) {
                if (workersActivity.get(workerName).size() == 0) {
                    sleepingGroup.add(worker);
                }
            } else {
                workersActivity.put(workerName, new HashSet<Long>());
                sleepingGroup.add(worker);
            }
            if (debug) {
                logger.debug("No task given to " + workerName);
            }
            // we return an empty queue, this will cause the worker to sleep for a while
            return new LinkedList<TaskIntern<Serializable>>();
        } else {
            if (sleepingGroup.contains(worker)) {
                sleepingGroup.remove(worker);
            }
            Queue<TaskIntern<Serializable>> tasksToDo = new LinkedList<TaskIntern<Serializable>>();
            Iterator<TaskID> it = pendingTasks.iterator();

            // If we are in a flooding scenario, we send at most initial_task_flooding tasks
            int flooding_value = flooding ? initial_task_flooding : 1;
            int i = 0;
            while (it.hasNext() && i < flooding_value) {
                TaskID taskId = it.next();
                // We remove the task from the pending list
                it.remove();

                // We add the task inside the launched list
                launchedTasks.put(taskId.getID(), taskId.getOriginator());
                // We record the worker activity
                if (workersActivity.containsKey(workerName)) {
                    Set<Long> wact = workersActivity.get(workerName);
                    wact.add(taskId.getID());
                } else {
                    Set<Long> wact = new HashSet<Long>();
                    wact.add(taskId.getID());
                    workersActivity.put(workerName, wact);
                }
                TaskIntern<Serializable> taskfuture = (TaskIntern<Serializable>) repository.getTask(taskId
                        .getID());
                TaskIntern<Serializable> realTask = (TaskIntern<Serializable>) PAFuture
                        .getFutureValue(taskfuture);
                repository.saveTask(taskId.getID());
                tasksToDo.offer(realTask);
                if (debug) {
                    logger.debug("Task " + taskId.getID() + " given to " + workerName);
                }
                i++;
            }

            return tasksToDo;
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Queue<TaskIntern<Serializable>> getTasks(final Worker worker, final String workerName,
            boolean reflooding) {
        return getTasksInternal(worker, workerName, reflooding);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void initActivity(final Body body) {
        stubOnThis = (AOMaster) PAActiveObject.getStubOnThis();
        // General initializations
        terminated = false;
        // Queues
        pendingTasks = new TaskQueue();
        launchedTasks = new HashMap<Long, String>();
        resultQueue = new ResultQueue<Serializable>(Master.COMPLETION_ORDER);
        pendingSubRequests = new HashMap<String, Request>();
        subResultQueues = new HashMap<String, ResultQueue<Serializable>>();

        // Workers
        try {
            String workerClassName = AOWorker.class.getName();
            // Worker Group
            workerGroupStub = (Worker) PAGroup.newGroup(workerClassName);
            workerGroup = PAGroup.getGroup(workerGroupStub);
            // Group of sleeping workers
            sleepingGroupStub = (Worker) PAGroup.newGroup(workerClassName);
            sleepingGroup = PAGroup.getGroup(sleepingGroupStub);
            workersActivity = new HashMap<String, Set<Long>>();
            workersByName = new HashMap<String, Worker>();
            workersByNameRev = new HashMap<Worker, String>();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            // These two objects are initiated inside the initActivity because of the need to the stub on this
            // The resource manager
            smanager = (AOWorkerManager) PAActiveObject.newActive(AOWorkerManager.class.getName(),
                    new Object[] { stubOnThis, initialMemory, masterDescriptorURL, applicationUsed,
                            masterVNNAme });

            // The worker pinger
            pinger = (WorkerWatcher) PAActiveObject.newActive(AOPinger.class.getName(),
                    new Object[] { stubOnThis });
        } catch (ActiveObjectCreationException e1) {
            e1.printStackTrace();
        } catch (NodeException e1) {
            e1.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    public boolean isDead(final Worker worker) {
        if (workersByNameRev.containsKey(worker)) {
            String workerName = workersByNameRev.get(worker);
            if (logger.isInfoEnabled()) {
                logger.info(workerName + " reported missing... removing it");
            }

            // we remove the worker from our lists
            if (workerGroup.contains(worker)) {
                workerGroup.remove(worker);
                if (sleepingGroup.contains(worker)) {
                    sleepingGroup.remove(worker);
                }

                // Among our "dictionary of workers", we remove only entries in the reverse dictionary,
                // By doing that, if ever the worker appears not completely dead and reappears, we can handle it
                workersByName.remove(workerName);
                // if the worker was handling tasks we put the tasks back to the pending queue
                for (Long taskId : workersActivity.get(workerName)) {
                    if (launchedTasks.containsKey(taskId)) {
                        String submitter = launchedTasks.remove(taskId);
                        if (emptyPending()) {
                            // if the queue was empty before the task is rescheduled, we wake-up all sleeping workers
                            if (sleepingGroup.size() > 0) {
                                if (debug) {
                                    logger.debug("Waking up sleeping workers...");
                                }

                                // We wake up the sleeping guys
                                sleepingGroupStub.wakeup();
                            }
                        }
                        pendingTasks.add(new TaskID(submitter, taskId));

                    }
                }
                smanager.isDead(workerName);
            }
            return true;
        }
        return false;

    }

    /** {@inheritDoc} */
    public boolean isDead(final String workerName) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public boolean isEmpty(String originatorName) {
        if (originatorName == null) {
            return (resultQueue.isEmpty() && pendingTasks.isEmpty());
        } else {
            if (subResultQueues.containsKey(originatorName)) {
                return (subResultQueues.get(originatorName).isEmpty() && pendingTasks
                        .hasTasksByOriginator(originatorName));
            } else {
                throw new IllegalArgumentException("Unknown originator " + originatorName);
            }
        }
    }

    /**
     * Record the given worker in our system
     *
     * @param worker     the worker to record
     * @param workerName the name of the worker
     */
    public void recordWorker(final Worker worker, final String workerName) {
        // We record the worker in our system
        workersByName.put(workerName, worker);
        workersByNameRev.put(worker, workerName);
        workerGroup.add(worker);

        // We tell the pinger to watch for this new worker
        pinger.addWorkerToWatch(worker);
    }

    /** {@inheritDoc} */
    public void runActivity(final Body body) {
        Service service = new Service(body);
        while (!terminated) {
            service.waitForRequest();

            // Serving methods other than waitXXX
            while (service.hasRequestToServe(new FindNotWaitAndTerminateFilter())) {
                service.serveAll(new FindNotWaitAndTerminateFilter());
            }

            // We detect all waitXXX requests in the request queue
            Request waitRequest = service.getOldest(new FindWaitFilter());
            while (waitRequest != null) {
                String originatorName = (String) waitRequest.getParameter(0);
                // if there is one and there was none previously found we remove it and store it for later
                if (originatorName == null) {
                    pendingRequest = waitRequest;
                    if (debug) {
                        logger.debug("pending waitXXX from main client stored");
                    }
                } else {
                    pendingSubRequests.put(originatorName, waitRequest);
                    if (debug) {
                        logger.debug("pending waitXXX from " + originatorName + " stored");
                    }
                }
                service.blockingRemoveOldest(new FindWaitFilter());
                waitRequest = service.getOldest(new FindWaitFilter());

            }

            // we maybe serve the pending waitXXX methods if there are some and if the necessary results are collected
            maybeServePending();
            service.serveAll("terminateIntern");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Master terminated...");
        }

        // we clear the service to avoid dirty pending requests 
        service.flushAll();
        // we block the communications because a getTask request might still be coming from a worker created just before the master termination
        body.blockCommunication();
        // we finally terminate the master
        body.terminate();
    }

    /** {@inheritDoc} */
    public Queue<TaskIntern<Serializable>> sendResultAndGetTasks(final ResultIntern<Serializable> result,
            final String originatorName, boolean reflooding) {

        sendResult(result, originatorName);
        Worker worker = workersByName.get(originatorName);
        // if the worker has already reported dead, we need to handle that it suddenly reappears
        if (!workersByNameRev.containsKey(worker)) {
            // We do this by removing the worker from our database, which will trigger that it will be recorded again
            workersByName.remove(originatorName);
        }
        return getTasksInternal(worker, originatorName, reflooding);
    }

    public boolean sendResult(ResultIntern<Serializable> result, String originatorName) {
        long id = result.getId();
        if (launchedTasks.containsKey(id)) {
            if (debug) {
                logger.debug(originatorName + " sends result of task " + id);
            }
            String submitter = launchedTasks.remove(id);
            // We remove the task from the worker activity
            if (workersActivity.containsKey(originatorName)) {
                Set<Long> wact = workersActivity.get(originatorName);
                wact.remove(id);
            }
            // We add the result in the result queue
            if (submitter == null) {
                resultQueue.addCompletedTask(result);
            } else {
                subResultQueues.get(submitter).addCompletedTask(result);
            }
            // We remove the task from the repository (it won't be needed anymore)
            repository.removeTask(id);
        } else {
            if (debug) {
                logger.debug(originatorName + " sends result of task " + id + " but it's unknown.");
            }
        }

        return true;
    }

    /** If there is a pending waitXXX method, we serve it if the necessary results are collected */
    private void maybeServePending() {
        Set<Map.Entry<String, Request>> newSet = new HashSet<Map.Entry<String, Request>>(pendingSubRequests
                .entrySet());
        for (Map.Entry<String, Request> ent : newSet) {
            String originator = ent.getKey();
            String methodName = ent.getValue().getMethodName();
            ResultQueue rq = subResultQueues.get(originator);
            if (methodName.equals("waitOneResult") && rq.isOneResultAvailable()) {
                servePending(originator);
            } else if (methodName.equals("waitAllResults") && rq.areAllResultsAvailable()) {
                servePending(originator);
            } else if (methodName.equals("waitKResults")) {
                int k = (Integer) ent.getValue().getParameter(1);
                if (rq.countAvailableResults() >= k) {
                    servePending(originator);
                }
            }
        }

        if (pendingRequest != null) {
            String methodName = pendingRequest.getMethodName();
            if (methodName.equals("waitOneResult") && resultQueue.isOneResultAvailable()) {
                servePending(null);
            } else if (methodName.equals("waitAllResults") && resultQueue.areAllResultsAvailable()) {
                servePending(null);
            } else if (methodName.equals("waitKResults")) {
                int k = (Integer) pendingRequest.getParameter(1);
                if (resultQueue.countAvailableResults() >= k) {
                    servePending(null);
                }
            }
        }
    }

    /** Serve the pending waitXXX method */
    private void servePending(String originator) {
        Body body = PAActiveObject.getBodyOnThis();
        if (originator == null) {
            if (debug) {
                logger.debug("serving pending waitXXX method from main client");
            }

            Request req = pendingRequest;
            pendingRequest = null;
            body.serve(req);
        } else {
            if (debug) {
                logger.debug("serving pending waitXXX method from " + originator);
            }
            Request req = pendingSubRequests.remove(originator);
            body.serve(req);
        }
    }

    /** {@inheritDoc} */
    public void clear() {
        if (debug) {
            logger.debug("Master cleared.");
        }
        // We clear the queues
        resultQueue.clear();
        pendingTasks.clear();
        launchedTasks.clear();
        // We clear the workers activity memory
        workersActivity.clear();
        // We tell all the worker to clear their pending tasks
        BooleanWrapper ack = workerGroupStub.clear();
        PAGroup.waitAll(ack);
        // Now every workers are sleeping
        sleepingGroup.clear();
        sleepingGroup.addAll(workerGroup);
        // We clear the repository
        repository.clear();
    }

    /** {@inheritDoc} */
    public void setPingPeriod(long periodMillis) {
        pinger.setPingPeriod(periodMillis);
    }

    /** {@inheritDoc} */
    public void setInitialTaskFlooding(final int number_of_tasks) {
        initial_task_flooding = number_of_tasks;
    }

    /** {@inheritDoc} */
    public int workerpoolSize() {
        return workerGroup.size();
    }

    /** {@inheritDoc} */
    private void solveIds(final List<Long> taskIds, String originator) {

        for (Long taskId : taskIds) {
            solve(taskId, originator);
        }
    }

    /**
     * Adds a task to solve
     *
     * @param taskId id of the task to solve
     * @throws IllegalArgumentException
     */
    private void solve(final Long taskId, String originator) {
        // If we have sleepers
        if (sleepingGroup.size() > 0) {
            if (debug) {
                logger.debug("Waking up sleeping workers...");
            }

            // We wake up the sleeping guys
            sleepingGroupStub.wakeup();
        }
        // If the main client is sending the tasks
        if (originator == null) {
            resultQueue.addPendingTask(taskId);
        } else {
            // If one worker is sending the tasks
            if (subResultQueues.containsKey(originator)) {
                subResultQueues.get(originator).addPendingTask(taskId);
            } else {
                ResultQueue rq = new ResultQueue(resultQueue.getMode());
                rq.addPendingTask(taskId);
                subResultQueues.put(originator, rq);
            }
        }
        pendingTasks.add(new TaskID(originator, taskId));
    }

    /**
     * Creates an internal wrapper of the given task
     * This wrapper will identify the task internally via an ID
     *
     * @param task task to be wrapped
     * @return wrapped version
     */
    private long createId(Task<? extends Serializable> task) {
        return repository.addTask(task);
    }

    /**
     * Creates an internal version of the given collection of tasks
     * This wrapper will identify the task internally via an ID
     *
     * @param tasks collection of tasks to be wrapped
     * @return wrapped version
     */
    private List<Long> createIds(List<? extends Task<? extends Serializable>> tasks) {
        List<Long> wrappings = new ArrayList<Long>();
        for (Task<? extends Serializable> task : tasks) {
            wrappings.add(createId(task));
        }

        return wrappings;
    }

    public void solveIntern(String originatorName, List<? extends Task<? extends Serializable>> tasks) {
        List<Long> wrappers = createIds(tasks);
        solveIds(wrappers, originatorName);
    }

    /** {@inheritDoc} */
    public void setResultReceptionOrder(final String originatorName, final SubMaster.OrderingMode mode) {
        if (originatorName == null) {
            resultQueue.setMode(mode);
        } else {
            if (subResultQueues.containsKey(originatorName)) {
                subResultQueues.get(originatorName).setMode(mode);
            } else {
                ResultQueue rq = new ResultQueue(mode);
                subResultQueues.put(originatorName, rq);
            }
        }
    }

    public void solve(List<TaskIntern<ResultIntern<Serializable>>> tasks) {
        throw new UnsupportedOperationException("Illegal call");
    }

    /**
     * Synchronous version of terminate
     *
     * @param freeResources do we free as well deployed resources
     * @return true if completed successfully
     */
    public boolean terminateIntern(final boolean freeResources) {

        if (debug) {
            logger.debug("Terminating Master...");
        }

        // We empty pending queues
        pendingTasks.clear();

        // we empty groups
        workerGroup.purgeExceptionAndNull();
        workerGroup.clear();
        workerGroupStub = null;
        sleepingGroup.purgeExceptionAndNull();
        sleepingGroup.clear();
        sleepingGroupStub = null;

        // We terminate the pinger
        PAFuture.waitFor(pinger.terminate());
        // We terminate the worker manager
        PAFuture.waitFor(smanager.terminate(freeResources));
        // We terminate the repository
        repository.terminate();

        launchedTasks.clear();

        workersActivity.clear();
        workersByName.clear();
        workersByNameRev.clear();

        terminated = true;
        return true;
    }

    /** {@inheritDoc} */
    public List<ResultIntern<Serializable>> waitAllResults(String originatorName) throws TaskException {
        if (originatorName == null) {
            if (debug) {
                if (originatorName == null) {
                    logger.debug("All results received by the main client.");
                } else {
                    logger.debug("All results received by " + originatorName);
                }

            }

            return resultQueue.getAll();
        } else {
            if (subResultQueues.containsKey(originatorName)) {
                return subResultQueues.get(originatorName).getAll();

            } else
                throw new IllegalArgumentException("Unknown originator: " + originatorName);

        }
    }

    /** {@inheritDoc} */
    public List<ResultIntern<Serializable>> waitKResults(final String originatorName, final int k)
            throws TaskException {
        if (originatorName == null) {

            if ((resultQueue.countPendingResults() + resultQueue.countAvailableResults()) < k) {
                throw new IllegalArgumentException("" + k + " is too big");
            } else if (k <= 0) {
                throw new IllegalArgumentException("Wrong value : " + k);
            }

            if (debug) {
                logger.debug("" + k + " results received by the main client.");

            }

            return resultQueue.getNextK(k);
        } else {
            if (subResultQueues.containsKey(originatorName)) {
                ResultQueue rq = subResultQueues.get(originatorName);
                if ((rq.countPendingResults() + rq.countAvailableResults()) < k) {
                    throw new IllegalArgumentException("" + k + " is too big");
                } else if (k <= 0) {
                    throw new IllegalArgumentException("Wrong value : " + k);
                }

                if (debug) {
                    logger.debug("" + k + " results received by " + originatorName);
                }

                return rq.getNextK(k);
            } else
                throw new IllegalArgumentException("Unknown originator: " + originatorName);

        }
    }

    /** {@inheritDoc} */
    public ResultIntern<Serializable> waitOneResult(String originatorName) throws TaskException {
        if (originatorName == null) {

            ResultIntern<Serializable> res = resultQueue.getNext();

            if (debug) {
                logger.debug("Result of task " + res.getId() + " received by the main client.");
            }

            return res;
        } else {
            if (subResultQueues.containsKey(originatorName)) {
                ResultIntern<Serializable> res = subResultQueues.get(originatorName).getNext();

                if (debug) {
                    logger.debug("Result of task " + res.getId() + " received by " + originatorName);
                }

                return res;
            } else
                throw new IllegalArgumentException("Unknown originator: " + originatorName);

        }
    }

    /**
     * @author The ProActive Team
     *         Internal class for filtering requests in the queue
     */
    private class FindWaitFilter implements RequestFilter {

        /** Creates a filter */
        public FindWaitFilter() {
        }

        /** {@inheritDoc} */
        public boolean acceptRequest(final Request request) {
            // We find all the requests that are not servable yet
            String name = request.getMethodName();
            return name.equals("waitOneResult") || name.equals("waitAllResults") ||
                name.equals("waitKResults");
        }
    }

    /**
     * @author The ProActive Team
     *         Internal class for filtering requests in the queue
     */
    private class FindNotWaitAndTerminateFilter implements RequestFilter {

        /** Creates the filter */
        public FindNotWaitAndTerminateFilter() {
        }

        /** {@inheritDoc} */
        public boolean acceptRequest(final Request request) {
            // We find all the requests which can't be served yet
            String name = request.getMethodName();
            return !name.equals("waitOneResult") && !name.equals("waitAllResults") &&
                !name.equals("waitKResults") && !name.equals("terminateIntern");
        }
    }

}
