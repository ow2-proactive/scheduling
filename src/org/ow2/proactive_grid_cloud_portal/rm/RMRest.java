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
package org.ow2.proactive_grid_cloud_portal.rm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MalformedObjectNameException;
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
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.GZIP;
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
import org.ow2.proactive.resourcemanager.common.util.RMCachingProxyUserInterface;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXHelper;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.proactive_grid_cloud_portal.common.LoginForm;
import org.ow2.proactive_grid_cloud_portal.common.StatHistoryCaching;
import org.ow2.proactive_grid_cloud_portal.common.StatHistoryCaching.StatHistoryCacheEntry;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;


@Path("/rm")
public class RMRest {

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

    public RMCachingProxyUserInterface checkAccess(String sessionId) throws NotConnectedException {
        RMCachingProxyUserInterface s = RMSessionMapper.getInstance().getSessionsMap().get(sessionId);

        if (s == null) {
            throw new NotConnectedException("you are not connected to the scheduler, you should log on first");
        }

        RMSessionMapper.getInstance().getSessionsLastAccessToClient().put(sessionId,
                new Long(System.currentTimeMillis()));
        return s;
    }

    /**
     * log into the resource manager using an form containing 2 fields 
     * @param username
     * @param password
     * @return the sessionid of the user if succeed
     * @throws RMException 
     * @throws LoginException 
     * @throws KeyException 
     * @throws NodeException 
     * @throws ActiveObjectCreationException 
     */
    @POST
    @Path("login")
    @Produces("application/json")
    public String rmConnect(@FormParam("username")
    String username, @FormParam("password")
    String password) throws KeyException, LoginException, RMException, ActiveObjectCreationException,
            NodeException {

        RMCachingProxyUserInterface rm;
        rm = PAActiveObject.newActive(RMCachingProxyUserInterface.class, new Object[] {});

        CredData credData = new CredData(CredData.parseLogin(username), CredData.parseDomain(username),
            password);

        rm.init(PortalConfiguration.getProperties().getProperty(PortalConfiguration.rm_url), credData);

        return RMSessionMapper.getInstance().add(rm);

    }

    @POST
    @Path("disconnect")
    @Produces("application/json")
    public void rmDisconnect(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException {
        RMCachingProxyUserInterface rm = checkAccess(sessionId);
        rm.disconnect();
        PAActiveObject.terminateActiveObject(rm, true);
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
    public String loginWithCredential(@MultipartForm
    LoginForm multipart) throws ActiveObjectCreationException, NodeException, KeyException, IOException,
            LoginException, RMException {

        RMCachingProxyUserInterface rm = PAActiveObject.newActive(RMCachingProxyUserInterface.class,
                new Object[] {});

        String url = PortalConfiguration.getProperties().getProperty(PortalConfiguration.rm_url);

        if (multipart.getCredential() != null) {
            Credentials credentials = Credentials.getCredentials(multipart.getCredential());
            rm.init(url, credentials);
        } else {
            CredData credData = new CredData(CredData.parseLogin(multipart.getUsername()), CredData
                    .parseDomain(multipart.getUsername()), multipart.getPassword(), multipart.getSshKey());
            rm.init(url, credData);
        }

        String sessionId = RMSessionMapper.getInstance().add(rm);
        //      logger.info("binding user "+  " to session " + sessionId );
        return sessionId;

    }

    /**
     * Returns the state of the Resource Manager
     * @param sessionId a valid session id
     * @return Returns the state of the scheduler
     * @throws NotConnectedException 
     */
    @GET
    @Path("state")
    @Produces("application/json")
    public RMState getState(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getState());
    }

    /**
     * Returns the initial state of the resource manager
     * @param sessionId
     * @return the initial state of the resource manager 
     * @throws NotConnectedException 
     */
    @GET
    @GZIP
    @Path("monitoring")
    @Produces("application/json")
    public RMInitialState getInitialState(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException {
        checkAccess(sessionId);
        return RMStateCaching.getRMInitialState();
    }

    /**
     * Returns true if the resource manager is operational.
     *
     * @param sessionId
     *            a valid session id
     * @return true if the resource manager is operational.
     * @throws NotConnectedException 
     */
    @GET
    @Path("isactive")
    @Produces("application/json")
    public boolean isActive(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.isActive().getBooleanValue();
    }

    /**
     * Adds an existing node to the default node source of the resource manager.
     *
     * @param sessionId
     * @param nodeUrl
     *            the url of the node
     * @return true if new node is added successfully
     * @throws NotConnectedException 
     */
    @POST
    @Produces("application/json")
    @Path("node")
    public boolean addNode(@HeaderParam("sessionid")
    String sessionId, @FormParam("nodeurl")
    String nodeUrl) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.addNode(nodeUrl).getBooleanValue();
    }

    /**
     * Adds an existing node to the particular node source.
     *
     * @param sessionId
     *            a valid session id
     * @param url
     *            the url of the node
     * @param nodesource
     *            the node source
     * @return true if new node is added successfully, runtime exception
     *         otherwise
     * @throws NotConnectedException 
     */
    @POST
    @Path("node")
    @Produces("application/json")
    public boolean addNode(@HeaderParam("sessionid")
    String sessionId, @FormParam("nodeurl")
    String url, @FormParam("nodesource")
    String nodesource) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.addNode(url, nodesource).getBooleanValue();
    }

    /**
     * Returns true if the node nodeUrl is registered (i.e. known by the RM) and
     * not down.
     *
     * @param sessionId
     *            a valid session id
     * @param url
     *            the url of the node
     * @return true if the node nodeUrl is registered and not down
     * @throws NotConnectedException 
     */
    @GET
    @Path("node/isavailable")
    @Produces("application/json")
    public boolean nodeIsAvailable(@HeaderParam("sessionid")
    String sessionId, @QueryParam("nodeurl")
    String url) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.nodeIsAvailable(url).getBooleanValue();
    }

    /**
     * Disconnects from resource manager and releases all the nodes taken by
     * user for computations.
     *
     * @param sessionId
     *            a valid session id
     * @return true if successfully disconnected
     * @throws NotConnectedException 
     */
    @POST
    @Path("disconnect")
    @Produces("application/json")
    public boolean disconnect(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        RMSessionMapper.getInstance().getSessionsMap().remove(rm);
        return rm.disconnect().getBooleanValue();
    }

    /**
     * Create a NodeSource
     * <p>
     *
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
     * @return true if a node source has been created
     * @throws NotConnectedException 
     */
    @POST
    @Path("nodesource/create")
    @Produces("application/json")
    public boolean createNodeSource(@HeaderParam("sessionid")
    String sessionId, @FormParam("nodeSourceName")
    String nodeSourceName, @FormParam("infrastructureType")
    String infrastructureType, @FormParam("infrastructureParameters")
    String[] infrastructureParameters, @FormParam("infrastructureFileParameters")
    String[] infrastructureFileParameters, @FormParam("policyType")
    String policyType, @FormParam("policyParameters")
    String[] policyParameters, @FormParam("policyFileParameters")
    String[] policyFileParameters) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        Object[] infraParams = new Object[infrastructureParameters.length +
            infrastructureFileParameters.length];
        Object[] policyParams = new Object[policyParameters.length + policyFileParameters.length];

        /*
         * we need to merge both infrastructureParameters and
         * infrastructureFileParameters into one to do so we need the
         * infrastructure parameter order from the RM
         */
        for (PluginDescriptor infra : rm.getSupportedNodeSourceInfrastructures()) {
            if (infra.getPluginName().equals(infrastructureType)) {
                int i = 0, j = 0, k = 0;
                for (ConfigurableField field : infra.getConfigurableFields()) {
                    if (field.getMeta().credential() || field.getMeta().fileBrowser()) {
                        // file parameter : insert from the other array, convert
                        // to byte[]
                        infraParams[i] = infrastructureFileParameters[k].getBytes();
                        k++;
                    } else {
                        infraParams[i] = infrastructureParameters[j];
                        j++;
                    }
                    i++;
                }
            }
        }
        for (PluginDescriptor pol : rm.getSupportedNodeSourcePolicies()) {
            if (pol.getPluginName().equals(policyType)) {
                int i = 0, j = 0, k = 0;
                for (ConfigurableField

                field : pol.getConfigurableFields()) {
                    if (field.getMeta().credential() || field.getMeta().password()) {
                        // file parameter : insert from the other array, convert
                        // to byte[]
                        policyParams[i] = policyFileParameters[k].getBytes();
                        k++;
                    } else {
                        policyParams[i] = policyParameters[j];
                        j++;
                    }
                    i++;
                }
            }
        }
        return rm.createNodeSource(nodeSourceName, infrastructureType, infraParams, policyType, policyParams)
                .getBooleanValue();
    }

    /**
     * Returns the ping frequency of a node source
     *
     * @param sessionId
     * @param sourceName
     * @return the ping frequency
     * @throws NotConnectedException 
     */

    @POST
    @Path("nodesource/pingfrequency")
    @Produces("application/json")
    public int getNodeSourcePingFrequency(@HeaderParam("sessionid")
    String sessionId, @FormParam("sourcename")
    String sourceName) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getNodeSourcePingFrequency(sourceName).getIntValue();
    }

    /**
     * Release a node
     *
     * @param sessionId
     * @param url
     * @return true of the node has been released
     * @throws NodeException
     * @throws NotConnectedException 
     */
    @POST
    @Path("node/release")
    @Produces("application/json")
    public boolean releaseNode(@HeaderParam("sessionid")
    String sessionId, @FormParam("url")
    String url) throws NodeException, NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        Node n;
        n = NodeFactory.getNode(url);
        return rm.releaseNode(n).getBooleanValue();
    }

    /**
     * Delete a node
     *
     * @param sessionId
     * @param nodeUrl
     * @param preempt
     * @return
     * @throws NotConnectedException 
     */
    @POST
    @Path("node/remove")
    @Produces("application/json")
    public boolean removeNode(@HeaderParam("sessionid")
    String sessionId, @FormParam("url")
    String nodeUrl, @FormParam("preempt")
    boolean preempt) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.removeNode(nodeUrl, preempt).getBooleanValue();
    }

    /**
     * Delete a nodesource
     *
     * @param sessionId
     * @param sourceName
     * @param preempt
     * @return
     * @throws NotConnectedException 
     */
    @POST
    @Path("nodesource/remove")
    @Produces("application/json")
    public boolean removeNodeSource(@HeaderParam("sessionid")
    String sessionId, @FormParam("name")
    String sourceName, @FormParam("preempt")
    boolean preempt) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.removeNodeSource(sourceName, preempt).getBooleanValue();
    }

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
    public boolean lockNodes(@HeaderParam("sessionid")
    String sessionId, @FormParam("nodeurls")
    Set<String> nodeUrls) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.lockNodes(nodeUrls).getBooleanValue();
    }

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
    public boolean unlockNodes(@HeaderParam("sessionid")
    String sessionId, @FormParam("nodeurls")
    Set<String> nodeUrls) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.unlockNodes(nodeUrls).getBooleanValue();
    }

    /**
     * Retrieves attributes of the specified mbean.
     * 
     * @param sessionId current session
     * @param name of mbean
     * @param nodeJmxUrl mbean server url
     * @param attrs set of mbean attributes
     * 
     * @return mbean attributes values
     */
    @GET
    @GZIP
    @Produces("application/json")
    @Path("node/mbean")
    public Object getNodeMBeanInfo(@HeaderParam("sessionid")
    String sessionId, @QueryParam("nodejmxurl")
    String nodeJmxUrl, @QueryParam("objectname")
    String objectName, @QueryParam("attrs")
    List<String> attrs) throws InstanceNotFoundException, IntrospectionException, ReflectionException,
            IOException, NotConnectedException, MalformedObjectNameException, NullPointerException {

        // checking that still connected to the RM
        RMCachingProxyUserInterface rmProxy = checkAccess(sessionId);
        return rmProxy.getNodeMBeanInfo(nodeJmxUrl, objectName, attrs);
    }

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
    public Object getNodeMBeansInfo(@HeaderParam("sessionid")
    String sessionId, @QueryParam("nodejmxurl")
    String nodeJmxUrl, @QueryParam("objectname")
    String objectNames, @QueryParam("attrs")
    List<String> attrs) throws InstanceNotFoundException, IntrospectionException, ReflectionException,
            IOException, NotConnectedException, MalformedObjectNameException, NullPointerException {

        // checking that still connected to the RM
        RMCachingProxyUserInterface rmProxy = checkAccess(sessionId);
        return rmProxy.getNodeMBeansInfo(nodeJmxUrl, objectNames, attrs);
    }

    /**
     * Each node source scan its nodes periodically to check their states. This
     * method changes the period of nodes scanning.
     *
     * @param arg0
     * @param arg1
     * @return
     * @throws NotConnectedException 
     */
    public boolean setNodeSourcePingFrequency(@HeaderParam("sessionid")
    String sessionId, int frequency, String sourcename) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.setNodeSourcePingFrequency(frequency, sourcename).getBooleanValue();
    }

    /**
     * Initiate the shutdowns the resource manager. During the shutdown resource
     * manager removed all the nodes and kills them if necessary.
     * RMEvent(SHUTDOWN) will be send when the shutdown is finished.
     *
     * @param sessionId
     * @param arg0
     * @return
     * @throws NotConnectedException 
     */
    @GET
    @Path("shutdown")
    @Produces("application/json")
    public boolean shutdown(@HeaderParam("sessionid")
    String sessionId, boolean preempt) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.shutdown(preempt).getBooleanValue();
    }

    public NodeSet getAtMostNodes(@HeaderParam("sessionid")
    String sessionId, int number, TopologyDescriptor descriptor, List<SelectionScript> selectionScriptsList,
            NodeSet exclusion) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getAtMostNodes(number, descriptor, selectionScriptsList, exclusion);
    }

    @GET
    @Path("topology")
    @Produces("application/json")
    public Topology getTopology(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getTopology());
    }

    public NodeSet getAtMostNodes(@HeaderParam("sessionid")
    String sessionId, int arg0, SelectionScript arg1) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getAtMostNodes(arg0, arg1);
    }

    public NodeSet getAtMostNodes(@HeaderParam("sessionid")
    String sessionId, int arg0, SelectionScript arg1, NodeSet arg2) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getAtMostNodes(arg0, arg1, arg2);
    }

    public NodeSet getAtMostNodes(@HeaderParam("sessionid")
    String sessionId, int arg0, List<SelectionScript> arg1, NodeSet arg2) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getAtMostNodes(arg0, arg1, arg2);
    }

    /**
     * Returns the list of supported node source infrastructures descriptors.
     *
     * @param sessionId
     * @return the list of supported node source infrastructures descriptors
     * @throws NotConnectedException 
     */
    @GET
    @GZIP
    @Path("infrastructures")
    @Produces("application/json")
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getSupportedNodeSourceInfrastructures();
    }

    /**
     * Returns the list of supported node source policies descriptors.
     *
     * @param sessionId
     * @return the list of supported node source policies descriptors
     * @throws NotConnectedException 
     */
    @GET
    @GZIP
    @Path("policies")
    @Produces("application/json")
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException {
        ResourceManager rm = checkAccess(sessionId);
        return rm.getSupportedNodeSourcePolicies();
    }

    /**
     * Returns the attributes <code>attr</code> of the mbean
     * registered as <code>name<code>
     * @param sessionId
     * @param name mbean's object name
     * @param attr attributes to enumerate
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
    public Object getMBeanInfo(@HeaderParam("sessionid")
    String sessionId, @PathParam("name")
    ObjectName name, @QueryParam("attr")
    List<String> attrs) throws InstanceNotFoundException, IntrospectionException, ReflectionException,
            IOException, NotConnectedException {
        RMCachingProxyUserInterface rm = checkAccess(sessionId);

        if ((attrs == null) || (attrs.size() == 0)) {
            // no attribute is requested, we return
            // the description of the mbean
            return rm.getMBeanInfo(name);

        } else {
            return rm.getMBeanAttributes(name, attrs.toArray(new String[] {}));
        }
    }

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
     * @param sessionId
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
    public String getStatHistory(@HeaderParam("sessionid")
    String sessionId, @QueryParam("range")
    String range) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
            MalformedObjectNameException, NullPointerException, InterruptedException, NotConnectedException {

        RMCachingProxyUserInterface rm = checkAccess(sessionId);

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

        ObjectName on = new ObjectName(RMJMXHelper.RUNTIMEDATA_MBEAN_NAME);
        AttributeList attrs = rm.getMBeanAttributes(on, new String[] { "StatisticHistory" });
        Attribute attr = (Attribute) attrs.get(0);
        // content of the RRD4J database backing file
        byte[] rrd4j = (byte[]) attr.getValue();

        File rrd4jDb = File.createTempFile("database", "rr4dj");
        rrd4jDb.deleteOnExit();

        OutputStream out = new FileOutputStream(rrd4jDb);
        out.write(rrd4j);
        out.close();

        // create RRD4J DB, should be identical to the one held by the RM
        RrdDb db = new RrdDb(rrd4jDb.getAbsolutePath(), true);

        long timeEnd = db.getLastUpdateTime();
        // formatting will greatly reduce response size
        DecimalFormat formatter = new DecimalFormat("###.###");

        // construct the JSON response directly in a String
        StringBuffer result = new StringBuffer();
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
            result.append("\"" + dataSource + "\":[");

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

    /**
     * Returns the version of the rest api
     * @return returns the version of the rest api
     */
    @GET
    @Path("version")
    public String getVersion() {
        return "{ " + "\"rm\" : \"" + RMRest.class.getPackage().getSpecificationVersion() + "\", " +
            "\"rest\" : \"" + RMRest.class.getPackage().getImplementationVersion() + "\"" + "}";
    }

}
