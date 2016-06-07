package org.ow2.proactive.scheduler.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.utils.TaskIdWrapper;
import org.apache.log4j.Logger;


/*
 * Keeps information about actions which should be executed when task/job
 * terminates.
 */
final class TerminationData {

    public static final Logger logger = Logger.getLogger(SchedulingService.class);

    static class TaskTerminationData {

        private final RunningTaskData taskData;

        private final boolean normalTermination;

        private final TaskResultImpl taskResult;

        private final InternalJob internalJob;

        TaskTerminationData(InternalJob internalJob, RunningTaskData taskData, boolean normalTermination, TaskResultImpl taskResult) {
            this.taskData = taskData;
            this.normalTermination = normalTermination;
            this.taskResult = taskResult;
            this.internalJob = internalJob;
        }

    }

    static class TaskRestartData {

        private final TaskId taskId;

        private final long waitTime;

        TaskRestartData(TaskId taskId, long waitTime) {
            this.taskId = taskId;
            this.waitTime = waitTime;
        }

    }

    private final Set<JobId> jobsToTerminate;

    private final Map<TaskIdWrapper, TaskTerminationData> tasksToTerminate;

    private final Map<TaskIdWrapper, TaskRestartData> tasksToRestart;

    static final TerminationData EMPTY = new TerminationData(Collections.<JobId>emptySet(), Collections
            .<TaskIdWrapper, TaskTerminationData>emptyMap(), Collections.<TaskIdWrapper, TaskRestartData>emptyMap());

    static TerminationData newTerminationData() {
        return new TerminationData(new HashSet<JobId>(),
                new HashMap<TaskIdWrapper, TaskTerminationData>(),
                new HashMap<TaskIdWrapper, TaskRestartData>());
    }

    private TerminationData(Set<JobId> jobsToTerminate, Map<TaskIdWrapper, TaskTerminationData> tasksToTerminate,
                            Map<TaskIdWrapper, TaskRestartData> tasksToRestart) {
        this.jobsToTerminate = jobsToTerminate;
        this.tasksToTerminate = tasksToTerminate;
        this.tasksToRestart = tasksToRestart;
    }

    void addJobToTerminate(JobId jobId) {
        jobsToTerminate.add(jobId);
    }

    void addTaskData(InternalJob jobData, RunningTaskData taskData, boolean normalTermination, TaskResultImpl taskResult) {
        tasksToTerminate
                .put(TaskIdWrapper.wrap(taskData.getTask().getId()), new TaskTerminationData(jobData, taskData, normalTermination, taskResult));
    }

    void addRestartData(TaskId taskId, long waitTime) {
        tasksToRestart.put(TaskIdWrapper.wrap(taskId), new TaskRestartData(taskId, waitTime));
    }

    boolean isEmpty() {
        return tasksToTerminate.isEmpty() && tasksToRestart.isEmpty() && jobsToTerminate.isEmpty();
    }

    boolean jobTerminated(JobId jobId) {
        return jobsToTerminate.contains(jobId);
    }

    boolean taskTerminated(JobId j, String taskName) {
        for (TaskIdWrapper taskIdWrapper : tasksToTerminate.keySet()) {
            if (taskIdWrapper.getTaskId().getReadableName().equals(taskName)) {
                return true;
            }
        }
        return false;
    }

    void handleTermination(final SchedulingService service) throws IOException, ClassNotFoundException {
        for (TaskTerminationData taskToTerminate : tasksToTerminate.values()) {
            RunningTaskData taskData = taskToTerminate.taskData;

            Map<String, Serializable> variables = getStringSerializableMap(service, taskToTerminate);

            try {
                if (!taskToTerminate.normalTermination) {
                    taskData.getLauncher().kill();
                }
            } catch (Throwable t) {
                logger
                        .info("Cannot terminate task launcher for task '" + taskData.getTask().getId() + "'",
                                t);
            }

            try {
                logger.debug("Releasing nodes for task '" + taskData.getTask().getId() + "'");
                RMProxiesManager proxiesManager = service.getInfrastructure().getRMProxiesManager();
                proxiesManager.getUserRMProxy(taskData.getUser(), taskData.getCredentials()).releaseNodes(
                        taskData.getNodes(), taskData.getTask().getCleaningScript(), variables);
            } catch (Throwable t) {
                logger.info("Failed to release nodes for task '" + taskData.getTask().getId() + "'", t);
            }
        }

        for (final TaskRestartData restartData : tasksToRestart.values()) {
            service.getInfrastructure().schedule(new Runnable() {
                public void run() {
                    service.jobs.restartWaitingTask(restartData.taskId);
                }
            }, restartData.waitTime);
        }

        for (JobId jobId : jobsToTerminate) {
            service.terminateJobHandling(jobId);
        }
    }

    @NotNull
    public Map<String, Serializable> getStringSerializableMap(SchedulingService service, TaskTerminationData taskToTerminate) throws IOException, ClassNotFoundException {
        Map<String, Serializable> variables = new HashMap<>();

        RunningTaskData taskData = taskToTerminate.taskData;
        TaskResultImpl taskResult = taskToTerminate.taskResult;
        InternalJob internalJob = taskToTerminate.internalJob;

        if (!taskToTerminate.normalTermination || taskResult == null) {
            List<InternalTask> iDependences = taskData.getTask().getIDependences();
            if (iDependences != null) {
                List<TaskId> parentIds = new ArrayList<>(iDependences.size());
                for (int i = 0; i < iDependences.size(); i++) {
                    parentIds.add(iDependences.get(i).getId());
                }
                Map<TaskId, TaskResult> taskResults = service.getInfrastructure().getDBManager()
                        .loadTasksResults(
                                taskData.getTask().getJobId(), parentIds);
                for (TaskResult currentTaskResult : taskResults.values()) {
                    if (currentTaskResult.getPropagatedVariables() != null) {
                        variables.putAll(SerializationUtil.deserializeVariableMap(currentTaskResult.getPropagatedVariables()));
                    }
                }
            } else {
                if(internalJob!=null)
                    variables.putAll(internalJob.getVariables());
            }
            variables.put(SchedulerVars.PA_TASK_SUCCESS.toString(),Boolean.toString(false));
        } else if (taskResult.hadException()) {
            variables = SerializationUtil.deserializeVariableMap(taskResult.getPropagatedVariables());
            variables.put(SchedulerVars.PA_TASK_SUCCESS.toString(),Boolean.toString(false));
        } else {
            variables = SerializationUtil.deserializeVariableMap(taskResult.getPropagatedVariables());
            variables.put(SchedulerVars.PA_TASK_SUCCESS.toString(), Boolean.toString(true));
        }
        return variables;
    }
}
