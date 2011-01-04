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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
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


/**
 * 
 * NodeSource Policy for Amazon EC2
 * 
 * Acquires resources according to the current load of the Scheduler using a
 * {@link SchedulerEventListener} similarly to the {@link SchedulerLoadingPolicy}, releases
 * resources every <code>releaseCycle</code> seconds, where releaseCycle is a dynamic parameter:
 * allows EC2 resources, which are paid by the hour, to be released only after one complete hour,
 * even if it is not needed. Uses {@link RMEventListener} to determine the time at which a node is
 * being acquired on EC2.
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 * 
 */

public class EC2Policy extends SchedulerAwarePolicy implements InitActive, RunActive, RMEventListener {

    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.POLICY);

    /**
     * Delay in seconds between each time the policy refreshes its internal state
     */
    @Configurable(description = "refresh frequency (s)")
    private int refreshTime = 5;
    /**
     * The policy will try to maintain a number <code>schedulerTasks/loadFactor</code> nodes in the whole RM
     */
    @Configurable(description = "tasks per node")
    private int loadFactor = 10;
    /**
     * Time after which a node is considered 'releasable', since the time of its acquisition
     */
    @Configurable(description = "delay between each node release (in ms)")
    private int releaseDelay = 3600;

    private EC2Policy thisStub;

    private Map<JobId, Integer> activeTasks;
    private int activeTask = 0;

    /**
     * current number of operational nodes in the NS
     */
    private int nodeNumber = 0;
    /**
     * Each time a node is requested to the NS, the result of 
     * <code>System.currentTimeMillis()</code> is added to the head of this structure;
     * each time a node registers in the NS, an element is removed from the tail. 
     */
    private LinkedList<Long> pendingNodes;

    private boolean rmShuttingDown = false;
    private RMMonitoring rmMonitoring;

    /**
     * Maps a Node Name to Date for all NS nodes
     */
    private HashMap<String, Calendar> nodes;

    /**
     * Empty constructor
     */
    public EC2Policy() {
    }

    /**
     * Configures the policy
     * 
     * @param params
     *            <ul>
     *            <li>params[0..2] : used by super
     *            <li>params[3]: refresh time: number of seconds before the policy updates its state
     *            <li>params[4]: loadFactor: number of tasks per node at best, for the whole RM
     *            <li>params[5]: releaseCycle: number of seconds before a node can be released
     *            </ul>
     * @throws IllegalArgumentException
     *             invalid parameters, policy creation fails
     */
    @Override
    public BooleanWrapper configure(Object... params) {
        super.configure(params);
        try {
            activeTasks = new HashMap<JobId, Integer>();
            pendingNodes = new LinkedList<Long>();
            nodes = new HashMap<String, Calendar>();

            int index = 4;
            refreshTime = Integer.parseInt(params[index++].toString());
            loadFactor = Integer.parseInt(params[index++].toString());
            releaseDelay = Integer.parseInt(params[index++].toString());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to parse parameters", e);
        }
        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    public void initActivity(Body body) {
        PAActiveObject.setImmediateService("getTotalNodeNumber");
    }

    /**
     * @return the current number of nodes available in the ResourceManager
     */
    protected IntWrapper getTotalNodeNumber() {
        return new IntWrapper(nodeSource.getRMCore().getState().getFreeNodesNumber());
    }

    /**
     * {@inheritDoc}
     */
    public void runActivity(Body body) {
        Service service = new Service(body);

        long t1 = System.currentTimeMillis() / 1000, t2;
        long dt = 0;

        while (body.isActive()) {

            /*
             * Request req = service.getOldest(); if (req != null) {
             * logger.debug(req.getMethodName()); }
             */
            service.blockingServeOldest(refreshTime);

            t2 = System.currentTimeMillis() / 1000;
            dt += t2 - t1;
            t1 = t2;

            if (dt > refreshTime) {
                if (nodeSource != null) {
                    try {
                        refreshNodes();
                    } catch (BodyTerminatedRequestException e) {
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
                dt = 0;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper activate() {
        thisStub = (EC2Policy) PAActiveObject.getStubOnThis();
        BooleanWrapper activationStatus = super.activate();
        if (!activationStatus.getBooleanValue()) {
            return activationStatus;
        }

        for (JobState js : state.getPendingJobs()) {
            activeTask += js.getTotalNumberOfTasks();
        }
        for (JobState js : state.getRunningJobs()) {
            activeTask += js.getNumberOfPendingTasks();
            activeTask += js.getNumberOfRunningTasks();
        }

        thisStub = (EC2Policy) PAActiveObject.getStubOnThis();
        PAActiveObject.setImmediateService("getTotalNodeNumber");

        rmMonitoring = nodeSource.getRMCore().getMonitoring();
        rmMonitoring.addRMEventListener((RMEventListener) thisStub);

        logger.info("Policy activated. Current number of tasks " + activeTask);
        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SchedulerEvent[] getEventsList() {
        return new SchedulerEvent[] { SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                SchedulerEvent.JOB_PENDING_TO_FINISHED, SchedulerEvent.JOB_SUBMITTED,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SchedulerEventListener getSchedulerListener() {
        return thisStub;
    }

    /**
     * Updates the current state of the nodesource: according to the current load factor of the
     * Scheduler, will acquire or release nodes. Releases nodes only when almost
     * <code>N * releaseCycle</code> seconds have passed since the node acquisition
     */
    private void refreshNodes() {
        int rmNodeNumber = thisStub.getTotalNodeNumber().getIntValue();
        int nsNodeNumber = nodeSource.getNodesCount();

        if (nsNodeNumber != nodeNumber) {
            logger.debug("Updated nodes number");
            nodeNumber = nsNodeNumber;
        }

        int requiredNodes = activeTask / loadFactor + (activeTask % loadFactor == 0 ? 0 : 1);

        logger.info("Policy state: RM=" + rmNodeNumber + " NS=" + nsNodeNumber + " pending=" +
            pendingNodes.size() + " required=" + requiredNodes + " tasks=" + activeTask);

        if (requiredNodes < rmNodeNumber && nodeNumber > 0) {
            int diff = Math.min(nodeNumber, rmNodeNumber - requiredNodes);
            for (String nodeUrl : nodes.keySet()) {
                long acq = nodes.get(nodeUrl).getTimeInMillis() / 1000;
                long now = Calendar.getInstance().getTimeInMillis() / 1000;
                long dt = now - acq;
                // 10secs delay at worse should be enough for the terminate request to be sent in time
                int delay = Math.max(refreshTime, 10);
                if ((dt + delay) % releaseDelay <= delay) {
                    nodeSource.getRMCore().removeNode(nodeUrl, false);
                    diff--;
                    if (diff == 0) {
                        break;
                    }
                }
            }
        } else if (requiredNodes > rmNodeNumber) {
            if (pendingNodes.size() + rmNodeNumber < requiredNodes) {
                for (int i = 0; i < requiredNodes - (pendingNodes.size() + rmNodeNumber); i++) {
                    nodeSource.acquireNode();
                    pendingNodes.addLast(System.currentTimeMillis());
                }
            }
        }

        for (Iterator<Long> it = pendingNodes.iterator(); it.hasNext();) {
            Long l = it.next();
            // waiting more than half the release cycle is considered timeout.
            // this is *wildly* inaccurate, but as the useful info is kept on IM side,
            // this provides at least the guarantee that nodes won't stay pending forever,
            // which is far worse than unexpected nodes poping in the NS without being expected
            if (System.currentTimeMillis() - l > (this.releaseDelay / 2) * 1000) {
                it.remove();
            } else {
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void jobSubmittedEvent(JobState jobState) {
        int nbTasks = jobState.getTotalNumberOfTasks();
        activeTasks.put(jobState.getId(), nbTasks);
        activeTask += nbTasks;
        logger.debug("Job submitted. Current number of tasks " + activeTask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        switch (notification.getEventType()) {
            case JOB_PENDING_TO_FINISHED:
            case JOB_RUNNING_TO_FINISHED:
                int tasksLeft = activeTasks.remove(notification.getData().getJobId());
                activeTask -= tasksLeft;
                break;
            case TASK_REPLICATED:
            case TASK_SKIPPED:
                JobId jid = notification.getData().getJobId();
                JobInfo ji = notification.getData();
                int i = ji.getNumberOfPendingTasks() + ji.getNumberOfRunningTasks();
                int oldActiveTask = activeTasks.get(jid);
                activeTasks.put(jid, i);
                activeTask += (i - oldActiveTask);
                logger.debug("Tasks replicated. Current number of tasks " + activeTask);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        switch (notification.getEventType()) {
            case TASK_RUNNING_TO_FINISHED:
                JobId id = notification.getData().getJobId();
                activeTasks.put(id, activeTasks.get(id) - 1);
                activeTask--;
                logger.debug("Task finished. Current number of tasks " + activeTask);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper disactivate() {
        if (rmShuttingDown) {
            return new BooleanWrapper(true);
        }

        try {
            rmMonitoring.removeRMEventListener();
        } catch (RMException e) {
            logger.error("" + e);
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    public void nodeSourceEvent(RMNodeSourceEvent event) {
    }

    /**
     * {@inheritDoc}
     */
    public void rmEvent(RMEvent event) {
        switch (event.getEventType()) {
            case SHUTTING_DOWN:
                rmShuttingDown = true;
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void nodeEvent(RMNodeEvent event) {
        if (!event.getNodeSource().equals(nodeSource.getName())) {
            return;
        }

        if (event.getEventType().equals(RMEventType.NODE_ADDED)) {
            if (pendingNodes.size() == 0) {
                logger.warn("Added new node while not awaiting one...");
            } else {
                pendingNodes.removeLast();
            }
            if (nodes.put(event.getNodeUrl(), Calendar.getInstance()) != null) {
                logger.warn("New node " + event.getNodeUrl() + " was already in nodeSource " +
                    event.getNodeSource());
            }
            nodeNumber++;
            logger.debug("New node registered: " + event.getNodeUrl());

        } else if (event.getEventType().equals(RMEventType.NODE_REMOVED)) {
            if (nodes.remove(event.getNodeUrl()) == null) {
                logger.warn("Removed node " + event.getNodeUrl() + " was not in nodeSource " +
                    event.getNodeSource());
            }
            nodeNumber--;
            logger.info("Node removed: " + event.getNodeUrl());
        }
    }

    /**
     * @return compact Policy status
     */
    @Override
    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName()) + " [release cycle: " +
            releaseDelay + "s refresh frequency: " + refreshTime + "s load factor: " + loadFactor + "]";
    }

    /**
     * @return quick Policy description
     */
    @Override
    public String getDescription() {
        return "Allocates resources according to the Scheduler loading factor,\n"
            + "releases resources considering EC2 instances are paid by the hour";
    }

}
