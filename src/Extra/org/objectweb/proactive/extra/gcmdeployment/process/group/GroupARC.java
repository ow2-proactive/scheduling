package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupARCParser.FileTransfer;


public class GroupARC extends AbstractGroup {
    private String jobName;
    private String count;
    private List<String> args;
    private String stdout;
    private String stderr;
    private String stdin;
    private String maxTime;
    private String notify;
    private List<FileTransfer> inputFiles;
    private List<FileTransfer> outputFiles;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setArguments(List<String> args) {
        this.args = args;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }

    public void setNotify(String notify) {
        this.notify = notify;
    }

    public void setInputFiles(List<FileTransfer> inputFiles) {
        this.inputFiles = inputFiles;
    }

    public void setOutputFiles(List<FileTransfer> outputFiles) {
        this.outputFiles = outputFiles;
    }
}
