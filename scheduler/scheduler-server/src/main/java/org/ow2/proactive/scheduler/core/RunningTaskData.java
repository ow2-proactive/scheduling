package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.TaskLauncher;


class RunningTaskData {

    private final InternalTask task;

    private final String user;

    private final Credentials credendtials;

    private final TaskLauncher launcher;

    private int pingAttempts = 0;

    RunningTaskData(InternalTask task, String user, Credentials credendtials, TaskLauncher launcher) {
        this.task = task;
        this.user = user;
        this.credendtials = credendtials;
        this.launcher = launcher;
    }

    InternalTask getTask() {
        return task;
    }

    TaskLauncher getLauncher() {
        return launcher;
    }

    String getUser() {
        return user;
    }

    Credentials getCredendtials() {
        return credendtials;
    }

    public int increaseAndGetPingAttempts() {
        return ++pingAttempts;
    }
}
