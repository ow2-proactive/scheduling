package org.ow2.proactive.scheduler.ext.scilab.exception;

/**
 * InvalidParameterException
 *
 * @author The ProActive Team
 */
public class InvalidParameterException extends Exception {
    public InvalidParameterException(Class<?> class1) {
        super(class1.getCanonicalName());
    }
}
