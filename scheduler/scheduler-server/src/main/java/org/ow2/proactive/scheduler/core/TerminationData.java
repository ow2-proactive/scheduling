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
package org.ow2.proactive.scheduler.core;

import static org.ow2.proactive.scheduler.core.TerminationData.TerminationStatus.ABORTED;
import static org.ow2.proactive.scheduler.core.TerminationData.TerminationStatus.NODEFAILED;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.ListUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.signal.SignalApiImpl;
import org.ow2.proactive.scheduler.synchronization.SynchronizationWrapper;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.internal.InternalTaskParentFinder;
import org.ow2.proactive.scheduler.task.utils.VariablesMap;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.utils.TaskIdWrapper;


/*
 * Keeps information about actions which should be executed when task/job
 * terminates.
 */
final class TerminationData {

    public static final Logger logger = Logger.getLogger(SchedulingService.class);

    static class TaskTerminationData {

        private final RunningTaskData taskData;

        private final TaskResultImpl taskResult;

        private final InternalJob internalJob;

        private final TerminationStatus terminationStatus;

        TaskTerminationData(InternalJob internalJob, RunningTaskData taskData, TerminationStatus terminationStatus,
                TaskResultImpl taskResult) {
            this.taskData = taskData;
            this.terminationStatus = terminationStatus;
            this.taskResult = taskResult;
            this.internalJob = internalJob;
        }

        public boolean terminatedWhileRunning() {
            return taskData.getLauncher() != null;
        }

    }

    public enum TerminationStatus {
        NORMAL,
        ABORTED,
        NODEFAILED
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

    private final Map<JobId, Map<String, String>> jobsToTerminateGenericInformation;

    private final Map<JobId, Credentials> jobsToTerminateCredentials;

    private final Map<TaskIdWrapper, TaskTerminationData> tasksToTerminate;

    private final Map<TaskIdWrapper, TaskRestartData> tasksToRestart;

    private final InternalTaskParentFinder internalTaskParentFinder;

    static final TerminationData EMPTY = new TerminationData(Collections.emptySet(),
                                                             Collections.emptyMap(),
                                                             Collections.emptyMap(),
                                                             Collections.emptyMap(),
                                                             Collections.emptyMap());

    static TerminationData newTerminationData() {
        return new TerminationData(new HashSet<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private TerminationData(Set<JobId> jobsToTerminate, Map<TaskIdWrapper, TaskTerminationData> tasksToTerminate,
            Map<TaskIdWrapper, TaskRestartData> tasksToRestart,
            Map<JobId, Map<String, String>> jobsToTerminateGenericInformation,
            Map<JobId, Credentials> jobsToTerminateCredentials) {
        this.jobsToTerminate = jobsToTerminate;
        this.tasksToTerminate = tasksToTerminate;
        this.jobsToTerminateGenericInformation = jobsToTerminateGenericInformation;
        this.tasksToRestart = tasksToRestart;
        this.internalTaskParentFinder = InternalTaskParentFinder.getInstance();
        this.jobsToTerminateCredentials = jobsToTerminateCredentials;
    }

    void addJobToTerminate(JobId jobId, Map<String, String> jobGenericInfo, Credentials credentials) {
        jobsToTerminate.add(jobId);
        jobsToTerminateGenericInformation.put(jobId, jobGenericInfo);
        jobsToTerminateCredentials.put(jobId, credentials);
    }

    public Credentials getCredentials(JobId jobId) {
        return jobsToTerminateCredentials.get(jobId);
    }

    void addTaskData(InternalJob jobData, RunningTaskData taskData, TerminationStatus terminationStatus,
            TaskResultImpl taskResult) {
        tasksToTerminate.put(TaskIdWrapper.wrap(taskData.getTask().getId()),
                             new TaskTerminationData(jobData, taskData, terminationStatus, taskResult));
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

        terminateTasks(service);

        restartWaitingTasks(service);

        terminateJobs(service);
    }

    private void terminateJobs(final SchedulingService service) {
        for (JobId jobId : jobsToTerminate) {
            service.terminateJobHandling(jobId, jobsToTerminateGenericInformation.get(jobId));
        }
    }

    private void restartWaitingTasks(final SchedulingService service) {
        for (final TaskRestartData restartData : tasksToRestart.values()) {
            service.getInfrastructure().schedule(() -> service.getJobs().restartWaitingTask(restartData.taskId),
                                                 restartData.waitTime);
        }
    }

    private void terminateTasks(final SchedulingService service) {

        if (tasksToTerminate.values().isEmpty()) {
            return;
        }

        ExecutorService parallelTerminationService = Executors.newFixedThreadPool(tasksToTerminate.values().size());

        try {
            List<Callable<Void>> callables = new ArrayList<>(tasksToTerminate.values().size());

            for (final TaskTerminationData taskToTerminate : tasksToTerminate.values()) {

                callables.add(() -> {
                    try {
                        RunningTaskData taskData = taskToTerminate.taskData;
                        if (taskToTerminate.terminatedWhileRunning()) {
                            terminateRunningTask(service, taskToTerminate, taskData);
                        } else {
                            TaskLogger.getInstance().close(taskToTerminate.taskData.getTask().getId());
                        }
                    } catch (Throwable e) {
                        logger.error("Failed to terminate task " + taskToTerminate.taskData.getTask().getName(), e);
                        throw new RuntimeException(e);
                    }
                    return null;
                });
            }
            parallelTerminationService.invokeAll(callables);
        } catch (Exception e) {
            logger.error("Failed to terminate tasks ", e);
        } finally {
            parallelTerminationService.shutdown();
        }
    }

    private void terminateRunningTask(SchedulingService service, TaskTerminationData taskToTerminate,
            RunningTaskData taskData) {
        Map<String, String> genericInformation = new HashMap<>();
        VariablesMap variables = null;
        if (taskToTerminate.internalJob != null) {
            genericInformation = taskData.getTask().getRuntimeGenericInformation();
        }
        try {
            variables = getStringSerializableMap(service, taskToTerminate);
        } catch (Exception e) {
            logger.error("Exception occurred, fail to get variables into the cleaning script: ", e);
        }
        try {
            if (taskToTerminate.terminationStatus == ABORTED) {
                taskData.getLauncher().kill();
            }
        } catch (Throwable t) {
            logger.info("Cannot terminate task launcher for task '" + taskData.getTask().getId() + "'", t);
            try {
                logger.info("Task launcher that cannot be terminated is identified by " +
                            taskData.getLauncher().toString());
            } catch (Throwable ignore) {
                logger.info("Getting information about Task launcher failed (remote object not accessible?)");
            }
        }

        try {
            logger.debug("Releasing nodes for task '" + taskData.getTask().getId() + "'");
            RMProxiesManager proxiesManager = service.getInfrastructure().getRMProxiesManager();
            proxiesManager.getUserRMProxy(taskData.getUser(), taskData.getCredentials())
                          .releaseNodes(taskData.getNodes(),
                                        getCleaningScript(taskToTerminate, taskData),
                                        variables,
                                        genericInformation,
                                        taskToTerminate.taskData.getTask().getId(),
                                        service.addThirdPartyCredentials(taskData.getCredentials()),
                                        new SynchronizationWrapper(taskToTerminate.internalJob.getOwner(),
                                                                   taskData.getTask().getId(),
                                                                   taskToTerminate.internalJob.getSynchronizationAPI()),
                                        new SignalApiImpl(taskToTerminate.internalJob.getOwner(),
                                                          taskData.getTask().getId(),
                                                          taskToTerminate.internalJob.getSynchronizationAPI()));

        } catch (Throwable t) {
            logger.info("Failed to release nodes for task '" + taskData.getTask().getId() + "'", t);
        }
    }

    private Script<?> getCleaningScript(TaskTerminationData taskToTerminate, RunningTaskData taskData) {
        return taskToTerminate.terminationStatus != NODEFAILED ? taskData.getTask().getCleaningScript() : null;
    }

    public VariablesMap getStringSerializableMap(SchedulingService service, TaskTerminationData taskToTerminate)
            throws Exception {
        VariablesMap variablesMap = new VariablesMap();

        RunningTaskData taskData = taskToTerminate.taskData;
        TaskResultImpl taskResult = taskToTerminate.taskResult;
        InternalJob internalJob = taskToTerminate.internalJob;

        if (taskToTerminate.terminationStatus == ABORTED || taskResult == null) {
            List<InternalTask> iDependences = taskData.getTask().getIDependences();
            if (iDependences != null) {
                taskData.getTask().updateParentTasksResults(service);
                getResultsFromListOfTaskResults(variablesMap.getInheritedMap(),
                                                taskData.getTask().getParentTasksResults());
            } else {
                if (internalJob != null) {
                    for (Map.Entry<String, JobVariable> jobVariableEntry : internalJob.getVariables().entrySet()) {
                        variablesMap.getInheritedMap().put(jobVariableEntry.getKey(),
                                                           jobVariableEntry.getValue().getValue());
                    }
                }
            }
            variablesMap.getInheritedMap().put(SchedulerVars.PA_TASK_SUCCESS.toString(), Boolean.toString(false));
        } else if (taskResult.hadException()) {
            variablesMap.setInheritedMap(fillMapWithTaskResult(taskResult, false));

        } else {
            variablesMap.setInheritedMap(fillMapWithTaskResult(taskResult, true));
        }
        variablesMap.setScopeMap(getNonInheritedScopeVariables(variablesMap.getInheritedMap(),
                                                               taskData.getTask().getScopeVariables(),
                                                               taskData.getTask().getVariables()));
        return variablesMap;
    }

    private Map<String, Serializable> getNonInheritedScopeVariables(Map<String, Serializable> inheritedVariables,
            Map<String, Serializable> scopeVariables, Map<String, TaskVariable> taskVariables) {
        Map<String, Serializable> scopeMap = new HashMap<>();
        for (Map.Entry<String, Serializable> entry : scopeVariables.entrySet()) {
            if (!taskVariables.get(entry.getKey()).isJobInherited() ||
                (taskVariables.get(entry.getKey()).isJobInherited() &&
                 !inheritedVariables.containsKey(entry.getKey()))) {
                scopeMap.put(entry.getKey(), entry.getValue());
            }
        }
        return scopeMap;
    }

    private Map<String, Serializable> fillMapWithTaskResult(TaskResultImpl taskResult, boolean normalTermination)
            throws IOException, ClassNotFoundException {
        Map<String, Serializable> variables;
        variables = SerializationUtil.deserializeVariableMap(taskResult.getPropagatedVariables());
        variables.put(SchedulerVars.PA_TASK_SUCCESS.toString(), Boolean.toString(normalTermination));
        return variables;
    }

    private void getResultsFromListOfTaskResults(Map<String, Serializable> variables,
            Map<TaskId, TaskResult> taskResults) throws IOException, ClassNotFoundException {
        for (TaskResult currentTaskResult : taskResults.values()) {
            if (currentTaskResult.getPropagatedVariables() != null) {
                variables.putAll(SerializationUtil.deserializeVariableMap(currentTaskResult.getPropagatedVariables()));
            }
        }
    }
}
