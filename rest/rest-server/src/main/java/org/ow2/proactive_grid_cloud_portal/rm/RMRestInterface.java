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
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.resourcemanager.common.NSState;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateDelta;
import org.ow2.proactive.resourcemanager.common.event.dto.RMStateFull;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.NodeSourceConfiguration;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;


@Path("/rm")
public interface RMRestInterface {

    /**
     * Log into the resource manager using an form containing 2 fields
     * @return the sessionid of the user if succeed
     */
    @POST
    @Path("login")
    @Produces("application/json")
    String rmConnect(@FormParam("username") String username, @FormParam("password") String password)
            throws KeyException, LoginException, RMException, ActiveObjectCreationException, NodeException;

    /**
     * Disconnects from resource manager and releases all the nodes taken by
     * user for computations.
     *
     * @param sessionId
     *            a valid session id
     */
    @POST
    @Path("disconnect")
    @Produces("application/json")
    void rmDisconnect(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    @Produces("application/json")
    String loginWithCredential(@MultipartForm LoginForm multipart)
            throws ActiveObjectCreationException, NodeException, KeyException, IOException, LoginException, RMException;

    /**
     * Get the login string associated to the {@code sessionId} if it exists
     *
     * In case that the given sessionId doesn't have an associated login (session id expired, or invalid),
     * this endpoint will return an empty string
     *
     * @param sessionId with which the endpoint is going to look for the login value
     * @return the associated login value or an empty string
     */
    @GET
    @Path("logins/sessionid/{sessionId}")
    @Produces("application/json")
    String getLoginFromSessionId(@PathParam("sessionId") String sessionId);

    /**
     * Get a UserData object associated to the user connected with the {@code sessionId}
     *
     * In case that the given sessionId doesn't have an associated login (session id expired, or invalid),
     * this endpoint will return null
     *
     * @param sessionId with which the endpoint is going to look for the login value
     * @return a UserData object or null
     */
    @GET
    @Path("logins/sessionid/{sessionId}/userdata")
    @Produces("application/json")
    UserData getUserDataFromSessionId(@PathParam("sessionId") String sessionId);

    /**
     * Returns the state of the Resource Manager
     * @param sessionId a valid session id
     * @return Returns the state of the scheduler
     */
    @GET
    @Path("state")
    @Produces("application/json")
    RMState getState(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    /**
     * Returns difference between current state of the resource manager
     * and state that the client is aware of. Each event that changes state fo the RM
     * has counter assosiacted to it. Thus client has to provide 'latestCounter',
     * i.e. latest event he is aware of.
     * @param sessionId a valid session id
     * @param clientCounter (optional) is the latest counter client has, if parameter is not provided then
     *                                 method returns all events
     * @return the difference between current state and state that client knows
     */
    @GET
    @GZIP
    @Path("monitoring")
    @Produces("application/json")
    RMStateDelta getRMStateDelta(@HeaderParam("sessionid") String sessionId,
            @HeaderParam("clientCounter") @DefaultValue("-1") String clientCounter) throws NotConnectedException;

    /**
     * Returns the full state of the RM, which does not include REMOVED node/nodesources
     * @param sessionId a valid session id
     * @return the state of the RM, which does not include REMOVED node/nodesources
     */
    @GET
    @GZIP
    @Path("monitoring/full")
    @Produces("application/json")
    RMStateFull getRMStateFull(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    /**
     * Returns true if the resource manager is operational.
     *
     * @param sessionId
     *            a valid session id
     * @return true if the resource manager is operational.
     */
    @GET
    @Path("isactive")
    @Produces("application/json")
    boolean isActive(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    /**
     * Adds an existing node to the particular node source.
     *
     * @param sessionId
     *            a valid session id
     * @param url
     *            the url of the node
     * @param nodesource
     *            the node source, can be null
     * @return true if new node is added successfully, runtime exception
     *         otherwise
     */
    @POST
    @Path("node")
    @Produces("application/json")
    boolean addNode(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurl") String url,
            @FormParam("nodesource") String nodesource) throws NotConnectedException;

    /**
     * Returns true if the node nodeUrl is registered (i.e. known by the RM) and
     * not down.
     *
     * @param sessionId
     *            a valid session id
     * @param url
     *            the url of the node
     * @return true if the node nodeUrl is registered and not down
     */
    @GET
    @Path("node/isavailable")
    @Produces("application/json")
    boolean nodeIsAvailable(@HeaderParam("sessionid") String sessionId, @QueryParam("nodeurl") String url)
            throws NotConnectedException;

    /**
     * Gives list of existing Node Sources
     * @return list of existing Node Sources
     */
    @GET
    @GZIP
    @Path("nodesource")
    @Produces("application/json")
    List<RMNodeSourceEvent> getExistingNodeSources(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException;

    @POST
    @Path("nodesource")
    @Produces("application/json")
    NSState defineNodeSource(@HeaderParam("sessionid") String sessionId, //NOSONAR
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters,
            @FormParam("nodesRecoverable") String nodesRecoverable) throws NotConnectedException;

    @PUT
    @Path("nodesource/edit")
    @Produces("application/json")
    NSState editNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters,
            @FormParam("nodesRecoverable") String nodesRecoverable) throws NotConnectedException;

    @PUT
    @Path("nodesource/parameter")
    @Produces("application/json")
    NSState updateDynamicParameters(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters) throws NotConnectedException;

    /**
     * @deprecated  As of version 8.1, replaced by {@link #defineNodeSource(String, String, String, String[], String[],
     * String, String[], String[], String)} and {@link #deployNodeSource(String, String)}
     */
    @Deprecated
    @POST
    @Path("nodesource/create")
    @Produces("application/json")
    NSState createNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters) throws NotConnectedException;

    /**
     * @deprecated  As of version 8.1, replaced by {@link #defineNodeSource(String, String,String, String[], String[],
     * String, String[], String[], String)} and {@link #deployNodeSource(String, String)}
     *
     * Create a NodeSource
     * <p>
     *
     * @param sessionId
     *            current session id
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
    @Deprecated
    @POST
    @Path("nodesource/create/recovery")
    @Produces("application/json")
    NSState createNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters,
            @FormParam("nodesRecoverable") String nodesRecoverable) throws NotConnectedException;

    /**
     * Start the nodes acquisition of the node source
     *
     * @param sessionId a valid session id
     * @param nodeSourceName the name of the node source to start
     * @return the result of the action, possibly containing the error message
     */
    @PUT
    @Path("nodesource/deploy")
    @Produces("application/json")
    NSState deployNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName) throws NotConnectedException;

    /**
     * Remove the nodes of the node source and keep the node source undeployed
     *
     * @param sessionId a valid session id
     * @param nodeSourceName the name of the node source to undeploy
     * @return the result of the action, possibly containing the error message
     */
    @PUT
    @Path("nodesource/undeploy")
    @Produces("application/json")
    NSState undeployNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName, @FormParam("preempt") boolean preempt)
            throws NotConnectedException;

    /**
     * Returns the ping frequency of a node source
     *
     * @param sessionId a valid session id
     * @param sourceName a node source
     * @return the ping frequency
     */
    @POST
    @Path("nodesource/pingfrequency")
    @Produces("application/json")
    int getNodeSourcePingFrequency(@HeaderParam("sessionid") String sessionId,
            @FormParam("sourcename") String sourceName) throws NotConnectedException;

    /**
     * Release a node
     *
     * @param sessionId a valid session id
     * @param url node's URL
     * @return true of the node has been released
     */
    @POST
    @Path("node/release")
    @Produces("application/json")
    boolean releaseNode(@HeaderParam("sessionid") String sessionId, @FormParam("url") String url)
            throws NodeException, NotConnectedException;

    /**
     * Delete a node
     *
     * @param sessionId a valid session id
     * @param nodeUrl node's URL
     * @param preempt if true remove node source immediatly whithout waiting for nodes to be freed
     * @return true if the node is removed successfully, false or exception otherwise
     */
    @POST
    @Path("node/remove")
    @Produces("application/json")
    boolean removeNode(@HeaderParam("sessionid") String sessionId, @FormParam("url") String nodeUrl,
            @FormParam("preempt") boolean preempt) throws NotConnectedException;

    /**
     * Delete a nodesource
     *
     * @param sessionId a valid session id
     * @param sourceName a node source
     * @param preempt if true remove node source immediatly whithout waiting for nodes to be freed
     * @return true if the node is removed successfully, false or exception otherwise
     * @throws NotConnectedException
     */
    @POST
    @Path("nodesource/remove")
    @Produces("application/json")
    boolean removeNodeSource(@HeaderParam("sessionid") String sessionId, @FormParam("name") String sourceName,
            @FormParam("preempt") boolean preempt) throws NotConnectedException;

    /**
     * prevent other users from using a set of locked nodes
     *
     * @param sessionId
     *            current session
     * @param nodeUrls
     *            set of node urls to lock
     * @return true when all nodes were free and have been locked
     * @throws NotConnectedException
     */
    @POST
    @Path("node/lock")
    @Produces("application/json")
    boolean lockNodes(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurls") Set<String> nodeUrls)
            throws NotConnectedException;

    /**
     * allow other users to use a set of previously locked nodes
     *
     * @param sessionId
     *            current session
     * @param nodeUrls
     *            set of node urls to unlock
     * @return true when all nodes were locked and have been unlocked
     * @throws NotConnectedException
     */
    @POST
    @Path("node/unlock")
    @Produces("application/json")
    boolean unlockNodes(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurls") Set<String> nodeUrls)
            throws NotConnectedException;

    /**
     * Retrieves attributes of the specified mbean.
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
    @Produces("application/json")
    @Path("node/mbean")
    Object getNodeMBeanInfo(@HeaderParam("sessionid") String sessionId, @QueryParam("nodejmxurl") String nodeJmxUrl,
            @QueryParam("objectname") String objectName, @QueryParam("attrs") List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException;

    /**
     * Return the statistic history contained in the node RRD database,
     * without redundancy, in a friendly JSON format.
     *
     * @param sessionId a valid session
     * @param range a String of 5 chars, one for each stat history source, indicating the time range to fetch
     *      for each source. Each char can be:<ul>
     *            <li>'a' 1 minute
     *            <li>'m' 10 minutes
     *            <li>'h' 1 hour
     *            <li>'H' 8 hours
     *            <li>'d' 1 day
     *            <li>'w' 1 week
     *            <li>'M' 1 month
     *            <li>'y' 1 year</ul>
     * @return a JSON object containing a key for each source
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @throws IOException
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     * @throws NotConnectedException
     */
    @GET
    @GZIP
    @Produces("application/json")
    @Path("node/mbean/history")
    Object getNodeMBeanHistory(@HeaderParam("sessionid") String sessionId, @QueryParam("nodejmxurl") String nodeJmxUrl,
            @QueryParam("objectname") String objectName, @QueryParam("attrs") List<String> attrs,
            @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException;

    /**
     * Retrieves attributes of the specified mbeans.
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
    @Produces("application/json")
    @Path("node/mbeans")
    Object getNodeMBeansInfo(@HeaderParam("sessionid") String sessionId, @QueryParam("nodejmxurl") String nodeJmxUrl,
            @QueryParam("objectname") String objectNames, @QueryParam("attrs") List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException;

    @GET
    @GZIP
    @Produces("application/json")
    @Path("node/mbeans/history")
    Object getNodeMBeansHistory(@HeaderParam("sessionid") String sessionId, @QueryParam("nodejmxurl") String nodeJmxUrl,
            @QueryParam("objectname") String objectNames, @QueryParam("attrs") List<String> attrs,
            @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException;

    /**
     * Initiate the shutdowns the resource manager. During the shutdown resource
     * manager removed all the nodes and kills them if necessary.
     * RMEvent(SHUTDOWN) will be send when the shutdown is finished.
     *
     * @param sessionId a valid session
     * @param preempt if true shutdown immediatly whithout waiting for nodes to be freed, default value is false
     * @return true if the shutdown process is successfully triggered, runtime exception otherwise
     * @throws NotConnectedException
     */
    @GET
    @Path("shutdown")
    @Produces("application/json")
    boolean shutdown(@HeaderParam("sessionid") String sessionId,
            @QueryParam("preempt") @DefaultValue("false") boolean preempt) throws NotConnectedException;

    @GET
    @Path("topology")
    @Produces("application/json")
    Topology getTopology(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    /**
     * Returns the list of supported node source infrastructures descriptors.
     *
     * @param sessionId a valid session
     * @return the list of supported node source infrastructures descriptors
     * @throws NotConnectedException
     */
    @GET
    @GZIP
    @Path("infrastructures")
    @Produces("application/json")
    Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException;

    /**
     * @param sessionId a valid session
     * @return a mapping which for each infrastructure provides list of policies
     * that can work together with given infrastructure
     */
    @GET
    @GZIP
    @Path("infrastructuresmapping")
    @Produces("application/json")
    Map<String, List<String>> getInfrasToPoliciesMapping(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException;

    /**
     * Returns the list of supported node source policies descriptors.
     *
     * @param sessionId a valid session
     * @return the list of supported node source policies descriptors
     * @throws NotConnectedException
     */
    @GET
    @GZIP
    @Path("policies")
    @Produces("application/json")
    Collection<PluginDescriptor> getSupportedNodeSourcePolicies(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException;

    @GET
    @GZIP
    @Path("nodesource/configuration")
    @Produces("application/json")
    NodeSourceConfiguration getNodeSourceConfiguration(@HeaderParam("sessionid") String sessionId,
            @QueryParam("nodeSourceName") String nodeSourceName) throws NotConnectedException;

    /**
     * Returns the attributes <code>attr</code> of the mbean
     * registered as <code>name</code>.
     * @param sessionId a valid session
     * @param name mbean's object name
     * @param attrs attributes to enumerate
     * @return returns the attributes of the mbean
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @throws IOException
     * @throws NotConnectedException
     */
    @GET
    @GZIP
    @Path("info/{name}")
    @Produces("application/json")
    Object getMBeanInfo(@HeaderParam("sessionid") String sessionId, @PathParam("name") ObjectName name,
            @QueryParam("attr") List<String> attrs) throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, IOException, NotConnectedException;

    /**
     * Set a single JMX attribute of the MBean <code>name</code>.
     * Only integer and string attributes are currently supported, see <code>type</code>.
     *
     * @param sessionId a valid session ID
     * @param name      the object name of the MBean
     * @param type      the type of the attribute to set ('integer' and 'string' are currently supported, see <code>RMProxyUserInterface</code>)
     * @param attr      the name of the attribute to set
     * @param value     the new value of the attribute (defined as a String, it is automatically converted according to <code>type</code>)
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @throws IOException
     * @throws NotConnectedException
     * @throws MBeanException
     * @throws AttributeNotFoundException
     * @throws InvalidAttributeValueException
     * @throws IllegalArgumentException
     */
    @POST
    @GZIP
    @Path("info/{name}")
    @Produces("application/json")
    void setMBeanInfo(@HeaderParam("sessionid") String sessionId, @PathParam("name") ObjectName name,
            @QueryParam("type") String type, @QueryParam("attr") String attr, @QueryParam("value") String value)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException;

    /**
     * Return the statistic history contained in the RM's RRD database,
     * without redundancy, in a friendly JSON format.
     *
     * The following sources will be queried from the RRD DB :<pre>
     * 	{ "BusyNodesCount",
     *    "FreeNodesCount",
     *    "DownNodesCount",
     *    "AvailableNodesCount",
     *    "AverageActivity" }</pre>
     *
     *
     * @param sessionId a valid session
     * @param range a String of 5 chars, one for each stat history source, indicating the time range to fetch
     *      for each source. Each char can be:<ul>
     *            <li>'a' 1 minute
     *            <li>'m' 10 minutes
     *            <li>'h' 1 hour
     *            <li>'H' 8 hours
     *            <li>'d' 1 day
     *            <li>'w' 1 week
     *            <li>'M' 1 month
     *            <li>'y' 1 year</ul>
     * @return a JSON object containing a key for each source
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @throws IOException
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     * @throws InterruptedException
     * @throws NotConnectedException
     */
    @GET
    @GZIP
    @Path("stathistory")
    @Produces("application/json")
    String getStatHistory(@HeaderParam("sessionid") String sessionId, @QueryParam("range") String range)
            throws ReflectionException, InterruptedException, IntrospectionException, NotConnectedException,
            InstanceNotFoundException, MalformedObjectNameException, IOException;

    /**
     * Returns the version of the rest api
     * @return returns the version of the rest api
     */
    @GET
    @Path("version")
    String getVersion();

    @POST
    @GZIP
    @Path("node/script")
    @Produces("application/json")
    ScriptResult<Object> executeNodeScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeurl") String nodeUrl, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable;

    @POST
    @GZIP
    @Path("nodesource/script")
    @Produces("application/json")
    List<ScriptResult<Object>> executeNodeSourceScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodesource") String nodeSource, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable;

    @POST
    @GZIP
    @Path("host/script")
    @Produces("application/json")
    ScriptResult<Object> executeHostScript(@HeaderParam("sessionid") String sessionId, @FormParam("host") String host,
            @FormParam("script") String script, @FormParam("scriptEngine") String scriptEngine) throws Throwable;

    @GET
    @GZIP
    @Path("threaddump")
    @Produces("application/json")
    String getRMThreadDump(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    @GET
    @GZIP
    @Path("node/threaddump")
    @Produces("application/json")
    String getNodeThreadDump(@HeaderParam("sessionid") String sessionId, @QueryParam("nodeurl") String nodeUrl)
            throws NotConnectedException;

}
