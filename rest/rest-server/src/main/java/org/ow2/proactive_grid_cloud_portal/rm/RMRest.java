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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
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
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.common.SessionStore;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import org.ow2.proactive_grid_cloud_portal.common.StatHistoryCaching;
import org.ow2.proactive_grid_cloud_portal.common.StatHistoryCaching.StatHistoryCacheEntry;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;


@Path("/rm")
public class RMRest implements RMRestInterface {

    protected static final String[] dataSources = { //
                                                    // "AverageInactivity", // redundant with AverageActivity
                                                    // "ToBeReleasedNodesCo", // useless info
                                                    "BusyNodesCount", //
                                                    "FreeNodesCount", //
                                                    "DownNodesCount", //
                                                    "AvailableNodesCount", //
                                                    "AverageActivity", //
            // "MaxFreeNodes" // redundant with AvailableNodesCount
    };

    private SessionStore sessionStore = SharedSessionStore.getInstance();

    private static final Pattern PATTERN = Pattern.compile("^[^:]*:(.*)");

    private RMProxyUserInterface checkAccess(String sessionId) throws NotConnectedException {
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
    @POST
    @Path("login")
    @Produces("application/json")
    public String rmConnect(@FormParam("username") String username, @FormParam("password") String password)
            throws KeyException, LoginException, RMException, ActiveObjectCreationException, NodeException {

        Session session = sessionStore.create(username);
        session.connectToRM(new CredData(CredData.parseLogin(username), CredData.parseDomain(username), password));
        return session.getSessionId();

    }

    @Override
    @POST
    @Path("disconnect")
    @Produces("application/json")
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
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    @Produces("application/json")
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
    @GET
    @Path("logins/sessionid/{sessionId}/userdata")
    @Produces("application/json")
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
    @GET
    @Path("state")
    @Produces("application/json")
    public RMState getState(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getState());
    }

    @Override
    @GET
    @GZIP
    @Path("monitoring")
    @Produces("application/json")
    public RMStateDelta getRMStateDelta(@HeaderParam("sessionid") String sessionId,
            @HeaderParam("clientCounter") @DefaultValue("-1") String clientCounter) throws NotConnectedException {
        checkAccess(sessionId);
        long counter = Integer.valueOf(clientCounter);
        return RMStateCaching.getRMStateDelta(counter);
    }

    @Override
    @GET
    @GZIP
    @Path("monitoring/full")
    @Produces("application/json")
    public RMStateFull getRMStateFull(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        checkAccess(sessionId);
        return RMStateCaching.getRMStateFull();
    }

    @Override
    @GET
    @Path("isactive")
    @Produces("application/json")
    public boolean isActive(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.isActive().getBooleanValue();
    }

    @Override
    @POST
    @Path("node")
    @Produces("application/json")
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
    @GET
    @Path("node/isavailable")
    @Produces("application/json")
    public boolean nodeIsAvailable(@HeaderParam("sessionid") String sessionId, @QueryParam("nodeurl") String url)
            throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.nodeIsAvailable(url).getBooleanValue();
    }

    @Override
    @GET
    @GZIP
    @Path("nodesource")
    @Produces("application/json")
    public List<RMNodeSourceEvent> getExistingNodeSources(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getExistingNodeSourcesList();
    }

    @Override
    @POST
    @Path("nodesource")
    @Produces("application/json")
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
    @PUT
    @Path("nodesource/edit")
    @Produces("application/json")
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
    @PUT
    @Path("nodesource/parameter")
    @Produces("application/json")
    public NSState updateDynamicParameters(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters) throws NotConnectedException {

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
    @POST
    @Path("nodesource/create")
    @Produces("application/json")
    public NSState createNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters) throws NotConnectedException {
        return createNodeSource(sessionId,
                                nodeSourceName,
                                infrastructureType,
                                infrastructureParameters,
                                infrastructureFileParameters,
                                policyType,
                                policyParameters,
                                policyFileParameters,
                                Boolean.TRUE.toString());
    }

    @Deprecated
    @Override
    @POST
    @Path("nodesource/create/recovery")
    @Produces("application/json")
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
    @PUT
    @Path("nodesource/deploy")
    @Produces("application/json")
    public NSState deployNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName) throws NotConnectedException {
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
    @PUT
    @Path("nodesource/undeploy")
    @Produces("application/json")
    public NSState undeployNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName, @FormParam("preempt") boolean preempt)
            throws NotConnectedException {
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
    @POST
    @Path("nodesource/pingfrequency")
    @Produces("application/json")
    public int getNodeSourcePingFrequency(@HeaderParam("sessionid") String sessionId,
            @FormParam("sourcename") String sourceName) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getNodeSourcePingFrequency(sourceName).getIntValue();
    }

    @Override
    @POST
    @Path("node/release")
    @Produces("application/json")
    public boolean releaseNode(@HeaderParam("sessionid") String sessionId, @FormParam("url") String url)
            throws NodeException, NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        Node n;
        n = NodeFactory.getNode(url);
        return rm.releaseNode(n).getBooleanValue();
    }

    @Override
    @POST
    @Path("node/remove")
    @Produces("application/json")
    public boolean removeNode(@HeaderParam("sessionid") String sessionId, @FormParam("url") String nodeUrl,
            @FormParam("preempt") boolean preempt) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.removeNode(nodeUrl, preempt).getBooleanValue();
    }

    @Override
    @POST
    @Path("nodesource/remove")
    @Produces("application/json")
    public boolean removeNodeSource(@HeaderParam("sessionid") String sessionId, @FormParam("name") String sourceName,
            @FormParam("preempt") boolean preempt) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.removeNodeSource(sourceName, preempt).getBooleanValue();
    }

    @Override
    @POST
    @Path("node/lock")
    @Produces("application/json")
    public boolean lockNodes(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurls") Set<String> nodeUrls)
            throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.lockNodes(nodeUrls).getBooleanValue();
    }

    @Override
    @POST
    @Path("node/unlock")
    @Produces("application/json")
    public boolean unlockNodes(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurls") Set<String> nodeUrls)
            throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.unlockNodes(nodeUrls).getBooleanValue();
    }

    @Override
    @GET
    @GZIP
    @Produces("application/json")
    @Path("node/mbean")
    public Object getNodeMBeanInfo(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodejmxurl") String nodeJmxUrl, @QueryParam("objectname") String objectName,
            @QueryParam("attrs") List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return rmProxy.getNodeMBeanInfo(nodeJmxUrl, objectName, attrs);
    }

    @Override
    @GET
    @GZIP
    @Produces("application/json")
    @Path("node/mbean/history")
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
    @GET
    @GZIP
    @Produces("application/json")
    @Path("node/mbeans")
    public Object getNodeMBeansInfo(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodejmxurl") String nodeJmxUrl, @QueryParam("objectname") String objectNames,
            @QueryParam("attrs") List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return rmProxy.getNodeMBeansInfo(nodeJmxUrl, objectNames, attrs);
    }

    @Override
    @GET
    @GZIP
    @Produces("application/json")
    @Path("node/mbeans/history")
    public Object getNodeMBeansHistory(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodejmxurl") String nodeJmxUrl, @QueryParam("objectname") String objectNames,
            @QueryParam("attrs") List<String> attrs, @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException {

        // checking that still connected to the RM
        RMProxyUserInterface rmProxy = checkAccess(sessionId);
        return rmProxy.getNodeMBeansHistory(nodeJmxUrl, objectNames, attrs, range);
    }

    @Override
    @GET
    @Path("shutdown")
    @Produces("application/json")
    public boolean shutdown(@HeaderParam("sessionid") String sessionId,
            @QueryParam("preempt") @DefaultValue("false") boolean preempt) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.shutdown(preempt).getBooleanValue();
    }

    @Override
    @GET
    @Path("topology")
    @Produces("application/json")
    public Topology getTopology(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getTopology());
    }

    @Override
    @GET
    @GZIP
    @Path("infrastructures")
    @Produces("application/json")
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures(
            @HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getSupportedNodeSourceInfrastructures();
    }

    @Override
    @GET
    @GZIP
    @Path("policies")
    @Produces("application/json")
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getSupportedNodeSourcePolicies();
    }

    @Override
    @GET
    @GZIP
    @Path("nodesource/configuration")
    @Produces("application/json")
    public NodeSourceConfiguration getNodeSourceConfiguration(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodeSourceName") String nodeSourceName) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getNodeSourceConfiguration(nodeSourceName);
    }

    @Override
    @GET
    @GZIP
    @Path("info/{name}")
    @Produces("application/json")
    public Object getMBeanInfo(@HeaderParam("sessionid") String sessionId, @PathParam("name") ObjectName name,
            @QueryParam("attr") List<String> attrs) throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, IOException, NotConnectedException {
        RMProxyUserInterface rm = checkAccess(sessionId);

        if ((attrs == null) || (attrs.size() == 0)) {
            // no attribute is requested, we return
            // the description of the mbean
            return rm.getMBeanInfo(name);

        } else {
            return rm.getMBeanAttributes(name, attrs.toArray(new String[attrs.size()]));
        }
    }

    @Override
    @POST
    @GZIP
    @Path("info/{name}")
    @Produces("application/json")
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
    @GET
    @GZIP
    @Path("stathistory")
    @Produces("application/json")
    public String getStatHistory(@HeaderParam("sessionid") String sessionId, @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            MalformedObjectNameException, NullPointerException, InterruptedException, NotConnectedException {

        RMProxyUserInterface rm = checkAccess(sessionId);

        // if range String is too large, shorten it
        // to make it recognizable by StatHistoryCaching
        if (range.length() > dataSources.length) {
            range = range.substring(0, dataSources.length);
        }
        // complete range if too short
        while (range.length() < dataSources.length) {
            range += 'a';
        }

        StatHistoryCacheEntry cache = StatHistoryCaching.getInstance().getEntry(range);
        // found unexpired cache entry matching the parameters: return it immediately
        if (cache != null) {
            return cache.getValue();
        }

        long l1 = System.currentTimeMillis();

        ObjectName on = new ObjectName(RMJMXBeans.RUNTIMEDATA_MBEAN_NAME);
        AttributeList attrs = rm.getMBeanAttributes(on, new String[] { "StatisticHistory" });
        Attribute attr = (Attribute) attrs.get(0);
        // content of the RRD4J database backing file
        byte[] rrd4j = (byte[]) attr.getValue();

        File rrd4jDb = File.createTempFile("database", "rr4dj");
        rrd4jDb.deleteOnExit();

        try (OutputStream out = new FileOutputStream(rrd4jDb)) {
            out.write(rrd4j);
        }

        // create RRD4J DB, should be identical to the one held by the RM
        RrdDb db = new RrdDb(rrd4jDb.getAbsolutePath(), true);

        long timeEnd = db.getLastUpdateTime();
        // force float separator for JSON parsing
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        // formatting will greatly reduce response size
        DecimalFormat formatter = new DecimalFormat("###.###", otherSymbols);

        // construct the JSON response directly in a String
        StringBuilder result = new StringBuilder();
        result.append("{");

        for (int i = 0; i < dataSources.length; i++) {
            String dataSource = dataSources[i];
            char zone = range.charAt(i);
            long timeStart;

            switch (zone) {
                default:
                case 'a': // 1 minute
                    timeStart = timeEnd - 60;
                    break;
                case 'm': // 10 minute
                    timeStart = timeEnd - 60 * 10;
                    break;
                case 'h': // 1 hours
                    timeStart = timeEnd - 60 * 60;
                    break;
                case 'H': // 8 hours
                    timeStart = timeEnd - 60 * 60 * 8;
                    break;
                case 'd': // 1 day
                    timeStart = timeEnd - 60 * 60 * 24;
                    break;
                case 'w': // 1 week
                    timeStart = timeEnd - 60 * 60 * 24 * 7;
                    break;
                case 'M': // 1 month
                    timeStart = timeEnd - 60 * 60 * 24 * 28;
                    break;
                case 'y': // 1 year
                    timeStart = timeEnd - 60 * 60 * 24 * 365;
                    break;
            }

            FetchRequest req = db.createFetchRequest(ConsolFun.AVERAGE, timeStart, timeEnd);
            req.setFilter(dataSource);
            FetchData fetchData = req.fetchData();
            result.append("\"").append(dataSource).append("\":[");

            double[] values = fetchData.getValues(dataSource);
            for (int j = 0; j < values.length; j++) {
                if (Double.compare(Double.NaN, values[j]) == 0) {
                    result.append("null");
                } else {
                    result.append(formatter.format(values[j]));
                }
                if (j < values.length - 1)
                    result.append(',');
            }
            result.append(']');
            if (i < dataSources.length - 1)
                result.append(',');
        }
        result.append("}");

        db.close();
        rrd4jDb.delete();

        String ret = result.toString();

        StatHistoryCaching.getInstance().addEntry(range, l1, ret);

        return ret;
    }

    @Override
    @GET
    @Path("version")
    public String getVersion() {
        return "{ " + "\"rm\" : \"" + RMRest.class.getPackage().getSpecificationVersion() + "\", " + "\"rest\" : \"" +
               RMRest.class.getPackage().getImplementationVersion() + "\"" + "}";
    }

    @Override
    @POST
    @GZIP
    @Path("node/script")
    @Produces("application/json")
    public ScriptResult<Object> executeNodeScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeurl") String nodeUrl, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable {
        RMProxyUserInterface rm = checkAccess(sessionId);
        return executeScriptAndGetResult(nodeUrl, script, scriptEngine, rm, TargetType.NODE_URL.name());
    }

    @Override
    @POST
    @GZIP
    @Path("nodesource/script")
    @Produces("application/json")
    public ScriptResult<Object> executeNodeSourceScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodesource") String nodeSource, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable {
        RMProxyUserInterface rm = checkAccess(sessionId);
        return executeScriptAndGetResult(nodeSource, script, scriptEngine, rm, TargetType.NODESOURCE_NAME.name());
    }

    @Override
    @POST
    @GZIP
    @Path("host/script")
    @Produces("application/json")
    public ScriptResult<Object> executeHostScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("host") String host, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable {
        RMProxyUserInterface rm = checkAccess(sessionId);
        return executeScriptAndGetResult(host, script, scriptEngine, rm, TargetType.HOSTNAME.name());
    }

    private ScriptResult<Object> executeScriptAndGetResult(String targetId, String script, String scriptEngine,
            RMProxyUserInterface rm, String targetType) {
        List<ScriptResult<Object>> results = rm.executeScript(script,
                                                              scriptEngine,
                                                              targetType,
                                                              Collections.singleton(targetId));
        checkEmptyScriptResults(results);
        return results.get(0);
    }

    private void checkEmptyScriptResults(List<ScriptResult<Object>> results) {
        if (results.isEmpty()) {
            results.add(new ScriptResult<>(new ScriptException("Empty results from script execution")));
        }
    }

    @GET
    @Path("/")
    public Response index() throws URISyntaxException {
        return Response.seeOther(new URI("doc/jaxrsdocs/rm/index.html")).build();
    }

    @Override
    @GET
    @GZIP
    @Path("threaddump")
    @Produces("application/json")
    public String getRMThreadDump(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        RMProxyUserInterface rm = checkAccess(sessionId);
        String threadDump;
        try {
            threadDump = rm.getRMThreadDump().getStringValue();
        } catch (Exception exception) {
            return exception.getMessage();
        }
        return threadDump;
    }

    @Override
    @GET
    @GZIP
    @Path("node/threaddump")
    @Produces("application/json")
    public String getNodeThreadDump(@HeaderParam("sessionid") String sessionId, @QueryParam("nodeurl") String nodeUrl)
            throws NotConnectedException {
        RMProxyUserInterface rm = checkAccess(sessionId);
        String threadDump;
        try {
            threadDump = rm.getNodeThreadDump(nodeUrl).getStringValue();
        } catch (Exception exception) {
            return exception.getMessage();
        }
        return threadDump;
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
            if (field.getMeta().credential() || field.getMeta().password() || field.getMeta().fileBrowser()) {
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

}
