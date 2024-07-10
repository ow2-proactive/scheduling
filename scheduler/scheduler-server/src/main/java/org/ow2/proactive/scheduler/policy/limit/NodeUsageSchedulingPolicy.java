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
package org.ow2.proactive.scheduler.policy.limit;

import static org.ow2.proactive.scheduler.common.SchedulerConstants.PARENT_JOB_ID;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.utils.NodeSet;


/**
 *
 * This Policy is designed to limit the number of Nodes a given job (and all its sub jobs) can use in parallel.
 *
 * When a job is submitted with a MAX_NODES_USAGE generic information, this job and all its sub jobs will be handled by the policy. It ensures that no more than MAX_NODES_USAGE Nodes can be used in parallel by the job tree.
 *
 */
public class NodeUsageSchedulingPolicy extends ExtendedSchedulerPolicy {

    private static final Logger logger = Logger.getLogger(NodeUsageSchedulingPolicy.class);

    public static final String MAX_NODES_USAGE = "MAX_NODES_USAGE";

    // For DB persistence
    private NodeUsageLimitSynchronization nodeUsageSynchronization = null;

    private void initialize() {
        if (nodeUsageSynchronization == null) {
            logger.debug("Initializing NodeUsageLimitSynchronization");
            nodeUsageSynchronization = new NodeUsageLimitSynchronization();
            nodeUsageSynchronization.persist();
        }
    }

    private void removeFinishedJobsAndReleaseTokens() {
        for (String jobId : nodeUsageSynchronization.findAllJobsHandled()) {
            JobId currentJobId = JobIdImpl.makeJobId(jobId);
            if (!schedulingService.isJobAlive(currentJobId)) {
                nodeUsageSynchronization.jobTerminated(jobId);
            }
        }
        nodeUsageSynchronization.persist();
    }

    private void removeFinishedTasksAndReleaseTokens() {
        for (String taskId : nodeUsageSynchronization.findAllTasksHandled()) {
            TaskId currentTaskId = TaskIdImpl.makeTaskId(taskId);
            TaskStatus currentTaskStatus = schedulingService.getTaskStatus(currentTaskId);
            if (currentTaskStatus != TaskStatus.RUNNING && currentTaskStatus != TaskStatus.PENDING) {
                nodeUsageSynchronization.taskNotRunning(currentTaskId.getJobId().value(), currentTaskId.toString());
            }
        }
        nodeUsageSynchronization.persist();
    }

    private int getMaxNodesUsageConfiguration(JobDescriptorImpl job) {
        final String maxNodesConfiguredString = job.getInternal().getRuntimeGenericInformation().get(MAX_NODES_USAGE);
        if (maxNodesConfiguredString == null) {
            return 0;
        }
        try {
            return Integer.parseInt(maxNodesConfiguredString);
        } catch (NumberFormatException e) {
            logger.error("Invalid " + MAX_NODES_USAGE + " configured in job " + job.getJobId().getReadableName());
            return 0;
        }
    }

    private String getParentJobId(JobDescriptorImpl job) {
        return job.getInternal().getRuntimeGenericInformation().get(PARENT_JOB_ID);
    }

    private boolean addJobWithNodeUsage(JobDescriptorImpl job) {
        // Retrieve MAX_NODES_USAGE configuration from the job generic information
        final int maxNodesConfigured = getMaxNodesUsageConfiguration(job);
        final String jobId = job.getJobId().value();
        final String parentJobId = getParentJobId(job);

        // If MAX_NODES_USAGE is configured, we register the new top-level job to handle
        if (maxNodesConfigured > 0) {
            logger.debug("For job " + jobId + " with max nodes configured : " + maxNodesConfigured);

            nodeUsageSynchronization.addJob(jobId, maxNodesConfigured);
        }
        // if the current job does not define a MAX_NODES_USAGE configuration and has a parent, we analyse if this job should be added in the tree
        // note that is this job defines MAX_NODES_USAGE with a negative value, it will be excluded from the parent tree, this allows for example to remove limits on some sub jobs.
        if (parentJobId != null && maxNodesConfigured == 0) {
            nodeUsageSynchronization.addChildJob(jobId, parentJobId);
        }

        return true;
    }

    /*
     * Here we only keep tasks which, are not part of a job tree which require max node usage, or do
     * not exhaust the available node usage tokens for a given job tree
     *
     * This means that we add tasks progressively until tokens are exhausted and no slot is left
     */
    @Override
    public LinkedList<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobDescList) {

        initialize();

        // Filter jobs according to the parent policy
        List<JobDescriptor> filteredJobDescList = super.filterJobs(jobDescList);
        // Analyse jobs and check if some need to be handled by this policy
        filteredJobDescList = filteredJobDescList.stream()
                                                 .filter(jobDesc -> addJobWithNodeUsage((JobDescriptorImpl) jobDesc))
                                                 .collect(Collectors.toList());

        // Retrieve the ordered tasks from the filtered jobs according to the parent policy
        LinkedList<EligibleTaskDescriptor> orderedTasksDescFromParentPolicy = super.getOrderedTasks(filteredJobDescList);

        // Update the number of Nodes currently used by all handled jobs, based on the jobs/tasks current status
        removeFinishedTasksAndReleaseTokens();
        removeFinishedJobsAndReleaseTokens();

        // Retrieve a structure containing all available slots
        final NodeUsageLimitSynchronization.NodeUsageTokens nodeUsageTokens = nodeUsageSynchronization.getNodeUsageTokens();

        if (logger.isTraceEnabled()) {
            logger.trace("Node usage tokens: " + nodeUsageTokens);
        }

        // Add pending tasks as long as we don't exhaust the node usage tokens per job
        LinkedList<EligibleTaskDescriptor> filteredOrderedTasksDescFromParentPolicy = orderedTasksDescFromParentPolicy.stream()
                                                                                                                      .filter(taskDesc -> nodeUsageTokens.acquireTokens(taskDesc.getJobId()
                                                                                                                                                                                .value(),
                                                                                                                                                                        taskDesc.getNumberOfNodesNeeded()))
                                                                                                                      .collect(Collectors.toCollection(LinkedList::new));

        return filteredOrderedTasksDescFromParentPolicy;
    }

    /*
     * This method is called by the scheduler loop when the task is about to start. We register here
     * the new task and record node tokens used
     */
    @Override
    public boolean isTaskExecutable(NodeSet selectedNodes, EligibleTaskDescriptor task) {
        nodeUsageSynchronization.addRunningTask(task.getJobId().value(),
                                                task.getTaskId().toString(),
                                                task.getNumberOfNodesNeeded());
        nodeUsageSynchronization.persist();
        return true;
    }

}
