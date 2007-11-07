package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;


public class GroupLoadLeveler extends AbstractGroup {
    private String jobName;
    private String stdout;
    private String stderr;
    private String directory;
    private String resources;
    private String maxTime;
    private List<String> argumentList;
    private String nbTasks;
    private String cpusPerTask;
    private String tasksPerHost;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }

    public void setArgumentList(List<String> argumentList) {
        this.argumentList = argumentList;
    }

    public void setNbTasks(String nbTasks) {
        this.nbTasks = nbTasks;
    }

    public void setCpusPerTask(String cpusPerTask) {
        this.cpusPerTask = cpusPerTask;
    }

    public void setTasksPerHost(String tasksPerHost) {
        this.tasksPerHost = tasksPerHost;
    }
}
