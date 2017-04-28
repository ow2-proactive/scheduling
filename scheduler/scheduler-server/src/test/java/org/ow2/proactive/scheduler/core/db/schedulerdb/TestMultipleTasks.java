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
package org.ow2.proactive.scheduler.core.db.schedulerdb;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestMultipleTasks extends BaseSchedulerDBTest {

    @Test
    public void testManyTasks() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        final int TASKS_NUMBER = 1000;

        for (int i = 0; i < TASKS_NUMBER; i++) {
            job.addTask(createDefaultTask("task-" + i));
        }

        InternalJob jobData = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertEquals(TASKS_NUMBER, jobData.getTasks().size());
    }

    @Test
    public void testDependencies() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask task1 = createDefaultTask("task1");
        job.addTask(task1);

        JavaTask task2 = createDefaultTask("task2");
        job.addTask(task2);

        task1.addDependence(task2);

        JavaTask task3 = createDefaultTask("task3");
        job.addTask(task3);

        task2.addDependence(task3);

        InternalJob jobData = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertEquals(3, jobData.getITasks().size());

        InternalTask taskData1 = jobData.getTask("task1");
        InternalTask taskData2 = jobData.getTask("task2");
        InternalTask taskData3 = jobData.getTask("task3");

        Assert.assertEquals(1, taskData1.getDependences().size());
        Assert.assertEquals(1, taskData2.getDependences().size());
        Assert.assertNull(taskData3.getDependences());
        Assert.assertEquals(taskData2.getId(), taskData1.getDependences().get(0).getId());
        Assert.assertEquals(taskData3.getId(), taskData2.getDependences().get(0).getId());

        Assert.assertEquals(taskData2.getTaskInfo().getTaskId(),
                            taskData1.getDependences().get(0).getTaskInfo().getTaskId());
        Assert.assertEquals(taskData3.getTaskInfo().getTaskId(),
                            taskData2.getDependences().get(0).getTaskInfo().getTaskId());

    }

}
