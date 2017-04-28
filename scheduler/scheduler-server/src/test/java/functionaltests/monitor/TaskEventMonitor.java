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
package functionaltests.monitor;

import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * Defines An EventMonitor for a Task event.
 * This class is used for two purposes :
 * - representing a task event thrown by scheduler (memorization of an event)
 * - representing an Monitor for a task event awaited and not yet occurred
 * 	(monitor object for an awaited event) .
 *
 * @author ProActive team
 *
 */
public class TaskEventMonitor extends JobEventMonitor {

    /**
     * Name of the task for which event is awaited or occurred
     */
    private String taskName;

    /**
     * TaskInfo object to return to Threads that
     * call waitForEventTask**()
     *
     */
    private TaskInfo taskInfo;

    /**
     * Constructor, to use this Object as waiting monitor.
     * (TaskEvent field is not yet defined).
     * @param event event type to wait for
     * @param id corresponding JobId of the task defining the monitor
     * @param name taskName for which event is awaited
     */
    public TaskEventMonitor(SchedulerEvent event, JobId id, String name) {
        super(event, id);
        this.taskName = name;
    }

    /**
     * Create a monitor with a corresponding event and
     * event's associated TaskInfo object
     * @param event
     * @param info associated TaskInfo object
     */
    public TaskEventMonitor(SchedulerEvent event, TaskInfo info) {
        super(event, info.getJobId());
        taskName = info.getTaskId().getReadableName();
        taskInfo = info;
    }

    /**
     * @return task name corresponding to this EventMonitor
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @return TaskEvent object associated to this EventMonitor, if
     * event has occurred, null otherwise.
     */
    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    /**
     * Set TaskEvent object, to use when event has occurred
     * @param info
     */
    public void setTaskInfo(TaskInfo info) {
        this.taskInfo = info;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return (((TaskEventMonitor) o).getTaskName().equals(taskName));
        }
        return false;
    }
}
