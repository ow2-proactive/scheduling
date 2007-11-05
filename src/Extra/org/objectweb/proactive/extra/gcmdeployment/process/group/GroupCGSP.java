package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;


public class GroupCGSP extends AbstractGroup {
    private String hostName;
    private String queue;
    private String count;
    private String directory;
    private String stdout;
    private String stderr;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setHostName(String hostname) {
        this.hostName = hostname;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }
}
