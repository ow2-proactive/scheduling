package org.objectweb.proactive.extra.scheduler.core.db;

import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;

/**
 * This class represents the scheduler at the last time which is was registered.
 * Since this class you can completely rebuild the scheduler after a crash.
 * 
 * @author FRADJ Johann
 */
public class GlobalState {

	/** all jobs snapshot at their submission */
	private List<InternalJob> jobs = null;
	/** all job events */
	private Map<JobId, JobEvent> jobEvents = null;
	/** all task events */
	private Map<JobId, TaskEvent> taskEvents = null;
	/** all job result */
	private List<JobResult> jobResults = null;

	/**
	 * The default constructor
	 * 
	 * @param jobs all jobs
	 * @param jobsEvents all job events
	 * @param tasksEvents all task events
	 * @param jobsResults all job result
	 */
	public GlobalState(List<InternalJob> jobs, Map<JobId, JobEvent> jobsEvents,
			Map<JobId, TaskEvent> tasksEvents, List<JobResult> jobsResults) {
		this.jobs = jobs;
		this.jobEvents = jobsEvents;
		this.taskEvents = tasksEvents;
		this.jobResults = jobsResults;
	}

	/**
	 * To get the jobs list
	 * 
	 * @return the jobs list
	 */
	public List<InternalJob> getJobs() {
		return jobs;
	}

	/**
	 * To set the jobs list
	 * 
	 * @param jobs the jobs list to set
	 */
	public void setJobs(List<InternalJob> jobs) {
		this.jobs = jobs;
	}

	/**
	 * To get the jobEvents
	 * 
	 * @return the jobEvents
	 */
	public Map<JobId, JobEvent> getJobEvents() {
		return jobEvents;
	}

	/**
	 * To set the jobEvents
	 * 
	 * @param jobEvents the jobEvents to set
	 */
	public void setJobEvents(Map<JobId, JobEvent> jobEvents) {
		this.jobEvents = jobEvents;
	}

	/**
	 * To get the taskEvents
	 * 
	 * @return the taskEvents
	 */
	public Map<JobId, TaskEvent> getTaskEvents() {
		return taskEvents;
	}

	/**
	 * To set the taskEvents
	 * 
	 * @param taskEvents the taskEvents to set
	 */
	public void setTaskEvents(Map<JobId, TaskEvent> taskEvents) {
		this.taskEvents = taskEvents;
	}

	/**
	 * To get the jobResults
	 * 
	 * @return the jobResults
	 */
	public List<JobResult> getJobResults() {
		return jobResults;
	}

	/**
	 * To set the jobResults
	 * 
	 * @param jobResults the jobResults to set
	 */
	public void setJobResults(List<JobResult> jobResults) {
		this.jobResults = jobResults;
	}
}
