/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.selection.policies;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.security.Permission;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * 
 * Test for NodeSourcePriorityPolicy
 *
 */
public class TestNodeSourcePolicy {

    class Node implements RMNode {

        String name;
        String nsName;

        Node(String name, String nsName) {
            this.name = name;
            this.nsName = nsName;
        }

        @Override
        public int compareTo(RMNode o) {
            return name.compareTo(o.getNodeName());
        }

        public ScriptResult<Boolean> executeScript(SelectionScript script) {
            return null;
        }

        @Override
        public HashMap<SelectionScript, Integer> getScriptStatus() {
            return null;
        }

        @Override
        public void clean() throws NodeException {
        }

        @Override
        public String getNodeInfo() {
            return null;
        }

        @Override
        public String getNodeName() {
            return name;
        }

        @Override
        public org.objectweb.proactive.core.node.Node getNode() {
            return null;
        }

        @Override
        public String getVNodeName() {
            return null;
        }

        @Override
        public String getHostName() {
            return null;
        }

        @Override
        public String getDescriptorVMName() {
            return null;
        }

        @Override
        public String getNodeSourceName() {
            return nsName;
        }

        @Override
        public String getNodeURL() {
            return null;
        }

        @Override
        public NodeSource getNodeSource() {
            return null;
        }

        @Override
        public NodeState getState() {
            return null;
        }

        @Override
        public long getStateChangeTime() {
            return 0;
        }

        @Override
        public Client getProvider() {
            return null;
        }

        @Override
        public Client getOwner() {
            return null;
        }

        @Override
        public Permission getUserPermission() {
            return null;
        }

        @Override
        public Permission getAdminPermission() {
            return null;
        }

        @Override
        public RMNodeEvent getAddEvent() {
            return null;
        }

        @Override
        public RMNodeEvent getLastEvent() {
            return null;
        }

        @Override
        public boolean isFree() {
            return false;
        }

        @Override
        public boolean isDown() {
            return false;
        }

        @Override
        public boolean isToRemove() {
            return false;
        }

        @Override
        public boolean isBusy() {
            return false;
        }

        @Override
        public boolean isConfiguring() {
            return false;
        }

        @Override
        public boolean isLocked() {
            return false;
        }

        @Override
        public long getLockTime() {
            return -1;
        }

        @Override
        public Client getLockedBy() {
            return null;
        }

        @Override
        public void setFree() {
        }

        @Override
        public void setBusy(Client owner) {
        }

        @Override
        public void setToRemove() {
        }

        @Override
        public void setDown() {
        }

        @Override
        public void setConfiguring(Client owner) {
        }

        @Override
        public void lock(Client owner) {
        }

        @Override
        public void unlock(Client owner) {

        }

        @Override
        public void setNodeSource(NodeSource nodeSource) {
        }

        @Override
        public void setAddEvent(RMNodeEvent addEvent) {
        }

        @Override
        public void setLastEvent(RMNodeEvent lastEvent) {
        }

        @Override
        public void setJMXUrl(JMXTransportProtocol protocol, String address) {
        }

        @Override
        public String getJMXUrl(JMXTransportProtocol protocol) {
            return null;
        }

        @Override
        public <T> ScriptResult<T> executeScript(Script<T> script, Map<String, Serializable> bindings) {
            return null;
        }

        @Override
        public boolean isProtectedByToken() {
            return false;
        }

        @Override
        public RMNodeEvent createNodeEvent(RMEventType eventType, NodeState previousNodeState,
                String initiator) {
            return null;
        }

        @Override
        public RMNodeEvent createNodeEvent() {
            return null;
        }

        @Override
        public boolean isDeploying() {
            return false;
        }

    }

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void action() throws Exception {

        /**
         * Creating a config with ns names 0, 1, 2, 3, ... 9
         */
        File config = tmpFolder.newFile("policies");
        BufferedWriter out = new BufferedWriter(new FileWriter(config));
        for (int i = 0; i < 10; i++) {
            out.write(i + "\n");
        }
        out.close();

        System.setProperty(NodeSourcePriorityPolicy.CONFIG_NAME_PROPERTY, config.getAbsolutePath());
        NodeSourcePriorityPolicy policy = new NodeSourcePriorityPolicy();

        // checking the arrangement of all nodes
        List<RMNode> nodes = createNodes();
        List<RMNode> res = policy.arrangeNodes(1000, nodes, null);

        assertEquals("Incorrect result size", 400, res.size());
        Iterator<RMNode> iterator = res.iterator();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 20; j++) {
                RMNode node = iterator.next();
                assertEquals("Incorrect arrangenemt (" + i + ", " + j + ")", String.valueOf(i),
                        node.getNodeName());
            }
        }

    }

    private List<RMNode> createNodes() {
        /**
         * Creating an artificial list of nodes
         */
        List<RMNode> nodes = new LinkedList<>();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                nodes.add(new Node(String.valueOf(j), String.valueOf(j)));
            }
        }
        return nodes;
    }

}
