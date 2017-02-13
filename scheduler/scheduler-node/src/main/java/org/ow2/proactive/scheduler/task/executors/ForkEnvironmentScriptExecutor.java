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

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.dataspaces.RemoteSpace;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient;
import org.ow2.proactive.scheduler.task.client.SchedulerNodeClient;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextVariableExtractor;
import org.ow2.proactive.scheduler.task.executors.forked.env.ForkedTaskVariablesManager;
import org.ow2.proactive.scheduler.task.utils.VariablesMap;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;


public class ForkEnvironmentScriptExecutor implements Serializable {
    private final ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();

    private final TaskContextVariableExtractor taskContextVariableExtractor = new TaskContextVariableExtractor();

    /**
     * Executes a fork environment script.
     *
     * @param context    The task context to execute the script in.
     * @param outputSink Standard output sink.
     * @param errorSink  Error output sink.
     * @return Returns a ScriptResult.
     * @throws Exception
     */
    public ScriptResult executeForkEnvironmentScript(TaskContext context, PrintStream outputSink, PrintStream errorSink)
            throws Exception {

        VariablesMap variables = new VariablesMap();
        variables.setInheritedMap(taskContextVariableExtractor.extractVariables(context, false));
        variables.setScopeMap(taskContextVariableExtractor.extractScopeVariables(context));
        Map<String, String> thirdPartyCredentials = forkedTaskVariablesManager.extractThirdPartyCredentials(context);
        ScriptHandler scriptHandler = ScriptLoader.createLocalHandler();
        Script<?> script = context.getInitializer().getForkEnvironment().getEnvScript();

        SchedulerNodeClient schedulerNodeClient = forkedTaskVariablesManager.createSchedulerNodeClient(context);
        RemoteSpace userSpaceClient = forkedTaskVariablesManager.createDataSpaceNodeClient(context,
                                                                                           schedulerNodeClient,
                                                                                           IDataSpaceClient.Dataspace.USER);
        RemoteSpace globalSpaceClient = forkedTaskVariablesManager.createDataSpaceNodeClient(context,
                                                                                             schedulerNodeClient,
                                                                                             IDataSpaceClient.Dataspace.GLOBAL);

        forkedTaskVariablesManager.addBindingsToScriptHandler(scriptHandler,
                                                              context,
                                                              variables,
                                                              thirdPartyCredentials,
                                                              schedulerNodeClient,
                                                              userSpaceClient,
                                                              globalSpaceClient,
                                                              Collections.<String, String> emptyMap());

        forkedTaskVariablesManager.replaceScriptParameters(script, thirdPartyCredentials, variables, errorSink);

        ScriptResult scriptResult = scriptHandler.handle(script, outputSink, errorSink);

        if (scriptResult.errorOccured()) {
            throw new Exception("Failed to execute fork environment script", scriptResult.getException());
        }

        return scriptResult;
    }
}
