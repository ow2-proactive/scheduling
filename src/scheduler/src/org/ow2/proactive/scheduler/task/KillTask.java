package org.ow2.proactive.scheduler.task;

import java.util.Timer;
import java.util.TimerTask;

import org.ow2.proactive.scheduler.common.task.executable.Executable;


/**
 * Class responsible for killing an executable if it does not finish before the walltime.
 * It accepts in constructor an executable and walltime, then we can start it by calling schedule method
 * If the executable finishes before the walltime we should call cancel method to cancel the killing function scheduled for future invocation.

 * @author The ProActive Team
 */
public class KillTask {

    private Executable executable;
    private Timer timer;
    private long walltime;

    /**
     * Create a new instance of KillTask.
     *
     * @param executable the executable that may be killed.
     * @param walltime the walltime not to be exceeded.
     */
    public KillTask(Executable executable, long walltime) {
        this.executable = executable;
        this.walltime = walltime;
    }

    /**
     * Starting a timer for killing an executable when the walltime is over
     */
    public void schedule() {
        timer = new Timer();
        timer.schedule(new KillProcess(), walltime);
    }

    /**
     * Canceling a timer scheduled for killing an executable in the future
     */
    synchronized public void cancel() {
        timer.cancel();
    }

    class KillProcess extends TimerTask {
        /**
         * @see java.util.TimerTask#run()
         */
        public void run() {
            executable.kill();
        }
    }
}
