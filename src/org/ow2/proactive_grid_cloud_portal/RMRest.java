/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyException;
import java.util.Collection;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.proactive_grid_cloud_portal.common.LoginForm;

@Path("/rm")
public class RMRest {

    public RMCachingProxyInterface checkAccess(String sessionId) throws WebApplicationException {
        RMCachingProxyInterface s = RMSessionMapper.getInstance().getSessionsMap().get(sessionId);

        if (s == null) {
            throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_UNAUTHORIZED)
                    .entity("you are not connected, try to log in first").build());
        }

        RMSessionMapper.getInstance().getSessionsLastAccessToClient().put(sessionId, new Long(System.currentTimeMillis()));
        return s;
    }

    /**
     * 
     * @param username
     * @param password
     * @return
     * @throws RMException 
     * @throws LoginException 
     * @throws KeyException 
     * @throws NodeException 
     * @throws ActiveObjectCreationException 
     */
    @POST
    @Path("login")
    @Produces("application/json")
    public String rmConnect(@FormParam("username") String username, @FormParam("password") String password) throws KeyException, LoginException, RMException, ActiveObjectCreationException, NodeException {


        RMCachingProxyInterface  rm;
           rm = PAActiveObject.newActive(RMCachingProxyInterface.class, new Object[] {});
           
            CredData credData = new CredData(CredData.parseLogin(username), CredData.parseDomain(username),
                password);

            rm.init(PortalConfiguration.getProperties().getProperty(PortalConfiguration.rm_url), credData);

            return "" + RMSessionMapper.getInstance().add(rm);


    }
    
    
    @POST
    @Path("disconnect")
    @Produces("application/json")
    public void rmDisconnect(@HeaderParam("sessionid") String sessionId) {
        RMCachingProxyInterface rm = checkAccess(sessionId);
        rm.disconnect();
        PAActiveObject.terminateActiveObject(rm,true);
        RMSessionMapper.getInstance().remove(sessionId);
    }


    

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive_grid_cloud_portal.SchedulerRestInterface#loginWithCredential(org.ow2.
     * proactive_grid_cloud_portal.LoginForm)
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    @Produces("application/json")
    public String loginWithCredential(@MultipartForm LoginForm multipart)
            throws ActiveObjectCreationException, NodeException, KeyException, IOException, LoginException,
            RMException {

        RMCachingProxyInterface rm = PAActiveObject.newActive(RMCachingProxyInterface.class, new Object[] {});

        String url = PortalConfiguration.getProperties().getProperty(PortalConfiguration.rm_url);

        if (multipart.getCredential() != null) {
            Credentials credentials = Credentials.getCredentials(multipart.getCredential());
            rm.init(url, credentials);
        } else {
            CredData credData = new CredData(CredData.parseLogin(multipart.getUsername()),
                CredData.parseDomain(multipart.getUsername()), multipart.getPassword(), multipart.getSshKey());
            rm.init(url, credData);
        }

        String sessionId = "" + RMSessionMapper.getInstance().add(rm);
        //      logger.info("binding user "+  " to session " + sessionId );
        return sessionId;

    }

    /**
     * Returns the state of the scheduler
     * @param sessionId a valid session id
     * @return Returns the state of the scheduler
     */
    @GET
    @Path("state")
    @Produces("application/json")
    public RMState getState(@HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getState());
    }

    /**
     * Returns the initial state of the resource manager
     * @param sessionId
     * @return the initial state of the resource manager 
     */
    @GET
    @Path("monitoring")
    @Produces("application/json")
    public RMInitialState getInitialState(@HeaderParam("sessionid") String sessionId) {
        RMCachingProxyInterface rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getRMInitialState());
    }

    /**
     * Returns true if the resource manager is operational.
     * @param sessionId a valid session id
     * @return true if the resource manager is operational.
     */
    @GET
    @Path("isactive")
    @Produces("application/json")
    public boolean isActive(@HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.isActive().getBooleanValue();
    }

    /**
     * Adds an existing node to the default node source of the resource manager.
     * @param sessionId
     * @param nodeUrl the url of the node
     * @return  true if new node is added successfully
     */
    @POST
    @Produces("application/json")
    @Path("node")
    public boolean addNode(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurl") String nodeUrl) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.addNode(nodeUrl).getBooleanValue();
    }

    /**
     * Adds an existing node to the particular node source.
     * @param sessionId a valid session id 
     * @param url the url of the node 
     * @param nodesource the node source 
     * @return  true if new node is added successfully, runtime exception otherwise
     */
    @POST
    @Path("node")
    @Produces("application/json")
    public boolean addNode(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurl") String url,
            @FormParam("nodesource") String nodesource) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.addNode(url, nodesource).getBooleanValue();
    }

    /**
     * Returns true if the node nodeUrl is registered (i.e. known by the RM) and not down.
     * @param sessionId a valid session id 
     * @param url the url of the node 
     * @return  true if the node nodeUrl is registered and not down
     */
    @GET
    @Path("node/isavailable")
    @Produces("application/json")
    public boolean nodeIsAvailable(@HeaderParam("sessionid") String sessionId,
            @FormParam("nodeurl") String url) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.nodeIsAvailable(url).getBooleanValue();
    }

    /**
     * Disconnects from resource manager and releases all the nodes taken by user for computations.
     * @param sessionId a valid session id 
     * @return true if successfully disconnected
     */
    @POST
    @Path("disconnect")
    @Produces("application/json")
    public boolean disconnect(@HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = checkAccess(sessionId);
        RMSessionMapper.getInstance().getSessionsMap().remove(rm);
        return rm.disconnect().getBooleanValue();
    }

    public boolean createNodeSource(@HeaderParam("sessionid") String sessionId, String nodeSourceName,
            String infrastructureType, Object[] infrastructureParameters, String policyType,
            Object[] policyParameters) {
        ResourceManager rm = checkAccess(sessionId);

        return rm.createNodeSource(nodeSourceName, infrastructureType, infrastructureParameters, policyType,
                policyParameters).getBooleanValue();
    }

    @POST
    @Path("nodesource/pingfrequency")
    @Produces("application/json")
    public int getNodeSourcePingFrequency(@HeaderParam("sessionid") String sessionId,
            @FormParam("sourcename") String sourceName) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getNodeSourcePingFrequency(sourceName).getIntValue();
    }

    /**
     * 
     * @param sessionId
     * @param url
     * @return
     * @throws NodeException 
     */
    public boolean releaseNode(@HeaderParam("sessionid") String sessionId, String url) throws NodeException {
        ResourceManager rm = checkAccess(sessionId);
        Node n;
        n = NodeFactory.getNode(url);
        return rm.releaseNode(n).getBooleanValue();
    }

    public boolean removeNode(@HeaderParam("sessionid") String sessionId, String nodeUrl, boolean preempt) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.removeNode(nodeUrl, preempt).getBooleanValue();
    }

    public boolean removeNodeSource(@HeaderParam("sessionid") String sessionId, String sourceName,
            boolean preempt) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.removeNodeSource(sourceName, preempt).getBooleanValue();
    }

    /**
     * Each node source scan its nodes periodically to check their states. 
     * This method changes the period of nodes scanning.
     * @param arg0
     * @param arg1
     * @return
     */
    public boolean setNodeSourcePingFrequency(@HeaderParam("sessionid") String sessionId, int frequency,
            String sourcename) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.setNodeSourcePingFrequency(frequency, sourcename).getBooleanValue();
    }

    /**
     * Initiate the shutdowns the resource manager. During the shutdown resource manager removed all the 
     * nodes and kills them if necessary. RMEvent(SHUTDOWN) will be send when the shutdown is finished.
     * @param sessionId
     * @param arg0
     * @return
     */
    @GET
    @Path("shutdown")
    @Produces("application/json")
    public boolean shutdown(@HeaderParam("sessionid") String sessionId, boolean preempt) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.shutdown(preempt).getBooleanValue();
    }

    public NodeSet getAtMostNodes(@HeaderParam("sessionid") String sessionId, int number,
            TopologyDescriptor descriptor, List<SelectionScript> selectionScriptsList, NodeSet exclusion) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getAtMostNodes(number, descriptor, selectionScriptsList, exclusion);
    }

    public Topology getTopology(@HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getTopology();
    }

    public NodeSet getAtMostNodes(@HeaderParam("sessionid") String sessionId, int arg0, SelectionScript arg1) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getAtMostNodes(arg0, arg1);
    }

    public NodeSet getAtMostNodes(@HeaderParam("sessionid") String sessionId, int arg0, SelectionScript arg1,
            NodeSet arg2) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getAtMostNodes(arg0, arg1, arg2);
    }

    public NodeSet getAtMostNodes(@HeaderParam("sessionid") String sessionId, int arg0,
            List<SelectionScript> arg1, NodeSet arg2) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getAtMostNodes(arg0, arg1, arg2);
    }

    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures(
            @HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getSupportedNodeSourceInfrastructures();
    }

    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies(
            @HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getSupportedNodeSourcePolicies();
    }

    @GET
    @Path("info/{name}")
    @Produces("application/json")
    public Object getMBeanInfo(@HeaderParam("sessionid") String sessionId,
            @PathParam("name") ObjectName name, @QueryParam("attr") List<String> attrs)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
        RMProxy rm = checkAccess(sessionId);

        if ((attrs == null) || (attrs.size() == 0)) {
            // no attribute is requested, we return
            // the description of the mbean
            return rm.getMBeanInfo(name);

        } else {
            return rm.getMBeanAttributes(name, attrs.toArray(new String[] {}));
        }

    }

    @GET
    @Path("version")
    public String getVersion() {
        return PortalConfiguration.REST_API_VERSION;
    }
    
}
