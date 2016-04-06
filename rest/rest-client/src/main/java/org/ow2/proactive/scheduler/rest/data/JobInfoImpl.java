/*
 *  
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;


public class JobInfoImpl implements JobInfo {

    private long submittedTime = -1;
    private long startTime = -1;
    private long inErrorTime = -1;
    private long finishedTime = -1;
    private long removedTime = -1;

    private JobId jobId;
    private String jobOwner;

    private int totalNumberOfTasks = 0;
    private int numberOfPendingTasks = 0;
    private int numberOfRunningTasks = 0;
    private int numberOfFinishedTasks = 0;

    private int numberOfFailedTasks = 0;
    private int numberOfFaultyTasks = 0;
    private int numberOfInErrorTasks = 0;

    private JobPriority jobPriority = JobPriority.NORMAL;
    private JobStatus jobStatus = JobStatus.PENDING;
    private boolean toBeRemoved;

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
        return jobId;
    }

    public void setJobOwner(String jobOwner) {
        this.jobOwner = jobOwner;
    }

    @Override
    public String getJobOwner() {
        return jobOwner;
    }

    public void setNumberOfFinishedTasks(int numberOfFinishedTasks) {
        this.numberOfFinishedTasks = numberOfFinishedTasks;
    }

    @Override
    public int getNumberOfFinishedTasks() {
        return numberOfFinishedTasks;
    }

    public void setNumberOfPendingTasks(int numberOfPendingTasks) {
        this.numberOfPendingTasks = numberOfPendingTasks;
    }

    @Override
    public int getNumberOfPendingTasks() {
        return numberOfPendingTasks;
    }

    public void setNumberOfRunningTasks(int numberOfRunningTasks) {
        this.numberOfRunningTasks = numberOfRunningTasks;
    }

    @Override
    public int getNumberOfRunningTasks() {
        return numberOfRunningTasks;
    }

    public void setJobPriority(JobPriority jobPriority) {
        this.jobPriority = jobPriority;
    }

    @Override
    public int getNumberOfFailedTasks() {
        return numberOfFailedTasks;
    }

    public void setNumberOfFailedTasks(int numberOfFailedTasks) {
        this.numberOfFailedTasks = numberOfFailedTasks;
    }

    @Override
    public int getNumberOfFaultyTasks() {
        return numberOfFaultyTasks;
    }

    public void setNumberOfFaultyTasks(int numberOfFaultyTasks) {
        this.numberOfFaultyTasks = numberOfFaultyTasks;
    }

    @Override
    public int getNumberOfInErrorTasks() {
        return numberOfInErrorTasks;
    }

    public void setNumberOfInErrorTasks(int numberOfInErrorTasks) {
        this.numberOfInErrorTasks = numberOfInErrorTasks;
    }

    @Override
    public JobPriority getPriority() {
        return jobPriority;
    }

    public void setRemovedTime(long removedTime) {
        this.removedTime = removedTime;
    }

    @Override
    public long getRemovedTime() {
        return removedTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getInErrorTime() {
        return inErrorTime;
    }

    public void setInErrorTime(long inErrorTime) {
        this.inErrorTime = inErrorTime;
    }

    @Override
    public JobStatus getStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    @Override
    public long getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(long submittedTime) {
        this.submittedTime = submittedTime;
    }

    @Override
    public int getTotalNumberOfTasks() {
        return totalNumberOfTasks;
    }

    public void setTotalNumberOfTasks(int totalNumberOfTasks) {
        this.totalNumberOfTasks = totalNumberOfTasks;
    }

    @Override
    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

    public void setToBeRemoved() {
        this.toBeRemoved = true;
    }

}
