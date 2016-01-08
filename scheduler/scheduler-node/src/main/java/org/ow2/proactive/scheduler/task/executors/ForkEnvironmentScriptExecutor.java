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

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextVariableExtractor;
import org.ow2.proactive.scheduler.task.executors.forked.env.ForkedTaskVariablesManager;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.jetbrains.annotations.NotNull;

public class ForkEnvironmentScriptExecutor implements Serializable {
    private final ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();
    private final TaskContextVariableExtractor taskContextVariableExtractor = new TaskContextVariableExtractor();


    public ScriptResult executeForkEnvironmentScript(TaskContext context,PrintStream outputSink,
            PrintStream errorSink) throws Exception {

        ForkEnvironment forkEnvironment = context.getInitializer().getForkEnvironment();
        Map<String, Serializable> variables = taskContextVariableExtractor.extractTaskVariables(context);
        Map<String, String> thirdPartyCredentials = forkedTaskVariablesManager.extractThirdPartyCredentials(context);
        ScriptResult scriptResult;
        ScriptHandler scriptHandler = ScriptLoader.createLocalHandler();
        Script<?> script = forkEnvironment.getEnvScript();

        forkedTaskVariablesManager
                .addBindingsToScriptHandler(scriptHandler,
                        context,
                        variables,
                        thirdPartyCredentials);

        forkedTaskVariablesManager.replaceScriptParameters(script,
                thirdPartyCredentials,
                variables,
                errorSink);

        scriptResult = scriptHandler.handle(script,
                outputSink,
                errorSink);

        if (scriptResult.errorOccured()) {
            throw new Exception("Failed to execute fork environment script",
                    scriptResult.getException());
        }

        return scriptResult;
    }
}
