package org.objectweb.proactive.core.util;


/**
 * All time are in milliseconds
 * We use System.nanoTime() as it tries to be monotonic
 */
public class TimeoutAccounter {
    private final long timeout;
    private long start;
    private static final TimeoutAccounter NO_TIMEOUT = new TimeoutAccounter(0);

    private TimeoutAccounter(long timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Negative timeout: " + timeout);
        }
        this.timeout = timeout;
        if (this.timeout != 0) {
            this.start = System.nanoTime() / 1000000;
        }
    }

    public static TimeoutAccounter getAccounter(long timeout) {
        if (timeout == 0) {
            return NO_TIMEOUT;
        }

        return new TimeoutAccounter(timeout);
    }

    public boolean isTimeoutElapsed() {
        return (this.timeout != 0) &&
        (((System.nanoTime() / 1000000) - this.start) >= this.timeout);
    }

    /**
     * Will never return 0 if a timeout was originally specified,
     * that's why you must check isTimeoutElapsed() before.
     */
    public long getRemainingTimeout() {
        long remainingTimeout = 0;
        if (this.timeout != 0) {
            remainingTimeout = (System.nanoTime() / 1000000) - start - timeout;
            if (remainingTimeout <= 0) {
                /* Returning a timeout of 0 would mean infinite timeout */
                remainingTimeout = 1;
            }
        }
        return remainingTimeout;
    }
}
