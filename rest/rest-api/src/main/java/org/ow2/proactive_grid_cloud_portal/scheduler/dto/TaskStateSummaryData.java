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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class TaskStateSummaryData implements Serializable {

    private String name;

    private int count;

    private int countFinished;

    private int countErrors = 0;

    private long avgExecutionTime = 0;

    private TaskStatusData taskStatus;

    public TaskStateSummaryData(String name, int count, TaskStatusData taskStatus) {
        this.name = name;
        this.count = count;
        this.taskStatus = taskStatus;
    }

    public TaskStateSummaryData(String name, int count, int countErrors, TaskStatusData taskStatus) {
        this.name = name;
        this.count = count;
        this.countErrors = countErrors;
        this.taskStatus = taskStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCountErrors() {
        return countErrors;
    }

    public void setCountErrors(int countErrors) {
        this.countErrors = countErrors;
    }

    public int getCountFinished() {
        return countFinished;
    }

    public void setCountFinished(int countFinished) {
        this.countFinished = countFinished;
    }

    public TaskStatusData getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatusData taskStatus) {
        this.taskStatus = taskStatus;
    }

    public long getAvgExecutionTime() {
        return avgExecutionTime;
    }

    public void setAvgExecutionTime(long avgExecutionTime) {
        this.avgExecutionTime = avgExecutionTime;
    }
}
