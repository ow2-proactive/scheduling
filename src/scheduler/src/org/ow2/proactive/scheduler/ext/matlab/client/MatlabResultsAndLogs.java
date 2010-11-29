package org.ow2.proactive.scheduler.ext.matlab.client;

/**
 * MatlabResultsAndLogs
 *
 * @author The ProActive Team
 */

import org.ow2.proactive.scheduler.ext.matsci.client.MatSciTaskStatus;
import org.ow2.proactive.scheduler.ext.matsci.client.ResultsAndLogs;
import ptolemy.data.Token;


public class MatlabResultsAndLogs extends ResultsAndLogs<Token> {

    public MatlabResultsAndLogs() {
        super();
    }

    public MatlabResultsAndLogs(Token result, String logs, Throwable exception, MatSciTaskStatus status) {
        super(result, logs, exception, status);
    }
}
