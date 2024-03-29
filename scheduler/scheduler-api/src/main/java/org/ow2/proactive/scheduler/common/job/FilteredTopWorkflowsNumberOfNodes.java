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
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
public class FilteredTopWorkflowsNumberOfNodes implements Serializable {

    private final String workflowName;

    private final String projectName;

    private final int numberOfNodes;

    private final int numberOfExecutions;

    public FilteredTopWorkflowsNumberOfNodes(String workflowName, String projectName, int numberOfNodes,
            int numberOfExecutions) {
        this.workflowName = workflowName;
        this.projectName = projectName;
        this.numberOfNodes = numberOfNodes;
        this.numberOfExecutions = numberOfExecutions;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getNumberOfExecutions() {
        return numberOfExecutions;
    }
}
