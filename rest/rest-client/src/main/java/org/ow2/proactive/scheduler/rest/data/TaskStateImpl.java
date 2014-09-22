/*
 *  
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest.data;

import static org.ow2.proactive.scheduler.task.TaskIdImpl.createTaskId;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.taskInfo;

import java.util.List;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;


public class TaskStateImpl extends TaskState {
    private static final long serialVersionUID = 60L;

    private TaskStateData d;

    public TaskStateImpl(TaskStateData d) {
        this.d = d;
    }

    @Override
    public TaskId getId() {
        TaskIdData idData = d.getTaskInfo().getTaskId();
        return createTaskId(null, idData.getReadableName(), idData.getId(), false);
    }

    @Override
    public List<TaskState> getDependences() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIterationIndex() {
        return d.getIterationIndex();
    }

    @Override
    public int getMaxNumberOfExecutionOnFailure() {
        return d.getMaxNumberOfExecutionOnFailure();
    }

    @Override
    public int getReplicationIndex() {
        return d.getReplicationIndex();
    }

    @Override
    public TaskInfo getTaskInfo() {
        return taskInfo(d.getTaskInfo());
    }

    @Override
    public TaskState replicate() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(TaskInfo arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getName() {
        return d.getName();
    }

    @Override
    public String getDescription() {
        return d.getDescription();
    }

}
