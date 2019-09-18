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

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyException;
import java.util.*;

import javax.management.*;
import javax.security.auth.login.LoginException;

import org.apache.http.client.HttpClient;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.http.HttpClientBuilder;
import org.ow2.proactive.resourcemanager.common.NSState;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeHistory;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateDelta;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateFull;
import org.ow2.proactive.resourcemanager.exception.RMActiveObjectCreationException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.exception.RMNodeException;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyInfo;
import org.ow2.proactive.resourcemanager.nodesource.common.NodeSourceConfiguration;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.rest.IRMClient;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive_grid_cloud_portal.common.RMRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.rm.client.RMRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.RestException;


@PublicAPI
public class RMNodeClient implements IRMClient, Serializable {

    private String schedulerRestUrl;

    private CredData userCreds;

    private RMRestInterface rm;

    private ConnectionInfo connectionInfo;

    private String sessionId;

    public RMNodeClient(CredData userCreds, String schedulerRestUrl) {
        this.schedulerRestUrl = schedulerRestUrl;
        this.userCreds = userCreds;
    }

    public void connect() throws Exception {
        init(new ConnectionInfo(schedulerRestUrl, userCreds.getLogin(), userCreds.getPassword(), null, true));
    }

    public void connect(String url) throws Exception {
        schedulerRestUrl = url;
        connect();
    }

    @Override
    public void init(ConnectionInfo connectionInfo) throws Exception {
        HttpClient client = new HttpClientBuilder().insecure(connectionInfo.isInsecure()).useSystemProperties().build();

        RMRestClient restApiClient = new RMRestClient(connectionInfo.getUrl(), new ApacheHttpClient4Engine(client));

        this.rm = restApiClient.getRm();

        this.connectionInfo = connectionInfo;

        renewSession();
    }

    public void renewSession() {
        try {
            String sid = rm.rmConnect(connectionInfo.getLogin(), connectionInfo.getPassword());
            setSession(sid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    @Override
    public void setSession(String sid) {
        this.sessionId = sid;
    }

    @Override
    public String getSession() {
        return sessionId;
    }

    public void disconnect() throws NotConnectedException {
        rm.rmDisconnect(sessionId);
    }

    public RMState getState() throws NotConnectedException {
        return rm.getState(sessionId);
    }

    public RMStateDelta getRMStateDelta(String clientCounter) throws NotConnectedException, PermissionRestException {
        return rm.getRMStateDelta(sessionId, clientCounter);
    }

    public RMStateFull getRMStateFull() throws NotConnectedException, PermissionRestException {
        return rm.getRMStateFull(sessionId);
    }

    public boolean isActive() throws NotConnectedException {
        return rm.isActive(sessionId);
    }

    public boolean addNode(String url, String nodesource) throws NotConnectedException {
        return rm.addNode(sessionId, url, nodesource);
    }

    public boolean nodeIsAvailable(String url) throws NotConnectedException {
        return rm.nodeIsAvailable(sessionId, url);
    }

    public List<RMNodeSourceEvent> getExistingNodeSources() throws NotConnectedException, PermissionRestException {
        return rm.getExistingNodeSources(sessionId);
    }

    public NSState defineNodeSource(String nodeSourceName, String infrastructureType, String[] infrastructureParameters,
            String[] infrastructureFileParameters, String policyType, String[] policyParameters,
            String[] policyFileParameters, String nodesRecoverable) throws NotConnectedException {
        return rm.defineNodeSource(sessionId,
                                   nodeSourceName,
                                   infrastructureType,
                                   infrastructureParameters,
                                   infrastructureFileParameters,
                                   policyType,
                                   policyParameters,
                                   policyFileParameters,
                                   nodesRecoverable);
    }

    public NSState editNodeSource(String nodeSourceName, String infrastructureType, String[] infrastructureParameters,
            String[] infrastructureFileParameters, String policyType, String[] policyParameters,
            String[] policyFileParameters, String nodesRecoverable) throws NotConnectedException {
        return rm.editNodeSource(sessionId,
                                 nodeSourceName,
                                 infrastructureType,
                                 infrastructureParameters,
                                 infrastructureFileParameters,
                                 policyType,
                                 policyParameters,
                                 policyFileParameters,
                                 nodesRecoverable);
    }

    public NSState updateDynamicParameters(String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters)
            throws NotConnectedException, PermissionRestException {
        return rm.updateDynamicParameters(sessionId,
                                          nodeSourceName,
                                          infrastructureType,
                                          infrastructureParameters,
                                          infrastructureFileParameters,
                                          policyType,
                                          policyParameters,
                                          policyFileParameters);
    }

    public NSState createNodeSource(String nodeSourceName, String infrastructureType, String[] infrastructureParameters,
            String[] infrastructureFileParameters, String policyType, String[] policyParameters,
            String[] policyFileParameters) throws NotConnectedException, PermissionRestException {
        return rm.createNodeSource(sessionId,
                                   nodeSourceName,
                                   infrastructureType,
                                   infrastructureParameters,
                                   infrastructureFileParameters,
                                   policyType,
                                   policyParameters,
                                   policyFileParameters);
    }

    public NSState createNodeSource(String nodeSourceName, String infrastructureType, String[] infrastructureParameters,
            String[] infrastructureFileParameters, String policyType, String[] policyParameters,
            String[] policyFileParameters, String nodesRecoverable) throws NotConnectedException {
        return rm.createNodeSource(sessionId,
                                   nodeSourceName,
                                   infrastructureType,
                                   infrastructureParameters,
                                   infrastructureFileParameters,
                                   policyType,
                                   policyParameters,
                                   policyFileParameters,
                                   nodesRecoverable);
    }

    public NSState deployNodeSource(String nodeSourceName) throws NotConnectedException, PermissionRestException {
        return rm.deployNodeSource(sessionId, nodeSourceName);
    }

    public NSState undeployNodeSource(String nodeSourceName, boolean preempt)
            throws NotConnectedException, PermissionRestException {
        return rm.undeployNodeSource(sessionId, nodeSourceName, preempt);
    }

    public int getNodeSourcePingFrequency(String sourceName) throws NotConnectedException, PermissionRestException {
        return rm.getNodeSourcePingFrequency(sessionId, sourceName);
    }

    public boolean releaseNode(String url) throws RMNodeException, NotConnectedException, PermissionRestException {
        return rm.releaseNode(sessionId, url);
    }

    public boolean removeNode(String nodeUrl, boolean preempt) throws NotConnectedException, PermissionRestException {
        return rm.removeNode(sessionId, nodeUrl, preempt);
    }

    public boolean removeNodeSource(String sourceName, boolean preempt)
            throws NotConnectedException, PermissionRestException {
        return rm.removeNodeSource(sessionId, sourceName, preempt);
    }

    public boolean lockNodes(Set<String> nodeUrls) throws NotConnectedException, PermissionRestException {
        return rm.lockNodes(sessionId, nodeUrls);
    }

    public boolean unlockNodes(Set<String> nodeUrls) throws NotConnectedException, PermissionRestException {
        return rm.unlockNodes(sessionId, nodeUrls);
    }

    public Object getNodeMBeanInfo(String nodeJmxUrl, String objectName, List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException {
        return rm.getNodeMBeanInfo(sessionId, nodeJmxUrl, objectName, attrs);
    }

    public String getNodeMBeanHistory(String nodeJmxUrl, String objectName, List<String> attrs, String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {
        return rm.getNodeMBeanHistory(sessionId, nodeJmxUrl, objectName, attrs, range);
    }

    public Map<String, Map<String, Object>> getNodesMBeanHistory(List<String> nodesJmxUrl, String objectName,
            List<String> attrs, String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {
        return rm.getNodesMBeanHistory(sessionId, nodesJmxUrl, objectName, attrs, range);
    }

    public Object getNodeMBeansInfo(String nodeJmxUrl, String objectNames, List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException {
        return rm.getNodeMBeanInfo(sessionId, nodeJmxUrl, objectNames, attrs);
    }

    public Object getNodeMBeansHistory(String nodeJmxUrl, String objectNames, List<String> attrs, String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException,
            PermissionRestException {
        return rm.getNodeMBeansHistory(sessionId, nodeJmxUrl, objectNames, attrs, range);
    }

    public boolean shutdown(boolean preempt) throws NotConnectedException, PermissionRestException {
        return rm.shutdown(sessionId, preempt);
    }

    public TopologyInfo getTopology() throws NotConnectedException, PermissionRestException {
        return rm.getTopology(sessionId);
    }

    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures()
            throws NotConnectedException, PermissionRestException {
        return rm.getSupportedNodeSourceInfrastructures(sessionId);
    }

    public Map<String, List<String>> getInfrasToPoliciesMapping()
            throws NotConnectedException, PermissionRestException {
        return rm.getInfrasToPoliciesMapping(sessionId);
    }

    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies()
            throws NotConnectedException, PermissionRestException {
        return rm.getSupportedNodeSourcePolicies(sessionId);
    }

    public NodeSourceConfiguration getNodeSourceConfiguration(String nodeSourceName)
            throws NotConnectedException, PermissionRestException {
        return rm.getNodeSourceConfiguration(sessionId, nodeSourceName);
    }

    public Object getMBeanInfo(ObjectName name, List<String> attrs) throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, IOException, NotConnectedException, PermissionRestException {
        return rm.getMBeanInfo(sessionId, name, attrs);
    }

    public void setMBeanInfo(ObjectName name, String type, String attr, String value)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException {
        rm.setMBeanInfo(sessionId, name, type, attr, value);
    }

    public String getStatHistory(String range) throws ReflectionException, InterruptedException, IntrospectionException,
            NotConnectedException, InstanceNotFoundException, MalformedObjectNameException, IOException {
        return rm.getStatHistory(sessionId, range);
    }

    public String getVersion() {
        return rm.getVersion();
    }

    public ScriptResult<Object> executeNodeScript(String nodeUrl, String script, String scriptEngine) throws Throwable {
        return rm.executeNodeScript(sessionId, nodeUrl, script, scriptEngine);
    }

    public List<ScriptResult<Object>> executeNodeSourceScript(String nodeSource, String script, String scriptEngine)
            throws Throwable {
        return rm.executeNodeSourceScript(sessionId, nodeSource, script, scriptEngine);
    }

    public ScriptResult<Object> executeHostScript(String host, String script, String scriptEngine) throws Throwable {
        return rm.executeNodeScript(sessionId, host, script, scriptEngine);
    }

    public String getRMThreadDump() throws NotConnectedException {
        return rm.getRMThreadDump(sessionId);
    }

    public String getNodeThreadDump(String nodeUrl) throws NotConnectedException {
        return rm.getNodeThreadDump(sessionId, nodeUrl);
    }

    public Map<String, Map<String, Map<String, List<RMNodeHistory>>>> getNodesHistory(long windowStart, long windowEnd)
            throws NotConnectedException {
        return rm.getNodesHistory(sessionId, windowStart, windowEnd);
    }

    public void addNodeToken(String nodeUrl, String token) throws NotConnectedException, RestException {
        rm.addNodeToken(sessionId, nodeUrl, token);
    }

    public void removeNodeToken(String nodeUrl, String token) throws NotConnectedException, RestException {
        rm.removeNodeToken(sessionId, nodeUrl, token);
    }

    public void setNodeTokens(String nodeUrl, List<String> tokens) throws NotConnectedException, RestException {
        rm.setNodeTokens(sessionId, nodeUrl, tokens);
    }
}
