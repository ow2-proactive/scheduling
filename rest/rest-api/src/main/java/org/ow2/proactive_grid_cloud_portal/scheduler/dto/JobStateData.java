/*
 * ################################################################
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
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.ow2.proactive.db.types.BigString;


@XmlRootElement
public class JobStateData {
    private String name;
    private String priority;
    private String owner;
    private JobInfoData jobInfo;
    private String projectName;
    private Map<String, TaskStateData> tasks;
    private Map<String, BigString> genericInformation;

    public Map<String, BigString> getGenericInformation() {
        return genericInformation;
    }

    public void setGenericInformation(Map<String, BigString> genericInformation) {
        this.genericInformation = genericInformation;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public JobInfoData getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfoData jobInfo) {
        this.jobInfo = jobInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return jobInfo.getJobId().getId();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Map<String, TaskStateData> getTasks() {
        return tasks;
    }

    public void setTasks(Map<String, TaskStateData> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "JobStateData{" + "name='" + name + '\'' + ", priority='" + priority + '\'' + ", owner='" +
            owner + '\'' + ", jobInfo=" + jobInfo + ", projectName='" + projectName + '\'' + ", tasks=" +
            tasks + '}';
    }

}
