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
package org.ow2.proactive.scheduler.rest.data;

import static org.ow2.proactive.scheduler.task.TaskIdImpl.createTaskId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.usage.TaskUsage;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.*;


public class DataUtility {

    private DataUtility() {
    }

    public static JobId jobId(JobIdData jobIdData) {
        return new JobIdImpl(jobIdData.getId(), jobIdData.getReadableName());
    }

    public static JobPriority jobPriority(JobPriorityData d) {
        return JobPriority.valueOf(d.name());
    }

    public static JobStatus jobStatus(JobStatusData d) {
        return JobStatus.valueOf(d.name());
    }

    public static JobInfo toJobInfo(JobInfoData d) {
        JobInfoImpl impl = new JobInfoImpl();
        impl.setJobId(jobId(d.getJobId()));
        impl.setParentId(d.getParentId());
        impl.setChildrenCount(d.getChildrenCount());
        impl.setFinishedTime(d.getFinishedTime());
        impl.setJobOwner(d.getJobOwner());
        impl.setTenant(d.getTenant());
        impl.setDomain(d.getDomain());
        impl.setBucketName(d.getBucketName());
        impl.setNumberOfFinishedTasks(d.getNumberOfFinishedTasks());
        impl.setNumberOfPendingTasks(d.getNumberOfPendingTasks());
        impl.setNumberOfRunningTasks(d.getNumberOfRunningTasks());
        impl.setNumberOfFailedTasks(d.getNumberOfFailedTasks());
        impl.setNumberOfFaultyTasks(d.getNumberOfFaultyTasks());
        impl.setNumberOfInErrorTasks(d.getNumberOfInErrorTasks());
        impl.setJobPriority(jobPriority(d.getPriority()));
        impl.setRemovedTime(d.getRemovedTime());
        impl.setStartTime(d.getStartTime());
        impl.setInErrorTime(d.getInErrorTime());
        impl.setJobStatus(jobStatus(d.getStatus()));
        impl.setSubmittedTime(d.getSubmittedTime());
        impl.setTotalNumberOfTasks(d.getTotalNumberOfTasks());
        impl.setGenericInformation(d.getGenericInformation());
        impl.setVariables(d.getVariables());
        impl.setDetailedVariables(d.getDetailedVariables());
        impl.setSignals(d.getSignals());
        impl.setDetailedSignals(d.getDetailedSignals());
        impl.setVisualizationConnectionStrings(d.getVisualizationConnectionStrings());
        impl.setVisualizationIcons(d.getVisualizationIcons());
        impl.setAttachedServices(d.getAttachedServices());
        impl.setExternalEndpointUrls(d.getExternalEndpointUrls());
        impl.setResultMapPresent(d.isResultMapPresent());
        impl.setPreciousTasks(d.getPreciousTasks());
        impl.setCumulatedCoreTime(d.getCumulatedCoreTime());
        impl.setNumberOfNodes(d.getNumberOfNodes());
        impl.setSubmissionMode(d.getSubmissionMode());
        impl.setLabel(d.getLabel());
        impl.setStartAt(d.getStartAt());
        return impl;
    }

    public static CompletedJobsCount toCompletedJobsCount(CompletedJobsCountData completedJobsCountData) {
        return new CompletedJobsCount(completedJobsCountData.getJobsWithIssues(),
                                      completedJobsCountData.getJobsWithoutIssues());
    }

    public static CompletedTasksCount toCompletedTasksCount(CompletedTasksCountData completedTasksCountData) {
        return new CompletedTasksCount(completedTasksCountData.getTasksWithIssues(),
                                       completedTasksCountData.getTasksWithoutIssues());
    }

    public static TaskInfo taskInfo(TaskInfoData d) {
        TaskInfoImpl impl = new TaskInfoImpl();
        JobIdData jobIdData = d.getJobId();
        if (jobIdData != null) {
            JobId jobId = jobId(jobIdData);
            impl.setJobId(jobId);
            TaskId taskId = taskId(jobId, d.getTaskId());
            impl.setTaskId(taskId);
        }
        impl.setExecutionDuration(d.getExecutionDuration());
        impl.setExecutionHostName(d.getExecutionHostName());
        impl.setInErrorTime(d.getInErrorTime());
        impl.setFinishedTime(d.getFinishedTime());
        impl.setNumberOfExecutionLeft(d.getNumberOfExecutionLeft());
        impl.setNumberOfExecutionOnFailureLeft(d.getNumberOfExecutionOnFailureLeft());
        impl.setStartTime(d.getStartTime());
        impl.setStatus(TaskStatus.valueOf(d.getTaskStatus().name()));
        impl.setName(d.getTaskId().getReadableName());
        impl.setProgress(d.getProgress());
        impl.setVisualizationActivated(d.isVisualizationActivated());
        impl.setVisualizationConnectionString(d.getVisualizationConnectionString());
        impl.setVariables(d.getVariables());
        return impl;
    }

    public static TaskState taskState(TaskStateData d) {
        return new TaskStateImpl(d);
    }

    public static TaskResult toTaskResult(JobId jobId, TaskResultData d) {
        return new TaskResultImpl(taskId(jobId, d.getId()), d);
    }

    public static TaskLogsImpl toTaskLogs(String all, String out, String err) {
        return new TaskLogsImpl(out, err, all);
    }

    public static JobState toJobState(JobStateData d) {
        return new JobStateImpl(d);
    }

    public static JobResult toJobResult(JobResultData d) throws IOException, ClassNotFoundException {
        return new JobResultImpl(d);
    }

    public static List<JobUsage> toJobUsages(List<JobUsageData> dataList) {
        List<JobUsage> jobUsages = new ArrayList<>(dataList.size());
        for (JobUsageData d : dataList) {
            jobUsages.add(jobUsage(d));
        }
        return jobUsages;
    }

    public static JobUsage jobUsage(JobUsageData d) {
        JobUsage impl = new JobUsage(d.getOwner(),
                                     d.getTenant(),
                                     d.getProject(),
                                     d.getJobId(),
                                     d.getJobName(),
                                     d.getJobDuration(),
                                     d.getStatus(),
                                     d.getSubmittedTime(),
                                     d.getParentId());
        List<TaskUsageData> taskUsageDataList = d.getTaskUsages();
        for (TaskUsageData taskUsageData : taskUsageDataList) {
            impl.add(taskUsage(taskUsageData));
        }
        return impl;
    }

    public static TaskUsage taskUsage(TaskUsageData d) {
        return new TaskUsage(d.getTaskId(),
                             d.getTaskStatus(),
                             d.getTaskName(),
                             d.getTaskTag(),
                             d.getTaskStartTime(),
                             d.getTaskFinishedTime(),
                             d.getTaskExecutionDuration(),
                             d.getTaskNodeNumber(),
                             d.getTaskDescription() == null ? null : d.getTaskDescription().trim(),
                             d.getExecutionHostName(),
                             d.getNumberOfExecutionLeft(),
                             d.getNumberOfExecutionOnFailureLeft(),
                             d.getMaxNumberOfExecution(),
                             PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.getValueAsInt());
    }

    public static List<JobInfo> toJobInfos(List<UserJobData> dataList) {
        List<JobInfo> jobInfos = new ArrayList<>(dataList.size());
        for (UserJobData ujd : dataList) {
            jobInfos.add(toJobInfo(ujd.getJobInfo()));
        }
        return jobInfos;
    }

    public static List<FilteredTopWorkflow>
            toFilteredTopWorkflowsWithIssues(List<FilteredTopWorkflowData> filteredTopWorkflowsData) {
        return filteredTopWorkflowsData.stream()
                                       .map(filteredTopWorkflow -> new FilteredTopWorkflow(filteredTopWorkflow.getWorkflowName(),
                                                                                           filteredTopWorkflow.getProjectName(),
                                                                                           filteredTopWorkflow.getNumberOfIssues(),
                                                                                           filteredTopWorkflow.getNumberOfExecutions()))
                                       .collect(Collectors.toList());
    }

    public static List<FilteredTopWorkflowsCumulatedCoreTime>
            toFilteredTopCumulatedCoreTime(List<FilteredTopWorkflowsCumulatedCoreTimeData> filteredTopWorkflowsData) {
        return filteredTopWorkflowsData.stream()
                                       .map(filteredTopWorkflow -> new FilteredTopWorkflowsCumulatedCoreTime(filteredTopWorkflow.getWorkflowName(),
                                                                                                             filteredTopWorkflow.getProjectName(),
                                                                                                             filteredTopWorkflow.getCumulatedCoreTime(),
                                                                                                             filteredTopWorkflow.getNumberOfExecutions()))
                                       .collect(Collectors.toList());
    }

    public static List<FilteredTopWorkflowsNumberOfNodes>
            tFilteredTopWorkflowsNumberOfNodes(List<FilteredTopWorkflowsNumberOfNodesData> filteredTopWorkflowsData) {
        return filteredTopWorkflowsData.stream()
                                       .map(filteredTopWorkflow -> new FilteredTopWorkflowsNumberOfNodes(filteredTopWorkflow.getWorkflowName(),
                                                                                                         filteredTopWorkflow.getProjectName(),
                                                                                                         filteredTopWorkflow.getNumberOfNodes(),
                                                                                                         filteredTopWorkflow.getNumberOfExecutions()))
                                       .collect(Collectors.toList());
    }

    public static List<WorkflowDuration> toWorkflowsDuration(List<WorkflowDurationData> filteredTopWorkflowsData) {
        return filteredTopWorkflowsData.stream()
                                       .map(filteredTopWorkflow -> new WorkflowDuration(filteredTopWorkflow.getWorkflowName(),
                                                                                        filteredTopWorkflow.getProjectName(),
                                                                                        filteredTopWorkflow.getDuration(),
                                                                                        filteredTopWorkflow.getNumberOfExecutions()))
                                       .collect(Collectors.toList());
    }

    public static List<SchedulerUserInfo> toSchedulerUserInfos(List<SchedulerUserData> dataList) {
        List<SchedulerUserInfo> schedulerUserInfos = new ArrayList<>(dataList.size());
        for (SchedulerUserData sud : dataList) {
            schedulerUserInfos.add(new SchedulerUserInfo(sud.getHostName(),
                                                         sud.getUsername(),
                                                         sud.getGroups(),
                                                         sud.getTenant(),
                                                         sud.getConnectionTime(),
                                                         sud.getLastSubmitTime(),
                                                         sud.getSubmitNumber()));
        }
        return schedulerUserInfos;
    }

    public static FilteredStatistics toFilteredStatistics(FilteredStatisticsData jobsReportData) {
        return new FilteredStatistics(jobsReportData.getCurrentJobs(),
                                      jobsReportData.getRunningJobs(),
                                      jobsReportData.getPausedJobs(),
                                      jobsReportData.getStalledJobs(),
                                      jobsReportData.getPendingJobs(),
                                      jobsReportData.getCurrentJobsWithoutIssues(),
                                      jobsReportData.getRunningJobsWithoutIssues(),
                                      jobsReportData.getPausedJobsWithoutIssues(),
                                      jobsReportData.getStalledJobsWithoutIssues(),
                                      jobsReportData.getCurrentJobsWithIssues(),
                                      jobsReportData.getInErrorJobs(),
                                      jobsReportData.getRunningJobsWithIssues(),
                                      jobsReportData.getPausedJobsWithIssues(),
                                      jobsReportData.getStalledJobsWithIssues(),
                                      jobsReportData.getPastJobsWithIssues(),
                                      jobsReportData.getCanceledJobs(),
                                      jobsReportData.getKilledJobs(),
                                      jobsReportData.getFailedJobs(),
                                      jobsReportData.getFinishedJobsWithIssues(),
                                      jobsReportData.getSuccessfulJobs(),
                                      jobsReportData.getTotalJobs(),
                                      jobsReportData.getSuccessfulRate());
    }

    public static UserIdentification userIdentification(SchedulerUserData d) {
        return new UserIdentificationImpl(d.getUsername(),
                                          d.getGroups(),
                                          d.getTenant(),
                                          d.getDomain(),
                                          d.getSubmitNumber(),
                                          d.getHostName(),
                                          d.getConnectionTime(),
                                          d.getLastSubmitTime(),
                                          d.isMyEventsOnly());
    }

    public static TaskId taskId(JobId jobId, TaskIdData taskIdData) {
        return createTaskId(jobId, taskIdData.getReadableName(), taskIdData.getId());
    }

    public static JobLabelInfo toJobLabelInfo(JobLabelInfoData jobLabel) {
        return new JobLabelInfo(jobLabel.getId(), jobLabel.getLabel());
    }

    public static List<JobLabelInfo> toJobLabelsInfo(List<JobLabelInfoData> jobLabel) {
        List<JobLabelInfo> jobLabelInfo = new ArrayList<>(jobLabel.size());
        jobLabel.forEach(jobLabelInfoData -> jobLabelInfo.add(toJobLabelInfo(jobLabelInfoData)));
        return jobLabelInfo;
    }

}
