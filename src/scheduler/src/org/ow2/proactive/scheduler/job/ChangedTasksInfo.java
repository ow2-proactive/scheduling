package org.ow2.proactive.scheduler.job;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class ChangedTasksInfo {

    private final Set<TaskId> updatedTasks = new TreeSet<TaskId>();

    private final Set<TaskId> skippedTasks = new TreeSet<TaskId>();

    private final Set<TaskId> newTasks = new TreeSet<TaskId>();

    public void newTasksAdded(Collection<InternalTask> tasks) {
        for (InternalTask task : tasks) {
            newTasks.add(task.getId());
        }
    }

    public void taskUpdated(InternalTask task) {
        if (!newTasks.contains(task.getId())) {
            updatedTasks.add(task.getId());
        }
    }

    public void taskSkipped(InternalTask task) {
        if (!newTasks.contains(task.getId())) {
            skippedTasks.add(task.getId());
        }
    }

    public Set<TaskId> getUpdatedTasks() {
        return updatedTasks;
    }

    public Set<TaskId> getSkippedTasks() {
        return skippedTasks;
    }

    public Set<TaskId> getNewTasks() {
        return newTasks;
    }

}
