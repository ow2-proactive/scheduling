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
package org.ow2.proactive.scheduler.task.executors.forked.env;


import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scheduler.task.client.SchedulerNodeClient;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.TaskScript;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ForkedTaskVariablesManager implements Serializable {

    /**
     * Will be replaced by the matching third-party credential
     * Example: if one of the third-party credentials' key-value pairs is 'foo:bar',
     * then '$credentials_foo' will be replaced by 'bar' in the arguments of the tasks.
     */
    public static final String CREDENTIALS_KEY_PREFIX = "credentials_";

    private static TaskResult[] tasksResults(TaskContext container) {
        TaskResult[] previousTasksResults = container.getPreviousTasksResults();
        if (previousTasksResults != null) {
            return previousTasksResults;
        } else {
            return new TaskResult[0];
        }
    }


    public void addBindingsToScriptHandler(ScriptHandler scriptHandler, TaskContext taskContext,
            Map<String, Serializable> variables, Map<String, String> thirdPartyCredentials, SchedulerNodeClient client) {
        scriptHandler.addBinding(SchedulerConstants.VARIABLES_BINDING_NAME, variables);

        scriptHandler.addBinding(SchedulerConstants.GENERIC_INFO_BINDING_NAME, taskContext.getInitializer().getGenericInformation());

        scriptHandler.addBinding(TaskScript.RESULTS_VARIABLE, tasksResults(taskContext));

        scriptHandler.addBinding(TaskScript.CREDENTIALS_VARIABLE, thirdPartyCredentials);
        if (client != null) {
            scriptHandler.addBinding(TaskScript.SCHEDULER_CLIENT_VARIABLE, client);
        }

        scriptHandler.addBinding(SchedulerConstants.DS_SCRATCH_BINDING_NAME, taskContext.getNodeDataSpaceURIs().getScratchURI());
        scriptHandler.addBinding(SchedulerConstants.DS_CACHE_BINDING_NAME, taskContext.getNodeDataSpaceURIs().getCacheURI());
        scriptHandler.addBinding(SchedulerConstants.DS_INPUT_BINDING_NAME, taskContext.getNodeDataSpaceURIs().getInputURI());
        scriptHandler.addBinding(SchedulerConstants.DS_OUTPUT_BINDING_NAME, taskContext.getNodeDataSpaceURIs().getOutputURI());
        scriptHandler.addBinding(SchedulerConstants.DS_GLOBAL_BINDING_NAME, taskContext.getNodeDataSpaceURIs().getGlobalURI());
        scriptHandler.addBinding(SchedulerConstants.DS_USER_BINDING_NAME, taskContext.getNodeDataSpaceURIs().getUserURI());

        scriptHandler.addBinding(SchedulerConstants.MULTI_NODE_TASK_NODESURL_BINDING_NAME, taskContext.getOtherNodesURLs());

        scriptHandler.addBinding(SchedulerConstants.FORK_ENVIRONMENT_BINDING_NAME,
                taskContext.getInitializer().getForkEnvironment());
    }

    public Map<String, String> extractThirdPartyCredentials(TaskContext container) throws Exception {
        try {
            Map<String, String> thirdPartyCredentials = new HashMap<>();
            if (container.getDecrypter() != null) {
                thirdPartyCredentials.putAll(container.getDecrypter().decrypt().getThirdPartyCredentials());
            }
            return thirdPartyCredentials;
        } catch (Exception e) {
            throw new Exception("Could read encrypted third party credentials", e);
        }
    }

    public SchedulerNodeClient createSchedulerNodeClient(TaskContext container) {
        if (container.getDecrypter() != null) {
            return new SchedulerNodeClient(container.getDecrypter(), container.getRestUrl());
        }
        return null;
    }

    public void replaceScriptParameters(Script script, Map<String, String> thirdPartyCredentials,
            Map<String, Serializable> variables, PrintStream errorStream) {

        Map<String, Serializable> variablesAndCredentials = new HashMap<>(variables);

        for (Map.Entry<String, String> credentialEntry : thirdPartyCredentials.entrySet()) {
            variablesAndCredentials.put(CREDENTIALS_KEY_PREFIX + credentialEntry.getKey(),
                    credentialEntry.getValue());
        }

        replace(script, variablesAndCredentials, errorStream);
    }

    private void replace(Script script, Map<String, Serializable> substitutes, PrintStream errorStream) {
        if (script != null) {
            if ("java".equals(script.getEngineName())) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Serializable> deserializedArgs =
                            SerializationUtil.deserializeVariableMap(
                                    (Map<String, byte[]>) script.getParameters()[0]);
                    for (Map.Entry<String, Serializable> deserializedArg : deserializedArgs.entrySet()) {
                        if (deserializedArg.getValue() instanceof String) {
                            deserializedArg.setValue(
                                    VariableSubstitutor.filterAndUpdate((String) deserializedArg.getValue(),
                                            substitutes));
                        }
                    }
                    script.getParameters()[0] = new HashMap<>(
                            SerializationUtil.serializeVariableMap(deserializedArgs));
                } catch (Exception e) {
                    errorStream.println("Cannot read Java parameters");
                    e.printStackTrace(errorStream);
                }
            } else if ("native".equals(script.getEngineName())) { // to replace script arguments
                script.setScript(VariableSubstitutor.filterAndUpdate(script.getScript(), substitutes));
            } else {
                Serializable[] args = script.getParameters();

                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof String) {
                            args[i] = VariableSubstitutor.filterAndUpdate((String) args[i], substitutes);
                        }
                    }
                }
            }
        }
    }
}
