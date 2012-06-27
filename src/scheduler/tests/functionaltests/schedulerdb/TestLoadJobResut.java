package functionaltests.schedulerdb;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.types.BigString;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestLoadJobResut extends BaseSchedulerDBTest {

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

        dbManager.updateAfterTaskFinished(job, task1, new TaskResultImpl(null, new TestResult(0, "1_1"),
            null, 0, null));

        result = dbManager.loadJobResult(job.getId());
        Assert.assertNotNull(result.getJobInfo());
        Assert.assertEquals(1, result.getAllResults().size());
        Assert.assertEquals(1, result.getJobInfo().getTotalNumberOfTasks());
    }

    @Test
    public void testLoadJobResult() throws Throwable {
        TaskFlowJob job = new TaskFlowJob();
        JobEnvironment env = new JobEnvironment();
        String[] classpath = { "/path1/path1", "/path2/path2" };
        env.setJobClasspath(classpath);
        job.setEnvironment(env);
        JavaTask javaTask1 = createDefaultTask("task1");
        javaTask1.setPreciousResult(true);
        job.addTask(javaTask1);
        job.addTask(createDefaultTask("task2"));
        job.addTask(createDefaultTask("task3"));
        job.addTask(createDefaultTask("task4"));
        JavaTask javaTask5 = createDefaultTask("task5");
        javaTask5.setPreciousResult(true);
        job.addTask(javaTask5);

        InternalJob internalJob = defaultSubmitJobAndLoadInternal(true, job);
        InternalTask task1 = internalJob.getTask("task1");
        InternalTask task2 = internalJob.getTask("task2");
        InternalTask task3 = internalJob.getTask("task3");
        InternalTask task4 = internalJob.getTask("task4");
        InternalTask task5 = internalJob.getTask("task5");

        Map<String, BigString> properties1 = new HashMap<String, BigString>();
        properties1.put("property1", new BigString("value1"));
        properties1.put("property2", new BigString("value2"));

        Map<String, BigString> properties2 = new HashMap<String, BigString>();
        properties2.put("property1", new BigString("value1"));
        properties2.put("property2", new BigString("value2"));
        properties2.put("property3", new BigString("value3"));
        properties2.put("property4", new BigString("value4"));

        dbManager.updateAfterTaskFinished(internalJob, task1, new TaskResultImpl(null, new TestResult(0,
            "1_1"), null, 0, properties1));
        dbManager.updateAfterTaskFinished(internalJob, task1, new TaskResultImpl(null, new TestResult(0,
            "1_2"), null, 0, properties1));

        dbManager.updateAfterTaskFinished(internalJob, task2, new TaskResultImpl(null, new TestResult(0,
            "2_1"), null, 0, properties2));
        dbManager.updateAfterTaskFinished(internalJob, task2, new TaskResultImpl(null, new TestResult(0,
            "2_2"), null, 0, properties2));

        dbManager.updateAfterTaskFinished(internalJob, task3, new TaskResultImpl(null, new TestResult(0,
            "3_1"), null, 0));

        dbManager.updateAfterTaskFinished(internalJob, task4, new TaskResultImpl(null, new TestException(
            "message4_1", "data4_1"), null, 0));
        dbManager.updateAfterTaskFinished(internalJob, task4, new TaskResultImpl(null, new TestException(
            "message4_2", "data4_2"), null, 0));

        dbManager.updateAfterTaskFinished(internalJob, task5, new TaskResultImpl(null, new TestException(
            "message5_1", "data5_1"), null, 0));

        TaskFlowJob job2 = new TaskFlowJob();
        job2.addTask(createDefaultTask("job2 task1"));
        InternalJob internalJob2 = defaultSubmitJobAndLoadInternal(true, job2);
        InternalTask task2_1 = internalJob2.getTask("job2 task1");
        dbManager.updateAfterTaskFinished(internalJob2, task2_1, new TaskResultImpl(null, new TestResult(0,
            "job2_task1"), null, 0));

        System.out.println("Load job result1");
        JobResult result = dbManager.loadJobResult(internalJob.getId());
        Assert.assertEquals(5, result.getAllResults().size());
        Assert.assertEquals(2, result.getExceptionResults().size());
        Assert.assertEquals(2, result.getPreciousResults().size());
        Assert.assertNotNull(result.getJobInfo());
        Assert.assertEquals(internalJob.getId(), result.getJobId());
        Assert.assertEquals(5, result.getJobInfo().getTotalNumberOfTasks());

        for (TaskResult taskResult : result.getAllResults().values()) {
            Assert.assertArrayEquals(classpath, ((TaskResultImpl) taskResult).getJobClasspath());
        }

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

        TaskResult taskResult;

        taskResult = result.getResult("task1");
        Assert.assertEquals(2, taskResult.getPropagatedProperties().size());
        Assert.assertEquals("value1", taskResult.getPropagatedProperties().get("property1"));
        Assert.assertEquals("value2", taskResult.getPropagatedProperties().get("property2"));

        taskResult = result.getResult("task2");
        Assert.assertEquals(4, taskResult.getPropagatedProperties().size());

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
        try {
            dbManager.loadJobResult(jobId);
            Assert.fail("Exception was expected");
        } catch (DatabaseManagerException e) {

        }
    }

}
