package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.PathElement;


public class GroupOARGrid extends AbstractGroup {
    private String queueName;
    private String accessProtocol;
    private String wallTime;
    private String resources;
    private PathElement scriptLocation;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setAccessProtocol(String accessProtocol) {
        this.accessProtocol = accessProtocol;
    }

    public void setScriptLocation(PathElement path) {
        this.scriptLocation = path;
    }

    public void setWallTime(String wallTime) {
        this.wallTime = wallTime;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }
}
