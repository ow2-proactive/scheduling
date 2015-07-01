package functionaltests.schedulerdb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.types.BigString;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;
import org.junit.Test;


public class TestTaskResultData extends BaseSchedulerDBTest {

    @Test
    public void testInvalidTask() throws Throwable {
        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.addTask(createDefaultTask("testTask"));
        InternalJob job = defaultSubmitJob(jobDef);

        // try to call when task exists but there is no result
        Assert.assertNull(dbManager.loadTaskResult(job.getId(), "testTask", 0));
        Assert.assertNull(dbManager.loadTaskResult(job.getId(), "testTask", 1));

        // try to call with invalid task name and invalid jobId
        try {
            dbManager.loadTaskResult(job.getId(), "testTask1", 1);
            Assert.fail();
        } catch (DatabaseManagerException e) {
            // expected
        }
        try {
            dbManager.loadTaskResult(JobIdImpl.makeJobId("12345789"), "testTask", 1);
            Assert.fail();
        } catch (DatabaseManagerException e) {
            // expected
        }
    }

    @Test
    public void testMultipleResults() throws Throwable {
        TaskFlowJob job = new TaskFlowJob();
        job.addTask(createDefaultTask("task1"));
        job.addTask(createDefaultTask("task2"));
        job.addTask(createDefaultTask("task3"));

        InternalJob internalJob = defaultSubmitJobAndLoadInternal(true, job);
        InternalTask task1 = internalJob.getTask("task1");
        InternalTask task2 = internalJob.getTask("task2");
        InternalTask task3 = internalJob.getTask("task3");

        dbManager.updateAfterTaskFinished(internalJob, task1, new TaskResultImpl(null, new TestResult(0,
            "1_1"), null, 0));
        dbManager.updateAfterTaskFinished(internalJob, task1, new TaskResultImpl(null, new TestResult(0,
            "1_2"), null, 0));
        dbManager.updateAfterTaskFinished(internalJob, task1, new TaskResultImpl(null, new TestResult(0,
            "1_3"), null, 0));

        dbManager.updateAfterTaskFinished(internalJob, task2, new TaskResultImpl(null, new TestResult(0,
            "2_1"), null, 0));
        dbManager.updateAfterTaskFinished(internalJob, task2, new TaskResultImpl(null, new TestResult(0,
            "2_2"), null, 0));
        dbManager.updateAfterTaskFinished(internalJob, task2, new TaskResultImpl(null, new TestResult(0,
            "2_3"), null, 0));

        dbManager.updateAfterTaskFinished(internalJob, task3, new TaskResultImpl(null, new TestResult(0,
            "3_1"), null, 0));
        dbManager.updateAfterTaskFinished(internalJob, task3, new TaskResultImpl(null, new TestResult(0,
            "3_2"), null, 0));
        dbManager.updateAfterTaskFinished(internalJob, task3, new TaskResultImpl(null, new TestResult(0,
            "3_3"), null, 0));

        TestResult result;

        result = (TestResult) dbManager.loadLastTaskResult(task1.getId()).value();
        Assert.assertEquals("1_3", result.getB());
        result = (TestResult) dbManager.loadTaskResult(internalJob.getId(), task1.getName(), 0).value();
        Assert.assertEquals("1_3", result.getB());

        result = (TestResult) dbManager.loadLastTaskResult(task2.getId()).value();
        Assert.assertEquals("2_3", result.getB());
        result = (TestResult) dbManager.loadTaskResult(internalJob.getId(), task2.getName(), 0).value();
        Assert.assertEquals("2_3", result.getB());

        result = (TestResult) dbManager.loadLastTaskResult(task3.getId()).value();
        Assert.assertEquals("3_3", result.getB());
        result = (TestResult) dbManager.loadTaskResult(internalJob.getId(), task3.getName(), 0).value();
        Assert.assertEquals("3_3", result.getB());

        result = (TestResult) dbManager.loadTaskResult(task2.getId(), 0).value();
        Assert.assertEquals("2_3", result.getB());
        result = (TestResult) dbManager.loadTaskResult(internalJob.getId(), task2.getName(), 0).value();
        Assert.assertEquals("2_3", result.getB());

        result = (TestResult) dbManager.loadTaskResult(task2.getId(), 1).value();
        Assert.assertEquals("2_2", result.getB());
        result = (TestResult) dbManager.loadTaskResult(internalJob.getId(), task2.getName(), 1).value();
        Assert.assertEquals("2_2", result.getB());

        result = (TestResult) dbManager.loadTaskResult(task2.getId(), 2).value();
        Assert.assertEquals("2_1", result.getB());
        result = (TestResult) dbManager.loadTaskResult(internalJob.getId(), task2.getName(), 2).value();
        Assert.assertEquals("2_1", result.getB());

        Assert.assertNull(dbManager.loadTaskResult(task2.getId(), 3));
        Assert.assertNull(dbManager.loadTaskResult(internalJob.getId(), task2.getName(), 3));

        List<TaskId> taskIds = Arrays.asList(task1.getId(), task2.getId(), task3.getId());
        System.out.println("Load results for 3 tasks");
        Map<TaskId, TaskResult> results = dbManager.loadTasksResults(internalJob.getId(), taskIds);
        Assert.assertEquals(3, results.size());
        result = (TestResult) results.get(task1.getId()).value();
        Assert.assertEquals("1_3", result.getB());
        result = (TestResult) results.get(task2.getId()).value();
        Assert.assertEquals("2_3", result.getB());
        result = (TestResult) results.get(task3.getId()).value();
        Assert.assertEquals("3_3", result.getB());

        taskIds = Arrays.asList(task2.getId(), task3.getId());
        System.out.println("Load results for 2 tasks");
        results = dbManager.loadTasksResults(internalJob.getId(), taskIds);
        Assert.assertEquals(2, results.size());
        result = (TestResult) results.get(task2.getId()).value();
        Assert.assertEquals("2_3", result.getB());
        result = (TestResult) results.get(task3.getId()).value();
        Assert.assertEquals("3_3", result.getB());
    }

    @Test
    public void testMultipleJobs() throws Throwable {
        // two jobs have tasks with the same name

        TaskFlowJob jobDef1 = new TaskFlowJob();
        jobDef1.addTask(createDefaultTask("task1"));

        TaskFlowJob jobDef2 = new TaskFlowJob();
        jobDef2.addTask(createDefaultTask("task1"));

        InternalJob job1 = defaultSubmitJobAndLoadInternal(true, jobDef1);
        InternalJob job2 = defaultSubmitJobAndLoadInternal(true, jobDef2);

        dbManager.updateAfterTaskFinished(job1, job1.getTask("task1"), new TaskResultImpl(null,
            new TestResult(0, "job1Res"), null, 0));
        dbManager.updateAfterTaskFinished(job2, job2.getTask("task1"), new TaskResultImpl(null,
                new TestResult(0, "job2Res"), null, 0));

        TestResult result;

        result = (TestResult) dbManager.loadTaskResult(job1.getId(), "task1", 0).value();
        Assert.assertEquals("job1Res", result.getB());

        result = (TestResult) dbManager.loadTaskResult(job2.getId(), "task1", 0).value();
        Assert.assertEquals("job2Res", result.getB());
    }

    @Test
    public void testFlowAction() throws Exception {
        InternalJob job = saveSingleTask(createDefaultTask("task"));
        InternalTask task = (InternalTask) job.getTasks().get(0);
        TaskResultImpl result = new TaskResultImpl(null, new TestResult(10, "12345"), null, 0);
        FlowAction action = new FlowAction(FlowActionType.LOOP);
        action.setDupNumber(33);
        action.setTarget("t");
        action.setTargetContinuation("tc");
        action.setTargetElse("te");
        result.setAction(action);
        dbManager.updateAfterTaskFinished(job, task, result);

        TaskResultImpl restoredResult = (TaskResultImpl) dbManager.loadLastTaskResult(task.getId());
        FlowAction restoredAction = restoredResult.getAction();
        Assert.assertNotNull(restoredAction);
        Assert.assertEquals(FlowActionType.LOOP, restoredAction.getType());
        Assert.assertEquals(33, restoredAction.getDupNumber());
        Assert.assertEquals("t", restoredAction.getTarget());
        Assert.assertEquals("tc", restoredAction.getTargetContinuation());
        Assert.assertEquals("te", restoredAction.getTargetElse());

        restoredResult = (TaskResultImpl) dbManager.loadTaskResult(job.getId(), "task", 0);
        restoredAction = restoredResult.getAction();
        Assert.assertEquals(FlowActionType.LOOP, restoredAction.getType());
    }

    @Test
    public void testResult() throws Throwable {
        InternalJob job = saveSingleTask(createDefaultTask("task"));
        TaskResultImpl result = new TaskResultImpl(null, new TestResult(10, "12345"), null, 0);
        String previewer = "org.ow2.proactive.scheduler.common.org.ow2.proactive.scheduler.common.ClassName";
        result.setPreviewerClassName(previewer);
        InternalTask task = (InternalTask) job.getTasks().get(0);
        System.out.println("Add task result");
        dbManager.updateAfterTaskFinished(job, task, result);

        System.out.println("Get last task result");
        TaskResultImpl restoredResult = (TaskResultImpl) dbManager.loadLastTaskResult(task.getId());
        Assert.assertEquals(task.getId(), restoredResult.getTaskId());
        Assert.assertNull(restoredResult.getException());
        Assert.assertNull(restoredResult.getOutput());
        TestResult value = (TestResult) restoredResult.value();
        Assert.assertNotNull(value);
        Assert.assertEquals(10, value.getA());
        Assert.assertEquals("12345", value.getB());
        Assert.assertEquals(previewer, restoredResult.getPreviewerClassName());
        Assert.assertNull(restoredResult.getPropagatedProperties());
        Assert.assertNull(restoredResult.getAction());
    }

    @Test
    public void testExceptionResult() throws Throwable {
        InternalJob job = saveSingleTask(createDefaultTask("task"));

        TaskResultImpl result = new TaskResultImpl(null, new TestException("message", "data"), null, 0);
        InternalTask task = (InternalTask) job.getTasks().get(0);
        dbManager.updateAfterTaskFinished(job, task, result);

        TaskResult restoredResult = dbManager.loadLastTaskResult(task.getId());
        TestException exception = (TestException) restoredResult.getException();
        Assert.assertNotNull(exception);
        Assert.assertEquals("message", exception.getMessage());
        Assert.assertEquals("data", exception.getData());
        try {
            restoredResult.value();
            Assert.fail("Exception is expected");
        } catch (TestException e) {
        }
        Assert.assertNull(restoredResult.getPropagatedProperties());
        Assert.assertNull(restoredResult.getOutput());
    }

    @Test
    public void testPropagatedProperties() throws Exception {
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longString.append("0123456789abcdefghijklmnopqrstuvwxyz");
        }
        Map<String, BigString> properties = new HashMap<String, BigString>();
        properties.put("longProperty", new BigString(longString.toString()));
        properties.put("property", new BigString("value"));

        InternalJob job = saveSingleTask(createDefaultTask("task"));
        InternalTask task = (InternalTask) job.getTasks().get(0);

        TaskResultImpl result = new TaskResultImpl(null, "result", null, 0, properties);
        dbManager.updateAfterTaskFinished(job, task, result);

        TaskResult restoredResult = dbManager.loadLastTaskResult(task.getId());
        Assert.assertNotNull(restoredResult.getPropagatedProperties());
        Assert.assertEquals(longString.toString(), restoredResult.getPropagatedProperties().get(
                "longProperty"));
        Assert.assertEquals("value", restoredResult.getPropagatedProperties().get("property"));
    }

    @Test
    public void testSimpleLogs() throws Exception {
        InternalJob job = saveSingleTask(createDefaultTask("task"));
        InternalTask task = (InternalTask) job.getTasks().get(0);

        TaskResultImpl result = new TaskResultImpl(null, "result",
            new SimpleTaskLogs("stdLogs", "errorLogs"), 0);
        dbManager.updateAfterTaskFinished(job, task, result);

        TaskResult restoredResult = dbManager.loadLastTaskResult(task.getId());
        TaskLogs logs = restoredResult.getOutput();
        Assert.assertNotNull(logs);
        Assert.assertEquals("stdLogs", logs.getStdoutLogs(false));
        Assert.assertEquals("errorLogs", logs.getStderrLogs(false));
    }

    @Test
    public void testLog4jLogs() throws Exception {
        InternalJob job = saveSingleTask(createDefaultTask("task"));
        InternalTask task = (InternalTask) job.getTasks().get(0);

        LinkedList<LoggingEvent> events = new LinkedList<LoggingEvent>();
        for (int i = 0; i < 3; i++) {
            events.add(new LoggingEvent("", Logger.getLogger(TestTaskResultData.class), Level.INFO, "info" +
                i, null));
            events.add(new LoggingEvent("", Logger.getLogger(TestTaskResultData.class), Level.ERROR, "error" +
                i, null));
        }

        TaskResultImpl result = new TaskResultImpl(null, "result", new Log4JTaskLogs(events, "0"), 0);
        dbManager.updateAfterTaskFinished(job, task, result);

        TaskResult restoredResult = dbManager.loadLastTaskResult(task.getId());
        TaskLogs logs = restoredResult.getOutput();
        Assert.assertNotNull(logs);

        String logsString = logs.getStdoutLogs(false);
        Assert.assertTrue(logsString.contains("info0"));
        Assert.assertTrue(logsString.contains("info1"));
        Assert.assertTrue(logsString.contains("info2"));
        logsString = logs.getStderrLogs(false);
        Assert.assertTrue(logsString.contains("error0"));
        Assert.assertTrue(logsString.contains("error1"));
        Assert.assertTrue(logsString.contains("error2"));
    }

}
