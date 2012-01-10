package org.ow2.proactive.scheduler.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;


public class ClientTaskState extends TaskState {

    private TaskInfo taskInfo;
    private int maxNumberOfExecutionOnFailure;
    private int iterationIndex;
    private int replicationIndex;
    private List<TaskId> dependenceIds = new ArrayList<TaskId>();
    private List<TaskState> dependences = new ArrayList<TaskState>();

    public ClientTaskState(TaskState taskState) {
        //copy information from the TaskStae passed as an argument
        taskInfo = taskState.getTaskInfo();
        maxNumberOfExecutionOnFailure = taskState.getMaxNumberOfExecutionOnFailure();
        iterationIndex = taskState.getIterationIndex();
        replicationIndex = taskState.getReplicationIndex();
        this.setName(taskState.getName());

        // Store only task IDs here; #restoreDependences is later called by
        // ClientJobState in order for this instance to store references to the
        // same ClientTaskState instances as the ones held in the
        // ClientJobState#tasks field.
        if (taskState.getDependences() != null) {
            for (TaskState dep : taskState.getDependences()) {
                dependenceIds.add(dep.getId());
            }
        }
    }

    @Override
    public void update(TaskInfo taskInfo) {
        taskInfo = taskInfo;
    }

    @Override
    public List<TaskState> getDependences() {
        return dependences;
    }

    @Override
    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    @Override
    public int getMaxNumberOfExecutionOnFailure() {
        return maxNumberOfExecutionOnFailure;
    }

    @Override
    public TaskState replicate() throws Exception {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public int getIterationIndex() {
        return iterationIndex;
    }

    @Override
    public int getReplicationIndex() {
        return replicationIndex;
    }

    public void restoreDependences(Map<TaskId, TaskState> tasksMap) {
        dependences.clear();
        for (TaskId id : dependenceIds) {
            dependences.add(tasksMap.get(id));
        }
    }
}
