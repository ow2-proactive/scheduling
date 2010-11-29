package org.ow2.proactive.scheduler.ext.matsci.client;

/**
 * PASolveException
 *
 * @author The ProActive Team
 */
public class PASolveException extends RuntimeException {
    public PASolveException(String message) {
        super(message);
    }

    public PASolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public PASolveException(Throwable cause) {
        super(cause);
    }

    public PASolveException() {
    }
}
