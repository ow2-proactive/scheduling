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

import static org.ow2.proactive.scheduler.common.util.VariableSubstitutor.filterAndUpdate;

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
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.executors.forked.env.ForkedTaskVariablesManager;


/**
 * This class will help you to create a Map containing all variables you need. I will
 * also manage to resolve values following some constraints of VariableSubstitutor class.
 */
public class TaskContextVariableExtractor implements Serializable {

    private final String ERROR_READING_VARIABLES = "Error reading variables from task context!";

    private static final Logger logger = Logger.getLogger(TaskContextVariableExtractor.class);

    private final ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();

    /**
     * Retrieve all third party credential variables in a map.
     *
     * @param taskContext all information to extract third party credentials is here.
     *
     * @return map containing thirdPartyCredentials
     */
    public Map<String, String>
            extractVariablesThirdPartyCredentialsAndSystemEnvironmentVariables(TaskContext taskContext) {
        ForkEnvironment forkEnvironment = taskContext.getInitializer().getForkEnvironment();
        Map<String, Serializable> variables = new HashMap<>();

        try {
            variables = extractAllVariables(taskContext, null, "");
        } catch (IOException | ClassNotFoundException e) {
            logger.error(ERROR_READING_VARIABLES, e);
        }

        Map<String, String> thirdPartyCredentials = new HashMap<>();

        try {
            thirdPartyCredentials = forkedTaskVariablesManager.extractThirdPartyCredentials(taskContext);
        } catch (Exception e) {
            logger.error(ERROR_READING_VARIABLES, e);
        }

        HashMap<String, Serializable> systemEnvironmentVariables = new HashMap<String, Serializable>(System.getenv());
        systemEnvironmentVariables.putAll(variables);
        systemEnvironmentVariables.putAll(thirdPartyCredentials);

        return filterAndUpdate(forkEnvironment.getSystemEnvironment(), systemEnvironmentVariables);
    }

    /**
     * Return all variables in scope of a given taskContext.
     *
     * @param taskContext task context container.
     * @return Map containing all variables resolved.
     */
    public Map<String, Serializable> getScopeVariables(TaskContext taskContext) {
        Map<String, Serializable> variables = new HashMap<>();
        Map<String, Serializable> inherited = new HashMap<>();
        Map<String, Serializable> dictionary = new HashMap<>();

        try {
            inherited.putAll(extractSystemVariables(taskContext, ""));
            inherited.putAll(extractJobVariables(taskContext));
            inherited.putAll(extractInheritedVariables(taskContext));

            for (TaskVariable taskVariable : taskContext.getInitializer().getTaskVariables().values()) {
                if (!taskVariable.isJobInherited()) {
                    //add non inherited variables
                    variables.put(taskVariable.getName(), taskVariable.getValue());
                } else if (!inherited.containsKey(taskVariable.getName())) {
                    //but if the variable is inherited
                    //replace by the inherited value if exists
                    variables.put(taskVariable.getName(), taskVariable.getValue());
                }
            }

            dictionary = extractAllVariables(taskContext, null, "");
        } catch (IOException | ClassNotFoundException e) {
            logger.error(ERROR_READING_VARIABLES, e);
        }
        return resolveVariables(variables, dictionary);
    }

    /**
     * Method to retrieve all variables in scope. Note it ignores TaskResult variables and nodesFile.
     *
     * @param taskContext task context container.
     *
     * @return Map containing all variables extracted.
     *
     */
    public Map<String, Serializable> getAllNonTaskVariables(TaskContext taskContext) {
        Map<String, Serializable> variables = new HashMap<>();
        Map<String, Serializable> dictionary = new HashMap<>();

        try {
            if (taskContext != null) {
                variables.putAll(extractSystemVariables(taskContext, ""));
                variables.putAll(extractJobVariables(taskContext));
                variables.putAll(extractInheritedVariables(taskContext));
            }
            dictionary = extractAllVariables(taskContext, null, "");
        } catch (IOException | ClassNotFoundException e) {
            logger.error(ERROR_READING_VARIABLES, e);
        }

        return resolveVariables(variables, dictionary);
    }

    /**
     * Method to retrieve all variables in scope. Note it ignores TaskResult variables and nodesFile.
     *
     * @param taskContext task context container.
     *
     * @return Map containing all variables extracted.
     */
    public Map<String, Serializable> getAllNonTaskVariablesInjectNodesFile(TaskContext taskContext, String nodesFile) {
        Map<String, Serializable> variables = new HashMap<>();
        Map<String, Serializable> dictionary = new HashMap<>();

        try {
            if (taskContext != null) {
                variables.putAll(extractSystemVariables(taskContext, nodesFile));
                variables.putAll(extractJobVariables(taskContext));
                variables.putAll(extractInheritedVariables(taskContext));
            }
            dictionary = extractAllVariables(taskContext, null, nodesFile);
        } catch (IOException | ClassNotFoundException e) {
            logger.error(ERROR_READING_VARIABLES, e);
        }

        return resolveVariables(variables, dictionary);
    }

    /**
     * Return all variables available.
     *
     * @param taskContext context containing all variables inside.
     * @param taskResult used to retrieve the result task variables.
     *
     * @return Map containing variables.
     */
    public Map<String, Serializable> getAllVariablesWithTaskResult(TaskContext taskContext, TaskResult taskResult) {
        Map<String, Serializable> variables = new HashMap<>();
        try {
            variables = extractAllVariables(taskContext, taskResult, "");
        } catch (IOException | ClassNotFoundException e) {
            logger.error(ERROR_READING_VARIABLES, e);
        }
        return resolveVariables(variables, variables);
    }

    /**
     * Method to retrieve all variables in scope. Note it ignores TaskResult variables and nodesFile.
     *
     * @param taskContext
     *
     * @return Map containing all variables extracted.
     */
    public Map<String, Serializable> getAllVariables(TaskContext taskContext) {
        Map<String, Serializable> variables = new HashMap<>();
        try {
            variables = extractAllVariables(taskContext, null, "");
        } catch (IOException | ClassNotFoundException e) {
            logger.error(ERROR_READING_VARIABLES, e);
        }
        return resolveVariables(variables, variables);
    }

    /**
     * Creates a HashMap with variablesDictionary where values are resolved.
     *
     * Solve variables value if bound to another variable, for instance "var log=${LOG_ENV_VAR}", we
     * expect that LOG_ENV_VAR is replaced by its value. To do so we have a variables hash, that must have
     * all references and then for each variable we do a filterAndUpdate that will recursively replace
     * when needed, @see VariableSubstitutor.
     *
     * Some limitations to consider: recursive substitution limit is VariableSubstitutor.MAXIMUM_DEPTH,
     * if the variable value is a complex data structure (array, List, Vector) we will not substitute it.
     *
     * @param variables input hash containing variables and their values may reference other variables
     * @return dictionary with the same variables however with their values resolved
     */
    private static Map<String, Serializable> resolveVariables(Map<String, Serializable> variables,
            Map<String, Serializable> dictionary) {
        Map<String, Serializable> dictionaryVariables = new HashMap<>();
        for (Map.Entry<String, Serializable> entry : variables.entrySet()) {
            String resolvedValue;
            if (entry.getValue() instanceof String) {
                resolvedValue = filterAndUpdate(entry.getValue().toString(), dictionary);
                dictionaryVariables.put(entry.getKey(), resolvedValue);
            } else {
                dictionaryVariables.put(entry.getKey(), entry.getValue());
            }
        }
        return dictionaryVariables;
    }

    /**
     * Extract all variables including task, inherited, and also variables from taskResult, this method must
     * be called only internally. The public methods depend on the usage context:
     *
     * @param taskContext includes job variables, inherited variables, and task variables itself.
     * @param taskResult  used to get the variables from taskResult, might be needed.
     *
     * @return map containing variables and unresolved values.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Map<String, Serializable> extractAllVariables(TaskContext taskContext, TaskResult taskResult,
            String nodesFile) throws IOException, ClassNotFoundException {

        Map<String, Serializable> variables = new HashMap<>();

        if (taskContext != null) {
            variables.putAll(extractJobVariables(taskContext));
            variables.putAll(extractInheritedVariables(taskContext));
            variables.putAll(extractTaskVariables(taskContext));
            variables.putAll(extractSystemVariables(taskContext, nodesFile));

        }

        if (taskResult != null) {
            variables.putAll(extractTaskResultVariables(taskResult));
        }

        return variables;
    }

    /**
     * Variables declared on the xml scheme for the workflow.
     *
     * @param taskContext object that contains it all.
     *
     * @return Map with the variables declared in xml at workflow level.
     */
    private Map<String, Serializable> extractJobVariables(TaskContext taskContext) {
        Map<String, Serializable> variables = new HashMap<>();

        // job variables from workflow definition
        if (taskContext.getInitializer().getJobVariables() != null) {
            for (JobVariable jobVariable : taskContext.getInitializer().getJobVariables().values()) {
                variables.put(jobVariable.getName(), jobVariable.getValue());
            }
        }
        return variables;
    }

    /**
     * Extract variables from the task result, useful for composing post script actions with variable values.
     *
     * @param taskResult object that contains it all.
     *
     * @return Map with variables or empty hash if taskResult is null or empty.
     *
     * @throws IOException might be triggered during deserialization
     * @throws ClassNotFoundException might be triggered during deserialization
     */
    private Map<String, Serializable> extractTaskResultVariables(TaskResult taskResult)
            throws IOException, ClassNotFoundException {
        HashMap<String, Serializable> variables = new HashMap<>();
        if (taskResult != null && taskResult.getPropagatedVariables() != null) {
            variables.putAll(SerializationUtil.deserializeVariableMap(taskResult.getPropagatedVariables()));
        }
        return variables;
    }

    /**
     * Extract default scheduller context variables, for a complete list see the documentation.
     * https://doc.activeeon.com/latest/user/ProActiveUserGuide.html#_proactive_system_variables
     *
     * @param taskContext object that contains job information to extract the desired variables.
     * @return map containing variables with values set.
     */
    private Map<String, Serializable> extractSystemVariables(TaskContext taskContext, String nodesFile) {
        TaskLauncherInitializer initializer = taskContext.getInitializer();

        Map<String, Serializable> variables = new HashMap<>();

        variables.put(SchedulerVars.PA_JOB_ID.toString(), initializer.getTaskId().getJobId().value());
        variables.put(SchedulerVars.PA_JOB_NAME.toString(), initializer.getTaskId().getJobId().getReadableName());
        variables.put(SchedulerVars.PA_TASK_ID.toString(), initializer.getTaskId().value());
        variables.put(SchedulerVars.PA_TASK_NAME.toString(), initializer.getTaskId().getReadableName());
        variables.put(SchedulerVars.PA_TASK_ITERATION.toString(), initializer.getIterationIndex());
        variables.put(SchedulerVars.PA_TASK_REPLICATION.toString(), initializer.getReplicationIndex());
        variables.put(SchedulerVars.PA_TASK_PROGRESS_FILE.toString(), taskContext.getProgressFilePath());
        variables.put(SchedulerVars.PA_SCHEDULER_HOME.toString(), taskContext.getSchedulerHome());
        variables.put(SchedulerVars.PA_USER.toString(), initializer.getJobOwner());
        variables.put(SchedulerVars.PA_NODESFILE.toString(), nodesFile);
        variables.put(SchedulerVars.PA_NODESNUMBER.toString(), taskContext.getOtherNodesURLs().size() + 1);
        variables.put(SchedulerVars.PA_SCHEDULER_REST_URL.toString(), initializer.getSchedulerRestUrl());
        variables.put(SchedulerVars.PA_CATALOG_REST_URL.toString(), initializer.getCatalogRestUrl());

        return variables;
    }

    /**
     * Extract variables from the previous task result to be used now.
     *
     * @param taskContext contains the information needed to extract.
     *
     * @return a map containing extracted variables or an empty hash if there are no variables or previous tasks.
     *
     * @throws IOException might be triggered during deserialization
     * @throws ClassNotFoundException might be triggered during deserialization
     */
    private Map<String, Serializable> extractInheritedVariables(TaskContext taskContext)
            throws IOException, ClassNotFoundException {
        Map<String, Serializable> variables = new HashMap<>();
        if (taskContext.getPreviousTasksResults() != null) {
            for (TaskResult previousTaskResult : taskContext.getPreviousTasksResults()) {
                if (previousTaskResult.getPropagatedVariables() != null) {
                    variables.putAll(SerializationUtil.deserializeVariableMap(previousTaskResult.getPropagatedVariables()));
                }
            }
        }
        return variables;
    }

    /**
     * Extract variables from the previous task result to be used now.
     *
     * @param taskContext contains the information needed to extract.
     *
     * @return a map containing extracted variables or an empty hash if there are no variables.
     */
    private Map<String, Serializable> extractTaskVariables(TaskContext taskContext)
            throws IOException, ClassNotFoundException {
        Map<String, Serializable> variables = new HashMap<>();

        for (TaskVariable taskVariable : taskContext.getInitializer().getTaskVariables().values()) {
            //ignore inherited variables
            if (!taskVariable.isJobInherited()) {
                variables.put(taskVariable.getName(), taskVariable.getValue());
            }
        }
        return variables;
    }
}
