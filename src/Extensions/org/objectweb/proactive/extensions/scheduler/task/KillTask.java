package org.objectweb.proactive.extensions.scheduler.task;

import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.proactive.extensions.scheduler.common.task.executable.Executable;


/**
 * Class responsible for killing an executable if it does not finish before the walltime.
 * It accepts in constructor an executable and walltime, then we can start it by calling schedule method
 * If the executable finishes before the walltime we should call cancel method to cancel the killing function scheduled for future invocation.

 * @author The ProActive Team
 */
public class KillTask {

    private Executable executable;
    private Timer timer;
    private long miliseconds;

    public KillTask(Executable executable, long miliseconds) {
        this.executable = executable;
        this.miliseconds = miliseconds;
        timer = new Timer();
    }

    /**
     * Starting a timer for killing an executable when the walltime is over
     */
    public void schedule() {
        timer.schedule(new KillProcess(), miliseconds);
    }

    /**
     * Cancelling a timer scheduled for killing an executable in the future
     */
    synchronized public void cancel() {
        timer.cancel();
    }

    synchronized private void kill() {
        executable.kill();
    }

    class KillProcess extends TimerTask {
        public void run() {
            kill();
        }
    }
}
