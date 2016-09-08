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
package org.ow2.proactive.scheduler.task.executors;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.FileUtils;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.client.SchedulerNodeClient;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextVariableExtractor;
import org.ow2.proactive.scheduler.task.exceptions.TaskException;
import org.ow2.proactive.scheduler.task.executors.forked.env.ForkedTaskVariablesManager;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.PAProperties;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Run a task through a script handler.
 * <p/>
 * Responsible for:
 * - running the different scripts
 * - variable propagation
 * - replacement for variables
 * - getting the task result or user code exceptions
 */
public class InProcessTaskExecutor implements TaskExecutor {

    private static final String NODES_FILE_DIRECTORY_NAME = ".pa_nodes";
    private final ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();
    private final TaskContextVariableExtractor taskContextVariableExtractor = new TaskContextVariableExtractor();

    /**
     * Writes a nodes file to disk.
     *
     * @param taskContext The task taskContext from which to extract the nodes file from.
     * @return The absolute path of the nodes file.
     * @throws IOException
     */
    private static String writeNodesFile(TaskContext taskContext) throws IOException {
        List<String> nodesHosts = taskContext.getNodesHosts();

        if (nodesHosts.isEmpty()) {
            return "";
        } else {
            File directory;
            if (taskContext.getNodeDataSpaceURIs().getScratchURI() == null || taskContext.getNodeDataSpaceURIs().getScratchURI().isEmpty()) {
                directory = new File(".");
            } else {
                directory = new File(taskContext.getNodeDataSpaceURIs().getScratchURI());
            }
            File nodesFile = File.createTempFile(NODES_FILE_DIRECTORY_NAME, null, directory);

            Writer outputWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(nodesFile),
                    PAProperties.getFileEncoding()));
            for (String nodeHost : nodesHosts) {
                outputWriter.append(nodeHost).append(System.lineSeparator());
            }
            outputWriter.close();

            return nodesFile.getAbsolutePath();
        }
    }

    /**
     * Executes a task inside a task context.
     *
     * @param taskContext Task context to execute.
     * @param output      Standard output sink.
     * @param error       Error sink.
     * @return Returns the task result.
     */
    @Override
    public TaskResultImpl execute(TaskContext taskContext, PrintStream output, PrintStream error) {
        ScriptHandler scriptHandler = ScriptLoader.createLocalHandler();
        String nodesFile = null;
        SchedulerNodeClient client = null;
        try {
            nodesFile = writeNodesFile(taskContext);
            Map<String, Serializable> variables = taskContextVariableExtractor.extractTaskVariables(
                    taskContext,
                    nodesFile);
            Map<String, String> thirdPartyCredentials = forkedTaskVariablesManager.extractThirdPartyCredentials(
                    taskContext);
            client = forkedTaskVariablesManager.createSchedulerNodeClient(taskContext);

            forkedTaskVariablesManager.addBindingsToScriptHandler(scriptHandler, taskContext, variables,
                    thirdPartyCredentials, client);

            Stopwatch stopwatch = Stopwatch.createUnstarted();
            TaskResultImpl taskResult;
            try {
                stopwatch.start();
                Serializable result = execute(taskContext, output, error, scriptHandler,
                        thirdPartyCredentials,
                        variables);
                stopwatch.stop();
                taskResult = new TaskResultImpl(
                        taskContext.getTaskId(), result, null, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            } catch (Throwable e) {
                stopwatch.stop();
                e.printStackTrace(error);
                taskResult = new TaskResultImpl(
                        taskContext.getTaskId(), e, null, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }

            executeFlowScript(taskContext.getControlFlowScript(), scriptHandler, output, error, taskResult);

            taskResult.setPropagatedVariables(SerializationUtil.serializeVariableMap(variables));

            return taskResult;
        } catch (Throwable e) {
            e.printStackTrace(error);
            return new TaskResultImpl(taskContext.getTaskId(), e);
        } finally {
            if (nodesFile != null && !nodesFile.isEmpty()) {
                FileUtils.deleteQuietly(new File(nodesFile));
            }
        }
    }

    /**
     * Executes a task.
     *
     * @param taskContext           Task context to execute
     * @param output                Standard output sink.
     * @param error                 Error sink.
     * @param scriptHandler
     * @param thirdPartyCredentials
     * @param variables             Environment variables.
     * @return The result of the executed script.
     * @throws Throwable
     */
    private Serializable execute(TaskContext taskContext, PrintStream output, PrintStream error,
            ScriptHandler scriptHandler, Map<String, String> thirdPartyCredentials,
            Map<String, Serializable> variables) throws Throwable {
        if (taskContext.getPreScript() != null) {
            Script<?> script = taskContext.getPreScript();
            forkedTaskVariablesManager.replaceScriptParameters(script, thirdPartyCredentials, variables,
                    error);
            ScriptResult preScriptResult = scriptHandler.handle(script, output, error);
            if (preScriptResult.errorOccured()) {
                throw new TaskException("Failed to execute pre script: " +
                        preScriptResult.getException().getMessage(), preScriptResult.getException());
            }
        }

        Script<Serializable> script = ((ScriptExecutableContainer) taskContext.getExecutableContainer())
                .getScript();
        forkedTaskVariablesManager.replaceScriptParameters(script, thirdPartyCredentials, variables, error);
        ScriptResult<Serializable> scriptResult = scriptHandler.handle(script, output, error);

        if (scriptResult.errorOccured()) {
            throw new TaskException("Failed to execute task: " + scriptResult.getException().getMessage(),
                    scriptResult.getException());
        }

        if (taskContext.getPostScript() != null) {
            forkedTaskVariablesManager.replaceScriptParameters(taskContext.getPostScript(),
                    thirdPartyCredentials, variables, error);
            ScriptResult postScriptResult = scriptHandler.handle(taskContext.getPostScript(), output, error);
            if (postScriptResult.errorOccured()) {
                throw new TaskException("Failed to execute post script: " +
                        postScriptResult.getException().getMessage(), postScriptResult.getException());
            }
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
                error.println(flowScriptResult.getException().getMessage());
                taskResult.setException(flowScriptResult.getException());
                taskResult.setAction(FlowAction.getDefaultAction(flowScript));
            } else {
                taskResult.setAction(flowScriptResult.getResult());
            }
        }
    }

}
