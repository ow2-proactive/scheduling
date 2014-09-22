package org.ow2.proactive.scheduler.core;

public class JobEmailNotificationException extends Exception {

    private static final long serialVersionUID = 60L;

    public JobEmailNotificationException(String message) {
        super(message);
    }

    public JobEmailNotificationException(String message, Exception cause) {
        super(message, cause);
    }
}