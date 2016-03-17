package org.ow2.proactive.scheduler.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

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
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

public class LiveJobsTest {
	
	private LiveJobs liveJobs;
	@Mock
	private SchedulerDBManager dbManager;
	@Mock
	private SchedulerStateUpdate listener;
	
	@Before
	public void init(){
		MockitoAnnotations.initMocks(this);
		liveJobs = new LiveJobs(dbManager, listener);
	}
	
	
	@Test
	public void testGetRunningTasksEmpty(){
		assertThat(liveJobs.getRunningTasks().isEmpty(), is(true));
	}
	
	@Test
	public void testJobSubmitted(){
		InternalJob job =  new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB, "description");
		JobId id = new JobIdImpl(666L, "test-name");
		job.setId(id);
		liveJobs.jobSubmitted(job);
		Mockito.verify(listener, Mockito.times(1)).jobSubmitted(Matchers.any(ClientJobState.class));
	}
	
	@Test
	public void testPauseJob(){
		InternalJob job =  new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB, "description");
		JobId id = new JobIdImpl(666L, "test-name");
		job.setId(id);
		List<InternalTask> tasksList =  new ArrayList<>();
		InternalTask internalTask = new InternalScriptTask();
		tasksList.add(internalTask);
		job.setTasks(tasksList);
		liveJobs.jobSubmitted(job);
		assertThat(liveJobs.pauseJob(id), is(true));
	}
	
	@Test
	public void testResumeUnstartedJob(){
		InternalJob job =  new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB, "description");
		JobId id = new JobIdImpl(666L, "test-name");
		job.setId(id);
		List<InternalTask> tasksList =  new ArrayList<>();
		InternalTask internalTask = new InternalScriptTask();
		tasksList.add(internalTask);
		job.setTasks(tasksList);
		liveJobs.jobSubmitted(job);
		assertThat(liveJobs.resumeJob(id), is(false));
	}

	@Test
	public void testResumeStartedJob() throws UnknownJobException, UnknownTaskException{
		InternalJob job =  new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB, "description");
		JobId id = new JobIdImpl(666L, "test-name");
		job.setId(id);
		List<InternalTask> tasksList =  new ArrayList<>();
		InternalTask internalTask = new InternalScriptTask();
		internalTask.setName("task-name");
		tasksList.add(internalTask);
		job.setTasks(tasksList);
		liveJobs.jobSubmitted(job);
		liveJobs.pauseJob(id);
		assertThat(liveJobs.resumeJob(id), is(true));
	}
	
	
	@Test
	public void testLockJobsToSchedule() throws UnknownJobException, UnknownTaskException{
		InternalJob job =  new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB, "description");
		JobId id = new JobIdImpl(666L, "test-name");
		job.setId(id);
		List<InternalTask> tasksList =  new ArrayList<>();
		InternalTask internalTask = new InternalScriptTask();
		internalTask.setName("task-name");
		tasksList.add(internalTask);
		job.setTasks(tasksList);
		liveJobs.jobSubmitted(job);
		liveJobs.pauseJob(id);
		assertThat(liveJobs.lockJobsToSchedule().size(), is(1));
	}
	
	
	@Test(expected=IllegalStateException.class)
	public void testRestartTaskOnNodeFailureRunningExceptionExpectedBecauseNotLockedTask() throws UnknownJobException, UnknownTaskException{
		InternalJob job =  new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB, "description");
		JobId id = new JobIdImpl(666L, "test-name");
		job.setId(id);
		List<InternalTask> tasksList =  new ArrayList<>();
		InternalTask internalTask = new InternalScriptTask();
		internalTask.setName("task-name");
		internalTask.setStatus(TaskStatus.RUNNING);
		tasksList.add(internalTask);
		job.setTasks(tasksList);
		liveJobs.jobSubmitted(job);
		liveJobs.restartTaskOnNodeFailure(internalTask);
	}
	
	
	@Test
	public void testRestartTaskOnNodeRunningDecreasingExecution() throws UnknownJobException, UnknownTaskException{
		
		PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.updateProperty("5");
		InternalJob job =  new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB, "description");
		JobId id = new JobIdImpl(666L, "test-name");
		job.setId(id);
		List<InternalTask> tasksList =  new ArrayList<>();
		InternalScriptTask internalTask = new InternalScriptTask();
		internalTask.setName("task-name");
		internalTask.setStatus(TaskStatus.RUNNING);
		internalTask.setMaxNumberOfExecution(5);
		internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
		tasksList.add(internalTask);
		job.setTasks(tasksList);
		liveJobs.jobSubmitted(job);
		liveJobs.lockJobsToSchedule();
		liveJobs.taskStarted(job,job.getTask("task-name"), null);
		
		assertThat(internalTask.getMaxNumberOfExecutionOnFailure(), is(5));
		assertThat(internalTask.getTaskInfo().getNumberOfExecutionOnFailureLeft(), is(5));
		
		
		liveJobs.restartTaskOnNodeFailure(internalTask);	
		internalTask.setStatus(TaskStatus.RUNNING);

		assertThat(internalTask.getMaxNumberOfExecutionOnFailure(), is(5));
		assertThat(internalTask.getTaskInfo().getNumberOfExecutionOnFailureLeft(), is(4));
		
		Mockito.verify(dbManager, Mockito.times(1)).taskRestarted(job, internalTask, null);
	}
	
	@Test
	public void testRestartTaskOnNodeRunning0ExecutionsLeft() throws UnknownJobException, UnknownTaskException{
		
		PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.updateProperty("0");
		InternalJob job =  new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB, "description");
		JobId id = new JobIdImpl(666L, "test-name");
		job.setId(id);
		List<InternalTask> tasksList =  new ArrayList<>();
		InternalScriptTask internalTask = new InternalScriptTask();
		internalTask.setName("task-name");
		internalTask.setStatus(TaskStatus.RUNNING);
		internalTask.setMaxNumberOfExecution(5);
		internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
		tasksList.add(internalTask);
		job.setTasks(tasksList);
		liveJobs.jobSubmitted(job);
		liveJobs.lockJobsToSchedule();
		liveJobs.taskStarted(job,job.getTask("task-name"), null);
		
		assertThat(internalTask.getMaxNumberOfExecutionOnFailure(), is(0));
		assertThat(internalTask.getTaskInfo().getNumberOfExecutionOnFailureLeft(), is(0));
		
		
		liveJobs.restartTaskOnNodeFailure(internalTask);	
		internalTask.setStatus(TaskStatus.RUNNING);
		
		Mockito.verify(dbManager, Mockito.times(0)).taskRestarted(job, internalTask, null);
	}
	
	
	@Test
	public void testCanPingTask() throws UnknownJobException, UnknownTaskException{
		InternalJob job =  new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB, "description");
		JobId id = new JobIdImpl(666L, "test-name");
		job.setId(id);
		List<InternalTask> tasksList =  new ArrayList<>();
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
		liveJobs.taskStarted(job,job.getTask("task-name"), null);
		assertThat(liveJobs.canPingTask(liveJobs.getRunningTasks().iterator().next()), is(true));
	}

	

}
