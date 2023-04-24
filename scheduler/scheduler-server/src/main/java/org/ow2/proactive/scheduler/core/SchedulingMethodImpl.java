/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.core;

import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyDisabledException;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxy;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxyCreationException;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.Policy;
import org.ow2.proactive.scheduler.signal.SignalApiImpl;
import org.ow2.proactive.scheduler.synchronization.SynchronizationWrapper;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.internal.TaskRecoveryData;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.MultipleTimingLogger;
import org.ow2.proactive.scheduler.util.SchedulerStarter;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.threading.TimeoutThreadPoolExecutor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.proactive.utils.TaskIdWrapper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;


/**
 * SchedulingMethodImpl is the default implementation for the scheduling process
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public final class SchedulingMethodImpl implements SchedulingMethod {

    /** Scheduler logger */
    public static final Logger logger = Logger.getLogger(SchedulingMethodImpl.class);

    public static final TaskLogger tlogger = TaskLogger.getInstance();

    public static final JobLogger jlogger = JobLogger.getInstance();

    /** Number of time to retry an active object creation if it fails to create */
    protected static final int ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER = 3;

    /** Maximum blocking time for the do task action */
    protected int dotaskActionTimeout;

    protected int activeObjectCreationRetryTimeNumber;

    protected final SchedulingService schedulingService;

    protected TimeoutThreadPoolExecutor threadPool;

    protected PrivateKey corePrivateKey;

    private TaskTerminateNotification terminateNotification;

    private String terminateNotificationNodeURL;

    private CheckEligibleTaskDescriptorScript checkEligibleTaskDescriptorScript;

    private MultipleTimingLogger schedulingMainLoopTimingLogger;

    // a cache which stores temporarily sessionids to improve performance
    private static Cache<String, String> sessionidCache = CacheBuilder.newBuilder()
                                                                      .expireAfterWrite(PASchedulerProperties.SCHEDULER_METHOD_SESSION_CACHE_EXPIRATION.getValueAsInt(),
                                                                                        TimeUnit.SECONDS)
                                                                      .build();

    public SchedulingMethodImpl(SchedulingService schedulingService) throws Exception {
        this.schedulingService = schedulingService;
        this.checkEligibleTaskDescriptorScript = new CheckEligibleTaskDescriptorScript(this);
        terminateNotification = new TerminateNotification(schedulingService);
        Node terminateNotificationNode = NodeFactory.createLocalNode("taskTerminationNode",
                                                                     true,
                                                                     "taskTerminationVNode");
        terminateNotification = PAActiveObject.turnActive(terminateNotification,
                                                          TaskTerminateNotification.class.getName(),
                                                          terminateNotificationNode);

        terminateNotificationNodeURL = PAActiveObject.getActiveObjectNode(terminateNotification)
                                                     .getNodeInformation()
                                                     .getURL();

        this.threadPool = TimeoutThreadPoolExecutor.newCachedThreadPool(PASchedulerProperties.SCHEDULER_STARTTASK_THREADNUMBER.getValueAsInt(),
                                                                        new NamedThreadFactory("DoTask_Action",
                                                                                               true,
                                                                                               7));
        this.corePrivateKey = Credentials.getPrivateKey(PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_PRIVKEY_PATH.getValueAsString()));
    }

    RMProxiesManager getRMProxiesManager() {
        return schedulingService.getInfrastructure().getRMProxiesManager();
    }

    private void releaseNodes(InternalJob job, NodeSet nodeSet) throws RMProxyCreationException {
        getRMProxiesManager().getUserRMProxy(job.getOwner(), job.getCredentials()).releaseNodes(nodeSet);
    }

    /**
     * Scheduling process. For this implementation, steps are :<br>
     * <ul>
     * 	<li>Select running and pending jobs to be scheduled
     * 	<li>Get an ordered list of the selected tasks to be scheduled
     * 	<li>While returned tasks list is not empty :
     * 		<ul>
     * 			<li>Get n first compatible tasks (same selection script, same node exclusion)
     * 			<li>Ask nodes to RM according to the previous specification
     * 			<li>Try to start each tasks
     * 			<li>Job started event if needed
     * 			<li>Task started event
     * 		</ul>
     * 	<li>Manage exception while deploying tasks on nodes
     * </ul>
     *
     * @return the number of tasks that have been started
     */
    public int schedule() {
        schedulingMainLoopTimingLogger = new MultipleTimingLogger("SchedulingMainLoopTiming", logger);

        schedulingMainLoopTimingLogger.start("schedule");

        Policy currentPolicy = schedulingService.getPolicy();
        currentPolicy.setSchedulingService(schedulingService);

        //Number of time to retry an active object creation before leaving scheduling loop
        activeObjectCreationRetryTimeNumber = ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER;

        //get job Descriptor list with eligible jobs (running and pending)
        Map<JobId, JobDescriptor> jobMap = schedulingService.lockJobsToSchedule();

        Map<JobId, JobDescriptor> toUnlock = jobMap;

        logSelectedJobs(jobMap);

        // If there are some jobs which could not be locked it is not possible to do any priority scheduling decision,
        // we wait for next scheduling loop and don't start any task
        if (jobMap.isEmpty()) {
            updateNeededNodes();
            return 0;
        }

        int tasksStarted = startTasks(currentPolicy, jobMap, toUnlock);
        schedulingMainLoopTimingLogger.end("schedule");

        if (tasksStarted > 0) {
            schedulingMainLoopTimingLogger.printTimings(Level.INFO);
        }

        return tasksStarted;
    }

    private void updateNeededNodes() {
        updateNeededNodes(0);
    }

    private int computeNeededNodes(Collection<? extends TaskDescriptor> eligibleByPolicyTasks) {
        // Needed nodes
        return eligibleByPolicyTasks.stream().mapToInt(TaskDescriptor::getNumberOfNodesNeeded).sum();
    }

    private void updateNeededNodes(int neededNodes) {
        // for statistics used in RM portal
        getRMProxiesManager().getRmProxy().setNeededNodes(neededNodes);

        // for statistics used in Scheduling portal
        schedulingService.getListener().updateNeededNodes(neededNodes);

    }

    private int startTasks(Policy currentPolicy, Map<JobId, JobDescriptor> jobMap, Map<JobId, JobDescriptor> toUnlock) {
        try {
            List<JobDescriptor> descriptors = new ArrayList<>(jobMap.values());

            schedulingMainLoopTimingLogger.start("getFreeResources");
            //get rmState and update it in scheduling policy
            Set<String> freeResources = null;
            if (PASchedulerProperties.SCHEDULER_POLCY_STRICT_FIFO.getValueAsBoolean()) {
                freeResources = getFreeResources(currentPolicy);
            }

            schedulingMainLoopTimingLogger.end("getFreeResources");

            schedulingMainLoopTimingLogger.start("getOrderedTasks");
            // ask the policy all the tasks to be schedule according to the jobs list.
            LinkedList<EligibleTaskDescriptor> fullListOfTaskRetrievedFromPolicy = currentPolicy.getOrderedTasks(descriptors);

            setPendingStatusesToAllEligibleTasks(fullListOfTaskRetrievedFromPolicy);

            //if there is no free resources, stop it right now without starting any task
            if (PASchedulerProperties.SCHEDULER_POLCY_STRICT_FIFO.getValueAsBoolean() && freeResources.isEmpty()) {

                updateNeededNodes(computeNeededNodes(fullListOfTaskRetrievedFromPolicy));
                return 0;
            }

            schedulingMainLoopTimingLogger.end("getOrderedTasks");

            //if there is no task to scheduled, return without starting any task
            if (fullListOfTaskRetrievedFromPolicy == null || fullListOfTaskRetrievedFromPolicy.isEmpty()) {
                updateNeededNodes();
                return 0;
            }

            schedulingMainLoopTimingLogger.start("unlockResources");
            toUnlock = unlockResources(toUnlock);
            schedulingMainLoopTimingLogger.end("unlockResources");

            return getNumberOfTaskStarted(currentPolicy, jobMap, freeResources, fullListOfTaskRetrievedFromPolicy);
        } finally {
            if (toUnlock != null) {
                schedulingService.unlockJobsToSchedule(toUnlock.values());
            }
        }
    }

    private void setPendingStatusesToAllEligibleTasks(List<EligibleTaskDescriptor> eligibleTasks) {
        List<EligibleTaskDescriptorImpl> notPendingYet = eligibleTasks.stream()
                                                                      .map(task -> (EligibleTaskDescriptorImpl) task)
                                                                      .filter(task -> !task.getInternal()
                                                                                           .getStatus()
                                                                                           .equals(TaskStatus.PENDING))
                                                                      .collect(Collectors.toList());

        notPendingYet.forEach(task -> {
            if (task.getInternal().getScheduledTime() == -1) {
                task.getInternal().setScheduledTime(System.currentTimeMillis());
            }
            task.getInternal().setStatus(TaskStatus.PENDING);
        });

        notPendingYet.forEach(task -> {
            getDBManager().updateTaskStatusAndScheduledTime(task,
                                                            TaskStatus.PENDING,
                                                            task.getInternal().getScheduledTime());
        });

    }

    private int getNumberOfTaskStarted(Policy currentPolicy, Map<JobId, JobDescriptor> jobMap,
            Set<String> freeResources, LinkedList<EligibleTaskDescriptor> fullListOfTaskRetrievedFromPolicy) {
        return selectAndStartTasks(currentPolicy, jobMap, freeResources, fullListOfTaskRetrievedFromPolicy);

    }

    private Map<JobId, JobDescriptor> unlockResources(Map<JobId, JobDescriptor> toUnlock) {
        schedulingService.unlockJobsToSchedule(toUnlock.values());
        toUnlock = null;
        return toUnlock;
    }

    private Set<String> getFreeResources(Policy currentPolicy) {
        RMState rmState = getRMProxiesManager().getRmProxy().getState();
        currentPolicy.setRMState(rmState);
        Set<String> freeResources = rmState.getFreeNodes();
        if (logger.isDebugEnabled()) {
            logger.debug("eligible nodes : " + (freeResources.size() < 5 ? freeResources : freeResources.size()));
        }
        return freeResources;
    }

    private void logSelectedJobs(Map<JobId, JobDescriptor> jobMap) {
        if (logger.isTraceEnabled() && jobMap == null || jobMap.isEmpty()) {
            logger.trace("No jobs selected to be scheduled");
        }
        if (logger.isDebugEnabled() && jobMap != null && !jobMap.isEmpty()) {
            logger.debug("jobs selected to be scheduled : " + (jobMap.size() < 5 ? jobMap : jobMap.size()));
        }
    }

    private int selectAndStartTasks(Policy currentPolicy, Map<JobId, JobDescriptor> jobMap, Set<String> freeResources,
            LinkedList<EligibleTaskDescriptor> tasksRetrievedFromPolicy) {
        int numberOfTaskStarted = 0;

        schedulingMainLoopTimingLogger.start("updateVariablesForTasksToSchedule");

        int neededNodes = computeNeededNodes(tasksRetrievedFromPolicy);

        if (logger.isDebugEnabled()) {
            loggingEligibleTasksDetails(tasksRetrievedFromPolicy);
        }

        updateVariablesForTasksToSchedule(tasksRetrievedFromPolicy);

        schedulingMainLoopTimingLogger.end("updateVariablesForTasksToSchedule");

        schedulingMainLoopTimingLogger.start("loadAndInit");

        for (Iterator<EligibleTaskDescriptor> iterator = tasksRetrievedFromPolicy.iterator(); iterator.hasNext();) {
            EligibleTaskDescriptorImpl taskDescriptor = (EligibleTaskDescriptorImpl) iterator.next();
            // load and Initialize the executable container
            InternalTask internalTask = taskDescriptor.getInternal();
            try {
                loadAndInit(internalTask);
            } catch (Exception e) {
                handleLoadExecutableContainerError(internalTask, iterator, e);
            }
        }

        schedulingMainLoopTimingLogger.end("loadAndInit");

        while (!tasksRetrievedFromPolicy.isEmpty() &&
               (!PASchedulerProperties.SCHEDULER_POLCY_STRICT_FIFO.getValueAsBoolean() || !freeResources.isEmpty())) {

            //get the next compatible tasks from the whole returned policy tasks
            LinkedList<EligibleTaskDescriptor> tasksToSchedule = new LinkedList<>();
            int neededResourcesNumber = 0;

            schedulingMainLoopTimingLogger.start("getNextCompatibleTasks");

            while (!tasksRetrievedFromPolicy.isEmpty() && neededResourcesNumber == 0) {
                //the loop will search for next compatible task until it find something
                neededResourcesNumber = getNextcompatibleTasks(jobMap, tasksRetrievedFromPolicy, tasksToSchedule);
            }

            schedulingMainLoopTimingLogger.end("getNextCompatibleTasks");

            if (logger.isDebugEnabled()) {
                logger.debug("tasksToSchedule : " + tasksToSchedule);
            }

            logger.debug("required number of nodes : " + neededResourcesNumber);
            if (neededResourcesNumber == 0 || tasksToSchedule.isEmpty()) {
                break;
            }

            schedulingMainLoopTimingLogger.start("getRMNodes");
            NodeSet nodeSet = getRMNodes(jobMap, neededResourcesNumber, tasksToSchedule, freeResources);
            schedulingMainLoopTimingLogger.end("getRMNodes");

            if (PASchedulerProperties.SCHEDULER_POLCY_STRICT_FIFO.getValueAsBoolean() && nodeSet != null) {
                freeResources.removeAll(nodeSet.getAllNodesUrls());
            }

            Map<String, Boolean> recoverableStatus = getRMProxiesManager().getRmProxy().areNodesRecoverable(nodeSet);

            //start selected tasks
            schedulingMainLoopTimingLogger.start("createExecutions");
            List<CreateExecutionInfo> executionFutures = new ArrayList<>();
            while (nodeSet != null && !nodeSet.isEmpty()) {
                EligibleTaskDescriptor taskDescriptor = tasksToSchedule.removeFirst();
                InternalJob currentJob = ((JobDescriptorImpl) jobMap.get(taskDescriptor.getJobId())).getInternal();
                InternalTask internalTask = ((EligibleTaskDescriptorImpl) taskDescriptor).getInternal();

                if (currentPolicy.isTaskExecutable(nodeSet, taskDescriptor)) {
                    //create launcher and try to start the task
                    if (nodeSet.size() >= internalTask.getNumberOfNodesNeeded() &&
                        (internalTask.getStatus() != TaskStatus.PAUSED)) {
                        Node node = nodeSet.remove(0);
                        NodeSet taskExtraNodes;
                        if (internalTask.isParallel()) {
                            taskExtraNodes = new NodeSet(nodeSet);
                            nodeSet.clear();
                        } else {
                            taskExtraNodes = new NodeSet();
                        }
                        final Node finalNode = node;
                        final InternalJob finalJob = currentJob;
                        final NodeSet allNodes = new NodeSet(taskExtraNodes);
                        allNodes.add(node);

                        executionFutures.add(new CreateExecutionInfo(threadPool.submit(() -> createExecution(taskExtraNodes,
                                                                                                             finalNode,
                                                                                                             finalJob,
                                                                                                             internalTask,
                                                                                                             taskDescriptor,
                                                                                                             recoverableStatus,
                                                                                                             terminateNotificationNodeURL)),
                                                                     currentJob,
                                                                     internalTask,
                                                                     taskDescriptor,
                                                                     allNodes));
                    }

                }

                //if every task that should be launched have been removed
                if (tasksToSchedule.isEmpty()) {
                    //get back unused nodes to the RManager
                    if (!nodeSet.isEmpty()) {
                        schedulingMainLoopTimingLogger.start("releaseNodes");
                        try {
                            releaseNodes(currentJob, nodeSet);
                            if (PASchedulerProperties.SCHEDULER_POLCY_STRICT_FIFO.getValueAsBoolean()) {
                                freeResources.addAll(nodeSet.getAllNodesUrls());
                            }
                        } catch (Exception e) {
                            logger.warn("Unable to get back the nodeSet to the RM", e);
                        }

                        schedulingMainLoopTimingLogger.end("releaseNodes");
                    }
                    //and leave the loop
                    break;
                }
            }

            for (CreateExecutionInfo executionInfo : executionFutures) {
                try {
                    if (executionInfo.get()) {
                        neededNodes -= executionInfo.getTaskDescriptor().getNumberOfNodesNeeded();
                        numberOfTaskStarted++;
                    } else {
                        releaseNodes(freeResources, executionInfo);
                    }
                } catch (Exception e) {
                    //if we are here, it is that something happened while launching the current task.
                    logger.warn(String.format("An error occurred while starting task id=%s name=%s",
                                              executionInfo.getTask().getId(),
                                              executionInfo.getTask().getName()),
                                e);
                    //so try to get back every nodes used by the task to the resource manager
                    releaseNodes(freeResources, executionInfo);
                }
            }
            schedulingMainLoopTimingLogger.end("createExecutions");

        }

        // number of nodes needed to start all pending tasks
        updateNeededNodes(neededNodes);

        return numberOfTaskStarted;
    }

    private void releaseNodes(Set<String> freeResources, CreateExecutionInfo executionInfo) {
        try {
            releaseNodes(executionInfo.getJob(), executionInfo.getNodes());
            if (PASchedulerProperties.SCHEDULER_POLCY_STRICT_FIFO.getValueAsBoolean()) {
                freeResources.addAll(executionInfo.getNodes().getAllNodesUrls());
            }
        } catch (Exception e2) {
            logger.warn("Unable to get back the nodeSet to the RM", e2);
        }
    }

    private void loggingEligibleTasksDetails(LinkedList<EligibleTaskDescriptor> taskRetrievedFromPolicy) {
        logger.debug("list of eligible tasks: " +
                     (taskRetrievedFromPolicy.size() < 5 ? taskRetrievedFromPolicy : taskRetrievedFromPolicy.size()));
    }

    /**
     * Extract the next compatible tasks from the eligible task list,
     *
     * Two tasks are compatible according to several criteria such as the same list of selection script,
     * the same node tokens, or node exclusions.
     * The check of compliance is currently done by the {@link SchedulingTaskComparator} class.<br>
     * This method has two side effects : extracted tasks are removed from the bagOfTasks and put in the toFill list
     *
     * @param bagOfTasks the list of tasks form which to extract tasks
     * @param toFill the list that will contains the task to schedule at the end. This list must not be null but must be empty.<br>
     * 		  this list will be filled with the first compatible tasks
     *
     * @return the number of nodes needed to start every task present in the 'toFill' argument at the end of the method.
     */
    protected int getNextcompatibleTasks(Map<JobId, JobDescriptor> jobsMap,
            LinkedList<EligibleTaskDescriptor> bagOfTasks, LinkedList<EligibleTaskDescriptor> toFill) {
        if (toFill == null || bagOfTasks == null) {
            throw new IllegalArgumentException("The two given lists must not be null !");
        }
        int totalNeededNodes = 0;
        if (PASchedulerProperties.JETTY_STARTED.isSet() && !PASchedulerProperties.JETTY_STARTED.getValueAsBoolean()) {
            Iterator<EligibleTaskDescriptor> it = bagOfTasks.iterator();
            EligibleTaskDescriptor etd;
            while (it.hasNext()) {
                etd = it.next();
                InternalJob internalJob = ((JobDescriptorImpl) jobsMap.get(etd.getJobId())).getInternal();
                if (checkEligibleTaskDescriptorScript.isTaskContainsAPIBinding(etd, internalJob)) {
                    //skip task here
                    it.remove();
                }
            }
        }
        if (!bagOfTasks.isEmpty()) {
            EligibleTaskDescriptor etd = bagOfTasks.removeFirst();
            ((EligibleTaskDescriptorImpl) etd).addAttempt();
            InternalJob currentJob = ((JobDescriptorImpl) jobsMap.get(etd.getJobId())).getInternal();
            InternalTask internalTask = currentJob.getIHMTasks().get(etd.getTaskId());
            internalTask.updateVariables(schedulingService);
            int neededNodes = internalTask.getNumberOfNodesNeeded();
            String sessionid = getSessionid(currentJob);
            SchedulingTaskComparator referent = new SchedulingTaskComparator(internalTask, currentJob, sessionid);
            boolean firstLoop = true;
            do {
                if (!firstLoop) {
                    //if bagOfTasks is not empty
                    if (!bagOfTasks.isEmpty()) {
                        etd = bagOfTasks.removeFirst();
                        ((EligibleTaskDescriptorImpl) etd).addAttempt();
                        currentJob = ((JobDescriptorImpl) jobsMap.get(etd.getJobId())).getInternal();
                        internalTask = currentJob.getIHMTasks().get(etd.getTaskId());
                        internalTask.updateVariables(schedulingService);
                        neededNodes = internalTask.getNumberOfNodesNeeded();
                    }
                } else {
                    firstLoop = false;
                }
                //check if the task is compatible with the other previous one
                if (referent.equals(new SchedulingTaskComparator(internalTask, currentJob, sessionid))) {
                    tlogger.debug(internalTask.getId(), "scheduling");
                    totalNeededNodes += neededNodes;
                    toFill.add(etd);
                } else {
                    bagOfTasks.addFirst(etd);
                    break;
                }
            } while (!bagOfTasks.isEmpty());
        }
        return totalNeededNodes;
    }

    /**
     * Ask to the RM the given number of node resources.<br>
     * If there is a problem with these task selection (such as bad selectionScript) this method
     * will terminate the corresponding tasks and jobs. As the selection scripts contain errors, the task
     * and its surrounding jobs must be stopped.
     *
     * @param neededResourcesNumber the number of resources to ask for (must be &gt; 0).
     * @param tasksToSchedule the task to be scheduled
     * @return A nodeSet that contains at most 'neededResourcesNumber' available compatible resources.
     * 		   An empty nodeSet if no nodes could be found
     * 		   null if the their was an exception when asking for the nodes (ie : selection script has failed)
     */
    protected NodeSet getRMNodes(Map<JobId, JobDescriptor> jobMap, int neededResourcesNumber,
            LinkedList<EligibleTaskDescriptor> tasksToSchedule, Set<String> freeResources) {
        NodeSet nodeSet;
        if (neededResourcesNumber <= 0) {
            throw new IllegalArgumentException("'neededResourcesNumber' must be greater than 0");
        }

        EligibleTaskDescriptor etd = tasksToSchedule.getFirst();
        InternalJob currentJob = ((JobDescriptorImpl) jobMap.get(etd.getJobId())).getInternal();
        InternalTask internalTask0 = currentJob.getIHMTasks().get(etd.getTaskId());
        try {

            TopologyDescriptor descriptor = null;
            boolean bestEffort = true;

            List<Map<String, String>> listUsageInfo = new LinkedList<>();
            if (internalTask0.isParallel()) {
                listUsageInfo.add(getUsageInfo(internalTask0));
                descriptor = internalTask0.getParallelEnvironment().getTopologyDescriptor();
                bestEffort = false;
                if (descriptor == null) {
                    logger.debug("Topology is not defined for the task " + internalTask0.getName());
                }
            } else {
                for (int i = 0; i < neededResourcesNumber; ++i) {
                    EligibleTaskDescriptor eligibleTaskDescriptor = tasksToSchedule.get(i);
                    InternalJob internalJob = ((JobDescriptorImpl) jobMap.get(eligibleTaskDescriptor.getJobId())).getInternal();
                    InternalTask internalTask = internalJob.getIHMTasks().get(eligibleTaskDescriptor.getTaskId());
                    listUsageInfo.add(getUsageInfo(internalTask));
                }
            }

            if (descriptor == null) {
                // descriptor is not defined, use default
                descriptor = TopologyDescriptor.ARBITRARY;
            }

            try {
                schedulingMainLoopTimingLogger.start("setCriteria");
                Criteria criteria = new Criteria(neededResourcesNumber);
                criteria.setListUsageInfo(listUsageInfo);
                criteria.setTopology(descriptor);
                // resolve script variables (if any) in the list of selection
                // scripts and then set it as the selection criteria.
                String sessionid = getSessionid(currentJob);
                criteria.setScripts(resolveScriptVariables(internalTask0.getSelectionScripts(),
                                                           internalTask0.getRuntimeVariables(),
                                                           sessionid,
                                                           currentJob.getOwner()));
                criteria.setBlackList(internalTask0.getNodeExclusion());
                criteria.setBestEffort(bestEffort);
                if (PASchedulerProperties.SCHEDULER_POLCY_STRICT_FIFO.getValueAsBoolean()) {
                    criteria.setAcceptableNodesUrls(freeResources);
                }
                criteria.setBindings(createBindingsForSelectionScripts(currentJob, internalTask0, schedulingService));
                Map<String, String> runtimeGenericInfo = internalTask0.getRuntimeGenericInformation();
                if (runtimeGenericInfo.containsKey(SchedulerConstants.NODE_ACCESS_TOKEN)) {
                    criteria.setNodeAccessToken(runtimeGenericInfo.get(SchedulerConstants.NODE_ACCESS_TOKEN));
                }
                if (runtimeGenericInfo.containsKey(SchedulerConstants.NODE_SOURCE_GENERIC_INFO)) {
                    criteria.setNodeSourceRestriction(runtimeGenericInfo.get(SchedulerConstants.NODE_SOURCE_GENERIC_INFO));
                }

                Collection<String> computationDescriptors = new ArrayList<>(tasksToSchedule.size());
                for (EligibleTaskDescriptor task : tasksToSchedule) {
                    computationDescriptors.add(TaskLogger.getTaskLogRelativePath(task.getTaskId()));
                }

                criteria.setComputationDescriptors(computationDescriptors);

                schedulingMainLoopTimingLogger.end("setCriteria");

                schedulingMainLoopTimingLogger.start("getNodeSetWithCriteria");
                RMProxy rmProxy = getRMProxiesManager().getUserRMProxy(currentJob.getOwner(),
                                                                       currentJob.getCredentials());
                nodeSet = rmProxy.getNodes(criteria);
                schedulingMainLoopTimingLogger.end("getNodeSetWithCriteria");
            } catch (TopologyDisabledException tde) {
                jlogger.warn(currentJob.getId(), "will be canceled as the topology is disabled");
                schedulingService.simulateJobStartAndCancelIt(tasksToSchedule, "Topology is disabled");
                return null;
            }
            //the following line is used to unwrap the future, warning when moving or removing
            //it may also throw a ScriptException which is a RuntimeException
            schedulingMainLoopTimingLogger.start("waitForNodeSetWithCriteria");

            PAFuture.waitFor(nodeSet, true);
            schedulingMainLoopTimingLogger.end("waitForNodeSetWithCriteria");

            logger.debug("provided nodes " + nodeSet.size());
            return nodeSet;

        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Failed to deserialize previous task variables before selection for task " +
                        internalTask0.getId().toString(), e);
            schedulingService.simulateJobStartAndCancelIt(tasksToSchedule,
                                                          "Failed to deserialize previous task variables before selection for task " +
                                                                           internalTask0.getId().toString());
            return null;
        } catch (RMProxyCreationException e) {
            logger.warn("Failed to create User RM Proxy", e);
            //simulate jobs starts and cancel it
            schedulingService.simulateJobStartAndCancelIt(tasksToSchedule,
                                                          "Failed to create User RM Proxy : Authentication Failed to Resource Manager for user '" +
                                                                           currentJob.getOwner() + "'");
            //leave the method by ss failure
            return null;
        }
    }

    private Map<String, String> getUsageInfo(InternalTask internalTask) {
        Map<String, String> metaInfoAboutTask = new HashMap<>();
        if (internalTask != null) {
            metaInfoAboutTask.put("TASK_ID", internalTask.getId().value());
            metaInfoAboutTask.put("TASK_NAME", internalTask.getId().getReadableName());
            metaInfoAboutTask.put("JOB_ID", internalTask.getId().getJobId().value());
            metaInfoAboutTask.put("JOB_NAME", internalTask.getId().getJobId().getReadableName());
        }
        return metaInfoAboutTask;
    }

    /**
     * Update all variables for the given scheduled tasks
     */
    private void updateVariablesForTasksToSchedule(LinkedList<EligibleTaskDescriptor> tasksToSchedule) {
        for (EligibleTaskDescriptor taskDescriptor : tasksToSchedule) {
            InternalTask internalTask = ((EligibleTaskDescriptorImpl) taskDescriptor).getInternal();
            internalTask.updateVariables(schedulingService);
        }
    }

    /**
     * Create bindings which will be used by selection scripts for the given tasks
     */
    public static Map<String, Serializable> createBindingsForSelectionScripts(InternalJob job, InternalTask task,
            SchedulingService service) throws IOException, ClassNotFoundException {
        Map<String, Serializable> bindings = new HashMap<>();
        Map<String, Serializable> variables = new HashMap<>();
        Map<String, Serializable> genericInfo = new HashMap<>();

        variables.putAll(task.getRuntimeVariables());

        genericInfo.putAll(job.getRuntimeGenericInformation());

        if (job.getType() == JobType.TASKSFLOW) {
            genericInfo.putAll(task.getRuntimeGenericInformation());
        }

        bindings.put(SchedulerConstants.VARIABLES_BINDING_NAME, (Serializable) variables);
        bindings.put(SchedulerConstants.GENERIC_INFO_BINDING_NAME, (Serializable) genericInfo);
        if (service != null) {
            bindings.put(SchedulerConstants.SYNCHRONIZATION_API_BINDING_NAME,
                         (Serializable) new SynchronizationWrapper(job.getOwner(),
                                                                   task.getId(),
                                                                   service.getSynchronizationAPI()));
            bindings.put(SchedulerConstants.SIGNAL_API_BINDING_NAME,
                         (Serializable) new SignalApiImpl(job.getOwner(),
                                                          task.getId(),
                                                          service.getSynchronizationAPI()));
        }
        return bindings;
    }

    public static SelectionScript replaceBindingsInsideScript(SelectionScript script,
            Map<String, Serializable> bindings) {
        String scriptContent = script.fetchScript();
        if (bindings != null && scriptContent != null) {
            for (Map.Entry<String, Serializable> entry : bindings.entrySet()) {
                scriptContent = scriptContent.replace(entry.getKey(), entry.getValue().toString());
            }
        }
        try {
            if (scriptContent != null) {
                return new SelectionScript(scriptContent,
                                           script.getEngineName(),
                                           script.getParameters(),
                                           script.isDynamic());
            } else {
                return new SelectionScript(script.getScriptUrl(),
                                           script.getEngineName(),
                                           script.getParameters(),
                                           script.isDynamic());
            }

        } catch (InvalidScriptException e) {
            logger.warn("Error when replacing bindings of script (revert to use original script):" +
                        System.lineSeparator() + script.toString(), e);
            return script;
        }
    }

    /**
     * Load and initialize the task to be started
     *
     * @param task the task to be initialized
     */
    protected void loadAndInit(InternalTask task) {
        if ((task.getExecutableContainer() == null) ||
            ((ScriptExecutableContainer) task.getExecutableContainer()).getScript() == null) {
            tlogger.debug(task.getId(), "initializing the executable container");
            ExecutableContainer container = getDBManager().loadExecutableContainer(task);
            task.setExecutableContainer(container);
        }
    }

    private void handleLoadExecutableContainerError(InternalTask task, Iterator<EligibleTaskDescriptor> etd,
            Exception e) {
        String message = "Error when loading task " + task.toString() +
                         " definition from the database. See server logs for more details. Killing task " +
                         task.getId();
        logger.error(message, e);
        etd.remove();
        try {
            schedulingService.killTask(task.getJobId(), task.getName(), message);
        } catch (Exception e2) {
            logger.error("Unable to kill task " + task.getId(), e2);
        }
    }

    /**
     * Create launcher and try to start the task.
     *
     * @param extraNodes the node set containing nodes used by this task execution
     * @param node the node on which to start the task
     * @param job the job that owns the task to be started
     * @param task the task to be started
     * @param taskDescriptor the descriptor of the task to be started
     *
     */
    protected boolean createExecution(NodeSet extraNodes, Node node, InternalJob job, InternalTask task,
            TaskDescriptor taskDescriptor, Map<String, Boolean> recoverableStatus, String terminateNotificationNodeURL)
            throws Exception {
        TaskLauncher launcher = null;
        LiveJobs.JobData jobData = null;
        String taskLauncherNodeUrl = null;

        String sessionid = getSessionid(job);

        jobData = schedulingService.lockJob(job.getId());
        if (jobData != null) {
            try {
                if (task.getStatus() != TaskStatus.PAUSED) {
                    //start dataspace app for this task
                    schedulingMainLoopTimingLogger.start("startDataspaceApp");
                    DataSpaceServiceStarter dsStarter = schedulingService.getInfrastructure()
                                                                         .getDataSpaceServiceStarter();
                    job.startDataSpaceApplication(dsStarter.getNamingService(), ImmutableList.of(task));
                    job.setSynchronizationAPI(schedulingService.getSynchronizationAPI());
                    schedulingMainLoopTimingLogger.end("startDataspaceApp");
                } else {
                    return false;
                }
            } finally {
                jobData.unlock();
            }
        }

        if (jobData != null) {
            // create launcher
            schedulingMainLoopTimingLogger.start("createLauncher");
            launcher = task.createLauncher(node, sessionid);
            try {
                taskLauncherNodeUrl = PAActiveObject.getUrl(launcher);
            } catch (Exception e) {
                logger.warn("TaskLauncher node URL could not be retrieved for task " + TaskIdWrapper.wrap(task.getId()),
                            e);
            }
            schedulingMainLoopTimingLogger.end("createLauncher");
        }

        schedulingMainLoopTimingLogger.start("jobLockAcquisition");
        jobData = schedulingService.lockJob(job.getId());
        schedulingMainLoopTimingLogger.end("jobLockAcquisition");
        if (jobData != null) {
            try {
                if (task.getStatus() != TaskStatus.PAUSED) {
                    Set<String> nodesUrls = new HashSet<>();

                    activeObjectCreationRetryTimeNumber = ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER;

                    nodesUrls.add(node.getNodeInformation().getURL());

                    //if topology is enabled and it is a multi task, give every nodes to the multi-nodes task
                    // we will need to update this code once topology will be allowed for single-node task
                    if (task.isParallel()) {
                        task.getExecuterInformation().addNodes(extraNodes);
                        for (Node parallelNode : extraNodes) {
                            nodesUrls.add(parallelNode.getNodeInformation().getURL());
                        }
                    }

                    //set nodes in the executable container
                    task.getExecutableContainer().setNodes(extraNodes);

                    tlogger.debug(task.getId(), "deploying");

                    // Dynamically adjust the start-task-timeout according to the number dependency tasks in a merge.
                    // above 500 parent tasks, it is worth adjusting.
                    if (taskDescriptor.getParents().size() > 500) {
                        dotaskActionTimeout = (int) (taskDescriptor.getParents().size() / 500.0 *
                                                     PASchedulerProperties.SCHEDULER_STARTTASK_TIMEOUT.getValueAsInt());
                    } else {
                        // reset the dotaskActionTimeout to its default value otherwise.
                        dotaskActionTimeout = PASchedulerProperties.SCHEDULER_STARTTASK_TIMEOUT.getValueAsInt();
                    }

                    schedulingMainLoopTimingLogger.start("areNodesRecoverable");
                    boolean taskRecoverable = nodesUrls.stream().allMatch(nodeUrl -> recoverableStatus.get(nodeUrl));
                    schedulingMainLoopTimingLogger.end("areNodesRecoverable");

                    TaskRecoveryData taskRecoveryData = new TaskRecoveryData(terminateNotificationNodeURL,
                                                                             taskRecoverable);

                    schedulingMainLoopTimingLogger.start("submitWithTimeout");

                    threadPool.submitWithTimeout(new TimedDoTaskAction(job,
                                                                       taskDescriptor,
                                                                       launcher,
                                                                       schedulingService,
                                                                       terminateNotification,
                                                                       corePrivateKey,
                                                                       taskRecoveryData,
                                                                       sessionid),

                                                 dotaskActionTimeout,
                                                 TimeUnit.MILLISECONDS);

                    schedulingMainLoopTimingLogger.end("submitWithTimeout");

                    // we advertise here that the task is started, however
                    // this is not entirely true: the only thing we are sure
                    // about at this point is that we submitted to the thread
                    // pool the action that will call the "doTask" of the task
                    // launcher. There is thus a small gap here where the task
                    // is seen as started whereas it is not yet started. We
                    // cannot easily move the task started notification because
                    // 1) it makes the job lock acquisition less predictable
                    // (because the TimeDoTaskAction will have to compete with
                    // the SchedulingMethodImpl)
                    // and more importantly 2) the
                    // SchedulingMethodImpl#createExecution may happen to be
                    // called a second time for the task that is currently being
                    // started by the TimedDoTaskAction.
                    schedulingMainLoopTimingLogger.start("finalizeStarting");
                    finalizeStarting(job, task, node, launcher, taskLauncherNodeUrl);
                    schedulingMainLoopTimingLogger.end("finalizeStarting");
                    return true;
                } else {
                    terminateLauncherIfNeeded(launcher);
                    return false;
                }
            } finally {
                if (jobData != null) {
                    jobData.unlock();
                }
            }

        } else {
            terminateLauncherIfNeeded(launcher);
            return false;
        }
    }

    private void terminateLauncherIfNeeded(TaskLauncher launcher) {
        if (launcher != null) {
            try {
                PAActiveObject.terminateActiveObject(launcher, true);
            } catch (Exception ignored) {

            }
        }
    }

    public synchronized String getSessionid(InternalJob job) {
        String sessionid = sessionidCache.getIfPresent(job.getOwner());
        if (sessionid != null) {
            return sessionid;
        }
        try {
            RMProxiesManager proxiesManager = getRMProxiesManager();
            sessionid = proxiesManager.getUserRMProxy(job.getOwner(), job.getCredentials()).getSessionid();
            if (sessionid != null) {
                sessionidCache.put(job.getOwner(), sessionid);
            } else if (!"true".equals(System.getProperty(SchedulerStarter.REST_DISABLED_PROPERTY))) {
                logger.warn("Unexpected null session id for user " + job.getOwner());
            }
            return sessionid;
        } catch (RMProxyCreationException e) {
            logger.warn("Error when creating proxy for " + job.getOwner(), e);
            return null;
        }
    }

    /**
     * Finalize the start of the task by mark it as started. Also mark the job if it is not already started.
     *
     * @param job the job that owns the task to be started
     * @param task the task to be started
     * @param node the node on which the task will be started
     * @param launcher the taskLauncher that has just been launched
     */
    void finalizeStarting(InternalJob job, InternalTask task, Node node, TaskLauncher launcher,
            String taskLauncherNodeUrl) {
        tlogger.info(task.getId(),
                     "started on " + node.getNodeInformation().getVMInformation().getHostName() + "(node: " +
                                   node.getNodeInformation().getName() + ")");

        schedulingService.taskStarted(job, task, launcher, taskLauncherNodeUrl);
    }

    private SchedulerDBManager getDBManager() {
        return schedulingService.getInfrastructure().getDBManager();
    }

    /*
     * Replace selection script variables with values specified in the map.
     */
    public static List<SelectionScript> resolveScriptVariables(List<SelectionScript> selectionScripts,
            Map<String, Serializable> variables, String sessionid, String owner) {
        List<SelectionScript> output = new LinkedList<>();
        if (selectionScripts == null) {
            return null;
        }
        for (SelectionScript script : selectionScripts) {
            if (sessionid != null) {
                script.setSessionid(sessionid);
                script.setOwner(owner);
            }
            SelectionScript resolved = SelectionScript.resolvedSelectionScript(script);
            VariableSubstitutor.filterAndUpdate(resolved, variables);
            output.add(resolved);
        }
        return output;
    }

}
