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
package org.ow2.proactive.resourcemanager.common.util;

import java.io.IOException;
import java.security.KeyException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.security.auth.login.LoginException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.JMXClientHelper;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;


/**
 * This class implements an active object managing a connection 
 * to the Resource Manager (a proxy to the Resource Manager)
 * You must init the proxy by calling the {@link #init(String, String, String)} method 
 * after having created it
 */

@ActiveObject
public class RMProxyUserInterface implements ResourceManager {

    protected ResourceManager target;
    protected JMXClientHelper jmxClient;

    public boolean init(String url, CredData credData) throws RMException, KeyException, LoginException {

        RMAuthentication rmAuth = RMConnection.join(url);
        Credentials cred = Credentials.createCredentials(credData, rmAuth.getPublicKey());

        return init(url, cred);
    }

    /**
     * Initialize the connection to the resource manager
     * @param url the url to the resource manager
     * @param credentials the credentials used to log in 
     * @return true if everything went fine
     * @throws RMException 
     * @throws KeyException
     * @throws LoginException
     */
    public boolean init(String url, Credentials credentials) throws RMException, KeyException, LoginException {

        RMAuthentication rmAuth = RMConnection.join(url);
        this.target = rmAuth.login(credentials);

        // here we log on using an empty login field to ensure that
        // credentials are used.

        this.jmxClient = new JMXClientHelper(rmAuth, new Object[] { "", credentials });
        this.jmxClient.connect();
        return true;
    }

    // Resource Manager delegation methods //

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#addNode(java.lang.String)
     */
    public BooleanWrapper addNode(String arg0) {
        return target.addNode(arg0);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#addNode(java.lang.String, java.lang.String)
     */
    public BooleanWrapper addNode(String arg0, String arg1) {
        return target.addNode(arg0, arg1);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#createNodeSource(java.lang.String, java.lang.String, java.lang.Object[], java.lang.String, java.lang.Object[])
     */
    public BooleanWrapper createNodeSource(String arg0, String arg1, Object[] arg2, String arg3, Object[] arg4) {
        return target.createNodeSource(arg0, arg1, arg2, arg3, arg4);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#disconnect()
     */
    public BooleanWrapper disconnect() {
        BooleanWrapper r = target.disconnect();
        PAActiveObject.terminateActiveObject(true);
        return r;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getAtMostNodes(int, org.ow2.proactive.scripting.SelectionScript)
     */
    public NodeSet getAtMostNodes(int arg0, SelectionScript arg1) {
        return target.getAtMostNodes(arg0, arg1);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getAtMostNodes(int, org.ow2.proactive.scripting.SelectionScript, org.ow2.proactive.utils.NodeSet)
     */
    public NodeSet getAtMostNodes(int arg0, SelectionScript arg1, NodeSet arg2) {
        return target.getAtMostNodes(arg0, arg1, arg2);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getAtMostNodes(int, java.util.List, org.ow2.proactive.utils.NodeSet)
     */
    public NodeSet getAtMostNodes(int arg0, List<SelectionScript> arg1, NodeSet arg2) {
        return target.getAtMostNodes(arg0, arg1, arg2);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getMonitoring()
     */
    public RMMonitoring getMonitoring() {
        return target.getMonitoring();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getNodeSourcePingFrequency(java.lang.String)
     */
    public IntWrapper getNodeSourcePingFrequency(String arg0) {
        return target.getNodeSourcePingFrequency(arg0);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getState()
     */
    public RMState getState() {
        return target.getState();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getSupportedNodeSourceInfrastructures()
     */
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures() {
        return target.getSupportedNodeSourceInfrastructures();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getSupportedNodeSourcePolicies()
     */
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies() {
        return target.getSupportedNodeSourcePolicies();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#isActive()
     */
    public BooleanWrapper isActive() {
        return target.isActive();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#nodeIsAvailable(java.lang.String)
     */
    public BooleanWrapper nodeIsAvailable(String arg0) {
        return target.nodeIsAvailable(arg0);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#releaseNode(org.objectweb.proactive.core.node.Node)
     */
    public BooleanWrapper releaseNode(Node arg0) {
        return target.releaseNode(arg0);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#releaseNodes(org.ow2.proactive.utils.NodeSet)
     */
    public BooleanWrapper releaseNodes(NodeSet arg0) {
        return target.releaseNodes(arg0);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#removeNode(java.lang.String, boolean)
     */
    public BooleanWrapper removeNode(String arg0, boolean arg1) {
        return target.removeNode(arg0, arg1);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#removeNodeSource(java.lang.String, boolean)
     */
    public BooleanWrapper removeNodeSource(String arg0, boolean arg1) {
        return target.removeNodeSource(arg0, arg1);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#setNodeSourcePingFrequency(int, java.lang.String)
     */
    public BooleanWrapper setNodeSourcePingFrequency(int arg0, String arg1) {
        return target.setNodeSourcePingFrequency(arg0, arg1);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#shutdown(boolean)
     */
    public BooleanWrapper shutdown(boolean arg0) {
        return target.shutdown(arg0);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getAtMostNodes(int, org.ow2.proactive.topology.descriptor.TopologyDescriptor, java.util.List, org.ow2.proactive.utils.NodeSet)
     */
    public NodeSet getAtMostNodes(int number, TopologyDescriptor descriptor,
            List<SelectionScript> selectionScriptsList, NodeSet exclusion) {
        return target.getAtMostNodes(number, descriptor, selectionScriptsList, exclusion);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getTopology()
     */
    public Topology getTopology() {
        return target.getTopology();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#lockNodes(java.util.Set)
     */
    public BooleanWrapper lockNodes(Set<String> urls) {
        return this.target.lockNodes(urls);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#unlockNodes(java.util.Set)
     */
    public BooleanWrapper unlockNodes(Set<String> urls) {
        return this.target.unlockNodes(urls);
    }

    // accounting features    

    /** MBeanInfo object of the mbean represented by its objectname <code>name</code> 
     * @param name  the objectname of the mbean
     * @return the MBeanInfo of the mbean
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @throws IOException
     */
    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, IOException {
        return this.jmxClient.getConnector().getMBeanServerConnection().getMBeanInfo(name);
    }

    /**
     * returns a list of attributes whose names come from  <code>attributes</code> of the
     * MBean with the name <code>name</code> 
     * @param name the object name of the mbean
     * @param attributes the names of the request attributes
     * @return an AttributeList with all the requested attributes 
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @throws IOException 
     */
    public AttributeList getMBeanAttributes(ObjectName name, String[] attributes)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
        return this.jmxClient.getConnector().getMBeanServerConnection().getAttributes(name, attributes);
    }

    public BooleanWrapper isNodeAdmin(String nodeUrl) {
        return this.target.isNodeAdmin(nodeUrl);
    }

    public BooleanWrapper isNodeUser(String nodeUrl) {
        return this.target.isNodeUser(nodeUrl);
    }

    @Override
    public <T> List<ScriptResult<T>> executeScript(Script<T> script, String targetType, Set<String> targets) {
        return this.target.executeScript(script, targetType, targets);
    }

    @Override
    public NodeSet getNodes(int number, TopologyDescriptor descriptor,
            List<SelectionScript> selectionScriptsList, NodeSet exclusion, boolean bestEffort) {
        return this.getNodes(number, descriptor, selectionScriptsList, exclusion, bestEffort);
    }

    @Override
    public NodeSet getNodes(Criteria criteria) {
        return target.getNodes(criteria);
    }

    @Override
    public List<ScriptResult<Object>> executeScript(String script, String scriptEngine, String targetType,
            Set<String> targets) {
        return this.target.executeScript(script, scriptEngine, targetType, targets);
    }
}
