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
import org.objectweb.proactive.extensions.masterworker.interfaces.MemoryFactory;
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

    /** How many tasks do we initially send to each worker */
    private int initial_task_flooding = Master.DEFAULT_TASK_FLOODING;

    // Global variables

    /** stub on this active object */
    private AOMaster stubOnThis;

    /** is the master terminated */
    private boolean terminated;

    /** is the master in the process of clearing all activity ? **/
    private boolean isClearing;

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
    private MemoryFactory memoryFactory;

    /** Stub to group of sleeping workers */
    private Worker sleepingGroupStub;

    /** Group of sleeping workers */
    private Group<Worker> sleepingGroup;

    /** Group of cleared workers */
    private Set<Worker> clearedWorkers;

    /** Names of workers which have been spawned **/
    private Set<String> spawnedWorkerNames;
    private HashMap<String, HashSet<String>> workersDependencies;
    private HashMap<String, String> workersDependenciesRev;

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

    /** Filters **/
    private final FindNotWaitAndTerminateFilter notWaitOrTerminateFilter = new FindNotWaitAndTerminateFilter();
    private final FindWaitFilter findWaitFilter = new FindWaitFilter();
    private final IsClearingFilter clearingFilter = new IsClearingFilter();

    /** Proactive empty no arg constructor */
    @Deprecated
    public AOMaster() {
        /* do nothing */
    }

    /**
     * Creates the master with the initial memory of the workers
     *
     * @param memoryFactory factory which will create memory for each new workers
     * @param masterDescriptorURL descriptor used to deploy the master (if any)
     * @param applicationUsed     GCMapplication used to deploy the master (if any)
     * @param masterVNNAme        VN Name of the master (if any)
     */
    public AOMaster(final MemoryFactory memoryFactory, final URL masterDescriptorURL,
            final GCMApplication applicationUsed, final String masterVNNAme) {
        this.memoryFactory = memoryFactory;
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
                // If the worker requests a flooding this means that its penqing queue is empty,
                // thus, it will sleep
                if (flooding) {
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
                long tid = taskId.getID();
                launchedTasks.put(tid, taskId.getOriginator());
                // We record the worker activity
                if (workersActivity.containsKey(workerName)) {
                    Set<Long> wact = workersActivity.get(workerName);
                    wact.add(tid);
                } else {
                    Set<Long> wact = new HashSet<Long>();
                    wact.add(tid);
                    workersActivity.put(workerName, wact);
                }
                TaskIntern<Serializable> taskfuture = (TaskIntern<Serializable>) repository.getTask(taskId
                        .getID());
                TaskIntern<Serializable> realTask = (TaskIntern<Serializable>) PAFuture
                        .getFutureValue(taskfuture);
                repository.saveTask(tid);
                tasksToDo.offer(realTask);
                if (debug) {
                    logger.debug("Task " + tid + " given to " + workerName);
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
        isClearing = false;
        // Queues
        pendingTasks = new TaskQueue();
        launchedTasks = new HashMap<Long, String>();
        resultQueue = new ResultQueue<Serializable>(Master.COMPLETION_ORDER);
        pendingSubRequests = new HashMap<String, Request>();
        subResultQueues = new HashMap<String, ResultQueue<Serializable>>();
        clearedWorkers = new HashSet<Worker>();
        spawnedWorkerNames = new HashSet<String>();
        workersDependencies = new HashMap<String, HashSet<String>>();
        workersDependenciesRev = new HashMap<String, String>();

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
                    new Object[] { stubOnThis, memoryFactory, masterDescriptorURL, applicationUsed,
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
                if (clearedWorkers.contains(worker)) {
                    clearedWorkers.remove(worker);
                }

                // Among our "dictionary of workers", we remove only entries in the reverse dictionary,
                // By doing that, if ever the worker appears not completely dead and reappears, we can handle it
                workersByName.remove(workerName);
                // We remove the activity of this worker and every children workers
                removeActivityOfWorker(workerName);
                if (workersDependencies.containsKey(workerName)) {
                    HashSet<String> childrenWorkers = workersDependencies.get(workerName);
                    for (String childWorkerName : childrenWorkers) {
                        removeActivityOfWorker(childWorkerName);
                        workersDependenciesRev.remove(childWorkerName);
                    }
                    childrenWorkers.clear();
                    workersDependencies.remove(workerName);

                }
                smanager.isDead(workerName);
            }
            return true;
        }
        return false;

    }

    private void removeActivityOfWorker(String workerName) {
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
    }

    public void isCleared(Worker worker) {
        if (debug) {
            String workerName = workersByNameRev.get(worker);
            logger.debug(workerName + " is cleared");
        }

        clearedWorkers.add(worker);
    }

    /** {@inheritDoc} */
    public boolean isDead(final String workerName) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public boolean isEmpty(String originatorName) throws IsClearingException {
        if (originatorName == null) {
            return (resultQueue.isEmpty() && pendingTasks.isEmpty());
        } else {
            if (isClearing) {
                clearingCallFromSpawnedWorker(originatorName);
            }
            if (subResultQueues.containsKey(originatorName)) {
                return (subResultQueues.get(originatorName).isEmpty() && !pendingTasks
                        .hasTasksByOriginator(originatorName));
            } else {
                throw new IllegalArgumentException("Unknown originator " + originatorName);
            }
        }
    }

    public int countPending(String originatorName) throws IsClearingException {
        if (originatorName == null) {
            return resultQueue.countPendingResults();
        } else {
            if (isClearing) {
                clearingCallFromSpawnedWorker(originatorName);
            }
            if (subResultQueues.containsKey(originatorName)) {
                return (subResultQueues.get(originatorName).countPendingResults());
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
            if (!service.hasRequestToServe()) {
                service.waitForRequest();
            }

            // Serving methods other than waitXXX
            while (service.hasRequestToServe(notWaitOrTerminateFilter)) {
                service.serveOldest(notWaitOrTerminateFilter);
                if (isClearing == true) {
                    break;
                }
            }
            // If a clear request is detected we enter a special mode
            if (isClearing) {
                clearingRunActivity(service);
                continue;
            }

            // We detect all waitXXX requests in the request queue
            Request waitRequest = service.getOldest(findWaitFilter);
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
                service.blockingRemoveOldest(findWaitFilter);
                waitRequest = service.getOldest(findWaitFilter);

            }

            // we maybe serve the pending waitXXX methods if there are some and if the necessary results are collected
            maybeServePending();
            service.serveAll("finalTerminate");
            service.serveAll("awaitsTermination");
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

    private void clearingRunActivity(Service service) {

        // To prevent concurrent modification exception, as the servePending method modifies the pendingSubRequests collection
        Set<String> newSet = new HashSet<String>(pendingSubRequests.keySet());
        // We first serve the pending sub requests
        for (String originator : newSet) {
            servePending(originator);
        }

        while (isClearing) {

            if (service.hasRequestToServe(clearingFilter)) {
                service.serveOldest(clearingFilter);
            }
            if (clearedWorkers.size() == workerGroup.size() + spawnedWorkerNames.size()) {
                sleepingGroup.addAll(clearedWorkers);
                isClearing = false;
                clearedWorkers.clear();
                break;
            }
            // ugly sleep but the service.waitForRequest() would return immediately here provided there are other requests than those of the filter
            // Besides that, performance is not mandatory in this mode
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (debug) {
            logger.debug("Master cleared");
        }
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

    /** {@inheritDoc} */
    public BooleanWrapper sendResult(ResultIntern<Serializable> result, String originatorName) {
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
                if (wact.size() == 0) {
                    workersActivity.remove(originatorName);
                }
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

        if (spawnedWorkerNames.contains(originatorName)) {
            // We remove the spawned worker from our knowledge
            spawnedWorkerNames.remove(originatorName);
            String parentWorker = workersDependenciesRev.remove(originatorName);
            workersDependencies.get(parentWorker).remove(originatorName);
        }

        return new BooleanWrapper(true);
    }

    /** {@inheritDoc} */
    public BooleanWrapper sendResults(List<ResultIntern<Serializable>> results, String workerName) {
        for (ResultIntern<Serializable> res : results) {
            sendResult(res, workerName);
        }
        return new BooleanWrapper(true);
    }

    /** {@inheritDoc} */
    public Queue<TaskIntern<Serializable>> sendResultsAndGetTasks(List<ResultIntern<Serializable>> results,
            String workerName, boolean reflooding) {
        sendResults(results, workerName);
        // if the worker has already reported dead, we need to handle that it suddenly reappears
        Worker worker = workersByName.get(workerName);
        if (!workersByNameRev.containsKey(worker)) {
            // We do this by removing the worker from our database, which will trigger that it will be recorded again
            workersByName.remove(workerName);
        }
        return getTasksInternal(worker, workerName, reflooding);
    }

    /** {@inheritDoc} */
    public BooleanWrapper forwardedTask(Long taskId, String oldWorkerName, String newWorkerName) {

        if (debug) {
            logger.debug(oldWorkerName + " forwarded Task " + taskId + " to " + newWorkerName);
        }
        Set<Long> wact = workersActivity.get(oldWorkerName);
        wact.remove(taskId);
        if (wact.size() == 0) {
            workersActivity.remove(oldWorkerName);
        }
        HashSet<Long> newSet = new HashSet<Long>();
        newSet.add(taskId);
        workersActivity.put(newWorkerName, newSet);
        spawnedWorkerNames.add(newWorkerName);
        // We record the dependency between the old worker and the new worker (for FT purpose)
        if (workersDependencies.containsKey(oldWorkerName)) {
            workersDependencies.get(oldWorkerName).add(newWorkerName);
        } else {
            HashSet<String> dependency = new HashSet<String>();
            workersDependencies.put(oldWorkerName, dependency);
        }
        workersDependenciesRev.put(newWorkerName, oldWorkerName);
        return new BooleanWrapper(true);
    }

    /** If there is a pending waitXXX method, we serve it if the necessary results are collected */
    private void maybeServePending() {
        // To prevent concurrent modification exception, as the servePending method modifies the pendingSubRequests collection
        Set<Map.Entry<String, Request>> newSet = new HashSet<Map.Entry<String, Request>>(pendingSubRequests
                .entrySet());
        for (Map.Entry<String, Request> ent : newSet) {
            Request req = ent.getValue();
            String originator = ent.getKey();
            String methodName = req.getMethodName();
            ResultQueue rq = subResultQueues.get(originator);
            if (methodName.equals("waitOneResult") && rq.isOneResultAvailable()) {
                servePending(originator);
            } else if (methodName.equals("waitAllResults") && rq.areAllResultsAvailable()) {
                servePending(originator);
            } else if (methodName.equals("waitKResults")) {
                int k = (Integer) req.getParameter(1);
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

        // Clearing the master is a quite complicated mechanism
        // It is not possible to wait synchronously for every workers'reply because workers might be requesting something from the master at the same time
        // therefore the clearing process must be first initiated, a message sent to every workers, and then the master will enter a mode "clearing"
        // where every call from the workers will be served immediately by an exception, excepting the acknowledgement of the clear message.
        // When every workers have answered the master will be declared "cleared" and it can starts its normal serving 

        if (debug) {
            logger.debug("Master is clearing...");
        }
        // We clear the queues
        resultQueue.clear();
        pendingTasks.clear();
        launchedTasks.clear();
        for (ResultQueue<Serializable> queue : subResultQueues.values()) {
            queue.clear();
        }
        subResultQueues.clear();
        // We clear the workers activity memory
        workersActivity.clear();
        // We tell all the worker to clear their pending tasks
        workerGroupStub.clear();
        // We clear every sleeping workers registered
        sleepingGroup.clear();
        // We clear the repository
        repository.clear();
        isClearing = true;
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
        if (debug) {
            if (originator == null) {
                logger.debug("Request for solving task " + taskId + " from main client");
            } else {
                logger.debug("Request for solving task " + taskId + " from " + originator);
            }
        }
        // If we have sleepers
        if (pendingTasks.size() == 0 && sleepingGroup.size() > 0) {
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

    /** {@inheritDoc} */
    public void solveIntern(String originatorName, List<? extends Task<? extends Serializable>> tasks)
            throws IsClearingException {
        if (isClearing) {
            clearingCallFromSpawnedWorker(originatorName);
        }
        List<Long> wrappers = createIds(tasks);
        solveIds(wrappers, originatorName);
    }

    /** {@inheritDoc} */
    public void setResultReceptionOrder(final String originatorName, final SubMaster.OrderingMode mode)
            throws IsClearingException {
        if (originatorName == null) {
            resultQueue.setMode(mode);
        } else {
            if (isClearing) {
                clearingCallFromSpawnedWorker(originatorName);
            }
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
     * When the master is clearing
     * Throws an exception to workers waiting for an answer from the master
     * @param originator worker waiting
     * @throws IsClearingException to notify that it's clearing
     */
    private void clearingCallFromSpawnedWorker(String originator) throws IsClearingException {
        if (debug) {
            logger.debug(originator + " is cleared");
        }
        workersActivity.remove(originator);
        spawnedWorkerNames.remove(originator);
        throw new IsClearingException();
    }

    /**
     * Synchronous version of terminate
     *
     * @param freeResources do we free as well deployed resources
     * @return true if completed successfully
     */
    public BooleanWrapper terminateIntern(final boolean freeResources) {

        if (debug) {
            logger.debug("Terminating Master...");
        }

        // The cleaner way is to first clearing the activity
        stubOnThis.clear();

        // then cleaning all instances
        stubOnThis.finalTerminate(freeResources);

        return new BooleanWrapper(true);

    }

    public boolean awaitsTermination() {
        return true;
    }

    protected BooleanWrapper finalTerminate(final boolean freeResources) {

        // We empty pending queues
        pendingTasks.clear();

        // we empty groups
        workerGroup.purgeExceptionAndNull();
        workerGroup.clear();
        workerGroupStub = null;
        sleepingGroup.purgeExceptionAndNull();
        sleepingGroup.clear();
        sleepingGroupStub = null;

        clearedWorkers.clear();
        pendingRequest = null;

        // We terminate the pinger
        PAFuture.waitFor(pinger.terminate());
        pinger = null;
        // We terminate the worker manager
        PAFuture.waitFor(smanager.terminate(freeResources));
        smanager = null;
        // We terminate the repository
        repository.terminate();
        repository = null;

        launchedTasks.clear();

        workersActivity.clear();
        workersByName.clear();
        workersByNameRev.clear();

        stubOnThis = null;

        terminated = true;
        return new BooleanWrapper(true);
    }

    /** {@inheritDoc} */
    public List<ResultIntern<Serializable>> waitAllResults(String originatorName) throws TaskException {
        if (originatorName == null) {
            if (debug) {
                logger.debug("All results received by the main client.");
            }

            return resultQueue.getAll();
        } else {
            if (isClearing) {
                clearingCallFromSpawnedWorker(originatorName);
            }
            if (debug) {
                logger.debug("All results received by " + originatorName);
            }
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
            if (isClearing) {
                clearingCallFromSpawnedWorker(originatorName);
            }
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
            if (isClearing) {
                clearingCallFromSpawnedWorker(originatorName);
            }
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
                !name.equals("waitKResults") && !name.equals("finalTerminate") &&
                !name.equals("awaitsTermination");
        }
    }

    private class IsClearingFilter implements RequestFilter {

        public IsClearingFilter() {

        }

        public boolean acceptRequest(Request request) {
            // We serve with an exception every request coming from workers (task requesting, results sending, result waiting), we serve nicely the isCleared request, finally, we serve as well the isDead notification coming from the pinger
            String name = request.getMethodName();
            if (name.equals("solveIntern") || name.equals("waitOneResult") || name.equals("waitAllResults") ||
                name.equals("waitKResults") || name.equals("isEmpty") ||
                name.equals("setResultReceptionOrder") || name.equals("countPending") ||
                name.equals("countAvailableResults")) {
                return request.getParameter(0) != null;
            }
            return (name.equals("isCleared") || name.equals("isDead") || name.equals("sendResult") ||
                name.equals("sendResultAndGetTasks") || name.equals("getTasks")) ||
                name.equals("forwardedTask");

        }
    }

}
