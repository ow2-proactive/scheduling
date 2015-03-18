package org.ow2.proactive.scheduler.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.exception.RunningProcessException;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalNativeTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.PerfLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.apache.log4j.Logger;


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

    private final Map<JobId, JobData> jobs = new ConcurrentHashMap<JobId, LiveJobs.JobData>();

    private final ConcurrentHashMap<TaskId, RunningTaskData> runningTasksData = new ConcurrentHashMap<TaskId, RunningTaskData>();

    LiveJobs(SchedulerDBManager dbManager, SchedulerStateUpdate listener) {
        this.dbManager = dbManager;
        this.listener = listener;
    }

    Collection<RunningTaskData> getRunningTasks() {
        return runningTasksData.values();
    }

    boolean canPingTask(RunningTaskData taskData) {
        return runningTasksData.get(taskData.getTask().getId()) == taskData;
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
        List<RunningTaskData> result = new ArrayList<RunningTaskData>();
        for (RunningTaskData taskData : runningTasksData.values()) {
            if (taskData.getTask().getJobId().equals(jobId)) {
                result.add(taskData);
            }
        }
        return result;
    }

    RunningTaskData getRunningTask(TaskId taskId) {
        List<RunningTaskData> result = new ArrayList<RunningTaskData>();
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

    void jobSubmitted(InternalJob job, SchedulerClassServers classServers,
            SchedulerSpacesSupport spacesSupport) {
        PerfLogger.log(job.getId() + ": job arrived", job.getSubmissionWatch());
        job.prepareTasks();
        job.submitAction();
        PerfLogger.log(job.getId() + ": job ready", job.getSubmissionWatch());
        dbManager.newJobSubmitted(job);
        PerfLogger.log(job.getId() + ": job stored", job.getSubmissionWatch());
        classServers.createTaskClassServer(job, spacesSupport);
        ClientJobState clientJobState = new ClientJobState(job);
        jobs.put(job.getId(), new JobData(job));
        PerfLogger.log(job.getId() + ": job queued", job.getSubmissionWatch());
        listener.jobSubmitted(clientJobState);
    }

    Map<JobId, JobDescriptor> lockJobsToSchedule() {
        Map<JobId, JobDescriptor> result = new HashMap<JobId, JobDescriptor>();
        for (Map.Entry<JobId, JobData> entry : jobs.entrySet()) {
            if (entry.getValue().jobLock.tryLock() && jobs.containsKey(entry.getKey())) {
                result.put(entry.getValue().job.getId(), entry.getValue().job.getJobDescriptor());
            }
        }
        return result;
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

    private void restartTaskOnNodeFailure(InternalTask task, JobData jobData, TerminationData terminationData) {
        final String errorMsg = "An error has occurred due to a node failure and the maximum amount of retries property has been reached.";

        task.setProgress(0);
        task.decreaseNumberOfExecutionOnFailureLeft();
        tlogger.info(task.getId(), "number of retry on failure left " +
            task.getNumberOfExecutionOnFailureLeft());
        if (task.getNumberOfExecutionOnFailureLeft() > 0) {
            task.setStatus(TaskStatus.WAITING_ON_FAILURE);
            jobData.job.newWaitingTask();
            listener
                    .taskStateUpdated(jobData.job.getOwner(), new NotificationData<TaskInfo>(
                        SchedulerEvent.TASK_WAITING_FOR_RESTART, new TaskInfoImpl((TaskInfoImpl) task
                                .getTaskInfo())));
            jobData.job.reStartTask(task);
            dbManager.taskRestarted(jobData.job, task, null);
            tlogger.info(task.getId(), " is waiting for restart");
        } else {
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

            RunningTaskData taskData = runningTasksData.remove(taskId);
            if (taskData == null) {
                throw new IllegalStateException("No information for: " + taskId);
            }

            TerminationData result = TerminationData.newTerminationData();
            result.addTaskData(taskData, false);

            restartTaskOnNodeFailure(task, jobData, result);

            return result;
        } finally {
            jobData.unlock();
        }
    }

    private void restartTaskOnError(JobData jobData, InternalTask task, TaskStatus status,
            TaskResultImpl result, long waitTime, TerminationData terminationData) {
        InternalJob job = jobData.job;

        logger.debug("Node Exclusion : restart mode is '" + task.getRestartTaskOnError() + "'");
        if (task.getRestartTaskOnError().equals(RestartMode.ELSEWHERE)) {
            task.setNodeExclusion(task.getExecuterInformations().getNodes());
        }
        task.setStatus(status);
        job.newWaitingTask();
        dbManager.updateAfterTaskFinished(job, task, result);
        listener.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
            SchedulerEvent.TASK_WAITING_FOR_RESTART, new TaskInfoImpl((TaskInfoImpl) task.getTaskInfo())));

        terminationData.addRestartData(task.getId(), waitTime);
    }

    TerminationData simulateJobStart(List<EligibleTaskDescriptor> tasksToSchedule, String errorMsg) {
        TerminationData terminationData = TerminationData.newTerminationData();
        for (EligibleTaskDescriptor eltd : tasksToSchedule) {
            JobId jobId = eltd.getJobId();
            if (!terminationData.jobTeminated(jobId)) {
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
        if (runningTasksData.containsKey(task.getId())) {
            throw new IllegalStateException("Task is already started");
        }

        runningTasksData.put(task.getId(), new RunningTaskData(task, job.getOwner(), job.getCredentials(),
            launcher));

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

    TerminationData emptyResult(TaskId taskId) {
        RunningTaskData taskData = runningTasksData.remove(taskId);
        if (taskData != null) {
            throw new IllegalStateException("Task is marked as running: " + taskId);
        }
        return TerminationData.EMPTY;
    }

    TerminationData emptyData(JobId jobId) {
        for (TaskId taskId : runningTasksData.keySet()) {
            if (taskId.getJobId().equals(jobId)) {
                throw new IllegalStateException("Unexpected task data: " + taskId);
            }
        }
        return TerminationData.EMPTY;
    }

    TerminationData taskTerminatedWithResult(TaskId taskId, TaskResultImpl result) {
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

            RunningTaskData taskData = runningTasksData.remove(taskId);
            if (taskData == null) {
                throw new IllegalStateException("No information for: " + taskId);
            }

            TerminationData terminationData = TerminationData.newTerminationData();
            terminationData.addTaskData(taskData, true);

            boolean errorOccurred;
            if (task instanceof InternalNativeTask) {
                try {
                    errorOccurred = handleNativeTaskResult((InternalNativeTask) task, result);
                } catch (RunningProcessException e) {
                    //if res.value throws a RunningProcessException, user is not responsible
                    //change status and update GUI
                    restartTaskOnNodeFailure(task, jobData, terminationData);
                    return terminationData;
                }
            } else {
                tlogger.debug(taskId, "is a java or a script task");
                errorOccurred = result.hadException();
                if (errorOccurred) {
                    tlogger.error(taskId, "error", result.getException());
                }
            }

            tlogger.info(taskId, "finished with" + (errorOccurred ? "" : "out") + " errors");

            if (errorOccurred) {
                task.decreaseNumberOfExecutionLeft();
                if (task.getNumberOfExecutionLeft() <= 0 && task.isCancelJobOnError()) {
                    endJob(jobData, terminationData, task, result,
                            "An error occurred in your task and the maximum number of executions has been reached. "
                                + "You also ask to cancel the job in such a situation !", JobStatus.CANCELED);
                    return terminationData;
                } else if (task.getNumberOfExecutionLeft() > 0) {
                    long waitTime = jobData.job.getNextWaitingTime(task.getMaxNumberOfExecution() -
                        task.getNumberOfExecutionLeft());
                    restartTaskOnError(jobData, task, TaskStatus.WAITING_ON_ERROR, result, waitTime,
                            terminationData);
                    return terminationData;
                }
            }

            terminateTask(jobData, task, errorOccurred, result, terminationData);

            return terminationData;
        } finally {
            jobData.unlock();
        }
    }

    private boolean handleNativeTaskResult(InternalNativeTask task, TaskResultImpl result)
            throws RunningProcessException {
        TaskId taskId = task.getId();

        tlogger.debug(taskId, "is a native task");

        boolean errorOccurred;
        try {
            // try to get the result, res.value can throw an exception,
            // it means that the process has failed before the end.
            int nativeIntegerResult = ((Integer) result.value());
            // an error occurred if res is not 0
            errorOccurred = (nativeIntegerResult != 0);
        } catch (Throwable e) {
            tlogger.error(taskId, "error", e);
            if (e instanceof RunningProcessException) {
                throw (RunningProcessException) e;
            } else {
                errorOccurred = true;
                tlogger.error(taskId, "error", e);
            }
            errorOccurred = true;
        }
        return errorOccurred;
    }

    TerminationData restartTask(JobId jobId, String taskName, int restartDelay) throws UnknownJobException,
            UnknownTaskException {
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
            RunningTaskData taskData = runningTasksData.remove(task.getId());
            if (taskData == null) {
                throw new IllegalStateException("No information for: " + task.getId());
            }

            TerminationData terminationData = TerminationData.newTerminationData();
            terminationData.addTaskData(taskData, false);

            TaskResultImpl taskResult = new TaskResultImpl(task.getId(), new TaskRestartedException(
                "Aborted by user"), new SimpleTaskLogs("", "Aborted by user"), System.currentTimeMillis() -
                task.getStartTime());

            task.decreaseNumberOfExecutionLeft();
            if (task.getNumberOfExecutionLeft() <= 0 && task.isCancelJobOnError()) {
                endJob(jobData, terminationData, task, taskResult,
                        "An error occurred in your task and the maximum number of executions has been reached. "
                            + "You also ask to cancel the job in such a situation !", JobStatus.CANCELED);
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

    TerminationData preemptTask(JobId jobId, String taskName, int restartDelay) throws UnknownJobException,
            UnknownTaskException {
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
            RunningTaskData taskData = runningTasksData.remove(task.getId());
            if (taskData == null) {
                throw new IllegalStateException("No information for: " + task.getId());
            }

            TerminationData terminationData = TerminationData.newTerminationData();
            terminationData.addTaskData(taskData, false);

            TaskResultImpl taskResult = new TaskResultImpl(task.getId(), new TaskPreemptedException(
                "Preempted by admin"), new SimpleTaskLogs("", "Preempted by admin"), System
                    .currentTimeMillis() -
                task.getStartTime());

            long waitTime = restartDelay * 1000l;
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
            RunningTaskData taskData = runningTasksData.remove(task.getId());
            if (taskData == null) {
                throw new IllegalStateException("No information for: " + task.getId());
            }

            TerminationData terminationData = TerminationData.newTerminationData();
            terminationData.addTaskData(taskData, false);

            TaskResultImpl taskResult = new TaskResultImpl(task.getId(), new TaskAbortedException(
                "Aborted by user"), new SimpleTaskLogs("", "Aborted by user"), System.currentTimeMillis() -
                task.getStartTime());

            if (task.isCancelJobOnError()) {
                endJob(jobData, terminationData, task, taskResult, "The task has been manually killed. "
                    + "You also ask to cancel the job in such a situation !", JobStatus.CANCELED);
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

        tlogger.info(taskId, "result added to job " + job.getId());
        //to be done before terminating the task, once terminated it is not running anymore..
        job.getRunningTaskDescriptor(taskId);
        ChangedTasksInfo changesInfo = job.terminateTask(errorOccurred, taskId, listener, result.getAction(),
                result);

        boolean jobFinished = job.isFinished();

        //update job info if it is terminated
        if (jobFinished) {
            //terminating job
            job.terminate();
            jlogger.info(job.getId(), "terminated");
            jobs.remove(job.getId());
            terminationData.addJobToTermiante(job.getId());
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
        terminationData.addJobToTermiante(jobId);

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
                terminationData.addTaskData(taskData, false);
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
                taskResult = new TaskResultImpl(task.getId(), new Exception(errorMsg), new SimpleTaskLogs("",
                    errorMsg), -1, null);
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
                listener.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
                    SchedulerEvent.TASK_RUNNING_TO_FINISHED, ti));
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

}
