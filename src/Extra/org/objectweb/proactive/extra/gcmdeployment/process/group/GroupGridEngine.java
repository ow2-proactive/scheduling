package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.PathElement;


public class GroupGridEngine extends AbstractGroup {
    private String queueName;
    private String hostNumber;
    private String bookingDuration;
    private PathElement scriptLocation;
    private String parallelEnvironment;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     *  Set the booking duration of the cluster's nodes. The default is 00:01:00
     * @param d duration
     */
    public void setBookingDuration(String d) {
        this.bookingDuration = d;
    }

    /**
     * Returns the number of nodes requested when running the job
     * @return the number of nodes requested when running the job
     */
    public String getHostsNumber() {
        return this.hostNumber;
    }

    /**
     * Sets the number of nodes requested when running the job
     * @param hostNumber the number of nodes requested when running the job
     */
    public void setHostsNumber(String hostNumber) {
        this.hostNumber = hostNumber;
    }

    /**
     * Sets the location of the script on the remote host
     * @param location
     */
    public void setScriptLocation(PathElement location) {
        this.scriptLocation = location;
    }

    /**
     * Sets the parallel environment for this GridEngineSubProcess
     * @param p the parallel environment to use
     */
    public void setParallelEnvironment(String p) {
        this.parallelEnvironment = p;
    }

    /**
     * Returns the parallel environment for this GridEngineSubProcess
     * @return the parallel environment for this GridEngineSubProcess
     */
    public String getParallelEnvironment() {
        return this.parallelEnvironment;
    }
}
