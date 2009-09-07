/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive.scheduler.resourcemanager.nodesource.policy;

import java.util.Calendar;
import java.util.HashMap;

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
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.nodesource.policy.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.policy.PolicyRestriction;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
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

@PolicyRestriction(supportedInfrastructures = { "org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.EC2Infrastructure" })
public class EC2Policy extends SchedulerAwarePolicy implements InitActive, RunActive {

    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.POLICY);

    @Configurable(description = "Refresh frequency (s)")
    private int refreshTime = 5;
    @Configurable(description = "Tasks per node")
    private int loadFactor = 10;
    @Configurable(description = "Release cycle (s)")
    private int releaseCycle = 3600;

    private EC2Policy thisStub;

    private int activeTask = 0;

    private int nodeNumber = 0;
    private int pendingNodes = 0;

    private HashMap<String, Calendar> nodes = new HashMap<String, Calendar>();

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
     *            <li>params[0..3] : used by super
     *            <li>params[4]: refresh time: number of seconds before the policy updates its state
     *            <li>params[5]: loadFactor: number of tasks per node at best, for the whole RM
     *            <li>params[6]: releaseCycle: number of seconds before a node can be released
     *            </ul>
     * @throws RMException
     *             invalid parameters, policy creation fails
     */
    public void configure(Object... params) throws RMException {
        super.configure(params);
        try {
            int index = 4;
            refreshTime = Integer.parseInt(params[index++].toString());
            loadFactor = Integer.parseInt(params[index++].toString());
            releaseCycle = Integer.parseInt(params[index++].toString());
        } catch (RuntimeException e) {
            throw new RMException("Unable to parse parameters", e);
        }
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
        return nodeSource.getRMCore().getNbAllRMNodes();
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
    public BooleanWrapper activate() {
        thisStub = (EC2Policy) PAActiveObject.getStubOnThis();
        BooleanWrapper activationStatus = super.activate();
        if (!activationStatus.booleanValue()) {
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

        logger.info("Policy activated. Current number of tasks " + activeTask);
        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    protected SchedulerEvent[] getEventsList() {
        return new SchedulerEvent[] { SchedulerEvent.JOB_SUBMITTED, SchedulerEvent.TASK_RUNNING_TO_FINISHED };
    }

    /**
     * {@inheritDoc}
     */
    protected SchedulerEventListener getSchedulerListener() {
        return thisStub;
    }

    /**
     * Updates the current state of the nodesource: according to the current load factor of the
     * Scheduler, will acquire or release nodes. Releases nodes only when almost
     * <code>N * releaseCycle</code> seconds have passed since the node acquisition
     */
    private void refreshNodes() {
        int rmNodeNumber = thisStub.getTotalNodeNumber().intValue();
        int nsNodeNumber = nodeSource.getNodesCount();

        if (nsNodeNumber != nodeNumber) {
            logger.debug("Updated nodes number");
            nodeNumber = nsNodeNumber;
        }

        int requiredNodes = activeTask / loadFactor + (activeTask % loadFactor == 0 ? 0 : 1);

        logger.info("Policy state: RM=" + rmNodeNumber + " NS=" + nsNodeNumber + " pending=" + pendingNodes +
            " required=" + requiredNodes + " tasks=" + activeTask);

        if (requiredNodes < nodeNumber) {
            int diff = nodeNumber - requiredNodes;
            for (String nodeUrl : nodes.keySet()) {
                long acq = nodes.get(nodeUrl).getTimeInMillis() / 1000;
                long now = Calendar.getInstance().getTimeInMillis() / 1000;
                long dt = now - acq;
                // 10secs delay at worse should be enough for the terminate request to be sent in time
                int delay = Math.max(refreshTime, 10);
                if ((dt + delay) % releaseCycle <= delay) {
                    nodeSource.getRMCore().removeNode(nodeUrl, super.preemptive, true);
                }
                if (diff-- == 0) {
                    break;
                }
            }
        } else if (requiredNodes > nodeNumber) {
            if (pendingNodes + nodeNumber < requiredNodes) {
                for (int i = 0; i < requiredNodes - (pendingNodes + nodeNumber); i++) {
                    nodeSource.acquireNode();
                    pendingNodes++;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void jobSubmittedEvent(JobState jobState) {
        activeTask += jobState.getTotalNumberOfTasks();
        logger.debug("Job submitted. Current number of tasks " + activeTask);
    }

    /**
     * {@inheritDoc}
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        switch (notification.getEventType()) {
            case TASK_RUNNING_TO_FINISHED:
                activeTask--;
                logger.debug("Task finished. Current number of tasks " + activeTask);
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
            if (pendingNodes <= 0) {
                logger.warn("Added new node while not awaiting one...");
                pendingNodes = 0;
            } else {
                pendingNodes--;
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
    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName()) + " [release cycle: " +
            releaseCycle + "s refresh frequency: " + refreshTime + "s load factor: " + loadFactor + "]";
    }

    /**
     * @return quick Policy description
     */
    public String getDescription() {
        return "Allocates resources according to the Scheduler loading factor,\n"
            + "releases resources considering EC2 instances are paid by the hour";
    }

}
