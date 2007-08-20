package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;


public class GroupGlobus extends AbstractGroup {
    private String count;
    private String queue;
    private String maxTime;
    private String stderr;
    private String stdout;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the count.
     * @return String
     */
    public String getCount() {
        return count;
    }

    /**
     * Sets the count.
     * @param count The count to set
     */
    public void setCount(String count) {
        this.count = count;
    }

    /**
     * @return Returns the queue.
     */
    public String getQueue() {
        return queue;
    }

    /**
     * @param queue The queue to set.
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }

    /**
     * @return Returns the stderr.
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * @param stderr The stderr to set.
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    /**
     * @return Returns the stdout.
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * @param stdout The stdout to set.
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }
}
