/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.core;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyDisabledException;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxyCreationException;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.descriptor.TaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.Policy;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.threading.TimeoutThreadPoolExecutor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.TimeUnit;


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
    protected static final int DOTASK_ACTION_TIMEOUT = PASchedulerProperties.SCHEDULER_STARTTASK_TIMEOUT
            .getValueAsInt();

    protected int activeObjectCreationRetryTimeNumber;

    protected final SchedulingService schedulingService;

    protected TimeoutThreadPoolExecutor threadPool;

    protected PrivateKey corePrivateKey;

    private InternalPolicy internalPolicy;

    private TaskTerminateNotification terminateNotification;

    public SchedulingMethodImpl(SchedulingService schedulingService) throws Exception {
        this.schedulingService = schedulingService;

        terminateNotification = new TerminateNotification(schedulingService);
        terminateNotification = PAActiveObject.turnActive(terminateNotification,
                TaskTerminateNotification.class.getName(), null);

        this.threadPool = TimeoutThreadPoolExecutor.newFixedThreadPool(
                PASchedulerProperties.SCHEDULER_STARTTASK_THREADNUMBER.getValueAsInt(),
                new NamedThreadFactory("DoTask_Action"));
        this.internalPolicy = new InternalPolicy();
        this.corePrivateKey = Credentials.getPrivateKey(PASchedulerProperties
                .getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_PRIVKEY_PATH.getValueAsString()));
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
        try {
            List<JobDescriptor> descriptors = new ArrayList<>(jobMap.size());
            descriptors.addAll(jobMap.values());

            //ask the policy all the tasks to be schedule according to the jobs list.
            //and filter them using internal policy
            LinkedList<EligibleTaskDescriptor> taskRetrievedFromPolicy = internalPolicy.filter(currentPolicy
                    .getOrderedTasks(descriptors));

            //if there is no task to scheduled, return
            if (taskRetrievedFromPolicy == null || taskRetrievedFromPolicy.size() == 0) {
                return numberOfTaskStarted;
            }

            logger.debug("eligible tasks : " + taskRetrievedFromPolicy.size());

            while (!taskRetrievedFromPolicy.isEmpty()) {
                //get rmState and update it in scheduling policy
                RMState rmState = getRMProxiesManager().getRmProxy().getState();
                currentPolicy.setRMState(rmState);
                internalPolicy.RMState = rmState;
                int freeResourcesNb = rmState.getFreeNodesNumber();
                logger.debug("eligible nodes : " + freeResourcesNb);
                //if there is no free resources, stop it right now
                if (freeResourcesNb == 0) {
                    break;
                }

                //get the next compatible tasks from the whole returned policy tasks
                LinkedList<EligibleTaskDescriptor> tasksToSchedule = new LinkedList<>();
                int neededResourcesNumber = 0;
                while (taskRetrievedFromPolicy.size() > 0 && neededResourcesNumber == 0) {
                    //the loop will search for next compatible task until it find something
                    neededResourcesNumber = getNextcompatibleTasks(jobMap, taskRetrievedFromPolicy,
                            freeResourcesNb, tasksToSchedule);
                }
                logger.debug("required number of nodes : " + neededResourcesNumber);
                if (neededResourcesNumber == 0) {
                    break;
                }

                NodeSet nodeSet = getRMNodes(jobMap, neededResourcesNumber, tasksToSchedule);

                currentPolicy.filterAfterSelection(nodeSet, tasksToSchedule);

                //start selected tasks
                Node node = null;
                InternalJob currentJob = null;
                try {
                    while (nodeSet != null && !nodeSet.isEmpty()) {
                        EligibleTaskDescriptor taskDescriptor = tasksToSchedule.removeFirst();
                        currentJob = jobMap.get(taskDescriptor.getJobId()).getInternal();
                        InternalTask internalTask = currentJob.getIHMTasks().get(taskDescriptor.getTaskId());

                        // load and Initialize the executable container
                        loadAndInit(internalTask);

                        //create launcher and try to start the task
                        node = nodeSet.get(0);
                        numberOfTaskStarted++;
                        createExecution(nodeSet, node, currentJob, internalTask, taskDescriptor);

                        //if every task that should be launched have been removed
                        if (tasksToSchedule.isEmpty()) {
                            //get back unused nodes to the RManager
                            if (!nodeSet.isEmpty()) {
                                releaseNodes(currentJob, nodeSet);
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
                    } catch (Exception e2) {
                        logger.info("Unable to get back the nodeSet to the RM", e2);
                    }
                }
            }

            return numberOfTaskStarted;
        } finally {
            schedulingService.unlockJobsToSchedule(jobMap.values());
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
            LinkedList<EligibleTaskDescriptor> bagOfTasks, int maxResource,
            LinkedList<EligibleTaskDescriptor> toFill) {
        if (toFill == null || bagOfTasks == null) {
            throw new IllegalArgumentException("The two given lists must not be null !");
        }
        int neededResource = 0;
        if (maxResource > 0 && !bagOfTasks.isEmpty()) {
            EligibleTaskDescriptor etd = bagOfTasks.removeFirst();
            ((EligibleTaskDescriptorImpl) etd).addAttempt();
            InternalJob currentJob = jobsMap.get(etd.getJobId()).getInternal();
            InternalTask internalTask = currentJob.getIHMTasks().get(etd.getTaskId());
            int neededNodes = internalTask.getNumberOfNodesNeeded();
            SchedulingTaskComparator referent = new SchedulingTaskComparator(internalTask, currentJob
                    .getOwner());
            boolean firstLoop = true;
            do {
                if (!firstLoop) {
                    //if bagOfTasks is not empty
                    if (!bagOfTasks.isEmpty()) {
                        etd = bagOfTasks.removeFirst();
                        ((EligibleTaskDescriptorImpl) etd).addAttempt();
                        currentJob = jobsMap.get(etd.getJobId()).getInternal();
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
                    if (referent.equals(new SchedulingTaskComparator(internalTask, currentJob.getOwner()))) {
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
            LinkedList<EligibleTaskDescriptor> tasksToSchedule) {
        NodeSet nodeSet = new NodeSet();

        if (neededResourcesNumber <= 0) {
            throw new IllegalArgumentException("'neededResourcesNumber' must be greater than 0");
        }

        EligibleTaskDescriptor etd = tasksToSchedule.getFirst();
        InternalJob currentJob = jobMap.get(etd.getJobId()).getInternal();
        InternalTask internalTask = currentJob.getIHMTasks().get(etd.getTaskId());

        try {
            TopologyDescriptor descriptor = null;
            boolean bestEffort = true;
            if (internalTask.isParallel()) {
                descriptor = internalTask.getParallelEnvironment().getTopologyDescriptor();
                bestEffort = false;
                if (descriptor == null) {
                    logger.debug("Topology is not defined for the task " + internalTask.getName());
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
                criteria.setScripts(resolveScriptVariables(internalTask.getSelectionScripts(), currentJob
                        .getVariables()));
                criteria.setBlackList(internalTask.getNodeExclusion());
                criteria.setBestEffort(bestEffort);

                if (internalTask.getGenericInformation().containsKey(SchedulerConstants.NODE_ACCESS_TOKEN)) {
                    criteria.setNodeAccessToken(internalTask.getGenericInformation().get(
                            SchedulerConstants.NODE_ACCESS_TOKEN));
                }

                Collection<String> computationDescriptors = new ArrayList<>(tasksToSchedule.size());
                for (EligibleTaskDescriptor task : tasksToSchedule) {
                    computationDescriptors.add(TaskLogger.getTaskLogFilename(task.getTaskId()));
                }

                criteria.setComputationDescriptors(computationDescriptors);

                nodeSet = getRMProxiesManager().getUserRMProxy(currentJob.getOwner(),
                        currentJob.getCredentials()).getNodes(criteria);
            } catch (TopologyDisabledException tde) {
                jlogger.info(currentJob.getId(), "will be canceled as the topology is disabled");
                schedulingService.simulateJobStartAndCancelIt(tasksToSchedule, "Topology is disabled");
                return null;
            }
            //the following line is used to unwrap the future, warning when moving or removing
            //it may also throw a ScriptException which is a RuntimeException
            PAFuture.waitFor(nodeSet, true);
            logger.debug("provided nodes " + nodeSet.size());
            return nodeSet;
        } catch (RMProxyCreationException e) {
            logger.info("Failed to create User RM Proxy : " + e.getMessage());
            logger.debug("", e);
            //simulate jobs starts and cancel it
            schedulingService.simulateJobStartAndCancelIt(tasksToSchedule,
                    "Failed to create User RM Proxy : Authentication Failed to Resource Manager for user '" +
                        currentJob.getOwner() + "'");
            //leave the method by ss failure
            return null;
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
    protected void createExecution(NodeSet nodeSet, Node node, InternalJob job, InternalTask task,
            TaskDescriptor taskDescriptor) throws Exception {
        TaskLauncher launcher = null;

        //enough nodes to be launched at same time for a communicating task
        if (nodeSet.size() >= task.getNumberOfNodesNeeded()) {
            //start dataspace app for this job
            DataSpaceServiceStarter dsStarter = schedulingService.getInfrastructure()
                    .getDataSpaceServiceStarter();
            job.startDataSpaceApplication(dsStarter.getNamingService());

            // create launcher
            launcher = task.createLauncher(job, node);

            activeObjectCreationRetryTimeNumber = ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER;

            nodeSet.remove(0);

            NodeSet nodes = new NodeSet();
            try {
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

                threadPool.submitWithTimeout(new TimedDoTaskAction(job, taskDescriptor, launcher,
                    schedulingService, terminateNotification, corePrivateKey), DOTASK_ACTION_TIMEOUT,
                        TimeUnit.MILLISECONDS);
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
        tlogger.info(task.getId(), "started on " +
            node.getNodeInformation().getVMInformation().getHostName() + "(node: " +
            node.getNodeInformation().getName() + ")");

        schedulingService.taskStarted(job, task, launcher);
    }

    private SchedulerDBManager getDBManager() {
        return schedulingService.getInfrastructure().getDBManager();
    }

    /*
     * Replace selection script variables with values specified in the map.
     */
    private List<SelectionScript> resolveScriptVariables(List<SelectionScript> selectionScripts,
            Map<String, String> variables) {
        if (selectionScripts == null) {
            return null;
        }
        for (SelectionScript script : selectionScripts) {
            VariableSubstitutor.filterAndUpdate(script, variables);
        }
        return selectionScripts;
    }

}
