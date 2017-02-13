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

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class JobResultData {

    private JobIdData id;

    private JobInfoData jobInfo;

    private Map<String, TaskResultData> allResults;

    private Map<String, TaskResultData> preciousResults;

    private Map<String, TaskResultData> exceptionResults;

    public JobIdData getId() {
        return id;
    }

    public void setId(JobIdData id) {
        this.id = id;
    }

    public Map<String, TaskResultData> getAllResults() {
        return allResults;
    }

    public void setAllResults(Map<String, TaskResultData> allResults) {
        this.allResults = allResults;
    }

    public JobInfoData getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfoData jobInfo) {
        this.jobInfo = jobInfo;
    }

    public Map<String, TaskResultData> getPreciousResults() {
        return preciousResults;
    }

    public void setPreciousResults(Map<String, TaskResultData> preciousResults) {
        this.preciousResults = preciousResults;
    }

    public Map<String, TaskResultData> getExceptionResults() {
        return exceptionResults;
    }

    public void setExceptionResults(Map<String, TaskResultData> exceptionResults) {
        this.exceptionResults = exceptionResults;
    }

    @Override
    public String toString() {
        return "JobResultData{" + "id=" + id + ", allResults=" + allResults + ", preciousResults=" + preciousResults +
               ", exceptionResults=" + exceptionResults + ", jobInfo=" + jobInfo + '}';
    }
}
