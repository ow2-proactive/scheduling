/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.nodesource.policy.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.policy.PolicyRestriction;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


@PolicyRestriction(supportedInfrastructures = { "org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMCustomisedInfrastructure" })
public class SchedulerLoadingPolicy extends SchedulerAwarePolicy implements InitActive {

    /**
     *
     */
    private static final long serialVersionUID = 10L;
    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.POLICY);
    private int activeTask = 0;

    @Configurable
    private int minNodes = 0;
    @Configurable
    private int maxNodes = 100;
    @Configurable(description = "number of tasks per node")
    private int loadingFactor = 10;
    @Configurable(description = "delay between each node release (in ms)")
    private int releasePeriod = 1000;

    // policy state
    private int currentNodeNumber = 0;
    private int pendingNodesNumber = 0;
    private int releaseNodesNumber = 0;
    private transient Timer timer;
    private SchedulerLoadingPolicy thisStub;

    public SchedulerLoadingPolicy() {
    }

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     */
    public void configure(Object... policyParameters) {
        url = policyParameters[0].toString();
        userName = policyParameters[1].toString();
        password = policyParameters[2].toString();
        preemptive = Boolean.parseBoolean(policyParameters[3].toString());
        minNodes = Integer.parseInt(policyParameters[4].toString());
        maxNodes = Integer.parseInt(policyParameters[5].toString());
        loadingFactor = Integer.parseInt(policyParameters[6].toString());
        releasePeriod = Integer.parseInt(policyParameters[7].toString());
    }

    public void initActivity(Body body) {
        thisStub = (SchedulerLoadingPolicy) PAActiveObject.getStubOnThis();
    }

    public BooleanWrapper activate() {
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

        logger.debug("Policy activated. Current number of tasks " + activeTask);
        return new BooleanWrapper(true);
    }

    protected SchedulerEvent[] getEventsList() {
        return new SchedulerEvent[] { SchedulerEvent.JOB_SUBMITTED, SchedulerEvent.TASK_RUNNING_TO_FINISHED };
    }

    protected SchedulerEventListener getSchedulerListener() {
        return thisStub;
    }

    private synchronized void refreshPolicyState() {
        int nodeSize = nodeSource.getNodesCount();
        if (pendingNodesNumber == 0) {
            currentNodeNumber = nodeSize;
        } else {

            if (nodeSize > currentNodeNumber) {
                pendingNodesNumber -= nodeSize - currentNodeNumber;
            }

            currentNodeNumber = nodeSize;
            if (pendingNodesNumber < 0) {
                logger.debug("Some nodes probably were removed from node source: old nodes size=" +
                    currentNodeNumber + ", new nodes size=" + nodeSize + ", pending nodes=" +
                    pendingNodesNumber);
                pendingNodesNumber = 0;
            }
        }
    }

    private void updateNumberOfNodes() {
        refreshPolicyState();

        logger.debug("updateNumberOfNodes() - currentNodeSize=" + currentNodeNumber + ", pendingNodesSize=" +
            pendingNodesNumber);
        int potentialNodeSize = currentNodeNumber + pendingNodesNumber;

        if (potentialNodeSize < minNodes) {
            int difference = minNodes - potentialNodeSize;
            acquireNNodes(difference);
            return;
        }

        if (potentialNodeSize > maxNodes) {
            int difference = potentialNodeSize - maxNodes;
            removeNodes(difference);
            return;
        }

        int requiredNodesNumber = activeTask / loadingFactor + (activeTask % loadingFactor == 0 ? 0 : 1);

        if (requiredNodesNumber == potentialNodeSize) {
            return;
        }

        int difference = 0;

        if (requiredNodesNumber < potentialNodeSize) {
            // releasing
            difference = requiredNodesNumber < minNodes ? potentialNodeSize - minNodes : potentialNodeSize -
                requiredNodesNumber;
            removeNodes(difference);
        } else {
            // acquiring
            difference = requiredNodesNumber > maxNodes ? maxNodes - potentialNodeSize : requiredNodesNumber -
                potentialNodeSize;
            acquireNNodes(difference);
        }
    }

    private void acquireNNodes(int number) {
        logger.debug("Acquiring " + number + " nodes");
        super.acquireNodes(number);

        if (timer != null) {
            synchronized (timer) {
                timer.cancel();
            }
        }

        pendingNodesNumber += number;
    }

    private void removeNodes(int number) {
        if (number < 0) {
            throw new RuntimeException("Negative nodes number " + number);
        }
        if (number == 0)
            return;

        if (timer != null) {
            synchronized (timer) {
                timer.cancel();
            }
        }

        logger.debug("Setup timer to remove " + number + " nodes");
        releaseNodesNumber = number;

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            int maxNumberOfAttempts = 100;

            public void run() {
                synchronized (timer) {
                    refreshPolicyState();

                    if (releaseNodesNumber == 0) {
                        logger.debug("Released everything");
                        timer.cancel();
                        return;
                    }

                    if (maxNumberOfAttempts > 0 && pendingNodesNumber > 0) {
                        maxNumberOfAttempts--;
                        logger.debug("Some nodes are still initializing. Release request will not be sent.");
                        return;
                    }

                    releaseNodesNumber--;
                    logger.debug("Releasing node");
                    thisStub.removeNodes(1, preemptive);
                }
            }
        }, releasePeriod, releasePeriod);
    }

    public String getDescription() {
        return "[BETA] Allocates as many resources as scheduler required according\nto loading factor. Releases resources smoothly.";
    }

    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName()) + " [Max Nodes: " + maxNodes +
            " Min Nodes: " + minNodes + " Job Per Node: " + loadingFactor + "]";
    }

    public void jobSubmittedEvent(JobState jobState) {
        activeTask += jobState.getTotalNumberOfTasks();
        logger.debug("Job submitted. Current number of tasks " + activeTask);
        updateNumberOfNodes();
    }

    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        switch (notification.getEventType()) {
            case TASK_RUNNING_TO_FINISHED:
                activeTask--;
                logger.debug("Task finished. Current number of tasks " + activeTask);
                updateNumberOfNodes();
                break;
        }
    }
}
