package org.ow2.proactive_grid_cloud_portal.cli.json;

import java.util.Map;

public class JobStateView implements Comparable<JobStateView>{

	private String name;
	private String priority;
	private String owner;
	private JobInfoView jobInfo;
	private String projectName;
	private Map<String, TaskStateView> tasks;

	
	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public JobInfoView getJobInfo() {
		return jobInfo;
	}

	public void setJobInfo(JobInfoView jobInfo) {
		this.jobInfo = jobInfo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return jobInfo.getJobId().getId();
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public Map<String, TaskStateView> getTasks() {
		return tasks;
	}

	public void setTasks(Map<String, TaskStateView> tasks) {
		this.tasks = tasks;
	}

	@Override
	public int compareTo(JobStateView o) {
		return compare(getId(), o.getId());
	}
	
	private int compare(int x, int y) {
		return (x < y) ?  -1 : ((x == y) ? 0 : 1);
	}
}
