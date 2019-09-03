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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyException;
import java.text.SimpleDateFormat;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.annotations.GZIP;
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
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeHistory;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateDelta;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateFull;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXBeans;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;
import org.ow2.proactive.resourcemanager.nodesource.common.NodeSourceConfiguration;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.utils.TargetType;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scripting.ScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.console.MBeanInfoViewer;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.common.SessionStore;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import org.ow2.proactive_grid_cloud_portal.common.StatHistoryCaching;
import org.ow2.proactive_grid_cloud_portal.common.StatHistoryCaching.StatHistoryCacheEntry;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.webapp.StatHistory;


@Path("/rm")
public class RMRest implements RMRestInterface {

    protected static final String[] dataSources = { "AvailableNodesCount", "FreeNodesCount", "NeededNodesCount",
                                                    "BusyNodesCount", "DeployingNodesCount", "ConfigNodesCount", // it is Config and not Configuring because RRDDataStore limits name 20 characters
                                                    "DownNodesCount", "LostNodesCount", "AverageActivity" };

    private SessionStore sessionStore = SharedSessionStore.getInstance();

    private static final Pattern PATTERN = Pattern.compile("^[^:]*:(.*)");

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private RMProxyUserInterface checkAccess(String sessionId) throws NotConnectedException {
        if (sessionId == null) {
            throw new NotConnectedException("you did not provide 'sessionid' in the header");
        }
        Session session = sessionStore.get(sessionId);
        if (session == null) {
            throw new NotConnectedException("you are not connected to the scheduler, you should log on first");
        }
        RMProxyUserInterface s = session.getRM();

        if (s == null) {
            throw new NotConnectedException("you are not connected to the scheduler, you should log on first");
        }

        return session.getRM();
    }

    @Override
    public String rmConnect(@FormParam("username") String username, @FormParam("password") String password)
            throws KeyException, LoginException, RMException, ActiveObjectCreationException, NodeException {

        Session session = sessionStore.create(username);
        session.connectToRM(new CredData(CredData.parseLogin(username), CredData.parseDomain(username), password));
        return session.getSessionId();

    }

    @Override
    public void rmDisconnect(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
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
    public String loginWithCredential(@MultipartForm LoginForm multipart) throws ActiveObjectCreationException,
            NodeException, KeyException, IOException, LoginException, RMException {

        Session session;
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

        return session.getSessionId();
    }

    @Override
    public String getLoginFromSessionId(String sessionId) {
        if (sessionId != null && sessionStore.exists(sessionId)) {
            return sessionStore.get(sessionId).getUserName();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserData getUserDataFromSessionId(@PathParam("sessionId") String sessionId) {
        if (sessionId != null && sessionStore.exists(sessionId)) {
            try {
                ResourceManager rm = sessionStore.get(sessionId).getRM();
                return PAFuture.getFutureValue(rm.getCurrentUserData());
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public RMState getState(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getState());
    }

    @Override
    public RMStateDelta getRMStateDelta(@HeaderParam("sessionid") String sessionId,
            @HeaderParam("clientCounter") @DefaultValue("-1") String clientCounter)
            throws NotConnectedException, PermissionRestException {
        checkAccess(sessionId);
        long counter = Integer.valueOf(clientCounter);
        return orThrowRPE(RMStateCaching.getRMStateDelta(counter));
    }

    @Override
    public RMStateFull getRMStateFull(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException {
        checkAccess(sessionId);
        return orThrowRPE(RMStateCaching.getRMStateFull());
    }

    @Override
    public boolean isActive(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.isActive().getBooleanValue();
    }

    @Override
    public boolean addNode(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurl") String url,
            @FormParam("nodesource") String nodesource) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        if (nodesource == null) {
            return rm.addNode(url).getBooleanValue();
        } else {
            return rm.addNode(url, nodesource).getBooleanValue();
        }
    }

    @Override
    public boolean nodeIsAvailable(@HeaderParam("sessionid") String sessionId, @QueryParam("nodeurl") String url)
            throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.nodeIsAvailable(url).getBooleanValue();
    }

    @Override
    public List<RMNodeSourceEvent> getExistingNodeSources(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(rm.getExistingNodeSourcesList());
    }

    @Override
    public NSState defineNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters,
            @FormParam("nodesRecoverable") String nodesRecoverable) throws NotConnectedException {
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
    public NSState editNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters,
            @FormParam("nodesRecoverable") String nodesRecoverable) throws NotConnectedException {
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
    public NSState updateDynamicParameters(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters)
            throws NotConnectedException, PermissionRestException {

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
    public NSState createNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters)
            throws NotConnectedException, PermissionRestException {
        return orThrowRPE(createNodeSource(sessionId,
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
    public NSState createNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters,
            @FormParam("nodesRecoverable") String nodesRecoverable) throws NotConnectedException {
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
    public NSState deployNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName) throws NotConnectedException, PermissionRestException {
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
    public NSState undeployNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName, @FormParam("preempt") boolean preempt)
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
    public int getNodeSourcePingFrequency(@HeaderParam("sessionid") String sessionId,
            @FormParam("sourcename") String sourceName) throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(rm.getNodeSourcePingFrequency(sourceName).getIntValue());
    }

    @Override
    public boolean releaseNode(@HeaderParam("sessionid") String sessionId, @FormParam("url") String url)
            throws NodeException, NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        Node n;
        n = NodeFactory.getNode(url);
        return orThrowRPE(rm.releaseNode(n).getBooleanValue());
    }

    @Override
    public boolean removeNode(@HeaderParam("sessionid") String sessionId, @FormParam("url") String nodeUrl,
            @FormParam("preempt") boolean preempt) throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(rm.removeNode(nodeUrl, preempt).getBooleanValue());
    }

    @Override
    public boolean removeNodeSource(@HeaderParam("sessionid") String sessionId, @FormParam("name") String sourceName,
            @FormParam("preempt") boolean preempt) throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(rm.removeNodeSource(sourceName, preempt).getBooleanValue());
    }

    @Override
    public boolean lockNodes(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurls") Set<String> nodeUrls)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(rm.lockNodes(nodeUrls).getBooleanValue());
    }

    @Override
    public boolean unlockNodes(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurls") Set<String> nodeUrls)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(rm.unlockNodes(nodeUrls).getBooleanValue());
    }

    @Override
    public Object getNodeMBeanInfo(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodejmxurl") String nodeJmxUrl, @QueryParam("objectname") String objectName,
            @QueryParam("attrs") List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return orThrowRPE(rmProxy.getNodeMBeanInfo(nodeJmxUrl, objectName, attrs));
    }

    @Override
    public String getNodeMBeanHistory(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodejmxurl") String nodeJmxUrl, @QueryParam("objectname") String objectName,
            @QueryParam("attrs") List<String> attrs, @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return rmProxy.getNodeMBeanHistory(nodeJmxUrl, objectName, attrs, range);
    }

    @Override
    public Map<String, Map<String, Object>> getNodesMBeanHistory(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodejmxurl") Set<String> nodesJmxUrl, @QueryParam("objectname") String objectName,
            @QueryParam("attrs") List<String> attrs, @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return nodesJmxUrl.stream().collect(Collectors.toMap(nodeJmxUrl -> nodeJmxUrl, nodeJmxUrl -> {
            try {
                return processHistoryResult(rmProxy.getNodeMBeanHistory(nodeJmxUrl, objectName, attrs, range), range);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Override
    public Object getNodeMBeansInfo(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodejmxurl") String nodeJmxUrl, @QueryParam("objectname") String objectNames,
            @QueryParam("attrs") List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return orThrowRPE(rmProxy.getNodeMBeansInfo(nodeJmxUrl, objectNames, attrs));
    }

    @Override
    public Object getNodeMBeansHistory(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodejmxurl") String nodeJmxUrl, @QueryParam("objectname") String objectNames,
            @QueryParam("attrs") List<String> attrs, @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException,
            PermissionRestException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return orThrowRPE(rmProxy.getNodeMBeansHistory(nodeJmxUrl, objectNames, attrs, range));
    }

    @Override
    public boolean shutdown(@HeaderParam("sessionid") String sessionId,
            @QueryParam("preempt") @DefaultValue("false") boolean preempt)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(rm.shutdown(preempt)).getBooleanValue();
    }

    @Override
    public Topology getTopology(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(PAFuture.getFutureValue(rm.getTopology()));
    }

    @Override
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures(
            @HeaderParam("sessionid") String sessionId) throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(rm.getSupportedNodeSourceInfrastructures());
    }

    @Override
    public Map<String, List<String>> getInfrasToPoliciesMapping(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);

        Collection<PluginDescriptor> supportedInfrastructures = orThrowRPE(rm.getSupportedNodeSourceInfrastructures());

        Collection<PluginDescriptor> supportedPolicies = orThrowRPE(rm.getSupportedNodeSourcePolicies());

        Map<String, List<String>> result = orThrowRPE(rm.getInfrasToPoliciesMapping()).entrySet()
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
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(rm.getSupportedNodeSourcePolicies());
    }

    @Override
    public NodeSourceConfiguration getNodeSourceConfiguration(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodeSourceName") String nodeSourceName) throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRPE(rm.getNodeSourceConfiguration(nodeSourceName));
    }

    @Override
    public Object getMBeanInfo(@HeaderParam("sessionid") String sessionId, @PathParam("name") ObjectName name,
            @QueryParam("attr") List<String> attrs) throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, IOException, NotConnectedException, PermissionRestException {
        RMProxyUserInterface rm = checkAccess(sessionId);

        if ((attrs == null) || (attrs.size() == 0)) {
            // no attribute is requested, we return
            // the description of the mbean
            return orThrowRPE(rm.getMBeanInfo(name));

        } else {
            return orThrowRPE(rm.getMBeanAttributes(name, attrs.toArray(new String[attrs.size()])));
        }
    }

    @Override
    public void setMBeanInfo(@HeaderParam("sessionid") String sessionId, @PathParam("name") ObjectName name,
            @QueryParam("type") String type, @QueryParam("attr") String attr, @QueryParam("value") String value)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MBeanException, AttributeNotFoundException, InvalidAttributeValueException,
            IllegalArgumentException {
        RMProxyUserInterface rm = checkAccess(sessionId);
        if ((type != null) && (attr != null) && (value != null)) {
            rm.setMBeanAttribute(name, type, attr, value);
        }
    }

    @Override
    public String getStatHistory(@HeaderParam("sessionid") String sessionId, @QueryParam("range") String range1)
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
    public ScriptResult<Object> executeNodeScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeurl") String nodeUrl, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable {

        RMProxyUserInterface rm = checkAccess(sessionId);

        List<ScriptResult<Object>> results = orThrowRPE(rm.executeScript(script,
                                                                         scriptEngine,
                                                                         TargetType.NODE_URL.name(),
                                                                         Collections.singleton(nodeUrl)));
        checkEmptyScriptResults(results);

        return results.get(0);
    }

    @Override
    public List<ScriptResult<Object>> executeNodeSourceScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodesource") String nodeSource, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable {
        RMProxyUserInterface rm = checkAccess(sessionId);

        List<ScriptResult<Object>> results = orThrowRPE(rm.executeScript(script,
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
    public ScriptResult<Object> executeHostScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("host") String hostname, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable {

        RMProxyUserInterface rm = checkAccess(sessionId);

        List<ScriptResult<Object>> results = orThrowRPE(rm.executeScript(script,
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
    public String getRMThreadDump(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        RMProxyUserInterface rm = checkAccess(sessionId);
        String threadDump;
        try {
            threadDump = orThrowRPE(rm.getRMThreadDump().getStringValue());
        } catch (Exception exception) {
            return exception.getMessage();
        }
        return threadDump;
    }

    @Override
    public String getNodeThreadDump(@HeaderParam("sessionid") String sessionId, @QueryParam("nodeurl") String nodeUrl)
            throws NotConnectedException {
        RMProxyUserInterface rm = checkAccess(sessionId);
        String threadDump;
        try {
            threadDump = orThrowRPE(rm.getNodeThreadDump(nodeUrl).getStringValue());
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
        long currentTime = new Date().getTime() / 1000;
        long duration = StatHistory.Range.valueOf(range).getDuration();
        int size = Optional.ofNullable(((ArrayList) jsonMap.entrySet().iterator().next().getValue()).size()).orElse(0);
        long step = duration / size;

        List<String> labels = new ArrayList<>(size + 1);
        for (int i = 0; i <= size; i++) {
            //The time instant where the metric is taken
            long t = currentTime - duration + step * i;
            Date date = new Date(t * 1000);

            labels.add(DATE_FORMAT.format(date));
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

    private <T> T orThrowRPE(T future) throws PermissionRestException {
        try {
            PAFuture.getFutureValue(future);
            return future;
        } catch (SecurityException e) {
            throw new PermissionRestException(e);
        }
    }

}
