/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.job;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.task.ClientTaskState;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;


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
public class ClientJobState extends JobState {

    private final ClientJobSerializationHelper clientJobSerializationHelper;

    private JobInfoImpl jobInfo;

    private String owner;

    private JobType type;

    private Map<TaskId, TaskState> tasks;

    private int maxNumberOfExecution;

    public ClientJobState(JobState jobState) {
        List<TaskState> taskStates = jobState.getTasks();
        this.tasks = new HashMap<>(taskStates.size());

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
        this.variables = jobState.getVariables();

        this.maxNumberOfExecution = jobState.getMaxNumberOfExecution();

        this.clientJobSerializationHelper = new ClientJobSerializationHelper();

        this.setOnTaskError(jobState.getOnTaskErrorProperty().getValue());

        this.genericInformation = new HashMap<>(jobState.getGenericInformation());

        List<ClientTaskState> clientTaskStates = new ArrayList<>(taskStates.size());
        for (TaskState ts : taskStates) {
            clientTaskStates.add(new ClientTaskState(ts));
        }
        addTasks(clientTaskStates);
    }

    @Override
    public int getMaxNumberOfExecution() {
        return this.maxNumberOfExecution;

    }

    @Override
    public void update(TaskInfo taskInfo) {
        if (!getId().equals(taskInfo.getJobId())) {
            throw new IllegalArgumentException("This task info is not applicable to this job. (expected job id is '" +
                                               getId() + "' but got '" + taskInfo.getJobId() + "'");
        }
        jobInfo.setNumberOfFinishedTasks(taskInfo.getJobInfo().getNumberOfFinishedTasks());
        jobInfo.setNumberOfPendingTasks(taskInfo.getJobInfo().getNumberOfPendingTasks());
        jobInfo.setNumberOfRunningTasks(taskInfo.getJobInfo().getNumberOfRunningTasks());
        jobInfo.setNumberOfFaultyTasks(taskInfo.getJobInfo().getNumberOfFaultyTasks());
        jobInfo.setNumberOfInErrorTasks(taskInfo.getJobInfo().getNumberOfInErrorTasks());
        jobInfo.setNumberOfFailedTasks(taskInfo.getJobInfo().getNumberOfFailedTasks());
        jobInfo.setTotalNumberOfTasks(taskInfo.getJobInfo().getTotalNumberOfTasks());
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
            throw new IllegalArgumentException("This job info is not applicable for this job. (expected id is '" +
                                               getId() + "' but was '" + info.getJobId() + "'");
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
        throw new RuntimeException("Not implemented: the restart task on error property is not available on client side.");
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.clientJobSerializationHelper.serializeTasks(this.tasks);
    }

}
