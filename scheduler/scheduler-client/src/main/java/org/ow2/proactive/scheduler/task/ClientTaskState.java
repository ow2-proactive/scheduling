package org.ow2.proactive.scheduler.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * 
 * This class is a client view of a {@link TaskState}. A client may access
 * instances of this class when connecting to the scheduler front-end and ask
 * for a JobState (for instance by using {@link Scheduler#getJobState(String)}).
 * 
 * The value of some attributes will not be available in this view of the
 * TaskState. Therefore, calling the respective getters will throw a
 * RuntimeException. See the public method's javadoc for more details.
 * 
 * @author esalagea
 * 
 */
public final class ClientTaskState extends TaskState {

    private static final long serialVersionUID = 60L;

    private TaskInfo taskInfo;
    private int maxNumberOfExecutionOnFailure;
    private List<TaskId> dependenceIds = new ArrayList<TaskId>();
    private List<TaskState> dependences = new ArrayList<TaskState>();

    private boolean cancelJobOnError;
    private int maxNumberOfExecution;

    public ClientTaskState(TaskState taskState) {
        // copy information from the TaskStae passed as an argument
        taskInfo = taskState.getTaskInfo();
        maxNumberOfExecutionOnFailure = taskState.getMaxNumberOfExecutionOnFailure();
        this.setName(taskState.getName());

        this.setDescription(taskState.getDescription());
        this.setResultPreview(taskState.getResultPreview());
        this.setRunAsMe(taskState.isRunAsMe());

        this.setWallTime(taskState.getWallTime());
        this.setPreciousResult(taskState.isPreciousResult());
        this.setPreciousLogs(taskState.isPreciousLogs());
        this.setRunAsMe(taskState.isRunAsMe());

        this.cancelJobOnError = taskState.isCancelJobOnError();
        this.maxNumberOfExecution = taskState.getMaxNumberOfExecution();

        this.setParallelEnvironment(taskState.getParallelEnvironment());
        this.setGenericInformations(taskState.getGenericInformations());

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
        this.taskInfo = taskInfo;
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
        return taskInfo.getTaskId().getIterationIndex();
    }

    @Override
    public int getReplicationIndex() {
        return taskInfo.getTaskId().getReplicationIndex();
    }

    public void restoreDependences(Map<TaskId, TaskState> tasksMap) {
        dependences.clear();
        for (TaskId id : dependenceIds) {
            dependences.add(tasksMap.get(id));
        }
    }

    @Override
    public int getMaxNumberOfExecution() {
        return this.maxNumberOfExecution;
    }

    @Override
    public boolean isCancelJobOnError() {
        return cancelJobOnError;
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public List<InputSelector> getInputFilesList() {
        throw new RuntimeException(
            "Not implemented: the input files atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public List<OutputSelector> getOutputFilesList() {
        throw new RuntimeException(
            "Not implemented: the output files atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public Script<?> getPreScript() {
        throw new RuntimeException(
            "Not implemented: the PreScript  atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public Script<?> getPostScript() {
        throw new RuntimeException(
            "Not implemented: the PostScript  atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public List<SelectionScript> getSelectionScripts() {
        throw new RuntimeException(
            "Not implemented: the SelectionScript  atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public FlowScript getFlowScript() {
        throw new RuntimeException(
            "Not implemented: the FlowScript  atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public RestartMode getRestartTaskOnError() {
        throw new RuntimeException(
            "Not implemented: the restart task on error property is not available on client side.");
    }
}
