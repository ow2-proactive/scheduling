package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.process.Bridge;
import org.objectweb.proactive.extra.gcmdeployment.process.Group;
import org.objectweb.proactive.extra.gcmdeployment.process.HostInfo;


public class GCMDeploymentResources {
    private List<Group> groups = Collections.synchronizedList(new ArrayList<Group>());
    private List<Bridge> bridges = Collections.synchronizedList(new ArrayList<Bridge>());
    private HostInfo hostInfo;

    public List<Group> getGroups() {
        return groups;
    }

    public List<Bridge> getBridges() {
        return bridges;
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public void addBridge(Bridge bridge) {
        bridges.add(bridge);
    }

    public HostInfo getHostInfo() {
        return hostInfo;
    }

    protected void setHostInfo(HostInfo hostInfo) {
        assert (this.hostInfo == null);
        this.hostInfo = hostInfo;
    }

    public void check() throws IllegalStateException {
        for (Group group : groups)
            group.check();

        for (Bridge bridge : bridges)
            bridge.check();

        hostInfo.check();
    }
}
