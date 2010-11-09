package org.ow2.proactive_grid_cloud_portal;

import javax.xml.bind.annotation.XmlRootElement;

import org.ow2.proactive.scheduler.common.job.JobInfo;

/**
 * A class that contains a subset of the information available
 * in a scheduler state.
 * It is mostly used to provide a fast access to meaningful
 * data within the scheduler state without having to manage the 
 * complete state
 *
 */
@XmlRootElement
public class UserJobInfo {
    /*
     * the id of the job
     */
    private String jobId;
    
    /*
     * the job's owner
     */
    private String jobOwner;

    /*
     * Jobinfo of the job
     */
    private JobInfo jobInfo;

    public UserJobInfo() {
    }

    public UserJobInfo(String jobid, String jobOwner, JobInfo jobinfo) {
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
