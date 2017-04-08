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
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestLoadJobResult extends BaseSchedulerDBTest {

    @Test
    public void testEmptyResult() throws Throwable {
        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.addTask(createDefaultTask("task1"));

        InternalJob job = defaultSubmitJobAndLoadInternal(true, jobDef);
        InternalTask task1 = job.getTask("task1");

        JobResult result;

        result = dbManager.loadJobResult(job.getId());
        Assert.assertNotNull(result.getJobInfo());
        Assert.assertEquals(0, result.getAllResults().size());
        Assert.assertEquals(1, result.getJobInfo().getTotalNumberOfTasks());

        dbManager.updateAfterTaskFinished(job, task1, new TaskResultImpl(null, new TestResult(0, "1_1"), null, 0));

        result = dbManager.loadJobResult(job.getId());
        Assert.assertNotNull(result.getJobInfo());
        Assert.assertEquals(1, result.getAllResults().size());
        Assert.assertEquals(1, result.getJobInfo().getTotalNumberOfTasks());
    }

    @Test
    public void testLoadJobResult() throws Throwable {
        TaskFlowJob job = new TaskFlowJob();

        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.addAdditionalClasspath("/path1/path1");
        forkEnvironment.addAdditionalClasspath("/path2/path2");

        JavaTask javaTask1 = createDefaultTask("task1");
        javaTask1.setPreciousResult(true);
        javaTask1.setForkEnvironment(forkEnvironment);

        job.addTask(javaTask1);

        for (int i = 2; i <= 4; i++) {
            JavaTask task = createDefaultTask("task" + i);
            task.setForkEnvironment(forkEnvironment);
            job.addTask(task);
        }

        JavaTask javaTask5 = createDefaultTask("task5");
        javaTask5.setPreciousResult(true);
        javaTask5.setForkEnvironment(forkEnvironment);
        job.addTask(javaTask5);

        InternalJob internalJob = defaultSubmitJobAndLoadInternal(true, job);
        InternalTask task1 = internalJob.getTask("task1");
        InternalTask task2 = internalJob.getTask("task2");
        InternalTask task3 = internalJob.getTask("task3");
        InternalTask task4 = internalJob.getTask("task4");
        InternalTask task5 = internalJob.getTask("task5");

        dbManager.updateAfterTaskFinished(internalJob,
                                          task1,
                                          new TaskResultImpl(null, new TestResult(0, "1_1"), null, 0));
        dbManager.updateAfterTaskFinished(internalJob,
                                          task1,
                                          new TaskResultImpl(null, new TestResult(0, "1_2"), null, 0));

        dbManager.updateAfterTaskFinished(internalJob,
                                          task2,
                                          new TaskResultImpl(null, new TestResult(0, "2_1"), null, 0));
        dbManager.updateAfterTaskFinished(internalJob,
                                          task2,
                                          new TaskResultImpl(null, new TestResult(0, "2_2"), null, 0));

        dbManager.updateAfterTaskFinished(internalJob,
                                          task3,
                                          new TaskResultImpl(null, new TestResult(0, "3_1"), null, 0));

        dbManager.updateAfterTaskFinished(internalJob,
                                          task4,
                                          new TaskResultImpl(null,
                                                             new TestException("message4_1", "data4_1"),
                                                             null,
                                                             0));
        dbManager.updateAfterTaskFinished(internalJob,
                                          task4,
                                          new TaskResultImpl(null,
                                                             new TestException("message4_2", "data4_2"),
                                                             null,
                                                             0));

        dbManager.updateAfterTaskFinished(internalJob,
                                          task5,
                                          new TaskResultImpl(null,
                                                             new TestException("message5_1", "data5_1"),
                                                             null,
                                                             0));

        TaskFlowJob job2 = new TaskFlowJob();
        job2.addTask(createDefaultTask("job2 task1"));
        InternalJob internalJob2 = defaultSubmitJobAndLoadInternal(true, job2);
        InternalTask task2_1 = internalJob2.getTask("job2 task1");
        dbManager.updateAfterTaskFinished(internalJob2,
                                          task2_1,
                                          new TaskResultImpl(null, new TestResult(0, "job2_task1"), null, 0));

        System.out.println("Load job result1");
        JobResult result = dbManager.loadJobResult(internalJob.getId());
        Assert.assertEquals(5, result.getAllResults().size());
        Assert.assertEquals(2, result.getExceptionResults().size());
        Assert.assertEquals(2, result.getPreciousResults().size());
        Assert.assertNotNull(result.getJobInfo());
        Assert.assertEquals(internalJob.getId(), result.getJobId());
        Assert.assertEquals(5, result.getJobInfo().getTotalNumberOfTasks());

        TestResult taskResultValue;

        taskResultValue = (TestResult) result.getResult("task1").value();
        Assert.assertEquals("1_2", taskResultValue.getB());
        taskResultValue = (TestResult) result.getResult("task2").value();
        Assert.assertEquals("2_2", taskResultValue.getB());
        taskResultValue = (TestResult) result.getResult("task3").value();
        Assert.assertEquals("3_1", taskResultValue.getB());

        TestException taskException;
        taskException = (TestException) result.getResult("task4").getException();
        Assert.assertEquals("message4_2", taskException.getMessage());
        taskException = (TestException) result.getResult("task5").getException();
        Assert.assertEquals("message5_1", taskException.getMessage());

        System.out.println("Load job result2");
        result = dbManager.loadJobResult(internalJob2.getId());
        Assert.assertEquals(1, result.getAllResults().size());
        Assert.assertEquals(0, result.getExceptionResults().size());
        Assert.assertEquals(0, result.getPreciousResults().size());
    }

    @Test
    public void testInvalidJobId() throws Exception {
        JobId jobId = new JobIdImpl(Long.MAX_VALUE, "dummy");
        System.out.println("Load result for invalid jobId");
        Assert.assertNull(dbManager.loadJobResult(jobId));
    }

}
