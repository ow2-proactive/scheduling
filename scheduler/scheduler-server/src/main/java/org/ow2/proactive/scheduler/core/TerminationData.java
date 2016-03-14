package org.ow2.proactive.scheduler.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
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

        TaskTerminationData(RunningTaskData taskData, boolean normalTermination) {
            this.taskData = taskData;
            this.normalTermination = normalTermination;
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

    static final TerminationData EMPTY = new TerminationData(Collections.<JobId> emptySet(), Collections
            .<TaskIdWrapper, TaskTerminationData> emptyMap(), Collections.<TaskIdWrapper, TaskRestartData> emptyMap());

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

    void addTaskData(RunningTaskData taskData, boolean normalTermination) {
        tasksToTerminate
                .put(TaskIdWrapper.wrap(taskData.getTask().getId()), new TaskTerminationData(taskData, normalTermination));
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

    boolean taskTerminated(JobId jobId, String taskName) {
        for (TaskIdWrapper taskIdWrapper : tasksToTerminate.keySet()) {
            if (taskIdWrapper.getTaskId().getReadableName().equals(taskName)) {
                return true;
            }
        }
        return false;
    }

    void handleTermination(final SchedulingService service) {
        for (TaskTerminationData taskToTerminate : tasksToTerminate.values()) {
            RunningTaskData taskData = taskToTerminate.taskData;
            try {
                if(!taskToTerminate.normalTermination){
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
                        taskData.getNodes(), taskData.getTask().getCleaningScript());
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
}
