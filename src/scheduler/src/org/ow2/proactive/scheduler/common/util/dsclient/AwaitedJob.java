package org.ow2.proactive.scheduler.common.util.dsclient;

public class AwaitedJob {

    private String jobId;

    //input data
    private String localInputFolder;
    private String inputSpaceURL;

    //output data
    private String localOutputFolder;
    private String outputSpaceURL;

    public AwaitedJob() {
    }

    public AwaitedJob(String jobId, String localInputFolder, String inputSpaceURL, String localOutputFolder,
            String outputSpaceURL) {
        super();
        this.jobId = jobId;
        this.localInputFolder = localInputFolder;
        this.inputSpaceURL = inputSpaceURL;
        this.localOutputFolder = localOutputFolder;
        this.outputSpaceURL = outputSpaceURL;
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
