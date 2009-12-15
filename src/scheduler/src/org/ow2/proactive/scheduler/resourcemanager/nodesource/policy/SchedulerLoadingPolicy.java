/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
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
import java.util.Timer;
import java.util.TimerTask;

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
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.policy.PolicyRestriction;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


@PolicyRestriction(supportedInfrastructures = {
        "org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMCustomisedInfrastructure",
        "org.ow2.proactive.resourcemanager.nodesource.infrastructure.PBSInfrastructure",
        "org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructure",
        "org.ow2.proactive.resourcemanager.nodesource.infrastructure.EC2Infrastructure",
        "org.ow2.proactive.resourcemanager.nodesource.infrastructure.VirtualInfrastructure",
        "org.ow2.proactive.resourcemanager.nodesource.infrastructure.WinHPCInfrastructure"})
public class SchedulerLoadingPolicy extends SchedulerAwarePolicy implements InitActive, RunActive {

    /**  */
    private static final long serialVersionUID = 200;

    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.POLICY);

    private Map<JobId, Integer> activeTasks = new HashMap<JobId, Integer>();
    private int activeTask = 0;

    @Configurable(description = "period of recalculating required number of nodes")
    private int policyPeriod = 10000;
    @Configurable
    private int minNodes = 0;
    @Configurable
    private int maxNodes = 100;
    @Configurable(description = "number of tasks per node")
    private int loadingFactor = 10;
    @Configurable(description = "delay between each node release (in ms)")
    private int releasePeriod = 1000;

    // policy state
    private int currentNodeNumberInNodeSource = 0;
    private int currentNodeNumberInResourceManager = 0;
    private int pendingNodesNumberAcq = 0;
    private int pendingNodesNumberRel = 0;
    private int releaseNodesNumber = 0;
    private transient Timer timer;
    private transient Object monitor = new Object();
    private SchedulerLoadingPolicy thisStub;

    public SchedulerLoadingPolicy() {
    }

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     */
    @Override
    public void configure(Object... policyParameters) throws RMException {
        super.configure(policyParameters);

        try {
            int index = 3;
            policyPeriod = Integer.parseInt(policyParameters[index++].toString());
            minNodes = Integer.parseInt(policyParameters[index++].toString());
            maxNodes = Integer.parseInt(policyParameters[index++].toString());
            loadingFactor = Integer.parseInt(policyParameters[index++].toString());
            releasePeriod = Integer.parseInt(policyParameters[index++].toString());
        } catch (RuntimeException e) {
            throw new RMException(e);
        }
    }

    public void initActivity(Body body) {
        thisStub = (SchedulerLoadingPolicy) PAActiveObject.getStubOnThis();
        PAActiveObject.setImmediateService("getTotalNodeNumber");
    }

    public void runActivity(Body body) {
        Service service = new Service(body);

        long timeStamp = System.currentTimeMillis();
        long delta = 0;

        // recalculating nodes number only once per policy period
        while (body.isActive()) {

            service.blockingServeOldest(policyPeriod);

            delta += System.currentTimeMillis() - timeStamp;
            timeStamp = System.currentTimeMillis();

            if (delta > policyPeriod) {
                synchronized (monitor) {
                    if (nodeSource != null) {
                        try {
                            updateNumberOfNodes();
                        } catch (BodyTerminatedRequestException e) {
                        }
                    }
                }
                delta = 0;
            }
        }
    }

    @Override
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

    @Override
    protected SchedulerEvent[] getEventsList() {
        return new SchedulerEvent[] { SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.JOB_SUBMITTED,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED };
    }

    @Override
    protected SchedulerEventListener getSchedulerListener() {
        return thisStub;
    }

    protected IntWrapper getTotalNodeNumber() {
        return nodeSource.getRMCore().getTotalAliveNodesNumber();
    }

    private void refreshPolicyState() {

        // recalculating the current node number in resource manager and the node source
        currentNodeNumberInResourceManager = thisStub.getTotalNodeNumber().intValue();
        int newNodeNumberInNodeSource = nodeSource.getNodesCount();

        // recalculating pending nodes to release and to acquire
        if (pendingNodesNumberRel != 0) {
            // there are some pending node removal requests
            if (newNodeNumberInNodeSource < currentNodeNumberInNodeSource) {
                pendingNodesNumberRel -= currentNodeNumberInNodeSource - newNodeNumberInNodeSource;

                if (pendingNodesNumberRel < 0) {
                    // possible only if some nodes were removed by user (should not be done for this policy)
                    // if it's the same this message could be ignored
                    logger.warn("Incorrect node source state: [pending node removal requests < 0] : " +
                        "currentNodeNumberInNodeSource" + currentNodeNumberInNodeSource +
                        ", newNodeNumberInNodeSource=" + newNodeNumberInNodeSource +
                        ", pendingNodesNumberAcq=" + pendingNodesNumberAcq + ", pendingNodesNumberRel=" +
                        pendingNodesNumberRel + ", activeTask=" + activeTask);
                    pendingNodesNumberRel = 0;
                }
            } else if (newNodeNumberInNodeSource > currentNodeNumberInNodeSource) {
                // pending node removal request > 0 => should not acquire nodes at this time
                logger
                        .warn("Incorrect node source state: [waiting for node removal and should not acquire nodes at this phase] : " +
                            "currentNodeNumberInNodeSource" +
                            currentNodeNumberInNodeSource +
                            ", newNodeNumberInNodeSource=" +
                            newNodeNumberInNodeSource +
                            ", pendingNodesNumberAcq=" +
                            pendingNodesNumberAcq +
                            ", pendingNodesNumberRel=" +
                            pendingNodesNumberRel + ", activeTask=" + activeTask);
            }
        }

        if (pendingNodesNumberAcq == 0) {
            // no pending acquisition requests
            // just updating current node size in the node source
            currentNodeNumberInNodeSource = newNodeNumberInNodeSource;
        } else {

            // updating pending node acquisition requests 
            if (newNodeNumberInNodeSource > currentNodeNumberInNodeSource) {
                pendingNodesNumberAcq -= newNodeNumberInNodeSource - currentNodeNumberInNodeSource;
            }

            currentNodeNumberInNodeSource = newNodeNumberInNodeSource;
            if (pendingNodesNumberAcq < 0) {
                // acquired more nodes than expected
                logger.warn("Incorrect node source state: [pending node acquisition requests < 0] : " +
                    "currentNodeNumberInNodeSource" + currentNodeNumberInNodeSource +
                    ", newNodeNumberInNodeSource=" + newNodeNumberInNodeSource + ", pendingNodesNumberAcq=" +
                    pendingNodesNumberAcq + ", pendingNodesNumberRel=" + pendingNodesNumberRel +
                    ", activeTask=" + activeTask);
                pendingNodesNumberAcq = 0;
            }
        }

        // consistency checks
        if (currentNodeNumberInNodeSource == minNodes && pendingNodesNumberRel > 0) {
            logger
                    .warn("Incorrect node source state: [the node source has min number of nodes but pendingNodesNumberRel > 0] : " +
                        "currentNodeNumberInNodeSource" +
                        currentNodeNumberInNodeSource +
                        ", pendingNodesNumberAcq=" +
                        pendingNodesNumberAcq +
                        ", pendingNodesNumberRel=" +
                        pendingNodesNumberRel + ", activeTask=" + activeTask);
            pendingNodesNumberRel = 0;
        }
        if (currentNodeNumberInNodeSource == maxNodes && pendingNodesNumberAcq > 0) {
            logger
                    .warn("Incorrect node source state: [the node source has max number of nodes but pendingNodesNumberAcq > 0] : " +
                        "currentNodeNumberInNodeSource" +
                        currentNodeNumberInNodeSource +
                        ", pendingNodesNumberAcq=" +
                        pendingNodesNumberAcq +
                        ", pendingNodesNumberRel=" +
                        pendingNodesNumberRel + ", activeTask=" + activeTask);
            pendingNodesNumberAcq = 0;
        }

    }

    private void updateNumberOfNodes() {
        refreshPolicyState();

        logger.debug("Policy State: currentNodeNumberInNodeSource=" + currentNodeNumberInNodeSource +
            ", currentNodeNumberInResourceManager=" + currentNodeNumberInResourceManager +
            ", pendingNodesNumberAcq=" + pendingNodesNumberAcq + ", pendingNodesNumberRel=" +
            pendingNodesNumberRel + ", activeTask=" + activeTask);

        int potentialNodeNumberInResourceManager = currentNodeNumberInResourceManager +
            pendingNodesNumberAcq - pendingNodesNumberRel;
        int potentialNodeNumberInNodeSource = currentNodeNumberInNodeSource + pendingNodesNumberAcq -
            pendingNodesNumberRel;

        if (potentialNodeNumberInNodeSource < minNodes) {
            int difference = minNodes - potentialNodeNumberInNodeSource;
            acquireNNodes(difference);
            return;
        }

        if (potentialNodeNumberInNodeSource > maxNodes) {
            int difference = potentialNodeNumberInNodeSource - maxNodes;
            removeNodes(difference);
            return;
        }

        int requiredNodesNumber = activeTask / loadingFactor + (activeTask % loadingFactor == 0 ? 0 : 1);
        logger.debug("Required node number " + requiredNodesNumber);

        if (requiredNodesNumber == potentialNodeNumberInResourceManager) {
            return;
        }

        int difference = 0;

        if (requiredNodesNumber < potentialNodeNumberInResourceManager) {
            // releasing nodes

            // how much do we need in total
            difference = potentialNodeNumberInResourceManager - requiredNodesNumber;
            // correction if after removal there will be too few nodes in the node source
            if (potentialNodeNumberInNodeSource - difference < minNodes) {
                difference = potentialNodeNumberInNodeSource - minNodes;
            }
            removeNodes(difference);
        } else {
            // acquiring nodes

            difference = requiredNodesNumber - potentialNodeNumberInResourceManager;
            // correct it if exceed max node number in the node source
            if (potentialNodeNumberInNodeSource + difference > maxNodes) {
                difference = maxNodes - potentialNodeNumberInNodeSource;
            }

            acquireNNodes(difference);
        }
    }

    private void acquireNNodes(int number) {
        if (pendingNodesNumberRel > 0) {
            logger.debug("Waiting for nodes to be removed. Acquire request ignored.");
            return;
        }

        // cancel the timer properly
        releaseNodesNumber = 0;

        logger.debug("Acquiring " + number + " nodes");
        super.acquireNodes(number);

        pendingNodesNumberAcq += number;
    }

    private void removeNodes(int number) {

        if (number < 0) {
            throw new RuntimeException("Negative nodes number " + number);
        }
        if (number == 0)
            return;

        if (timer == null) {
            timer = new Timer(true);
        }

        if (releaseNodesNumber > 0) {
            logger.debug("Timer has already been scheduled");
            releaseNodesNumber = Math.max(number, releaseNodesNumber);
            return;
        }

        logger.debug("Setup timer to remove " + number + " nodes");
        releaseNodesNumber = number;

        timer.scheduleAtFixedRate(new TimerTask() {
            int maxNumberOfAttempts = 100;

            @Override
            public void run() {
                synchronized (monitor) {
                    if (releaseNodesNumber == 0) {
                        logger.debug("Timer finished releasing nodes");
                        timer.cancel();
                        timer = null;
                        return;
                    }

                    if (maxNumberOfAttempts > 0 && pendingNodesNumberAcq > 0) {
                        maxNumberOfAttempts--;
                        logger.debug("Waiting for nodes to be acquired. Release request ignored.");
                        return;
                    } else if (pendingNodesNumberAcq > 0) {
                        logger
                                .warn("Some nodes have not been acquired for a long time. It prevents policy to release nodes.");
                        logger
                                .warn("Probably maximum number of nodes in policy configuration exeeds the physical capacity of the infrastructure");
                        logger.warn("Reseting pending acquiring nodes number");
                        pendingNodesNumberAcq = 0;
                    }

                    releaseNodesNumber--;
                    pendingNodesNumberRel++;
                    thisStub.removeNodes(1, preemptive);
                }
            }
        }, releasePeriod, releasePeriod);
    }

    @Override
    public String getDescription() {
        return "Allocates as many resources as scheduler required according\nto loading factor. Releases resources smoothly.";
    }

    @Override
    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName()) + " [Max Nodes: " + maxNodes +
            " Min Nodes: " + minNodes + " Job Per Node: " + loadingFactor + "]";
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
            case JOB_RUNNING_TO_FINISHED:
                int tasksLeft = activeTasks.remove(notification.getData().getJobId());
                activeTask -= tasksLeft;
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

}
