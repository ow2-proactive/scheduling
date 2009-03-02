/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.db;

import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * This class represents the scheduler state at the last time which is was registered.
 * Since this class you can completely rebuild the scheduler after crashes.
 *
 * @author The ProActive Team
 */
public class RecoverableState {

    /** all jobs snapshot at their submission */
    private List<InternalJob> jobs = null;

    /** all job result */
    private List<JobResult> jobResults = null;

    /** all job info */
    private Map<JobId, JobInfo> jobInfos = null;

    /** all task info */
    private Map<TaskId, TaskInfo> taskInfos = null;

    /**
     * The default constructor
     *
     * @param jobs all jobs
     * @param jobsInfos all job info
     * @param tasksInfos all task info
     * @param jobsResults all job result
     */
    public RecoverableState(List<InternalJob> jobs, List<JobResult> jobsResults,
            Map<JobId, JobInfo> jobsInfos, Map<TaskId, TaskInfo> tasksInfos) {
        this.jobs = jobs;
        this.jobResults = jobsResults;
        this.jobInfos = jobsInfos;
        this.taskInfos = tasksInfos;
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

    /**
     * To get the jobInfos
     *
     * @return the jobInfos
     */
    public Map<JobId, JobInfo> getJobInfos() {
        return jobInfos;
    }

    /**
     * To set the jobInfos
     *
     * @param jobInfos the jobInfos to set
     */
    public void setJobInfos(Map<JobId, JobInfo> jobInfos) {
        this.jobInfos = jobInfos;
    }

    /**
     * To get the taskInfos
     *
     * @return the taskInfos
     */
    public Map<TaskId, TaskInfo> getTaskInfos() {
        return taskInfos;
    }

    /**
     * To set the taskInfos
     *
     * @param taskInfos the taskInfos to set
     */
    public void setTaskInfos(Map<TaskId, TaskInfo> taskInfos) {
        this.taskInfos = taskInfos;
    }
}
