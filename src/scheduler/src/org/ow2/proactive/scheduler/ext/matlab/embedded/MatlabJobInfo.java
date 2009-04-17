/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matlab.embedded;

import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.objectweb.proactive.core.body.request.Request;
import ptolemy.data.Token;

import java.util.ArrayList;
import java.io.Serializable;


/**
 * JobState
 *
 * @author The ProActive Team
 */
public class MatlabJobInfo implements Serializable {
    private JobStatus status;

    private ArrayList<Token> results;

    private Throwable errorToThrow;

    private Request pendingRequest;

    private boolean jobFinished;

    private boolean debugCurrentJob;

    public boolean isDebugCurrentJob() {
        return debugCurrentJob;
    }

    public MatlabJobInfo(boolean debug) {
        // convenience notation, the job is probably not running yet, we are only interested in the other states not pending/running
        this.status = JobStatus.RUNNING;
        this.jobFinished = false;
        this.debugCurrentJob = debug;
    }

    public boolean isJobFinished() {
        return jobFinished;
    }

    public void setJobFinished(boolean jobFinished) {
        this.jobFinished = jobFinished;
    }

    public Throwable getErrorToThrow() {
        return errorToThrow;
    }

    public void setErrorToThrow(Throwable errorToThrow) {
        this.errorToThrow = errorToThrow;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public ArrayList<Token> getResults() {
        return results;
    }

    public void setResults(ArrayList<Token> results) {
        this.results = results;
    }

    public Request getPendingRequest() {
        return pendingRequest;
    }

    public void setPendingRequest(Request pendingRequest) {
        this.pendingRequest = pendingRequest;
    }

}
