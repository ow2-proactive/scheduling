package org.ow2.proactive.scheduler.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification.TerminateTaskException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;

public class TerminateNotificationTest {
	
	private TerminateNotification terminateNotification;
	
	@Mock
	private SchedulingService schedulingService;
	
	@Before
	public void init(){
		MockitoAnnotations.initMocks(this);
		terminateNotification = new TerminateNotification(schedulingService);		
	}
	
	@Test
	public void testTerminate() throws TerminateTaskException{
		TaskId taskId = TaskIdImpl.createTaskId(new JobIdImpl(666, "readableName"), "task-name", 777L);
		TaskResult taskResult = new TaskResultImpl(taskId, new Throwable());
		terminateNotification.terminate(taskId, taskResult);
		Mockito.verify(schedulingService, Mockito.times(1)).taskTerminatedWithResult(taskId, taskResult);
	}
	

}
