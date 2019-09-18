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
package org.ow2.proactive.scheduler.core.rmproxies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAEventProgramming;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.task.client.RMNodeClient;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.RemoteSpace;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient;
import org.ow2.proactive.scheduler.synchronization.Synchronization;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.client.DataSpaceNodeClient;
import org.ow2.proactive.scheduler.task.client.SchedulerNodeClient;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scheduler.task.utils.VariablesMap;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;


@ActiveObject
public class RMProxyActiveObject {

    protected static final Logger logger = Logger.getLogger(RMProxyActiveObject.class);

    protected ResourceManager rm;

    private Map<NodeSet, TaskId> nodesTaskId = new ConcurrentHashMap<>();

    public RMProxyActiveObject() {
    }

    static RMProxyActiveObject createAOProxy(RMAuthentication rmAuth, Credentials creds)
            throws RMProxyCreationException {
        try {
            RMProxyActiveObject proxy = PAActiveObject.newActive(RMProxyActiveObject.class, new Object[] {});
            proxy.connect(rmAuth, creds);
            return proxy;
        } catch (Exception e) {
            throw new RMProxyCreationException(e);
        }
    }

    @ImmediateService
    public void connect(RMAuthentication rmAuth, Credentials creds) throws LoginException {
        this.rm = rmAuth.login(creds);
    }

    @ImmediateService
    public void terminateProxy() {
        try {
            //try disconnect
            PAFuture.waitFor(disconnect());
        } catch (Exception e) {
            //RM is not responding, do nothing
        }
        PAActiveObject.terminateActiveObject(false);
    }

    @ImmediateService
    public BooleanWrapper disconnect() {
        return rm.disconnect();
    }

    @ImmediateService
    public BooleanWrapper isActive() {
        return rm.isActive();
    }

    @ImmediateService
    public RMState getState() {
        return rm.getState();
    }

    @ImmediateService
    public NodeSet getNodes(Criteria criteria) {
        return rm.getNodes(criteria);
    }

    @ImmediateService
    public BooleanWrapper releaseNode(Node node) {
        return rm.releaseNode(node);
    }

    @ImmediateService
    public BooleanWrapper releaseNodes(NodeSet nodes) {
        return rm.releaseNodes(nodes);
    }

    @ImmediateService
    public boolean isNodeSetForThisRM(NodeSet nodeSet) {
        List<Node> allNodes = new ArrayList<>(nodeSet);
        if (nodeSet.getExtraNodes() != null) {
            allNodes.addAll(nodeSet.getExtraNodes());
        }
        for (Node node : allNodes) {
            BooleanWrapper nodeIsAvailable = rm.nodeIsAvailable(node.getNodeInformation().getURL());
            if (nodeIsAvailable.getBooleanValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Execute the given CleaningScript on each nodes before releasing them.
     *
     * @param nodes          the node set to release
     * @param cleaningScript the cleaning script to apply to each node before releasing
     * @param variables
     * @param taskId
     * @param creds Credentials containing the user's unencrypted 3rd party credentials
     * @see #releaseNodes(NodeSet)
     */
    @ImmediateService
    public void releaseNodes(NodeSet nodes, Script<?> cleaningScript, VariablesMap variables,
            Map<String, String> genericInformation, TaskId taskId, Credentials creds, Synchronization store) {
        if (nodes != null && nodes.size() > 0) {
            if (cleaningScript == null) {
                releaseNodes(nodes).booleanValue();
                closeTaskLogger(taskId);
            } else if (InternalTask.isScriptAuthorized(taskId, cleaningScript)) {
                handleCleaningScript(nodes, cleaningScript, variables, genericInformation, taskId, creds, store);
            } else {
                TaskLogger.getInstance().error(taskId,
                                               "Unauthorized clean script: " + System.getProperty("line.separator") +
                                                       cleaningScript.fetchScript());
                releaseNodes(nodes).booleanValue();
                closeTaskLogger(taskId);
            }
        } else {
            closeTaskLogger(taskId);
        }
    }

    private void closeTaskLogger(TaskId taskId) {
        if (taskId != null) {
            TaskLogger.getInstance().close(taskId);
        }
    }

    /**
     * Execute the given script on the given node.
     * Also register a callback on {@link #cleanCallBack(Future, NodeSet)} method when script has returned.
     * @param nodes           the nodeset on which to start the script
     * @param cleaningScript the script to be executed
     * @param variables
     * @param genericInformation
     * @param taskId
     * @param creds credentials with CredData containing third party credentials
     */
    private void handleCleaningScript(NodeSet nodes, Script<?> cleaningScript, VariablesMap variables,
            Map<String, String> genericInformation, TaskId taskId, Credentials creds, Synchronization store) {
        TaskLogger instance = TaskLogger.getInstance();

        try {
            this.nodesTaskId.put(nodes, taskId);

            //create a decrypter to access scheduler and retrieve Third Party User Credentials
            String privateKeyPath = PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_PRIVKEY_PATH.getValueAsString());
            Decrypter decrypter = new Decrypter(Credentials.getPrivateKey(privateKeyPath));
            decrypter.setCredentials(creds);

            Node node = nodes.get(0);
            String nodeUrl = node.getNodeInformation().getURL();
            String nodeName = node.getNodeInformation().getName();
            String hostName = node.getVMInformation().getHostName();

            HashMap<String, Serializable> dictionary = new HashMap<>();

            dictionary.putAll(variables.getScriptMap());
            dictionary.putAll(variables.getInheritedMap());
            dictionary.putAll(variables.getPropagatedVariables());
            dictionary.putAll(variables.getScopeMap());
            dictionary.put(SchedulerVars.PA_NODE_URL.toString(), nodeUrl);
            dictionary.put(SchedulerVars.PA_NODE_NAME.toString(), nodeName);
            dictionary.put(SchedulerVars.PA_NODE_HOST.toString(), hostName);

            //start handler for binding

            ScriptHandler handler = ScriptLoader.createHandler(node);
            VariablesMap resolvedMap = new VariablesMap();
            resolvedMap.setInheritedMap(VariableSubstitutor.resolveVariables(variables.getInheritedMap(), dictionary));
            resolvedMap.setScopeMap(VariableSubstitutor.resolveVariables(variables.getScopeMap(), dictionary));
            resolvedMap.put(SchedulerVars.PA_NODE_URL.toString(), nodeUrl);
            resolvedMap.put(SchedulerVars.PA_NODE_NAME.toString(), nodeName);
            resolvedMap.put(SchedulerVars.PA_NODE_HOST.toString(), hostName);
            handler.addBinding(SchedulerConstants.VARIABLES_BINDING_NAME, (Serializable) resolvedMap);
            handler.addBinding(SchedulerConstants.GENERIC_INFO_BINDING_NAME, (Serializable) genericInformation);
            handler.addBinding(SchedulerConstants.SYNCHRONIZATION_API_BINDING_NAME, store);

            //retrieve scheduler URL to bind with schedulerapi, globalspaceapi, and userspaceapi
            String schedulerUrl = PASchedulerProperties.SCHEDULER_REST_URL.getValueAsString();

            logger.debug("Binding schedulerapi...");
            SchedulerNodeClient client = new SchedulerNodeClient(decrypter, schedulerUrl);
            handler.addBinding(SchedulerConstants.SCHEDULER_CLIENT_BINDING_NAME, client);

            logger.debug("Binging rmapi...");
            RMNodeClient rmNodeClient = new RMNodeClient(decrypter.decrypt(), schedulerUrl);
            handler.addBinding(SchedulerConstants.RM_CLIENT_BINDING_NAME, rmNodeClient);

            logger.debug("Binding globalspaceapi...");
            RemoteSpace globalSpaceClient = new DataSpaceNodeClient(client,
                                                                    IDataSpaceClient.Dataspace.GLOBAL,
                                                                    schedulerUrl);
            handler.addBinding(SchedulerConstants.DS_GLOBAL_API_BINDING_NAME, (Serializable) globalSpaceClient);

            logger.debug("Binding userspaceapi...");
            RemoteSpace userSpaceClient = new DataSpaceNodeClient(client,
                                                                  IDataSpaceClient.Dataspace.USER,
                                                                  schedulerUrl);
            handler.addBinding(SchedulerConstants.DS_USER_API_BINDING_NAME, (Serializable) userSpaceClient);

            logger.debug("Binding credentials...");
            Map<String, String> resolvedThirdPartyCredentials = VariableSubstitutor.filterAndUpdate(decrypter.decrypt()
                                                                                                             .getThirdPartyCredentials(),
                                                                                                    dictionary);
            handler.addBinding(SchedulerConstants.CREDENTIALS_VARIABLE, (Serializable) resolvedThirdPartyCredentials);

            ScriptResult<?> future = handler.handle(cleaningScript);
            try {
                PAEventProgramming.addActionOnFuture(future, "cleanCallBack", nodes);
            } catch (IllegalArgumentException e) {
                //TODO - linked to PROACTIVE-936 -> IllegalArgumentException is raised if method name is unknown
                //should be replaced by checked exception
                instance.error(taskId,
                               "ERROR : Callback method won't be executed, node won't be released. This is a critical state, check the callback method name",
                               e);
                instance.close(taskId);
            }
            instance.info(taskId, "Cleaning Script started on node " + nodes.get(0).getNodeInformation().getURL());

        } catch (Exception e) {
            //if active object cannot be created or script has failed
            instance.error(taskId,
                           "Error while starting cleaning script for task " + taskId + " on " + nodes.get(0),
                           e);
            instance.close(taskId);
            releaseNodes(nodes).booleanValue();
        }
    }

    /**
     * Called when a script has returned (call is made as an active object call)
     * <p>
     * Check the nodes to release and release the one that have to (clean script has returned)
     * Take care when renaming this method, method name is linked to
     * {@link #handleCleaningScript(NodeSet, Script, VariablesMap, Map, TaskId, Credentials, Synchronization)}
     */
    @ImmediateService
    public synchronized void cleanCallBack(Future<ScriptResult<?>> future, NodeSet nodes) {

        String nodeUrl = nodes.get(0).getNodeInformation().getURL();
        ScriptResult<?> sResult = null;
        TaskId taskId = nodesTaskId.get(nodes);
        try {
            sResult = future.get();
        } catch (Exception e) {
            logger.error("Exception occurred while executing cleaning script on node " + nodeUrl + ":", e);
        }
        printCleaningScriptInformations(nodes, sResult, taskId);
        closeTaskLogger(taskId);
        releaseNodes(nodes);
    }

    private void printCleaningScriptInformations(NodeSet nodes, ScriptResult<?> sResult, TaskId taskId) {
        if (logger.isInfoEnabled()) {
            TaskLogger instance = TaskLogger.getInstance();
            String nodeUrl = nodes.get(0).getNodeInformation().getURL();
            if (sResult.errorOccured()) {
                instance.error(taskId, "Exception while running cleaning script on " + nodeUrl, sResult.getException());
            } else {
                instance.info(taskId, "Cleaning script successful.");
            }
            if (sResult.getOutput() != null && !sResult.getOutput().isEmpty()) {
                instance.info(taskId, "Cleaning script output on node " + nodeUrl + ":");
                instance.info(taskId, sResult.getOutput());
            }
        }
    }

    @ImmediateService
    public void releaseDanglingBusyNodes(List<NodeSet> verifiedBusyNodes) {
        rm.releaseBusyNodesNotInList(verifiedBusyNodes);
    }

    @ImmediateService
    public boolean areNodesKnown(NodeSet nodes) {
        return rm.areNodesKnown(nodes);
    }

    @ImmediateService
    public boolean areNodesRecoverable(NodeSet nodes) {
        return rm.areNodesRecoverable(nodes);
    }

    @ImmediateService
    public boolean setNeededNodes(int neededNodes) {
        rm.setNeededNodes(neededNodes);
        return true;
    }
}
