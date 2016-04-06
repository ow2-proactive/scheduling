package org.ow2.proactive.scheduler.core;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxy;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxyCreationException;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.utils.NodeSet;

public class TerminationDataTest {
	
	private TerminationData terminationData;
	
	@Mock
	private SchedulingService service;
	
	@Mock
	private TaskLauncher launcher;
	
	@Mock
	private SchedulingInfrastructure schedulingInfrastructure; 
	
	@Mock
	private RMProxiesManager proxiesManager;
	
	@Mock
	private RMProxy rmProxy;
	
	@Before
	public void init() throws RMProxyCreationException{
		MockitoAnnotations.initMocks(this);
		Mockito.when(service.getInfrastructure()).thenReturn(schedulingInfrastructure);
		Mockito.when(schedulingInfrastructure.getRMProxiesManager()).thenReturn(proxiesManager);
		Mockito.when(proxiesManager.getUserRMProxy("user", null)).thenReturn(rmProxy);

		terminationData = TerminationData.newTerminationData();
	}
	
	@Test
	public void testAddJobToTerminate(){
		assertThat(terminationData.isEmpty(), is(true));
		JobId jobId = new JobIdImpl(666, "readableName");
		terminationData.addJobToTerminate(jobId);
		assertThat(terminationData.isEmpty(), is(false));
		assertThat(terminationData.jobTerminated(jobId), is(true));
	}
	
	@Test
	public void testAddTaskData(){
		assertThat(terminationData.isEmpty(), is(true));
		JobId jobId = new JobIdImpl(666, "readableName");
		InternalTask internalTask = new InternalScriptTask();
		TaskId taskId = TaskIdImpl.createTaskId(jobId, "task-name", 777L);
		internalTask.setId(taskId);
		internalTask.setName("task-name");
		internalTask.setStatus(TaskStatus.RUNNING);
		internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
		RunningTaskData taskData = new RunningTaskData(internalTask, "user", null, null);
		terminationData.addTaskData(taskData, true);
		assertThat(terminationData.isEmpty(), is(false));
		assertThat(terminationData.taskTerminated(jobId,"task-name"), is(true));
	}
	
	@Test
	public void testAddRestartData(){
		assertThat(terminationData.isEmpty(), is(true));
		JobId jobId = new JobIdImpl(666, "readableName");
		TaskId taskId = TaskIdImpl.createTaskId(jobId, "task-name", 777L);
		terminationData.addRestartData(taskId, 1000L);
		assertThat(terminationData.isEmpty(), is(false));
	}
	

	@Test
	public void testHandleTerminationForJob(){
		JobId jobId = new JobIdImpl(666, "readableName");
		terminationData.addJobToTerminate(jobId);
		terminationData.handleTermination(service);
		Mockito.verify(service, Mockito.times(1)).terminateJobHandling(jobId);
	}
	
	@Test
	public void testHandleTerminationForTaskNotNormalTermination(){
		JobId jobId = new JobIdImpl(666, "readableName");
		InternalTask internalTask = new InternalScriptTask();
		TaskId taskId = TaskIdImpl.createTaskId(jobId, "task-name", 777L);
		internalTask.setId(taskId);
		internalTask.setName("task-name");
		internalTask.setStatus(TaskStatus.RUNNING);
		internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
		RunningTaskData taskData = new RunningTaskData(internalTask, "user", null, launcher);
		terminationData.addTaskData(taskData, false);
		terminationData.handleTermination(service);
		Mockito.verify(launcher, Mockito.times(1)).kill();
	}

	
	@Test
	public void testHandleTerminationForTaskNormalTermination() throws RMProxyCreationException{
		JobId jobId = new JobIdImpl(666, "readableName");
		InternalTask internalTask = new InternalScriptTask();
		TaskId taskId = TaskIdImpl.createTaskId(jobId, "task-name", 777L);
		internalTask.setId(taskId);
		internalTask.setName("task-name");
		internalTask.setStatus(TaskStatus.RUNNING);
		internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
		RunningTaskData taskData = new RunningTaskData(internalTask, "user", null, launcher);
		terminationData.addTaskData(taskData, true);
		terminationData.handleTermination(service);
		Mockito.verify(proxiesManager, Mockito.times(1)).getUserRMProxy("user", null);
		Mockito.verify(rmProxy, Mockito.times(1)).releaseNodes(org.mockito.Matchers.any(NodeSet.class),org.mockito.Matchers.any(org.ow2.proactive.scripting.Script.class) );
		
	}

	
	@Test
	public void testHandleTerminationForTaskToRestart() throws RMProxyCreationException{
		JobId jobId = new JobIdImpl(666, "readableName");
		TaskId taskId = TaskIdImpl.createTaskId(jobId, "task-name", 777L);
		terminationData.addRestartData(taskId, 1000L);
		terminationData.handleTermination(service);
		Mockito.verify(schedulingInfrastructure, Mockito.times(1)).schedule(org.mockito.Matchers.any(Runnable.class), org.mockito.Matchers.anyLong());
		
	}

}
