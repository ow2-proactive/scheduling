package org.ow2.proactive_grid_cloud_portal.cli.json;

public class JobInfoView {

	private long startTime;
	private long finishedTime;
	private String status;
	private JobIdView jobId;
	private int totalNumberOfTasks;

	public JobIdView getJobId() {
		return jobId;
	}

	public void setJobId(JobIdView jobId) {
		this.jobId = jobId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getFinishedTime() {
		return finishedTime;
	}

	public void setFinishedTime(long finishedTime) {
		this.finishedTime = finishedTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getTotalNumberOfTasks() {
		return totalNumberOfTasks;
	}

	public void setTotalNumberOfTasks(int totalNumberOfTasks) {
		this.totalNumberOfTasks = totalNumberOfTasks;
	}
}
