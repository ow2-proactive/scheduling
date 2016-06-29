/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.TaskDescriptor;
import org.ow2.proactive.scheduler.job.termination.handlers.TerminateIfTaskHandler;
import org.ow2.proactive.scheduler.job.termination.handlers.TerminateLoopHandler;
import org.ow2.proactive.scheduler.job.termination.handlers.TerminateReplicateTaskHandler;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import it.sauronsoftware.cron4j.Predictor;


/**
 * Internal and global description of a job. This class contains all information
 * about the job to launch. It also provides method to manage the content
 * regarding the scheduling process. Specific internal job may extend this
 * abstract class.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public abstract class InternalJob extends JobState {

    protected static final Logger LOGGER = Logger.getLogger(InternalJob.class);

    /** List of every tasks in this job. */
    protected Map<TaskId, InternalTask> tasks = new HashMap<>();

    /** Informations (that can be modified) about job execution */
    protected JobInfoImpl jobInfo = new JobInfoImpl();

    /** Job descriptor for dependences management */
    @XmlTransient
    private JobDescriptor jobDescriptor;

    /** DataSpace application manager per task. The key is the task ID */
    // Not DB managed, created once needed.
    @XmlTransient
    private Map<Long, TaskDataSpaceApplication> taskDataSpaceApplications;

    /** Initial waiting time for a task before restarting in millisecond */
    private long restartWaitingTimer = PASchedulerProperties.REEXECUTION_INITIAL_WAITING_TIME.getValueAsInt();

    /**
     * used credentials to fork as user id. Can be null, or contain
     * user/pwd[/key]
     */
    @XmlTransient
    private Credentials credentials = null;

    @XmlTransient
    private final transient TerminateLoopHandler terminateLoopHandler;

    @XmlTransient
    private final transient TerminateIfTaskHandler terminateIfTaskHandler;

    @XmlTransient
    private final transient TerminateReplicateTaskHandler terminateReplicateTaskHandler;

    @XmlTransient
    private final transient Set<TaskId> faultyTasks;

    /** Hibernate default constructor */
    public InternalJob() {
        this.faultyTasks = new HashSet<>();
        this.terminateLoopHandler = new TerminateLoopHandler(this);
        this.terminateIfTaskHandler = new TerminateIfTaskHandler(this);
        this.terminateReplicateTaskHandler = new TerminateReplicateTaskHandler(this);
    }

    /**
     * Create a new Job with the given parameters. It provides methods to add or
     * remove tasks.
     *
     * @param name
     *            the current job name.
     * @param priority
     *            the priority of this job between 1 and 5.
     * @param onTaskError
     *            Sets the error behavior if a task fails.
     * @param description
     *            a short description of the job and what it will do.
     */

    public InternalJob(String name, JobPriority priority, OnTaskError onTaskError, String description) {
        this();
        this.name = name;
        this.jobInfo.setPriority(priority);
        this.setOnTaskError(onTaskError);
        this.description = description;
    }

    /**
     * This method will perform two actions. First, it will update the job with
     * the information contained in the givem taskInfo. Then, it will update the
     * proper task using the same given taskInfo.
     *
     * @param info
     *            a taskInfo containing new information about the task.
     */
    @Override
    public synchronized void update(TaskInfo info) {
        // ensure that is a JobInfoImpl
        // if not, we are in client side and client brings its own JobInfo
        // Implementation
        if (!getId().equals(info.getJobId())) {
            throw new IllegalArgumentException(
                "This job info is not applicable for this job. (expected id is '" + getId() + "' but was '" +
                    info.getJobId() + "'");
        }
        jobInfo = (JobInfoImpl) info.getJobInfo();
        try {
            tasks.get(info.getTaskId()).update(info);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("This task info is not applicable in this job. (task id '" +
                info.getTaskId() + "' not found)");
        }
    }

    @Override
    public synchronized void update(JobInfo info) {
        if (!getId().equals(info.getJobId())) {
            throw new IllegalArgumentException(
                "This job info is not applicable for this job. (expected id is '" + getId() + "' but was '" +
                    info.getJobId() + "'");
        }
        // update job info
        this.jobInfo = (JobInfoImpl) info;

        // update skipped tasks
        if (this.jobInfo.getTasksSkipped() != null) {
            for (TaskId id : tasks.keySet()) {
                if (this.jobInfo.getTasksSkipped().contains(id)) {
                    TaskInfoImpl taskInfo = (TaskInfoImpl) tasks.get(id).getTaskInfo();
                    taskInfo.setStatus(TaskStatus.SKIPPED);
                }
            }
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobState#getJobInfo()
     */
    @Override
    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfoImpl jobInfo) {
        this.jobInfo = jobInfo;
    }

    /**
     * Append a task to this job.
     *
     * @param task
     *            the task to add.
     * @return true if the task has been correctly added to the job, false if
     *         not.
     */
    public boolean addTask(InternalTask task) {
        task.setJobId(getId());

        int taskId = tasks.size();
        task.setId(TaskIdImpl.createTaskId(getId(), task.getName(), taskId));

        boolean result = (tasks.put(task.getId(), task) == null);

        if (result) {
            jobInfo.setTotalNumberOfTasks(jobInfo.getTotalNumberOfTasks() + 1);
        }

        return result;
    }

    /**
     * Start a new task will set some count and update dependencies if
     * necessary.
     *
     * @param td
     *            the task which has just been started.
     */
    public void startTask(InternalTask td) {
        setNumberOfPendingTasks(getNumberOfPendingTasks() - 1);
        setNumberOfRunningTasks(getNumberOfRunningTasks() + 1);

        if (getStatus() == JobStatus.STALLED) {
            setStatus(JobStatus.RUNNING);
        }

        getJobDescriptor().start(td.getId());
        td.setStatus(TaskStatus.RUNNING);
        td.setStartTime(System.currentTimeMillis());
        td.setFinishedTime(-1);
        td.setExecutionHostName(td.getExecuterInformation().getHostName() + " (" +
            td.getExecuterInformation().getNodeName() + ")");
    }

    /**
     * Start dataspace configuration and application
     */
    public void startDataSpaceApplication(NamingService namingService, List<InternalTask> tasks) {
        if (taskDataSpaceApplications == null) {
            taskDataSpaceApplications = new HashMap<>();
        }

        for (InternalTask internalTask : tasks) {
            long taskId = internalTask.getId().longValue();

            // reuse the already configured dataspaceApplication
            // if a task restart due to a failure for instance
            if (!taskDataSpaceApplications.containsKey(taskId)) {
                String appId = internalTask.getId().toString();
                TaskDataSpaceApplication taskDataSpaceApplication = new TaskDataSpaceApplication(appId,
                    namingService);

                taskDataSpaceApplications.put(taskId, taskDataSpaceApplication);

                taskDataSpaceApplication.startDataSpaceApplication(getInputSpace(), getOutputSpace(),
                        getGlobalSpace(), getUserSpace(), getOwner(), getId());
            }
        }
    }

    /**
     * Updates count for running to pending event.
     */
    public void newWaitingTask() {
        setNumberOfPendingTasks(getNumberOfPendingTasks() + 1);
        setNumberOfRunningTasks(getNumberOfRunningTasks() - 1);
        if (getNumberOfRunningTasks() == 0 &&
            !(getStatus() == JobStatus.PAUSED || getStatus() == JobStatus.IN_ERROR)) {
            setStatus(JobStatus.STALLED);
        }
    }

    /**
     * Set this task in restart mode, it will set the task to pending status and
     * change task count.
     *
     * @param task
     *            the task which has to be restarted.
     */
    public void reStartTask(InternalTask task) {
        getJobDescriptor().reStart(task.getId());
        task.setProgress(0);
        if (getStatus() == JobStatus.PAUSED) {
            task.setStatus(TaskStatus.PAUSED);
            getJobDescriptor().pause(task.getId());
        } else {
            task.setStatus(TaskStatus.PENDING);
        }
    }

    /**
     * Terminate a task, change status, managing dependencies
     *
     * Also, apply a Control Flow Action if provided. This may alter the number
     * of tasks in the job, events have to be sent accordingly.
     *
     * @param errorOccurred
     *            has an error occurred for this termination
     * @param taskId
     *            the task to terminate.
     * @param frontend
     *            Used to notify all listeners of the replication of tasks,
     *            triggered by the FlowAction
     * @param action
     *            a Control Flow Action that will potentially create new tasks
     *            inside the job
     * @return the taskDescriptor that has just been terminated.
     */
    public ChangedTasksInfo terminateTask(boolean errorOccurred, TaskId taskId, SchedulerStateUpdate frontend,
            FlowAction action, TaskResultImpl result) {
        final InternalTask descriptor = tasks.get(taskId);

        if (!errorOccurred) {
            decreaseNumberOfFaultyTasks(taskId);
        }

        descriptor.setFinishedTime(System.currentTimeMillis());
        descriptor.setStatus(errorOccurred ? TaskStatus.FAULTY : TaskStatus.FINISHED);
        descriptor.setExecutionDuration(result.getTaskDuration());

        setNumberOfRunningTasks(getNumberOfRunningTasks() - 1);
        setNumberOfFinishedTasks(getNumberOfFinishedTasks() + 1);

        if ((getStatus() == JobStatus.RUNNING) && (getNumberOfRunningTasks() == 0)) {
            setStatus(JobStatus.STALLED);
        }

        ChangedTasksInfo changesInfo = new ChangedTasksInfo();
        changesInfo.taskUpdated(descriptor);

        boolean didAction = false;
        if (action != null) {
            InternalTask initiator = tasks.get(taskId);

            switch (action.getType()) {
                /*
                 * LOOP action
                 */
                case LOOP: {
                    didAction = terminateLoopHandler.terminateLoopTask(action, initiator, changesInfo,
                            frontend);
                    break;
                }

                    /*
                     * IF action
                     */
                case IF: {
                    didAction = terminateIfTaskHandler.terminateIfTask(action, initiator, changesInfo,
                            frontend, descriptor, taskId);
                    break;
                }

                    /*
                     * REPLICATE action
                     */
                case REPLICATE: {
                    didAction = terminateReplicateTaskHandler.terminateReplicateTask(action, initiator,
                            changesInfo, frontend, taskId);
                    break;

                }

                    /*
                     * CONTINUE action : - continue taskflow as if no action was provided
                     */
                case CONTINUE:

                    LOGGER.debug("Task flow Action CONTINUE on task " + initiator.getId().getReadableName());
                    break;
            }

            /**
             * System.out.println("******** task dump ** " +
             * this.getJobInfo().getJobId() + " " + initiator.getName() +
             * " does " + action.getType() + " " + ((action.getTarget() == null)
             * ? "." : action.getTarget()) + " " + ((action.getTargetElse() ==
             * null) ? "." : action.getTargetElse()) + " " +
             * ((action.getTargetJoin() == null) ? "." :
             * action.getTargetJoin())); for (InternalTask it :
             * this.tasks.values()) { System.out.print(it.getName() + " "); if
             * (it.getIDependences() != null) { System.out.print("deps "); for
             * (InternalTask parent : it.getIDependences()) {
             * System.out.print(parent.getName() + " "); } } if
             * (it.getIfBranch() != null) { System.out.print("if " +
             * it.getIfBranch().getName() + " "); } if (it.getJoinedBranches()
             * != null && it.getJoinedBranches().size() == 2) {
             * System.out.print("join " +
             * it.getJoinedBranches().get(0).getName() + " " +
             * it.getJoinedBranches().get(1).getName()); } System.out.println();
             * } System.out.println("******** task dump ** " +
             * this.getJobInfo().getJobId()); System.out.println();
             **/

        }

        // terminate this task
        if (!didAction) {
            getJobDescriptor().terminate(taskId);
        }

        return changesInfo;
    }

    /**
     * Assign a tag to new duplicated task because of a REPLICATE or LOOP.
     * 
     * @param replicatedTask
     *            the new duplicated task.
     * @param initiator
     *            the initiator of the duplication.
     * @param loopAction
     *            true if the duplication if after a loop or, false if it is a
     *            replicate.
     * @param action
     *            the duplication action.
     */
    private void assignReplicationTag(InternalTask replicatedTask, InternalTask initiator, boolean loopAction,
            FlowAction action) {
        StringBuilder buf = new StringBuilder();

        if (loopAction) {
            buf.append("LOOP-");
            buf.append(InternalTask.getInitialName(initiator.getName()));
            if (initiator.getReplicationIndex() > 0) {
                buf.append("*");
                buf.append(initiator.getReplicationIndex());
            }
        } else {
            buf.append("REPLICATE-");
            buf.append(initiator.getName());
        }

        buf.append("-");

        if (loopAction) {
            String cronExpr = action.getCronExpr();
            if (cronExpr.isEmpty()) {
                buf.append(replicatedTask.getIterationIndex());
            } else {
                // cron task: the replication index is the next date that
                // matches the cron expression
                Date resolvedCron = new Predictor(cronExpr).nextMatchingDate();
                SimpleDateFormat dt = new SimpleDateFormat("dd_MM_YY_HH_mm");
                buf.append(dt.format(resolvedCron));
            }
        } else {
            buf.append(replicatedTask.getReplicationIndex());
        }

        replicatedTask.setTag(buf.toString());
    }

    public boolean replicateForNextLoopIteration(InternalTask initiator, InternalTask target,
            ChangedTasksInfo changesInfo, SchedulerStateUpdate frontend, FlowAction action) {

        LOGGER.info("LOOP (init:" + initiator.getId() + "; target:" + target.getId() + ")");

        // accumulates the tasks between the initiator and the target
        Map<TaskId, InternalTask> dup = new HashMap<>();

        // replicate the tasks between the initiator and the target
        try {
            initiator.replicateTree(dup, target.getId(), true, initiator.getReplicationIndex(),
                    initiator.getIterationIndex());
        } catch (ExecutableCreationException e) {
            LOGGER.error("", e);
            return false;
        }

        ((JobInfoImpl) this.getJobInfo())
                .setNumberOfPendingTasks(this.getJobInfo().getNumberOfPendingTasks() + dup.size());

        // ensure naming unicity
        // time-consuming but safe
        for (InternalTask nt : dup.values()) {
            boolean ok;
            do {
                ok = true;
                for (InternalTask task : tasks.values()) {
                    if (nt.getName().equals(task.getName())) {
                        nt.setIterationIndex(nt.getIterationIndex() + 1);
                        ok = false;
                    }
                }
            } while (!ok);
        }

        // configure the new tasks
        InternalTask newTarget = null;
        InternalTask newInit = null;
        for (Entry<TaskId, InternalTask> it : dup.entrySet()) {
            InternalTask nt = it.getValue();
            if (target.getId().equals(it.getKey())) {
                newTarget = nt;
            }
            if (initiator.getId().equals(it.getKey())) {
                newInit = nt;
            }
            nt.setJobInfo(getJobInfo());
            this.addTask(nt);
            assignReplicationTag(nt, initiator, true, action);
        }
        changesInfo.newTasksAdded(dup.values());

        // connect replicated tree
        newTarget.addDependence(initiator);
        changesInfo.taskUpdated(newTarget);

        // connect mergers
        List<InternalTask> mergers = new ArrayList<>();
        for (InternalTask t : this.tasks.values()) {
            if (t.getIDependences() != null) {

                for (InternalTask p : t.getIDependences()) {
                    if (p.getId().equals(initiator.getId())) {
                        if (!t.equals(newTarget)) {
                            mergers.add(t);
                        }
                    }
                }
            }
        }
        for (InternalTask t : mergers) {
            t.getIDependences().remove(initiator);
            t.addDependence(newInit);
            changesInfo.taskUpdated(t);
        }

        // propagate the changes in the job descriptor
        getJobDescriptor().doLoop(initiator.getId(), dup, newTarget, newInit);

        this.jobInfo.setTasksChanges(changesInfo, this);
        // notify frontend that tasks were added and modified
        frontend.jobStateUpdated(this.getOwner(),
                new NotificationData<JobInfo>(SchedulerEvent.TASK_REPLICATED, new JobInfoImpl(jobInfo)));
        this.jobInfo.clearTasksChanges();

        return true;
    }

    /**
     * Walk up <code>down</code>'s dependences until a task <code>name</code> is
     * met
     *
     * also walks weak references created by {@link FlowActionType#IF}
     *
     * @return the task names <code>name</code>, or null
     */
    public InternalTask findTaskUp(String name, InternalTask down) {
        InternalTask ret = null;
        List<InternalTask> ideps = new ArrayList<>();
        if (down.getIDependences() != null) {
            ideps.addAll(down.getIDependences());
        }
        if (down.getJoinedBranches() != null) {
            ideps.addAll(down.getJoinedBranches());
        }
        if (down.getIfBranch() != null) {
            ideps.add(down.getIfBranch());
        }
        for (InternalTask up : ideps) {
            if (up.getName().equals(name)) {
                ret = up;
            } else {
                InternalTask r = findTaskUp(name, up);
                if (r != null) {
                    ret = r;
                }
            }
        }
        return ret;
    }

    /**
     * Simulate that a task have been started and terminated. Used only by the
     * recovery method in scheduler core.
     *
     * @param id
     *            the id of the task to start and terminate.
     */
    public void recoverTask(TaskId id) {
        getJobDescriptor().recoverTask(id);
    }

    /**
     * Failed this job due to the given task failure or job has been killed
     *
     * @param taskId
     *            the task that has been the cause to failure. Can be null if
     *            the job has been killed
     * @param jobStatus
     *            type of the failure on this job. (failed/canceled/killed)
     */
    public Set<TaskId> failed(TaskId taskId, JobStatus jobStatus) {
        if (jobStatus != JobStatus.KILLED) {
            InternalTask descriptor = tasks.get(taskId);
            if (descriptor.getStartTime() > 0) {
                descriptor.setFinishedTime(System.currentTimeMillis());
                setNumberOfFinishedTasks(getNumberOfFinishedTasks() + 1);
                if (descriptor.getExecutionDuration() < 0) {
                    descriptor.setExecutionDuration(descriptor.getFinishedTime() - descriptor.getStartTime());
                }
            }
            descriptor.setStatus((jobStatus == JobStatus.FAILED) ? TaskStatus.FAILED : TaskStatus.FAULTY);
            // terminate this job descriptor
            getJobDescriptor().failed();
        }
        // set the new status of the job
        setFinishedTime(System.currentTimeMillis());
        setNumberOfPendingTasks(0);
        setNumberOfRunningTasks(0);
        setStatus(jobStatus);

        // creating list of status
        Set<TaskId> updatedTasks = new HashSet<>();

        for (InternalTask td : tasks.values()) {
            if (!td.getId().equals(taskId)) {
                if (td.getStatus() == TaskStatus.RUNNING) {
                    td.setStatus(TaskStatus.ABORTED);
                    td.setFinishedTime(System.currentTimeMillis());
                    if (td.getStartTime() > 0 && td.getExecutionDuration() < 0) {
                        td.setExecutionDuration(td.getFinishedTime() - td.getStartTime());
                    }
                    updatedTasks.add(td.getId());
                } else if (td.getStatus() == TaskStatus.WAITING_ON_ERROR ||
                    td.getStatus() == TaskStatus.WAITING_ON_FAILURE) {
                    td.setStatus(TaskStatus.NOT_RESTARTED);
                    updatedTasks.add(td.getId());
                } else if (td.getStatus() != TaskStatus.FINISHED && td.getStatus() != TaskStatus.FAILED &&
                    td.getStatus() != TaskStatus.FAULTY && td.getStatus() != TaskStatus.SKIPPED) {
                    td.setStatus(TaskStatus.NOT_STARTED);
                    updatedTasks.add(td.getId());
                }
            }
        }

        terminateTaskDataSpaceApplications();

        return updatedTasks;
    }

    /**
     * Get a task descriptor that is in the running task queue.
     *
     * @param id
     *            the id of the task descriptor to retrieve.
     * @return the task descriptor associated to this id, or null if not
     *         running.
     */
    public TaskDescriptor getRunningTaskDescriptor(TaskId id) {
        return getJobDescriptor().GetRunningTaskDescriptor(id);
    }

    /**
     * Set all properties following a job submitting.
     */
    public void submitAction() {
        setSubmittedTime(System.currentTimeMillis());
        setStatus(JobStatus.PENDING);
    }

    /**
     * Prepare tasks in order to be ready to be scheduled. The task may have a
     * consistent id and job info.
     */
    public synchronized void prepareTasks() {
        // get tasks
        ArrayList<InternalTask> sorted = getITasks();
        // sort task according to the ID
        Collections.sort(sorted);
        tasks.clear();
        // re-init taskId
        int id = 0;
        for (InternalTask td : sorted) {
            TaskId newId = TaskIdImpl.createTaskId(getId(), td.getName(), id++);
            td.setId(newId);
            td.setJobInfo(getJobInfo());
            tasks.put(newId, td);
        }
    }

    /**
     * Set all properties in order to start the job. After this method and for
     * better performances you may have to set the taskStatusModify to "null" :
     * setTaskStatusModify(null);
     */
    public void start() {
        setStartTime(System.currentTimeMillis());
        setNumberOfPendingTasks(getTotalNumberOfTasks());
        setNumberOfRunningTasks(0);
        setStatus(JobStatus.RUNNING);

        List<InternalTask> internalTasks = getITasks();
        HashMap<TaskId, TaskStatus> taskStatus = new HashMap<>(internalTasks.size());

        for (InternalTask internalTask : internalTasks) {
            internalTask.setStatus(TaskStatus.PENDING);
            taskStatus.put(internalTask.getId(), TaskStatus.PENDING);
        }
    }

    /**
     * Set all properties in order to terminate the job.
     */
    public void terminate() {
        long finishTime = System.currentTimeMillis();
        setStatus(JobStatus.FINISHED);
        setFinishedTime(finishTime);
        terminateTaskDataSpaceApplications();
    }

    private void terminateTaskDataSpaceApplications() {
        if (taskDataSpaceApplications != null) {
            for (TaskDataSpaceApplication taskDataSpaceApplication : taskDataSpaceApplications.values()) {
                taskDataSpaceApplication.terminateDataSpaceApplication();
            }
        }
    }

    /**
     * Paused every running and submitted tasks in this pending job. After this
     * method and for better performances you may have to set the
     * taskStatusModify to "null" : setTaskStatusModify(null);
     *
     * @return true if the job has correctly been paused, false if not.
     */
    public Set<TaskId> setPaused() {
        if (jobInfo.getStatus() == JobStatus.PAUSED) {
            return new HashSet<>(0);
        }

        jobInfo.setStatus(JobStatus.PAUSED);

        Collection<InternalTask> values = tasks.values();
        Set<TaskId> updatedTasks = new HashSet<>(values.size());

        for (InternalTask td : values) {
            if ((td.getStatus() != TaskStatus.FINISHED) && (td.getStatus() != TaskStatus.RUNNING) &&
                (td.getStatus() != TaskStatus.SKIPPED) && (td.getStatus() != TaskStatus.FAULTY) &&
                (td.getStatus() != TaskStatus.IN_ERROR)) {
                td.setStatus(TaskStatus.PAUSED);
                getJobDescriptor().pause(td.getId());
                updatedTasks.add(td.getId());
            }
        }
        return updatedTasks;
    }

    public void setTaskPausedOnError(InternalTask internalTask) {
        internalTask.setStatus(TaskStatus.IN_ERROR);
        getJobDescriptor().pausedTaskOnError(internalTask.getId());
    }

    /**
     * Status of every paused tasks becomes pending or submitted in this pending
     * job. After this method and for better performances you may have to set
     * the taskStatusModify to "null" : setTaskStatusModify(null);
     *
     * @return true if the job has correctly been unpaused, false if not.
     */
    public Set<TaskId> setUnPause() {
        if (jobInfo.getStatus() != JobStatus.PAUSED && jobInfo.getStatus() != JobStatus.IN_ERROR) {
            return new HashSet<>(0);
        }

        boolean jobContainsInErrorTasks = false;

        if ((getNumberOfPendingTasks() + getNumberOfRunningTasks() + getNumberOfFinishedTasks()) == 0) {
            jobInfo.setStatus(JobStatus.PENDING);
        } else if (getNumberOfRunningTasks() == 0) {
            jobInfo.setStatus(JobStatus.STALLED);
        } else {
            jobInfo.setStatus(JobStatus.RUNNING);
        }

        Set<TaskId> updatedTasks = new HashSet<>();
        for (InternalTask task : tasks.values()) {
            if (jobInfo.getStatus() == JobStatus.PENDING) {
                task.setStatus(TaskStatus.SUBMITTED);
                updatedTasks.add(task.getId());
            } else if ((jobInfo.getStatus() == JobStatus.RUNNING) ||
                (jobInfo.getStatus() == JobStatus.STALLED)) {
                if ((task.getStatus() != TaskStatus.FINISHED) && (task.getStatus() != TaskStatus.RUNNING) &&
                    (task.getStatus() != TaskStatus.SKIPPED) && (task.getStatus() != TaskStatus.FAULTY) &&
                    (task.getStatus() != TaskStatus.IN_ERROR)) {
                    task.setStatus(TaskStatus.PENDING);
                    updatedTasks.add(task.getId());
                }
            }

            if (task.getStatus() == TaskStatus.IN_ERROR) {
                jobContainsInErrorTasks = true;
            } else {
                getJobDescriptor().unpause(task.getId());
            }
        }

        if (jobContainsInErrorTasks) {
            jobInfo.setStatus(JobStatus.IN_ERROR);
        }

        return updatedTasks;
    }

    public void restartInErrorTask(InternalTask internalTask) {
        if (internalTask.getStatus() == TaskStatus.IN_ERROR) {
            newWaitingTask();
            setNumberOfInErrorTasks(getNumberOfInErrorTasks() - 1);
            internalTask.setStatus(TaskStatus.PENDING);
            getJobDescriptor().unpause(internalTask.getId());
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#setPriority(org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    @Override
    public void setPriority(JobPriority priority) {
        jobInfo.setPriority(priority);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobState#getTasks()
     */
    @Override
    public ArrayList<TaskState> getTasks() {
        return new ArrayList<TaskState>(tasks.values());
    }

    public void setTasks(Collection<InternalTask> tasksList) {
        tasks = new HashMap<>(tasksList.size());
        for (InternalTask task : tasksList) {
            tasks.put(task.getId(), task);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobState#getHMTasks()
     */
    @Override
    public Map<TaskId, TaskState> getHMTasks() {
        Set<Entry<TaskId, InternalTask>> entries = tasks.entrySet();
        Map<TaskId, TaskState> tmp = new HashMap<>(entries.size());
        for (Entry<TaskId, InternalTask> e : entries) {
            tmp.put(e.getKey(), e.getValue());
        }
        return tmp;
    }

    /**
     * To get the tasks as an array list.
     *
     * @return the tasks
     */
    public ArrayList<InternalTask> getITasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * To get the tasks as a hash map.
     *
     * @return the tasks
     */
    public Map<TaskId, InternalTask> getIHMTasks() {
        return tasks;
    }

    /**
     * To set the id
     *
     * @param id
     *            the id to set
     */
    public void setId(JobId id) {
        jobInfo.setJobId(id);
    }

    /**
     * To set the finishedTime
     *
     * @param finishedTime
     *            the finishedTime to set
     */
    public void setFinishedTime(long finishedTime) {
        jobInfo.setFinishedTime(finishedTime);
    }

    /**
     * To set the startTime
     *
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(long startTime) {
        jobInfo.setStartTime(startTime);
    }

    /**
     * To set the inErrorTime
     *
     * @param inErrorTime
     *            the inErrorTime to set
     */
    public void setInErrorTime(long inErrorTime) {
        jobInfo.setInErrorTime(inErrorTime);
    }

    /**
     * To set the submittedTime
     *
     * @param submittedTime
     *            the submittedTime to set
     */
    public void setSubmittedTime(long submittedTime) {
        jobInfo.setSubmittedTime(submittedTime);
    }

    /**
     * To set the removedTime
     *
     * @param removedTime
     *            the removedTime to set
     */
    public void setRemovedTime(long removedTime) {
        jobInfo.setRemovedTime(removedTime);
    }

    /**
     * To set the numberOfFinishedTasks
     *
     * @param numberOfFinishedTasks
     *            the numberOfFinishedTasks to set
     */
    public void setNumberOfFinishedTasks(int numberOfFinishedTasks) {
        jobInfo.setNumberOfFinishedTasks(numberOfFinishedTasks);
    }

    /**
     * To set the numberOfPendingTasks
     *
     * @param numberOfPendingTasks
     *            the numberOfPendingTasks to set
     */
    public void setNumberOfPendingTasks(int numberOfPendingTasks) {
        jobInfo.setNumberOfPendingTasks(numberOfPendingTasks);
    }

    /**
     * To set the numberOfRunningTasks
     *
     * @param numberOfRunningTasks
     *            the numberOfRunningTasks to set
     */
    public void setNumberOfRunningTasks(int numberOfRunningTasks) {
        jobInfo.setNumberOfRunningTasks(numberOfRunningTasks);
    }

    public void incrementNumberOfFailedTasksBy(int increment) {
        jobInfo.setNumberOfFailedTasks(getNumberOfFailedTasks() + increment);
    }

    /**
     * To set the numberOfFailedTasks
     *
     * @param numberOfFailedTasks
     *            the numberOfFailedTasks to set
     */
    public void setNumberOfFailedTasks(int numberOfFailedTasks) {
        jobInfo.setNumberOfFailedTasks(numberOfFailedTasks);
    }

    public boolean saveFaultyTaskId(TaskId taskId) {
        return faultyTasks.add(taskId);
    }

    public void increaseNumberOfFaultyTasks(TaskId taskId) {
        if (faultyTasks.add(taskId)) {
            incrementNumberOfFaultyTasksBy(1);
        }
    }

    public void decreaseNumberOfFaultyTasks(TaskId taskId) {
        if (faultyTasks.remove(taskId)) {
            incrementNumberOfFaultyTasksBy(-1);
        }
    }

    public void incrementNumberOfFaultyTasksBy(int increment) {
        jobInfo.setNumberOfFaultyTasks(getNumberOfFaultyTasks() + increment);
    }

    /**
     * To set the numberOfFaultyTasks
     *
     * @param numberOfFaultyTasks
     *            the numberOfFaultyTasks to set
     */
    public void setNumberOfFaultyTasks(int numberOfFaultyTasks) {
        jobInfo.setNumberOfFaultyTasks(numberOfFaultyTasks);
    }

    public void incrementNumberOfInErrorTasksBy(int increment) {
        jobInfo.setNumberOfInErrorTasks(getNumberOfInErrorTasks() + increment);
    }

    /**
     * To set the numberOfInErrorTasks
     *
     * @param numberOfInErrorTasks
     *            the numberOfInErrorTasks to set
     */
    public void setNumberOfInErrorTasks(int numberOfInErrorTasks) {
        jobInfo.setNumberOfInErrorTasks(numberOfInErrorTasks);
    }

    /**
     * To get the jobDescriptor
     *
     * @return the jobDescriptor
     */
    @XmlTransient
    public JobDescriptorImpl getJobDescriptor() {
        if (jobDescriptor == null) {
            jobDescriptor = new JobDescriptorImpl(this);
        }
        return (JobDescriptorImpl) jobDescriptor;
    }

    /**
     * Set the job Descriptor
     *
     * @param jobD
     *            the JobDescriptor to set.
     */
    public void setJobDescriptor(JobDescriptor jobD) {
        this.jobDescriptor = jobD;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(JobStatus status) {
        jobInfo.setStatus(status);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobState#getOwner()
     */
    @Override
    public String getOwner() {
        return jobInfo.getJobOwner();
    }

    /**
     * To set the owner of this job.
     *
     * @param owner
     *            the owner to set.
     */
    public void setOwner(String owner) {
        this.jobInfo.setJobOwner(owner);
    }

    /**
     * Get the credentials for this job
     *
     * @return the credentials for this job
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Set the credentials value to the given credentials value
     *
     * @param credentials
     *            the credentials to set
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Get the next restart waiting time in millis.
     *
     * @return the next restart waiting time in millis.
     */
    public long getNextWaitingTime(int executionNumber) {
        if (executionNumber <= 0) {
            // execution number is 0 or less, restart with the minimal amount of
            // time
            return restartWaitingTimer;
        } else if (executionNumber > 10) {
            // execution timer exceed 10, restart after 60 seconds
            return 60000;
        } else {
            // else restart according to this function
            return (getNextWaitingTime(executionNumber - 1) + executionNumber * 1000);
        }
    }

    /**
     * Set this job to the state toBeRemoved.
     */
    public void setToBeRemoved() {
        jobInfo.setToBeRemoved();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof InternalJob) {
            return getId().equals(((InternalJob) o).getId());
        }

        return false;
    }

    /**
     * Get the jobDataSpaceApplication
     *
     * @return the jobDataSpaceApplication
     */
    public Map<Long, TaskDataSpaceApplication> getTaskDataSpaceApplications() {
        return taskDataSpaceApplications;
    }

    /**
     * Return the internal task associated with the given task name for this
     * job.
     *
     * @param taskName
     *            the task name to find
     * @return the internal task associated with the given name.
     * @throws UnknownTaskException
     *             if the given taskName does not exist.
     */
    public InternalTask getTask(String taskName) throws UnknownTaskException {
        for (InternalTask task : tasks.values()) {
            if (task.getId().getReadableName().equals(taskName)) {
                return task;
            }
        }
        throw new UnknownTaskException("'" + taskName + "' does not exist in this job.");
    }

    public InternalTask getTask(TaskId taskId) throws UnknownTaskException {
        InternalTask task = tasks.get(taskId);
        if (task != null) {
            return task;
        } else {
            throw new UnknownTaskException("'" + taskId + "' does not exist in this job.");
        }
    }

    /**
     *
     * Return generic info replacing $PA_JOB_NAME, $PA_JOB_ID, $PA_TASK_NAME,
     * $PA_TASK_ID, $PA_TASK_ITERATION $PA_TASK_REPLICATION by it's actual value
     *
     */
    public Map<String, String> getGenericInformation() {
        if (genericInformations == null) {
            // task is not yet properly initialized
            return new HashMap<>(0);
        }

        Map<String, String> replacements = new HashMap<>();
        JobId jobId = jobInfo.getJobId();
        if (jobId != null) {
            replacements.put(SchedulerVars.PA_JOB_ID.toString(), jobId.toString());
            replacements.put(SchedulerVars.PA_JOB_NAME.toString(), jobId.getReadableName());
        }
        return applyReplacementsOnGenericInformation(replacements);
    }

    /**
     *
     * Gets the task generic information.
     * 
     * @param replaceVariables
     *            - if set to true method replaces variables in the generic
     *            information
     *
     */
    public Map<String, String> getGenericInformations(boolean replaceVariables) {
        if (replaceVariables) {
            return this.getGenericInformation();
        } else {
            return super.getGenericInformation();
        }
    }

    @Override
    public String display() {
        String nl = System.lineSeparator();
        String answer = super.display();
        return answer + nl + "\tTasks = " + displayAllTasks();
    }

    private String displayAllTasks() {
        String nl = System.lineSeparator();
        String answer = "{" + nl;
        for (TaskId tid : tasks.keySet()) {
            answer += tasks.get(tid).display() + nl + nl;
        }
        answer += "}";
        return answer;
    }

}