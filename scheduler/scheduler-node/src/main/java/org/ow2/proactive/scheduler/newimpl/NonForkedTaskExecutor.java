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

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskLauncherBak;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.utils.ClasspathUtils;
import org.ow2.proactive.utils.NodeSet;


public class NonForkedTaskExecutor implements TaskExecutor {

    @Override
    public TaskResultImpl execute(TaskContext container, PrintStream output, PrintStream error) {
        ScriptHandler scriptHandler = ScriptLoader.createLocalHandler();

        Map<String, Serializable> variables = new HashMap<String, Serializable>();

        // variables from workflow definition
        if (container.getInitializer().getVariables() != null) {
            variables.putAll(container.getInitializer().getVariables());
        }

        List<TaskResult> results = new ArrayList<TaskResult>();
        try {
            // variables from dependent tasks
            addResultsAndVariablesFromResults(container.getPreviousTasksResults(), variables, results);
        } catch (Exception e) {
            e.printStackTrace(error);
            return new TaskResultImpl(container.getTaskId(), new Exception("Could deserialize variables", e),
                    null, 0);
        }

        // variables from current job/task context
        variables.putAll(contextVariables(container.getInitializer()));

        for (String v : new String[]{"proactive.home", "pa.scheduler.home", "pa.rm.home"}) {
            variables.put(v, ClasspathUtils.findSchedulerHome());
        }

        Map<String, String> thirdPartyCredentials = new HashMap<String, String>();
        try {
            if (container.getDecrypter() != null) {
                thirdPartyCredentials.putAll(container.getDecrypter().decrypt().getThirdPartyCredentials());
            }
        } catch (Exception e) {
            e.printStackTrace(error);
            return new TaskResultImpl(container.getTaskId(), new Exception(
                    "Could read encrypted third party credentials", e), null, 0);
        }

        scriptHandler.addBinding(TaskScript.RESULTS_VARIABLE, results.toArray(new TaskResult[results.size()]));
        scriptHandler.addBinding(TaskScript.CREDENTIALS_VARIABLE, thirdPartyCredentials);
        scriptHandler.addBinding(TaskLauncherBak.VARIABLES_BINDING_NAME, variables);

        scriptHandler.addBinding(TaskLauncherBak.DS_SCRATCH_BINDING_NAME, container.getScratchURI());
        scriptHandler.addBinding(TaskLauncherBak.DS_INPUT_BINDING_NAME, container.getInputURI());
        scriptHandler.addBinding(TaskLauncherBak.DS_OUTPUT_BINDING_NAME, container.getOutputURI());
        scriptHandler.addBinding(TaskLauncherBak.DS_GLOBAL_BINDING_NAME, container.getGlobalURI());
        scriptHandler.addBinding(TaskLauncherBak.DS_USER_BINDING_NAME, container.getUserURI());

        Set<String> nodesUrls = container.getNodesURLs();
        scriptHandler.addBinding(TaskLauncherBak.MULTI_NODE_TASK_NODESET_BINDING_NAME,
                nodesUrls);
        scriptHandler.addBinding(TaskLauncherBak.MULTI_NODE_TASK_NODESURL_BINDING_NAME, nodesUrls);

        StopWatch stopWatch = new StopWatch();
        TaskResultImpl taskResult;
        try {
            stopWatch.start();
            Serializable result = executeScripts(container, output, error, scriptHandler);
            taskResult = new TaskResultImpl(container.getTaskId(), result, null, stopWatch.stop());
        } catch (Throwable e) {
            e.printStackTrace(error);
            taskResult = new TaskResultImpl(container.getTaskId(), e, null, stopWatch.stop());
        }

        executeFlowScript(container.getControlFlowScript(), scriptHandler, output, error, taskResult);

        taskResult.setPropagatedVariables(SerializationUtil.serializeVariableMap(variables));
        return taskResult;
    }

    private Set<String> getNodesURLs(TaskContext container) {
        Set<String> nodesURLs = new HashSet<String>();
        for (Node node : container.getExecutableContainer().getNodes()) {
            nodesURLs.add(node.getNodeInformation().getURL());
        }
        return nodesURLs;
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
        variables.put(SchedulerVars.JAVAENV_TASK_ITERATION.toString(), initializer.getTaskId().getIterationIndex());
        variables.put(SchedulerVars.JAVAENV_TASK_REPLICATION.toString(), initializer.getTaskId().getReplicationIndex());
        //        variables.put(PASchedulerProperties.SCHEDULER_HOME.getKey(),
        //                CentralPAPropertyRepository.PA_HOME.getValue());
        //        variables.put(PAResourceManagerProperties.RM_HOME.getKey(),
        //                PAResourceManagerProperties.RM_HOME.getValueAsString());
        //        variables.put(CentralPAPropertyRepository.PA_HOME.getName(),
        //                CentralPAPropertyRepository.PA_HOME.getValueAsString());
        return variables;
    }

    private void addResultsAndVariablesFromResults(TaskResult[] previousTasksResults,
                                                   Map<String, Serializable> variables, List<TaskResult> results) throws IOException,
            ClassNotFoundException {
        if (previousTasksResults != null) {
            for (TaskResult taskResult : previousTasksResults) {
                if (taskResult.getPropagatedVariables() != null) {
                    variables.putAll(SerializationUtil.deserializeVariableMap(taskResult
                            .getPropagatedVariables()));
                }
                results.add(taskResult);
            }
        }
    }

    private Serializable executeScripts(TaskContext container, PrintStream output, PrintStream error,
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

//        if(container.getExecutableContainer() instanceof ForkedScriptExecutableContainer) {

        ForkedScriptExecutableContainer executableContainer = (ForkedScriptExecutableContainer) container
                .getExecutableContainer();

        ScriptResult<Serializable> scriptResult = scriptHandler.handle(executableContainer.getScript(), output, error);

        if (scriptResult.errorOccured()) {
            throw new Exception("Failed to execute task: " + scriptResult.getException().getMessage(), scriptResult.getException());
        }

        return scriptResult.getResult();
//        }
//        else {
//
//            JavaExecutableContainer javaContainer = (JavaExecutableContainer) container
//              .getExecutableContainer();
//
//            AbstractJavaExecutable javaExecutable = (AbstractJavaExecutable) javaContainer.getExecutable();
//
//            JavaExecutableInitializerImpl initializer = javaContainer.createExecutableInitializer();
//            initializer.setOutputSink(output);
//            initializer.setErrorSink(error);
//            initializer.setPropagatedVariables(new HashMap<String, byte[]>());
//            // TODO probably more things
//            javaExecutable.internalInit(initializer);
//
//            try {
//                return javaExecutable.execute(new TaskResult[0]);
//            } catch (Throwable throwable) {
//                throw new Exception("Failed to execute Java task", throwable);
//            }
//
//            //return scriptHandler.handle(javaContainer.getScript(), output, error);
//
//        }
    }

    private void executeFlowScript(Script<FlowAction> flowScript, ScriptHandler scriptHandler,
                                   PrintStream output, PrintStream error, TaskResultImpl taskResult) {
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
            } else {
                taskResult.setAction(flowScriptResult.getResult());
            }
        }
    }

}
