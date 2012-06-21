package org.ow2.proactive.scheduler.common.util.dsclient;

/**
 * A job which has been submitted to the scheduler and whose output data needs
 * to be transfered after termination.
 * 
 * @author esalagea
 * 
 */
public class AwaitedJob {

    private String jobId;

    // input data
    private String localInputFolder;
    private String inputSpaceURL;
    private String pushURL;

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
            String localOutputFolder, String outputSpaceURL, String pull_url) {
        super();
        this.jobId = jobId;
        this.localInputFolder = localInputFolder;
        this.inputSpaceURL = inputSpaceURL;
        this.localOutputFolder = localOutputFolder;
        this.outputSpaceURL = outputSpaceURL;
        this.pushURL = push_url;
        this.pullURL = pull_url;
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

}
