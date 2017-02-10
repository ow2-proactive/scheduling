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
package org.ow2.proactive.resourcemanager.frontend.topology;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.topology.pinging.HostsPinger;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyManager;


/**
 * Test add and remove nodes from multiple threads
 * <p/>
 * At the end, remove all previously added nodes and verify that
 * the topology manager structure is empty
 */
public class topologyConcurrencyTest {

    // number of thread used in this test
    final int nbThreads = 20;

    // multiplier for the number of nodes to create
    final int nodeFactor = 4;

    // size of collisions, as many operations will be done on the same node
    // more removal than add
    final int collisionSize = 10;

    // total number of tasks
    final int total = nbThreads * nodeFactor * collisionSize;

    String baseUrl = "pnp://localhost:1234/LocalNode-";

    ExecutorService s = Executors.newFixedThreadPool(nbThreads);

    @Test
    public void action() throws Exception {

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getLogger(TopologyManager.class).setLevel(Level.DEBUG);
        PAResourceManagerProperties.RM_TOPOLOGY_PINGER.updateProperty(HostsPinger.class.getName());
        PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.updateProperty("true");
        PAResourceManagerProperties.RM_TOPOLOGY_DISTANCE_ENABLED.updateProperty("false");

        final ProActiveRuntimeImpl runtime = mock(ProActiveRuntimeImpl.class);
        when(runtime.getVMInformation()).thenReturn(new DummyVMInfo());
        final TopologyManager manager = new TopologyManager();
        List<Callable<Boolean>> calls = new ArrayList<>(total);

        // first set of tasks, add nodes and remove them
        for (int i = 0; i < total; i++) {
            final int j = i;
            calls.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    int index = j - (j % collisionSize);
                    NodeImpl node = new NodeImpl(runtime, baseUrl + index);
                    if ((j % (collisionSize / 2)) == 0) {
                        manager.addNode(node);
                    } else {
                        // more remove than add, to try to reproduce empty list problems
                        manager.removeNode(node);
                    }
                    return true;
                }
            });
        }
        List<Future<Boolean>> futures = s.invokeAll(calls);

        for (Future<Boolean> fut : futures) {
            Assert.assertTrue(fut.get());
        }

        calls = new ArrayList<>(nbThreads * nodeFactor);
        // second set of task remove all nodes created
        for (int i = 0; i < total; i += collisionSize) {
            final int j = i;
            calls.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    int index = j;
                    manager.removeNode(new NodeImpl(runtime, baseUrl + index));
                    return true;
                }
            });
        }

        futures = s.invokeAll(calls);

        for (Future<Boolean> fut : futures) {
            Assert.assertTrue(fut.get());
        }
        System.out.println(manager.getTopology().getHosts());
        // finally verify that the nodes on host structure is empty (null)
        Assert.assertNull(manager.getNodesOnHost(DummyVMInfo.address));
    }

    public static class DummyVMInfo implements VMInformation {

        static VMID vmId = UniqueID.getCurrentVMID();

        static java.net.InetAddress address = ProActiveInet.getInstance().getInetAddress();

        static String hostname = ProActiveInet.getInstance().getHostname();

        @Override
        public VMID getVMID() {
            return vmId;
        }

        @Override
        public InetAddress getInetAddress() {
            return address;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getHostName() {
            return hostname;
        }

        @Override
        public String getDescriptorVMName() {
            return null;
        }

        @Override
        public long getCapacity() {
            return 0;
        }

        @Override
        public long getDeploymentId() {
            return 0;
        }

        @Override
        public long getTopologyId() {
            return 0;
        }
    }
}
