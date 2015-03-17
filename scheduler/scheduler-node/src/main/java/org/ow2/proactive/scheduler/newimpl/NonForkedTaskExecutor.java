/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
package org.ow2.proactive.scheduler.newimpl;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.newimpl.utils.StopWatch;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.script.ScriptExecutableContainer;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.utils.ClasspathUtils;


/**
 * Run a task through a script handler.
 * Responsible for:
 *  - running the different scripts
 *  - variable propagation
 *  - getting the task result or user code exceptions
 */
public class NonForkedTaskExecutor implements TaskExecutor {

    public static final String DS_SCRATCH_BINDING_NAME = "localspace";
    public static final String DS_INPUT_BINDING_NAME = "input";
    public static final String DS_OUTPUT_BINDING_NAME = "output";
    public static final String DS_GLOBAL_BINDING_NAME = "global";
    public static final String DS_USER_BINDING_NAME = "user";

    public static final String MULTI_NODE_TASK_NODESET_BINDING_NAME = "nodeset";
    public static final String MULTI_NODE_TASK_NODESURL_BINDING_NAME = "nodesurl";

    public static final String VARIABLES_BINDING_NAME = "variables";

    /**
     * Will be replaced by the matching third-party credential
     * Example: if one of the third-party credentials' key-value pairs is 'foo:bar',
     * then '$CREDENTIALS_foo' will be replaced by 'bar' in the arguments of the tasks.
     */
    public static final String CREDENTIALS_KEY_PREFIX = "$CREDENTIALS_";

    @Override
    public TaskResultImpl execute(TaskContext container, PrintStream output, PrintStream error) {
        ScriptHandler scriptHandler = ScriptLoader.createLocalHandler();

        try {
            Map<String, Serializable> variables = taskVariables(container);
            scriptHandler.addBinding(VARIABLES_BINDING_NAME, variables);

            TaskResult[] results = tasksResults(container);
            scriptHandler.addBinding(TaskScript.RESULTS_VARIABLE, results);

            Map<String, String> thirdPartyCredentials = thirdPartyCredentials(container);
            scriptHandler.addBinding(TaskScript.CREDENTIALS_VARIABLE, thirdPartyCredentials);

            scriptHandler.addBinding(DS_SCRATCH_BINDING_NAME, container.getScratchURI());
            scriptHandler.addBinding(DS_INPUT_BINDING_NAME, container.getInputURI());
            scriptHandler.addBinding(DS_OUTPUT_BINDING_NAME, container.getOutputURI());
            scriptHandler.addBinding(DS_GLOBAL_BINDING_NAME, container.getGlobalURI());
            scriptHandler.addBinding(DS_USER_BINDING_NAME, container.getUserURI());

            Set<String> nodesUrls = container.getNodesURLs();
            scriptHandler.addBinding(MULTI_NODE_TASK_NODESET_BINDING_NAME, nodesUrls);
            scriptHandler.addBinding(MULTI_NODE_TASK_NODESURL_BINDING_NAME, nodesUrls);

            replaceScriptParameters(container, thirdPartyCredentials);

            StopWatch stopWatch = new StopWatch();
            TaskResultImpl taskResult;
            try {
                stopWatch.start();
                Serializable result = execute(container, output, error, scriptHandler);
                taskResult = new TaskResultImpl(container.getTaskId(), result, null, stopWatch.stop());
            } catch (Throwable e) {
                e.printStackTrace(error);
                taskResult = new TaskResultImpl(container.getTaskId(), e, null, stopWatch.stop());
            }

            executeFlowScript(container.getControlFlowScript(), scriptHandler, output, error, taskResult);

            taskResult.setPropagatedVariables(SerializationUtil.serializeVariableMap(variables));

            return taskResult;
        } catch (Throwable e) {
            e.printStackTrace(error);
            return new TaskResultImpl(container.getTaskId(), e);
        }
    }

    private Map<String, Serializable> taskVariables(TaskContext container) throws Exception {
        Map<String, Serializable> variables = new HashMap<String, Serializable>();

        // variables from workflow definition
        if (container.getInitializer().getVariables() != null) {
            variables.putAll(container.getInitializer().getVariables());
        }

        // variables from previous tasks
        try {
            if (container.getPreviousTasksResults() != null) {
                for (TaskResult taskResult : container.getPreviousTasksResults()) {
                    if (taskResult.getPropagatedVariables() != null) {
                        variables.putAll(SerializationUtil.deserializeVariableMap(taskResult
                          .getPropagatedVariables()));
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Could deserialize variables", e);
        }

        // variables from current job/task context
        variables.putAll(contextVariables(container.getInitializer()));

        // ProActive homes
        for (String variableName : new String[] { CentralPAPropertyRepository.PA_HOME.getName(),
          PASchedulerProperties.SCHEDULER_HOME.getKey(), PAResourceManagerProperties.RM_HOME.getKey() }) {
            variables.put(variableName, ClasspathUtils.findSchedulerHome());
        }
        return variables;
    }

    private TaskResult[] tasksResults(TaskContext container) throws Exception {
        TaskResult[] previousTasksResults = container.getPreviousTasksResults();
        if (previousTasksResults != null) {
            return previousTasksResults;
        } else {
            return new TaskResult[0];
        }
    }

    private Map<String, Serializable> contextVariables(TaskLauncherInitializer initializer) {
        Map<String, Serializable> variables = new HashMap<String, Serializable>();
        variables.put(SchedulerVars.JAVAENV_JOB_ID_VARNAME.toString(), initializer.getTaskId().getJobId()
          .value());
        variables.put(SchedulerVars.JAVAENV_JOB_NAME_VARNAME.toString(), initializer.getTaskId().getJobId()
          .getReadableName());
        variables.put(SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString(), initializer.getTaskId().value());
        variables.put(SchedulerVars.JAVAENV_TASK_NAME_VARNAME.toString(), initializer.getTaskId()
          .getReadableName());
        variables.put(SchedulerVars.JAVAENV_TASK_ITERATION.toString(), initializer.getIterationIndex());
        variables.put(SchedulerVars.JAVAENV_TASK_REPLICATION.toString(), initializer.getReplicationIndex());
        return variables;
    }

    private Map<String, String> thirdPartyCredentials(TaskContext container) throws Exception {
        try {
            Map<String, String> thirdPartyCredentials = new HashMap<String, String>();
            if (container.getDecrypter() != null) {
                thirdPartyCredentials.putAll(container.getDecrypter().decrypt().getThirdPartyCredentials());
            }
            return thirdPartyCredentials;
        } catch (Exception e) {
            throw new Exception("Could read encrypted third party credentials", e);
        }
    }

    private void replaceScriptParameters(TaskContext container, Map<String, String> thirdPartyCredentials) {
        // parameters replacement

        Map<String, String> replacements = new HashMap<String, String>();
        for (Map.Entry<String, String> credentialEntry : thirdPartyCredentials.entrySet()) {
            replacements.put(CREDENTIALS_KEY_PREFIX + credentialEntry.getKey(),
              credentialEntry.getValue());
        }

        // TODO should be done after script is executed (like if pre changes on replacement, should it be resolved after)

        Script<Serializable> taskScript;
        if (container.getExecutableContainer() instanceof ScriptExecutableContainer) {
            taskScript = ((ScriptExecutableContainer) container.getExecutableContainer()).getScript();
        } else {
            taskScript = ((ForkedScriptExecutableContainer) container.getExecutableContainer())
              .getScript();
        }
        Script[] scripts = new Script[] { container.getPreScript(), container.getPostScript(),
          taskScript, container.getControlFlowScript() };

        for (Script script : scripts) {
            if (script != null) {
                Serializable[] args;
                // TODO deal with java task
                //                if(script.getEngineName().equals("java")){
                //                    // args = (Map<>) script.getParameters()[0];
                //                } else {
                args = script.getParameters();
                //                }
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof String) {
                            args[i] = replace((String) args[i], replacements);
                        }
                    }
                }
            }
        }
    }

    private static String replace(String input, Map<String, String> replacements) {
        String output = input;
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            output = output.replace(replacement.getKey(), replacement.getValue());
        }
        return output;
    }

    private Serializable execute(TaskContext container, PrintStream output, PrintStream error,
      ScriptHandler scriptHandler) throws Exception {
        if (container.getPreScript() != null) {
            ScriptResult preScriptResult = scriptHandler.handle(container.getPreScript(), output, error);
            if (preScriptResult.errorOccured()) {
                throw new Exception("Failed to execute pre script", preScriptResult.getException());
            }
        }

        Serializable scriptResult = executeTask(container, output, error, scriptHandler);

        if (container.getPostScript() != null) {
            ScriptResult postScriptResult = scriptHandler.handle(container.getPostScript(), output, error);
            if (postScriptResult.errorOccured()) {
                throw new Exception("Failed to execute post script", postScriptResult.getException());
            }
        }
        return scriptResult;
    }

    protected Serializable executeTask(TaskContext container, PrintStream output, PrintStream error,
            ScriptHandler scriptHandler) throws Exception {

        Script<Serializable> script;
        if (container.getExecutableContainer() instanceof ScriptExecutableContainer) {
            script = ((ScriptExecutableContainer) container.getExecutableContainer()).getScript();
        } else {
            script = ((ForkedScriptExecutableContainer) container.getExecutableContainer()).getScript();
        }
        ScriptResult<Serializable> scriptResult = scriptHandler.handle(script, output, error);

        if (scriptResult.errorOccured()) {
            throw new Exception("Failed to execute task: " + scriptResult.getException().getMessage(),
                scriptResult.getException());
        }

        return scriptResult.getResult();
    }

    private void executeFlowScript(FlowScript flowScript, ScriptHandler scriptHandler, PrintStream output,
            PrintStream error, TaskResultImpl taskResult) {
        if (flowScript != null) {
            try {
                scriptHandler.addBinding(FlowScript.resultVariable, taskResult.value());
            } catch (Throwable throwable) {
                scriptHandler.addBinding(FlowScript.resultVariable, throwable);
            }
            ScriptResult<FlowAction> flowScriptResult = scriptHandler.handle(flowScript, output, error);
            if (flowScriptResult.errorOccured()) {
                flowScriptResult.getException().printStackTrace(error);
                taskResult.setException(flowScriptResult.getException());
                taskResult.setAction(FlowAction.getDefaultAction(flowScript));
            } else {
                taskResult.setAction(flowScriptResult.getResult());
            }
        }
    }

}
