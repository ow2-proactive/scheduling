package org.ow2.proactive.scheduler.ext.matsci.worker.util;

import java.io.Serializable;


/**
 * MatSciTaskServerConfig
 *
 * @author The ProActive Team
 */
public class MatSciTaskServerConfig implements Serializable {

    private boolean deployIoThread;
    private int taskCountBeforeJVMRespawn;
    private int taskCountBeforeJVMRespawnWindows;
    private long semaphoreTimeout;
    private int semaphoreRetryAquire;
    private int maxNbAttempts;

    public MatSciTaskServerConfig(boolean deployIoThread, int taskCountBeforeJVMRespawn,
            int taskCountBeforeJVMRespawnWindows, long semaphoreTimeout, int semaphoreRetryAquire,
            int maxNbAttempts) {
        this.deployIoThread = deployIoThread;
        this.taskCountBeforeJVMRespawn = taskCountBeforeJVMRespawn;
        this.taskCountBeforeJVMRespawnWindows = taskCountBeforeJVMRespawnWindows;
        this.semaphoreTimeout = semaphoreTimeout;
        this.semaphoreRetryAquire = semaphoreRetryAquire;
        this.maxNbAttempts = maxNbAttempts;
    }

    public boolean isDeployIoThread() {
        return deployIoThread;
    }

    public int getTaskCountBeforeJVMRespawn() {
        return taskCountBeforeJVMRespawn;
    }

    public int getTaskCountBeforeJVMRespawnWindows() {
        return taskCountBeforeJVMRespawnWindows;
    }

    public long getSemaphoreTimeout() {
        return semaphoreTimeout;
    }

    public int getSemaphoreRetryAquire() {
        return semaphoreRetryAquire;
    }

    public int getMaxNbAttempts() {
        return maxNbAttempts;
    }
}
