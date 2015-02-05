/*
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest.data;

import static org.ow2.proactive.scheduler.task.TaskIdImpl.createTaskId;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.usage.TaskUsage;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobPriorityData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStatusData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobUsageData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.SchedulerUserData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskUsageData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;


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
        impl.setFinishedTime(d.getFinishedTime());
        impl.setJobOwner(d.getJobOwner());
        impl.setNumberOfFinishedTasks(d.getNumberOfFinishedTasks());
        impl.setNumberOfPendingTasks(d.getNumberOfPendingTasks());
        impl.setNumberOfRunningTasks(d.getNumberOfRunningTasks());
        impl.setJobPriority(jobPriority(d.getPriority()));
        impl.setRemovedTime(d.getRemovedTime());
        impl.setStartTime(d.getStartTime());
        impl.setJobStatus(jobStatus(d.getStatus()));
        impl.setSubmittedTime(d.getSubmittedTime());
        impl.setTotalNumberOfTasks(d.getTotalNumberOfTasks());
        return impl;
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
        impl.setFinishedTime(d.getFinishedTime());
        impl.setNumberOfExecutionLeft(d.getNumberOfExecutionLeft());
        impl.setNumberOfExecutionOnFailureLeft(d.getNumberOfExecutionOnFailureLeft());
        impl.setStartTime(d.getStartTime());
        impl.setStatus(TaskStatus.valueOf(d.getTaskStatus().name()));
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

    public static JobResult toJobResult(JobResultData d) {
        return new JobResultImpl(d);
    }

    public static List<JobUsage> toJobUsages(List<JobUsageData> dataList) {
        List<JobUsage> jobUsages = new ArrayList<JobUsage>();
        for (JobUsageData d : dataList) {
            jobUsages.add(jobUsage(d));
        }
        return jobUsages;
    }

    public static JobUsage jobUsage(JobUsageData d) {
        JobUsage impl = new JobUsage(d.getOwner(), d.getProject(), d.getJobId(), d.getJobName(), d.getJobDuration());
        List<TaskUsageData> taskUsageDataList = d.getTaskUsages();
        for (TaskUsageData taskUsageData : taskUsageDataList) {
            impl.add(taskUsage(taskUsageData));
        }
        return impl;
    }

    public static TaskUsage taskUsage(TaskUsageData d) {
        return new TaskUsage(d.getTaskId(), d.getTaskName(), d.getTaskStartTime(), d.getTaskFinishedTime(), d
                .getTaskExecutionDuration(), d.getTaskNodeNumber());
    }

    public static List<JobInfo> toJobInfos(List<UserJobData> dataList) {
        List<JobInfo> jobInfos = new ArrayList<JobInfo>();
        for (UserJobData ujd : dataList) {
            jobInfos.add(toJobInfo(ujd.getJobInfo()));
        }
        return jobInfos;
    }

    public static List<SchedulerUserInfo> toSchedulerUserInfos(List<SchedulerUserData> dataList) {
        List<SchedulerUserInfo> schedulerUserInfos = new ArrayList<SchedulerUserInfo>();
        for (SchedulerUserData sud : dataList) {
            schedulerUserInfos.add(new SchedulerUserInfo(sud.getHostName(), sud.getUsername(), sud
                    .getConnectionTime(), sud.getLastSubmitTime(), sud.getSubmitNumber()));
        }
        return schedulerUserInfos;
    }

    private static TaskId taskId(JobId jobId, TaskIdData taskIdData) {
        return createTaskId(jobId, taskIdData.getReadableName(), taskIdData.getId(), false);
    }

}
