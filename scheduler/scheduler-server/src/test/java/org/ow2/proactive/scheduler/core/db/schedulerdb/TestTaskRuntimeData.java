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
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalForkedScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestTaskRuntimeData extends BaseSchedulerDBTest {

    @Test
    public void testTaskTypes() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        JavaTask taskDef1 = createDefaultTask("task1");
        jobDef.addTask(taskDef1);
        JavaTask taskDef2 = createDefaultTask("task2");
        taskDef2.setForkEnvironment(new ForkEnvironment());
        jobDef.addTask(taskDef2);
        NativeTask taskDef3 = new NativeTask();
        taskDef3.setName("task3");
        taskDef3.setCommandLine("commandline");
        jobDef.addTask(taskDef3);

        InternalJob job = defaultSubmitJobAndLoadInternal(false, jobDef);

        // by default all tasks are executed in a forked JVM
        for (int i = 1; i <= 3; i++) {
            Assert.assertEquals(InternalForkedScriptTask.class, job.getTask("task" + i).getClass());
        }
    }

    @Test
    public void testTaskRuntimeData() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask task1 = createDefaultTask("task1");
        task1.setMaxNumberOfExecution(5);
        job.addTask(task1);

        InternalJob jobData = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertEquals(1, jobData.getITasks().size());

        InternalTask runtimeData = jobData.getITasks().get(0);
        Assert.assertEquals("task1", runtimeData.getName());
        Assert.assertEquals(TaskStatus.SUBMITTED, runtimeData.getStatus());
        Assert.assertEquals(5, runtimeData.getNumberOfExecutionLeft());
        Assert.assertEquals(PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.getValueAsInt(),
                            runtimeData.getNumberOfExecutionOnFailureLeft());
        Assert.assertNull(runtimeData.getDependences());
    }

    @Test
    public void testStartJobExecution() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.addTask(createDefaultTask("task1"));
        job.addTask(createDefaultTask("task2"));
        job.addTask(createDefaultTask("task3"));

        InternalJob internalJob = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertEquals(TaskStatus.SUBMITTED, internalJob.getTask("task1").getStatus());
        Assert.assertEquals(TaskStatus.SUBMITTED, internalJob.getTask("task2").getStatus());
        Assert.assertEquals(TaskStatus.SUBMITTED, internalJob.getTask("task3").getStatus());

        internalJob.start();
        InternalTask task = startTask(internalJob, internalJob.getTask("task1"));
        System.out.println("Job started");
        dbManager.jobTaskStarted(internalJob, task, true);

        internalJob = loadInternalJob(true, internalJob.getId());
        Assert.assertEquals(TaskStatus.RUNNING, internalJob.getTask("task1").getStatus());
        Assert.assertEquals(TaskStatus.PENDING, internalJob.getTask("task2").getStatus());
        Assert.assertEquals(TaskStatus.PENDING, internalJob.getTask("task3").getStatus());
    }

}
