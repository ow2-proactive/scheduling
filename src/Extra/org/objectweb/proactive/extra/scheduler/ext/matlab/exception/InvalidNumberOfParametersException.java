package org.objectweb.proactive.extra.scheduler.ext.matlab.exception;

public class InvalidNumberOfParametersException extends Exception {
    public InvalidNumberOfParametersException(int number) {
        super("" + number + " parameters");
    }
}
