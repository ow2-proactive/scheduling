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
package org.ow2.proactive.scheduler.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.VMInformation;
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
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.tests.ProActiveTestClean;
import org.python.google.common.collect.ImmutableSet;


public class LiveJobsTest extends ProActiveTestClean {

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

    @Test(timeout = 60000)
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

    private TreeSet<JobPriority> createTreeSet(JobPriority... priorities) {
        return new TreeSet<>(ImmutableSet.copyOf(priorities));
    }

    @Test(timeout = 60000)
    public void testGetRunningTasksEmpty() {
        assertThat(liveJobs.getRunningTasks().isEmpty(), is(true));
    }

    @Test(timeout = 600000)
    public void testGetRunningTaskByTaskId() throws UnknownTaskException {
        PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.updateProperty("5");

        JobId id = new JobIdImpl(666L, "test-name");
        TaskId tid = TaskIdImpl.createTaskId(id, "task-name", 0L);
        submitJobAndStartTask(id, tid);

        JobId id2 = new JobIdImpl(667L, "test-name");
        TaskId tid2 = TaskIdImpl.createTaskId(id2, "task-name2", 0L);
        submitJobAndStartTask(id2, tid2);

        JobId id3 = new JobIdImpl(668L, "test-name");
        TaskId tid3 = TaskIdImpl.createTaskId(id3, "task-name3", 0L);
        submitJobAndStartTask(id3, tid3);

        JobId id4 = new JobIdImpl(669L, "test-name");
        TaskId tid4 = TaskIdImpl.createTaskId(id4, "task-name4", 0L);
        submitJobAndStartTask(id4, tid4);

        JobId id5 = new JobIdImpl(670L, "test-name");
        TaskId tid5 = TaskIdImpl.createTaskId(id5, "task-name5", 0L);
        submitJobAndStartTask(id5, tid5);

        assertThat(liveJobs.getRunningTasks().size(), is(5));

        assertThat(liveJobs.getRunningTask(tid).getTask().getName(), is("task-name"));
        assertThat(liveJobs.getRunningTask(tid).getTask().getJobId(), is(id));

    }

    private void submitJobAndStartTask(JobId id, TaskId tid) throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");

        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalScriptTask internalTask = new InternalScriptTask(job);
        internalTask.setName(tid.getReadableName());
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setMaxNumberOfExecution(5);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule(false);
        liveJobs.taskStarted(job, job.getTask(tid.getReadableName()), null);
    }

    @Test(timeout = 60000)
    public void testJobSubmitted() {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        liveJobs.jobSubmitted(job);
        Mockito.verify(listener, Mockito.times(1)).jobSubmitted(Matchers.any(ClientJobState.class));
    }

    @Test(timeout = 60000)
    public void testFinishInErrorTask() throws UnknownTaskException, UnknownJobException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CONTINUE_JOB_EXECUTION,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);

        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.IN_ERROR);

        Node node = Mockito.mock(Node.class);
        Mockito.when(node.getVMInformation()).thenAnswer(new Answer<VMInformation>() {
            @Override
            public VMInformation answer(InvocationOnMock invocation) throws Throwable {
                return Mockito.mock(VMInformation.class);
            }
        });

        Mockito.when(node.getNodeInformation()).thenAnswer(new Answer<NodeInformation>() {
            @Override
            public NodeInformation answer(InvocationOnMock invocation) throws Throwable {
                return Mockito.mock(NodeInformation.class);
            }
        });

        TaskLauncher taskLauncher = Mockito.mock(TaskLauncher.class);
        internalTask.setExecuterInformation(new ExecuterInformation(taskLauncher, node));
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.finishInErrorTask(job.getId(), "task-name");
        assertThat(internalTask.getStatus(), is(TaskStatus.FINISHED));
    }

    @Test(timeout = 60000)
    public void testFinishInErrorTaskDoesNotFinishPendingTask() throws UnknownTaskException, UnknownJobException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CONTINUE_JOB_EXECUTION,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.PENDING);
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.finishInErrorTask(job.getId(), "task-name");
        assertThat(internalTask.getStatus(), is(TaskStatus.PENDING));
    }

    @Test(timeout = 60000)
    public void testPauseJob() {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        assertThat(liveJobs.pauseJob(id), is(true));
    }

    @Test(timeout = 60000)
    public void testUpdateStartAt() {
        String startAt = "2017-07-07T00:00:00+01:00";
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        assertThat(liveJobs.updateStartAt(id, startAt), is(true));
    }

    @Test(timeout = 60000)
    public void testResumeUnstartedJob() {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        assertThat(liveJobs.resumeJob(id), is(false));
    }

    @Test(timeout = 60000)
    public void testResumeStartedJob() throws UnknownJobException, UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
        internalTask.setName("task-name");
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.pauseJob(id);
        assertThat(liveJobs.resumeJob(id), is(true));
    }

    @Test(timeout = 60000)
    public void testLockJobsToSchedule() throws UnknownJobException, UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
        internalTask.setName("task-name");
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.pauseJob(id);
        assertThat(liveJobs.lockJobsToSchedule(false).size(), is(1));
    }

    @Test(expected = IllegalStateException.class, timeout = 60000)
    public void testRestartTaskOnNodeFailureRunningExceptionExpectedBecauseNotLockedTask()
            throws UnknownJobException, UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.restartTaskOnNodeFailure(internalTask);
    }

    @Test(timeout = 60000)
    public void testRestartTaskOnNodeRunningDecreasingExecution() throws UnknownJobException, UnknownTaskException {

        PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.updateProperty("5");
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalScriptTask internalTask = new InternalScriptTask(job);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setMaxNumberOfExecution(5);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule(false);
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        assertThat(internalTask.getMaxNumberOfExecutionOnFailure(), is(5));
        assertThat(internalTask.getTaskInfo().getNumberOfExecutionOnFailureLeft(), is(5));

        liveJobs.restartTaskOnNodeFailure(internalTask);
        internalTask.setStatus(TaskStatus.RUNNING);

        assertThat(internalTask.getMaxNumberOfExecutionOnFailure(), is(5));
        assertThat(internalTask.getTaskInfo().getNumberOfExecutionOnFailureLeft(), is(4));

        Mockito.verify(dbManager, Mockito.times(1)).taskRestarted(job, internalTask, null);
    }

    @Test(timeout = 60000)
    public void testRestartTaskOnNodeRunning0ExecutionsLeft() throws UnknownJobException, UnknownTaskException {

        PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.updateProperty("0");
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalScriptTask internalTask = new InternalScriptTask(job);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setMaxNumberOfExecution(5);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule(false);
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        assertThat(internalTask.getMaxNumberOfExecutionOnFailure(), is(0));
        assertThat(internalTask.getTaskInfo().getNumberOfExecutionOnFailureLeft(), is(0));

        liveJobs.restartTaskOnNodeFailure(internalTask);
        internalTask.setStatus(TaskStatus.RUNNING);

        Mockito.verify(dbManager, Mockito.times(0)).taskRestarted(job, internalTask, null);
    }

    @Test(timeout = 60000)
    public void testCanPingTask() throws UnknownJobException, UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
        TaskId taskId = TaskIdImpl.createTaskId(id, "task-name", 777L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        tasksList.add(internalTask);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule(false);
        liveJobs.taskStarted(job, job.getTask("task-name"), null);
        assertThat(liveJobs.canPingTask(liveJobs.getRunningTasks().iterator().next()), is(true));
    }

    @Test(timeout = 60000)
    public void testTaskTerminatedWithResultSuspendTaskOnError() throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
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
        liveJobs.lockJobsToSchedule(false);
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        TaskResultImpl result = new TaskResultImpl(taskId, new Exception(), null, 330);

        liveJobs.taskTerminatedWithResult(taskId, result);

        assertThat(taskInfoImpl.getNumberOfExecutionLeft(), is(1));

        assertThat(taskInfoImpl.getStatus(), is(TaskStatus.WAITING_ON_ERROR));

        assertThat(job.getStatus(), is(JobStatus.STALLED));

    }

    @Test(timeout = 60000)
    public void testTaskTerminatedWithResultSuspendTaskOnErrorLastExecution() throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
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
        liveJobs.lockJobsToSchedule(false);
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        TaskResultImpl result = new TaskResultImpl(taskId, new Exception(), null, 330);

        liveJobs.taskTerminatedWithResult(taskId, result);

        assertThat(taskInfoImpl.getNumberOfExecutionLeft(), is(-1));

        assertThat(taskInfoImpl.getStatus(), is(TaskStatus.IN_ERROR));

        assertThat(job.getStatus(), is(JobStatus.IN_ERROR));

    }

    @Test(timeout = 60000)
    public void testTaskTerminatedWithResultPauseJobOnError() throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
        TaskId taskId = TaskIdImpl.createTaskId(id, "task-name", 0L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        TaskInfoImpl taskInfoImpl = (TaskInfoImpl) internalTask.getTaskInfo();
        taskInfoImpl.setNumberOfExecutionLeft(10);

        internalTask.setOnTaskError(OnTaskError.PAUSE_JOB);

        InternalTask internalTask2 = new InternalScriptTask(job);
        TaskId taskId2 = TaskIdImpl.createTaskId(id, "task-name2", 1L);
        internalTask2.setId(taskId2);
        internalTask2.setName("task-name2");
        internalTask2.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        TaskInfoImpl taskInfoImpl2 = (TaskInfoImpl) internalTask2.getTaskInfo();
        taskInfoImpl2.setNumberOfExecutionLeft(10);

        internalTask2.setOnTaskError(OnTaskError.NONE);

        tasksList.add(internalTask);
        tasksList.add(internalTask2);
        job.setTasks(tasksList);
        liveJobs.jobSubmitted(job);
        liveJobs.lockJobsToSchedule(false);
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        TaskResultImpl result = new TaskResultImpl(taskId, new Exception());

        liveJobs.taskTerminatedWithResult(taskId, result);

        assertThat(taskInfoImpl.getNumberOfExecutionLeft(), is(9));

        assertThat(taskInfoImpl.getStatus(), is(TaskStatus.WAITING_ON_ERROR));

        assertThat(taskInfoImpl2.getNumberOfExecutionLeft(), is(10));

        assertThat(taskInfoImpl2.getStatus(), is(TaskStatus.SUBMITTED));

        assertThat(job.getStatus(), is(JobStatus.STALLED));

    }

    @Test(timeout = 60000)
    public void testTaskTerminatedWithResultCancelJobOnError() throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
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
        liveJobs.lockJobsToSchedule(false);
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        TaskResultImpl result = new TaskResultImpl(taskId, new Exception());
        liveJobs.taskTerminatedWithResult(taskId, result);

        assertThat(taskInfoImpl.getNumberOfExecutionLeft(), is(0));

        assertThat(taskInfoImpl.getStatus(), is(TaskStatus.FAULTY));

        assertThat(job.getStatus(), is(JobStatus.CANCELED));

    }

    @Test(timeout = 60000)
    public void testTaskTerminatedWithResultContinueJobOnError() throws UnknownTaskException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId id = new JobIdImpl(666L, "test-name");
        job.setId(id);
        List<InternalTask> tasksList = new ArrayList<>();
        InternalTask internalTask = new InternalScriptTask(job);
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
        liveJobs.lockJobsToSchedule(false);
        liveJobs.taskStarted(job, job.getTask("task-name"), null);

        TaskResultImpl result = new TaskResultImpl(taskId, new Exception());
        liveJobs.taskTerminatedWithResult(taskId, result);

        assertThat(taskInfoImpl.getNumberOfExecutionLeft(), is(1));

        assertThat(taskInfoImpl.getStatus(), is(TaskStatus.WAITING_ON_ERROR));

        assertThat(job.getStatus(), is(JobStatus.STALLED));

    }

}
