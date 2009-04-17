/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.scilab.embedded;

import javasci.SciData;

import java.io.Serializable;


/**
 * ResultsAndLogs wrapper of scilab tasks results
 *
 * @author The ProActive Team
 */
public class ResultsAndLogs implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 10L;
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
