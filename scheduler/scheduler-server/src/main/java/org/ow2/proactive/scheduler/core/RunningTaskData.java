package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.utils.NodeSet;


class RunningTaskData {

    private final InternalTask task;
    private final String user;
    private final Credentials credentials;
    private final TaskLauncher launcher;
    private final NodeSet nodes;
    private int pingAttempts = 0;

    RunningTaskData(InternalTask task, String user, Credentials credentials, TaskLauncher launcher) {
        this.task = task;
        // keep track of nodes that executed the task, can change in case of restarts
        this.nodes = task.getExecuterInformation().getNodes();
        this.user = user;
        this.credentials = credentials;
        this.launcher = launcher;
    }

    /**
     * @return Mutable task state, can be changed by multiple threads
     */
    InternalTask getTask() {
        return task;
    }

    TaskLauncher getLauncher() {
        return launcher;
    }

    String getUser() {
        return user;
    }

    Credentials getCredentials() {
        return credentials;
    }

    public int increaseAndGetPingAttempts() {
        return ++pingAttempts;
    }

    /**
     * @return Nodes that were used to run this particular instance of the task
     *  (those in {@link InternalTask#getExecuterInformation()} could have changed in case of restarts)
     */
    public NodeSet getNodes() {
        return nodes;
    }
}
