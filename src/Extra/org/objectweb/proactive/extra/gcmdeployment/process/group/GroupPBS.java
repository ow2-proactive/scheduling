package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.PathElement;


public class GroupPBS extends AbstractGroup {
    private String hostList;
    private String hostNumber;
    private String processorPerNode;
    private String bookingDuration;
    private String outputFile;
    private PathElement scriptLocation;
    private String queueName;
    private String interactive;

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

    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    public void setHostsNumber(String nodeNumber) {
        this.hostNumber = nodeNumber;
    }

    public void setProcessorPerNodeNumber(String processorPerNode) {
        this.processorPerNode = processorPerNode;
    }

    public void setBookingDuration(String bookingDuration) {
        this.bookingDuration = bookingDuration;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public void setScriptLocation(PathElement location) {
        this.scriptLocation = location;
    }
}
