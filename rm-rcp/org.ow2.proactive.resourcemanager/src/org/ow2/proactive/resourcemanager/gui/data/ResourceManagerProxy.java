/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.resourcemanager.gui.data;

import java.util.Collection;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * ResourceManagerProxy is used as a gateway to talk to the RMcore with the same thread !
 * Needed by authentication methods
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class ResourceManagerProxy implements ResourceManager {

    private ResourceManager resourceManager = null;

    public ResourceManagerProxy() {
    }

    public void connect(RMAuthentication auth, Credentials creds) throws RMException, LoginException {
        resourceManager = auth.login(creds);
    }

    public BooleanWrapper addNode(String arg0) {
        return resourceManager.addNode(arg0);
    }

    public BooleanWrapper addNode(String arg0, String arg1) {
        return resourceManager.addNode(arg0, arg1);
    }

    public BooleanWrapper createNodeSource(String arg0, String arg1, Object[] arg2, String arg3, Object[] arg4) {
        return resourceManager.createNodeSource(arg0, arg1, arg2, arg3, arg4);
    }

    public BooleanWrapper disconnect() {
        return resourceManager.disconnect();
    }

    public NodeSet getAtMostNodes(int arg0, SelectionScript arg1) {
        return resourceManager.getAtMostNodes(arg0, arg1);
    }

    public NodeSet getAtMostNodes(int arg0, SelectionScript arg1, NodeSet arg2) {
        return resourceManager.getAtMostNodes(arg0, arg1, arg2);
    }

    public NodeSet getAtMostNodes(int arg0, List<SelectionScript> arg1, NodeSet arg2) {
        return resourceManager.getAtMostNodes(arg0, arg1, arg2);
    }

    public RMMonitoring getMonitoring() {
        return resourceManager.getMonitoring();
    }

    public IntWrapper getNodeSourcePingFrequency(String arg0) {
        return resourceManager.getNodeSourcePingFrequency(arg0);
    }

    public RMState getState() {
        return resourceManager.getState();
    }

    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures() {
        return resourceManager.getSupportedNodeSourceInfrastructures();
    }

    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies() {
        return resourceManager.getSupportedNodeSourcePolicies();
    }

    public BooleanWrapper isActive() {
        return resourceManager.isActive();
    }

    public BooleanWrapper nodeIsAvailable(String arg0) {
        return resourceManager.nodeIsAvailable(arg0);
    }

    public BooleanWrapper releaseNode(Node arg0) {
        return resourceManager.releaseNode(arg0);
    }

    public BooleanWrapper releaseNodes(NodeSet arg0) {
        return resourceManager.releaseNodes(arg0);
    }

    public BooleanWrapper removeNode(String arg0, boolean arg1) {
        return resourceManager.removeNode(arg0, arg1);
    }

    public BooleanWrapper removeNodeSource(String arg0, boolean arg1) {
        return resourceManager.removeNodeSource(arg0, arg1);
    }

    public BooleanWrapper setNodeSourcePingFrequency(int arg0, String arg1) {
        return resourceManager.setNodeSourcePingFrequency(arg0, arg1);
    }

    public BooleanWrapper shutdown(boolean arg0) {
        return resourceManager.shutdown(arg0);
    }
}
