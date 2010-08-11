package org.ow2.proactive_grid_cloud_portal;

import org.ow2.proactive.scheduler.common.job.JobInfo;

public class UserJobInfo {
	private String jobId;
	private String jobOwner;
	
	private JobInfo jobInfo;
	
	public UserJobInfo(){}
	
	public UserJobInfo(String jobid, String jobOwner, JobInfo jobinfo){
		this.jobId = jobid;
		this.jobOwner = jobOwner;
		this.jobInfo = jobinfo;
	}
	
	public String getJobid() {
		return jobId;
	}
	public void setJobid(String jobid) {
		this.jobId = jobid;
	}
	
	public String getJobOwner() {
		return jobOwner;
	}

	public void setJobOwner(String jobOwner) {
		this.jobOwner = jobOwner;
	}
	
	public JobInfo getJobinfo() {
		return jobInfo;
	}
	public void setJobinfo(JobInfo jobinfo) {
		this.jobInfo = jobinfo;
	}
	

}
