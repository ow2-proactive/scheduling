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

import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
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

    /** all job events */
    private Map<JobId, JobEvent> jobEvents = null;

    /** all task events */
    private Map<TaskId, TaskEvent> taskEvents = null;

    /**
     * The default constructor
     *
     * @param jobs all jobs
     * @param jobsEvents all job events
     * @param tasksEvents all task events
     * @param jobsResults all job result
     */
    public RecoverableState(List<InternalJob> jobs, List<JobResult> jobsResults,
            Map<JobId, JobEvent> jobsEvents, Map<TaskId, TaskEvent> tasksEvents) {
        this.jobs = jobs;
        this.jobResults = jobsResults;
        this.jobEvents = jobsEvents;
        this.taskEvents = tasksEvents;
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
    public Map<TaskId, TaskEvent> getTaskEvents() {
        return taskEvents;
    }

    /**
     * To set the taskEvents
     *
     * @param taskEvents the taskEvents to set
     */
    public void setTaskEvents(Map<TaskId, TaskEvent> taskEvents) {
        this.taskEvents = taskEvents;
    }
}
