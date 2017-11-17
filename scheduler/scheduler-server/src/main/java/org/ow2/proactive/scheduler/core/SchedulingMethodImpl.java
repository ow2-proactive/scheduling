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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import org.ow2.proactive.scheduler.core.rmproxies.RMProxyCreationException;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.Policy;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.threading.TimeoutThreadPoolExecutor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import com.google.common.collect.ImmutableList;


/**
 * SchedulingMethodImpl is the default implementation for the scheduling process
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public final class SchedulingMethodImpl implements SchedulingMethod {

    /** Scheduler logger */
    public static final Logger logger = Logger.getLogger(SchedulingService.class);

    public static final TaskLogger tlogger = TaskLogger.getInstance();

    public static final JobLogger jlogger = JobLogger.getInstance();

    /** Number of time to retry an active object creation if it fails to create */
    protected static final int ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER = 3;

    /** Maximum blocking time for the do task action */
    protected static final int DOTASK_ACTION_TIMEOUT = PASchedulerProperties.SCHEDULER_STARTTASK_TIMEOUT.getValueAsInt();

    protected int activeObjectCreationRetryTimeNumber;

    protected final SchedulingService schedulingService;

    protected TimeoutThreadPoolExecutor threadPool;

    protected PrivateKey corePrivateKey;

    private TaskTerminateNotification terminateNotification;

    private CheckEligibleTaskDescriptorScript checkEligibleTaskDescriptorScript;

    public SchedulingMethodImpl(SchedulingService schedulingService) throws Exception {
        this.schedulingService = schedulingService;
        this.checkEligibleTaskDescriptorScript = new CheckEligibleTaskDescriptorScript();
        terminateNotification = new TerminateNotification(schedulingService);
        terminateNotification = PAActiveObject.turnActive(terminateNotification,
                                                          TaskTerminateNotification.class.getName(),
                                                          NodeFactory.createLocalNode("taskTerminationNode",
                                                                                      true,
                                                                                      "taskTerminationVNode"));

        this.threadPool = TimeoutThreadPoolExecutor.newFixedThreadPool(PASchedulerProperties.SCHEDULER_STARTTASK_THREADNUMBER.getValueAsInt(),
                                                                       new NamedThreadFactory("DoTask_Action"));
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
        Policy currentPolicy = schedulingService.getPolicy();

        int numberOfTaskStarted = 0;
        //Number of time to retry an active object creation before leaving scheduling loop
        activeObjectCreationRetryTimeNumber = ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER;

        //get job Descriptor list with eligible jobs (running and pending)
        Map<JobId, JobDescriptor> jobMap = schedulingService.lockJobsToSchedule();

        Map<JobId, JobDescriptor> toUnlock = jobMap;

        if (logger.isTraceEnabled() && jobMap == null || jobMap.isEmpty()) {
            logger.trace("No jobs selected to be scheduled");
        }
        if (logger.isDebugEnabled() && !jobMap.isEmpty()) {
            logger.debug("jobs selected to be scheduled : " + jobMap);
        }

        // If there are some jobs which could not be locked it is not possible to do any priority scheduling decision,
        // we wait for next scheduling loop
        if (jobMap.isEmpty()) {
            return numberOfTaskStarted;
        }

        try {
            List<JobDescriptor> descriptors = new ArrayList<>(jobMap.values());

            //get rmState and update it in scheduling policy
            RMState rmState = getRMProxiesManager().getRmProxy().getState();
            currentPolicy.setRMState(rmState);
            Set<String> freeResources = rmState.getFreeNodes();
            if (logger.isDebugEnabled()) {
                logger.debug("eligible nodes : " + freeResources);
            }
            //if there is no free resources, stop it right now
            if (freeResources.isEmpty()) {
                return numberOfTaskStarted;
            }

            // ask the policy all the tasks to be schedule according to the jobs list.

            LinkedList<EligibleTaskDescriptor> taskRetrievedFromPolicy = currentPolicy.getOrderedTasks(descriptors);

            //if there is no task to scheduled, return
            if (taskRetrievedFromPolicy == null || taskRetrievedFromPolicy.isEmpty()) {
                return numberOfTaskStarted;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("eligible tasks : " + taskRetrievedFromPolicy);
            }

            schedulingService.unlockJobsToSchedule(toUnlock.values());
            toUnlock = null;

            updateVariablesForTasksToSchedule(jobMap, taskRetrievedFromPolicy);

            for (EligibleTaskDescriptor etd : taskRetrievedFromPolicy) {
                // load and Initialize the executable container
                loadAndInit(((EligibleTaskDescriptorImpl) etd).getInternal());
            }

            while (!taskRetrievedFromPolicy.isEmpty()) {

                if (freeResources.isEmpty()) {
                    return numberOfTaskStarted;
                }

                //get the next compatible tasks from the whole returned policy tasks
                LinkedList<EligibleTaskDescriptor> tasksToSchedule = new LinkedList<>();
                int neededResourcesNumber = 0;

                while (taskRetrievedFromPolicy.size() > 0 && neededResourcesNumber == 0) {
                    //the loop will search for next compatible task until it find something
                    neededResourcesNumber = getNextcompatibleTasks(jobMap,
                                                                   taskRetrievedFromPolicy,
                                                                   freeResources.size(),
                                                                   tasksToSchedule);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("tasksToSchedule : " + tasksToSchedule);
                }

                logger.debug("required number of nodes : " + neededResourcesNumber);
                if (neededResourcesNumber == 0 || tasksToSchedule.isEmpty()) {
                    break;
                }

                NodeSet nodeSet = getRMNodes(jobMap, neededResourcesNumber, tasksToSchedule, freeResources);

                if (nodeSet != null) {
                    freeResources.removeAll(nodeSet.getAllNodesUrls());
                }

                //start selected tasks
                Node node = null;
                InternalJob currentJob = null;
                try {
                    while (nodeSet != null && !nodeSet.isEmpty()) {
                        EligibleTaskDescriptor taskDescriptor = tasksToSchedule.removeFirst();
                        currentJob = ((JobDescriptorImpl) jobMap.get(taskDescriptor.getJobId())).getInternal();
                        InternalTask internalTask = currentJob.getIHMTasks().get(taskDescriptor.getTaskId());

                        if (currentPolicy.isTaskExecutable(nodeSet, taskDescriptor)) {
                            //create launcher and try to start the task
                            node = nodeSet.get(0);

                            if (createExecution(nodeSet, node, currentJob, internalTask, taskDescriptor)) {
                                numberOfTaskStarted++;
                            }

                        }

                        //if every task that should be launched have been removed
                        if (tasksToSchedule.isEmpty()) {
                            //get back unused nodes to the RManager
                            if (!nodeSet.isEmpty()) {
                                releaseNodes(currentJob, nodeSet);
                                freeResources.addAll(nodeSet.getAllNodesUrls());
                            }
                            //and leave the loop
                            break;
                        }
                    }
                } catch (ActiveObjectCreationException e1) {
                    //Something goes wrong with the active object creation (createLauncher)
                    logger.warn("An exception occured while creating the task launcher.", e1);
                    //so try to get back every remaining nodes to the resource manager
                    try {
                        releaseNodes(currentJob, nodeSet);
                        freeResources.addAll(nodeSet.getAllNodesUrls());
                    } catch (Exception e2) {
                        logger.info("Unable to get back the nodeSet to the RM", e2);
                    }
                    if (--activeObjectCreationRetryTimeNumber == 0) {
                        return numberOfTaskStarted;
                    }
                } catch (Exception e1) {
                    //if we are here, it is that something append while launching the current task.
                    logger.warn("An exception occured while starting task.", e1);
                    //so try to get back every remaining nodes to the resource manager
                    try {
                        releaseNodes(currentJob, nodeSet);
                        freeResources.addAll(nodeSet.getAllNodesUrls());
                    } catch (Exception e2) {
                        logger.info("Unable to get back the nodeSet to the RM", e2);
                    }
                }
            }

            return numberOfTaskStarted;
        } finally {
            if (toUnlock != null) {
                schedulingService.unlockJobsToSchedule(toUnlock.values());
            }
        }
    }

    /**
     * Extract the n first compatible tasks from the first argument list,
     * and return them according that the extraction is stopped when the maxResource number is reached.<br>
     * Two tasks are compatible if and only if they have the same list of selection script and
     * the same list of node exclusion.
     * The check of compliance is currently done by the {@link SchedulingTaskComparator} class.<br>
     * This method has two side effects : extracted tasks are removed from the bagOfTasks and put in the toFill list
     *
     * @param bagOfTasks the list of tasks form which to extract tasks
     * @param maxResource the limit number of resources that the extraction should not exceed
     * @param toFill the list that will contains the task to schedule at the end. This list must not be null but must be empty.<br>
     * 		  this list will be filled with the n first compatible tasks according that the number of resources needed
     * 		  by these tasks does not exceed the given max resource number.
     * @return the number of nodes needed to start every task present in the 'toFill' argument at the end of the method.
     */
    protected int getNextcompatibleTasks(Map<JobId, JobDescriptor> jobsMap,
            LinkedList<EligibleTaskDescriptor> bagOfTasks, int maxResource, LinkedList<EligibleTaskDescriptor> toFill) {
        if (toFill == null || bagOfTasks == null) {
            throw new IllegalArgumentException("The two given lists must not be null !");
        }
        int neededResource = 0;
        if (!PASchedulerProperties.SCHEDULER_REST_URL.isSet()) {
            Iterator<EligibleTaskDescriptor> it = bagOfTasks.iterator();
            EligibleTaskDescriptor etd;
            while (it.hasNext()) {
                etd = it.next();
                if (checkEligibleTaskDescriptorScript.isTaskContainsAPIBinding(etd)) {
                    //skip task here
                    it.remove();
                }
            }
        }
        if (maxResource > 0 && !bagOfTasks.isEmpty()) {
            EligibleTaskDescriptor etd = bagOfTasks.removeFirst();
            ((EligibleTaskDescriptorImpl) etd).addAttempt();
            InternalJob currentJob = ((JobDescriptorImpl) jobsMap.get(etd.getJobId())).getInternal();
            InternalTask internalTask = currentJob.getIHMTasks().get(etd.getTaskId());
            int neededNodes = internalTask.getNumberOfNodesNeeded();
            SchedulingTaskComparator referent = new SchedulingTaskComparator(internalTask, currentJob);
            boolean firstLoop = true;
            do {
                if (!firstLoop) {
                    //if bagOfTasks is not empty
                    if (!bagOfTasks.isEmpty()) {
                        etd = bagOfTasks.removeFirst();
                        ((EligibleTaskDescriptorImpl) etd).addAttempt();
                        currentJob = ((JobDescriptorImpl) jobsMap.get(etd.getJobId())).getInternal();
                        internalTask = currentJob.getIHMTasks().get(etd.getTaskId());
                        neededNodes = internalTask.getNumberOfNodesNeeded();
                    }
                } else {
                    firstLoop = false;
                }
                if (neededNodes > maxResource) {
                    //no instruction is important :
                    //in this case, a multi node task leads the search to be stopped and the
                    //the current task would be retried on the next step
                    //we continue to start the maximum number of task in a single scheduling loop.
                    //this case will focus on starting single node task first if lot of resources are busy.
                    //(multi-nodes starvation may occurs)
                } else {
                    //check if the task is compatible with the other previous one
                    if (referent.equals(new SchedulingTaskComparator(internalTask, currentJob))) {
                        tlogger.debug(internalTask.getId(), "scheduling");
                        neededResource += neededNodes;
                        maxResource -= neededNodes;
                        toFill.add(etd);
                    } else {
                        bagOfTasks.addFirst(etd);
                        break;
                    }
                }
            } while (maxResource > 0 && !bagOfTasks.isEmpty());
        }
        return neededResource;
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
        NodeSet nodeSet = new NodeSet();

        if (neededResourcesNumber <= 0) {
            throw new IllegalArgumentException("'neededResourcesNumber' must be greater than 0");
        }

        EligibleTaskDescriptor etd = tasksToSchedule.getFirst();
        InternalJob currentJob = ((JobDescriptorImpl) jobMap.get(etd.getJobId())).getInternal();
        InternalTask internalTask0 = currentJob.getIHMTasks().get(etd.getTaskId());

        try {

            TopologyDescriptor descriptor = null;
            boolean bestEffort = true;

            if (internalTask0.isParallel()) {
                descriptor = internalTask0.getParallelEnvironment().getTopologyDescriptor();
                bestEffort = false;
                if (descriptor == null) {
                    logger.debug("Topology is not defined for the task " + internalTask0.getName());
                }
            }
            if (descriptor == null) {
                // descriptor is not defined, use default
                descriptor = TopologyDescriptor.ARBITRARY;
            }

            try {
                Criteria criteria = new Criteria(neededResourcesNumber);
                criteria.setTopology(descriptor);
                // resolve script variables (if any) in the list of selection
                // scripts and then set it as the selection criteria.
                criteria.setScripts(resolveScriptVariables(internalTask0.getSelectionScripts(),
                                                           internalTask0.getRuntimeVariables()));
                criteria.setBlackList(internalTask0.getNodeExclusion());
                criteria.setBestEffort(bestEffort);
                criteria.setAcceptableNodesUrls(freeResources);
                criteria.setBindings(createBindingsForSelectionScripts(currentJob, internalTask0));

                if (internalTask0.getRuntimeGenericInformation().containsKey(SchedulerConstants.NODE_ACCESS_TOKEN)) {
                    criteria.setNodeAccessToken(internalTask0.getRuntimeGenericInformation()
                                                             .get(SchedulerConstants.NODE_ACCESS_TOKEN));
                }

                Collection<String> computationDescriptors = new ArrayList<>(tasksToSchedule.size());
                for (EligibleTaskDescriptor task : tasksToSchedule) {
                    computationDescriptors.add(TaskLogger.getTaskLogRelativePath(task.getTaskId()));
                }

                criteria.setComputationDescriptors(computationDescriptors);

                nodeSet = getRMProxiesManager().getUserRMProxy(currentJob.getOwner(), currentJob.getCredentials())
                                               .getNodes(criteria);
            } catch (TopologyDisabledException tde) {
                jlogger.warn(currentJob.getId(), "will be canceled as the topology is disabled");
                schedulingService.simulateJobStartAndCancelIt(tasksToSchedule, "Topology is disabled");
                return null;
            }
            //the following line is used to unwrap the future, warning when moving or removing
            //it may also throw a ScriptException which is a RuntimeException
            PAFuture.waitFor(nodeSet, true);
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

    /**
     * Update all variables for the given scheduled tasks
     */
    private void updateVariablesForTasksToSchedule(Map<JobId, JobDescriptor> jobMap,
            LinkedList<EligibleTaskDescriptor> tasksToSchedule) {
        for (EligibleTaskDescriptor taskDescriptor : tasksToSchedule) {
            InternalJob associatedJob = ((JobDescriptorImpl) jobMap.get(taskDescriptor.getJobId())).getInternal();
            InternalTask internalTask = associatedJob.getIHMTasks().get(taskDescriptor.getTaskId());
            internalTask.updateVariables(schedulingService);
        }
    }

    /**
     * Create bindings which will be used by selection scripts for the given tasks
     */
    public static Map<String, Serializable> createBindingsForSelectionScripts(InternalJob job, InternalTask task)
            throws IOException, ClassNotFoundException {
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
        return bindings;
    }

    public static SelectionScript replaceBindingsInsideScript(SelectionScript script,
            Map<String, Serializable> bindings) {
        String scriptContent = script.getScript();
        if (bindings != null) {
            for (Map.Entry<String, Serializable> entry : bindings.entrySet()) {
                scriptContent = scriptContent.replace(entry.getKey(), entry.getValue().toString());
            }
        }
        try {
            return new SelectionScript(scriptContent,
                                       script.getEngineName(),
                                       script.getParameters(),
                                       script.isDynamic());
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
        tlogger.debug(task.getId(), "initializing the executable container");
        ExecutableContainer container = getDBManager().loadExecutableContainer(task);
        task.setExecutableContainer(container);
    }

    /**
     * Create launcher and try to start the task.
     *
     * @param nodeSet the node set containing every available nodes that can be used for execution
     * @param node the node on which to start the task
     * @param job the job that owns the task to be started
     * @param task the task to be started
     * @param taskDescriptor the descriptor of the task to be started
     *
     */
    protected boolean createExecution(NodeSet nodeSet, Node node, InternalJob job, InternalTask task,
            TaskDescriptor taskDescriptor) throws Exception {
        TaskLauncher launcher = null;
        LiveJobs.JobData jobData = null;
        try {
            jobData = schedulingService.lockJob(job.getId());
            //enough nodes to be launched at same time for a communicating task
            // task is not paused
            if (nodeSet.size() >= task.getNumberOfNodesNeeded() && (task.getStatus() != TaskStatus.PAUSED) &&
                (jobData != null)) {
                //start dataspace app for this job
                DataSpaceServiceStarter dsStarter = schedulingService.getInfrastructure().getDataSpaceServiceStarter();
                job.startDataSpaceApplication(dsStarter.getNamingService(), ImmutableList.of(task));

                NodeSet nodes = new NodeSet();
                try {

                    // create launcher
                    launcher = task.createLauncher(node);

                    activeObjectCreationRetryTimeNumber = ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER;

                    nodeSet.remove(0);

                    //if topology is enabled and it is a multi task, give every nodes to the multi-nodes task
                    // we will need to update this code once topology will be allowed for single-node task
                    if (task.isParallel()) {
                        nodes = new NodeSet(nodeSet);
                        task.getExecuterInformation().addNodes(nodes);
                        nodeSet.clear();
                    }

                    //set nodes in the executable container
                    task.getExecutableContainer().setNodes(nodes);

                    tlogger.debug(task.getId(), "deploying");

                    finalizeStarting(job, task, node, launcher);

                    threadPool.submitWithTimeout(new TimedDoTaskAction(job,
                                                                       taskDescriptor,
                                                                       launcher,
                                                                       schedulingService,
                                                                       terminateNotification,
                                                                       corePrivateKey),
                                                 DOTASK_ACTION_TIMEOUT,
                                                 TimeUnit.MILLISECONDS);
                    return true;
                } catch (Exception t) {
                    try {
                        //if there was a problem, free nodeSet for multi-nodes task
                        nodes.add(node);
                        releaseNodes(job, nodes);
                    } catch (Throwable ni) {
                        //miam miam
                    }
                    throw t;
                }

            } else {
                return false;
            }
        } finally {
            if (jobData != null) {
                jobData.unlock();
            }
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
    void finalizeStarting(InternalJob job, InternalTask task, Node node, TaskLauncher launcher) {
        tlogger.info(task.getId(),
                     "started on " + node.getNodeInformation().getVMInformation().getHostName() + "(node: " +
                                   node.getNodeInformation().getName() + ")");

        schedulingService.taskStarted(job, task, launcher);
    }

    private SchedulerDBManager getDBManager() {
        return schedulingService.getInfrastructure().getDBManager();
    }

    /*
     * Replace selection script variables with values specified in the map.
     */
    public static List<SelectionScript> resolveScriptVariables(List<SelectionScript> selectionScripts,
            Map<String, Serializable> variables) {
        if (selectionScripts == null) {
            return null;
        }
        for (SelectionScript script : selectionScripts) {
            VariableSubstitutor.filterAndUpdate(script, variables);
        }
        return selectionScripts;
    }

}
