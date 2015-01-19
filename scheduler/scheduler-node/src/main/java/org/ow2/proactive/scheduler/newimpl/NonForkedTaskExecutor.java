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

import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskLauncherBak;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.TaskScript;


public class NonForkedTaskExecutor implements TaskExecutor {

    @Override
    public TaskResultImpl execute(TaskContext container, PrintStream output, PrintStream error) {
        ScriptHandler scriptHandler = ScriptLoader.createLocalHandler();

        Map<String, Serializable> variables = contextVariables(container.getInitializer());
        if (container.getInitializer().getVariables() != null) {
            variables.putAll(container.getInitializer().getVariables());
        }

        if (container.getDecrypter() != null) {
            try {
                Map<String, String> thirdPartyCredentials = container.getDecrypter().decrypt()
                        .getThirdPartyCredentials();
                scriptHandler.addBinding(TaskScript.CREDENTIALS_VARIABLE, thirdPartyCredentials);

            } catch (Exception e) {
                e.printStackTrace(error);
                return new TaskResultImpl(container.getTaskId(), new Exception(
                    "Could read encrypted third party credentials", e), null, 0);
            }
        }
        scriptHandler.addBinding(TaskLauncherBak.VARIABLES_BINDING_NAME, variables);

        if (container.getPreScript() != null) {
            ScriptResult preScriptResult = scriptHandler.handle(container.getPreScript(), output, error);
            if (preScriptResult.errorOccured()) {
                preScriptResult.getException().printStackTrace(error);
                return new TaskResultImpl(container.getTaskId(), preScriptResult.getException(), null, 0);
            }
        }

        ScriptResult<Serializable> scriptResult = scriptHandler.handle(container.getExecutableContainer()
                .getScript(), output, error);

        if (scriptResult.errorOccured()) {
            scriptResult.getException().printStackTrace(error);
            return new TaskResultImpl(container.getTaskId(), scriptResult.getException(), null, 0);
        }
        TaskResultImpl taskResult = new TaskResultImpl(container.getTaskId(), scriptResult.getResult(), null,
            0);

        if (container.getPostScript() != null) {
            ScriptResult postScriptResult = scriptHandler.handle(container.getPostScript(), output, error);
            if (postScriptResult.errorOccured()) {
                postScriptResult.getException().printStackTrace(error);
                return new TaskResultImpl(container.getTaskId(), postScriptResult.getException(), null, 0);
            }
        }

        if (container.getControlFlowScript() != null) {
            ScriptResult<FlowAction> flowScriptResult = scriptHandler.handle(
                    container.getControlFlowScript(), output, error);
            if (flowScriptResult.errorOccured()) {
                flowScriptResult.getException().printStackTrace(error);
                return new TaskResultImpl(container.getTaskId(), flowScriptResult.getException(), null, 0);
            } else {
                taskResult.setAction(flowScriptResult.getResult());
            }
        }

        taskResult.setPropagatedVariables(SerializationUtil.serializeVariableMap(variables));
        return taskResult;
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
        variables.put(SchedulerVars.JAVAENV_TASK_ITERATION.toString(),
                String.valueOf(initializer.getIterationIndex()));
        variables.put(SchedulerVars.JAVAENV_TASK_REPLICATION.toString(),
                String.valueOf(initializer.getReplicationIndex()));
        //        variables.put(PASchedulerProperties.SCHEDULER_HOME.getKey(),
        //                CentralPAPropertyRepository.PA_HOME.getValue());
        //        variables.put(PAResourceManagerProperties.RM_HOME.getKey(),
        //                PAResourceManagerProperties.RM_HOME.getValueAsString());
        //        variables.put(CentralPAPropertyRepository.PA_HOME.getName(),
        //                CentralPAPropertyRepository.PA_HOME.getValueAsString());
        return variables;
    }

}
