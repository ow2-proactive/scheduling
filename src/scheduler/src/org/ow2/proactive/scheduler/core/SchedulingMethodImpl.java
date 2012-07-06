/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import java.security.KeyException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyDisabledException;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxyCreationException;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.descriptor.TaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.ExecutableContainerInitializer;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.scripting.ScriptException;
import org.ow2.proactive.threading.TimeoutThreadPoolExecutor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Formatter;
import org.ow2.proactive.utils.NodeSet;


/**
 * SchedulingMethodImpl is the default implementation for the scheduling process
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
final class SchedulingMethodImpl implements SchedulingMethod {

    /** Scheduler logger */
    protected static final Logger logger = ProActiveLogger.getLogger(SchedulingMethodImpl.class);

    /** Number of time to retry an active object creation if it fails to create */
    protected static final int ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER = 3;
    /** Maximum blocking time for the do task action */
    protected static final int DOTASK_ACTION_TIMEOUT = PASchedulerProperties.SCHEDULER_STARTTASK_TIMEOUT
            .getValueAsInt();
    /** MAximum number of thread used for the doTask action */
    protected static final int DOTASK_ACTION_THREADNUMBER = PASchedulerProperties.SCHEDULER_STARTTASK_THREADNUMBER
            .getValueAsInt();

    protected int activeObjectCreationRetryTimeNumber;

    protected SchedulerCore core = null;

    protected TimeoutThreadPoolExecutor threadPool;

    protected PrivateKey corePrivateKey;

    private InternalPolicy internalPolicy;

    SchedulingMethodImpl(SchedulerCore core) {
        this.core = core;
        this.threadPool = TimeoutThreadPoolExecutor.newFixedThreadPool(DOTASK_ACTION_THREADNUMBER,
                new NamedThreadFactory("DoTask_Action"));
        this.internalPolicy = new InternalPolicy();
        try {
            this.corePrivateKey = Credentials.getPrivateKey(PASchedulerProperties
                    .getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_PRIVKEY_PATH.getValueAsString()));
        } catch (KeyException e) {
            SchedulerCore.exitFailure(e, null);
        }
    }

    /**
     * Scheduling process. For this implementation, steps are :<br>
     * <ul>
     * 	<li>Select running and pending jobs to be scheduled
     * 	<li>Get an ordered list of the selected tasks to be scheduled
     * 	<li>While returned tasks list is not empty :
     * 		<ul>
     * 			<li>Get n first compatible tasks (same selection script, same node exclusion
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
        int numberOfTaskStarted = 0;
        //Number of time to retry an active object creation before leaving scheduling loop
        activeObjectCreationRetryTimeNumber = ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER;

        //get job Descriptor list with eligible jobs (running and pending)
        ArrayList<JobDescriptor> jobDescriptorList = createJobDescriptorList();

        //ask the policy all the tasks to be schedule according to the jobs list.
        //and filter them using internal policy
        LinkedList<EligibleTaskDescriptor> taskRetrivedFromPolicy = internalPolicy.filter(core.policy
                .getOrderedTasks(jobDescriptorList));

        //if there is no task to scheduled, return
        if (taskRetrivedFromPolicy == null || taskRetrivedFromPolicy.size() == 0) {
            return numberOfTaskStarted;
        }

        logger.info("Number of tasks ready to be scheduled : " + taskRetrivedFromPolicy.size());

        while (!taskRetrivedFromPolicy.isEmpty()) {
            //get rmState and update it in scheduling policy
            RMState rmState = core.rmProxiesManager.getSchedulerRMProxy().getState();
            core.policy.setRMState(rmState);
            internalPolicy.RMState = rmState;
            int freeResourcesNb = rmState.getFreeNodesNumber();
            logger.info("Number of free resources : " + freeResourcesNb);
            //if there is no free resources, stop it right now
            if (freeResourcesNb == 0) {
                break;
            }

            //get the next compatible tasks from the whole returned policy tasks
            LinkedList<EligibleTaskDescriptor> tasksToSchedule = new LinkedList<EligibleTaskDescriptor>();
            int neededResourcesNumber = 0;
            while (taskRetrivedFromPolicy.size() > 0 && neededResourcesNumber == 0) {
                //the loop will search for next compatible task until it find something
                neededResourcesNumber = getNextcompatibleTasks(taskRetrivedFromPolicy, freeResourcesNb,
                        tasksToSchedule);
            }
            logger.debug("Number of nodes to ask for : " + neededResourcesNumber);
            if (neededResourcesNumber == 0) {
                break;
            }

            NodeSet nodeSet = getRMNodes(neededResourcesNumber, tasksToSchedule);

            //start selected tasks
            Node node = null;
            InternalJob currentJob = null;
            try {
                while (nodeSet != null && !nodeSet.isEmpty()) {
                    EligibleTaskDescriptor taskDescriptor = tasksToSchedule.removeFirst();
                    currentJob = core.jobs.get(taskDescriptor.getJobId());
                    InternalTask internalTask = currentJob.getIHMTasks().get(taskDescriptor.getTaskId());

                    // load and Initialize the executable container
                    loadAndInit(currentJob, internalTask);

                    //create launcher and try to start the task
                    node = nodeSet.get(0);
                    numberOfTaskStarted++;
                    createExecution(nodeSet, node, currentJob, internalTask, taskDescriptor);

                    //if every task that should be launched have been removed
                    if (tasksToSchedule.isEmpty()) {
                        //get back unused nodes to the RManager
                        if (!nodeSet.isEmpty()) {
                            core.rmProxiesManager.getUserRMProxy(currentJob.getOwner(),
                                    currentJob.getCredentials()).releaseNodes(nodeSet);
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
                    core.rmProxiesManager.getUserRMProxy(currentJob.getOwner(), currentJob.getCredentials())
                            .releaseNodes(nodeSet);
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
                    core.rmProxiesManager.getUserRMProxy(currentJob.getOwner(), currentJob.getCredentials())
                            .releaseNodes(nodeSet);
                } catch (Exception e2) {
                    logger.info("Unable to get back the nodeSet to the RM", e2);
                }
            }
        }
        return numberOfTaskStarted;
    }

    /**
     * Create the eligible job descriptor list.
     * This list contains every eligible jobs containing eligible tasks.
     *
     * @return eligible job descriptor list
     */
    protected ArrayList<JobDescriptor> createJobDescriptorList() {
        ArrayList<JobDescriptor> list = new ArrayList<JobDescriptor>();

        //add running jobs
        for (InternalJob j : core.runningJobs) {
            list.add(j.getJobDescriptor());
        }

        //if scheduler is not paused, add pending jobs
        if (core.status != SchedulerStatus.PAUSED) {
            for (InternalJob j : core.pendingJobs) {
                list.add(j.getJobDescriptor());
            }
        }

        if (list.size() > 0) {
            logger.info("Number of jobs containing tasks to be scheduled : " + list.size());
        }

        return list;
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
    protected int getNextcompatibleTasks(LinkedList<EligibleTaskDescriptor> bagOfTasks, int maxResource,
            LinkedList<EligibleTaskDescriptor> toFill) {
        if (toFill == null || bagOfTasks == null) {
            throw new IllegalArgumentException("The two given lists must not be null !");
        }
        int neededResource = 0;
        if (maxResource > 0 && !bagOfTasks.isEmpty()) {
            EligibleTaskDescriptor etd = bagOfTasks.removeFirst();
            ((EligibleTaskDescriptorImpl) etd).addAttempt();
            InternalJob currentJob = core.jobs.get(etd.getJobId());
            InternalTask internalTask = currentJob.getIHMTasks().get(etd.getTaskId());
            int neededNodes = internalTask.getNumberOfNodesNeeded();
            SchedulingTaskComparator referent = new SchedulingTaskComparator(internalTask, currentJob
                    .getOwner());
            logger.debug("Get the most nodes matching the current selection ");
            boolean firstLoop = true;
            do {
                if (!firstLoop) {
                    //if bagOfTasks is not empty
                    if (!bagOfTasks.isEmpty()) {
                        etd = bagOfTasks.removeFirst();
                        ((EligibleTaskDescriptorImpl) etd).addAttempt();
                        currentJob = core.jobs.get(etd.getJobId());
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
     * @param neededResourcesNumber the number of resources to ask for (must be > 0).
     * @param tasksToSchedule the task to be scheduled
     * @return A nodeSet that contains at most 'neededResourcesNumber' available compatible resources.
     * 		   An empty nodeSet if no nodes could be found
     * 		   null if the their was an exception when asking for the nodes (ie : selection script has failed)
     */
    protected NodeSet getRMNodes(int neededResourcesNumber, LinkedList<EligibleTaskDescriptor> tasksToSchedule) {
        NodeSet nodeSet = new NodeSet();

        if (neededResourcesNumber <= 0) {
            throw new IllegalArgumentException("Args 'neededResourcesNumber' must be > 0");
        }

        EligibleTaskDescriptor etd = tasksToSchedule.getFirst();
        InternalJob currentJob = core.jobs.get(etd.getJobId());
        InternalTask internalTask = currentJob.getIHMTasks().get(etd.getTaskId());

        if (logger.isDebugEnabled()) {
            SchedulingTaskComparator referent = new SchedulingTaskComparator(internalTask, currentJob
                    .getOwner());
            logger.debug("Referent task         : " + internalTask.getId());
            logger.debug("Selection script(s)   : " +
                ((referent.getSsHashCode() == 0) ? "no" : "yes (" + referent.getSsHashCode() + ")"));
            logger.debug("Node(s) exclusion     : " + internalTask.getNodeExclusion());
        }

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
                nodeSet = core.rmProxiesManager.getUserRMProxy(currentJob.getOwner(),
                        currentJob.getCredentials()).getNodes(neededResourcesNumber, descriptor,
                        internalTask.getSelectionScripts(), internalTask.getNodeExclusion(), bestEffort);
            } catch (TopologyDisabledException tde) {
                logger.info("Cancel job " + currentJob.getName() + " as the topology is disabled");
                simulateJobStartAndCancelIt(tasksToSchedule, "Topology is disabled");
                return null;
            }
            //the following line is used to unwrap the future, warning when moving or removing
            //it may also throw a ScriptException which is a RuntimeException
            PAFuture.waitFor(nodeSet, true);
            logger.debug("Got " + nodeSet.size() + " node(s)");
            return nodeSet;
        } catch (ScriptException e) {
            Throwable t = e;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            logger.info("Selection script throws an exception : " + t);
            logger.debug("", t);
            //simulate jobs starts and cancel it
            simulateJobStartAndCancelIt(tasksToSchedule, "Selection script has failed : " +
                Formatter.stackTraceToString(t));
            //leave the method by ss failure
            return null;
        } catch (RMProxyCreationException e) {
            logger.info("Failed to create User RM Proxy : " + e.getMessage());
            logger.debug("", e);
            //simulate jobs starts and cancel it
            simulateJobStartAndCancelIt(tasksToSchedule,
                    "Failed to create User RM Proxy : Authentication Failed to Resource Manager for user '" +
                        currentJob.getOwner() + "'");
            //leave the method by ss failure
            return null;
        }
    }

    /**
     * simulate jobs starts and cancel it
     */
    private void simulateJobStartAndCancelIt(LinkedList<EligibleTaskDescriptor> tasksToSchedule,
            String errorMsg) {
        Set<InternalJob> alreadyDone = new HashSet<InternalJob>();
        for (EligibleTaskDescriptor eltd : tasksToSchedule) {
            InternalJob ij = core.jobs.get(eltd.getJobId());
            InternalTask it = ij.getIHMTasks().get(eltd.getTaskId());
            if (alreadyDone.contains(ij)) {
                continue;
            } else {
                alreadyDone.add(ij);
            }
            // set the different informations on job if it is the first task of this job
            if (ij.getStartTime() < 0) {
                ij.start();
                core.pendingJobs.remove(ij);
                core.runningJobs.add(ij);
                //update tasks events list and send it to front-end
                core.updateTaskInfosList(ij, SchedulerEvent.JOB_PENDING_TO_RUNNING);
                logger.info("Job '" + ij.getId() + "' started");
            }
            //selection script has failed : end the job
            core.endJob(ij, it, null, errorMsg, JobStatus.CANCELED);
        }
    }

    /**
     * Load and initialize the task to be started
     *
     * @param job the job owning the task to be initialized
     * @param task the task to be initialized
     */
    protected void loadAndInit(InternalJob job, InternalTask task) {
        logger.debug("Load and Initialize the executable container for task '" + task.getId() + "'");
        ExecutableContainer container = core.getDBManager().loadExecutableContainer(task);
        task.setExecutableContainer(container);

        ExecutableContainerInitializer eci = new ExecutableContainerInitializer();
        // TCS can be null for non-java task
        eci.setClassServer(core.getTaskClassServer(job.getId()));
        task.getExecutableContainer().init(eci);
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
            job.startDataSpaceApplication(core.dataSpaceNSStarter.getNamingService(), core.dataSpaceNSStarter
                    .getNamingServiceURL());

            //create launcher
            launcher = task.createLauncher(job, node);
            activeObjectCreationRetryTimeNumber = ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER;

            nodeSet.remove(0);

            NodeSet nodes = new NodeSet();
            try {
                //if topology is enabled and it is a multi task, give every nodes to the multi-nodes task
                // we will need to update this code once topology will be allowed for single-node task
                if (task.isParallel()) {
                    nodes = new NodeSet(nodeSet);
                    task.getExecuterInformations().addNodes(nodes);
                    nodeSet.clear();
                }

                // activate loggers for this task if needed
                if (core.jobsToBeLogged.containsKey(job.getId())) {
                    launcher.activateLogs(core.lfs.getAppenderProvider());
                }

                // Set to empty array to emulate varargs behavior (i.e. not defined is
                // equivalent to empty array, not null.
                TaskResult[] params = new TaskResult[0];
                //if job is TASKSFLOW, preparing the list of parameters for this task.
                int resultSize = taskDescriptor.getParents().size();
                if ((job.getType() == JobType.TASKSFLOW) && (resultSize > 0) && task.handleResultsArguments()) {
                    params = new TaskResult[resultSize];
                    List<TaskId> parentIds = new ArrayList<TaskId>(resultSize);
                    for (int i = 0; i < resultSize; i++) {
                        parentIds.add(taskDescriptor.getParents().get(i).getTaskId());
                    }
                    Map<TaskId, TaskResult> taskResults = core.getDBManager().loadTasksResults(job.getId(),
                            parentIds);
                    for (int i = 0; i < resultSize; i++) {
                        params[i] = taskResults.get(taskDescriptor.getParents().get(i).getTaskId());
                    }
                }

                //set nodes in the executable container
                task.getExecutableContainer().setNodes(nodes);

                logger.info("Starting deployment of task '" + task.getName() + "' for job '" + job.getId() +
                    "'");

                finalizeStarting(job, task, node, launcher);

                threadPool.submitWithTimeout(new TimedDoTaskAction(job, task, launcher, core,
                    (SchedulerCore) PAActiveObject.getStubOnThis(), params, corePrivateKey),
                        DOTASK_ACTION_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (Exception t) {
                try {
                    //if there was a problem, free nodeSet for multi-nodes task
                    nodes.add(node);
                    core.rmProxiesManager.getUserRMProxy(job.getOwner(), job.getCredentials()).releaseNodes(
                            nodes);
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
        logger.info("Task '" + task.getId() + "' started on " +
            node.getNodeInformation().getVMInformation().getHostName() + "(node: " +
            node.getNodeInformation().getName() + ")");
        // set the different informations on job

        boolean firstTaskStarted;

        if (job.getStartTime() < 0) {
            // if it is the first task of this job
            job.start();
            core.pendingJobs.remove(job);
            core.runningJobs.add(job);
            //update tasks events list and send it to front-end
            core.updateTaskInfosList(job, SchedulerEvent.JOB_PENDING_TO_RUNNING);
            logger.info("Job '" + job.getId() + "' started");

            firstTaskStarted = true;
        } else {
            firstTaskStarted = false;
        }

        // set the different informations on task
        job.startTask(task);

        core.getDBManager().jobTaskStarted(job, task, firstTaskStarted);

        //set this task as started
        core.currentlyRunningTasks.get(task.getJobId()).put(task.getId(), launcher);
        // send task event to front-end
        core.frontend.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
            SchedulerEvent.TASK_PENDING_TO_RUNNING, task.getTaskInfo()));
        //fill previous task progress with 0, means task has started
        task.setProgress(0);
    }
}
