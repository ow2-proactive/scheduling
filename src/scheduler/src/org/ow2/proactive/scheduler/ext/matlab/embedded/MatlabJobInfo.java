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
