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

import static org.ow2.proactive.scheduler.rest.data.DataUtility.toJobInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;


public class JobStateImpl extends JobState {

    private static final long serialVersionUID = 1L;

    private JobStateData jobStateData;

    JobStateImpl(JobStateData d) {
        this.jobStateData = d;
        copyGenericInformation(jobStateData);
    }

    private void copyGenericInformation(JobStateData jobStateData) {
        super.setGenericInformation(jobStateData.getGenericInformation());
    }

    @Override
    public Map<TaskId, TaskState> getHMTasks() {
        Map<String, TaskStateData> taskStateDataMap = jobStateData.getTasks();
        Map<TaskId, TaskState> taskStateMap = new HashMap<>();
        for (TaskStateData taskStateData : taskStateDataMap.values()) {
            TaskIdData taskIdData = taskStateData.getTaskInfo().getTaskId();
            taskStateMap.put(DataUtility.taskId(DataUtility.jobId(jobStateData.getJobInfo().getJobId()), taskIdData),
                             DataUtility.taskState(taskStateData));
        }
        return taskStateMap;
    }

    @Override
    public JobInfo getJobInfo() {
        return toJobInfo(jobStateData.getJobInfo());
    }

    @Override
    public String getOwner() {
        return jobStateData.getOwner();
    }

    @Override
    public List<TaskState> getTasks() {
        Map<String, TaskStateData> taskStateMap = jobStateData.getTasks();
        List<TaskState> taskStateList = new ArrayList<>(taskStateMap.size());
        for (TaskStateData ts : taskStateMap.values()) {
            taskStateList.add(DataUtility.taskState(ts));
        }
        return taskStateList;
    }

    @Override
    public List<TaskState> getTasksByTag(String tag) {
        Map<String, TaskStateData> taskStateMap = jobStateData.getTasks();
        List<TaskState> taskStateList = new ArrayList<>(taskStateMap.size());
        for (TaskStateData ts : taskStateMap.values()) {
            String taskTag = ts.getTag();
            if (taskTag != null && taskTag.equals(tag)) {
                taskStateList.add(DataUtility.taskState(ts));
            }
        }

        return taskStateList;
    }

    @Override
    public void update(TaskInfo taskInfo) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void update(JobInfo jobInfo) {
        throw new UnsupportedOperationException();

    }

    @Override
    public JobType getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return jobStateData.getName();
    }

    @Override
    public JobPriority getPriority() {
        return JobPriority.valueOf(jobStateData.getPriority());
    }

    public JobStateData getJobStateData() {
        return this.jobStateData;
    }
}
