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

public class FilteredTopWorkflowsCumulatedCoreTimeData implements java.io.Serializable {

    private String workflowName;

    private String projectName;

    private long cumulatedCoreTime;

    private long numberOfExecutions;

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public long getCumulatedCoreTime() {
        return cumulatedCoreTime;
    }

    public void setCumulatedCoreTime(long cumulatedCoreTime) {
        this.cumulatedCoreTime = cumulatedCoreTime;
    }

    public long getNumberOfExecutions() {
        return numberOfExecutions;
    }

    public void setNumberOfExecutions(long numberOfExecutions) {
        this.numberOfExecutions = numberOfExecutions;
    }

    @Override
    public String toString() {
        return "FilteredTopWorkflowData{" + "workflowName='" + workflowName + '\'' + ", projectName='" + projectName +
               '\'' + ", cumulatedCoreTime=" + cumulatedCoreTime + ", numberOfExecutions=" + numberOfExecutions + '}';
    }
}
