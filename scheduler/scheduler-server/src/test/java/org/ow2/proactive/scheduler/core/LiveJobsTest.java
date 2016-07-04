package org.ow2.proactive.scheduler.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.python.google.common.collect.ImmutableSet;


public class LiveJobsTest {

    private LiveJobs liveJobs;

    @Mock
    private SchedulerDBManager dbManager;
    @Mock
    private SchedulerStateUpdate listener;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        liveJobs = new LiveJobs(dbManager, listener);
    }

    @Test
    public void testPriorityConflict() throws Exception {
        TreeSet<JobPriority> scheduled = createTreeSet(JobPriority.LOW);
        TreeSet<JobPriority> notScheduled = createTreeSet(JobPriority.HIGHEST);

        Assert.assertTrue(liveJobs.priorityConflict(scheduled, notScheduled));

        scheduled = createTreeSet(JobPriority.LOW);
        notScheduled = createTreeSet(JobPriority.LOW);

        Assert.assertFalse(liveJobs.priorityConflict(scheduled, notScheduled));

        scheduled = createTreeSet(JobPriority.HIGHEST);
        notScheduled = createTreeSet(JobPriority.LOW);

        Assert.assertFalse(liveJobs.priorityConflict(scheduled, notScheduled));

        scheduled = createTreeSet(JobPriority.LOW, JobPriority.HIGHEST);
        notScheduled = createTreeSet(JobPriority.NORMAL);

        Assert.assertTrue(liveJobs.priorityConflict(scheduled, notScheduled));

        scheduled = createTreeSet(JobPriority.HIGHEST, JobPriority.LOW);
        notScheduled = createTreeSet(JobPriority.NORMAL);

        Assert.assertTrue(liveJobs.priorityConflict(scheduled, notScheduled));

        scheduled = createTreeSet(JobPriority.LOW, JobPriority.NORMAL);
        notScheduled = createTreeSet(JobPriority.NORMAL);

        Assert.assertTrue(liveJobs.priorityConflict(scheduled, notScheduled));

        scheduled = createTreeSet(JobPriority.NORMAL, JobPriority.HIGHEST);
        notScheduled = createTreeSet(JobPriority.NORMAL);

        Assert.assertFalse(liveJobs.priorityConflict(scheduled, notScheduled));

        scheduled = createTreeSet(JobPriority.NORMAL);
        notScheduled = createTreeSet(JobPriority.LOW, JobPriority.HIGHEST);

        Assert.assertTrue(liveJobs.priorityConflict(scheduled, notScheduled));

        scheduled = createTreeSet(JobPriority.NORMAL);
        notScheduled = createTreeSet(JobPriority.LOW, JobPriority.NORMAL);

        Assert.assertFalse(liveJobs.priorityConflict(scheduled, notScheduled));

        scheduled = createTreeSet(JobPriority.NORMAL);
        notScheduled = createTreeSet(JobPriority.NORMAL, JobPriority.HIGHEST);

        Assert.assertTrue(liveJobs.priorityConflict(scheduled, notScheduled));
    }

    @NotNull
    private TreeSet<JobPriority> createTreeSet(JobPriority... priorities) {
        return new TreeSet<>(ImmutableSet.copyOf(priorities));
    }

    @Test
    public void testGetRunningTasksEmpty() {
        assertThat(liveJobs.getRunningTasks().isEmpty(), is(true));
    }

    @Test
    public void testJobSubmitted() {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        liveJobs.jobSubmitted(job);
        Mockito.verify(listener, Mockito.times(1)).jobSubmitted(Matchers.any(ClientJobState.class));
    }

    @Test
    public void testPauseJob() {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        assertThat(liveJobs.pauseJob(id), is(true));
    }

    @Test
    public void testUpdateStartAt() {
        String startAt = "2017-07-07T00:00:00+01:00";
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        assertThat(liveJobs.updateStartAt(id, startAt), is(true));
    }

    @Test
    public void testResumeUnstartedJob() {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        assertThat(liveJobs.resumeJob(id), is(false));
    }

    @Test
    public void testResumeStartedJob() throws UnknownJobException, UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        internalTask.setName("task-name");
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.pauseJob(id);
        assertThat(liveJobs.resumeJob(id), is(true));
    }

    @Test
    public void testLockJobsToSchedule() throws UnknownJobException, UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        internalTask.setName("task-name");
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.pauseJob(id);
        assertThat(liveJobs.lockJobsToSchedule().size(), is(1));
    }

    @Test(expected = IllegalStateException.class)
    public void testRestartTaskOnNodeFailureRunningExceptionExpectedBecauseNotLockedTask()
            throws UnknownJobException, UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.restartTaskOnNodeFailure(internalTask);
    }

    @Test
    public void testRestartTaskOnNodeRunningDecreasingExecution()
            throws UnknownJobException, UnknownTaskException {

        PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.updateProperty("5");
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalScriptTask internalTask = new InternalScriptTask();
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setMaxNumberOfExecution(5);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule();
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        assertThat(internalTask.getMaxNumberOfExecutionOnFailure(), is(5));
        assertThat(internalTask.getTaskInfo().getNumberOfExecutionOnFailureLeft(), is(5));

        liveJobs.restartTaskOnNodeFailure(internalTask);
        internalTask.setStatus(TaskStatus.RUNNING);

        assertThat(internalTask.getMaxNumberOfExecutionOnFailure(), is(5));
        assertThat(internalTask.getTaskInfo().getNumberOfExecutionOnFailureLeft(), is(4));

        Mockito.verify(dbManager, Mockito.times(1)).taskRestarted(job, internalTask, null);
    }

    @Test
    public void testRestartTaskOnNodeRunning0ExecutionsLeft()
            throws UnknownJobException, UnknownTaskException {

        PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.updateProperty("0");
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalScriptTask internalTask = new InternalScriptTask();
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setMaxNumberOfExecution(5);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule();
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        assertThat(internalTask.getMaxNumberOfExecutionOnFailure(), is(0));
        assertThat(internalTask.getTaskInfo().getNumberOfExecutionOnFailureLeft(), is(0));

        liveJobs.restartTaskOnNodeFailure(internalTask);
        internalTask.setStatus(TaskStatus.RUNNING);

        Mockito.verify(dbManager, Mockito.times(0)).taskRestarted(job, internalTask, null);
    }

    @Test
    public void testCanPingTask() throws UnknownJobException, UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        TaskId taskId = TaskIdImpl.createTaskId(id, "task-name", 777L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule();
        liveJobs.taskStarted(job, job.getTask("task-name"), null);
        assertThat(liveJobs.canPingTask(liveJobs.getRunningTasks().iterator().next()), is(true));
    }

    @Test
    public void testTaskTerminatedWithResultSuspendTaskOnError() throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        TaskId taskId = TaskIdImpl.createTaskId(id, "task-name", 0L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        TaskInfoImpl taskInfoImpl = (TaskInfoImpl) internalTask.getTaskInfo();
        taskInfoImpl.setNumberOfExecutionLeft(2);

        internalTask.setOnTaskError(OnTaskError.PAUSE_TASK);

        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule();
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        TaskResultImpl result = new TaskResultImpl(taskId, new Exception(), null, 330);

        liveJobs.taskTerminatedWithResult(taskId, result);

        assertThat(taskInfoImpl.getNumberOfExecutionLeft(), is(1));

        assertThat(taskInfoImpl.getInErrorTime(), is((taskInfoImpl.getStartTime() + 330)));

        assertThat(taskInfoImpl.getStatus(), is(TaskStatus.IN_ERROR));

        assertThat(job.getStatus(), is(JobStatus.IN_ERROR));

    }

    @Test
    public void testTaskTerminatedWithResultSuspendTaskOnErrorLastExecution() throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        TaskId taskId = TaskIdImpl.createTaskId(id, "task-name", 0L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        TaskInfoImpl taskInfoImpl = (TaskInfoImpl) internalTask.getTaskInfo();
        taskInfoImpl.setNumberOfExecutionLeft(0);

        internalTask.setOnTaskError(OnTaskError.PAUSE_TASK);

        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule();
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        TaskResultImpl result = new TaskResultImpl(taskId, new Exception(), null, 330);

        liveJobs.taskTerminatedWithResult(taskId, result);

        assertThat(taskInfoImpl.getNumberOfExecutionLeft(), is(-1));

        assertThat(taskInfoImpl.getStatus(), is(TaskStatus.FAULTY));

        assertThat(job.getStatus(), is(JobStatus.STALLED));

    }

    @Test
    public void testTaskTerminatedWithResultPauseJobOnError() throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        TaskId taskId = TaskIdImpl.createTaskId(id, "task-name", 0L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        TaskInfoImpl taskInfoImpl = (TaskInfoImpl) internalTask.getTaskInfo();
        taskInfoImpl.setNumberOfExecutionLeft(10);

        internalTask.setOnTaskError(OnTaskError.PAUSE_JOB);

        InternalTask internalTask2 = new InternalScriptTask();
        TaskId taskId2 = TaskIdImpl.createTaskId(id, "task-name2", 1L);
        internalTask2.setId(taskId2);
        internalTask2.setName("task-name2");
        internalTask2.setStatus(TaskStatus.RUNNING);
        internalTask2.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        TaskInfoImpl taskInfoImpl2 = (TaskInfoImpl) internalTask2.getTaskInfo();
        taskInfoImpl2.setNumberOfExecutionLeft(10);

        internalTask2.setOnTaskError(OnTaskError.NONE);

        tasksList.add(internalTask);
        tasksList.add(internalTask2);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule();
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        TaskResultImpl result = new TaskResultImpl(taskId, new Exception());
        liveJobs.taskTerminatedWithResult(taskId, result);

        assertThat(taskInfoImpl.getNumberOfExecutionLeft(), is(9));

        assertThat(taskInfoImpl.getStatus(), is(TaskStatus.IN_ERROR));

        assertThat(taskInfoImpl2.getNumberOfExecutionLeft(), is(10));

        assertThat(taskInfoImpl2.getStatus(), is(TaskStatus.PAUSED));

        assertThat(job.getStatus(), is(JobStatus.PAUSED));

    }

    @Test
    public void testTaskTerminatedWithResultCancelJobOnError() throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        TaskId taskId = TaskIdImpl.createTaskId(id, "task-name", 0L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        TaskInfoImpl taskInfoImpl = (TaskInfoImpl) internalTask.getTaskInfo();
        taskInfoImpl.setNumberOfExecutionLeft(1);

        internalTask.setOnTaskError(OnTaskError.CANCEL_JOB);

        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule();
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        TaskResultImpl result = new TaskResultImpl(taskId, new Exception());
        liveJobs.taskTerminatedWithResult(taskId, result);

        assertThat(taskInfoImpl.getNumberOfExecutionLeft(), is(0));

        assertThat(taskInfoImpl.getStatus(), is(TaskStatus.FAULTY));

        assertThat(job.getStatus(), is(JobStatus.CANCELED));

    }

    @Test
    public void testTaskTerminatedWithResultContinueJobOnError() throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask();
        TaskId taskId = TaskIdImpl.createTaskId(id, "task-name", 0L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        TaskInfoImpl taskInfoImpl = (TaskInfoImpl) internalTask.getTaskInfo();
        taskInfoImpl.setNumberOfExecutionLeft(2);

        internalTask.setOnTaskError(OnTaskError.CONTINUE_JOB_EXECUTION);

        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule();
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        TaskResultImpl result = new TaskResultImpl(taskId, new Exception());
        liveJobs.taskTerminatedWithResult(taskId, result);

        assertThat(taskInfoImpl.getNumberOfExecutionLeft(), is(1));

        assertThat(taskInfoImpl.getStatus(), is(TaskStatus.WAITING_ON_ERROR));

        assertThat(job.getStatus(), is(JobStatus.STALLED));

    }

}
