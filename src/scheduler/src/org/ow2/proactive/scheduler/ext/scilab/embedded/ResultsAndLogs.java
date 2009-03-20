package org.ow2.proactive.scheduler.ext.scilab.embedded;

import javasci.SciData;

import java.io.Serializable;


/**
 * ResultsAndLogs
 *
 * @author The ProActive Team
 */
public class ResultsAndLogs implements Serializable {

    private SciData result;
    private String logs;
    private Throwable exception;

    public ResultsAndLogs(SciData result, String logs, Throwable exception) {
        this.result = result;
        this.logs = logs;
        this.exception = exception;
    }

    public SciData getResult() {
        return result;
    }

    public String getLogs() {
        return logs;
    }

    public String toString() {
        return result.toString();
    }

    public Throwable getException() {
        return exception;
    }
}
