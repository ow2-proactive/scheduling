/*
 *  *
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
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.context;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.executors.forked.env.ForkedTaskVariablesManager;

public class TaskContextVariableExtractor implements Serializable {
    private final ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();

    public Map<String, String> extractVariablesThirdPartyCredentialsAndSystemEnvironmentVariables(TaskContext taskContext) throws Exception {
        ForkEnvironment forkEnvironment = taskContext.getInitializer().getForkEnvironment();
        Map<String, Serializable> variables = this.extractTaskVariables(taskContext);
        Map<String, String> thirdPartyCredentials = forkedTaskVariablesManager.extractThirdPartyCredentials(taskContext);
        HashMap<String, Serializable> systemEnvironmentVariables = new HashMap<String, Serializable>(
                System.getenv());
        systemEnvironmentVariables.putAll(variables);
        systemEnvironmentVariables.putAll(thirdPartyCredentials);

        return VariableSubstitutor.filterAndUpdate(forkEnvironment.getSystemEnvironment(),
                systemEnvironmentVariables);
    }

    public Map<String, Serializable> extractTaskVariables(TaskContext taskContext) throws Exception {
        return extractTaskVariables(taskContext, (TaskResult) null);
    }

    private Map<String, Serializable> extractTaskVariables(TaskContext container, TaskResult taskResult,
            String nodesFile)
            throws Exception {
        Map<String, Serializable> variables = extractTaskVariables(container, taskResult);

        variables.put(SchedulerVars.PA_NODESNUMBER.toString(), container.getOtherNodesURLs().size() + 1);
        variables.put(SchedulerVars.PA_NODESFILE.toString(), nodesFile);

        variables.put(SchedulerVars.PA_TASK_PROGRESS_FILE.toString(), container.getProgressFilePath());

        return variables;
    }

    public Map<String, Serializable> extractTaskVariables(TaskContext taskContext,
            TaskResult taskResult) throws Exception {
        Map<String, Serializable> variables = new HashMap<>();

        // variables from workflow definition
        if (taskContext.getInitializer().getVariables() != null) {
            variables.putAll(taskContext.getInitializer().getVariables());
        }

        try {
            // variables from previous tasks
            if (taskContext.getPreviousTasksResults() != null) {
                variables.putAll(extractPrevoiusTaskResultVariablesFromTaskContext(taskContext));
            }
            // and from this task execution
            if (taskResult != null && taskResult.getPropagatedVariables() != null) {
                variables.putAll(SerializationUtil.deserializeVariableMap(taskResult
                        .getPropagatedVariables()));
            }
        } catch (Exception e) {
            throw new Exception("Could not deserialize variables", e);
        }

        // variables from current job/task context
        variables.putAll(retrieveContextVariables(taskContext.getInitializer()));

        variables.put(SchedulerVars.PA_SCHEDULER_HOME.toString(), taskContext.getSchedulerHome());
        return variables;
    }


    public Map<String, Serializable> extractTaskVariables(TaskContext container,
            String nodesFile) throws Exception {
        return extractTaskVariables(container, null, nodesFile);
    }

    public Map<String, Serializable> retrieveContextVariables(TaskLauncherInitializer initializer) {
        Map<String, Serializable> variables = new HashMap<>();
        variables.put(SchedulerVars.PA_JOB_ID.toString(), initializer.getTaskId().getJobId().value());
        variables.put(SchedulerVars.PA_JOB_NAME.toString(), initializer.getTaskId().getJobId()
                .getReadableName());
        variables.put(SchedulerVars.PA_TASK_ID.toString(), initializer.getTaskId().value());
        variables.put(SchedulerVars.PA_TASK_NAME.toString(), initializer.getTaskId().getReadableName());
        variables.put(SchedulerVars.PA_TASK_ITERATION.toString(), initializer.getIterationIndex());
        variables.put(SchedulerVars.PA_TASK_REPLICATION.toString(), initializer.getReplicationIndex());
        variables.put(SchedulerVars.PA_USER.toString(), initializer.getJobOwner());
        variables.put(SchedulerVars.PA_REST_URL.toString(), initializer.getRestUrl());
        return variables;
    }

    private Map<String, Serializable> extractPrevoiusTaskResultVariablesFromTaskContext(TaskContext container)
            throws IOException, ClassNotFoundException {
        Map<String, Serializable> result = new HashMap<>();
        for (TaskResult previousTaskResult : container.getPreviousTasksResults()) {
            if (previousTaskResult.getPropagatedVariables() != null) {
                result.putAll(SerializationUtil.deserializeVariableMap(
                        previousTaskResult.getPropagatedVariables()));
            }
        }
        return result;
    }
}
