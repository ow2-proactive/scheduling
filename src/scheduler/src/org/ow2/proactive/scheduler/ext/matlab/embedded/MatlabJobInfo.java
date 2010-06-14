/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
    /**  */
	private static final long serialVersionUID = 21L;

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
