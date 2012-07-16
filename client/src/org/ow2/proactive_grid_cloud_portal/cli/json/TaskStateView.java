package org.ow2.proactive_grid_cloud_portal.cli.json;

public class TaskStateView {

	private int iterationIndex;
	private int replicationIndex;
	private int maxNumberOfExecution;
	private int maxNumberOfExecutionOnFailure;
	private TaskInfoView taskInfo;
	private ParallelEnvironmentView parallelEnvironment;

	public int getIterationIndex() {
		return iterationIndex;
	}

	public void setIterationIndex(int iterationIndex) {
		this.iterationIndex = iterationIndex;
	}

	public int getReplicationIndex() {
		return replicationIndex;
	}

	public void setReplicationIndex(int replicationIndex) {
		this.replicationIndex = replicationIndex;
	}

	public int getMaxNumberOfExecution() {
		return maxNumberOfExecution;
	}

	public void setMaxNumberOfExecution(int maxNumberOfExecution) {
		this.maxNumberOfExecution = maxNumberOfExecution;
	}

	public int getMaxNumberOfExecutionOnFailure() {
		return maxNumberOfExecutionOnFailure;
	}

	public void setMaxNumberOfExecutionOnFailure(
			int maxNumberOfExecutionOnFailure) {
		this.maxNumberOfExecutionOnFailure = maxNumberOfExecutionOnFailure;
	}

	public TaskInfoView getTaskInfo() {
		return taskInfo;
	}

	public void setTaskInfo(TaskInfoView taskInfo) {
		this.taskInfo = taskInfo;
	}

	public ParallelEnvironmentView getParallelEnvironment() {
		return parallelEnvironment;
	}

	public void setParallelEnvironment(
			ParallelEnvironmentView parallelEnvironment) {
		this.parallelEnvironment = parallelEnvironment;
	}

	public int getNumberOfNodesNeeded() {
		return (parallelEnvironment != null) ? parallelEnvironment
				.getNodesNumber() : 1;
	}

}
