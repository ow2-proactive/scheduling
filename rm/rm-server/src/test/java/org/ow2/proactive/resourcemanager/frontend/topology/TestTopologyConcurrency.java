package org.ow2.proactive.resourcemanager.frontend.topology;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.filetransfer.FileTransferEngine;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.server.ServerConnector;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.topology.pinging.HostsPinger;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyManager;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.rmi.AlreadyBoundException;
import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test add and remove nodes from multiple threads
 * <p/>
 * At the end, remove all previously added nodes and verify that
 * the topology manager structure is empty
 */
public class TestTopologyConcurrency {

    // number of thread used in this test
    final int nb_threads = 20;

    // multiplier for the number of nodes to create
    final int node_factor = 4;

    // size of collisions, as many operations will be done on the same node
    // more removal than add
    final int collision_size = 10;

    // total number of tasks
    final int total = nb_threads * node_factor * collision_size;

    String baseUrl = "pnp://localhost:1234/LocalNode-";

    ExecutorService s = Executors.newFixedThreadPool(nb_threads);

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
                    int index = j - (j % collision_size);
                    if ((j % (collision_size / 2)) == 0) {
                        manager.addNode(new NodeImpl(runtime, baseUrl + index));
                    } else {
                        // more remove than add, to try to reproduce empty list problems
                        manager.removeNode(new NodeImpl(runtime, baseUrl + index));
                    }
                    return true;
                }
            });
        }
        List<Future<Boolean>> futures = s.invokeAll(calls);

        for (Future<Boolean> fut : futures) {
            Assert.assertTrue(fut.get());
        }

        calls = new ArrayList<>(nb_threads * node_factor);
        // second set of task remove all nodes created
        for (int i = 0; i < total; i += collision_size) {
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
