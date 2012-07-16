package org.ow2.proactive_grid_cloud_portal.cli.json;

public class TaskInfoView {
	private TaskIdView taskId;
	private long startTime;
	private long finishedTime;
	private long executionDuration;
	private String taskStatus;
	private String executionHostName;
	private int numberOfExecutionLeft;
	private int numberOfExecutionOnFailureLeft;

	public TaskIdView getTaskId() {
		return taskId;
	}

	public void setTaskId(TaskIdView taskId) {
		this.taskId = taskId;
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

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public String getExecutionHostName() {
		return executionHostName;
	}

	public void setExecutionHostName(String executionHostName) {
		this.executionHostName = executionHostName;
	}

	public int getNumberOfExecutionLeft() {
		return numberOfExecutionLeft;
	}

	public void setNumberOfExecutionLeft(int numberOfExecutionLeft) {
		this.numberOfExecutionLeft = numberOfExecutionLeft;
	}

	public int getNumberOfExecutionOnFailureLeft() {
		return numberOfExecutionOnFailureLeft;
	}

	public void setNumberOfExecutionOnFailureLeft(
			int numberOfExecutionOnFailureLeft) {
		this.numberOfExecutionOnFailureLeft = numberOfExecutionOnFailureLeft;
	}

	public long getExecutionDuration() {
		return executionDuration;
	}

	public void setExecutionDuration(long executionDuration) {
		this.executionDuration = executionDuration;
	}
}
