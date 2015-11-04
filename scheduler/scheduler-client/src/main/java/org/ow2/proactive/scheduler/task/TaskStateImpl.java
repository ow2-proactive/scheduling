package org.ow2.proactive.scheduler.task;

import java.util.List;

import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

public class TaskStateImpl extends TaskState {

    private TaskInfo taskInfo;
    private int maxNumberOfExecutionOnFailure = PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.getValueAsInt();
    private int iterationIndex;
    private int replicationIndex;
    
    @Override
    public void update(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    @Override
    public List<TaskState> getDependences() {
        return null;
    }

    @Override
    public TaskInfo getTaskInfo() {
        return taskInfo;
    }
    
    public void setMaxNumberOfExecutionOnFailure(int maxNumberOfExecutionOnFailure) {
        this.maxNumberOfExecutionOnFailure = maxNumberOfExecutionOnFailure;
    }
    
    @Override
    public int getMaxNumberOfExecutionOnFailure() {
        return maxNumberOfExecutionOnFailure;
    }

    @Override
    public TaskState replicate() throws Exception {
        TaskStateImpl impl = new TaskStateImpl();
        impl.update(taskInfo);
        impl.setName(name);
        impl.setDescription(getDescription());
        impl.setTag(getTag());
        impl.setIterationIndex(getIterationIndex());
        impl.setReplicationIndex(getReplicationIndex());
        impl.setMaxNumberOfExecution(getMaxNumberOfExecution());
        impl.setParallelEnvironment(getParallelEnvironment());
        impl.setNumberOfNeededNodes(getNumberOfNodesNeeded());
        return impl;
    }

    public void setIterationIndex(int iterationIndex) {
        this.iterationIndex = iterationIndex;
    }
    
    @Override
    public int getIterationIndex() {
        return iterationIndex;
    }

    public void setReplicationIndex(int replicationIndex) {
        this.replicationIndex = replicationIndex;
    }
    
    @Override
    public int getReplicationIndex() {
        return replicationIndex;
    }

}
