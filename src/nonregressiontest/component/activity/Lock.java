package nonregressiontest.component.activity;


/**
 * A simple semaphore
 *
 * @author Matthieu Morel
 *
 */
public class Lock {
    boolean locked = false;

    public Lock() {
    }

    public synchronized void acquireLock() {
        locked = true;
        notify();
    }

    public synchronized void waitForRelease() {
        while (locked) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void releaseLock() {
        if (locked) {
            locked = false;
        }
        notify();
    }
}
