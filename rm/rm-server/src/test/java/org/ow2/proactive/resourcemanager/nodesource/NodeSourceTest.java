package org.ow2.proactive.resourcemanager.nodesource;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.frontend.topology.pinging.HostsPinger;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyManager;
import org.ow2.proactive.utils.Subjects;


/**
 * @author ActiveEon Team
 * @since 06/03/17
 */
public class NodeSourceTest {

    @Test
    public void testDetectedPingedDownNodeCallingInfrastructureManagerInternalRemoveNodeTrueFlag()
            throws RMException, ClassNotFoundException {

        InfrastructureManager infrastructureManager = mock(InfrastructureManager.class);

        NodeSourcePolicy nodeSourcePolicy = mock(NodeSourcePolicy.class);

        when(nodeSourcePolicy.getProviderAccessType()).thenReturn(AccessType.ALL);

        NodeSource nodeSource = new NodeSource("registrationURL",
                                               "name",
                                               new Client(Subjects.create("admin"), false),
                                               infrastructureManager,
                                               nodeSourcePolicy,
                                               mock(RMCore.class),
                                               mock(RMMonitoringImpl.class));

        RMCore.topologyManager = new TopologyManager(HostsPinger.class);

        Node node = mock(Node.class);
        NodeInformation nodeInformation = mock(NodeInformation.class);
        when(node.getNodeInformation()).thenReturn(nodeInformation);
        when(nodeInformation.getURL()).thenReturn("protocol://authoritypart/");

        nodeSource.internalAddNode(node);

        nodeSource.detectedPingedDownNode(node.getNodeInformation().getURL());

        verify(infrastructureManager).internalRemoveNode(any(Node.class), eq(true));
    }

}
