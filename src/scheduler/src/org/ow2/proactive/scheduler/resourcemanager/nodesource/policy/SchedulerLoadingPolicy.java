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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.resourcemanager.nodesource.policy;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedRequestException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;


public class SchedulerLoadingPolicy extends SchedulerAwarePolicy implements InitActive, RunActive,
        RMEventListener {

    /**
     * 
     */
    private static final long serialVersionUID = 30L;

    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.POLICY);

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
    private int nodesNumberInNodeSource = 0;
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
            activeTasks = new HashMap<JobId, Integer>();

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

    private void updateNumberOfNodes() {
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
            acquireNodes(1);
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

        if (requiredNodesNumber > nodesNumberInRM && nodesNumberInNodeSource < maxNodes) {
            logger.debug("Node deployment request");
            timeStamp = System.currentTimeMillis();
            acquireNodes(1);
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
        RMInitialState state = nodeSource.getRMCore().getMonitoring().addRMEventListener(
                (RMEventListener) PAActiveObject.getStubOnThis());
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
        return NamesConvertor.beautifyName(this.getClass().getSimpleName()) + " [Max Nodes: " + maxNodes +
            " Min Nodes: " + minNodes + " Job Per Node: " + loadFactor + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void jobSubmittedEvent(JobState jobState) {
        //computing the required number of nodes regarding tasks' parallel environment
        int nodesForThisJob = this.computeRequiredNodesForPendingJob(jobState);
        activeTasks.put(jobState.getId(), nodesForThisJob);
        activeTask += nodesForThisJob;
        logger.debug("Job submitted. Current number of tasks " + activeTask);
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
                        logger.debug("Waiting for node to be removed but new node arrived " +
                            event.getNodeUrl());
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
                        logger.debug("Waiting for node to be acquired but the node " + event.getNodeUrl() +
                            " removed");
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
     * Returns the required number of nodes for a running job
     * @param jobState
     * @return the required number of nodes for a running job
     */
    private int computeRequiredNodesForRunningJob(JobState jobState) {
        int nodesForThisJob = 0;
        for (TaskState taskState : jobState.getTasks()) {
            if (TaskStatus.PENDING.equals(taskState.getStatus()) ||
                TaskStatus.RUNNING.equals(taskState.getStatus())) {
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
     * {@inheritDoc}
     */
    @Override
    public void shutdown(Client initiator) {
        active = false;
        super.shutdown(initiator);
    }

}
