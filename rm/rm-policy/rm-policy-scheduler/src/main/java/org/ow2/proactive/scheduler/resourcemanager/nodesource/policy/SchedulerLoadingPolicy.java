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
package org.ow2.proactive.scheduler.resourcemanager.nodesource.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedRequestException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.Policy;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


@ActiveObject
public class SchedulerLoadingPolicy extends SchedulerAwarePolicy implements InitActive, RunActive, RMEventListener {

    protected static Logger logger = Logger.getLogger(SchedulerLoadingPolicy.class);

    private Map<JobId, Integer> activeTasks;

    private int activeTask = 0;

    @Configurable(description = "refresh frequency (ms)")
    private int refreshTime = 1000;

    @Configurable
    private int minNodes = 0;

    @Configurable
    private int maxNodes = 10;

    @Configurable(description = "number of tasks per node")
    private int loadFactor = 10;

    @Configurable()
    protected int nodeDeploymentTimeout = 10000;

    // policy state
    private boolean active = false;

    private SchedulerLoadingPolicy thisStub;

    protected int nodesNumberInNodeSource = 0;

    private int nodesNumberInRM = 0;

    private String nodeSourceName = null;

    // positive when deploying, negative when removing, zero when idle 
    protected long timeStamp = 0;

    public SchedulerLoadingPolicy() {
    }

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     */
    @Override
    public BooleanWrapper configure(Object... policyParameters) {
        super.configure(policyParameters);

        try {
            activeTasks = new HashMap<>();

            int index = 4;
            refreshTime = Integer.parseInt(policyParameters[index++].toString());
            minNodes = Integer.parseInt(policyParameters[index++].toString());
            maxNodes = Integer.parseInt(policyParameters[index++].toString());
            loadFactor = Integer.parseInt(policyParameters[index++].toString());
            nodeDeploymentTimeout = Integer.parseInt(policyParameters[index++].toString());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }
        return new BooleanWrapper(true);
    }

    public void initActivity(Body body) {
        thisStub = (SchedulerLoadingPolicy) PAActiveObject.getStubOnThis();
    }

    public void runActivity(Body body) {
        Service service = new Service(body);

        long timeStamp = System.currentTimeMillis();
        long delta = 0;

        // recalculating nodes number only once per policy period
        while (body.isActive()) {

            try {
                service.blockingServeOldest(refreshTime);
                delta += System.currentTimeMillis() - timeStamp;
                timeStamp = System.currentTimeMillis();

                if (delta > refreshTime) {
                    if (active && nodeSource != null) {
                        try {
                            updateNumberOfNodes();
                        } catch (BodyTerminatedRequestException e) {
                        }
                    }
                    delta = 0;
                }
            } catch (InterruptedException e) {
                logger.warn("runActivity interrupted", e);
            }
        }
    }

    @Override
    public BooleanWrapper activate() {
        BooleanWrapper activationStatus = super.activate();
        if (!activationStatus.getBooleanValue()) {
            return activationStatus;
        }

        for (JobState js : state.getPendingJobs()) {
            int nodesForThisJob = this.computeRequiredNodesForPendingJob(js);
            activeTask += nodesForThisJob;
            activeTasks.put(js.getId(), nodesForThisJob);
        }

        for (JobState js : state.getRunningJobs()) {
            int nodesForThisJob = this.computeRequiredNodesForRunningJob(js);
            activeTask += nodesForThisJob;
            activeTasks.put(js.getId(), nodesForThisJob);
        }

        nodeSourceName = nodeSource.getName();

        thisStub.registerRMListener();

        logger.debug("Policy activated. Current number of tasks " + activeTask);
        return new BooleanWrapper(true);
    }

    protected void updateNumberOfNodes() {
        logger.debug("Refreshing policy state: " + nodesNumberInNodeSource + " nodes in node source, " +
                     nodesNumberInRM + " nodes in RM");

        if (timeStamp > 0) {
            logger.debug("Pending node deployment request");
            // pending node deployment
            if (System.currentTimeMillis() - timeStamp > nodeDeploymentTimeout) {
                logger.debug("Node deployment timeout.");
                timeStamp = 0;
            }
        }

        if (timeStamp != 0) {
            if (timeStamp < 0) {
                logger.debug("Pending node removal request");
            }
            return;
        }

        if (nodesNumberInNodeSource < minNodes) {
            logger.debug("Node deployment request");
            timeStamp = System.currentTimeMillis();
            acquireNodes(minNodes - nodesNumberInNodeSource, new HashMap<String, Object>());
            return;
        }

        if (nodesNumberInNodeSource > maxNodes) {
            logger.debug("Node removal request");
            timeStamp = -System.currentTimeMillis();
            removeNode();
            return;
        }

        int requiredNodesNumber = activeTask / loadFactor + (activeTask % loadFactor == 0 ? 0 : 1);
        logger.debug("Required node number according to scheduler loading " + requiredNodesNumber);
        System.out.println(" required Nodes Number= " + requiredNodesNumber + " nodes Number In RM= " +
                           nodesNumberInRM + " nodesNumberInNodeSource " + nodesNumberInNodeSource);
        if (requiredNodesNumber > nodesNumberInRM && nodesNumberInNodeSource < maxNodes) {
            logger.debug("Node deployment request");
            timeStamp = System.currentTimeMillis();
            acquireNodes(requiredNodesNumber, new HashMap<String, Object>());
            return;
        }

        if (requiredNodesNumber < nodesNumberInRM && nodesNumberInNodeSource > minNodes) {
            logger.debug("Node removal request");
            timeStamp = -System.currentTimeMillis();
            removeNode();
            return;
        }
    }

    /**
     * Too many nodes are held by the NodeSource,
     * remove one node
     */
    protected void removeNode() {
        removeNodes(1, false);
    }

    @Override
    protected SchedulerEvent[] getEventsList() {
        return new SchedulerEvent[] { SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.JOB_SUBMITTED,
                                      SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.JOB_PENDING_TO_FINISHED };
    }

    @Override
    protected SchedulerEventListener getSchedulerListener() {
        return thisStub;
    }

    protected void registerRMListener() {
        RMInitialState state = nodeSource.getRMCore()
                                         .getMonitoring()
                                         .addRMEventListener((RMEventListener) PAActiveObject.getStubOnThis());
        for (RMNodeEvent event : state.getNodesEvents()) {
            //we only count "useable" nodes
            if (checkNodeStates(event)) {
                nodesNumberInRM++;
            }
        }
        logger.debug("RM listener successully registered. RM node number is " + nodesNumberInRM);
        active = true;
    }

    @Override
    public String getDescription() {
        return "Allocates as many resources as scheduler required according\nto loading factor. Releases resources smoothly.";
    }

    @Override
    public String toString() {
        return super.toString() + " [Max Nodes: " + maxNodes + " Min Nodes: " + minNodes + " Job Per Node: " +
               loadFactor + " Refresh period " + refreshTime + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void jobSubmittedEvent(JobState jobState) {
        //computing the required number of nodes regarding tasks' parallel environment
        int nodesForThisJob = this.computeRequiredNodesForPendingJob(jobState);
        int nodesToStartThisJob = this.computeRequiredNodesToStartPendingJob(jobState);
        activeTasks.put(jobState.getId(), nodesForThisJob);
        activeTask += nodesForThisJob;
        logger.debug("Job submitted. Current number of tasks " + activeTask);
        System.out.println("Job submitted. Current number of tasks " + activeTask + " nodesForStartingThisJob=" +
                           nodesToStartThisJob);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        switch (notification.getEventType()) {
            case JOB_RUNNING_TO_FINISHED:
            case JOB_PENDING_TO_FINISHED:
                int tasksLeft = activeTasks.remove(notification.getData().getJobId());
                activeTask -= tasksLeft;
                break;
            case TASK_REPLICATED:
            case TASK_SKIPPED:
                JobId jid = notification.getData().getJobId();
                //need to get an up to date job view from the scheduler
                //computing the required number of nodes from 0...
                JobState jobState = null;
                try {
                    jobState = this.scheduler.getJobState(jid);
                } catch (Exception e) {
                    logger.error("Cannot update the " + this.getClass().getSimpleName() +
                                 " state as an exception occured", e);
                    break;
                }
                int nodesForThisTask = this.computeRequiredNodesForRunningJob(jobState);
                int oldActiveTask = activeTasks.get(jid);
                activeTasks.put(jid, nodesForThisTask);
                activeTask += (nodesForThisTask - oldActiveTask);
                logger.debug("Tasks replicated. Current number of tasks " + activeTask);
                break;
        }
    }

    public void rmEvent(RMEvent event) {
    }

    public void nodeSourceEvent(RMNodeSourceEvent event) {
    }

    public void nodeEvent(RMNodeEvent event) {
        switch (event.getEventType()) {
            case NODE_ADDED:
                //if node addition is related to pending nodes/down nodes
                //we discard the computation
                if (!checkNodeStates(event)) {
                    break;
                }
                nodesNumberInRM++;
                if (event.getNodeSource().equals(nodeSourceName)) {
                    nodesNumberInNodeSource++;

                    if (timeStamp > 0) {
                        logger.debug("Requested node arrived " + event.getNodeUrl());
                        timeStamp = 0;
                    }
                    if (timeStamp < 0) {
                        logger.debug("Waiting for node to be removed but new node arrived " + event.getNodeUrl());
                    }
                }
                break;
            case NODE_REMOVED:
                //we only care about non pending nodes
                if (event.getNodeState() == NodeState.LOST || event.getNodeState() == NodeState.DEPLOYING) {
                    break;
                }
                nodesNumberInRM--;
                if (event.getNodeSource().equals(nodeSourceName)) {
                    nodesNumberInNodeSource--;

                    if (timeStamp > 0) {
                        logger.debug("Waiting for node to be acquired but the node " + event.getNodeUrl() + " removed");
                    }
                    if (timeStamp < 0) {
                        logger.debug("Requested node removed " + event.getNodeUrl());
                        timeStamp = 0;
                    }
                }

                break;
        }
    }

    private boolean checkNodeStates(RMNodeEvent event) {
        NodeState ns = event.getNodeState();
        if (ns == NodeState.LOST || ns == NodeState.DEPLOYING || ns == NodeState.DOWN) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
       // System.out.println("Event type= " + notification.getEventType() + computeRequiredNodesForEligibleTask());
        switch (notification.getEventType()) {
            case TASK_RUNNING_TO_FINISHED:
                JobId id = notification.getData().getJobId();
                if (activeTasks.containsKey(id)) {
                    JobState jobState = null;
                    int nodesForThisTask = 0;
                    try {
                        jobState = this.scheduler.getJobState(id);
                        TaskState taskState = jobState.getHMTasks().get(notification.getData().getTaskId());
                        if (taskState.isParallel()) {
                            nodesForThisTask = taskState.getParallelEnvironment().getNodesNumber();
                        } else {
                            nodesForThisTask = 1;
                        }
                    } catch (Exception e) {
                        logger.error("Cannot update " + this.getClass().getSimpleName() +
                                     "'s state because of an exception.", e);
                        break;
                    }
                    activeTasks.put(id, activeTasks.get(id) - nodesForThisTask);
                    activeTask -= nodesForThisTask;
                    logger.debug("Task finished. Current number of tasks " + activeTask);
                } else {
                    logger.error("Unknown job id " + id);
                }
                break;
            case TASK_PENDING_TO_RUNNING:
                JobId id1 = notification.getData().getJobId();
                if (activeTasks.containsKey(id1)) {
                    JobState jobState = null;
                    int nodesForThisTask = 0;
                    try {
                        jobState = this.scheduler.getJobState(id1);
                        TaskState taskState = jobState.getHMTasks().get(notification.getData().getTaskId());
                        System.out.println("Task running id " + taskState.getId());
                        if (taskState.isParallel()) {
                            nodesForThisTask = taskState.getParallelEnvironment().getNodesNumber();
                        } else {
                            nodesForThisTask = 1;
                        }
                    } catch (Exception e) {
                        logger.error("Cannot update " + this.getClass().getSimpleName() +
                                "'s state because of an exception.", e);
                        break;
                    }
                    activeTasks.put(id1, activeTasks.get(id1) + nodesForThisTask);
                    activeTask += nodesForThisTask;
                    logger.debug("Task is running. Current number of tasks " + activeTask);
                } else {
                    logger.error("Unknown job id " + id1);
                }
                break;
        }
    }

    /**
     * Returns the required number of nodes for a pending job
     * @param jobState
     * @return Returns the required number of nodes for a pending job
     */
    private int computeRequiredNodesForPendingJob(JobState jobState) {
        int nodesForThisJob = 0;
        for (TaskState taskState : jobState.getTasks()) {
            if (taskState.isParallel()) {
                nodesForThisJob += taskState.getParallelEnvironment().getNodesNumber();
            } else {
                nodesForThisJob++;
            }
        }
        return nodesForThisJob;
    }

    /**
     * Returns the required number of nodes to start a pending job
     * @param jobState
     * @return Returns the minimum required number of nodes to start a pending job
     */
    private int computeRequiredNodesToStartPendingJob(JobState jobState) {
        int nodesToStartThisJob = 0;
        TaskState taskState = jobState.getTasks().get(0);
        if (taskState.isParallel()) {
            nodesToStartThisJob += taskState.getParallelEnvironment().getNodesNumber();
        } else {
            nodesToStartThisJob++;
        }

        return nodesToStartThisJob;
    }

    /**
     * Returns the required number of nodes for a running job
     * @param jobState
     * @return the required number of nodes for a running job
     */
    private int computeRequiredNodesForRunningJob(JobState jobState) {
        int nodesForThisJob = 0;
        for (TaskState taskState : jobState.getTasks()) {
            if (TaskStatus.PENDING.equals(taskState.getStatus()) || TaskStatus.RUNNING.equals(taskState.getStatus())) {
                if (taskState.isParallel()) {
                    nodesForThisJob += taskState.getParallelEnvironment().getNodesNumber();
                } else {
                    nodesForThisJob++;
                }
            }
        }
        return nodesForThisJob;
    }

    /**
     * Returns the required number of nodes for eligible tasks
     * @return the required number of nodes for eligible tasks
     */

    private int computeRequiredNodesForEligibleTask() throws NotConnectedException, PermissionException {
        int nodesForEligibleTask = 0;
        String currentPolicy = scheduler.getCurrentPolicy();
        Policy po = null;
        try {
            po = (Policy) Class.forName(currentPolicy).newInstance();
        } catch (Exception e) {
            logger.error("Error Loading Current Policy:", e);
        }
        Map<JobId, JobDescriptor> jobMap = scheduler.getJobsToSchedule();
        if (jobMap.isEmpty()) {
            return nodesForEligibleTask;
        }
        List<JobDescriptor> descriptors = new ArrayList<>(jobMap.values());
        // ask the policy all the tasks to be schedule according to the jobs list.
        //System.out.println(jobMap.size() + "  " + po.toString());
        LinkedList<EligibleTaskDescriptor> taskRetrievedFromPolicy = po.getOrderedTasks(descriptors);

        //if there is no task to scheduled
        if (taskRetrievedFromPolicy == null || taskRetrievedFromPolicy.isEmpty()) {
            return nodesForEligibleTask;

        }

        EligibleTaskDescriptor etd = taskRetrievedFromPolicy.removeFirst();
        InternalJob currentJob = jobMap.get(etd.getJobId()).getInternal();
        InternalTask internalTask = currentJob.getIHMTasks().get(etd.getTaskId());
        nodesForEligibleTask = internalTask.getNumberOfNodesNeeded();

        /*
         * for (EligibleTaskDescriptor etd : taskRetrievedFromPolicy) {
         * InternalJob currentJob = jobMap.get(etd.getJobId()).getInternal();
         * InternalTask internalTask = currentJob.getIHMTasks().get(etd.getTaskId());
         * nodesForEligibleTasks += internalTask.getNumberOfNodesNeeded();
         * }
         */
        // System.out.println("taskRetrievedFromPolicy= " + taskRetrievedFromPolicy.size());
        return nodesForEligibleTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(Client initiator) {
        active = false;
        super.shutdown(initiator);
    }

}
