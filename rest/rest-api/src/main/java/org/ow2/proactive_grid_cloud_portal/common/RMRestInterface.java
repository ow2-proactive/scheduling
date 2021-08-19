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
package org.ow2.proactive_grid_cloud_portal.common;

import java.io.IOException;
import java.security.KeyException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.ws.rs.DELETE;
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

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.resourcemanager.common.NSState;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeHistory;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateDelta;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateFull;
import org.ow2.proactive.resourcemanager.exception.RMActiveObjectCreationException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.exception.RMNodeException;
import org.ow2.proactive.resourcemanager.frontend.PluginDescriptorData;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyData;
import org.ow2.proactive.resourcemanager.nodesource.common.NodeSourceConfiguration;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.RestException;


/**
 * Resource Manager REST API
 */
@Path("/rm")
public interface RMRestInterface {

    /**
     * RM server URL.
     *
     * Returns the URL of the rm server.
     *
     * @return ProActive url of the rm server
     */
    @GET
    @Path("url")
    @Produces(MediaType.TEXT_PLAIN)
    String getUrl();

    /**
     * Login with username/password.
     *
     * Log into the resource manager using an form containing 2 fields
     * @param username user name
     * @param password password
     * @return the sessionid of the user if succeed
     */
    @POST
    @Path("login")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    String rmConnect(@FormParam("username") String username, @FormParam("password") String password)
            throws KeyException, LoginException, RMException, RMActiveObjectCreationException, RMNodeException;

    /**
     * Resource Manager disconnect.
     *
     * Disconnects from resource manager and releases all the nodes taken by
     * user for computations.
     *
     * @param sessionId current session
     */
    @POST
    @Path("disconnect")
    @Produces(MediaType.APPLICATION_JSON)
    void rmDisconnect(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    /**
     * Login to the resource manager using a multipart form.
     *
     * It can be used either by submitting:<ul>
     *     <li>2 fields: 'username' and 'password'</li>
     *     <li>a credential file with field name 'credential'</li>
     * </ul>
     *
     * @param multipart multipart form
     * @return the session id associated to this new connection
     * @throws LoginException if the authentication fails
     * @throws KeyException if the credentials file cannot be decrypted
     * @throws IOException if an I/O error occur while reading credentials
     * @throws RMException if any other error occurs
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    String loginWithCredential(@MultipartForm LoginForm multipart) throws RMActiveObjectCreationException,
            RMNodeException, KeyException, IOException, LoginException, RMException;

    /**
     * Get Login of a session.
     *
     * Returns the login string associated to the {@code sessionId} if it exists
     *
     * In case that the given sessionId doesn't have an associated login (session id expired, or invalid),
     * this endpoint will return an empty string
     *
     * @param sessionId with which the endpoint is going to look for the login value
     * @return the associated login value or an empty string
     */
    @GET
    @Path("logins/sessionid/{sessionId}")
    @Produces(MediaType.TEXT_PLAIN)
    String getLoginFromSessionId(@PathParam("sessionId") String sessionId);

    /**
     * Get User Data of a session.
     *
     * Returns a UserData object associated to the user connected with the {@code sessionId}
     *
     * In case that the given sessionId doesn't have an associated login (session id expired, or invalid),
     * this endpoint will return null
     *
     * @param sessionId with which the endpoint is going to look for the login value
     * @return a UserData object or null
     */
    @GET
    @Path("logins/sessionid/{sessionId}/userdata")
    @Produces(MediaType.APPLICATION_JSON)
    UserData getUserDataFromSessionId(@PathParam("sessionId") String sessionId);

    /**
     * Minimal state of the Resource Manager.
     *
     * Returns the state of the Resource Manager with minimal information.
     *
     * @param sessionId current session
     * @return Returns the state of the scheduler
     */
    @GET
    @Path("state")
    @Produces(MediaType.APPLICATION_JSON)
    RMState getState(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    /**
     * Delta state of Resource Manager.
     *
     * Returns difference between current full state of the resource manager
     * and full state that the client is aware of. Each event that changes state fo the RM
     * has counter assosiacted to it. Thus client has to provide 'latestCounter',
     * i.e. latest event he is aware of.
     * @param sessionId current session
     * @param clientCounter (optional) is the latest counter client has, if parameter is not provided then
     *                                 method returns all events
     * @return the difference between current state and state that client knows
     */
    @GET
    @GZIP
    @Path("monitoring")
    @Produces(MediaType.APPLICATION_JSON)
    RMStateDelta getRMStateDelta(@HeaderParam("sessionid") String sessionId,
            @HeaderParam("clientCounter") @DefaultValue("-1") String clientCounter)
            throws NotConnectedException, PermissionRestException;

    /**
     * Full state of the resource manager.
     *
     * Returns the full state of the RM, which does not include REMOVED node/nodesources
     * @param sessionId current session
     * @return the state of the RM, which does not include REMOVED node/nodesources
     */
    @GET
    @GZIP
    @Path("monitoring/full")
    @Produces(MediaType.APPLICATION_JSON)
    RMStateFull getRMStateFull(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException;

    /**
     * List of registered node hosts as variable model.
     *
     * Returns a task variable model string containing the list of registered hosts in the resource manager
     * @return a model containing the list of hosts, including an empty name. e.g. PA:LIST(,hostname1,hostname2)
     */
    @GET
    @Path("model/hosts")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    String getModelHosts() throws PermissionRestException;

    /**
     * list of node sources as variable model.
     *
     * Returns a task variable model string containing the list of registered node sources in the resource manager
     * @return a model containing the list of node sources name, including an empty name and the default node source e.g. PA:LIST(,Default,LocalNodes)
     */
    @GET
    @Path("model/nodesources")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    String getModelNodeSources() throws PermissionRestException;

    /**
     * Check Resource Manager availability.
     *
     * Returns true if the resource manager is operational.
     *
     * @param sessionId current session
     * @return true if the resource manager is operational.
     */
    @GET
    @Path("isactive")
    @Produces(MediaType.APPLICATION_JSON)
    boolean isActive(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    /**
     * Add an existing node to a particular node source.
     *
     * @param sessionId current session
     * @param url the URL of the node
     * @param nodesource the node source, can be null
     * @return true if new node is added successfully, runtime exception
     *         otherwise
     */
    @POST
    @Path("node")
    @Produces(MediaType.APPLICATION_JSON)
    boolean addNode(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurl") String url,
            @FormParam("nodesource") String nodesource) throws NotConnectedException;

    /**
     * Check node availability.
     *
     * Returns true if the node nodeUrl is registered (i.e. known by the RM) and
     * not down.
     *
     * @param sessionId current session
     * @param url the URL of the node
     * @return true if the node nodeUrl is registered and not down
     */
    @GET
    @Path("node/isavailable")
    @Produces(MediaType.APPLICATION_JSON)
    boolean nodeIsAvailable(@HeaderParam("sessionid") String sessionId, @QueryParam("nodeurl") String url)
            throws NotConnectedException;

    /**
     * List existing node sources.
     *
     * Returns the list of existing Node Sources.
     * @param sessionId current session
     * @return list of existing Node Sources
     */
    @GET
    @GZIP
    @Path("nodesource")
    @Produces(MediaType.APPLICATION_JSON)
    List<RMNodeSourceEvent> getExistingNodeSources(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException;

    /**
     * Create/define a node source
     *
     * @param sessionId current session
     * @param nodeSourceName name of the node source
     * @param infrastructureType infrastructure class name
     * @param infrastructureParameters infrastructure non-file parameters
     * @param infrastructureFileParameters infrastructure file parameters
     * @param policyType policy class name
     * @param policyParameters policy non-file parameters
     * @param policyFileParameters policy file parameters
     * @param nodesRecoverable Whether the nodes can be recovered after a crash of the RM
     * @return the new node source state
     */
    @POST
    @Path("nodesource")
    @Produces(MediaType.APPLICATION_JSON)
    NSState defineNodeSource(@HeaderParam("sessionid") String sessionId, //NOSONAR
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters,
            @FormParam("nodesRecoverable") String nodesRecoverable) throws NotConnectedException;

    /**
     * Edit parameters of an un-deployed node source.
     *
     * @param sessionId current session
     * @param nodeSourceName name of the node source to edit
     * @param infrastructureType fully qualified class name of the infrastructure to edit
     * @param infrastructureParameters string parameters of the infrastructure, without the
     *        parameters containing files or credentials
     * @param infrastructureFileParameters infrastructure file or credential parameters
     * @param policyType fully qualified class name of the policy to edit
     * @param policyParameters string parameters of the policy, without the parameters
     *        containing files or credentials
     * @param policyFileParameters policy file or credential parameters
     * @param nodesRecoverable Whether the nodes can be recovered after a crash of the RM
     * @return the new node source state
     */
    @PUT
    @Path("nodesource/edit")
    @Produces(MediaType.APPLICATION_JSON)
    NSState editNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters,
            @FormParam("nodesRecoverable") String nodesRecoverable) throws NotConnectedException;

    /**
     * Update dynamic parameters of a deployed node source.
     *
     * @param sessionId current session
     * @param nodeSourceName name of the node source to edit
     * @param infrastructureType fully qualified class name of the infrastructure to edit
     * @param infrastructureParameters string parameters of the infrastructure, without the
     *        parameters containing files or credentials
     * @param infrastructureFileParameters infrastructure file or credential parameters
     * @param policyType fully qualified class name of the policy to edit
     * @param policyParameters string parameters of the policy, without the parameters
     *        containing files or credentials
     * @param policyFileParameters policy file or credential parameters
     * @return the new node source state
     */
    @PUT
    @Path("nodesource/parameter")
    @Produces(MediaType.APPLICATION_JSON)
    NSState updateDynamicParameters(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters)
            throws NotConnectedException, PermissionRestException;

    /**
     * @deprecated  As of version 8.1, replaced by {@link #defineNodeSource(String, String, String, String[], String[],
     * String, String[], String[], String)} and {@link #deployNodeSource(String, String)}
     */
    @Deprecated
    @POST
    @Path("nodesource/create")
    @Produces(MediaType.APPLICATION_JSON)
    NSState createNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters)
            throws NotConnectedException, PermissionRestException;

    /**
     * @deprecated  As of version 8.1, replaced by {@link #defineNodeSource(String, String,String, String[], String[],
     * String, String[], String[], String)} and {@link #deployNodeSource(String, String)}
     *
     * Create a NodeSource
     * <p>
     *
     * @param sessionId current session
     * @param nodeSourceName name of the node source to create
     * @param infrastructureType  fully qualified class name of the infrastructure to create
     * @param infrastructureParameters String parameters of the infrastructure, without the
     *            parameters containing files or credentials
     * @param infrastructureFileParameters File or credential parameters
     * @param policyType fully qualified class name of the policy to create
     * @param policyParameters String parameters of the policy, without the parameters
     *            containing files or credentials
     * @param policyFileParameters File or credential parameters
     * @param nodesRecoverable Whether the nodes can be recovered after a crash of the RM
     * @return the new node source state
     */
    @Deprecated
    @POST
    @Path("nodesource/create/recovery")
    @Produces(MediaType.APPLICATION_JSON)
    NSState createNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters,
            @FormParam("nodesRecoverable") String nodesRecoverable) throws NotConnectedException;

    /**
     * Deploy a node source.
     *
     * Start the nodes acquisition of an existing node source.
     *
     * @param sessionId current session
     * @param nodeSourceName the name of the node source to start
     * @return the result of the action, possibly containing the error message
     */
    @PUT
    @Path("nodesource/deploy")
    @Produces(MediaType.APPLICATION_JSON)
    NSState deployNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName) throws NotConnectedException, PermissionRestException;

    /**
     * Un-deploy a node source.
     *
     * Remove the nodes of the node source and keep the node source un-deployed.
     *
     * @param sessionId current session
     * @param nodeSourceName the name of the node source to undeploy
     * @return the result of the action, possibly containing the error message
     */
    @PUT
    @Path("nodesource/undeploy")
    @Produces(MediaType.APPLICATION_JSON)
    NSState undeployNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName, @FormParam("preempt") boolean preempt)
            throws NotConnectedException, PermissionRestException;

    /**
     * Ping frequency of a node source.
     *
     * Returns the ping frequency of a node source.
     *
     * @param sessionId current session
     * @param sourceName a node source
     * @return the ping frequency
     */
    @POST
    @Path("nodesource/pingfrequency")
    @Produces(MediaType.APPLICATION_JSON)
    int getNodeSourcePingFrequency(@HeaderParam("sessionid") String sessionId,
            @FormParam("sourcename") String sourceName) throws NotConnectedException, PermissionRestException;

    /**
     * Acquire new nodes of a specified node source
     * @param sessionId current session
     * @param sourceName the name of the node source
     * @param numberNodes the number of nodes to be acquired
     * @param synchronous whether the request is synchronous or asynchronous, when true, the request returns the url of acquired nodes after the nodes are successfully deployed.
     * @param timeout the maximum duration of the request, in seconds.
     * @param nodeConfigJson the specific configuration of nodes to be acquired, in json format
     * @return When synchronous is true, returns the url of acquired nodes after the nodes are successfully deployed.
     *         When synchronous is false, returns immediately with an empty list.
     */
    @POST
    @Path("nodesource/{sourcename}/nodes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Set<String> acquireNodes(@HeaderParam("sessionid") String sessionId, @PathParam("sourcename") String sourceName,
            @QueryParam("numbernodes") int numberNodes, @QueryParam("sync") @DefaultValue("false") boolean synchronous,
            @QueryParam("timeout") @DefaultValue("600") long timeout, final String nodeConfigJson)
            throws NotConnectedException, RestException;

    /**
     * Release a node.
     *
     * @param sessionId current session
     * @param url node's URL
     * @return true of the node has been released
     */
    @POST
    @Path("node/release")
    @Produces(MediaType.APPLICATION_JSON)
    boolean releaseNode(@HeaderParam("sessionid") String sessionId, @FormParam("url") String url)
            throws RMNodeException, NotConnectedException, PermissionRestException;

    /**
     * Delete a node.
     *
     * @param sessionId current session
     * @param nodeUrl node's URL
     * @param preempt if true remove node source immediatly whithout waiting for nodes to be freed
     * @return true if the node is removed successfully, false or exception otherwise
     */
    @POST
    @Path("node/remove")
    @Produces(MediaType.APPLICATION_JSON)
    boolean removeNode(@HeaderParam("sessionid") String sessionId, @FormParam("url") String nodeUrl,
            @FormParam("preempt") boolean preempt) throws NotConnectedException, PermissionRestException;

    /**
     * Delete a node source.
     *
     * @param sessionId current session
     * @param sourceName a node source name
     * @param preempt if true remove node source immediatly whithout waiting for nodes to be freed
     * @return true if the node is removed successfully, false or exception otherwise
     */
    @POST
    @Path("nodesource/remove")
    @Produces(MediaType.APPLICATION_JSON)
    boolean removeNodeSource(@HeaderParam("sessionid") String sessionId, @FormParam("name") String sourceName,
            @FormParam("preempt") boolean preempt) throws NotConnectedException, PermissionRestException;

    /**
     * Prevent other users from using a set of locked nodes.
     *
     * @param sessionId
     *            current session
     * @param nodeUrls
     *            set of node urls to lock
     * @return true when all nodes were free and have been locked
     */
    @POST
    @Path("node/lock")
    @Produces(MediaType.APPLICATION_JSON)
    boolean lockNodes(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurls") Set<String> nodeUrls)
            throws NotConnectedException, PermissionRestException;

    /**
     * Allow other users to use a set of previously locked nodes.
     *
     * @param sessionId
     *            current session
     * @param nodeUrls
     *            set of node urls to unlock
     * @return true when all nodes were locked and have been unlocked
     */
    @POST
    @Path("node/unlock")
    @Produces(MediaType.APPLICATION_JSON)
    boolean unlockNodes(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurls") Set<String> nodeUrls)
            throws NotConnectedException, PermissionRestException;

    /**
     * Retrieves attributes of the specified JMX MBean.
     *
     * @param sessionId current session
     * @param nodeJmxUrl mbean server url
     * @param objectName name of mbean
     * @param attrs set of mbean attributes
     *
     * @return mbean attributes values
     */
    @GET
    @GZIP
    @Path("node/mbean")
    @Produces(MediaType.APPLICATION_JSON)
    Object getNodeMBeanInfo(@HeaderParam("sessionid") String sessionId, @QueryParam("nodejmxurl") String nodeJmxUrl,
            @QueryParam("objectname") String objectName, @QueryParam("attrs") List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException;

    /**
     * Statistic history of a single node.
     *
     * Return the statistic history contained in the node RRD database,
     * without redundancy, in a friendly JSON format.
     *
     * @param sessionId a valid session
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
    @GET
    @GZIP
    @Path("node/mbean/history")
    @Produces(MediaType.APPLICATION_JSON)
    String getNodeMBeanHistory(@HeaderParam("sessionid") String sessionId, @QueryParam("nodejmxurl") String nodeJmxUrl,
            @QueryParam("objectname") String objectName, @QueryParam("attrs") List<String> attrs,
            @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException;

    /**
     * Statistic history of a list of nodes for a single MBean.
     *
     * Return the statistic history of a list of nodes contained in the node RRD database,
     * without redundancy, in a friendly JSON format.
     *
     * @param sessionId a valid session
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
     * @return a Map where each entry contains the mbean server url as key and a map of statistic values as value. Each entry has an mbean attribute as key and
     * list of its mbean values as value.
     */
    @GET
    @GZIP
    @Path("nodes/mbean/history")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Map<String, Object>> getNodesMBeanHistory(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodesjmxurl") List<String> nodesJmxUrl, @QueryParam("objectname") String objectName,
            @QueryParam("attrs") List<String> attrs, @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException;

    /**
     * Retrieves attributes of the specified JMX MBeans.
     *
     * @param sessionId current session
     * @param objectNames mbean names (@see ObjectName format)
     * @param nodeJmxUrl mbean server url
     * @param attrs set of mbean attributes
     *
     * @return mbean attributes values
     */
    @GET
    @GZIP
    @Path("node/mbeans")
    @Produces(MediaType.APPLICATION_JSON)
    Object getNodeMBeansInfo(@HeaderParam("sessionid") String sessionId, @QueryParam("nodejmxurl") String nodeJmxUrl,
            @QueryParam("objectname") String objectNames, @QueryParam("attrs") List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, PermissionRestException;

    /**
     * Retrieves history of specified MBean attributes on a single node.
     *
     * @param sessionId current session
     * @param nodeJmxUrl mbean server url
     * @param objectNames mbean names (@see ObjectName format)
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
     * @return a JSON object containing, for each attribute, a list of statistics values
     */
    @GET
    @GZIP
    @Path("node/mbeans/history")
    @Produces(MediaType.APPLICATION_JSON)
    Object getNodeMBeansHistory(@HeaderParam("sessionid") String sessionId, @QueryParam("nodejmxurl") String nodeJmxUrl,
            @QueryParam("objectname") String objectNames, @QueryParam("attrs") List<String> attrs,
            @QueryParam("range") String range) throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, IOException, NotConnectedException, MalformedObjectNameException, NullPointerException,
            MBeanException, PermissionRestException;

    /**
     * Initiate the shutdown of the resource manager.
     *
     * During the shutdown resource manager removed all the nodes and kills them if necessary.
     * RMEvent(SHUTDOWN) will be send when the shutdown is finished.
     *
     * @param sessionId a valid session
     * @param preempt if true shutdown immediatly whithout waiting for nodes to be freed, default value is false
     * @return true if the shutdown process is successfully triggered, runtime exception otherwise
     */
    @GET
    @Path("shutdown")
    @Produces(MediaType.APPLICATION_JSON)
    boolean shutdown(@HeaderParam("sessionid") String sessionId,
            @QueryParam("preempt") @DefaultValue("false") boolean preempt)
            throws NotConnectedException, PermissionRestException;

    @GET
    @Path("topology")
    @Produces(MediaType.APPLICATION_JSON)
    TopologyData getTopology(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException;

    /**
     * Supported infrastructures list.
     *
     * Returns the list of supported node source infrastructures descriptors.
     *
     * @param sessionId a valid session
     * @return the list of supported node source infrastructures descriptors
     */
    @GET
    @GZIP
    @Path("infrastructures")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<PluginDescriptorData> getSupportedNodeSourceInfrastructures(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException;

    /**
     * Supported infrastructures mapping to policies.
     *
     * @param sessionId a valid session
     * @return a mapping which for each infrastructure provides list of policies
     * that can work together with given infrastructure
     */
    @GET
    @GZIP
    @Path("infrastructures/mapping")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, List<String>> getInfrasToPoliciesMapping(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException;

    /**
     * Returns the list of supported node source policies descriptors.
     *
     * @param sessionId current session
     * @return the list of supported node source policies descriptors
     */
    @GET
    @GZIP
    @Path("policies")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<PluginDescriptorData> getSupportedNodeSourcePolicies(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException, PermissionRestException;

    /**
     * Get Node Source configuration.
     *
     * @param sessionId current session
     * @param nodeSourceName node source name
     * @return a node source configuration object.
     */
    @GET
    @GZIP
    @Path("nodesource/configuration")
    @Produces(MediaType.APPLICATION_JSON)
    NodeSourceConfiguration getNodeSourceConfiguration(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodeSourceName") String nodeSourceName) throws NotConnectedException, PermissionRestException;

    /**
     * JMX Mbean information.
     *
     * Returns the attributes <code>attr</code> of the mbean
     * registered as <code>name</code>.
     * @param sessionId a valid session
     * @param name mbean's object name
     * @param attrs attributes to enumerate
     * @return returns the attributes of the mbean
     */
    @GET
    @GZIP
    @Path("info/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    Object getMBeanInfo(@HeaderParam("sessionid") String sessionId, @PathParam("name") ObjectName name,
            @QueryParam("attr") List<String> attrs) throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, IOException, NotConnectedException, PermissionRestException;

    /**
     * Set a JMX Mbean single attribute.
     *
     * Set a single JMX attribute of the MBean <code>name</code>.
     * Only integer and string attributes are currently supported, see <code>type</code>.
     *
     * @param sessionId a valid session ID
     * @param name      the object name of the MBean
     * @param type      the type of the attribute to set ('integer' and 'string' are currently supported, see <code>RMProxyUserInterface</code>)
     * @param attr      the name of the attribute to set
     * @param value     the new value of the attribute (defined as a String, it is automatically converted according to <code>type</code>)
     */
    @POST
    @GZIP
    @Path("info/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    void setMBeanInfo(@HeaderParam("sessionid") String sessionId, @PathParam("name") ObjectName name,
            @QueryParam("type") String type, @QueryParam("attr") String attr, @QueryParam("value") String value)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException;

    /**
     * Statistics history.
     *
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
     * @param sessionId a valid session
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
     * @param function function applying to statistics, one of: <ul>
     *            <li>AVERAGE: The average of the data points is stored.</li>
     *            <li>MIN: The smallest of the data points is stored.</li>
     *            <li>MAX: The largest of the data points is stored.</li>
     *            <li>LAST: The last data point is used.</li>
     *            <li>FIRST: The fist data point is used.</li>
     *            <li>TOTAL: The total of the data points is stored.</li>
     *            </ul>
     *            Default value is AVERAGE.
     * @return a JSON object containing a key for each source
     */
    @GET
    @GZIP
    @Path("stathistory")
    @Produces(MediaType.APPLICATION_JSON)
    String getStatHistory(@HeaderParam("sessionid") String sessionId, @QueryParam("range") String range,
            @QueryParam("function") String function)
            throws ReflectionException, InterruptedException, IntrospectionException, NotConnectedException,
            InstanceNotFoundException, MalformedObjectNameException, IOException;

    /**
     * Returns the version of the rest api.
     *
     * @return returns the version of the rest api
     */
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    String getVersion();

    /**
     * Execute a script on the given node.
     *
     * @param sessionId current session
     * @param nodeUrl URL of the node where the script will be executed
     * @param script content of the script to execute
     * @param scriptEngine script engine name (language of the script)
     * @return the script result object
     * @throws Throwable if the script connot be executed
     */
    @POST
    @GZIP
    @Path("node/script")
    @Produces(MediaType.APPLICATION_JSON)
    ScriptResult<Object> executeNodeScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeurl") String nodeUrl, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable;

    /**
     * Execute a script on all machines connected to the given node source.
     *
     * @param sessionId current session
     * @param nodeSource node source name
     * @param script content of the script to execute
     * @param scriptEngine script engine name (language of the script)
     * @return the script result object
     * @throws Throwable if the script connot be executed
     */
    @POST
    @GZIP
    @Path("nodesource/script")
    @Produces(MediaType.APPLICATION_JSON)
    List<ScriptResult<Object>> executeNodeSourceScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodesource") String nodeSource, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable;

    /**
     * Execute a script on the given machine.
     *
     * Any available node on the given machine will be selected to execute the script.
     *
     * @param sessionId current session
     * @param host machine hostname
     * @param script content of the script to execute
     * @param scriptEngine script engine name (language of the script)
     * @return the script result object
     * @throws Throwable if the script connot be executed
     */
    @POST
    @GZIP
    @Path("host/script")
    @Produces(MediaType.APPLICATION_JSON)
    ScriptResult<Object> executeHostScript(@HeaderParam("sessionid") String sessionId, @FormParam("host") String host,
            @FormParam("script") String script, @FormParam("scriptEngine") String scriptEngine) throws Throwable;

    /**
     * Resource Manager Thread Dump.
     *
     * Returns a java thread dump representing the current state of the resource manager.
     *
     * @param sessionId current session
     * @return the resource manager thread dump as string
     */
    @GET
    @GZIP
    @Path("threaddump")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    String getRMThreadDump(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    /**
     * Node Thread Dump.
     *
     * Returns a java thread dump representing the current state of a Node.
     * @param sessionId current session
     * @param nodeUrl node URL
     * @return the node thread dump as string
     */
    @GET
    @GZIP
    @Path("node/threaddump")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    String getNodeThreadDump(@HeaderParam("sessionid") String sessionId, @QueryParam("nodeurl") String nodeUrl)
            throws NotConnectedException;

    /**
     * Get usage history for all Nodes.
     * Returns a 3-level deep JSON tree structure containing RMNodeHistory objects:
     * <ol>
     *     <li>Node source name</li>
     *     <li>Host name</li>
     *     <li>Node name</li>
     * </ol>
     * @param sessionId current session
     * @param windowStart EPOCH start time
     * @param windowEnd EPOCH end time
     * @return a JSON structure containing history for all nodes.
     * @throws NotConnectedException
     */
    @GET
    @GZIP
    @Path("nodes/history")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Map<String, Map<String, List<RMNodeHistory>>>> getNodesHistory(
            @HeaderParam("sessionid") String sessionId, @HeaderParam("windowStart") long windowStart,
            @HeaderParam("windowEnd") long windowEnd) throws NotConnectedException;

    /**
     * Add access token to the given node.
     * @param sessionId current session
     * @param nodeUrl URL of the Node
     * @param token single token
     */
    @POST
    @GZIP
    @Path("node/token")
    @Produces(MediaType.APPLICATION_JSON)
    void addNodeToken(@HeaderParam("sessionid") String sessionId, @HeaderParam("nodeurl") String nodeUrl,
            @HeaderParam("token") String token) throws NotConnectedException, RestException;

    /**
     * Remove access token from the given node.
     * @param sessionId current session
     * @param nodeUrl URL of the Node
     * @param token single token
     */
    @DELETE
    @GZIP
    @Path("node/token")
    @Produces(MediaType.APPLICATION_JSON)
    void removeNodeToken(@HeaderParam("sessionid") String sessionId, @HeaderParam("nodeurl") String nodeUrl,
            @HeaderParam("token") String token) throws NotConnectedException, RestException;

    /**
     * Set all node tokens to the given node.
     * @param sessionId current session
     * @param nodeUrl URL of the Node
     * @param tokens list of tokens to set
     */
    @POST
    @GZIP
    @Path("node/tokens")
    @Produces(MediaType.APPLICATION_JSON)
    void setNodeTokens(@HeaderParam("sessionid") String sessionId, @HeaderParam("nodeurl") String nodeUrl,
            @QueryParam("tokens") List<String> tokens) throws NotConnectedException, RestException;

    /**
     * Get the set of all tags present in all nodes
     * @param sessionId current session
     * @return a set of all nodes tags
     */
    @GET
    @Path("node/tags")
    @Produces(MediaType.APPLICATION_JSON)
    Set<String> getNodeTags(@HeaderParam("sessionid") String sessionId) throws NotConnectedException, RestException;

    /**
     * Get the tags of a specific node
     * @param sessionId current session
     * @param url the url of the requested node
     * @return a set of tags for the specified node
     */
    @GET
    @Path("node/tags/search")
    @Produces(MediaType.APPLICATION_JSON)
    Set<String> getNodeTags(@HeaderParam("sessionid") String sessionId, @QueryParam("nodeurl") String url)
            throws NotConnectedException, RestException;

    /**
     * Search the nodes with specific tags.
     * @param sessionId current session
     * @param tags a list of tags which the nodes should contain. When not specified or an empty list, all the nodes known urls are returned
     * @param all When true, the search return nodes which contain all tags;
     *            when false, the search return nodes which contain any tag among the list tags.
     * @return the set of urls which match the search condition
     */
    @GET
    @Path("nodes/search")
    @Produces(MediaType.APPLICATION_JSON)
    Set<String> searchNodes(@HeaderParam("sessionid") String sessionId, @QueryParam("tags") List<String> tags,
            @QueryParam("all") @DefaultValue("true") boolean all) throws NotConnectedException, RestException;

    /**
     * Check is the user has admin permission for the given nodes or it is a node source provider
     *
     * @param sessionId
     *            current session
     * @param nodeUrls
     *            set of node urls to check the permission for
     * @return a map containing the node url and true if the user has permission for the node
     */
    @POST
    @Path("nodes/permission")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Boolean> checkNodesPermission(@HeaderParam("sessionid") String sessionId, Set<String> nodeUrls)
            throws NotConnectedException, PermissionRestException;

}
