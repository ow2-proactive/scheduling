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
import java.util.*;

import javax.management.*;

import org.apache.http.client.HttpClient;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.http.HttpClientBuilder;
import org.ow2.proactive.resourcemanager.common.NSState;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeHistory;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateDelta;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateFull;
import org.ow2.proactive.resourcemanager.exception.RMNodeException;
import org.ow2.proactive.resourcemanager.frontend.PluginDescriptorData;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyData;
import org.ow2.proactive.resourcemanager.nodesource.common.NodeSourceConfiguration;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.rest.IRMClient;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive_grid_cloud_portal.common.RMRestInterface;
import org.ow2.proactive_grid_cloud_portal.rm.client.RMRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.RestException;
import org.rrd4j.ConsolFun;


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

        try {
            String sid = rm.rmConnect(connectionInfo.getLogin(), connectionInfo.getPassword());
            setSession(sid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkNonEmptySession() throws NotConnectedException {
        if (sessionId == null) {
            throw new NotConnectedException("You are not connected to the Resource Manager. Call 'connect()' first before using the interface.");
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

    /**
     * Disconnects from resource manager and releases all the nodes taken by
     * user for computations.
     */
    public void disconnect() throws NotConnectedException {
        checkNonEmptySession();
        rm.rmDisconnect(sessionId);
        sessionId = null;
    }

    /**
     * @return Returns the state of the Resource Manager
     */
    public RMState getState() throws NotConnectedException {
        checkNonEmptySession();
        return rm.getState(sessionId);
    }

    /**
     * Returns difference between current state of the resource manager
     * and state that the client is aware of. Each event that changes state fo the RM
     * has counter assosiacted to it. Thus client has to provide 'latestCounter',
     * i.e. latest event he is aware of.
     * @param clientCounter (optional) is the latest counter client has, if parameter is not provided then
     *                                 method returns all events
     * @return the difference between current state and state that client knows
     */
    public RMStateDelta getRMStateDelta(String clientCounter) throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.getRMStateDelta(sessionId, clientCounter);
    }

    /**
     * Returns the full state of the RM, which does not include REMOVED node/nodesources
     * @return the state of the RM, which does not include REMOVED node/nodesources
     */
    public RMStateFull getRMStateFull() throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.getRMStateFull(sessionId);
    }

    /**
     * @return true if the resource manager is operational.
     */
    public boolean isActive() throws NotConnectedException {
        checkNonEmptySession();
        return rm.isActive(sessionId);
    }

    /**
     * Adds an existing node to the particular node source.
     *
     * @param url
     *            the url of the node
     * @param nodesource
     *            the node source, can be null
     * @return true if new node is added successfully, runtime exception
     *         otherwise
     */
    public boolean addNode(String url, String nodesource) throws NotConnectedException {
        checkNonEmptySession();
        return rm.addNode(sessionId, url, nodesource);
    }

    /**
     * @param url
     *            the url of the node
     * @return true if the node nodeUrl is registered (i.e. known by the RM) and not down
     */
    public boolean nodeIsAvailable(String url) throws NotConnectedException {
        checkNonEmptySession();
        return rm.nodeIsAvailable(sessionId, url);
    }

    /**
     * @return list of existing Node Sources
     */
    public List<RMNodeSourceEvent> getExistingNodeSources() throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.getExistingNodeSources(sessionId);
    }

    public NSState defineNodeSource(String nodeSourceName, String infrastructureType, String[] infrastructureParameters,
            String[] infrastructureFileParameters, String policyType, String[] policyParameters,
            String[] policyFileParameters, String nodesRecoverable) throws NotConnectedException {
        checkNonEmptySession();
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
        checkNonEmptySession();
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
        checkNonEmptySession();
        return rm.updateDynamicParameters(sessionId,
                                          nodeSourceName,
                                          infrastructureType,
                                          infrastructureParameters,
                                          infrastructureFileParameters,
                                          policyType,
                                          policyParameters,
                                          policyFileParameters);
    }

    /**
     * @deprecated  As of version 8.1, replaced by {@link #defineNodeSource(String, String, String[], String[],
     * String, String[], String[], String)} and {@link #deployNodeSource(String)}
     */
    public NSState createNodeSource(String nodeSourceName, String infrastructureType, String[] infrastructureParameters,
            String[] infrastructureFileParameters, String policyType, String[] policyParameters,
            String[] policyFileParameters) throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.createNodeSource(sessionId,
                                   nodeSourceName,
                                   infrastructureType,
                                   infrastructureParameters,
                                   infrastructureFileParameters,
                                   policyType,
                                   policyParameters,
                                   policyFileParameters);
    }

    /**
     * @deprecated  As of version 8.1, replaced by {@link #defineNodeSource(String,String, String[], String[],
     * String, String[], String[], String)} and {@link #deployNodeSource(String)}
     *
     * Create a NodeSource
     * <p>
     *
     * @param nodeSourceName
     *            name of the node source to create
     * @param infrastructureType
     *            fully qualified class name of the infrastructure to create
     * @param infrastructureParameters
     *            String parameters of the infrastructure, without the
     *            parameters containing files or credentials
     * @param infrastructureFileParameters
     *            File or credential parameters
     * @param policyType
     *            fully qualified class name of the policy to create
     * @param policyParameters
     *            String parameters of the policy, without the parameters
     *            containing files or credentials
     * @param policyFileParameters
     *            File or credential parameters
     * @param nodesRecoverable
     *            Whether the nodes can be recovered after a crash of the RM
     * @return true if a node source has been created
     */
    public NSState createNodeSource(String nodeSourceName, String infrastructureType, String[] infrastructureParameters,
            String[] infrastructureFileParameters, String policyType, String[] policyParameters,
            String[] policyFileParameters, String nodesRecoverable) throws NotConnectedException {
        checkNonEmptySession();
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

    /**
     * Start the nodes acquisition of the node source
     *
     * @param nodeSourceName the name of the node source to start
     * @return the result of the action, possibly containing the error message
     */
    public NSState deployNodeSource(String nodeSourceName) throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.deployNodeSource(sessionId, nodeSourceName);
    }

    /**
     * Remove the nodes of the node source and keep the node source undeployed
     *
     * @param nodeSourceName the name of the node source to undeploy
     * @return the result of the action, possibly containing the error message
     */
    public NSState undeployNodeSource(String nodeSourceName, boolean preempt)
            throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.undeployNodeSource(sessionId, nodeSourceName, preempt);
    }

    /**
     * Returns the ping frequency of a node source
     *
     * @param sourceName a node source
     * @return the ping frequency
     */
    public int getNodeSourcePingFrequency(String sourceName) throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.getNodeSourcePingFrequency(sessionId, sourceName);
    }

    /**
     * Release a node
     *
     * @param url node's URL
     * @return true of the node has been released
     */
    public boolean releaseNode(String url) throws RMNodeException, NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.releaseNode(sessionId, url);
    }

    /**
     * Delete a node
     *
     * @param nodeUrl node's URL
     * @param preempt if true remove node source immediatly whithout waiting for nodes to be freed
     * @return true if the node is removed successfully, false or exception otherwise
     */
    public boolean removeNode(String nodeUrl, boolean preempt) throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.removeNode(sessionId, nodeUrl, preempt);
    }

    /**
     * Delete a nodesource
     *
     * @param sourceName a node source
     * @param preempt if true remove node source immediatly whithout waiting for nodes to be freed
     * @return true if the node is removed successfully, false or exception otherwise
     */
    public boolean removeNodeSource(String sourceName, boolean preempt)
            throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.removeNodeSource(sessionId, sourceName, preempt);
    }

    /**
     * prevent other users from using a set of locked nodes
     *
     * @param nodeUrls
     *            set of node urls to lock
     * @return true when all nodes were free and have been locked
     */
    public boolean lockNodes(Set<String> nodeUrls) throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.lockNodes(sessionId, nodeUrls);
    }

    /**
     * allow other users to use a set of previously locked nodes
     *
     * @param nodeUrls
     *            set of node urls to unlock
     * @return true when all nodes were locked and have been unlocked
     */
    public boolean unlockNodes(Set<String> nodeUrls) throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.unlockNodes(sessionId, nodeUrls);
    }

    /**
     * Retrieves attributes of the specified mbean.
     *
     * @param nodeJmxUrl mbean server url
     * @param objectName name of mbean
     * @param attrs set of mbean attributes
     *
     * @return mbean attributes values
     */
    public Object getNodeMBeanInfo(String nodeJmxUrl, String objectName, List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException {
        checkNonEmptySession();
        return rm.getNodeMBeanInfo(sessionId, nodeJmxUrl, objectName, attrs);
    }

    /**
     * Return the statistic history contained in the node RRD database,
     * without redundancy, in a friendly JSON format.
     *
     * @param nodeJmxUrl mbean server url
     * @param objectName name of mbean
     * @param attrs set of mbean attributes
     * @param range a String of 5 chars, one for each stat history source, indicating the time range to fetch
     *      for each source. Each char can be:<ul>
     *            <li>'a' 1 minute
     *            <li>'n' 5 minutes
     *            <li>'m' 10 minutes
     *            <li>'t' 30 minutes
     *            <li>'h' 1 hour
     *            <li>'j' 2 hours
     *            <li>'k' 4 hours
     *            <li>'H' 8 hours
     *            <li>'d' 1 day
     *            <li>'w' 1 week
     *            <li>'M' 1 month
     *            <li>'y' 1 year</ul>
     * @return a JSON object containing a key for each source
     */
    public String getNodeMBeanHistory(String nodeJmxUrl, String objectName, List<String> attrs, String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {
        checkNonEmptySession();
        return rm.getNodeMBeanHistory(sessionId, nodeJmxUrl, objectName, attrs, range);
    }

    /**
     * Return the statistic history of a list of nodes contained in the node RRD database,
     * without redundancy, in a friendly JSON format.
     *
     * @param nodesJmxUrl a set of mbean server urls
     * @param objectName name of mbean
     * @param attrs set of mbean attributes
     * @param range a String of 5 chars, one for each stat history source, indicating the time range to fetch
     *      for each source. Each char can be:<ul>
     *            <li>'a' 1 minute
     *            <li>'n' 5 minutes
     *            <li>'m' 10 minutes
     *            <li>'t' 30 minutes
     *            <li>'h' 1 hour
     *            <li>'j' 2 hours
     *            <li>'k' 4 hours
     *            <li>'H' 8 hours
     *            <li>'d' 1 day
     *            <li>'w' 1 week
     *            <li>'M' 1 month
     *            <li>'y' 1 year</ul>
     * @return a Map where each entry containse mbean server url as key and a map of statistic values as value. Each entry has an mbean attribute as key and
     * list of its mbean values as value.
     */
    public Map<String, Map<String, Object>> getNodesMBeanHistory(List<String> nodesJmxUrl, String objectName,
            List<String> attrs, String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {
        checkNonEmptySession();
        return rm.getNodesMBeanHistory(sessionId, nodesJmxUrl, objectName, attrs, range);
    }

    /**
     * Retrieves attributes of the specified mbeans.
     *
     * @param objectNames mbean names (@see ObjectName format)
     * @param nodeJmxUrl mbean server url
     * @param attrs set of mbean attributes
     *
     * @return mbean attributes values
     */
    public Object getNodeMBeansInfo(String nodeJmxUrl, String objectNames, List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException {
        checkNonEmptySession();
        return rm.getNodeMBeanInfo(sessionId, nodeJmxUrl, objectNames, attrs);
    }

    public Object getNodeMBeansHistory(String nodeJmxUrl, String objectNames, List<String> attrs, String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException,
            PermissionRestException {
        checkNonEmptySession();
        return rm.getNodeMBeansHistory(sessionId, nodeJmxUrl, objectNames, attrs, range);
    }

    /**
     * Initiate the shutdowns the resource manager. During the shutdown resource
     * manager removed all the nodes and kills them if necessary.
     * RMEvent(SHUTDOWN) will be send when the shutdown is finished.
     *
     * @param preempt if true shutdown immediatly whithout waiting for nodes to be freed, default value is false
     * @return true if the shutdown process is successfully triggered, runtime exception otherwise
     */
    public boolean shutdown(boolean preempt) throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.shutdown(sessionId, preempt);
    }

    public TopologyData getTopology() throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.getTopology(sessionId);
    }

    /**
     * @return the list of supported node source infrastructures descriptors
     */
    public Collection<PluginDescriptorData> getSupportedNodeSourceInfrastructures()
            throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.getSupportedNodeSourceInfrastructures(sessionId);
    }

    /**
     * @return a mapping which for each infrastructure provides list of policies
     * that can work together with given infrastructure
     */
    public Map<String, List<String>> getInfrasToPoliciesMapping()
            throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.getInfrasToPoliciesMapping(sessionId);
    }

    /**
     * @return the list of supported node source policies descriptors
     */
    public Collection<PluginDescriptorData> getSupportedNodeSourcePolicies()
            throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.getSupportedNodeSourcePolicies(sessionId);
    }

    public NodeSourceConfiguration getNodeSourceConfiguration(String nodeSourceName)
            throws NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.getNodeSourceConfiguration(sessionId, nodeSourceName);
    }

    /**
     * Returns the attributes <code>attr</code> of the mbean
     * registered as <code>name</code>.
     * @param name mbean's object name
     * @param attrs attributes to enumerate
     * @return returns the attributes of the mbean
     */
    public Object getMBeanInfo(ObjectName name, List<String> attrs) throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, IOException, NotConnectedException, PermissionRestException {
        checkNonEmptySession();
        return rm.getMBeanInfo(sessionId, name, attrs);
    }

    /**
     * Set a single JMX attribute of the MBean <code>name</code>.
     * Only integer and string attributes are currently supported, see <code>type</code>.
     *
     * @param name      the object name of the MBean
     * @param type      the type of the attribute to set ('integer' and 'string' are currently supported, see <code>RMProxyUserInterface</code>)
     * @param attr      the name of the attribute to set
     * @param value     the new value of the attribute (defined as a String, it is automatically converted according to <code>type</code>)
     */
    public void setMBeanInfo(ObjectName name, String type, String attr, String value)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException {
        checkNonEmptySession();
        rm.setMBeanInfo(sessionId, name, type, attr, value);
    }

    /**
     * Return the statistic history contained in the RM's RRD database,
     * without redundancy, in a friendly JSON format.
     *
     * The following sources will be queried from the RRD DB :<pre>
     * 	{ AvailableNodesCount",
     * 	"FreeNodesCount",
     * 	"NeededNodesCount",
     * 	"BusyNodesCount",
     * 	"DeployingNodesCount",
     * 	"ConfigNodesCount",
     * 	"DownNodesCount",
     * 	"LostNodesCount",
     * 	"AverageActivity" };
     * 	</pre>
     *
     *
     * @param range a String of 9 chars, one for each stat history source, indicating the time range to fetch
     *      for each source. Each char can be:<ul>
     *            <li>'a' 1 minute</li>
     *            <li>'m' 10 minutes</li>
     *            <li>'n' 5 minutes</li>
     *            <li>'t' 30 minutes</li>
     *            <li>'h' 1 hour</li>
     *            <li>'j' 2 hours</li>
     *            <li>'k' 4 hours</li>
     *            <li>'H' 8 hours</li>
     *            <li>'d' 1 day</li>
     *            <li>'w' 1 week</li>
     *            <li>'M' 1 month</li>
     *            <li>'y' 1 year</li></ul>
     * @return a JSON object containing a key for each source
     */
    public String getStatHistory(String range) throws ReflectionException, InterruptedException, IntrospectionException,
            NotConnectedException, InstanceNotFoundException, MalformedObjectNameException, IOException {
        checkNonEmptySession();
        return rm.getStatHistory(sessionId, range, ConsolFun.AVERAGE.name());
    }

    /**
     * @return returns the version of the rest api
     */
    public String getVersion() {
        return rm.getVersion();
    }

    public ScriptResult<Object> executeNodeScript(String nodeUrl, String script, String scriptEngine) throws Throwable {
        checkNonEmptySession();
        return rm.executeNodeScript(sessionId, nodeUrl, script, scriptEngine);
    }

    public List<ScriptResult<Object>> executeNodeSourceScript(String nodeSource, String script, String scriptEngine)
            throws Throwable {
        checkNonEmptySession();
        return rm.executeNodeSourceScript(sessionId, nodeSource, script, scriptEngine);
    }

    public ScriptResult<Object> executeHostScript(String host, String script, String scriptEngine) throws Throwable {
        checkNonEmptySession();
        return rm.executeNodeScript(sessionId, host, script, scriptEngine);
    }

    public String getRMThreadDump() throws NotConnectedException {
        checkNonEmptySession();
        return rm.getRMThreadDump(sessionId);
    }

    public String getNodeThreadDump(String nodeUrl) throws NotConnectedException {
        checkNonEmptySession();
        return rm.getNodeThreadDump(sessionId, nodeUrl);
    }

    public Map<String, Map<String, Map<String, List<RMNodeHistory>>>> getNodesHistory(long windowStart, long windowEnd)
            throws NotConnectedException {
        checkNonEmptySession();
        return rm.getNodesHistory(sessionId, windowStart, windowEnd);
    }

    /**
     * Add access token to given node
     * @param token single token
     */
    public void addNodeToken(String nodeUrl, String token) throws NotConnectedException, RestException {
        checkNonEmptySession();
        rm.addNodeToken(sessionId, nodeUrl, token);
    }

    /**
     * Remove access token from given node
     * @param token single token
     */
    public void removeNodeToken(String nodeUrl, String token) throws NotConnectedException, RestException {
        checkNonEmptySession();
        rm.removeNodeToken(sessionId, nodeUrl, token);
    }

    /**
     * Set all node tokens to given node
     */
    public void setNodeTokens(String nodeUrl, List<String> tokens) throws NotConnectedException, RestException {
        checkNonEmptySession();
        rm.setNodeTokens(sessionId, nodeUrl, tokens);
    }
}
