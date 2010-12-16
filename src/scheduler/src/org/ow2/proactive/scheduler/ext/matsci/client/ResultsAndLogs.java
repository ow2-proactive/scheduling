package org.ow2.proactive.scheduler.ext.matsci.client;

import java.io.Serializable;


/**
 * ResultsAndLogs
 *
 * @author The ProActive Team
 */
public class ResultsAndLogs<R> implements Serializable {
    protected R result;
    protected String logs;
    protected Throwable exception;

    public ResultsAndLogs() {
    }

    public void setResult(R result) {

        this.result = result;
    }

    public void setLogs(String logs) {
        this.logs = logs;
        compressLogs();
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    protected MatSciTaskStatus status;

    public boolean isGlobalError() {
        return status == MatSciTaskStatus.GLOBAL_ERROR;
    }

    public boolean isRuntimeError() {
        return status == MatSciTaskStatus.RUNTIME_ERROR;
    }

    public boolean isOK() {
        return status == MatSciTaskStatus.OK;
    }

    public MatSciTaskStatus getStatus() {
        return status;
    }

    public void setStatus(MatSciTaskStatus status) {
        this.status = status;
    }

    public ResultsAndLogs(R result, String logs, Throwable exception, MatSciTaskStatus status) {
        this.result = result;
        this.logs = logs;
        compressLogs();
        this.exception = exception;
        this.status = status;

    }

    public R getResult() {
        return result;
    }

    public String getLogs() {
        return logs;
    }

    public boolean isMatSciError() {
        return status == MatSciTaskStatus.MATSCI_ERROR;
    }

    @Override
    public String toString() {
        if (result != null)
            return result.toString();
        else if (exception != null) {
            return exception.getMessage();
        } else {
            return "Error in remote script code";
        }
    }

    public Throwable getException() {
        return exception;
    }

    private void compressLogs() {
        if (logs.length() > 0) {
            StringBuilder sb = new StringBuilder(logs);
            char last = sb.charAt(0);
            for (int i = 1; i < sb.length(); i++) {
                if (last == '\r' && sb.charAt(i) == '\n') {
                    sb.deleteCharAt(i - 1);
                } else {
                    last = sb.charAt(i);
                }
            }
            logs = sb.toString();
        }
    }
}
