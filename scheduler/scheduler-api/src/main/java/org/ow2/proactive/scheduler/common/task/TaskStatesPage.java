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
package org.ow2.proactive.scheduler.common.task;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * This class holds a paginated list of <code>TaskState</code>
 * server-wise and the total number of tasks.
 * 
 */
@XmlRootElement
public class TaskStatesPage {

    private int size;

    private List<TaskState> taskStates;

    public TaskStatesPage() {

    }

    public TaskStatesPage(List<TaskState> taskStates, int size) {
        this.taskStates = taskStates;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public List<TaskState> getTaskStates() {
        return taskStates;
    }

    public void setTaskStates(List<TaskState> taskStates) {
        this.taskStates = taskStates;
    }

    @Override
    public String toString() {
        return "TaskStatePage{" + "size=" + size + ", taskStates='" + taskStates + '\'' + '}';
    }
}
