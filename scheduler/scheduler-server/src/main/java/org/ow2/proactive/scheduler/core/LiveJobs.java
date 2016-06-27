package org.ow2.proactive.scheduler.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.exception.TaskPreemptedException;
import org.ow2.proactive.scheduler.common.exception.TaskRestartedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.scheduler.util.policy.ISO8601DateUtil;
import org.ow2.proactive.utils.TaskIdWrapper;


class LiveJobs {

    private static final Logger logger = Logger.getLogger(SchedulingService.class);

    private static final JobLogger jlogger = JobLogger.getInstance();

    private static final TaskLogger tlogger = TaskLogger.getInstance();

    private static class JobData {

        final InternalJob job;

        final ReentrantLock jobLock = new ReentrantLock();

        private JobData(InternalJob job) {
            this.job = job;
        }

        void unlock() {
            jobLock.unlock();
        }
    }

    private final SchedulerDBManager dbManager;

    private final SchedulerStateUpdate listener;

    private final Map<JobId, JobData> jobs = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<TaskIdWrapper, RunningTaskData> runningTasksData = new ConcurrentHashMap<>();

    private final OnErrorPolicyInterpreter onErrorPolicyInterpreter = new OnErrorPolicyInterpreter();

    LiveJobs(SchedulerDBManager dbManager, SchedulerStateUpdate listener) {
        this.dbManager = dbManager;
        this.listener = listener;
    }

    Collection<RunningTaskData> getRunningTasks() {
        return runningTasksData.values();
    }

    boolean canPingTask(RunningTaskData taskData) {
        return runningTasksData.get(TaskIdWrapper.wrap(taskData.getTask().getId())) == taskData;
    }

    void jobRecovered(InternalJob job) {
        jobs.put(job.getId(), new JobData(job));
    }

    void unpauseAll() {
        for (JobId jobId : jobs.keySet()) {
            JobData jobData = lockJob(jobId);
            if (jobData != null) {
                try {
                    InternalJob job = jobData.job;
                    if (job.getStatus() == JobStatus.PAUSED) {
                        job.setUnPause();
                        dbManager.updateJobAndTasksState(job);
                        updateJobInSchedulerState(job, SchedulerEvent.JOB_RESUMED);
                    }
                } finally {
                    jobData.unlock();
                }
            }
        }
    }

    List<RunningTaskData> getRunningTasks(JobId jobId) {
        List<RunningTaskData> result = new ArrayList<>();
        for (RunningTaskData taskData : runningTasksData.values()) {
            if (taskData.getTask().getJobId().equals(jobId)) {
                result.add(taskData);
            }
        }
        return result;
    }

    RunningTaskData getRunningTask(TaskId taskId) {
        for (RunningTaskData taskData : runningTasksData.values()) {
            if (taskData.getTask().getId().equals(taskId)) {
                return taskData;
            }
        }
        return null;
    }

    boolean hasJobOwnedByUser(String user) {
        for (JobData jobData : jobs.values()) {
            if (jobData.job.getOwner().equals(user)) {
                return true;
            }
        }
        return false;
    }

    void changeJobPriority(JobId jobId, JobPriority priority) {
        JobData jobData = lockJob(jobId);
        if (jobData == null) {
            return;
        }
        try {
            jobData.job.setPriority(priority);

            dbManager.changeJobPriority(jobId, priority);

            listener.jobStateUpdated(jobData.job.getOwner(), new NotificationData<JobInfo>(
                SchedulerEvent.JOB_CHANGE_PRIORITY, new JobInfoImpl((JobInfoImpl) jobData.job.getJobInfo())));
        } finally {
            jobData.unlock();
        }
    }

    public Boolean restartAllInErrorTasks(JobId jobId) {
        JobData jobData = lockJob(jobId);
        if (jobData == null) {
            return false;
        }
        try {
            InternalJob job = jobData.job;
            for (TaskState taskState : job.getTasks()) {
                try {
                    restartInErrorTask(jobId, taskState.getName());
                } catch (UnknownTaskException e) {
                    logger.error("", e);
                    jlogger.error(jobId, "", e);
                    tlogger.error(taskState.getId(), "", e);
                }
            }

            setJobStatusToInErrorIfNotPaused(job);

            dbManager.updateJobAndTasksState(job);
            updateJobInSchedulerState(job, SchedulerEvent.JOB_RESTARTED_FROM_ERROR);

            return Boolean.TRUE;
        } finally {
            jobData.unlock();
        }
    }

    boolean resumeJob(JobId jobId) {
        JobData jobData = lockJob(jobId);
        if (jobData == null) {
            return false;
        }
        try {
            InternalJob job = jobData.job;
            Set<TaskId> updatedTasks = job.setUnPause();

            if (updatedTasks.size() > 0) {
                jlogger.debug(jobId, "has just been resumed !");
                dbManager.updateJobAndTasksState(job);
                updateTasksInSchedulerState(job, updatedTasks);
            }

            //update tasks events list and send it to front-end
            updateJobInSchedulerState(job, SchedulerEvent.JOB_RESUMED);

            return updatedTasks.size() > 0;
        } finally {
            jobData.unlock();
        }
    }

    boolean pauseJob(JobId jobId) {
        JobData jobData = lockJob(jobId);
        if (jobData == null) {
            return false;
        }
        try {
            InternalJob job = jobData.job;

            Set<TaskId> updatedTasks = job.setPaused();

            if (updatedTasks.size() > 0) {
                jlogger.debug(jobId, "has just been paused !");
                dbManager.updateJobAndTasksState(job);
                updateTasksInSchedulerState(job, updatedTasks);
            }

            //update tasks events list and send it to front-end
            updateJobInSchedulerState(job, SchedulerEvent.JOB_PAUSED);

            return updatedTasks.size() > 0;
        } finally {
            jobData.unlock();
        }
    }

    boolean updateStartAt(JobId jobId, String startAt) {
        JobData jobData = lockJob(jobId);
        if (jobData == null) {
            return false;
        }
        try {
            InternalJob job = jobData.job;

            long scheduledTime = ISO8601DateUtil.toDate(startAt).getTime();

            Set<TaskId> updatedTasks = job.updateStartAtAndTasksScheduledTime(startAt, scheduledTime);

            if (updatedTasks.size() > 0) {
                jlogger.debug(jobId, " start at has just been set to :" + startAt);
                dbManager.updateTaskSchedulingTime(job, scheduledTime);
            }

            updateJobInSchedulerState(job, SchedulerEvent.JOB_RESUMED);

            return updatedTasks.size() > 0;
        } finally {
            jobData.unlock();
        }
    }

    void jobSubmitted(InternalJob job) {
        job.prepareTasks();
        job.submitAction();
        dbManager.newJobSubmitted(job);
        ClientJobState clientJobState = new ClientJobState(job);
        jobs.put(job.getId(), new JobData(job));
        listener.jobSubmitted(clientJobState);
    }

    Map<JobId, JobDescriptor> lockJobsToSchedule() {

        TreeSet<JobPriority> prioritiesScheduled = new TreeSet<>();
        TreeSet<JobPriority> prioritiesNotScheduled = new TreeSet<>();

        Map<JobId, JobDescriptor> result = new HashMap<>();
        for (Map.Entry<JobId, JobData> entry : jobs.entrySet()) {
            JobData value = entry.getValue();

            if (value.jobLock.tryLock()) {
                InternalJob job = entry.getValue().job;
                result.put(job.getId(), job.getJobDescriptor());
                prioritiesScheduled.add(job.getPriority());

                if (unlockIfConflict(prioritiesScheduled, prioritiesNotScheduled, result))
                    return new HashMap<>(0);
            } else {
                prioritiesNotScheduled.add(value.job.getPriority());
                if (unlockIfConflict(prioritiesScheduled, prioritiesNotScheduled, result))
                    return new HashMap<>(0);
            }
        }
        return result;
    }

    private boolean unlockIfConflict(TreeSet<JobPriority> prioritiesScheduled,
            TreeSet<JobPriority> prioritiesNotScheduled, Map<JobId, JobDescriptor> result) {
        if (priorityConflict(prioritiesScheduled, prioritiesNotScheduled)) {
            unlockJobsToSchedule(result.values());
            return true;
        }
        return false;
    }

    /**
     * This method checks if there is a conflict priority between the jobs selected to be scheduled and those not selected.
     * There is a conflict if any job scheduled has a strictly lower priority than any unscheduled job
     *
     * @param prioritiesScheduled
     * @param prioritiesNotScheduled
     * @return
     */
    public boolean priorityConflict(TreeSet<JobPriority> prioritiesScheduled,
            TreeSet<JobPriority> prioritiesNotScheduled) {

        for (JobPriority jp : prioritiesNotScheduled) {
            if (!prioritiesScheduled.headSet(jp).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    void unlockJobsToSchedule(Collection<JobDescriptor> jobDescriptors) {
        for (JobDescriptor desc : jobDescriptors) {
            JobData jobData = checkJobAccess(desc.getJobId());
            jobData.unlock();
        }
    }

    void restartWaitingTask(TaskId taskId) {
        JobData jobData = lockJob(taskId.getJobId());
        if (jobData == null) {
            return;
        }
        try {
            InternalTask task = jobData.job.getTask(taskId);
            if (!task.getStatus().isTaskAlive()) {
                tlogger.info(taskId, "task to be restarted isn't alive " + task.getStatus());
                return;
            }
            jobData.job.reStartTask(task);
        } catch (UnknownTaskException e) {
            logger.error("Unexpected exception", e);
        } finally {
            jobData.unlock();
        }
    }

    private void restartTaskOnNodeFailure(InternalTask task, JobData jobData,
            TerminationData terminationData) {
        final String errorMsg = "An error has occurred due to a node failure and the maximum amount of retries property has been reached.";

        task.setProgress(0);
        task.decreaseNumberOfExecutionOnFailureLeft();
        tlogger.info(task.getId(),
                "number of retry on failure left " + task.getNumberOfExecutionOnFailureLeft());

        InternalJob job = jobData.job;

        if (task.getNumberOfExecutionOnFailureLeft() > 0) {
            task.setStatus(TaskStatus.WAITING_ON_FAILURE);

            job.newWaitingTask();
            listener.taskStateUpdated(job.getOwner(),
                    new NotificationData<TaskInfo>(SchedulerEvent.TASK_WAITING_FOR_RESTART,
                        new TaskInfoImpl((TaskInfoImpl) task.getTaskInfo())));
            job.reStartTask(task);
            dbManager.taskRestarted(job, task, null);
            tlogger.info(task.getId(), " is waiting for restart");
        } else {
            job.incrementNumberOfFailedTasksBy(1);
            endJob(jobData, terminationData, task, null, errorMsg, JobStatus.FAILED);
        }
    }

    TerminationData restartTaskOnNodeFailure(InternalTask task) {
        JobData jobData = lockJob(task.getJobId());
        if (jobData == null) {
            return emptyResult(task.getId());
        }
        try {
            TaskId taskId = task.getId();

            if (task.getStatus() != TaskStatus.RUNNING) {
                return emptyResult(taskId);
            }

            RunningTaskData taskData = runningTasksData.remove(TaskIdWrapper.wrap(taskId));
            if (taskData == null) {
                throw new IllegalStateException("No information for: " + taskId);
            }

            TerminationData result = TerminationData.newTerminationData();
            result.addTaskData(jobData.job, taskData, false, null);

            restartTaskOnNodeFailure(task, jobData, result);

            return result;
        } finally {
            jobData.unlock();
        }
    }

    private void restartTaskOnError(JobData jobData, InternalTask task, TaskStatus status,
            TaskResultImpl result, long waitTime, TerminationData terminationData) {
        InternalJob job = jobData.job;

        logger.info("Node Exclusion : restart mode is '" + task.getRestartTaskOnError() + "'");
        if (task.getRestartTaskOnError().equals(RestartMode.ELSEWHERE)) {
            task.setNodeExclusion(task.getExecuterInformation().getNodes());
        }
        task.setStatus(status);
        job.newWaitingTask();
        dbManager.updateAfterTaskFinished(job, task, result);
        listener.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
            SchedulerEvent.TASK_WAITING_FOR_RESTART, new TaskInfoImpl((TaskInfoImpl) task.getTaskInfo())));

        terminationData.addRestartData(task.getId(), waitTime);

        logger.info("END restartTaskOnError");
    }

    TerminationData simulateJobStart(List<EligibleTaskDescriptor> tasksToSchedule, String errorMsg) {
        TerminationData terminationData = TerminationData.newTerminationData();
        for (EligibleTaskDescriptor eltd : tasksToSchedule) {
            JobId jobId = eltd.getJobId();
            if (!terminationData.jobTerminated(jobId)) {
                JobData jobData = lockJob(jobId);
                if (jobData != null) {
                    try {
                        if (jobData.job.getStartTime() < 0) {
                            jobData.job.start();
                            updateJobInSchedulerState(jobData.job, SchedulerEvent.JOB_PENDING_TO_RUNNING);
                            jlogger.info(jobId, "started");
                        }
                        endJob(jobData, terminationData, eltd.getInternal(), null, errorMsg,
                                JobStatus.CANCELED);
                    } finally {
                        jobData.unlock();
                    }
                }
            }
        }
        return terminationData;
    }

    void taskStarted(InternalJob job, InternalTask task, TaskLauncher launcher) {
        checkJobAccess(job.getId());
        if (runningTasksData.containsKey(TaskIdWrapper.wrap(task.getId()))) {
            throw new IllegalStateException("Task is already started");
        }

        runningTasksData.put(TaskIdWrapper.wrap(task.getId()),
                new RunningTaskData(task, job.getOwner(), job.getCredentials(), launcher));

        boolean firstTaskStarted;

        if (job.getStartTime() < 0) {
            // if it is the first task of this job
            job.start();
            updateJobInSchedulerState(job, SchedulerEvent.JOB_PENDING_TO_RUNNING);
            jlogger.info(job.getId(), "started");
            firstTaskStarted = true;
        } else {
            firstTaskStarted = false;
        }

        // set the different informations on task
        job.startTask(task);
        dbManager.jobTaskStarted(job, task, firstTaskStarted);

        listener.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
            SchedulerEvent.TASK_PENDING_TO_RUNNING, new TaskInfoImpl((TaskInfoImpl) task.getTaskInfo())));

        //fill previous task progress with 0, means task has started
        task.setProgress(0);
    }

    private TerminationData emptyResult(TaskId taskId) {
        RunningTaskData taskData = runningTasksData.remove(TaskIdWrapper.wrap(taskId));
        if (taskData != null) {
            throw new IllegalStateException("Task is marked as running: " + taskId);
        }
        return TerminationData.EMPTY;
    }

    private TerminationData emptyData(JobId jobId) {
        for (TaskIdWrapper taskId : runningTasksData.keySet()) {
            if (taskId.getTaskId().getJobId().equals(jobId)) {
                throw new IllegalStateException("Unexpected task data: " + taskId);
            }
        }
        return TerminationData.EMPTY;
    }

    public TerminationData taskTerminatedWithResult(TaskId taskId, TaskResultImpl result) {
        JobData jobData = lockJob(taskId.getJobId());
        if (jobData == null) {
            return emptyResult(taskId);
        }
        try {
            InternalTask task;
            try {
                task = jobData.job.getTask(taskId);
            } catch (UnknownTaskException e) {
                logger.error("Unexpected exception", e);
                return emptyResult(taskId);
            }
            if (task.getStatus() != TaskStatus.RUNNING) {
                tlogger.info(taskId, "task isn't running anymore");
                return emptyResult(taskId);
            }

            TaskIdWrapper taskIdWrapper = TaskIdWrapper.wrap(taskId);
            RunningTaskData taskData = runningTasksData.remove(taskIdWrapper);
            if (taskData == null) {
                throw new IllegalStateException("No information for: " + taskId);
            }

            TerminationData terminationData = createAndFillTerminationData(result, taskData, jobData.job,
                    true);

            boolean errorOccurred = result.hadException();
            if (errorOccurred) {
                tlogger.error(taskId, "error", result.getException());
            }

            tlogger.info(taskId, "finished with" + (errorOccurred ? "" : "out") + " errors");

            if (errorOccurred) {
                logger.info("Task has terminated with an error ");
                task.decreaseNumberOfExecutionLeft();

                boolean requiresPauseJobOnError = onErrorPolicyInterpreter.requiresPauseJobOnError(task);

                int numberOfExecutionLeft = task.getNumberOfExecutionLeft();

                if (numberOfExecutionLeft <= 0 && onErrorPolicyInterpreter.requiresCancelJobOnError(task)) {
                    logger.info("No retry left and task is tagged with cancel job on error");

                    jobData.job.increaseNumberOfFaultyTasks(taskId);
                    endJob(jobData, terminationData, task, result,
                            "An error occurred in your task and the maximum number of executions has been reached. " +
                                "You also ask to cancel the job in such a situation!",
                            JobStatus.CANCELED);

                    logger.info("Job has been canceled");

                    return terminationData;
                } else if (numberOfExecutionLeft > 0) {
                    logger.info("Number of execution left is " + numberOfExecutionLeft);

                    if (onErrorPolicyInterpreter.requiresPauseTaskOnError(task)) {
                        suspendTaskOnError(jobData, task, result.getTaskDuration());

                        logger.info("Task is paused on error");

                        return terminationData;
                    } else if (requiresPauseJobOnError) {
                        suspendTaskOnError(jobData, task, result.getTaskDuration());
                        pauseJob(task.getJobId());

                        logger.info("Job is paused on error");

                        return terminationData;
                    } else {
                        jobData.job.increaseNumberOfFaultyTasks(taskId);

                        long waitTime = jobData.job
                                .getNextWaitingTime(task.getMaxNumberOfExecution() - numberOfExecutionLeft);
                        restartTaskOnError(jobData, task, TaskStatus.WAITING_ON_ERROR, result, waitTime,
                                terminationData);

                        logger.info("New restart is scheduled");

                        return terminationData;
                    }
                } else if (numberOfExecutionLeft <= 0) {
                    jobData.job.increaseNumberOfFaultyTasks(taskId);

                    if (requiresPauseJobOnError) {
                        pauseJob(task.getJobId());
                    }
                }
            }

            terminateTask(jobData, task, errorOccurred, result, terminationData);

            return terminationData;
        } finally {
            jobData.unlock();
        }
    }

    @NotNull
    private TerminationData createAndFillTerminationData(TaskResultImpl result, RunningTaskData taskData,
            InternalJob job, boolean normalTermination) {
        TerminationData terminationData = TerminationData.newTerminationData();
        terminationData.addTaskData(job, taskData, normalTermination, result);
        return terminationData;
    }

    private void suspendTaskOnError(JobData jobData, InternalTask task, long taskDuration) {
        InternalJob job = jobData.job;
        job.setInErrorTime(System.currentTimeMillis());
        job.setTaskPausedOnError(task);
        setJobStatusToInErrorIfNotPaused(job);
        job.incrementNumberOfInErrorTasksBy(1);

        task.setInErrorTime(task.getStartTime() + taskDuration);

        dbManager.updateJobAndTasksState(job);

        updateTaskPausedOnerrorState(job, task.getId());
        updateJobInSchedulerState(job, SchedulerEvent.JOB_IN_ERROR);
    }

    private void setJobStatusToInErrorIfNotPaused(InternalJob job) {
        if (!job.getStatus().equals(JobStatus.PAUSED)) {
            job.setStatus(JobStatus.IN_ERROR);
        }
    }

    void restartInErrorTask(JobId jobId, String taskName) throws UnknownTaskException {
        JobData jobData = lockJob(jobId);
        try {
            jobData.job.restartInErrorTask(jobData.job.getTask(taskName));
            dbManager.updateJobAndTasksState(jobData.job);
            updateJobInSchedulerState(jobData.job, SchedulerEvent.JOB_RESTARTED_FROM_ERROR);
        } finally {
            jobData.unlock();
        }
    }

    TerminationData restartTask(JobId jobId, String taskName, int restartDelay)
            throws UnknownJobException, UnknownTaskException {
        JobData jobData = lockJob(jobId);
        if (jobData == null) {
            throw new UnknownJobException(jobId);
        }
        try {
            InternalTask task = jobData.job.getTask(taskName);
            if (!task.getStatus().isTaskAlive()) {
                tlogger.info(task.getId(), "task isn't alive: " + task.getStatus());
                return emptyResult(task.getId());
            }

            TaskIdWrapper taskIdWrapper = TaskIdWrapper.wrap(task.getId());
            RunningTaskData taskData = runningTasksData.remove(taskIdWrapper);
            if (taskData == null) {
                throw new IllegalStateException("No information for: " + task.getId());
            }

            TaskResultImpl taskResult = new TaskResultImpl(task.getId(),
                new TaskRestartedException("Aborted by user"), new SimpleTaskLogs("", "Aborted by user"),
                System.currentTimeMillis() - task.getStartTime());
            TerminationData terminationData = createAndFillTerminationData(taskResult, taskData, jobData.job,
                    false);

            task.decreaseNumberOfExecutionLeft();

            if (task.getNumberOfExecutionLeft() <= 0 &&
                onErrorPolicyInterpreter.requiresCancelJobOnError(task)) {
                endJob(jobData, terminationData, task, taskResult,
                        "An error occurred in your task and the maximum number of executions has been reached. " +
                            "You also ask to cancel the job in such a situation !",
                        JobStatus.CANCELED);
                return terminationData;
            } else if (task.getNumberOfExecutionLeft() > 0) {
                long waitTime = restartDelay * 1000l;
                restartTaskOnError(jobData, task, TaskStatus.WAITING_ON_ERROR, taskResult, waitTime,
                        terminationData);
                return terminationData;
            }

            terminateTask(jobData, task, true, taskResult, terminationData);

            return terminationData;
        } finally {
            jobData.unlock();
        }
    }

    TerminationData preemptTask(JobId jobId, String taskName, int restartDelay)
            throws UnknownJobException, UnknownTaskException {
        JobData jobData = lockJob(jobId);
        if (jobData == null) {
            throw new UnknownJobException(jobId);
        }
        try {
            InternalTask task = jobData.job.getTask(taskName);
            if (!task.getStatus().isTaskAlive()) {
                tlogger.info(task.getId(), "task isn't alive: " + task.getStatus());
                return emptyResult(task.getId());
            }
            RunningTaskData taskData = runningTasksData.remove(TaskIdWrapper.wrap(task.getId()));
            if (taskData == null) {
                throw new IllegalStateException("No information for: " + task.getId());
            }
            TaskResultImpl taskResult = new TaskResultImpl(task.getId(),
                new TaskPreemptedException("Preempted by admin"),
                new SimpleTaskLogs("", "Preempted by admin"),
                System.currentTimeMillis() - task.getStartTime());

            TerminationData terminationData = createAndFillTerminationData(taskResult, taskData, jobData.job,
                    false);

            long waitTime = restartDelay * 1000L;
            restartTaskOnError(jobData, task, TaskStatus.PENDING, taskResult, waitTime, terminationData);

            return terminationData;
        } finally {
            jobData.unlock();
        }
    }

    TerminationData killTask(JobId jobId, String taskName) throws UnknownJobException, UnknownTaskException {
        JobData jobData = lockJob(jobId);
        if (jobData == null) {
            throw new UnknownJobException(jobId);
        }
        try {
            InternalTask task = jobData.job.getTask(taskName);
            if (!task.getStatus().isTaskAlive()) {
                tlogger.info(task.getId(), "task isn't alive: " + task.getStatus());
                return emptyResult(task.getId());
            }
            RunningTaskData taskData = runningTasksData.remove(TaskIdWrapper.wrap(task.getId()));
            if (taskData == null) {
                throw new IllegalStateException("No information for: " + task.getId());
            }
            TaskResultImpl taskResult = new TaskResultImpl(task.getId(),
                new TaskAbortedException("Aborted by user"), new SimpleTaskLogs("", "Aborted by user"),
                System.currentTimeMillis() - task.getStartTime());

            TerminationData terminationData = createAndFillTerminationData(taskResult, taskData, jobData.job,
                    false);

            if (onErrorPolicyInterpreter.requiresCancelJobOnError(task)) {
                endJob(jobData, terminationData, task, taskResult, "The task has been manually killed. " +
                    "You also ask to cancel the job in such a situation!", JobStatus.CANCELED);
            } else {
                terminateTask(jobData, task, true, taskResult, terminationData);
            }

            return terminationData;
        } finally {
            jobData.unlock();
        }
    }

    private void terminateTask(JobData jobData, InternalTask task, boolean errorOccurred,
            TaskResultImpl result, TerminationData terminationData) {
        InternalJob job = jobData.job;
        TaskId taskId = task.getId();

        tlogger.debug(taskId, "result added to job " + job.getId());
        //to be done before terminating the task, once terminated it is not running anymore..
        job.getRunningTaskDescriptor(taskId);
        ChangedTasksInfo changesInfo = job.terminateTask(errorOccurred, taskId, listener, result.getAction(),
                result);

        boolean jobFinished = job.isFinished();

        //update job info if it is terminated
        if (jobFinished) {
            //terminating job
            job.terminate();
            jlogger.debug(job.getId(), "terminated");
            jobs.remove(job.getId());
            terminationData.addJobToTerminate(job.getId());
        }

        //Update database
        if (result.getAction() != null) {
            dbManager.updateAfterWorkflowTaskFinished(job, changesInfo, result);
        } else {
            dbManager.updateAfterTaskFinished(job, task, result);
        }

        //send event
        listener.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
            SchedulerEvent.TASK_RUNNING_TO_FINISHED, new TaskInfoImpl((TaskInfoImpl) task.getTaskInfo())));
        //if this job is finished (every task have finished)
        jlogger.info(job.getId(), "finished tasks " + job.getNumberOfFinishedTasks() + ", total tasks " +
            job.getTotalNumberOfTasks() + ", finished " + jobFinished);
        if (jobFinished) {
            //send event to client
            listener.jobStateUpdated(job.getOwner(), new NotificationData<JobInfo>(
                SchedulerEvent.JOB_RUNNING_TO_FINISHED, new JobInfoImpl((JobInfoImpl) job.getJobInfo())));
        }
    }

    TerminationData killJob(JobId jobId) {
        JobData jobData = lockJob(jobId);
        if (jobData == null) {
            return emptyData(jobId);
        }
        try {
            TerminationData terminationData = TerminationData.newTerminationData();
            endJob(jobData, terminationData, null, null, "", JobStatus.KILLED);
            return terminationData;
        } finally {
            jobData.unlock();
        }
    }

    private void endJob(JobData jobData, TerminationData terminationData, InternalTask task,
            TaskResultImpl taskResult, String errorMsg, JobStatus jobStatus) {
        JobId jobId = jobData.job.getId();

        jobs.remove(jobId);
        terminationData.addJobToTerminate(jobId);

        InternalJob job = jobData.job;

        SchedulerEvent event;
        if (job.getStatus() == JobStatus.PENDING) {
            event = SchedulerEvent.JOB_PENDING_TO_FINISHED;
        } else {
            event = SchedulerEvent.JOB_RUNNING_TO_FINISHED;
        }

        if (task != null) {
            jlogger.info(job.getId(), "ending request caused by task " + task.getId());
        } else {
            jlogger.info(job.getId(), "ending request");
        }

        for (Iterator<RunningTaskData> i = runningTasksData.values().iterator(); i.hasNext();) {
            RunningTaskData taskData = i.next();
            if (taskData.getTask().getJobId().equals(jobId)) {
                i.remove();
                //remove previous read progress
                taskData.getTask().setProgress(0);
                terminationData.addTaskData(job, taskData, false, taskResult);
            }
        }

        //if job has been killed
        if (jobStatus == JobStatus.KILLED) {
            Set<TaskId> tasksToUpdate = job.failed(null, jobStatus);
            dbManager.updateAfterJobKilled(job, tasksToUpdate);
            updateTasksInSchedulerState(job, tasksToUpdate);

        } else {
            //if not killed
            Set<TaskId> tasksToUpdate = job.failed(task.getId(), jobStatus);

            //store the exception into jobResult / To prevent from empty task result (when job canceled), create one
            boolean noResult = (jobStatus == JobStatus.CANCELED && taskResult == null);
            if (jobStatus == JobStatus.FAILED || noResult) {
                taskResult = new TaskResultImpl(task.getId(), new Exception(errorMsg),
                    new SimpleTaskLogs("", errorMsg), -1);
            }

            dbManager.updateAfterJobFailed(job, task, taskResult, tasksToUpdate);
            updateTasksInSchedulerState(job, tasksToUpdate);
        }

        //update job and tasks events list and send it to front-end
        updateJobInSchedulerState(job, event);

        jlogger.info(job.getId(), "finished (" + jobStatus + ")");
    }

    private void updateTasksInSchedulerState(InternalJob job, Set<TaskId> tasksToUpdate) {
        for (TaskId tid : tasksToUpdate) {
            try {
                InternalTask t = job.getTask(tid);
                TaskInfo ti = new TaskInfoImpl((TaskInfoImpl) t.getTaskInfo());
                listener.taskStateUpdated(job.getOwner(),
                        new NotificationData<>(SchedulerEvent.TASK_RUNNING_TO_FINISHED, ti));
            } catch (UnknownTaskException e) {
                logger.error(e);
            }
        }
    }

    private JobData lockJob(JobId jobId) {
        JobData jobData = jobs.get(jobId);
        if (jobData == null) {
            jlogger.info(jobId, "does not exist");
            return null;
        }
        jobData.jobLock.lock();
        if (jobs.containsKey(jobId)) {
            return jobData;
        } else {
            jobData.unlock();
            return null;
        }
    }

    private JobData checkJobAccess(JobId jobId) {
        JobData jobData = jobs.get(jobId);
        if (jobData == null) {
            throw new IllegalArgumentException("Unknown job: " + jobId);
        }
        if (!jobData.jobLock.isHeldByCurrentThread()) {
            throw new IllegalThreadStateException("Thread doesn't hold lock for job " + jobId);
        } else {
            return jobData;
        }
    }

    private void updateJobInSchedulerState(InternalJob currentJob, SchedulerEvent eventType) {
        try {
            listener.jobStateUpdated(currentJob.getOwner(), new NotificationData<JobInfo>(eventType,
                new JobInfoImpl((JobInfoImpl) currentJob.getJobInfo())));
        } catch (Throwable t) {
            //Just to prevent update method error
        }
    }

    private void updateTaskPausedOnerrorState(InternalJob job, TaskId taskToUpdate) {
        try {
            InternalTask t = job.getTask(taskToUpdate);
            TaskInfo ti = new TaskInfoImpl((TaskInfoImpl) t.getTaskInfo());
            listener.taskStateUpdated(job.getOwner(),
                    new NotificationData<>(SchedulerEvent.TASK_IN_ERROR, ti));
        } catch (UnknownTaskException e) {
            logger.error(e);
        }

    }

}
