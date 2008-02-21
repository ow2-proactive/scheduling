package unitTests.gcmdeployment.virtualnode;

import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptorInternal;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.NodeProvider;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;
import org.objectweb.proactive.extra.gcmdeployment.core.Topology;


public class GCMApplicationDescriptorMockup implements GCMApplicationDescriptorInternal {
    public long deploymentId;

    public GCMApplicationDescriptorMockup() {
        deploymentId = ProActiveRandom.nextInt();
    }

    public Set<Node> getCurrentMappedNodes() {
        throw new RuntimeException("Not implemented");
    }

    public Topology getCurrentTopology() {
        throw new RuntimeException("Not implemented");
    }

    public Set<Node> getCurrentUnmappedNodes() {
        throw new RuntimeException("Not implemented");
    }

    public long getDeploymentId() {
        return deploymentId;
    }

    public VariableContractImpl getVariableContract() {
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
        // Do nothing
    }

    public NodeProvider getNodeProviderFromTopologyId(Long topologyId) {
        throw new RuntimeException("Not implemented");
    }

    public String debugUnmappedNodes() {
        throw new RuntimeException("Not implemented");
    }

    public long getNbUnmappedNodes() {
        throw new RuntimeException("Not implemented");
    }
}
