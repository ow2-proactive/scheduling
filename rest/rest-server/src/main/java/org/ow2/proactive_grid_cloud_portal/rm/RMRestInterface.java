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
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;


@Path("/rm")
public interface RMRestInterface {
    @POST
    @Path("login")
    @Produces("application/json")
    String rmConnect(@FormParam("username") String username, @FormParam("password") String password)
            throws KeyException, LoginException, RMException, ActiveObjectCreationException, NodeException;

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

    @GET
    @Path("state")
    @Produces("application/json")
    RMState getState(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    @GET
    @GZIP
    @Path("monitoring")
    @Produces("application/json")
    RMInitialState getInitialState(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    @GET
    @Path("isactive")
    @Produces("application/json")
    boolean isActive(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    @POST
    @Path("node")
    @Produces("application/json")
    boolean addNode(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurl") String url,
            @FormParam("nodesource") String nodesource) throws NotConnectedException;

    @GET
    @Path("node/isavailable")
    @Produces("application/json")
    boolean nodeIsAvailable(@HeaderParam("sessionid") String sessionId, @QueryParam("nodeurl") String url)
            throws NotConnectedException;

    @GET
    @GZIP
    @Path("nodesource")
    @Produces("application/json")
    List<RMNodeSourceEvent> getExistingNodeSources(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException;

    @POST
    @Path("nodesource/create")
    @Produces("application/json")
    boolean createNodeSource(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeSourceName") String nodeSourceName,
            @FormParam("infrastructureType") String infrastructureType,
            @FormParam("infrastructureParameters") String[] infrastructureParameters,
            @FormParam("infrastructureFileParameters") String[] infrastructureFileParameters,
            @FormParam("policyType") String policyType, @FormParam("policyParameters") String[] policyParameters,
            @FormParam("policyFileParameters") String[] policyFileParameters) throws NotConnectedException;

    @POST
    @Path("nodesource/pingfrequency")
    @Produces("application/json")
    int getNodeSourcePingFrequency(@HeaderParam("sessionid") String sessionId,
            @FormParam("sourcename") String sourceName) throws NotConnectedException;

    @POST
    @Path("node/release")
    @Produces("application/json")
    boolean releaseNode(@HeaderParam("sessionid") String sessionId, @FormParam("url") String url)
            throws NodeException, NotConnectedException;

    @POST
    @Path("node/remove")
    @Produces("application/json")
    boolean removeNode(@HeaderParam("sessionid") String sessionId, @FormParam("url") String nodeUrl,
            @FormParam("preempt") boolean preempt) throws NotConnectedException;

    @POST
    @Path("nodesource/remove")
    @Produces("application/json")
    boolean removeNodeSource(@HeaderParam("sessionid") String sessionId, @FormParam("name") String sourceName,
            @FormParam("preempt") boolean preempt) throws NotConnectedException;

    @POST
    @Path("node/lock")
    @Produces("application/json")
    boolean lockNodes(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurls") Set<String> nodeUrls)
            throws NotConnectedException;

    @POST
    @Path("node/unlock")
    @Produces("application/json")
    boolean unlockNodes(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurls") Set<String> nodeUrls)
            throws NotConnectedException;

    @GET
    @GZIP
    @Produces("application/json")
    @Path("node/mbean")
    Object getNodeMBeanInfo(@HeaderParam("sessionid") String sessionId, @QueryParam("nodejmxurl") String nodeJmxUrl,
            @QueryParam("objectname") String objectName, @QueryParam("attrs") List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException;

    @GET
    @GZIP
    @Produces("application/json")
    @Path("node/mbean/history")
    Object getNodeMBeanHistory(@HeaderParam("sessionid") String sessionId, @QueryParam("nodejmxurl") String nodeJmxUrl,
            @QueryParam("objectname") String objectName, @QueryParam("attrs") List<String> attrs,
            @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MalformedObjectNameException, NullPointerException, MBeanException;

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

    @GET
    @Path("shutdown")
    @Produces("application/json")
    boolean shutdown(@HeaderParam("sessionid") String sessionId,
            @QueryParam("preempt") @DefaultValue("false") boolean preempt) throws NotConnectedException;

    @GET
    @Path("topology")
    @Produces("application/json")
    Topology getTopology(@HeaderParam("sessionid") String sessionId) throws NotConnectedException;

    @GET
    @GZIP
    @Path("infrastructures")
    @Produces("application/json")
    Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException;

    @GET
    @GZIP
    @Path("policies")
    @Produces("application/json")
    Collection<PluginDescriptor> getSupportedNodeSourcePolicies(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedException;

    @GET
    @GZIP
    @Path("info/{name}")
    @Produces("application/json")
    Object getMBeanInfo(@HeaderParam("sessionid") String sessionId, @PathParam("name") ObjectName name,
            @QueryParam("attr") List<String> attrs) throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, IOException, NotConnectedException;

    @POST
    @GZIP
    @Path("info/{name}")
    @Produces("application/json")
    void setMBeanInfo(@HeaderParam("sessionid") String sessionId, @PathParam("name") ObjectName name,
            @QueryParam("type") String type, @QueryParam("attr") String attr, @QueryParam("value") String value)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            NotConnectedException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException;

    @GET
    @GZIP
    @Path("stathistory")
    @Produces("application/json")
    String getStatHistory(@HeaderParam("sessionid") String sessionId, @QueryParam("range") String range)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            MalformedObjectNameException, NullPointerException, InterruptedException, NotConnectedException;

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
    List<ScriptResult<Object>> executeHostScript(@HeaderParam("sessionid") String sessionId,
            @FormParam("host") String host, @FormParam("script") String script,
            @FormParam("scriptEngine") String scriptEngine) throws Throwable;
}
