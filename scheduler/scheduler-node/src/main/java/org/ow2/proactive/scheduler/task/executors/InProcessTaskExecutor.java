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
package org.ow2.proactive.scheduler.task.executors;

import static org.ow2.proactive.scheduler.common.task.ForkEnvironment.DOCKER_FORK_WINDOWS2LINUX;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive.resourcemanager.task.client.RMNodeClient;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.dataspaces.RemoteSpace;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.client.SchedulerNodeClient;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextVariableExtractor;
import org.ow2.proactive.scheduler.task.exceptions.TaskException;
import org.ow2.proactive.scheduler.task.executors.forked.env.ForkedTaskVariablesManager;
import org.ow2.proactive.scheduler.task.utils.VariablesMap;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.PAProperties;

import com.google.common.base.Stopwatch;


/**
 * Run a task through a script handler.
 * <p>
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

    private final static boolean isDockerWindows2Linux = "true".equals(System.getProperty(DOCKER_FORK_WINDOWS2LINUX));

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
            if (taskContext.getNodeDataSpaceURIs().getScratchURI() == null ||
                taskContext.getNodeDataSpaceURIs().getScratchURI().isEmpty()) {
                directory = new File(".");
            } else {
                String scratchUri = taskContext.getNodeDataSpaceURIs().getScratchURI();
                directory = new File(isDockerWindows2Linux ? ForkEnvironment.convertToLinuxPath(scratchUri)
                                                           : scratchUri);
            }
            File nodesFile = new File(directory, NODES_FILE_DIRECTORY_NAME + "_" + taskContext.getTaskId());

            try (Writer outputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nodesFile),
                                                                                 PAProperties.getFileEncoding()))) {
                for (String nodeHost : nodesHosts) {
                    outputWriter.append(nodeHost).append(System.lineSeparator());
                }
            }

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
        SchedulerNodeClient schedulerNodeClient = null;
        RMNodeClient rmNodeClient = null;
        RemoteSpace userSpaceClient = null;
        RemoteSpace globalSpaceClient = null;
        try {
            nodesFile = writeNodesFile(taskContext);
            VariablesMap variables = new VariablesMap();
            variables.setInheritedMap(taskContextVariableExtractor.getAllNonTaskVariablesInjectNodesFile(taskContext,
                                                                                                         nodesFile));
            variables.setScopeMap(taskContextVariableExtractor.getScopeVariables(taskContext));
            Map<String, String> resultMetadata = new HashMap<>();
            Map<String, Serializable> resultMap = new HashMap<>();
            Map<String, String> thirdPartyCredentials = forkedTaskVariablesManager.extractThirdPartyCredentials(taskContext);
            schedulerNodeClient = forkedTaskVariablesManager.createSchedulerNodeClient(taskContext);
            rmNodeClient = forkedTaskVariablesManager.createRMNodeClient(taskContext);
            userSpaceClient = forkedTaskVariablesManager.createDataSpaceNodeClient(taskContext,
                                                                                   schedulerNodeClient,
                                                                                   IDataSpaceClient.Dataspace.USER);
            globalSpaceClient = forkedTaskVariablesManager.createDataSpaceNodeClient(taskContext,
                                                                                     schedulerNodeClient,
                                                                                     IDataSpaceClient.Dataspace.GLOBAL);

            forkedTaskVariablesManager.addBindingsToScriptHandler(scriptHandler,
                                                                  taskContext,
                                                                  variables,
                                                                  resultMap,
                                                                  thirdPartyCredentials,
                                                                  schedulerNodeClient,
                                                                  rmNodeClient,
                                                                  userSpaceClient,
                                                                  globalSpaceClient,
                                                                  resultMetadata,
                                                                  output,
                                                                  error);

            Stopwatch stopwatch = Stopwatch.createUnstarted();
            TaskResultImpl taskResult;
            try {
                stopwatch.start();
                Serializable result = execute(taskContext,
                                              output,
                                              error,
                                              scriptHandler,
                                              thirdPartyCredentials,
                                              variables);
                stopwatch.stop();
                taskResult = new TaskResultImpl(taskContext.getTaskId(),
                                                result,
                                                null,
                                                stopwatch.elapsed(TimeUnit.MILLISECONDS));
            } catch (Throwable e) {
                stopwatch.stop();
                e.printStackTrace(error);
                taskResult = new TaskResultImpl(taskContext.getTaskId(),
                                                e,
                                                null,
                                                stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }

            executeFlowScript(taskContext.getControlFlowScript(), scriptHandler, output, error, taskResult);

            taskResult.setPropagatedVariables(SerializationUtil.serializeVariableMap(variables.getPropagatedVariables()));
            taskResult.setResultMap(resultMap);
            taskResult.setMetadata(resultMetadata);

            return taskResult;
        } catch (Throwable e) {
            e.printStackTrace(error);
            return new TaskResultImpl(taskContext.getTaskId(), e);
        } finally {
            if (nodesFile != null && !nodesFile.isEmpty()) {
                FileUtils.deleteQuietly(new File(nodesFile));
            }
            if (schedulerNodeClient != null && schedulerNodeClient.isConnected()) {
                try {
                    schedulerNodeClient.disconnect();
                } catch (Exception ignored) {

                }
            }

            if (rmNodeClient != null && rmNodeClient.getSession() != null) {
                try {
                    rmNodeClient.disconnect();
                } catch (Exception ignored) {

                }
            }
        }
    }

    private void saveScriptAsFile(String path, Script<?> script, TaskContext taskContext) throws Throwable {
        //If the path doesn't contain an extension, add an extension to it
        if (!path.contains(".")) {
            ScriptEngineFactory factory = new ScriptEngineManager().getEngineByName(script.getEngineName())
                                                                   .getFactory();
            String extension = factory.getExtensions().get(0);
            path = path + "." + extension;
        }

        String fetchedScript = null;

        try {
            fetchedScript = script.fetchScriptWithExceptionHandling();
        } catch (IOException e) {
            throw new TaskException("Error fetching script from url " + script.getScriptUrl(), e);
        }

        String code = fetchedScript.trim();

        Path p = Paths.get(path);

        //Check if the path is an absolute path or a relative path, if it is a relative path store the file in the local space
        boolean isAbsolute = p.isAbsolute();
        if (isAbsolute) {
            //Check if the parent path exists, if not create it
            Path hasParent = p.getParent();
            if (!Files.exists(hasParent)) {
                FileUtils.forceMkdirParent(new File(path));
            }
            try (FileWriter fw = new FileWriter(new File(path))) {
                fw.write(code);
            } catch (IOException e) {
                throw new TaskException("Error writing script as file: " + path, e);
            }
        } else {
            //Needs to be stored in the local space
            String uri = taskContext.getNodeDataSpaceURIs().getScratchURI();
            if (isDockerWindows2Linux) {
                uri = ForkEnvironment.convertToLinuxPath(uri);
            }
            p = Paths.get(uri, path);
            Path hasParent = p.getParent();
            if (!Files.exists(hasParent)) {
                FileUtils.forceMkdirParent(new File(uri, path));
            }
            try (FileWriter fw = new FileWriter(new File(uri, path))) {
                fw.write(code);
            } catch (IOException e) {
                throw new TaskException("Error writing script as file: " + path, e);
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
            ScriptHandler scriptHandler, Map<String, String> thirdPartyCredentials, VariablesMap variables)
            throws Throwable {
        if (taskContext.getPreScript() != null) {
            Script<?> script = taskContext.getPreScript();
            forkedTaskVariablesManager.replaceScriptParameters(script, thirdPartyCredentials, variables, error);
            Map<String, String> genericInfo = taskContext.getInitializer().getGenericInformation();
            if (genericInfo != null && genericInfo.containsKey("PRE_SCRIPT_AS_FILE")) {
                String path = genericInfo.get("PRE_SCRIPT_AS_FILE");
                saveScriptAsFile(path, script, taskContext);
            } else {
                ScriptResult preScriptResult = scriptHandler.handle(script, output, error);
                if (preScriptResult.errorOccured()) {
                    throw new TaskException("Failed to execute pre script: " +
                                            preScriptResult.getException().getMessage(),
                                            preScriptResult.getException());
                }
            }
        }

        Script<Serializable> script = ((ScriptExecutableContainer) taskContext.getExecutableContainer()).getScript();
        forkedTaskVariablesManager.replaceScriptParameters(script, thirdPartyCredentials, variables, error);
        ScriptResult<Serializable> scriptResult = scriptHandler.handle(script, output, error);

        if (scriptResult.errorOccured()) {
            throw new TaskException("Failed to execute task: " + scriptResult.getException().getMessage(),
                                    scriptResult.getException());
        }

        if (taskContext.getPostScript() != null) {
            forkedTaskVariablesManager.replaceScriptParameters(taskContext.getPostScript(),
                                                               thirdPartyCredentials,
                                                               variables,
                                                               error);
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
