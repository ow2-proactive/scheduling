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
package org.ow2.proactive.resourcemanager.task.client;

import java.io.Serializable;
import java.util.*;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeHistory;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.NodeSourceConfiguration;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scheduler.rest.IRMClient;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.Lambda.RunnableThatThrows;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.proactive_grid_cloud_portal.common.RMRestInterface;
import org.ow2.proactive_grid_cloud_portal.rm.client.RMRestClient;


/**
 * Scheduler api available as a variable during the execution of a task. it is based on the ISchedulerClient interface.
 *
 * @author ActiveEon Team
 */
@PublicAPI
public class RMNodeClient implements IRMClient, Serializable {

    private String schedulerRestUrl;

    private RMRestClient rmRestClient;

    private RMRestInterface rm;

    public RMNodeClient(CredData userCreds, String schedulerRestUrl) {
        this.schedulerRestUrl = schedulerRestUrl;
        RMRestInterface rm = rmRestClient.getRm();
    }

    @Override
    public void init(ConnectionInfo connectionInfo) throws Exception {

    }

    @Override
    public ConnectionInfo getConnectionInfo() {
        return null;
    }

    @Override
    public void setSession(String sid) {

    }

    @Override
    public String getSession() {
        return null;
    }

    private BooleanWrapper falseIfException(RunnableThatThrows runnableThatThrows) {
        try {
            runnableThatThrows.run();
            return new BooleanWrapper(true);
        } catch (Exception e) {
            return new BooleanWrapper(false);
        }
    }

    @Override
    public BooleanWrapper defineNodeSource(String nodeSourceName, String infrastructureType, Object[] infraParams,
            String policyType, Object[] policyParams, boolean nodesRecoverable) {
        return null;
    }

    @Override
    public BooleanWrapper editNodeSource(String nodeSourceName, String infrastructureType, Object[] infraParams,
            String policyType, Object[] policyParams, boolean nodesRecoverable) {
        return null;
    }

    @Override
    public BooleanWrapper updateDynamicParameters(String nodeSourceName, String infrastructureType,
            Object[] infraParams, String policyType, Object[] policyParams) {
        return null;
    }

    @Override
    public BooleanWrapper createNodeSource(String nodeSourceName, String infrastructureType,
            Object[] infrastructureParameters, String policyType, Object[] policyParameters, boolean nodesRecoverable) {
        return null;
    }

    @Override
    public BooleanWrapper deployNodeSource(String nodeSourceName) {
        return falseIfException(() -> rmRestClient.getRm().deployNodeSource(getSession(), nodeSourceName));
    }

    @Override
    public BooleanWrapper undeployNodeSource(String nodeSourceName, boolean preempt) {
        return null;
    }

    @Override
    public BooleanWrapper removeNodeSource(String sourceName, boolean preempt) {
        return null;
    }

    @Override
    public List<RMNodeSourceEvent> getExistingNodeSourcesList() {
        return null;
    }

    @Override
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures() {
        return null;
    }

    @Override
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies() {
        return null;
    }

    @Override
    public NodeSourceConfiguration getNodeSourceConfiguration(String nodeSourceName) {
        return null;
    }

    @Override
    public BooleanWrapper setNodeSourcePingFrequency(int frequency, String sourceName) {
        return null;
    }

    @Override
    public IntWrapper getNodeSourcePingFrequency(String sourceName) {
        return null;
    }

    @Override
    public BooleanWrapper addNode(String nodeUrl) {
        return null;
    }

    @Override
    public BooleanWrapper addNode(String nodeUrl, String sourceName) {
        return null;
    }

    @Override
    public BooleanWrapper removeNode(String nodeUrl, boolean preempt) {
        return null;
    }

    @Override
    public BooleanWrapper lockNodes(Set<String> urls) {
        return null;
    }

    @Override
    public BooleanWrapper unlockNodes(Set<String> urls) {
        return null;
    }

    @Override
    public BooleanWrapper nodeIsAvailable(String nodeUrl) {
        return null;
    }

    @Override
    public Set<String> setNodesAvailable(Set<String> nodeUrls) {
        return null;
    }

    @Override
    public BooleanWrapper isActive() {
        return null;
    }

    @Override
    public RMState getState() {
        return null;
    }

    @Override
    public RMMonitoring getMonitoring() {
        return null;
    }

    @Override
    public StringWrapper getRMThreadDump() {
        return null;
    }

    @Override
    public StringWrapper getNodeThreadDump(String nodeUrl) {
        return null;
    }

    @Override
    public Set<String> listAliveNodeUrls() {
        return null;
    }

    @Override
    public Set<String> listAliveNodeUrls(Set<String> nodeSourceNames) {
        return null;
    }

    @Override
    public NodeSet getAtMostNodes(int number, SelectionScript selectionScript) {
        return null;
    }

    @Override
    public NodeSet getAtMostNodes(int number, SelectionScript selectionScript, NodeSet exclusion) {
        return null;
    }

    @Override
    public NodeSet getAtMostNodes(int number, List<SelectionScript> selectionScriptsList, NodeSet exclusion) {
        return null;
    }

    @Override
    public NodeSet getAtMostNodes(int number, TopologyDescriptor descriptor, List<SelectionScript> selectionScriptsList,
            NodeSet exclusion) {
        return null;
    }

    @Override
    public NodeSet getNodes(int number, TopologyDescriptor descriptor, List<SelectionScript> selectionScriptsList,
            NodeSet exclusion, boolean bestEffort) {
        return null;
    }

    @Override
    public NodeSet getNodes(Criteria criteria) {
        return null;
    }

    @Override
    public BooleanWrapper releaseNode(Node node) {
        return null;
    }

    @Override
    public BooleanWrapper releaseNodes(NodeSet nodes) {
        return null;
    }

    @Override
    public BooleanWrapper disconnect() {
        return null;
    }

    @Override
    public BooleanWrapper shutdown(boolean preempt) {
        return null;
    }

    @Override
    public Topology getTopology() {
        return null;
    }

    @Override
    public BooleanWrapper isNodeAdmin(String nodeUrl) {
        return null;
    }

    @Override
    public BooleanWrapper isNodeUser(String nodeUrl) {
        return null;
    }

    @Override
    public <T> List<ScriptResult<T>> executeScript(Script<T> script, String targetType, Set<String> targets) {
        return null;
    }

    @Override
    public List<ScriptResult<Object>> executeScript(String script, String scriptEngine, String targetType,
            Set<String> targets) {
        return null;
    }

    @Override
    public StringWrapper getCurrentUser() {
        return null;
    }

    @Override
    public UserData getCurrentUserData() {
        return null;
    }

    @Override
    public void releaseBusyNodesNotInList(List<NodeSet> verifiedBusyNodes) {

    }

    @Override
    public boolean areNodesKnown(NodeSet nodes) {
        return false;
    }

    @Override
    public boolean areNodesRecoverable(NodeSet nodes) {
        return false;
    }

    @Override
    public void setNeededNodes(int neededNodes) {

    }

    @Override
    public Map<String, List<String>> getInfrasToPoliciesMapping() {
        return null;
    }

    @Override
    public List<RMNodeHistory> getNodesHistory(long windowStart, long windowEnd) {
        return null;
    }

    @Override
    public void addNodeToken(String nodeUrl, String token) throws RMException {

    }

    @Override
    public void removeNodeToken(String nodeUrl, String token) throws RMException {

    }

    @Override
    public void setNodeTokens(String nodeUrl, List<String> tokens) throws RMException {

    }
}
