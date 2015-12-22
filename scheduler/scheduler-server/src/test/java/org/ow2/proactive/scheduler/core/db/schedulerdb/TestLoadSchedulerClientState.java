package org.ow2.proactive.scheduler.core.db.schedulerdb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.junit.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.db.RecoveredSchedulerState;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestLoadSchedulerClientState extends BaseSchedulerDBTest {

    @Test
    public void testStateAfterTaskFinished() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        JavaTask taskDef1 = createDefaultTask("task1");
        JavaTask taskDef2 = createDefaultTask("task2");
        taskDef2.addDependence(taskDef1);
        jobDef.addTask(taskDef1);
        jobDef.addTask(taskDef2);

        InternalJob job = defaultSubmitJobAndLoadInternal(true, jobDef);
        InternalTask task1 = job.getTask("task1");

        job.start();
        startTask(job, task1);
        dbManager.jobTaskStarted(job, task1, true);

        TaskResultImpl result = new TaskResultImpl(null, new TestResult(1, "res1"), null, 1000);
        terminateTask(job, task1, result);
        dbManager.updateAfterTaskFinished(job, task1, result);

        SchedulerStateRecoverHelper stateRecoverHelper = new SchedulerStateRecoverHelper(dbManager);
        RecoveredSchedulerState recovered;

        recovered = stateRecoverHelper.recover(-1);

        JobStateMatcher expectedJob;

        expectedJob = job(job.getId(), JobStatus.STALLED).withFinished(
                task("task1", TaskStatus.FINISHED).checkFinished()).withPending(
                task("task2", TaskStatus.PENDING), true).withEligible("task2");

        checkRecoveredState(recovered, state().withRunning(expectedJob));

        job = recovered.getRunningJobs().get(0);
        InternalTask task2 = job.getTask("task2");

        startTask(job, task2);
        dbManager.jobTaskStarted(job, task2, false);

        expectedJob = job(job.getId(), JobStatus.STALLED).withFinished(
                task("task1", TaskStatus.FINISHED).checkFinished()).withPending(
                task("task2", TaskStatus.PENDING), true).withEligible("task2");
        recovered = stateRecoverHelper.recover(-1);
        checkRecoveredState(recovered, state().withRunning(expectedJob));

        job = recovered.getRunningJobs().get(0);
        task2 = job.getTask("task2");

        startTask(job, task2);
        dbManager.jobTaskStarted(job, task2, false);
        terminateTask(job, task2, result);
        dbManager.updateAfterTaskFinished(job, task2, result);

        expectedJob = job(job.getId(), JobStatus.FINISHED).withFinished(
                task("task1", TaskStatus.FINISHED).checkFinished()).withFinished(
                task("task2", TaskStatus.FINISHED).checkFinished());
        recovered = stateRecoverHelper.recover(-1);
        checkRecoveredState(recovered, state().withFinished(expectedJob));
    }

    protected void terminateTask(InternalJob job, InternalTask task, TaskResultImpl result) {
        task.setFinishedTime(System.currentTimeMillis());
        task.setStatus(TaskStatus.FINISHED);
        task.setExecutionDuration(result.getTaskDuration());
        job.setNumberOfRunningTasks(job.getNumberOfRunningTasks() - 1);
        job.setNumberOfFinishedTasks(job.getNumberOfFinishedTasks() + 1);

        if (job.isFinished()) {
            job.terminate();
        }
    }

    @Test
    public void testStateAfterJobEnd() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.addTask(createDefaultTask("task1"));
        InternalJob job = defaultSubmitJobAndLoadInternal(false, jobDef);
        dbManager.removeJob(job.getId(), System.currentTimeMillis(), false);

        jobDef = new TaskFlowJob();
        jobDef.addTask(createDefaultTask("task1"));
        jobDef.addTask(createDefaultTask("task2"));

        job = defaultSubmitJobAndLoadInternal(true, jobDef);
        InternalTask task1 = job.getTask("task1");
        InternalTask task2 = job.getTask("task2");

        job.start();
        startTask(job, task1);
        dbManager.jobTaskStarted(job, task1, true);
        startTask(job, task2);
        dbManager.jobTaskStarted(job, task2, false);

        // task 2 finished with error, stop job
        Set<TaskId> ids = job.failed(task2.getId(), JobStatus.CANCELED);
        TaskResultImpl res = new TaskResultImpl(null, new TestException("message", "data"), null, 0);
        dbManager.updateAfterJobFailed(job, task2, res, ids);

        SchedulerStateRecoverHelper stateRecoverHelper = new SchedulerStateRecoverHelper(dbManager);

        JobStateMatcher expectedJob = job(job.getId(), JobStatus.CANCELED).withFinished(
                task("task1", TaskStatus.ABORTED).checkFinished(), false).withFinished(
                task("task2", TaskStatus.FAULTY).checkFinished()).checkFinished();

        checkRecoveredState(stateRecoverHelper.recover(-1), state().withFinished(expectedJob));
    }

    @Test
    public void testClientStateLoading() throws Exception {
        TaskFlowJob job1 = new TaskFlowJob();
        job1.setName(this.getClass().getSimpleName());
        job1.setDescription("desc1");
        job1.setProjectName("p1");
        job1.setInputSpace("is1");
        job1.setOutputSpace("os1");
        job1.setMaxNumberOfExecution(22);
        job1.setCancelJobOnError(false);
        JavaTask task1 = createDefaultTask("task1");
        task1.setDescription("d1");
        task1.setCancelJobOnError(true);
        task1.setMaxNumberOfExecution(4);
        task1.setPreciousLogs(true);
        task1.setPreciousResult(true);
        task1.setRunAsMe(true);
        task1.setWallTime(440000);
        JavaTask task2 = createDefaultTask("task2");
        task2.setDescription("d2");
        task2.setCancelJobOnError(false);
        task2.setMaxNumberOfExecution(3);
        task2.setPreciousLogs(false);
        task2.setPreciousResult(false);
        task2.setRunAsMe(false);
        task2.setWallTime(240000);
        JavaTask task3 = createDefaultTask("task3");
        task1.addDependence(task2);
        task1.addDependence(task3);
        task2.addDependence(task3);
        job1.addTask(task1);
        job1.addTask(task2);
        job1.addTask(task3);
        job1.setPriority(JobPriority.LOW);
        Map<String, String> genericInfo = new HashMap<>();
        genericInfo.put("p1", "v1");
        genericInfo.put("p2", "v2");
        job1.setGenericInformations(genericInfo);

        InternalJob jobData1 = defaultSubmitJob(job1);

        TaskFlowJob job2 = new TaskFlowJob();
        job2.setName(this.getClass().getSimpleName() + "_2");
        job2.setGenericInformations(new HashMap<String, String>());
        job2.addTask(createDefaultTask("task1"));
        job2.setPriority(JobPriority.HIGH);
        InternalJob jobData2 = defaultSubmitJob(job2);

        System.out.println("Load scheduler client state");

        SchedulerStateRecoverHelper stateRecoverHelper = new SchedulerStateRecoverHelper(dbManager);

        SchedulerState state = stateRecoverHelper.recover(-1).getSchedulerState();

        Assert.assertEquals("Unexpected jobs number", 2, state.getPendingJobs().size());

        JobState jobState;

        jobState = checkJobData(state.getPendingJobs(), jobData1.getId(), job1, 3);

        checkTaskData(task1, findTask(jobState, "task1"), "task2", "task3");
        checkTaskData(task2, findTask(jobState, "task2"), "task3");
        checkTaskData(task3, findTask(jobState, "task3"));

        checkJobData(state.getPendingJobs(), jobData2.getId(), job2, 1);

    }

    private void checkTaskData(JavaTask expected, TaskState taskState, String... dependences) {
        Assert.assertEquals(expected.getName(), taskState.getName());
        Assert.assertEquals(expected.getDescription(), taskState.getDescription());
        Assert.assertEquals(expected.getName(), taskState.getTaskInfo().getTaskId().getReadableName());
        Assert.assertEquals(expected.isCancelJobOnError(), taskState.isCancelJobOnError());
        Assert.assertEquals(expected.getMaxNumberOfExecution(), taskState.getMaxNumberOfExecution());
        Assert.assertEquals(expected.isPreciousLogs(), taskState.isPreciousLogs());
        Assert.assertEquals(expected.isPreciousResult(), taskState.isPreciousResult());
        Assert.assertEquals(expected.isRunAsMe(), taskState.isRunAsMe());
        Assert.assertEquals(expected.getWallTime(), taskState.getWallTime());

        Assert.assertEquals("Unexpected number of dependencies", dependences.length, taskState
                .getDependences().size());
        Set<String> dependenciesSet = new HashSet<>();
        for (String dependecy : dependences) {
            dependenciesSet.add(dependecy);
        }
        Set<String> actualDependenciesSet = new HashSet<>();
        for (TaskState task : taskState.getDependences()) {
            actualDependenciesSet.add(task.getName());
        }
        Assert.assertEquals("Unexpected dependencies", dependenciesSet, actualDependenciesSet);
    }

    private JobState checkJobData(Vector<JobState> jobList, JobId id, TaskFlowJob job, int tasksNumber) {
        for (JobState state : jobList) {
            if (state.getJobInfo().getJobId().equals(id)) {
                Assert.assertEquals(job.getName(), state.getId().getReadableName());
                Assert.assertEquals(job.getName(), state.getName());
                Assert.assertEquals(job.getPriority(), state.getPriority());
                Assert.assertEquals(job.getGenericInformation(), state.getGenericInformation());
                Assert.assertEquals("Unexpected tasks number", tasksNumber, state.getTasks().size());
                Assert.assertEquals(JobType.TASKSFLOW, state.getType());
                Assert.assertEquals(DEFAULT_USER_NAME, state.getOwner());
                Assert.assertEquals(job.getDescription(), state.getDescription());
                Assert.assertEquals(job.getProjectName(), state.getProjectName());
                Assert.assertEquals(job.getInputSpace(), state.getInputSpace());
                Assert.assertEquals(job.getOutputSpace(), state.getOutputSpace());
                Assert.assertEquals(job.isCancelJobOnError(), state.isCancelJobOnError());
                Assert.assertEquals(job.getMaxNumberOfExecution(), state.getMaxNumberOfExecution());

                Assert.assertEquals(0, state.getNumberOfFinishedTasks());
                Assert.assertEquals(0, state.getNumberOfRunningTasks());
                Assert.assertEquals(0, state.getNumberOfPendingTasks());
                Assert.assertEquals(tasksNumber, state.getTotalNumberOfTasks());

                return state;
            }
        }

        Assert.fail("Failed to find job " + id);
        return null;
    }

}
