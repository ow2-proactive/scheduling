package org.ow2.proactive.scheduler.job;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.*;
import org.ow2.proactive.scheduler.task.ClientTaskState;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is a client view of a {@link JobState}. A client will receive an
 * instance of this class when connecting to the scheduler front-end and ask for
 * a JobState (for instance by using {@link Scheduler#getJobState(String)}).
 * 
 * The value of some attributes will not be available in this view of the
 * JobState. Therefore, calling the respective getters will throw a
 * RuntimeException. See the public method's javadoc for more details.
 * 
 * @author esalagea
 */
public final class ClientJobState extends JobState {

    private final ClientJobSerializationHelper clientJobSerializationHelper;
    private JobInfoImpl jobInfo;
    private String owner;
    private JobType type;
    private Map<TaskId, TaskState> tasks = new HashMap<>();
    private boolean cancelJobOnError;
    private int maxNumberOfExecution;
    private HashMap<String, String> genericInformations;

    public ClientJobState(JobState jobState) {
        // converting internal job into a light job descriptor
        jobInfo = (JobInfoImpl) jobState.getJobInfo();
        owner = jobState.getOwner();
        type = jobState.getType();

        this.name = jobState.getName();
        this.description = jobState.getDescription();
        this.projectName = jobState.getProjectName();
        this.priority = jobState.getPriority();
        this.inputSpace = jobState.getInputSpace();
        this.outputSpace = jobState.getOutputSpace();

        this.cancelJobOnError = jobState.isCancelJobOnError();
        this.maxNumberOfExecution = jobState.getMaxNumberOfExecution();

        this.genericInformations = new HashMap<>(jobState.getGenericInformations());

        this.clientJobSerializationHelper = new ClientJobSerializationHelper();

        List<ClientTaskState> taskStates = new ArrayList<>();
        for (TaskState ts : jobState.getTasks()) {
            taskStates.add(new ClientTaskState(ts));
        }
        addTasks(taskStates);

    }

    @Override
    public int getMaxNumberOfExecution() {
        return this.maxNumberOfExecution;

    }

    @Override
    public boolean isCancelJobOnError() {
        return cancelJobOnError;
    }

    @Override
    public Map<String, String> getGenericInformations() {
        return this.genericInformations;
    }

    @Override
    public void update(TaskInfo taskInfo) {
        if (!getId().equals(taskInfo.getJobId())) {
            throw new IllegalArgumentException(
                "This task info is not applicable to this job. (expected job id is '" + getId() +
                    "' but got '" + taskInfo.getJobId() + "'");
        }
        jobInfo.setNumberOfFinishedTasks(taskInfo.getJobInfo().getNumberOfFinishedTasks());
        jobInfo.setNumberOfPendingTasks(taskInfo.getJobInfo().getNumberOfPendingTasks());
        jobInfo.setNumberOfRunningTasks(taskInfo.getJobInfo().getNumberOfRunningTasks());
        try {
            tasks.get(taskInfo.getTaskId()).update(taskInfo);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("This task info is not applicable in this job. (task id '" +
                taskInfo.getTaskId() + "' not found)");
        }
    }

    @Override
    public void update(JobInfo info) {
        if (!getId().equals(info.getJobId())) {
            throw new IllegalArgumentException(
                "This job info is not applicable for this job. (expected id is '" + getId() + "' but was '" +
                    info.getJobId() + "'");
        }
        // update job info
        this.jobInfo = new JobInfoImpl((JobInfoImpl) info);
        // update skipped tasks
        if (this.jobInfo.getTasksSkipped() != null) {
            for (TaskId id : tasks.keySet()) {
                if (this.jobInfo.getTasksSkipped().contains(id)) {
                    TaskInfoImpl taskInfo = (TaskInfoImpl) tasks.get(id).getTaskInfo();
                    taskInfo.setStatus(TaskStatus.SKIPPED);
                }
            }
        }
        // add new or modify existing tasks
        // additions and modifications can be caused by control flow actions
        if (this.jobInfo.getModifiedTasks() != null) {
            addTasks(this.jobInfo.getModifiedTasks());
        }

    }

    @Override
    public JobInfo getJobInfo() {
        return jobInfo;
    }

    @Override
    public ArrayList<TaskState> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Map<TaskId, TaskState> getHMTasks() {
        return tasks;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public JobType getType() {
        return type;
    }

    private void addTasks(List<ClientTaskState> newTasks) {
        for (ClientTaskState ts : newTasks) {
            tasks.put(ts.getId(), ts);
        }
        for (ClientTaskState ts : newTasks) {
            ts.restoreDependences(tasks);
        }
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


    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.clientJobSerializationHelper.serializeTasks(this.tasks);
    }

}
