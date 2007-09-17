package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.PathElement;


public class GroupLSF extends AbstractGroup {
    private String interactive;
    private String jobName;
    private String queueName;
    private String hostList;
    private String processorNumber;
    private String resourceRequirement;
    private PathElement scriptLocation;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    public void setProcessorNumber(String processorNumber) {
        this.processorNumber = processorNumber;
    }

    public void setResourceRequirement(String resourceRequirement) {
        this.resourceRequirement = resourceRequirement;
    }

    public void setScriptLocation(PathElement path) {
        this.scriptLocation = path;
    }
}
