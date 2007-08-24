package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;


public class GroupPrun extends AbstractGroup {
    private String queueName;
    private String hostList;
    private String bookingDuration;
    private String hosts;
    private String processorPerNode;
    private String outputFile;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the name of the queue where the job was launched
     * @return String
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Sets the value of the queue where the job will be launched. The default is 'normal'
     * @param queueName
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * Sets the value of the hostList parameter with the given value
     * @param hostList
     */
    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    /**
     * Returns the hostList value of this process.
     * @return String
     */
    public String getHostList() {
        return hostList;
    }

    /**
     *  Set the booking duration of the cluster's nodes. The default is 00:01:00
     * @param d duration
     */
    public void setBookingDuration(String d) {
        this.bookingDuration = d;
    }

    /**
     *  Return the booking duration of the cluster's nodes.
     * @return String
     */
    public String getBookingDuration() {
        return this.bookingDuration;
    }

    /**
     * Sets the number of nodes requested when running the job
     * @param hosts
     */
    public void setHostsNumber(String hosts) {
        this.hosts = hosts;
    }

    /**
     * Returns the number of nodes requested for the job
     * @return String
     */
    public String getHostsNumber() {
        return this.hosts;
    }

    /**
     * Sets the number of nodes requested when running the job
     * @param processorPerNode processor per node
     */
    public void setProcessorPerNodeNumber(String processorPerNode) {
        this.processorPerNode = processorPerNode;
    }

    public String getProcessorPerNodeNumber() {
        return this.processorPerNode;
    }

    /**
     * @return the filename given to prun using -o
     */
    public String getOutputFile() {
        return outputFile;
    }

    /** Set the output file to be passed to prun
     * using the -o option
     * @param string
     */
    public void setOutputFile(String string) {
        this.outputFile = string;
    }
}
