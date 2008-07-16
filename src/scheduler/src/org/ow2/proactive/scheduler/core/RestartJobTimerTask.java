/**
 * 
 */
package org.ow2.proactive.scheduler.core;

import java.util.TimerTask;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * RestartJobTimerTask is used to manage the timeout from which a task can be restarted after an error.
 * This timeout will increase according to a function defined in the job.
 *
 * @author jlscheef - ProActiveTeam
 * @date 10 juin 08
 * @since ProActive 4.0
 *
 */
public class RestartJobTimerTask extends TimerTask {

    /** The job on which to restart the task */
    private InternalJob job;
    /** The task that have to be restarted */
    private InternalTask task;

    /**
     * Create a new instance of RestartJobTimerTask using given job and task.
     *
     * @param job The job on which to restart the task
     * @param task The task that have to be restarted
     */
    public RestartJobTimerTask(InternalJob job, InternalTask task) {
        super();
        this.job = job;
        this.task = task;
    }

    /**
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {
        job.reStartTask(task);
    }

}
