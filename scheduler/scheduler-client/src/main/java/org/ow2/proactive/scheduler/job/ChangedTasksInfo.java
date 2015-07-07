package org.ow2.proactive.scheduler.job;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;


public class ChangedTasksInfo {

    private final Set<TaskId> updatedTasks = new TreeSet<>();

    private final Set<TaskId> skippedTasks = new TreeSet<>();

    private final Set<TaskId> newTasks = new TreeSet<>();

    public void newTasksAdded(Collection<? extends TaskState> tasks) {
        for (TaskState task : tasks) {
            newTasks.add(task.getId());
        }
    }

    public void taskUpdated(TaskState task) {
        if (!newTasks.contains(task.getId())) {
            updatedTasks.add(task.getId());
        }
    }

    public void taskSkipped(TaskState task) {
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
