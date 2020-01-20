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

public class TaskUsageData {
    private String taskId;

    private String taskName;

    private long taskStartTime;

    private long taskFinishedTime;

    private long taskExecutionDuration;

    private int taskNodeNumber;

    private String taskStatus;

    private String taskTag;

    private String taskDescription;

    private String executionHostName;

    private int numberOfExecutionLeft;

    private int numberOfExecutionOnFailureLeft;

    private int maxNumberOfExecution;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public long getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(long taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public long getTaskFinishedTime() {
        return taskFinishedTime;
    }

    public void setTaskFinishedTime(long taskFinishedTime) {
        this.taskFinishedTime = taskFinishedTime;
    }

    public long getTaskExecutionDuration() {
        return taskExecutionDuration;
    }

    public void setTaskExecutionDuration(long taskExecutionDuration) {
        this.taskExecutionDuration = taskExecutionDuration;
    }

    public int getTaskNodeNumber() {
        return taskNodeNumber;
    }

    public void setTaskNodeNumber(int taskNodeNumber) {
        this.taskNodeNumber = taskNodeNumber;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTag() {
        return taskTag;
    }

    public void setTaskTag(String taskTag) {
        this.taskTag = taskTag;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
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

    public void setNumberOfExecutionOnFailureLeft(int numberOfExecutionOnFailureLeft) {
        this.numberOfExecutionOnFailureLeft = numberOfExecutionOnFailureLeft;
    }

    public int getMaxNumberOfExecution() {
        return maxNumberOfExecution;
    }

    public void setMaxNumberOfExecution(int maxNumberOfExecution) {
        this.maxNumberOfExecution = maxNumberOfExecution;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }
}
