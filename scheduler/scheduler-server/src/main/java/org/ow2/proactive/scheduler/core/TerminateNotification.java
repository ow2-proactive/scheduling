package org.ow2.proactive.scheduler.core;

import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;


@ActiveObject
public class TerminateNotification implements TaskTerminateNotification {

    private SchedulingService schedulingService;

    public TerminateNotification() {
    }

    TerminateNotification(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Override
    public void terminate(TaskId taskId, TaskResult taskResult) {
        schedulingService.taskTerminatedWithResult(taskId, taskResult);
    }
}
