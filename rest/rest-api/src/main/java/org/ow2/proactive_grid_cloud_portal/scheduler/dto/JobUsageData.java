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

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class JobUsageData {
    private String owner;

    private String tenant;

    private String project;

    private String jobId;

    private String jobName;

    private long jobDuration;

    private List<TaskUsageData> taskUsages;

    private String status;

    private long submittedTime;

    private Long parentId;

    public JobUsageData() {
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setJobDuration(long jobDuration) {
        this.jobDuration = jobDuration;
    }

    public void setTaskUsages(List<TaskUsageData> taskUsages) {
        this.taskUsages = taskUsages;
    }

    public String getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public long getJobDuration() {
        return jobDuration;
    }

    public List<TaskUsageData> getTaskUsages() {
        return taskUsages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(long submittedTime) {
        this.submittedTime = submittedTime;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
