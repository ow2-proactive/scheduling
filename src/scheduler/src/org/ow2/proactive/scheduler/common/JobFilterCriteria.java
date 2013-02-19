package org.ow2.proactive.scheduler.common;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;

@PublicAPI
public class JobFilterCriteria implements Serializable {

    private final boolean myJobsOnly;

    private final boolean pending;

    private final boolean running;

    private final boolean finished;

    public JobFilterCriteria(boolean myJobsOnly, boolean pending, boolean running, boolean finished) {
        this.myJobsOnly = myJobsOnly;
        this.pending = pending;
        this.running = running;
        this.finished = finished;
    }

    public boolean isMyJobsOnly() {
        return myJobsOnly;
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isFinished() {
        return finished;
    }

}
