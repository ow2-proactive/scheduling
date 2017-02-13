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
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class TaskStateData implements Serializable {

    private String name;

    private String description;

    private String tag;

    private int iterationIndex;

    private int replicationIndex;

    private int maxNumberOfExecution;

    private int maxNumberOfExecutionOnFailure;

    private TaskInfoData taskInfo;

    private ParallelEnvironmentData parallelEnvironment;

    private Map<String, String> genericInformation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getIterationIndex() {
        return iterationIndex;
    }

    public void setIterationIndex(int iterationIndex) {
        this.iterationIndex = iterationIndex;
    }

    public int getReplicationIndex() {
        return replicationIndex;
    }

    public void setReplicationIndex(int replicationIndex) {
        this.replicationIndex = replicationIndex;
    }

    public int getMaxNumberOfExecution() {
        return maxNumberOfExecution;
    }

    public void setMaxNumberOfExecution(int maxNumberOfExecution) {
        this.maxNumberOfExecution = maxNumberOfExecution;
    }

    public int getMaxNumberOfExecutionOnFailure() {
        return maxNumberOfExecutionOnFailure;
    }

    public void setMaxNumberOfExecutionOnFailure(int maxNumberOfExecutionOnFailure) {
        this.maxNumberOfExecutionOnFailure = maxNumberOfExecutionOnFailure;
    }

    public TaskInfoData getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfoData taskInfo) {
        this.taskInfo = taskInfo;
    }

    public ParallelEnvironmentData getParallelEnvironment() {
        return parallelEnvironment;
    }

    public void setParallelEnvironment(ParallelEnvironmentData parallelEnvironment) {
        this.parallelEnvironment = parallelEnvironment;
    }

    public Map<String, String> getGenericInformation() {
        return genericInformation;
    }

    public void setGenericInformation(Map<String, String> genericInformation) {
        this.genericInformation = genericInformation;
    }

    public int getNumberOfNodesNeeded() {
        return (parallelEnvironment != null) ? parallelEnvironment.getNodesNumber() : 1;
    }

}
