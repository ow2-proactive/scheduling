package functionnaltests.monitor;

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

    public boolean equals(Object o) {
        if (super.equals(o)) {
            return (((TaskEventMonitor) o).getTaskName().equals(taskName));
        }
        return false;
    }
}