package org.ow2.proactive.scheduler.ext.matlab.common.exception;

/**
 * UnsufficientLicencesException
 *
 * @author The ProActive Team
 */
public class UnsufficientLicencesException extends MatlabInitException {
    public UnsufficientLicencesException() {
        super("Unsufficient licences");
    }

    public UnsufficientLicencesException(String string) {
        super(string);
    }

    public UnsufficientLicencesException(Throwable cause) {
        super(cause);
    }
}
