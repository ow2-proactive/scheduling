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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.task.ClientTaskState;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;


public class DozerMappingTest {

    private final Mapper mapper = new DozerBeanMapper(Collections.singletonList("org/ow2/proactive_grid_cloud_portal/scheduler/dozer-mappings.xml"));

    @Test
    public void jobStateTasks_Key_AreMappedToString() throws Exception {
        JobState jobState = createJobState();

        JobStateData jobStateData = mapper.map(jobState, JobStateData.class);

        assertFalse(jobStateData.getTasks().isEmpty());
        for (String s : jobStateData.getTasks().keySet()) {
            assertEquals("1", s);
        }
    }

    private JobState createJobState() {
        return new ClientJobState(new JobState() {
            @Override
            public void update(TaskInfo taskInfo) {

            }

            @Override
            public void update(JobInfo jobInfo) {

            }

            @Override
            public JobInfo getJobInfo() {
                return new JobInfoImpl();
            }

            @Override
            public ArrayList<TaskState> getTasks() {
                return new ArrayList<>(getHMTasks().values());
            }

            @Override
            public Map<TaskId, TaskState> getHMTasks() {
                TaskId taskId = TaskIdImpl.createTaskId(new JobIdImpl(42, "job"), "remoteVisuTask", 1);
                TaskState value = new ClientTaskState(new TaskState() {
                    @Override
                    public void update(TaskInfo taskInfo) {

                    }

                    @Override
                    public List<TaskState> getDependences() {
                        return null;
                    }

                    @Override
                    public TaskInfo getTaskInfo() {
                        TaskInfoImpl taskInfo = new TaskInfoImpl();
                        taskInfo.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(42, "job"), "remoteVisuTask", 1));
                        return taskInfo;
                    }

                    @Override
                    public int getMaxNumberOfExecutionOnFailure() {
                        return 0;
                    }

                    @Override
                    public TaskState replicate() throws Exception {
                        return null;
                    }

                    @Override
                    public int getIterationIndex() {
                        return 0;
                    }

                    @Override
                    public int getReplicationIndex() {
                        return 0;
                    }
                });
                return Collections.singletonMap(taskId, value);
            }

            @Override
            public String getOwner() {
                return null;
            }

            @Override
            public JobType getType() {
                return null;
            }
        });
    }
}
