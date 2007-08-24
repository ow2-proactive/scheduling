package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.PathElement;


public class GroupNG extends AbstractGroup {
    private String jobname;
    private String queue;
    private String count;
    private String stderr;
    private String stdout;
    private PathElement executablePath;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setJobname(String jobname) {
        this.jobname = jobname;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setExecutable(PathElement execPath) {
        this.executablePath = execPath;
    }
}
