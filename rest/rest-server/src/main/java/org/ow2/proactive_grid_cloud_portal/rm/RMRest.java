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
package org.ow2.proactive_grid_cloud_portal.rm;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.ow2.proactive.utils.Lambda.mapKeys;
import static org.ow2.proactive.utils.Lambda.mapValues;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.security.auth.login.LoginException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.NSState;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeHistory;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateDelta;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateFull;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXBeans;
import org.ow2.proactive.resourcemanager.exception.RMActiveObjectCreationException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.exception.RMNodeException;
import org.ow2.proactive.resourcemanager.frontend.PluginDescriptorData;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyData;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyImpl;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;
import org.ow2.proactive.resourcemanager.nodesource.common.NodeSourceConfiguration;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.utils.TargetType;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scripting.ScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.console.MBeanInfoViewer;
import org.ow2.proactive_grid_cloud_portal.common.*;
import org.ow2.proactive_grid_cloud_portal.common.StatHistoryCaching.StatHistoryCacheEntry;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.RestException;
import org.ow2.proactive_grid_cloud_portal.webapp.StatHistory;


public class RMRest implements RMRestInterface {

    protected static final String[] dataSources = { "AvailableNodesCount", "FreeNodesCount", "NeededNodesCount",
                                                    "BusyNodesCount", "DeployingNodesCount", "ConfigNodesCount", // it is Config and not Configuring because RRDDataStore limits name 20 characters
                                                    "DownNodesCount", "LostNodesCount", "AverageActivity" };

    public static final Logger LOGGER = Logger.getLogger(RMRest.class);

    private SessionStore sessionStore = SharedSessionStore.getInstance();

    private static final Pattern PATTERN = Pattern.compile("^[^:]*:(.*)");

    private RMProxyUserInterface checkAccess(String sessionId) throws NotConnectedException {
        Session session = null;
        try {
            session = sessionStore.get(sessionId);
        } catch (NotConnectedRestException e) {
            throw new NotConnectedException(SessionStore.YOU_ARE_NOT_CONNECTED_TO_THE_SERVER_YOU_SHOULD_LOG_ON_FIRST);
        }
        RMProxyUserInterface s = session.getRM();

        if (s == null) {
            throw new NotConnectedException("you are not connected to the resource manager, you should log on first");
        }

        return session.getRM();
    }

    @Override
    public String rmConnect(String username, String password)
            throws KeyException, LoginException, RMException, RMActiveObjectCreationException, RMNodeException {

        Session session = sessionStore.create(username);
        try {
            session.connectToRM(new CredData(CredData.parseLogin(username), CredData.parseDomain(username), password));
        } catch (ActiveObjectCreationException e) {
            throw new RMActiveObjectCreationException(e);
        } catch (NodeException e) {
            throw new RMNodeException(e);
        }
        return session.getSessionId();

    }

    @Override
    public void rmDisconnect(String sessionId) throws NotConnectedException {
        RMProxyUserInterface rm = checkAccess(sessionId);
        rm.disconnect();
        sessionStore.terminate(sessionId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive_grid_cloud_portal.SchedulerRestInterface#loginWithCredential(org.ow2.
     * proactive_grid_cloud_portal.LoginForm)
     */
    @Override
    public String loginWithCredential(@MultipartForm LoginForm multipart) throws RMActiveObjectCreationException,
            RMNodeException, KeyException, IOException, LoginException, RMException {

        Session session;
        try {

            if (multipart.getCredential() != null) {
                session = sessionStore.createUnnamedSession();
                Credentials credentials = Credentials.getCredentials(multipart.getCredential());
                session.connectToRM(credentials);
            } else {
                session = sessionStore.create(multipart.getUsername());
                CredData credData = new CredData(CredData.parseLogin(multipart.getUsername()),
                                                 CredData.parseDomain(multipart.getUsername()),
                                                 multipart.getPassword(),
                                                 multipart.getSshKey());
                session.connectToRM(credData);

            }

        } catch (ActiveObjectCreationException e) {
            throw new RMActiveObjectCreationException(e);
        } catch (NodeException e) {
            throw new RMNodeException(e);
        }
        return session.getSessionId();
    }

    @Override
    public String getLoginFromSessionId(String sessionId) {
        try {
            return sessionStore.get(sessionId).getUserName();
        } catch (Exception e) {
            LOGGER.debug("Invalid session : " + sessionId);
            return "";
        }
    }

    @Override
    public UserData getUserDataFromSessionId(String sessionId) {
        if (sessionId != null && sessionStore.exists(sessionId)) {
            try {
                ResourceManager rm = sessionStore.get(sessionId).getRM();
                return PAFuture.getFutureValue(rm.getCurrentUserData());
            } catch (Exception e) {
                LOGGER.debug("Invalid session : " + sessionId);
                return null;
            }
        } else {
            LOGGER.debug("Invalid session : " + sessionId);
            return null;
        }
    }

    @Override
    public RMState getState(String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getState());
    }

    @Override
    public RMStateDelta getRMStateDelta(String sessionId, String clientCounter)
            throws NotConnectedException, PermissionRestException {
        checkAccess(sessionId);
        long counter = Integer.valueOf(clientCounter);
        return orThrowRpe(RMStateCaching.getRMStateDelta(counter));
    }

    @Override
    public RMStateFull getRMStateFull(String sessionId) throws NotConnectedException, PermissionRestException {
        checkAccess(sessionId);
        return orThrowRpe(RMStateCaching.getRMStateFull());
    }

    @Override
    public String getModelHosts() throws PermissionRestException {
        RMStateFull state = orThrowRpe(RMStateCaching.getRMStateFull());
        return String.format("PA:LIST(,%s)", state.getNodesEvents()
                                                  .stream()
                                                  .map(RMNodeEvent::getHostName)
                                                  .distinct()
                                                  .filter(hostName -> !hostName.isEmpty())
                                                  .collect(Collectors.joining(",")));
    }

    @Override
    public String getModelNodeSources() throws PermissionRestException {
        RMStateFull state = orThrowRpe(RMStateCaching.getRMStateFull());
        return String.format("PA:LIST(,%s,%s)", RMConstants.DEFAULT_STATIC_SOURCE_NAME, state.getNodeSource()
                                                                                             .stream()
                                                                                             .map(RMNodeSourceEvent::getNodeSourceName)
                                                                                             .distinct()
                                                                                             .filter(nodeSourceName -> !nodeSourceName.isEmpty() &&
                                                                                                                       !nodeSourceName.equals(RMConstants.DEFAULT_STATIC_SOURCE_NAME))
                                                                                             .collect(Collectors.joining(",")));
    }

    @Override
    public boolean isActive(String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.isActive().getBooleanValue();
    }

    @Override
    public boolean addNode(String sessionId, String url, String nodesource) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        if (nodesource == null) {
            return rm.addNode(url).getBooleanValue();
        } else {
            return rm.addNode(url, nodesource).getBooleanValue();
        }
    }

    @Override
    public boolean nodeIsAvailable(String sessionId, String url) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.nodeIsAvailable(url).getBooleanValue();
    }

    @Override
    public List<RMNodeSourceEvent> getExistingNodeSources(String sessionId)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRpe(rm.getExistingNodeSourcesList());
    }

    @Override
    public NSState defineNodeSource(String sessionId, String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters, String nodesRecoverable)
            throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        NSState nsState = new NSState();

        Object[] infraParams = this.getAllInfrastructureParameters(infrastructureType,
                                                                   infrastructureParameters,
                                                                   infrastructureFileParameters,
                                                                   rm);
        Object[] policyParams = this.getAllPolicyParameters(policyType, policyParameters, policyFileParameters, rm);

        try {
            nsState.setResult(rm.defineNodeSource(nodeSourceName,
                                                  infrastructureType,
                                                  infraParams,
                                                  policyType,
                                                  policyParams,
                                                  Boolean.parseBoolean(nodesRecoverable))
                                .getBooleanValue());
        } catch (RuntimeException ex) {
            nsState.setResult(false);
            nsState.setErrorMessage(cleanDisplayedErrorMessage(ex.getMessage()));
            nsState.setStackTrace(StringEscapeUtils.escapeJson(getStackTrace(ex)));
        }

        return nsState;
    }

    @Override
    public NSState editNodeSource(String sessionId, String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters, String nodesRecoverable)
            throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        NSState nsState = new NSState();

        Object[] infraParams = this.getAllInfrastructureParameters(infrastructureType,
                                                                   infrastructureParameters,
                                                                   infrastructureFileParameters,
                                                                   rm);
        Object[] policyParams = this.getAllPolicyParameters(policyType, policyParameters, policyFileParameters, rm);

        try {
            nsState.setResult(rm.editNodeSource(nodeSourceName,
                                                infrastructureType,
                                                infraParams,
                                                policyType,
                                                policyParams,
                                                Boolean.parseBoolean(nodesRecoverable))
                                .getBooleanValue());
        } catch (RuntimeException ex) {
            nsState.setResult(false);
            nsState.setErrorMessage(cleanDisplayedErrorMessage(ex.getMessage()));
            nsState.setStackTrace(StringEscapeUtils.escapeJson(getStackTrace(ex)));
        }

        return nsState;
    }

    @Override
    public NSState updateDynamicParameters(String sessionId, String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters) throws NotConnectedException {

        ResourceManager rm = checkAccess(sessionId);
        NSState nsState = new NSState();

        Object[] infraParams = this.getAllInfrastructureParameters(infrastructureType,
                                                                   infrastructureParameters,
                                                                   infrastructureFileParameters,
                                                                   rm);

        Object[] policyParams = this.getAllPolicyParameters(policyType, policyParameters, policyFileParameters, rm);

        try {
            nsState.setResult(rm.updateDynamicParameters(nodeSourceName,
                                                         infrastructureType,
                                                         infraParams,
                                                         policyType,
                                                         policyParams)
                                .getBooleanValue());
        } catch (RuntimeException ex) {
            nsState.setResult(false);
            nsState.setErrorMessage(cleanDisplayedErrorMessage(ex.getMessage()));
            nsState.setStackTrace(StringEscapeUtils.escapeJson(getStackTrace(ex)));
        }

        return nsState;
    }

    @Deprecated
    @Override
    public NSState createNodeSource(String sessionId, String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters)
            throws NotConnectedException, PermissionRestException {
        return orThrowRpe(createNodeSource(sessionId,
                                           nodeSourceName,
                                           infrastructureType,
                                           infrastructureParameters,
                                           infrastructureFileParameters,
                                           policyType,
                                           policyParameters,
                                           policyFileParameters,
                                           Boolean.TRUE.toString()));
    }

    @Deprecated
    @Override
    public NSState createNodeSource(String sessionId, String nodeSourceName, String infrastructureType,
            String[] infrastructureParameters, String[] infrastructureFileParameters, String policyType,
            String[] policyParameters, String[] policyFileParameters, String nodesRecoverable)
            throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        NSState nsState = new NSState();

        Object[] allInfrastructureParameters = this.getAllInfrastructureParameters(infrastructureType,
                                                                                   infrastructureParameters,
                                                                                   infrastructureFileParameters,
                                                                                   rm);

        Object[] allPolicyParameters = this.getAllPolicyParameters(policyType,
                                                                   policyParameters,
                                                                   policyFileParameters,
                                                                   rm);

        try {
            nsState.setResult(rm.createNodeSource(nodeSourceName,
                                                  infrastructureType,
                                                  allInfrastructureParameters,
                                                  policyType,
                                                  allPolicyParameters,
                                                  Boolean.parseBoolean(nodesRecoverable))
                                .getBooleanValue());
        } catch (RuntimeException ex) {
            nsState.setResult(false);
            nsState.setErrorMessage(cleanDisplayedErrorMessage(ex.getMessage()));
            nsState.setStackTrace(StringEscapeUtils.escapeJson(getStackTrace(ex)));

        } finally {
            return nsState;
        }

    }

    private String cleanDisplayedErrorMessage(String errorMessage) {
        Matcher matcher = PATTERN.matcher(errorMessage);
        if (matcher.find()) {
            errorMessage = matcher.group(1);
        }
        return (errorMessage);
    }

    @Override
    public NSState deployNodeSource(String sessionId, String nodeSourceName)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        NSState nsState = new NSState();
        try {
            nsState.setResult(rm.deployNodeSource(nodeSourceName).getBooleanValue());
        } catch (RuntimeException ex) {
            nsState.setResult(false);
            nsState.setErrorMessage(cleanDisplayedErrorMessage(ex.getMessage()));
            nsState.setStackTrace(StringEscapeUtils.escapeJson(getStackTrace(ex)));
        }
        return nsState;
    }

    @Override
    public NSState undeployNodeSource(String sessionId, String nodeSourceName, boolean preempt)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        NSState nsState = new NSState();
        try {
            nsState.setResult(rm.undeployNodeSource(nodeSourceName, preempt).getBooleanValue());
        } catch (RuntimeException ex) {
            nsState.setResult(false);
            nsState.setErrorMessage(cleanDisplayedErrorMessage(ex.getMessage()));
            nsState.setStackTrace(StringEscapeUtils.escapeJson(getStackTrace(ex)));
        }
        return nsState;
    }

    @Override
    public int getNodeSourcePingFrequency(String sessionId, String sourceName)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRpe(rm.getNodeSourcePingFrequency(sourceName).getIntValue());
    }

    @Override
    public boolean releaseNode(String sessionId, String url)
            throws RMNodeException, NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        Node n;
        try {
            n = NodeFactory.getNode(url);
        } catch (NodeException e) {
            throw new RMNodeException(e);
        }
        return orThrowRpe(rm.releaseNode(n).getBooleanValue());
    }

    @Override
    public boolean removeNode(String sessionId, String nodeUrl, boolean preempt)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRpe(rm.removeNode(nodeUrl, preempt).getBooleanValue());
    }

    @Override
    public boolean removeNodeSource(String sessionId, String sourceName, boolean preempt)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRpe(rm.removeNodeSource(sourceName, preempt).getBooleanValue());
    }

    @Override
    public boolean lockNodes(String sessionId, Set<String> nodeUrls)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRpe(rm.lockNodes(nodeUrls).getBooleanValue());
    }

    @Override
    public boolean unlockNodes(String sessionId, Set<String> nodeUrls)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRpe(rm.unlockNodes(nodeUrls).getBooleanValue());
    }

    @Override
    public Object getNodeMBeanInfo(String sessionId, String nodeJmxUrl, String objectName, List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return orThrowRpe(rmProxy.getNodeMBeanInfo(nodeJmxUrl, objectName, attrs));
    }

    @Override
    public String getNodeMBeanHistory(String sessionId, String nodeJmxUrl, String objectName, List<String> attrs,
            String range) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return rmProxy.getNodeMBeanHistory(nodeJmxUrl, objectName, attrs, range);
    }

    @Override
    public Map<String, Map<String, Object>> getNodesMBeanHistory(String sessionId, List<String> nodesJmxUrl,
            String objectName, List<String> attrs, String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return nodesJmxUrl.stream().collect(Collectors.toMap(nodeJmxUrl -> nodeJmxUrl, nodeJmxUrl -> {
            try {
                return processHistoryResult(rmProxy.getNodeMBeanHistory(nodeJmxUrl, objectName, attrs, range), range);
            } catch (Exception e) {
                LOGGER.error(e);
                return Collections.EMPTY_MAP;
            }
        }));
    }

    @Override
    public Object getNodeMBeansInfo(String sessionId, String nodeJmxUrl, String objectNames, List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return orThrowRpe(rmProxy.getNodeMBeansInfo(nodeJmxUrl, objectNames, attrs));
    }

    @Override
    public Object getNodeMBeansHistory(String sessionId, String nodeJmxUrl, String objectNames, List<String> attrs,
            String range) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException,
            PermissionRestException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return orThrowRpe(rmProxy.getNodeMBeansHistory(nodeJmxUrl, objectNames, attrs, range));
    }

    @Override
    public boolean shutdown(String sessionId, boolean preempt) throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRpe(rm.shutdown(preempt)).getBooleanValue();
    }

    @Override
    public TopologyData getTopology(String sessionId) throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        TopologyImpl topology = (TopologyImpl) orThrowRpe(PAFuture.getFutureValue(rm.getTopology()));
        TopologyData topologyData = new TopologyData();
        Map<String, Map<String, Long>> distances = mapValues(mapKeys(topology.getDistances(), InetAddress::toString),
                                                             map -> mapKeys(map, InetAddress::toString));
        topologyData.setDistances(distances);
        topologyData.setHosts(mapValues(topology.getHostsMap(), InetAddress::toString));
        return topologyData;
    }

    @Override
    public Collection<PluginDescriptorData> getSupportedNodeSourceInfrastructures(String sessionId)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        Collection<PluginDescriptor> pluginDescriptors = orThrowRpe(rm.getSupportedNodeSourceInfrastructures());
        return pluginDescriptors.stream().map(PluginDescriptorData::new).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> getInfrasToPoliciesMapping(String sessionId)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);

        Collection<PluginDescriptor> supportedInfrastructures = orThrowRpe(rm.getSupportedNodeSourceInfrastructures());

        Collection<PluginDescriptor> supportedPolicies = orThrowRpe(rm.getSupportedNodeSourcePolicies());

        Map<String, List<String>> result = orThrowRpe(rm.getInfrasToPoliciesMapping()).entrySet()
                                                                                      .stream()
                                                                                      .filter(entry -> supportedInfrastructures.stream()
                                                                                                                               .anyMatch(infra -> infra.getPluginName()
                                                                                                                                                       .equals(entry.getKey())))
                                                                                      .collect(Collectors.toMap(Map.Entry::getKey,
                                                                                                                Map.Entry::getValue));

        return result.entrySet().stream().map(entry -> {
            List<String> policies = entry.getValue()
                                         .stream()
                                         .filter(policy -> containsPlugin(policy, supportedPolicies))
                                         .collect(Collectors.toList());
            return new AbstractMap.SimpleEntry<>(entry.getKey(), policies);

        }).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @Override
    public Collection<PluginDescriptorData> getSupportedNodeSourcePolicies(String sessionId)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        Collection<PluginDescriptor> pluginDescriptors = orThrowRpe(rm.getSupportedNodeSourcePolicies());
        return pluginDescriptors.stream().map(PluginDescriptorData::new).collect(Collectors.toList());
    }

    @Override
    public NodeSourceConfiguration getNodeSourceConfiguration(String sessionId, String nodeSourceName)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRpe(rm.getNodeSourceConfiguration(nodeSourceName));
    }

    @Override
    public Object getMBeanInfo(String sessionId, ObjectName name, List<String> attrs) throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, IOException, NotConnectedException, PermissionRestException {
        RMProxyUserInterface rm = checkAccess(sessionId);

        if ((attrs == null) || (attrs.size() == 0)) {
            // no attribute is requested, we return
            // the description of the mbean
            return orThrowRpe(rm.getMBeanInfo(name));

        } else {
            return orThrowRpe(rm.getMBeanAttributes(name, attrs.toArray(new String[attrs.size()])));
        }
    }

    @Override
    public void setMBeanInfo(String sessionId, ObjectName name, String type, String attr, String value)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MBeanException, AttributeNotFoundException, InvalidAttributeValueException,
            IllegalArgumentException {
        RMProxyUserInterface rm = checkAccess(sessionId);
        if ((type != null) && (attr != null) && (value != null)) {
            rm.setMBeanAttribute(name, type, attr, value);
        }
    }

    @Override
    public String getStatHistory(String sessionId, String range1)
            throws ReflectionException, InterruptedException, IntrospectionException, NotConnectedException,
            InstanceNotFoundException, MalformedObjectNameException, IOException {

        String newRange = MBeanInfoViewer.possibleModifyRange(range1, dataSources, 'a');

        StatHistoryCacheEntry entry = StatHistoryCaching.getInstance().getEntryOrCompute(newRange, () -> {
            RMProxyUserInterface rm = checkAccess(sessionId);

            AttributeList attrs = rm.getMBeanAttributes(new ObjectName(RMJMXBeans.RUNTIMEDATA_MBEAN_NAME),
                                                        new String[] { "StatisticHistory" });

            Attribute attr = (Attribute) attrs.get(0);

            // content of the RRD4J database backing file
            byte[] rrd4j = (byte[]) attr.getValue();

            return MBeanInfoViewer.rrdContent(rrd4j, newRange, dataSources);
        });

        return entry.getValue();
    }

    @Override
    public String getVersion() {
        return String.format("{ \"rm\" : \"%s\", \"rest\" : \"%s\"}",
                             RMRest.class.getPackage().getSpecificationVersion(),
                             RMRest.class.getPackage().getImplementationVersion());
    }

    @Override
    public ScriptResult<Object> executeNodeScript(String sessionId, String nodeUrl, String script, String scriptEngine)
            throws Throwable {

        RMProxyUserInterface rm = checkAccess(sessionId);

        List<ScriptResult<Object>> results = orThrowRpe(rm.executeScript(script,
                                                                         scriptEngine,
                                                                         TargetType.NODE_URL.name(),
                                                                         Collections.singleton(nodeUrl)));
        checkEmptyScriptResults(results);

        return results.get(0);
    }

    @Override
    public List<ScriptResult<Object>> executeNodeSourceScript(String sessionId, String nodeSource, String script,
            String scriptEngine) throws Throwable {
        RMProxyUserInterface rm = checkAccess(sessionId);

        List<ScriptResult<Object>> results = orThrowRpe(rm.executeScript(script,
                                                                         scriptEngine,
                                                                         TargetType.NODESOURCE_NAME.name(),
                                                                         Collections.singleton(nodeSource)));

        List<ScriptResult<Object>> awaitedResults = results.stream()
                                                           .map(PAFuture::getFutureValue)
                                                           .collect(Collectors.toList());

        checkEmptyScriptResults(awaitedResults);

        return awaitedResults;
    }

    @Override
    public ScriptResult<Object> executeHostScript(String sessionId, String hostname, String script, String scriptEngine)
            throws Throwable {

        RMProxyUserInterface rm = checkAccess(sessionId);

        List<ScriptResult<Object>> results = orThrowRpe(rm.executeScript(script,
                                                                         scriptEngine,
                                                                         TargetType.HOSTNAME.name(),
                                                                         Collections.singleton(hostname)));
        checkEmptyScriptResults(results);

        return results.get(0);
    }

    private void checkEmptyScriptResults(List<ScriptResult<Object>> results) {
        if (results.isEmpty()) {
            results.add(new ScriptResult<>(new ScriptException("No results from script execution")));
        }
    }

    @GET
    @Path("/")
    public Response index() throws URISyntaxException {
        return Response.seeOther(new URI("doc/jaxrsdocs/rm/index.html")).build();
    }

    @Override
    public String getRMThreadDump(String sessionId) throws NotConnectedException {
        RMProxyUserInterface rm = checkAccess(sessionId);
        String threadDump;
        try {
            threadDump = orThrowRpe(rm.getRMThreadDump().getStringValue());
        } catch (Exception exception) {
            return exception.getMessage();
        }
        return threadDump;
    }

    @Override
    public String getNodeThreadDump(String sessionId, String nodeUrl) throws NotConnectedException {
        RMProxyUserInterface rm = checkAccess(sessionId);
        String threadDump;
        try {
            threadDump = orThrowRpe(rm.getNodeThreadDump(nodeUrl).getStringValue());
        } catch (Exception exception) {
            return exception.getMessage();
        }
        return threadDump;
    }

    @Override
    public Map<String, Map<String, Map<String, List<RMNodeHistory>>>> getNodesHistory(String sessionId,
            long windowStart, long windowEnd) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);

        List<RMNodeHistory> rawDataFromRM = rm.getNodesHistory(windowStart, windowEnd);

        // grouped by node source name, host name, and node name
        Map<String, Map<String, Map<String, List<RMNodeHistory>>>> grouped = rawDataFromRM.stream()
                                                                                          .collect(Collectors.groupingBy(RMNodeHistory::getNodeSource,
                                                                                                                         Collectors.groupingBy(RMNodeHistory::getHost,
                                                                                                                                               Collectors.groupingBy(this::getNodeName))));
        grouped.values().forEach(a -> a.values().forEach(b -> {
            b.values().forEach(c -> {
                // sorting by startTime
                c.sort(Comparator.comparing(RMNodeHistory::getStartTime));

                c.forEach(nh -> {
                    // if startTime before window
                    if (nh.getStartTime() < windowStart) {
                        nh.setStartTime(windowStart);
                    }

                    // if endTime after window
                    if (windowEnd < nh.getEndTime()) {
                        nh.setEndTime(windowEnd);
                    }

                    if (nh.getEndTime() == 0) {
                        nh.setEndTime(windowEnd);
                    }
                });
            });
        }));

        return grouped;
    }

    @Override
    public void addNodeToken(String sessionId, String nodeUrl, String token)
            throws NotConnectedException, RestException {
        ResourceManager rm = checkAccess(sessionId);
        try {
            rm.addNodeToken(nodeUrl, token);
        } catch (SecurityException e) {
            throw new PermissionRestException(e);
        } catch (RMException e) {
            throw new RestException(e);
        }
    }

    @Override
    public void removeNodeToken(String sessionId, String nodeUrl, String token)
            throws NotConnectedException, RestException {
        ResourceManager rm = checkAccess(sessionId);
        try {
            rm.removeNodeToken(nodeUrl, token);
        } catch (SecurityException e) {
            throw new PermissionRestException(e);
        } catch (RMException e) {
            throw new RestException(e);
        }
    }

    @Override
    public void setNodeTokens(String sessionId, String nodeUrl, List<String> tokens)
            throws NotConnectedException, RestException {
        ResourceManager rm = checkAccess(sessionId);
        try {
            rm.setNodeTokens(nodeUrl, tokens);
        } catch (SecurityException e) {
            throw new PermissionRestException(e);
        } catch (RMException e) {
            throw new RestException(e);
        }
    }

    private String getNodeName(RMNodeHistory rmNodeHistory) {
        String nodeUrl = rmNodeHistory.getNodeUrl();
        if (nodeUrl.contains("/")) {
            return nodeUrl.substring(nodeUrl.lastIndexOf("/") + 1);
        } else {
            return nodeUrl;
        }
    }

    private Object[] getAllInfrastructureParameters(String infrastructureType, String[] infrastructureParameters,
            String[] infrastructureFileParameters, ResourceManager rm) {
        for (PluginDescriptor infrastructureDescriptor : rm.getSupportedNodeSourceInfrastructures()) {
            if (infrastructureDescriptor.getPluginName().equals(infrastructureType)) {
                Collection<ConfigurableField> infrastructureFields = infrastructureDescriptor.getConfigurableFields();
                return this.concatenateParametersAndFileParameters(infrastructureParameters,
                                                                   infrastructureFileParameters,
                                                                   infrastructureFields);
            }
        }

        throw new IllegalArgumentException("Infrastructure " + infrastructureType + " is unknown");
    }

    private Object[] getAllPolicyParameters(String policyType, String[] policyParameters, String[] policyFileParameters,
            ResourceManager rm) {
        for (PluginDescriptor policyDescriptor : rm.getSupportedNodeSourcePolicies()) {
            if (policyDescriptor.getPluginName().equals(policyType)) {
                Collection<ConfigurableField> policyFields = policyDescriptor.getConfigurableFields();
                return this.concatenateParametersAndFileParameters(policyParameters,
                                                                   policyFileParameters,
                                                                   policyFields);
            }
        }

        throw new IllegalArgumentException("Policy " + policyType + " is unknown");
    }

    private Object[] concatenateParametersAndFileParameters(String[] parameters, String[] fileParameters,
            Collection<ConfigurableField> fields) {

        int parametersLength = this.getParametersLength(parameters);
        int fileParametersLength = this.getParametersLength(fileParameters);

        Object[] concatenatedParameters = new Object[parametersLength + fileParametersLength];

        int parametersIndex = 0;
        int fileParametersIndex = 0;
        int concatenatedParametersIndex = 0;

        for (ConfigurableField field : fields) {
            if (field.getMeta().credential() || field.getMeta().fileBrowser()) {
                concatenatedParameters[concatenatedParametersIndex] = fileParameters[fileParametersIndex].getBytes();
                fileParametersIndex++;
            } else {
                concatenatedParameters[concatenatedParametersIndex] = parameters[parametersIndex];
                parametersIndex++;
            }
            concatenatedParametersIndex++;
        }

        return concatenatedParameters;
    }

    private int getParametersLength(String[] parameters) {
        if (parameters != null) {
            return parameters.length;
        } else {
            return 0;
        }
    }

    private Map<String, Object> processHistoryResult(String mbeanHistory, String range) throws IOException {
        mbeanHistory = removingInternalEscaping(mbeanHistory);
        Map<String, Object> jsonMap = parseJSON(mbeanHistory);
        if (jsonMap == null) {
            return null;
        }
        Map<String, Object> processedJsonMap = new HashMap<>();
        long timeStamp = new Date().getTime() / 1000;
        long duration = StatHistory.Range.valueOf(range).getDuration();
        int size = Optional.ofNullable(((ArrayList) jsonMap.entrySet().iterator().next().getValue()).size()).orElse(1);
        long step = duration / size;

        List<Long> labels = new ArrayList<>(size + 1);
        for (int i = 0; i <= size; i++) {
            //The time instant where the metric is taken
            long t = timeStamp - duration + step * i;
            labels.add(t * 1000);
        }
        JSONObject measureInfo;
        JSONArray measureInfoList;
        for (String key : jsonMap.keySet()) {
            ArrayList values = (ArrayList) jsonMap.get(key);
            measureInfoList = new JSONArray();
            for (int i = 0; i < size; i++) {
                measureInfo = new JSONObject();
                measureInfo.put("value", values.get(i));
                measureInfo.put("from", labels.get(i));
                measureInfo.put("to", labels.get(i + 1));
                measureInfoList.add(measureInfo);
            }
            processedJsonMap.put(key, measureInfoList);
        }
        return processedJsonMap;
    }

    private String removingInternalEscaping(String result) {
        result = result.replace("\\\"", "\"");
        result = result.replace("\"{", "{");
        result = result.replace("}\"", "}");
        return result;
    }

    private Map<String, Object> parseJSON(final String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            final JsonParser parser = mapper.getJsonFactory().createJsonParser(jsonString);
            while (parser.nextToken() != null) {
            }
        } catch (Exception jpe) {
            throw new RuntimeException("Invalid JSON: " + jpe.getMessage(), jpe);
        }
        return mapper.readValue(jsonString, Map.class);
    }

    private boolean containsPlugin(String pluginName, Collection<PluginDescriptor> plugins) {
        for (PluginDescriptor supportedPolicy : plugins) {
            if (supportedPolicy.getPluginName().equals(pluginName)) {
                return true;
            }
        }
        return false;
    }

    private <T> T orThrowRpe(T future) throws PermissionRestException {
        try {
            PAFuture.getFutureValue(future);
            return future;
        } catch (SecurityException e) {
            throw new PermissionRestException(e);
        }
    }

}
