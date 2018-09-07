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
package org.ow2.proactive.resourcemanager.common.util;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.NodeSourceConfiguration;
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
 * You must init the proxy by calling the {@link #init(String, Credentials)} method
 * after having created it
 */
@ActiveObject
public class RMProxyUserInterface extends RMListenerProxy implements ResourceManager {

    /**
     * Enum of managed MBean attribute types
     * Convert a String MBean attribute value to Object
     */
    public enum MBeanAttributeType {
        INTEGER {
            @Override
            public Object convert(String value) {
                return Integer.valueOf(value);
            }
        },
        STRING {
            @Override
            public Object convert(String value) {
                return value;
            }
        };
        public abstract Object convert(String value);
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

    @Override
    public List<RMNodeSourceEvent> getExistingNodeSourcesList() {
        return target.getExistingNodeSourcesList();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#defineNodeSource(String, String, Object[], String, Object[], boolean)
     */
    @Override
    public BooleanWrapper defineNodeSource(String arg0, String arg1, Object[] arg2, String arg3, Object[] arg4,
            boolean arg5) {
        return target.defineNodeSource(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#editNodeSource(String, String, Object[], String, Object[], boolean)
     */
    @Override
    public BooleanWrapper editNodeSource(String arg0, String arg1, Object[] arg2, String arg3, Object[] arg4,
            boolean arg5) {
        return target.editNodeSource(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#updateDynamicParameters(String, String, Object[], String, Object[])
     */
    @Override
    public BooleanWrapper updateDynamicParameters(String arg0, String arg1, Object[] arg2, String arg3, Object[] arg4) {
        return target.updateDynamicParameters(arg0, arg1, arg2, arg3, arg4);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#createNodeSource(String, String, Object[], String, Object[], boolean)
     */
    public BooleanWrapper createNodeSource(String arg0, String arg1, Object[] arg2, String arg3, Object[] arg4,
            boolean arg5) {
        return target.createNodeSource(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#deployNodeSource(String)
     */
    public BooleanWrapper deployNodeSource(String arg0) {
        return target.deployNodeSource(arg0);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#undeployNodeSource(String, boolean)
     */
    public BooleanWrapper undeployNodeSource(String arg0, boolean arg1) {
        return target.undeployNodeSource(arg0, arg1);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getAtMostNodes(int, org.ow2.proactive.scripting.SelectionScript)
     */
    @SuppressWarnings("deprecation")
    public NodeSet getAtMostNodes(int arg0, SelectionScript arg1) {
        return target.getAtMostNodes(arg0, arg1);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getAtMostNodes(int, org.ow2.proactive.scripting.SelectionScript, org.ow2.proactive.utils.NodeSet)
     */
    @SuppressWarnings("deprecation")
    public NodeSet getAtMostNodes(int arg0, SelectionScript arg1, NodeSet arg2) {
        return target.getAtMostNodes(arg0, arg1, arg2);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getAtMostNodes(int, java.util.List, org.ow2.proactive.utils.NodeSet)
     */
    @SuppressWarnings("deprecation")
    public NodeSet getAtMostNodes(int arg0, List<SelectionScript> arg1, NodeSet arg2) {
        return target.getAtMostNodes(arg0, arg1, arg2);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getMonitoring()
     */
    public RMMonitoring getMonitoring() {
        return target.getMonitoring();
    }

    @Override
    public Set<String> listAliveNodeUrls() {
        return target.listAliveNodeUrls();
    }

    @Override
    public Set<String> listAliveNodeUrls(Set<String> nodeSourceNames) {
        return target.listAliveNodeUrls(nodeSourceNames);
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
     * @see org.ow2.proactive.resourcemanager.frontend.ResourceManager#getNodeSourceConfiguration(String)
     */
    public NodeSourceConfiguration getNodeSourceConfiguration(String nodeSourceName) {
        return target.getNodeSourceConfiguration(nodeSourceName);
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
     * {@inheritDoc}
     */
    @ImmediateService
    @Override
    public Set<String> setNodesAvailable(Set<String> nodeUrls) {
        return target.setNodesAvailable(nodeUrls);
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
    @SuppressWarnings("deprecation")
    public NodeSet getAtMostNodes(int number, TopologyDescriptor descriptor, List<SelectionScript> selectionScriptsList,
            NodeSet exclusion) {
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
    public MBeanInfo getMBeanInfo(ObjectName name)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
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

    /**
     * Set a single JMX attribute of the MBean <code>objectName</code>.
     * Only integer and string attributes are currently supported, see <code>type</code>.
     *
     * @param objectName    the object name of the MBean
     * @param type          the type of the attribute to set ('integer' and 'string' are currently supported, see <code>RMProxyUserInterface</code>)
     * @param name          the name of the attribute to set
     * @param value         the new value of the attribute (defined as a String, it is automatically converted according to <code>type</code>)
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @throws IOException
     * @throws MBeanException
     * @throws InvalidAttributeValueException
     * @throws AttributeNotFoundException
     * @throws IllegalArgumentException
     */
    public void setMBeanAttribute(ObjectName objectName, String type, String name, String value)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException, MBeanException,
            InvalidAttributeValueException, AttributeNotFoundException, IllegalArgumentException {
        Object attributeValue = MBeanAttributeType.valueOf(type.toUpperCase()).convert(value);
        this.jmxClient.getConnector().getMBeanServerConnection().setAttribute(objectName,
                                                                              new Attribute(name, attributeValue));
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
    @Deprecated
    public NodeSet getNodes(int number, TopologyDescriptor descriptor, List<SelectionScript> selectionScriptsList,
            NodeSet exclusion, boolean bestEffort) {
        return this.target.getNodes(number, descriptor, selectionScriptsList, exclusion, bestEffort);
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

    @Override
    public StringWrapper getCurrentUser() {
        return this.target.getCurrentUser();
    }

    @Override
    public UserData getCurrentUserData() {
        return this.target.getCurrentUserData();
    }

    @Override
    public void releaseBusyNodesNotInList(List<NodeSet> verifiedBusyNodes) {
        target.releaseBusyNodesNotInList(verifiedBusyNodes);
    }

    @Override
    public boolean areNodesKnown(NodeSet nodes) {
        return target.areNodesKnown(nodes);
    }

    @Override
    public boolean areNodesRecoverable(NodeSet nodes) {
        return target.areNodesRecoverable(nodes);
    }

    @Override
    public StringWrapper getNodeThreadDump(String nodeUrl) {
        return target.getNodeThreadDump(nodeUrl);
    }

}
