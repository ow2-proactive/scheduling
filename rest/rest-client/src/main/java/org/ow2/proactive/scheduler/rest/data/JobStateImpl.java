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

import static org.ow2.proactive.scheduler.rest.data.DataUtility.toJobInfo;

import java.util.ArrayList;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;


public class JobStateImpl extends JobState {
    private static final long serialVersionUID = 62L;

    private JobStateData d;

    JobStateImpl(JobStateData d) {
        this.d = d;
    }

    @Override
    public Map<TaskId, TaskState> getHMTasks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JobInfo getJobInfo() {
        return toJobInfo(d.getJobInfo());
    }

    @Override
    public String getOwner() {
        return d.getOwner();
    }

    @Override
    public ArrayList<TaskState> getTasks() {
        ArrayList<TaskState> taskStateList = new ArrayList<TaskState>();
        Map<String, TaskStateData> taskStateMap = d.getTasks();
        for (TaskStateData ts : taskStateMap.values()) {
            taskStateList.add(DataUtility.taskState(ts));
        }
        return taskStateList;
    }

    @Override
    public void update(TaskInfo taskInfo) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void update(JobInfo jobInfo) {
        throw new UnsupportedOperationException();

    }

    @Override
    public JobType getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return d.getName();
    }

    @Override
    public JobPriority getPriority() {
        return JobPriority.valueOf(d.getPriority());
    }
}
