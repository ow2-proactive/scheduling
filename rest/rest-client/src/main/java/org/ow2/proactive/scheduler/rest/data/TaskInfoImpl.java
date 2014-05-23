/*
 *  
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest.data;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;


public class TaskInfoImpl implements TaskInfo {

    private static final String[] EMPTY_STRING_ARRAY = new String[] {};

    private long executionDuration = -1;

    private String executionHostName;

    private long finishedTime = -1;

    private JobId jobId;

    private JobInfo jobInfo;

    private String name;

    private int numberOfExecutionLeft = 1;

    private int numberOfExecutionOnFailureLeft = 1;

    private int progress;

    private long startTime = -1;

    private TaskStatus status = TaskStatus.SUBMITTED;

    private TaskId taskId;

    public void setExecutionDuration(long executionDuration) {
        this.executionDuration = executionDuration;
    }

    @Override
    public long getExecutionDuration() {
        return executionDuration;
    }

    public void setExecutionHostName(String executionHostName) {
        this.executionHostName = executionHostName;
    }

    @Override
    public String getExecutionHostName() {
        return executionHostName;
    }

    @Override
    public String[] getExecutionHostNameList() {
        return (executionHostName == null || executionHostName.isEmpty()) ? EMPTY_STRING_ARRAY
                : executionHostName.split(",");
    }

    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    @Override
    public long getFinishedTime() {
        return finishedTime;
    }

    public void setJobId(JobId jobId) {
        this.jobId = jobId;
    }

    @Override
    public JobId getJobId() {
        return this.jobId;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    @Override
    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setNumberOfExecutionLeft(int numberOfExecutionLeft) {
        this.numberOfExecutionLeft = numberOfExecutionLeft;
    }

    @Override
    public int getNumberOfExecutionLeft() {
        return numberOfExecutionLeft;
    }

    public void setNumberOfExecutionOnFailureLeft(int numberOfExecutionOnFailureLeft) {
        this.numberOfExecutionOnFailureLeft = numberOfExecutionOnFailureLeft;
    }

    @Override
    public int getNumberOfExecutionOnFailureLeft() {
        return numberOfExecutionOnFailureLeft;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    public void setTaskId(TaskId taskId) {
        this.taskId = taskId;
    }

    @Override
    public TaskId getTaskId() {
        return taskId;
    }

}
