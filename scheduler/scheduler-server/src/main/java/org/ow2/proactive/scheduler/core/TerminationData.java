package org.ow2.proactive.scheduler.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;


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

    private final Map<TaskId, TaskTerminationData> tasksToTerminate;

    private final Map<TaskId, TaskRestartData> tasksToRestart;

    static final TerminationData EMPTY = new TerminationData(Collections.<JobId> emptySet(), Collections
            .<TaskId, TaskTerminationData> emptyMap(), Collections.<TaskId, TaskRestartData> emptyMap());

    static TerminationData newTerminationData() {
        return new TerminationData(new HashSet<JobId>(),
                new HashMap<TaskId, TaskTerminationData>(),
                new HashMap<TaskId, TaskRestartData>());
    }

    private TerminationData(Set<JobId> jobsToTerminate, Map<TaskId, TaskTerminationData> tasksToTerminate,
            Map<TaskId, TaskRestartData> tasksToRestart) {
        this.jobsToTerminate = jobsToTerminate;
        this.tasksToTerminate = tasksToTerminate;
        this.tasksToRestart = tasksToRestart;
    }

    void addJobToTermiante(JobId jobId) {
        jobsToTerminate.add(jobId);
    }

    void addTaskData(RunningTaskData taskData, boolean normalTermination) {
        tasksToTerminate
                .put(taskData.getTask().getId(), new TaskTerminationData(taskData, normalTermination));
    }

    void addRestartData(TaskId taskId, long waitTime) {
        tasksToRestart.put(taskId, new TaskRestartData(taskId, waitTime));
    }

    boolean isEmpty() {
        return tasksToTerminate.isEmpty() && tasksToRestart.isEmpty() && jobsToTerminate.isEmpty();
    }

    boolean jobTeminated(JobId jobId) {
        return jobsToTerminate.contains(jobId);
    }

    boolean taskTeminated(JobId jobId, String taskName) {
        for (TaskId taskId : tasksToTerminate.keySet()) {
            if (taskId.getReadableName().equals(taskName)) {
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
                    taskData.getLauncher().terminate(false);
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
