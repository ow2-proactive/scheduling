package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.List;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
public interface Topology {
    public List<String> getDeploymentPath();

    public Set<TopologyRuntime> getRuntimes();

    public List<TopologyImpl> getChildren();

    public boolean hasChildren();

    public String getApplicationDescriptorPath();

    public String getDeploymentDescriptorPath();

    public String getNodeProvider();
}
