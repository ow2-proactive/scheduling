package org.ow2.proactive.scheduler.task.utils;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.task.forked.JavaForkerExecutable;


@ActiveObject
public class TaskResultCallback implements TaskTerminateNotification {

    public TaskResultCallback() {
    };

    private JavaForkerExecutable.LauncherGuard launcherGuard;

    public TaskResultCallback(JavaForkerExecutable.LauncherGuard launcherGuard) {
        this.launcherGuard = launcherGuard;
    }

    @Override
    public void terminate(TaskId taskId, TaskResult taskResult) {
        launcherGuard.setResult(taskResult);
        PAActiveObject.terminateActiveObject(false);
    }
}
