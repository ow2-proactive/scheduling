package org.ow2.proactive.scheduler.core;

public class JobEmailNotificationException extends Exception {

    private static final long serialVersionUID = 61L;

    public JobEmailNotificationException(String message) {
        super(message);
    }

    public JobEmailNotificationException(String message, Exception cause) {
        super(message, cause);
    }
}