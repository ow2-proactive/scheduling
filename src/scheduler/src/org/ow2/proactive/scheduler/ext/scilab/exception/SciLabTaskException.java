package org.ow2.proactive.scheduler.ext.scilab.exception;

/**
 * This exception represents errors which occur in SciLab scripts when executed
 * @author The ProActive Team
 *
 */
public class SciLabTaskException extends RuntimeException {

    public SciLabTaskException(String message) {
        super(message);
    }
}
