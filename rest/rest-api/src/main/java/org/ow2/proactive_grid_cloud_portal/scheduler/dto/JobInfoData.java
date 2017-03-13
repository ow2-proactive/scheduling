/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import java.util.Map;


public class JobInfoData implements java.io.Serializable {

    private long startTime;

    private long inErrorTime;

    private long finishedTime;

    private long submittedTime;

    private long removedTime;

    private JobStatusData status;

    private JobIdData jobId;

    private int totalNumberOfTasks;

    private int numberOfPendingTasks;

    private int numberOfRunningTasks;

    private int numberOfFinishedTasks;

    private int numberOfFailedTasks;

    private int numberOfFaultyTasks;

    private int numberOfInErrorTasks;

    private JobPriorityData priority;

    private String jobOwner;

    private boolean toBeRemoved = false;

    private Map<String, String> genericInformation;

    public void setToBeRemoved() {
        toBeRemoved = true;
    }

    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

    public long getRemovedTime() {
        return removedTime;
    }

    public void setRemovedTime(long removedTime) {
        this.removedTime = removedTime;
    }

    public long getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(long submittedTime) {
        this.submittedTime = submittedTime;
    }

    public int getNumberOfPendingTasks() {
        return numberOfPendingTasks;
    }

    public void setNumberOfPendingTasks(int numberOfPendingTasks) {
        this.numberOfPendingTasks = numberOfPendingTasks;
    }

    public int getNumberOfRunningTasks() {
        return numberOfRunningTasks;
    }

    public void setNumberOfRunningTasks(int numberOfRunningTasks) {
        this.numberOfRunningTasks = numberOfRunningTasks;
    }

    public int getNumberOfFinishedTasks() {
        return numberOfFinishedTasks;
    }

    public void setNumberOfFinishedTasks(int numberOfFinishedTasks) {
        this.numberOfFinishedTasks = numberOfFinishedTasks;
    }

    public int getNumberOfFailedTasks() {
        return numberOfFailedTasks;
    }

    public void setNumberOfFailedTasks(int numberOfFailedTasks) {
        this.numberOfFailedTasks = numberOfFailedTasks;
    }

    public int getNumberOfFaultyTasks() {
        return numberOfFaultyTasks;
    }

    public void setNumberOfFaultyTasks(int numberOfFaultyTasks) {
        this.numberOfFaultyTasks = numberOfFaultyTasks;
    }

    public int getNumberOfInErrorTasks() {
        return numberOfInErrorTasks;
    }

    public void setNumberOfInErrorTasks(int numberOfInErrorTasks) {
        this.numberOfInErrorTasks = numberOfInErrorTasks;
    }

    public String getJobOwner() {
        return jobOwner;
    }

    public void setJobOwner(String jobOwner) {
        this.jobOwner = jobOwner;
    }

    public JobIdData getJobId() {
        return jobId;
    }

    public void setJobId(JobIdData jobId) {
        this.jobId = jobId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getInErrorTime() {
        return inErrorTime;
    }

    public void setInErrorTime(long inErrorTime) {
        this.inErrorTime = inErrorTime;
    }

    public long getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    public JobStatusData getStatus() {
        return status;
    }

    public void setStatus(JobStatusData status) {
        this.status = status;
    }

    public int getTotalNumberOfTasks() {
        return totalNumberOfTasks;
    }

    public void setTotalNumberOfTasks(int totalNumberOfTasks) {
        this.totalNumberOfTasks = totalNumberOfTasks;
    }

    public JobPriorityData getPriority() {
        return priority;
    }

    public void setPriority(JobPriorityData priority) {
        this.priority = priority;
    }

    public Map<String, String> getGenericInformation() {
        return genericInformation;
    }

    public void setGenericInformation(Map<String, String> genericInformation) {
        this.genericInformation = genericInformation;
    }

    @Override
    public String toString() {
        return "JobInfoData{" + "startTime=" + startTime + ", finishedTime=" + finishedTime + ", submittedTime=" +
               submittedTime + ", removedTime=" + removedTime + ", status=" + status + ", jobId=" + jobId +
               ", totalNumberOfTasks=" + totalNumberOfTasks + ", numberOfPendingTasks=" + numberOfPendingTasks +
               ", numberOfRunningTasks=" + numberOfRunningTasks + ", numberOfFinishedTasks=" + numberOfFinishedTasks +
               ", numberOfFailedTasks=" + numberOfFailedTasks + ", numberOfFaultyTasks=" + numberOfFaultyTasks +
               ", numberOfInErrorTasks=" + numberOfInErrorTasks + ", priority=" + priority + ", jobOwner='" + jobOwner +
               '\'' + ", toBeRemoved=" + toBeRemoved + ", genericInformation=" + genericInformation + '}';
    }

}
