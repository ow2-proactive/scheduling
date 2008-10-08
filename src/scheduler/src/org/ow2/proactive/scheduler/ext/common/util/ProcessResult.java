package org.ow2.proactive.scheduler.ext.common.util;

/**
 * Utility class which represents the result of a process (return value + logs)
 * @author The ProActive Team
 *
 */
public class ProcessResult {
    private int returnValue;
    private String[] output;
    private String[] error;

    public ProcessResult(int returnValue, String[] output, String[] error) {
        this.returnValue = returnValue;
        this.output = (output != null) ? output : new String[0];
        this.error = (error != null) ? error : new String[0];
    }

    public int getReturnValue() {
        return returnValue;
    }

    public String[] getOutput() {
        return output;
    }

    public String[] getError() {
        return error;
    }
}
