package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;


public class GroupFura extends AbstractGroup {
    private String jobName;
    private String description;
    private String stdout;
    private String stderr;
    private String stdin;
    private String maxTime;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
