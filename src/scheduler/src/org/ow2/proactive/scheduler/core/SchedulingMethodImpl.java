/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyDisabledException;
import org.ow2.proactive.resourcemanager.frontend.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.JobDescriptor;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.common.task.TaskDescriptor;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.db.DatabaseManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxyCreationException;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.task.ExecutableContainerInitializer;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.scripting.ScriptException;
import org.ow2.proactive.threading.ExecutorServiceTasksInvocator;
import org.ow2.proactive.utils.NodeSet;

import edu.emory.mathcs.backport.java.util.Collections;


/**
 * SchedulingMethodImpl is the default implementation for the scheduling process
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
final class SchedulingMethodImpl implements SchedulingMethod {

    /** Scheduler logger */
    protected static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.SCHEDULE);
    protected static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.SCHEDULE);

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

    protected ExecutorService threadPool;

    protected PrivateKey corePrivateKey;

    protected TopologyDescriptor tdescriptor = null;

    protected boolean topologyDisabled = false;

    SchedulingMethodImpl(SchedulerCore core) {
        this.core = core;
        this.threadPool = Executors.newFixedThreadPool(DOTASK_ACTION_THREADNUMBER);
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
        LinkedList<EligibleTaskDescriptor> taskRetrivedFromPolicy = new LinkedList<EligibleTaskDescriptor>(
            core.policy.getOrderedTasks(jobDescriptorList));

        //if there is no task to scheduled, return
        if (taskRetrivedFromPolicy == null || taskRetrivedFromPolicy.size() == 0) {
            return numberOfTaskStarted;
        }

        logger_dev.info("Number of tasks ready to be scheduled : " + taskRetrivedFromPolicy.size());

        while (!taskRetrivedFromPolicy.isEmpty()) {
            //get rmState and update it in scheduling policy
            RMState rmState = null;
            try {
                rmState = core.rmProxiesManager.getSchedulerRMProxy().getState();
            } catch (RMProxyCreationException rmpce) {
                logger_dev.error("", rmpce);
                break;
            }
            core.policy.RMState = rmState;
            int freeResourcesNb = rmState.getFreeNodesNumber();
            logger_dev.info("Number of free resources : " + freeResourcesNb);
            //if there is no free resources, stop it right now
            if (freeResourcesNb == 0) {
                break;
            }

            //get the next compatible tasks from the whole returned policy tasks
            LinkedList<EligibleTaskDescriptor> tasksToSchedule = new LinkedList<EligibleTaskDescriptor>();
            int neededResourcesNumber = getNextcompatibleTasks(taskRetrivedFromPolicy, freeResourcesNb,
                    tasksToSchedule);
            logger.debug("Number of nodes to ask for : " + neededResourcesNumber);
            if (neededResourcesNumber == 0) {
                break;
            }

            //ask nodes to the RM, fail tasks and jobs if selection script fails (tasks could never be started)
            NodeSet nodeSet = getRMNodes(neededResourcesNumber, tasksToSchedule);

            //start selected tasks
            Node node = null;
            InternalJob currentJob = null;
            try {
                while (nodeSet != null && !nodeSet.isEmpty()) {
                    EligibleTaskDescriptor taskDescriptor = tasksToSchedule.removeFirst();
                    currentJob = core.jobs.get(taskDescriptor.getJobId());
                    InternalTask internalTask = currentJob.getIHMTasks().get(taskDescriptor.getId());

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
                            core.rmProxiesManager.getUserRMProxy(currentJob).releaseNodes(nodeSet);
                        }
                        //and leave the loop
                        break;
                    }
                }
            } catch (ActiveObjectCreationException e1) {
                //Something goes wrong with the active object creation (createLauncher)
                logger.warn("", e1);
                //so try to get back every remaining nodes to the resource manager
                try {
                    core.rmProxiesManager.getUserRMProxy(currentJob).releaseNodes(nodeSet);
                } catch (Exception e2) {
                    logger_dev.info("Unable to get back the nodeSet to the RM", e2);
                }
                if (--activeObjectCreationRetryTimeNumber == 0) {
                    return numberOfTaskStarted;
                }
            } catch (Exception e1) {
                //if we are here, it is that something append while launching the current task.
                logger.warn("", e1);
                //so try to get back every remaining nodes to the resource manager
                try {
                    core.rmProxiesManager.getUserRMProxy(currentJob).releaseNodes(nodeSet);
                } catch (Exception e2) {
                    logger_dev.info("Unable to get back the nodeSet to the RM", e2);
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
            if (j.getJobDescriptor().getEligibleTasks().size() > 0) {
                list.add(j.getJobDescriptor());
            }
        }

        //if scheduler is not paused, add pending jobs
        if (core.status != SchedulerStatus.PAUSED) {
            for (InternalJob j : core.pendingJobs) {
                if (j.getJobDescriptor().getEligibleTasks().size() > 0) {
                    list.add(j.getJobDescriptor());
                }
            }
        }

        if (list.size() > 0) {
            logger_dev.info("Number of jobs containing tasks to be scheduled : " + list.size());
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
            InternalJob currentJob = core.jobs.get(etd.getJobId());
            InternalTask internalTask = currentJob.getIHMTasks().get(etd.getId());
            int neededNodes = internalTask.getNumberOfNodesNeeded();
            SchedulingTaskComparator referent = new SchedulingTaskComparator(internalTask, currentJob
                    .getOwner(), tdescriptor);
            logger_dev.debug("Get the most nodes matching the current selection");
            boolean firstLoop = true;
            do {
                if (!firstLoop) {
                    //if bagOfTasks is not empty
                    if (!bagOfTasks.isEmpty()) {
                        etd = bagOfTasks.removeFirst();
                        currentJob = core.jobs.get(etd.getJobId());
                        internalTask = currentJob.getIHMTasks().get(etd.getId());
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
                    if (referent.equals(new SchedulingTaskComparator(internalTask, currentJob.getOwner(),
                        tdescriptor))) {
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
        InternalTask internalTask = currentJob.getIHMTasks().get(etd.getId());

        if (logger.isDebugEnabled()) {
            SchedulingTaskComparator referent = new SchedulingTaskComparator(internalTask, currentJob
                    .getOwner(), null);
            logger.debug("Referent task         : " + internalTask.getId());
            logger.debug("Selection script(s)   : " +
                ((referent.getSsHashCode() == 0) ? "no" : "yes (" + referent.getSsHashCode() + ")"));
            logger.debug("Node(s) exclusion     : " + internalTask.getNodeExclusion());
        }

        try {
            //apply topology if number of resource demanded is more than one
            //if multinode is demanded, every following internal task have more than one task
            //if simple node task is demanded, every internal task have only one node
            if (!isTopologyDisabled() && internalTask.getNumberOfNodesNeeded() > 1) {
                tdescriptor = TopologyDescriptor.BEST_PROXIMITY;
            } else {
                tdescriptor = null;
            }
            try {
                nodeSet = core.rmProxiesManager.getUserRMProxy(currentJob).getAtMostNodes(
                        neededResourcesNumber, tdescriptor, internalTask.getSelectionScripts(),
                        internalTask.getNodeExclusion());
            } catch (TopologyDisabledException tde) {
                //TopologyDisabledException exception is runtimeException
                logger_dev
                        .info("UNARBITRARY Topology was demanded and topology is disabled : switch to ARBITRARY");
                setTopologyDisabled(true);
                tdescriptor = null;
                nodeSet = core.rmProxiesManager.getUserRMProxy(currentJob).getAtMostNodes(
                        neededResourcesNumber, tdescriptor, internalTask.getSelectionScripts(),
                        internalTask.getNodeExclusion());
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
            logger_dev.info("Selection script throws an exception : " + t);
            logger_dev.debug("", t);
            //simulate jobs starts and cancel it
            simulateJobStartAndCancelIt(tasksToSchedule, "Selection script has failed : " + t);
            //leave the method by ss failure
            return null;
        } catch (RMProxyCreationException e) {
            logger_dev.info("Failed to create User RM Proxy : " + e.getMessage());
            logger_dev.debug("", e);
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
            InternalTask it = ij.getIHMTasks().get(eltd.getId());
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
            core.endJob(ij, it, errorMsg, JobStatus.CANCELED);
        }
    }

    /**
     * Load and initialize the task to be started
     *
     * @param job the job owning the task to be initialized
     * @param task the task to be initialized
     */
    protected void loadAndInit(InternalJob job, InternalTask task) {
        logger_dev.debug("Load and Initialize the executable container for task '" + task.getId() + "'");
        DatabaseManager.getInstance().load(task);
        ExecutableContainerInitializer eci = new ExecutableContainerInitializer();
        // TCS can be null for non-java task
        eci.setClassServer(SchedulerCore.getTaskClassServer(job.getId()));
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
    @SuppressWarnings("unchecked")
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
                if (!isTopologyDisabled() && task.getNumberOfNodesNeeded() > 1) {
                    nodes = new NodeSet(nodeSet);
                } else {
                    //else distribute normally
                    for (int i = 0; i < (task.getNumberOfNodesNeeded() - 1); i++) {
                        nodes.add(nodeSet.remove(0));
                    }
                }
                task.getExecuterInformations().addNodes(nodes);

                // activate loggers for this task if needed
                if (core.jobsToBeLogged.containsKey(job.getId()) ||
                    core.jobsToBeLoggedinAFile.containsKey(job.getId())) {
                    launcher.activateLogs(core.lfs.getAppenderProvider());
                }

                // Set to empty array to emulate varargs behavior (i.e. not defined is
                // equivalent to empty array, not null.
                TaskResult[] params = new TaskResult[0];
                //if job is TASKSFLOW, preparing the list of parameters for this task.
                int resultSize = taskDescriptor.getParents().size();
                if ((job.getType() == JobType.TASKSFLOW) && (resultSize > 0) && task.handleResultsArguments()) {
                    params = new TaskResult[resultSize];
                    for (int i = 0; i < resultSize; i++) {
                        //get parent task number i
                        InternalTask parentTask = job.getIHMTasks().get(
                                taskDescriptor.getParents().get(i).getId());
                        //set the task result in the arguments array.
                        params[i] = job.getJobResult().getResult(parentTask.getName());
                        //if this result has been unloaded, (extremely rare but possible)
                        if (params[i].getOutput() == null) {
                            //get the result and load the content from database
                            DatabaseManager.getInstance().load(params[i]);
                        }
                    }
                }

                //set nodes in the executable container
                task.getExecutableContainer().setNodes(nodes);

                logger_dev.info("Starting deployment of task '" + task.getName() + "' for job '" +
                    job.getId() + "'");

                //enqueue next instruction, and execute whole process in the thread-pool controller
                TimedDoTaskAction tdta = new TimedDoTaskAction(job, task, launcher,
                    (SchedulerCore) PAActiveObject.getStubOnThis(), params, corePrivateKey);
                List<Future<TaskResult>> futurResults = ExecutorServiceTasksInvocator
                        .invokeAllWithTimeoutAction(threadPool, Collections.singletonList(tdta),
                                DOTASK_ACTION_TIMEOUT);

                //wait for only one result
                Future<TaskResult> future = futurResults.get(0);
                if (future.isDone()) {
                    //if task has finished
                    if (future.get() != null) {
                        //and result is not null
                        ((JobResultImpl) job.getJobResult()).storeFuturResult(task.getName(), future.get());
                        //mark the task and job (if needed) as started and send events
                        finalizeStarting(job, task, node, launcher);
                    } else {
                        //if there was a problem, free nodeSet for multi-nodes task (1)
                        throw new RuntimeException("Free nodes 1");
                    }
                } else {
                    //if there was a problem, free nodeSet for multi-nodes task (2)
                    throw new RuntimeException("Free nodes 2");
                }

            } catch (Exception t) {
                try {
                    //if there was a problem, free nodeSet for multi-nodes task
                    //exception can come from future.get() -> cancellationException
                    //exception can also come from (1) or (2)
                    nodes.add(node);
                    core.rmProxiesManager.getUserRMProxy(job).releaseNodes(nodes);
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
            node.getNodeInformation().getVMInformation().getHostName());
        // set the different informations on job
        if (job.getStartTime() < 0) {
            // if it is the first task of this job
            job.start();
            core.pendingJobs.remove(job);
            core.runningJobs.add(job);
            //update tasks events list and send it to front-end
            core.updateTaskInfosList(job, SchedulerEvent.JOB_PENDING_TO_RUNNING);
            logger.info("Job '" + job.getId() + "' started");
        }

        // set the different informations on task
        job.startTask(task);
        //set this task as started
        core.currentlyRunningTasks.get(task.getJobId()).put(task.getId(), launcher);
        // send task event to front-end
        core.frontend.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
            SchedulerEvent.TASK_PENDING_TO_RUNNING, task.getTaskInfo()));
    }

    /**
     * Get the topologyDisabled
     *
     * @return the topologyDisabled
     */
    boolean isTopologyDisabled() {
        return topologyDisabled;
    }

    /**
     * Set the topologyDisabled value to the given topologyDisabled value
     *
     * @param topologyDisabled the topologyDisabled to set
     */
    public void setTopologyDisabled(boolean topologyDisabled) {
        this.topologyDisabled = topologyDisabled;
    }

}
