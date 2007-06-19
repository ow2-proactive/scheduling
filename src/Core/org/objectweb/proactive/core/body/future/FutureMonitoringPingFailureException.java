package org.objectweb.proactive.core.body.future;

public class FutureMonitoringPingFailureException extends RuntimeException {
    public FutureMonitoringPingFailureException(Throwable cause) {
        super("Failure to ping creator body", cause);
    }
}
