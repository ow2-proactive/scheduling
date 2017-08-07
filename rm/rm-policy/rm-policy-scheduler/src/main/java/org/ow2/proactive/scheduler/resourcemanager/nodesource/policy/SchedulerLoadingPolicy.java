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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.queue.CircularFifoQueue;
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
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;

import com.google.common.math.DoubleMath;


@ActiveObject
public class SchedulerLoadingPolicy extends SchedulerAwarePolicy implements InitActive, RunActive, RMEventListener {

    protected static Logger logger = Logger.getLogger(SchedulerLoadingPolicy.class);

    @Configurable(description = "refresh frequency (ms)")
    protected long refreshTime = 10000;

    @Configurable(description = "minimum number of nodes deployed")
    protected int minNodes = 0;

    @Configurable(description = "maximum number of nodes deployed")
    protected int maxNodes = 10;

    @Configurable(description = "number of tasks per node")
    protected int loadFactor = 10;

    @Configurable(description = "Number of refresh cycles used to memorize scheduler load. Actual load will be computed as an average")
    protected int refreshCycleWindow = 5;

    @Configurable(description = "paid instance duration in Milliseconds: time after which the instance has to be paid again. Default is 0 (not a paid instance, no delay). This parameter can be used for paid cloud instances such as Amazon, Azure, etc. Example for Amazon : 3600000 ms (1 hour)")
    protected long releaseDelay = 0;

    @Configurable(description = "nodes can be released if current time is in interval [releaseDelay - threshold, releaseDelay]. Default is 0 (not a paid instance, no delay). Related to releaseDelay. Example for amazon : 600000 ms (10 minutes)")
    protected long threshold = 0;

    /**
     * associates a Node URL with a acquisition time the time (as return by
     * System.currentTimeMillis()) is actually when it was registered in the RM,
     * not the VM startup in AWS accounting, which probably occurred ~2mn sooner
     */
    protected Map<String, Long> nodes = new LinkedHashMap<>();

    // policy state
    private boolean active = false;

    protected SchedulerLoadingPolicy thisStub;

    protected AtomicInteger nodesNumberInNodeSource = new AtomicInteger(0);

    protected AtomicInteger pendingAddedNodesNumberInNodeSource = new AtomicInteger(0);

    protected AtomicInteger pendingRemovedNodesNumberInNodeSource = new AtomicInteger(0);

    protected CircularFifoQueue<Integer> refreshCycleWindowQueue;

    protected String nodeSourceName = null;

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
            int index = 4;
            refreshTime = Long.parseLong(policyParameters[index++].toString());
            minNodes = Integer.parseInt(policyParameters[index++].toString());
            maxNodes = Integer.parseInt(policyParameters[index++].toString());
            loadFactor = Integer.parseInt(policyParameters[index++].toString());
            refreshCycleWindow = Integer.parseInt(policyParameters[index++].toString());

            refreshCycleWindowQueue = new CircularFifoQueue(refreshCycleWindow);

            releaseDelay = Long.parseLong(policyParameters[index++].toString());
            threshold = Long.parseLong(policyParameters[index++].toString());
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

        nodeSourceName = nodeSource.getName();

        thisStub.registerRMListener();
        logger.info("Policy activated.");
        return new BooleanWrapper(true);
    }

    /**
     * Compare the scheduler state which the current number of available nodes. Initiate requests to add new nodes or to remove existing nodes.
     */
    protected void updateNumberOfNodes() {
        logger.debug("Refreshing policy state: " + nodesNumberInNodeSource + " nodes in node source.");

        int consideredNumberOfAliveTasks = getConsideredNumberOfAliveTasks();

        int requiredNodesNumber = (int) Math.ceil((double) consideredNumberOfAliveTasks / loadFactor);

        logger.debug("Number of nodes required by computation: " + requiredNodesNumber);

        int estimateNodeSourceNodesNumber = nodesNumberInNodeSource.get() + pendingAddedNodesNumberInNodeSource.get() -
                                            pendingRemovedNodesNumberInNodeSource.get();

        logger.debug("Number of nodes currently in the node source: " + estimateNodeSourceNodesNumber);

        int estimateRMNodesNumber = getNumberOfAliveNodes() + pendingAddedNodesNumberInNodeSource.get() -
                                    pendingRemovedNodesNumberInNodeSource.get();

        logger.debug("Number of nodes currently in the rm: " + estimateRMNodesNumber);

        if (estimateNodeSourceNodesNumber < minNodes) {
            logger.info("Number of nodes in node source less than minimum");
            addNewNodes(minNodes - estimateNodeSourceNodesNumber);
            return;
        }

        if (estimateNodeSourceNodesNumber > maxNodes) {
            logger.info("Number of nodes in node source greater than maximum");
            removeSomeNodes(estimateNodeSourceNodesNumber - maxNodes);
            return;
        }

        int numberOfNodesToAdd = Math.min(maxNodes - estimateNodeSourceNodesNumber,
                                          requiredNodesNumber - estimateRMNodesNumber);

        if (numberOfNodesToAdd > 0) {
            logger.info("Number of nodes in RM less than required by computation");

            if (numberOfNodesToAdd > 0) {
                addNewNodes(Math.min(requiredNodesNumber, maxNodes) - estimateNodeSourceNodesNumber);
                return;
            }
        }

        int numberOfNodesToRemove = Math.min(estimateNodeSourceNodesNumber - minNodes,
                                             estimateRMNodesNumber - requiredNodesNumber);

        if (numberOfNodesToRemove > 0) {
            logger.info("Number of nodes in RM greater than required by computation");

            removeSomeNodes(numberOfNodesToRemove);
            return;
        }
    }

    /**
     * Returns the average number of alive tasks based on the window size to avoid creating and removing nodes too quickly
     */
    private int getConsideredNumberOfAliveTasks() {
        int totalNumberOfAliveTasks = 0;
        try {
            totalNumberOfAliveTasks = getTotalNumberOfAliveTasks();
        } catch (Exception e) {
            logger.error("Error in computing required Nodes for Eligible tasks:", e);
        }

        refreshCycleWindowQueue.add(totalNumberOfAliveTasks);

        return computeMean(refreshCycleWindowQueue);
    }

    private int computeMean(Collection<Integer> values) {
        return (int) Math.ceil(DoubleMath.mean(values));
    }

    private void removeSomeNodes(int numberToRemove) {
        logger.info("Nodes removal request: " + numberToRemove);
        int effectiveNumberToRemove = numberToRemove;
        pendingRemovedNodesNumberInNodeSource.addAndGet(numberToRemove);
        for (int i = 0; i < numberToRemove; i++) {
            boolean nodeRemoved = removeNode();
            if (!nodeRemoved) {
                effectiveNumberToRemove--;
                pendingRemovedNodesNumberInNodeSource.decrementAndGet();
            }
        }
        logger.info("Nodes actually removed: " + effectiveNumberToRemove);
    }

    private void addNewNodes(int numberOfNodesToAdd) {
        logger.info("Nodes deployment request: " + numberOfNodesToAdd);
        pendingAddedNodesNumberInNodeSource.addAndGet(numberOfNodesToAdd);
        acquireNodes(numberOfNodesToAdd, new HashMap<String, Object>());
    }

    /**
     * Remove a node present in the node source
     * @return
     */
    protected boolean removeNode() {
        String bestFree = null;
        String bestBusy = null;
        String bestDown = null;
        synchronized (nodes) {

            long t = System.currentTimeMillis();

            if (releaseDelay <= 0) {
                Iterator<Map.Entry<String, Long>> iterator = nodes.entrySet().iterator();
                if (!iterator.hasNext()) {
                    return false;
                }
                Map.Entry<String, Long> entry = iterator.next();
                iterator.remove();
                NodeState nodeState = nodeSource.getRMCore().getNodeState(entry.getKey());
                switch (nodeState) {
                    case FREE:
                    case BUSY:
                        removeNode(entry.getKey(), false);
                        return true;
                    case DOWN:
                    case LOST:
                        removeNode(entry.getKey(), false);
                        return false;
                    default:
                        return false;
                }
            }

            /*
             * A Node can be removed only if (minutes_since_acquisition % 60 < 10),
             * ie. we are in the last 10 minutes of the last paid hour Down nodes
             * are removed in priority, then free nodes, then busy nodes
             */

            for (Map.Entry<String, Long> node : nodes.entrySet()) {
                long rt = releaseDelay - ((t - node.getValue()) % releaseDelay);
                NodeState nodeState = null;
                try {
                    nodeState = nodeSource.getRMCore().getNodeState(node.getKey());
                } catch (Throwable exc) {
                    // pending / configuring
                    continue;
                }

                switch (nodeState) {
                    case BUSY:
                    case CONFIGURING:
                    case DEPLOYING:
                        if (rt <= threshold) {
                            bestBusy = node.getKey();
                        }
                        break;
                    case LOST:
                    case DOWN:
                        if (rt <= threshold) {
                            bestDown = node.getKey();
                        }
                        break;
                    case FREE:
                        if (rt <= threshold) {
                            bestFree = node.getKey();
                        }
                        break;
                }
            }

            if (bestDown != null) {
                removeNode(bestDown, false);
                this.nodes.remove(bestDown);
                // removing a down or lost node does not count, but we do this in priority
                return false;
            } else if (bestFree != null) {
                removeNode(bestFree, false);
                this.nodes.remove(bestFree);
                return true;
            } else if (bestBusy != null) {
                removeNode(bestBusy, false);
                this.nodes.remove(bestBusy);
                return true;
            } else {
                // no node can be removed, cancel request
                return false;
            }
        }

    }

    @Override
    protected SchedulerEvent[] getEventsList() {
        return new SchedulerEvent[] { SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.JOB_SUBMITTED,
                                      SchedulerEvent.TASK_PENDING_TO_RUNNING, SchedulerEvent.TASK_IN_ERROR_TO_FINISHED,
                                      SchedulerEvent.TASK_WAITING_FOR_RESTART, SchedulerEvent.TASK_RUNNING_TO_FINISHED,
                                      SchedulerEvent.JOB_PENDING_TO_FINISHED };
    }

    @Override
    protected SchedulerEventListener getSchedulerListener() {
        return thisStub;
    }

    protected void registerRMListener() {
        nodeSource.getRMCore().getMonitoring().addRMEventListener((RMEventListener) PAActiveObject.getStubOnThis());
        logger.debug("RM listener successfully registered.");
        active = true;
    }

    @Override
    public String getDescription() {
        return "Allocates as many resources as scheduler required according\nto loading factor. Releases resources smoothly.";
    }

    @Override
    public String toString() {
        return super.toString() + " [Max Nodes: " + maxNodes + " Min Nodes: " + minNodes + " Job Per Node: " +
               loadFactor + " Refresh period: " + refreshTime + " Refresh cycle window: " + refreshCycleWindow +
               " Release delay: " + releaseDelay + " Threshold: " + threshold + "]";
    }

    public void rmEvent(RMEvent event) {
    }

    public void nodeSourceEvent(RMNodeSourceEvent event) {
    }

    public void nodeEvent(RMNodeEvent event) {
        synchronized (nodes) {
            switch (event.getEventType()) {
                case NODE_ADDED:
                    //if node addition is related to pending nodes/down nodes
                    //we discard the computation
                    if (!checkNodeStates(event)) {
                        break;
                    }
                    if (event.getNodeSource().equals(nodeSourceName)) {
                        nodesNumberInNodeSource.incrementAndGet();
                        pendingAddedNodesNumberInNodeSource.decrementAndGet();
                        nodes.put(event.getNodeUrl(), System.currentTimeMillis());
                        logger.debug("Requested node arrived " + event.getNodeUrl());
                    }
                    break;
                case NODE_REMOVED:
                    //we only care about non pending nodes
                    if (event.getNodeState() == NodeState.LOST || event.getNodeState() == NodeState.DEPLOYING) {
                        break;
                    }
                    if (event.getNodeSource().equals(nodeSourceName)) {
                        nodesNumberInNodeSource.decrementAndGet();
                        pendingRemovedNodesNumberInNodeSource.decrementAndGet();
                        nodes.remove(event.getNodeUrl());
                        logger.debug("Requested node removed " + event.getNodeUrl());

                    }

                    break;
                case NODE_STATE_CHANGED:
                    //we only care about down nodes
                    if (event.getNodeSource().equals(nodeSourceName)) {
                        if (event.getNodeState() == NodeState.DOWN) {
                            nodesNumberInNodeSource.decrementAndGet();
                        } else if (event.getPreviousNodeState() == NodeState.DOWN &&
                                   (event.getNodeState() == NodeState.BUSY || event.getNodeState() == NodeState.FREE)) {
                            nodesNumberInNodeSource.incrementAndGet();
                        } else if (event.getNodeState() == NodeState.LOST) {
                            pendingAddedNodesNumberInNodeSource.decrementAndGet();
                        }
                    }

            }
        }
    }

    /**
     * Returns the total number of nodes alive in the resource manager (i.e. not lost, down, to_be_removed, etc)
     */
    private int getNumberOfAliveNodes() {
        return nodeSource.getRMCore().listAliveNodeUrls().size();
    }

    /**
     * Returns the total number of tasks ready to be scheduled
     */
    private int getTotalNumberOfAliveTasks() throws NotConnectedException, PermissionException, UnknownJobException {
        Map<JobId, JobDescriptor> jobsRetrievedFromPolicy = scheduler.getJobsToSchedule();

        int totalNumberOfRunningTasks = 0;
        for (JobId jobId : jobsRetrievedFromPolicy.keySet()) {
            JobState jobState = scheduler.getJobState(jobId);
            totalNumberOfRunningTasks += jobState.getNumberOfRunningTasks();
        }

        int totalNumberOfEligibleTasks = 0;
        List<TaskDescriptor> taskRetrievedFromPolicy = scheduler.getTasksToSchedule();
        //if there is no task to be scheduled
        for (TaskDescriptor etd : taskRetrievedFromPolicy) {
            totalNumberOfEligibleTasks += etd.getNumberOfNodesNeeded();
        }
        return totalNumberOfRunningTasks + totalNumberOfEligibleTasks;
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
    public void shutdown(Client initiator) {
        active = false;
        super.shutdown(initiator);
    }

}
