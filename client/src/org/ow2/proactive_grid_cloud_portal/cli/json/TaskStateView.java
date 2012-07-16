/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.json;

public class TaskStateView {

    private int iterationIndex;
    private int replicationIndex;
    private int maxNumberOfExecution;
    private int maxNumberOfExecutionOnFailure;
    private TaskInfoView taskInfo;
    private ParallelEnvironmentView parallelEnvironment;

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

    public void setMaxNumberOfExecutionOnFailure(
            int maxNumberOfExecutionOnFailure) {
        this.maxNumberOfExecutionOnFailure = maxNumberOfExecutionOnFailure;
    }

    public TaskInfoView getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfoView taskInfo) {
        this.taskInfo = taskInfo;
    }

    public ParallelEnvironmentView getParallelEnvironment() {
        return parallelEnvironment;
    }

    public void setParallelEnvironment(
            ParallelEnvironmentView parallelEnvironment) {
        this.parallelEnvironment = parallelEnvironment;
    }

    public int getNumberOfNodesNeeded() {
        return (parallelEnvironment != null) ? parallelEnvironment
                .getNodesNumber() : 1;
    }

}
