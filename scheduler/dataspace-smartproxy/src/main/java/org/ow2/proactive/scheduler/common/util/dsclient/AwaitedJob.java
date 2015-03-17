package org.ow2.proactive.scheduler.common.util.dsclient;

import java.io.Serializable;
import java.util.HashMap;


/**
 * A job which has been submitted to the scheduler and whose output data needs
 * to be transfered after termination.
 * 
 * @author esalagea
 * 
 */
public class AwaitedJob implements Serializable {

    private static final long serialVersionUID = 61L;

    private String jobId;

    // input data
    private String localInputFolder;
    private String inputSpaceURL;
    private String pushURL;
    private HashMap<String, AwaitedTask> awaitedTasks;
    private boolean isolateTaskOutputs;
    private boolean automaticTransfer;

    public boolean isIsolateTaskOutputs() {
        return isolateTaskOutputs;
    }

    public void setIsolateTaskOutputs(boolean isolateTaskOutputs) {
        this.isolateTaskOutputs = isolateTaskOutputs;
    }

    public void setPushURL(String pushURL) {
        this.pushURL = pushURL;
    }

    public void setPullURL(String pullURL) {
        this.pullURL = pullURL;
    }

    // output data
    private String localOutputFolder;
    private String outputSpaceURL;
    private String pullURL;

    public String getPushURL() {
        return pushURL;
    }

    public String getPullURL() {
        return pullURL;
    }

    public AwaitedJob() {
    }

    public AwaitedJob(String jobId, String localInputFolder, String inputSpaceURL, String push_url,
            String localOutputFolder, String outputSpaceURL, String pull_url, boolean isolatetaskOutputs,
            boolean automaticTransfer, HashMap<String, AwaitedTask> awaitedTasks) {
        super();
        this.jobId = jobId;
        this.localInputFolder = localInputFolder;
        this.inputSpaceURL = inputSpaceURL;
        this.localOutputFolder = localOutputFolder;
        this.outputSpaceURL = outputSpaceURL;
        this.pushURL = push_url;
        this.pullURL = pull_url;
        this.awaitedTasks = awaitedTasks;
        this.isolateTaskOutputs = isolatetaskOutputs;
        this.automaticTransfer = automaticTransfer;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getLocalInputFolder() {
        return localInputFolder;
    }

    public void setLocalInputFolder(String localInputFolder) {
        this.localInputFolder = localInputFolder;
    }

    public String getInputSpaceURL() {
        return inputSpaceURL;
    }

    public void setInputSpaceURL(String inputSpaceURL) {
        this.inputSpaceURL = inputSpaceURL;
    }

    public String getLocalOutputFolder() {
        return localOutputFolder;
    }

    public void setLocalOutputFolder(String localOutputFolder) {
        this.localOutputFolder = localOutputFolder;
    }

    public String getOutputSpaceURL() {
        return outputSpaceURL;
    }

    public void setOutputSpaceURL(String outputSpaceURL) {
        this.outputSpaceURL = outputSpaceURL;
    }

    public HashMap<String, AwaitedTask> getAwaitedTasks() {
        return awaitedTasks;
    }

    public AwaitedTask getAwaitedTask(String tname) {
        return awaitedTasks.get(tname);
    }

    public void removeAwaitedTask(String tname) {
        this.awaitedTasks.remove(tname);
    }

    public void setAwaitedTasks(HashMap<String, AwaitedTask> awaitedTasks) {
        this.awaitedTasks = awaitedTasks;
    }

    public void putAwaitedTask(String tname, AwaitedTask at) {
        this.awaitedTasks.put(tname, at);
    }

    public boolean isAutomaticTransfer() {
        return automaticTransfer;
    }

    public void setAutomaticTransfer(boolean automaticTransfer) {
        this.automaticTransfer = automaticTransfer;
    }

    @Override
    public String toString() {
        return "AwaitedJob{" + "jobId='" + jobId + '\'' + ", localInputFolder='" + localInputFolder + '\'' +
            ", inputSpaceURL='" + inputSpaceURL + '\'' + ", pushURL='" + pushURL + '\'' +
            ", isolateTaskOutputs=" + isolateTaskOutputs + ", automaticTransfer=" + automaticTransfer +
            ", localOutputFolder='" + localOutputFolder + '\'' + ", outputSpaceURL='" + outputSpaceURL +
            '\'' + ", pullURL='" + pullURL + '\'' + '}';
    }
}
