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
    private Map<String, String> genericInformations;

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

    public String getTag() { return this.tag; }

    public void setTag(String tag) { this.tag = tag; }

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

    public Map<String, String> getGenericInformations() { return genericInformations; }

    public void setGenericInformations(Map<String, String> genericInformations) {
        this.genericInformations = genericInformations;
    }

    public int getNumberOfNodesNeeded() {
        return (parallelEnvironment != null) ? parallelEnvironment.getNodesNumber() : 1;
    }

}
