/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matsci.client.common.data;

import java.io.Serializable;


/**
 * ResultsAndLogs an object encapsulating PAsolve Matlab or Scilab results plus logs and exceptions
 *
 * @author The ProActive Team
 */
public class ResultsAndLogs<R> implements Serializable {
    /**
     * Result of the task
     */
    protected R result;

    /**
     * logs of the task
     */
    protected String logs;

    /**
     * Exception thrown if any
     */
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
