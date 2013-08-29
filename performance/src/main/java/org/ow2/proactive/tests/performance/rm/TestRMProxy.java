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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.rm;

import java.security.KeyException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.management.JMException;
import javax.security.auth.login.LoginException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
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


public class TestRMProxy implements ResourceManager {

    protected ResourceManager target;

    private String jmxROUrl;

    public void init(String url, CredData credData) throws RMException, KeyException, LoginException,
            JMException {
        RMAuthentication rmAuth = RMConnection.join(url);
        Credentials cred = Credentials.createCredentials(credData, rmAuth.getPublicKey());

        this.target = rmAuth.login(cred);
        this.jmxROUrl = rmAuth.getJMXConnectorURL(JMXTransportProtocol.RO);
    }

    public String getJmxROUrl() {
        return jmxROUrl;
    }

    public RMInitialState syncAddEventListener(RMEventListener listener, RMEventType... events) {
        RMInitialState state = getMonitoring().addRMEventListener(listener, events);
        PAFuture.waitFor(state);
        // to check addRMEventListener really finished successfully
        state.getNodesEvents();
        return state;
    }

    public void removeEventListener() throws RMException {
        getMonitoring().removeRMEventListener();
    }

    public BooleanWrapper addNode(String arg0) {
        return target.addNode(arg0);
    }

    public BooleanWrapper addNode(String arg0, String arg1) {
        return target.addNode(arg0, arg1);
    }

    public BooleanWrapper createNodeSource(String arg0, String arg1, Object[] arg2, String arg3, Object[] arg4) {
        return target.createNodeSource(arg0, arg1, arg2, arg3, arg4);
    }

    public BooleanWrapper disconnect() {
        BooleanWrapper r = target.disconnect();
        PAActiveObject.terminateActiveObject(true);
        return r;
    }

    public NodeSet getAtMostNodes(int arg0, SelectionScript arg1) {
        return target.getAtMostNodes(arg0, arg1);
    }

    public NodeSet getAtMostNodes(int arg0, SelectionScript arg1, NodeSet arg2) {
        return target.getAtMostNodes(arg0, arg1, arg2);
    }

    public NodeSet getAtMostNodes(int arg0, List<SelectionScript> arg1, NodeSet arg2) {
        return target.getAtMostNodes(arg0, arg1, arg2);
    }

    public RMMonitoring getMonitoring() {
        return target.getMonitoring();
    }

    public IntWrapper getNodeSourcePingFrequency(String arg0) {
        return target.getNodeSourcePingFrequency(arg0);
    }

    public RMState getState() {
        return target.getState();
    }

    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures() {
        return target.getSupportedNodeSourceInfrastructures();
    }

    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies() {
        return target.getSupportedNodeSourcePolicies();
    }

    public BooleanWrapper isActive() {
        return target.isActive();
    }

    public BooleanWrapper nodeIsAvailable(String arg0) {
        return target.nodeIsAvailable(arg0);
    }

    public BooleanWrapper releaseNode(Node arg0) {
        return target.releaseNode(arg0);
    }

    public BooleanWrapper releaseNodes(NodeSet arg0) {
        return target.releaseNodes(arg0);
    }

    public BooleanWrapper removeNode(String arg0, boolean arg1) {
        return target.removeNode(arg0, arg1);
    }

    public BooleanWrapper removeNodeSource(String arg0, boolean arg1) {
        return target.removeNodeSource(arg0, arg1);
    }

    public BooleanWrapper setNodeSourcePingFrequency(int arg0, String arg1) {
        return target.setNodeSourcePingFrequency(arg0, arg1);
    }

    public BooleanWrapper shutdown(boolean arg0) {
        return target.shutdown(arg0);
    }

    public NodeSet getAtMostNodes(int number, TopologyDescriptor descriptor,
            List<SelectionScript> selectionScriptsList, NodeSet exclusion) {
        return target.getAtMostNodes(number, descriptor, selectionScriptsList, exclusion);
    }

    public Topology getTopology() {
        return target.getTopology();
    }

    public BooleanWrapper lockNodes(Set<String> urls) {
        return this.target.lockNodes(urls);
    }

    public BooleanWrapper unlockNodes(Set<String> urls) {
        return this.target.unlockNodes(urls);
    }

    public BooleanWrapper isNodeAdmin(String nodeUrl) {
        return target.isNodeAdmin(nodeUrl);
    }

    public BooleanWrapper isNodeUser(String nodeUrl) {
        return target.isNodeUser(nodeUrl);
    }

    public <T> List<ScriptResult<T>> executeScript(Script<T> script, String targetType, Set<String> targets) {
        return target.executeScript(script, targetType, targets);
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
        return target.executeScript(script, scriptEngine, targetType, targets);
    }
}
