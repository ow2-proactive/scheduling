package unitTests.gcmdeployment.virtualnode;

import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptorInternal;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.NodeProvider;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;
import org.objectweb.proactive.extra.gcmdeployment.core.Topology;


public class GCMApplicationDescriptorMockup implements GCMApplicationDescriptorInternal {
    public long deploymentId;

    public GCMApplicationDescriptorMockup() {
        deploymentId = ProActiveRandom.nextInt();
    }

    public Set<Node> getCurrentNodes() {
        throw new RuntimeException("Not implemented");
    }

    public Topology getCurrentTopology() {
        throw new RuntimeException("Not implemented");
    }

    public Set<FakeNode> getCurrentUnusedNodes() {
        throw new RuntimeException("Not implemented");
    }

    public long getDeploymentId() {
        return deploymentId;
    }

    public VariableContract getVariableContract() {
        throw new RuntimeException("Not implemented");
    }

    public GCMVirtualNode getVirtualNode(String vnName) {
        throw new RuntimeException("Not implemented");
    }

    public Map<String, ? extends GCMVirtualNode> getVirtualNodes() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isStarted() {
        throw new RuntimeException("Not implemented");
    }

    public void kill() {
        throw new RuntimeException("Not implemented");
    }

    public void startDeployment() {
        throw new RuntimeException("Not implemented");
    }

    public void updateTopology(Topology topology) {
        throw new RuntimeException("Not implemented");
    }

    public void addNode(Node node) {
        throw new RuntimeException("Not implemented");
    }

    public NodeProvider getNodeProviderFromTopologyId(Long topologyId) {
        throw new RuntimeException("Not implemented");
    }
}
