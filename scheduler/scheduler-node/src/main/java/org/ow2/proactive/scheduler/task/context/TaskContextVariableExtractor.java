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
package org.ow2.proactive.scheduler.task.context;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.executors.forked.env.ForkedTaskVariablesManager;

import com.google.common.collect.ImmutableMap;


public class TaskContextVariableExtractor implements Serializable {

    private static final Logger logger = Logger.getLogger(TaskContextVariableExtractor.class);

    private final ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();

    public Map<String, String> extractVariablesThirdPartyCredentialsAndSystemEnvironmentVariables(
            TaskContext taskContext) throws Exception {
        ForkEnvironment forkEnvironment = taskContext.getInitializer().getForkEnvironment();
        Map<String, Serializable> variables = this.extractVariables(taskContext, true);
        Map<String, String> thirdPartyCredentials = forkedTaskVariablesManager.extractThirdPartyCredentials(taskContext);
        HashMap<String, Serializable> systemEnvironmentVariables = new HashMap<String, Serializable>(System.getenv());
        systemEnvironmentVariables.putAll(variables);
        systemEnvironmentVariables.putAll(thirdPartyCredentials);

        return VariableSubstitutor.filterAndUpdate(forkEnvironment.getSystemEnvironment(), systemEnvironmentVariables);
    }

    public Map<String, Serializable> extractVariables(TaskContext taskContext, boolean useTaskVariables)
            throws IOException, ClassNotFoundException {
        return extractVariables(taskContext, (TaskResult) null, useTaskVariables);
    }

    private Map<String, Serializable> extractVariables(TaskContext container, TaskResult taskResult, String nodesFile,
            boolean useTaskVariables) throws IOException, ClassNotFoundException {
        Map<String, Serializable> variables = extractVariables(container, taskResult, useTaskVariables);

        variables.put(SchedulerVars.PA_NODESNUMBER.toString(), container.getOtherNodesURLs().size() + 1);
        variables.put(SchedulerVars.PA_NODESFILE.toString(), nodesFile);

        variables.put(SchedulerVars.PA_TASK_PROGRESS_FILE.toString(), container.getProgressFilePath());

        return variables;
    }

    @SuppressWarnings("squid:S134")
    public Map<String, Serializable> extractVariables(TaskContext taskContext, TaskResult taskResult,
            boolean useTaskVariables) throws IOException, ClassNotFoundException {
        Map<String, Serializable> variables = new HashMap<>();

        // job variables from workflow definition
        if (taskContext.getInitializer().getJobVariables() != null) {
            for (JobVariable jobVariable : taskContext.getInitializer().getJobVariables().values()) {
                variables.put(jobVariable.getName(), jobVariable.getValue());
            }
        }

        try {
            // variables from previous tasks
            if (taskContext.getPreviousTasksResults() != null) {
                variables.putAll(extractPreviousTaskResultVariablesFromTaskContext(taskContext));
            }

            // task variables from workflow definition
            if (useTaskVariables && taskContext.getInitializer().getTaskVariables() != null) {
                addTaskVariablesAndBackupInheritedVariables(variables, taskContext.getInitializer().getTaskVariables());
            }

            // and from this task execution
            if (taskResult != null && taskResult.getPropagatedVariables() != null) {
                variables.putAll(SerializationUtil.deserializeVariableMap(taskResult.getPropagatedVariables()));
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Could not deserialize variables", e);
            throw e;
        }

        // variables from current job/task context
        variables.putAll(retrieveContextVariables(taskContext.getInitializer()));

        variables.put(SchedulerVars.PA_SCHEDULER_HOME.toString(), taskContext.getSchedulerHome());
        return variables;
    }

    private void addTaskVariablesAndBackupInheritedVariables(Map<String, Serializable> variables,
            ImmutableMap<String, TaskVariable> taskVariables) {
        Map<String, Serializable> taskAndBackupVariables = new HashMap<>();
        for (TaskVariable taskVariable : taskVariables.values()) {
            String taskName = taskVariable.getName();
            if (!taskVariable.isJobInherited() || (taskVariable.isJobInherited() && variables.get(taskName) == null)) {
                if (variables.get(taskName) != null) {
                    //propagated variable to be restored at the end of the task
                    taskAndBackupVariables.put(SchedulerVars.PA_FLAG_PROPAGATED_VAR_TO_RESTORE.toString() + taskName,
                                               variables.get(taskName));
                } else {
                    //task variable to be cleared at the end of the task
                    taskAndBackupVariables.put(SchedulerVars.PA_FLAG_TASK_VAR_TO_CLEAR.toString() + taskName,
                                               taskVariable.getValue());
                }
                taskAndBackupVariables.put(taskName, taskVariable.getValue());
            }
        }
        variables.putAll(taskAndBackupVariables);
        //return taskAndBackupVariables;
    }

    public Map<String, Serializable> extractScopeVariables(TaskContext taskContext)
            throws IOException, ClassNotFoundException {
        Map<String, Serializable> variables = new HashMap<>();

        // variables from task definition
        if (taskContext.getInitializer() != null && taskContext.getInitializer().getTaskVariables() != null) {
            variables = appendNonInheritedTaskVariables(taskContext);
        }
        return variables;
    }

    public Map<String, Serializable> appendNonInheritedTaskVariables(TaskContext taskContext)
            throws IOException, ClassNotFoundException {
        Map<String, Serializable> variables = new HashMap<>();
        Map<String, Serializable> previousVariables = new HashMap<>();
        try {
            previousVariables = extractVariables(taskContext, false);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Unable to extract variables", e);
            throw e;
        }
        for (TaskVariable taskVariable : taskContext.getInitializer().getTaskVariables().values()) {
            if (!taskVariable.isJobInherited() ||
                (taskVariable.isJobInherited() && !previousVariables.containsKey(taskVariable.getName()))) {
                variables.put(taskVariable.getName(), taskVariable.getValue());
            }
        }
        return variables;
    }

    public Map<String, Serializable> extractVariables(TaskContext container, String nodesFile, boolean useTaskVariables)
            throws Exception {
        return extractVariables(container, null, nodesFile, useTaskVariables);
    }

    public Map<String, Serializable> retrieveContextVariables(TaskLauncherInitializer initializer) {
        Map<String, Serializable> variables = new HashMap<>();
        variables.put(SchedulerVars.PA_JOB_ID.toString(), initializer.getTaskId().getJobId().value());
        variables.put(SchedulerVars.PA_JOB_NAME.toString(), initializer.getTaskId().getJobId().getReadableName());
        variables.put(SchedulerVars.PA_TASK_ID.toString(), initializer.getTaskId().value());
        variables.put(SchedulerVars.PA_TASK_NAME.toString(), initializer.getTaskId().getReadableName());
        variables.put(SchedulerVars.PA_TASK_ITERATION.toString(), initializer.getIterationIndex());
        variables.put(SchedulerVars.PA_TASK_REPLICATION.toString(), initializer.getReplicationIndex());
        variables.put(SchedulerVars.PA_USER.toString(), initializer.getJobOwner());
        variables.put(SchedulerVars.PA_SCHEDULER_REST_URL.toString(), initializer.getSchedulerRestUrl());
        return variables;
    }

    private Map<String, Serializable> extractPreviousTaskResultVariablesFromTaskContext(TaskContext container)
            throws IOException, ClassNotFoundException {
        Map<String, Serializable> result = new HashMap<>();
        for (TaskResult previousTaskResult : container.getPreviousTasksResults()) {
            if (previousTaskResult.getPropagatedVariables() != null) {
                result.putAll(SerializationUtil.deserializeVariableMap(previousTaskResult.getPropagatedVariables()));
            }
        }
        return result;
    }
}
