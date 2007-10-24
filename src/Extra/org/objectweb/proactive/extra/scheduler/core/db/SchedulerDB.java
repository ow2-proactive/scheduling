package org.objectweb.proactive.extra.scheduler.core.db;

import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;

/**
 * @author FRADJ Johann
 */
public interface SchedulerDB {

	public void addJob();
	
	public void setJobStatus();
	
	public void setTaskStatus();
	
	public void addTaskResult();
	
	public GlobalState getGlobalState();
	
	public JobResult getJobResult();
	
	public TaskResult getTaskResult();
}
