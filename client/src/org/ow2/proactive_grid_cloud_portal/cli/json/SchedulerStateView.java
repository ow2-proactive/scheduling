package org.ow2.proactive_grid_cloud_portal.cli.json;

public class SchedulerStateView {

	private JobStateView[] finishedJobs;
	private JobStateView[] pendingJobs;
	private JobStateView[] runningJobs;

	public JobStateView[] getFinishedJobs() {
		return finishedJobs;
	}

	public void setFinishedJobs(JobStateView[] finishedJobs) {
		this.finishedJobs = finishedJobs;
	}

	public JobStateView[] getPendingJobs() {
		return pendingJobs;
	}

	public void setPendingJobs(JobStateView[] pendingJobs) {
		this.pendingJobs = pendingJobs;
	}

	public JobStateView[] getRunningJobs() {
		return runningJobs;
	}

	public void setRunningJobs(JobStateView[] runningJobs) {
		this.runningJobs = runningJobs;
	}
}
