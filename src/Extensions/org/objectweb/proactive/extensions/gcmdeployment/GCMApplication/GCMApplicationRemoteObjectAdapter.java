package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication;

import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMA_LOGGER;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.gcmdeployment.Topology;


public class GCMApplicationRemoteObjectAdapter extends Adapter<GCMApplication> implements GCMApplication {
    long deploymentId;
    Set<String> virtualNodeNames;

    @Override
    protected void construct() {
        deploymentId = target.getDeploymentId();
        virtualNodeNames = target.getVirtualNodeNames();
    }

    public VariableContract getVariableContract() {
        return target.getVariableContract();
    }

    public GCMVirtualNode getVirtualNode(String vnName) {
        GCMVirtualNode vn = null;
        long deploymentId = target.getDeploymentId();
        String name = deploymentId + "/VirtualNode/" + vnName;
        URI uri = URIBuilder.buildURI("localhost", name);
        try {

            RemoteObject ro = RemoteObjectHelper.lookup(uri);
            vn = (GCMVirtualNode) RemoteObjectHelper.generatedObjectStub(ro);
        } catch (ProActiveException e) {
            GCMA_LOGGER.error("Virtual Node \"" + vnName + "\" is not exported as " + uri);
        }

        return vn;

    }

    public Map<String, GCMVirtualNode> getVirtualNodes() {
        Map<String, GCMVirtualNode> map = new HashMap<String, GCMVirtualNode>();

        for (String vnName : virtualNodeNames) {
            map.put(vnName, this.getVirtualNode(vnName));
        }

        return map;
    }

    public boolean isStarted() {
        return target.isStarted();
    }

    public void kill() {
        target.kill();
    }

    public void startDeployment() {
        target.startDeployment();
    }

    public void updateTopology(Topology topology) {
        target.updateTopology(topology);
    }

    public void waitReady() {
        target.waitReady();
    }

    public long getDeploymentId() {
        return target.getDeploymentId();
    }

    public Set<String> getVirtualNodeNames() {
        return target.getVirtualNodeNames();
    }

    public URL getDescriptorURL() {
        return target.getDescriptorURL();
    }

    public List<Node> getAllNodes() {
        return target.getAllNodes();
    }

    public String getDebugInformation() {
        return target.getDebugInformation();
    }

    public Topology getTopology() {
        return target.getTopology();
    }

}
