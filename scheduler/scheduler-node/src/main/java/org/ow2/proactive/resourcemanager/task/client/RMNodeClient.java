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

    @Override
    public void init(ConnectionInfo connectionInfo) throws Exception {
        HttpClient client = new HttpClientBuilder().insecure(connectionInfo.isInsecure()).useSystemProperties().build();

        RMRestClient restApiClient = new RMRestClient(connectionInfo.getUrl(), new ApacheHttpClient4Engine(client));

        //        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        //        factory.register(new WildCardTypeReader());
        //        factory.register(new OctetStreamReader());
        //        factory.register(new TaskResultReader());
        //        SchedulerRestClient.registerGzipEncoding(factory);

        this.rm = restApiClient.getRm();

        this.connectionInfo = connectionInfo;

        renewSession();
    }

    public void renewSession() throws NotConnectedException {
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

    @Override
    public String rmConnect(String username, String password) throws KeyException, LoginException, RMException {
        return rm.rmConnect(username, password);
    }

    @Override
    public void rmDisconnect(String sessionId) throws NotConnectedException {
        rm.rmDisconnect(sessionId);
    }

    @Override
    public String loginWithCredential(LoginForm multipart)
            throws KeyException, IOException, LoginException, RMException {
        return rm.loginWithCredential(multipart);
    }

    @Override
    public String getLoginFromSessionId(String sessionId) {
        return rm.getLoginFromSessionId(sessionId);
    }

    @Override
    public UserData getUserDataFromSessionId(String sessionId) {
        return rm.getUserDataFromSessionId(sessionId);
    }

    @Override
    public RMState getState(String sessionId) throws NotConnectedException {
        return rm.getState(sessionId);
    }

    @Override
    public RMStateDelta getRMStateDelta(String sessionId, String clientCounter)
            throws NotConnectedException, PermissionRestException {
        return rm.getRMStateDelta(sessionId, clientCounter);
    }

    @Override
    public RMStateFull getRMStateFull(String sessionId) throws NotConnectedException, PermissionRestException {
        return rm.getRMStateFull(sessionId);
    }

    @Override
    public boolean isActive(String sessionId) throws NotConnectedException {
        return false;
    }

    @Override
    public boolean addNode(String sessionId, String url, String nodesource) throws NotConnectedException {
        return false;
    }

    @Override
    public boolean nodeIsAvailable(String sessionId, String url) throws NotConnectedException {
        return false;
    }

    @Override
    public List<RMNodeSourceEvent> getExistingNodeSources(String sessionId)
            throws NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public NSState defineNodeSource(String sessionId, String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters, String nodesRecoverable)
            throws NotConnectedException {
        return null;
    }

    @Override
    public NSState editNodeSource(String sessionId, String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters, String nodesRecoverable)
            throws NotConnectedException {
        return null;
    }

    @Override
    public NSState updateDynamicParameters(String sessionId, String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters)
            throws NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public NSState createNodeSource(String sessionId, String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters)
            throws NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public NSState createNodeSource(String sessionId, String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters, String nodesRecoverable)
            throws NotConnectedException {
        return null;
    }

    @Override
    public NSState deployNodeSource(String sessionId, String nodeSourceName)
            throws NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public NSState undeployNodeSource(String sessionId, String nodeSourceName, boolean preempt)
            throws NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public int getNodeSourcePingFrequency(String sessionId, String sourceName)
            throws NotConnectedException, PermissionRestException {
        return 0;
    }

    @Override
    public boolean releaseNode(String sessionId, String url)
            throws RMNodeException, NotConnectedException, PermissionRestException {
        return false;
    }

    @Override
    public boolean removeNode(String sessionId, String nodeUrl, boolean preempt)
            throws NotConnectedException, PermissionRestException {
        return false;
    }

    @Override
    public boolean removeNodeSource(String sessionId, String sourceName, boolean preempt)
            throws NotConnectedException, PermissionRestException {
        return false;
    }

    @Override
    public boolean lockNodes(String sessionId, Set<String> nodeUrls)
            throws NotConnectedException, PermissionRestException {
        return false;
    }

    @Override
    public boolean unlockNodes(String sessionId, Set<String> nodeUrls)
            throws NotConnectedException, PermissionRestException {
        return false;
    }

    @Override
    public Object getNodeMBeanInfo(String sessionId, String nodeJmxUrl, String objectName, List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException {
        return null;
    }

    @Override
    public String getNodeMBeanHistory(String sessionId, String nodeJmxUrl, String objectName, List<String> attrs,
            String range) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {
        return null;
    }

    @Override
    public Map<String, Map<String, Object>> getNodesMBeanHistory(String sessionId, List<String> nodesJmxUrl,
            String objectName, List<String> attrs, String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {
        return null;
    }

    @Override
    public Object getNodeMBeansInfo(String sessionId, String nodeJmxUrl, String objectNames, List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException {
        return null;
    }

    @Override
    public Object getNodeMBeansHistory(String sessionId, String nodeJmxUrl, String objectNames, List<String> attrs,
            String range) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException,
            PermissionRestException {
        return null;
    }

    @Override
    public boolean shutdown(String sessionId, boolean preempt) throws NotConnectedException, PermissionRestException {
        return false;
    }

    @Override
    public TopologyInfo getTopology(String sessionId) throws NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures(String sessionId)
            throws NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public Map<String, List<String>> getInfrasToPoliciesMapping(String sessionId)
            throws NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies(String sessionId)
            throws NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public NodeSourceConfiguration getNodeSourceConfiguration(String sessionId, String nodeSourceName)
            throws NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public Object getMBeanInfo(String sessionId, ObjectName name, List<String> attrs) throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, IOException, NotConnectedException, PermissionRestException {
        return null;
    }

    @Override
    public void setMBeanInfo(String sessionId, ObjectName name, String type, String attr, String value)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException {

    }

    @Override
    public String getStatHistory(String sessionId, String range)
            throws ReflectionException, InterruptedException, IntrospectionException, NotConnectedException,
            InstanceNotFoundException, MalformedObjectNameException, IOException {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public ScriptResult<Object> executeNodeScript(String sessionId, String nodeUrl, String script, String scriptEngine)
            throws Throwable {
        return null;
    }

    @Override
    public List<ScriptResult<Object>> executeNodeSourceScript(String sessionId, String nodeSource, String script,
            String scriptEngine) throws Throwable {
        return null;
    }

    @Override
    public ScriptResult<Object> executeHostScript(String sessionId, String host, String script, String scriptEngine)
            throws Throwable {
        return null;
    }

    @Override
    public String getRMThreadDump(String sessionId) throws NotConnectedException {
        return null;
    }

    @Override
    public String getNodeThreadDump(String sessionId, String nodeUrl) throws NotConnectedException {
        return null;
    }

    @Override
    public Map<String, Map<String, Map<String, List<RMNodeHistory>>>> getNodesHistory(String sessionId,
            long windowStart, long windowEnd) throws NotConnectedException {
        return null;
    }

    @Override
    public void addNodeToken(String sessionId, String nodeUrl, String token)
            throws NotConnectedException, RestException {

    }

    @Override
    public void removeNodeToken(String sessionId, String nodeUrl, String token)
            throws NotConnectedException, RestException {

    }

    @Override
    public void setNodeTokens(String sessionId, String nodeUrl, List<String> tokens)
            throws NotConnectedException, RestException {

    }
}
