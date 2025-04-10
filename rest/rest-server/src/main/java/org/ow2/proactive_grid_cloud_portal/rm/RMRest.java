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
import static org.ow2.proactive_grid_cloud_portal.common.StatHistoryCaching.NO_TENANT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.regex.MatchResult;
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
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.core.properties.PASharedProperties;
import org.ow2.proactive.resourcemanager.common.NSState;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.RMStatistics;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeHistory;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateDelta;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateFull;
import org.ow2.proactive.resourcemanager.common.util.RMListenerProxy;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXBeans;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
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
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;
import org.ow2.proactive_grid_cloud_portal.webapp.StatHistory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;


public class RMRest implements RMRestInterface {

    protected static final String[] dataSources = { "AvailableNodesCount", "FreeNodesCount", "NeededNodesCount",
                                                    "BusyNodesCount", "DeployingNodesCount", "ConfigNodesCount", // it is Config and not Configuring because RRDDataStore limits name 20 characters
                                                    "DownNodesCount", "LostNodesCount", "AverageActivity" };

    public static final Logger LOGGER = Logger.getLogger(RMRest.class);

    private SessionStore sessionStore = SharedSessionStore.getInstance();

    private static final Pattern PATTERN = Pattern.compile("^[^:]*:(.*)");

    protected static final String NODE_CONFIG_TAGS_KEY = "nodeTags";

    private static final String NODE_SOURCE_DEPLOYED_STATUS = "deployed";

    private static final int REQUESTS_INTERVAL_SECONDS = 10; // how long to wait before sending the next request

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
    public String getUrl() {
        return PortalConfiguration.RM_URL.getValueAsString();
    }

    @Override
    public List<String> getDomains() {
        if (PASharedProperties.ALLOWED_DOMAINS.isSet()) {
            return PASharedProperties.ALLOWED_DOMAINS.getValueAsList(",", true);
        }

        // windows current machine domain
        String currentMachineDomain = System.getenv("USERDOMAIN");
        if (currentMachineDomain != null) {
            return ImmutableList.of("", currentMachineDomain.toLowerCase());
        } else {
            return Collections.emptyList();
        }
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
        PAFuture.getFutureValue(rm.disconnect(), RMListenerProxy.MONITORING_INTERFACE_TIMEOUT);
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
    public RMStatistics getStatistics(String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        UserData userData = rm.getCurrentUserData();
        RMStatistics statistics = new RMStatistics();

        List<RMNodeEvent> nodeEvents = rm.getMonitoring().getState().getNodeEvents();

        if (PAResourceManagerProperties.RM_FILTER_BY_TENANT.getValueAsBoolean() && !userData.isAllTenantPermission()) {
            nodeEvents = nodeEvents.stream()
                                   .filter(e -> e.getTenant() == null || e.getTenant().equals(userData.getTenant()))
                                   .collect(Collectors.toList());
        }

        for (RMNodeEvent node : nodeEvents) {
            switch (node.getNodeState()) {
                case FREE:
                    statistics.addFreeNode();
                    break;
                case BUSY:
                    statistics.addBusyNode();
                    break;
                case DEPLOYING:
                    statistics.addDeployingNode();
                    break;
                case CONFIGURING:
                    statistics.addConfigNode();
                    break;
                case DOWN:
                    statistics.addDownNode();
                    break;
                case LOST:
                    statistics.addLostNode();
                    break;
                case TO_BE_REMOVED:
                    statistics.addToBeRemovedNodesCount();
                    break;
            }
        }

        statistics.setNeededNodesCount(rm.getMonitoring().getNeededNodes());

        return statistics;
    }

    @Override
    public RMState getState(String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getState());
    }

    @Override
    public RMStateDelta getRMStateDelta(String sessionId, String clientCounter)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        long counter = Integer.valueOf(clientCounter);
        return orThrowRpe(filterByTenant(rm.getCurrentUserData(), RMStateCaching.getRMStateDelta(counter)));
    }

    @Override
    public RMStateFull getRMStateFull(String sessionId) throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);

        return orThrowRpe(filterByTenant(rm.getCurrentUserData(), RMStateCaching.getRMStateFull()));
    }

    private RMStateFull filterByTenant(UserData user, RMStateFull state) {
        if (PAResourceManagerProperties.RM_FILTER_BY_TENANT.getValueAsBoolean() && !user.isAllTenantPermission()) {
            state.setNodeSource(state.getNodeSource()
                                     .stream()
                                     .filter(nse -> nse.getTenant() == null || nse.getTenant().equals(user.getTenant()))
                                     .collect(Collectors.toList()));
            state.setNodesEvents(state.getNodesEvents()
                                      .stream()
                                      .filter(ne -> ne.getTenant() == null || ne.getTenant().equals(user.getTenant()))
                                      .collect(Collectors.toList()));
            return state;
        } else {
            return state;
        }
    }

    private RMStateDelta filterByTenant(UserData user, RMStateDelta state) {
        if (PAResourceManagerProperties.RM_FILTER_BY_TENANT.getValueAsBoolean() && !user.isAllTenantPermission()) {
            state.setNodeSource(state.getNodeSource()
                                     .stream()
                                     .filter(nse -> nse.getTenant() == null || nse.getTenant().equals(user.getTenant()))
                                     .collect(Collectors.toList()));
            state.setNodesEvents(state.getNodesEvents()
                                      .stream()
                                      .filter(ne -> ne.getTenant() == null || ne.getTenant().equals(user.getTenant()))
                                      .collect(Collectors.toList()));
            return state;
        } else {
            return state;
        }
    }

    @Override
    public String getModelHosts(String name, String noEmpty) throws PermissionRestException {
        RMStateFull state = orThrowRpe(RMStateCaching.getRMStateFull());
        List<String> filteredHosts = state.getNodesEvents()
                                          .stream()
                                          .map(RMNodeEvent::getHostName)
                                          .distinct()
                                          .filter(hostName -> !hostName.isEmpty() &&
                                                              (name != null ? hostName.matches(name) : true))
                                          .collect(Collectors.toList());
        if (filteredHosts.isEmpty()) {
            return "PA:LIST()";
        } else {
            if ("true".equalsIgnoreCase(noEmpty)) {
                return String.format("PA:LIST(%s)", filteredHosts.stream().distinct().collect(Collectors.joining(",")));
            } else {
                return String.format("PA:LIST(,%s)",
                                     filteredHosts.stream().distinct().collect(Collectors.joining(",")));
            }
        }
    }

    @Override
    public String getModelNodeSources(String name, String infrastructure, String policy, String noDefault,
            String noEmpty) throws PermissionRestException {
        List<String> filteredNodeSources = getFilteredNodeSources(name, infrastructure, policy, noDefault);
        if (filteredNodeSources.isEmpty()) {
            return "PA:LIST()";
        } else {
            if ("true".equalsIgnoreCase(noEmpty)) {
                return String.format("PA:LIST(%s)",
                                     filteredNodeSources.stream().distinct().collect(Collectors.joining(",")));
            } else {
                return String.format("PA:LIST(,%s)",
                                     filteredNodeSources.stream().distinct().collect(Collectors.joining(",")));
            }
        }
    }

    private List<String> getFilteredNodeSources(String name, String infrastructure, String policy, String noDefault)
            throws PermissionRestException {
        Map<String, Pair<String, String>> nodeSources = new LinkedHashMap<>();
        RMStateFull state = orThrowRpe(RMStateCaching.getRMStateFull());
        nodeSources.put(RMConstants.DEFAULT_STATIC_SOURCE_NAME,
                        Pair.of("DefaultInfrastructureManager", "StaticPolicy"));
        state.getNodeSource()
             .stream()
             .filter(event -> !event.getNodeSourceName().isEmpty() &&
                              "DEPLOYED".equalsIgnoreCase(event.getNodeSourceStatus()))
             .forEach(event -> {
                 nodeSources.put(event.getNodeSourceName(),
                                 Pair.of(event.getInfrastructureType()
                                              .substring(event.getInfrastructureType().lastIndexOf(".") + 1),
                                         event.getPolicyType().substring(event.getPolicyType().lastIndexOf(".") + 1)));
             });
        if (infrastructure != null) {
            for (Iterator<Map.Entry<String, Pair<String, String>>> it = nodeSources.entrySet()
                                                                                   .iterator(); it.hasNext();) {
                Map.Entry<String, Pair<String, String>> entry = it.next();
                if (!entry.getValue().getLeft().matches(infrastructure)) {
                    it.remove();
                }

            }
        }
        if (policy != null) {
            for (Iterator<Map.Entry<String, Pair<String, String>>> it = nodeSources.entrySet()
                                                                                   .iterator(); it.hasNext();) {
                Map.Entry<String, Pair<String, String>> entry = it.next();
                if (!entry.getValue().getRight().matches(policy)) {
                    it.remove();
                }

            }
        }
        if (name != null) {
            for (Iterator<Map.Entry<String, Pair<String, String>>> it = nodeSources.entrySet()
                                                                                   .iterator(); it.hasNext();) {
                Map.Entry<String, Pair<String, String>> entry = it.next();
                if (!entry.getKey().matches(name)) {
                    it.remove();
                }
            }
        }
        if ("true".equalsIgnoreCase(noDefault)) {
            for (Iterator<Map.Entry<String, Pair<String, String>>> it = nodeSources.entrySet()
                                                                                   .iterator(); it.hasNext();) {
                Map.Entry<String, Pair<String, String>> entry = it.next();
                if (entry.getKey().equals(RMConstants.DEFAULT_STATIC_SOURCE_NAME)) {
                    it.remove();
                }
            }
        }

        return new ArrayList<>(nodeSources.keySet());
    }

    @Override
    public String getModelTokens(String name, String nodeSource, String noEmpty) throws PermissionRestException {
        RMStateFull state = orThrowRpe(RMStateCaching.getRMStateFull());
        Set<String> tokens = new LinkedHashSet<>();
        final MutableBoolean allowEmptyToken = new MutableBoolean(name == null);
        state.getNodeSource().forEach(event -> {
            if (event.getAccessTokens() != null && !event.getAccessTokens().isEmpty()) {
                if (!Strings.isNullOrEmpty(nodeSource)) {
                    if (!event.getNodeSourceName().equals(nodeSource)) {
                        return;
                    } else {
                        // if the nodeSource parameter is used and this nodeSource is globally protected by a token,
                        // then the model should not return an empty token choice
                        allowEmptyToken.setValue(event.getAccessTokens().isEmpty());
                    }
                }
                tokens.addAll(event.getAccessTokens()
                                   .stream()
                                   .filter(token -> (name != null ? token.matches(name) : true))
                                   .collect(Collectors.toList()));
            }
        });
        state.getNodesEvents().forEach(event -> {
            if (event.getTokens() != null) {
                if (!Strings.isNullOrEmpty(nodeSource)) {
                    if (!event.getNodeSource().equals(nodeSource)) {
                        return;
                    }
                }
                tokens.addAll(event.getTokens()
                                   .stream()
                                   .filter(token -> (name != null ? token.matches(name) : true))
                                   .collect(Collectors.toList()));
            }
        });
        if (tokens.isEmpty()) {
            return "PA:LIST()";
        } else {
            if ("true".equalsIgnoreCase(noEmpty)) {
                return String.format("PA:LIST(%s)", String.join(",", tokens));
            } else {
                return String.format("PA:LIST(" + (allowEmptyToken.booleanValue() ? "," : "") + "%s)",
                                     String.join(",", tokens));
            }
        }
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
    public Set<String> getNodeTags(String sessionId, String url) throws NotConnectedException, RestException {
        try {
            ResourceManager rm = checkAccess(sessionId);
            return rm.getNodeTags(url);
        } catch (RMException e) {
            throw new RestException(e);
        }
    }

    @Override
    public Set<String> getNodeTags(String sessionId) throws NotConnectedException, RestException {
        try {
            ResourceManager rm = checkAccess(sessionId);
            Set<String> allTags = new HashSet<>();
            for (String nodeUrl : rm.listNodeUrls()) {
                allTags.addAll(rm.getNodeTags(nodeUrl));
            }
            return allTags;
        } catch (RMException e) {
            throw new RestException(e);
        }
    }

    @Override
    public Set<String> searchNodes(String sessionId, List<String> tags, boolean all)
            throws NotConnectedException, RestException {
        ResourceManager rm = checkAccess(sessionId);
        if (tags == null || tags.isEmpty()) {
            return rm.listNodeUrls();
        } else {
            return rm.getNodesByTags(new HashSet<>(tags), all);
        }
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
    public NSState redeployNodeSource(String sessionId, String nodeSourceName)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        NSState nsState = new NSState();
        try {
            nsState.setResult(rm.redeployNodeSource(nodeSourceName).getBooleanValue());
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
    public Set<String> acquireNodes(String sessionId, String sourceName, int numberNodes, boolean synchronous,
            long timeout, String nodeConfigJson) throws NotConnectedException, RestException {
        if (numberNodes <= 0) {
            throw new IllegalArgumentException("Invalid number of nodes: " + numberNodes);
        }
        ResourceManager rm = checkAccess(sessionId);
        if (sourceName == null) {
            throw new IllegalArgumentException("Node source name should not be null.");
        }
        Optional<RMNodeSourceEvent> nodeSource = rm.getExistingNodeSourcesList()
                                                   .stream()
                                                   .filter(ns -> sourceName.equals(ns.getSourceName()))
                                                   .findAny();
        if (!nodeSource.isPresent()) {
            throw new IllegalArgumentException(String.format("Specified node source [%s] not exist.", sourceName));
        }
        if (!NODE_SOURCE_DEPLOYED_STATUS.equals(nodeSource.get().getNodeSourceStatus())) {
            throw new IllegalArgumentException(String.format("Specified node source [%s] is not deployed.",
                                                             sourceName));
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> nodeConfig;
        try {
            nodeConfig = mapper.readValue(nodeConfigJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Error during parsing the node configuration: " + nodeConfigJson, e);
        }
        if (synchronous) {
            String acquireRequestId = "tmp:" + UUID.randomUUID().toString();
            setRequestIdInNodeConfig(nodeConfig, acquireRequestId);
            rm.acquireNodes(sourceName, numberNodes, timeout * 1000, nodeConfig);
            waitUntil(timeout,
                      "Nodes are not deployed within the specified timeout.",
                      () -> rm.getNodesByTag(acquireRequestId).size() == numberNodes);
            return orThrowRpe(rm.getNodesByTag(acquireRequestId));
        } else {
            rm.acquireNodes(sourceName, numberNodes, timeout * 1000, nodeConfig);
            return new HashSet<>();
        }
    }

    private void setRequestIdInNodeConfig(Map<String, Object> nodeConfig, String acquireRequestId) {
        String nodeTags;
        try {
            nodeTags = (String) nodeConfig.getOrDefault(NODE_CONFIG_TAGS_KEY, "");
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("The value of node configuration [%s] can't be casted to String",
                                                             NODE_CONFIG_TAGS_KEY),
                                               e);
        }
        if (!nodeTags.isEmpty()) {
            nodeTags += "," + acquireRequestId;
        } else {
            nodeTags = acquireRequestId;
        }
        nodeConfig.put(NODE_CONFIG_TAGS_KEY, nodeTags);
    }

    private void waitUntil(long timeoutSeconds, String timeoutMessage, BooleanSupplier conditionToMatch)
            throws RestException {
        try {
            int waitedTime = 0;
            while (!conditionToMatch.getAsBoolean()) {
                TimeUnit.SECONDS.sleep(REQUESTS_INTERVAL_SECONDS);
                waitedTime += REQUESTS_INTERVAL_SECONDS;
                if (waitedTime >= timeoutSeconds) {
                    throw new RestException(timeoutMessage);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
        Map<String, Map<String, Long>> distances = mapValues(mapKeys(topology.getDistances(), String::toString),
                                                             map -> mapKeys(map, String::toString));
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
    public String getStatHistory(String sessionId, String range1, String function)
            throws ReflectionException, InterruptedException, IntrospectionException, NotConnectedException,
            InstanceNotFoundException, MalformedObjectNameException, IOException {

        String newRange = MBeanInfoViewer.possibleModifyRange(range1, dataSources, 'a');
        RMProxyUserInterface rm = checkAccess(sessionId);
        UserData userData = rm.getCurrentUserData();

        StatHistoryCaching caching;

        String beanName;

        if (PAResourceManagerProperties.RM_FILTER_BY_TENANT.isSet() &&
            PAResourceManagerProperties.RM_JMX_TENANT_NAMES.isSet()) {
            if (userData.isAllTenantPermission()) {
                caching = StatHistoryCaching.getInstance();
                beanName = RMJMXBeans.RUNTIMEDATA_MBEAN_NAME;
            } else if (userData.getTenant() == null) {
                caching = StatHistoryCaching.getTenantInstance(NO_TENANT);
                beanName = RMJMXBeans.RUNTIMEDATA_MBEAN_NAME + "_" + NO_TENANT;
            } else {
                caching = StatHistoryCaching.getTenantInstance(userData.getTenant());
                beanName = RMJMXBeans.RUNTIMEDATA_MBEAN_NAME + "_" + userData.getTenant();
            }
        } else {
            caching = StatHistoryCaching.getInstance();
            beanName = RMJMXBeans.RUNTIMEDATA_MBEAN_NAME;
        }

        StatHistoryCacheEntry entry = caching.getEntryOrCompute(newRange, () -> {

            AttributeList attrs = rm.getMBeanAttributes(new ObjectName(beanName), new String[] { "StatisticHistory" });

            Attribute attr = (Attribute) attrs.get(0);

            // content of the RRD4J database backing file
            byte[] rrd4j = (byte[]) attr.getValue();

            return MBeanInfoViewer.rrdContent(rrd4j, newRange, dataSources, function);
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

        UserData userData = rm.getCurrentUserData();

        // filter by tenant
        if (PAResourceManagerProperties.RM_FILTER_BY_TENANT.getValueAsBoolean() && !userData.isAllTenantPermission()) {
            rawDataFromRM = rawDataFromRM.stream()
                                         .filter(e -> e.getTenant() == null ||
                                                      e.getTenant().equals(userData.getTenant()))
                                         .collect(Collectors.toList());
        }

        // grouped by node source name, host name, and node name
        Map<String, Map<String, Map<String, List<RMNodeHistory>>>> grouped = rawDataFromRM.stream()
                                                                                          .collect(Collectors.groupingBy(rmNodeHistory -> rmNodeHistory.getNodeSource() == null ? ""
                                                                                                                                                                                : rmNodeHistory.getNodeSource(),
                                                                                                                         Collectors.groupingBy(rmNodeHistory -> rmNodeHistory.getHost() == null ? ""
                                                                                                                                                                                                : rmNodeHistory.getHost(),
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
            future = PAFuture.getFutureValue(future);
            return future;
        } catch (SecurityException e) {
            throw new PermissionRestException(e);
        }
    }

    @Override
    public boolean checkNodePermission(String sessionId, String nodeUrl, boolean provider)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRpe(rm.checkNodePermission(nodeUrl, provider).getBooleanValue());
    }

    @Override
    public boolean checkNodeSourcePermission(String sessionId, String nodeSourceName, boolean provider)
            throws NotConnectedException, PermissionRestException {
        ResourceManager rm = checkAccess(sessionId);
        return orThrowRpe(rm.checkNodeSourcePermission(nodeSourceName, provider).getBooleanValue());
    }

}
