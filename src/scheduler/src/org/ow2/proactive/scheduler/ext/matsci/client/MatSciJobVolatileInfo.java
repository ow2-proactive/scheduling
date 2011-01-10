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
package org.ow2.proactive.scheduler.ext.matsci.client;

import org.objectweb.proactive.core.body.request.Request;
import org.ow2.proactive.scheduler.common.job.JobStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;


/**
 * MatSciJobVolatileInfo
 *
 * @author The ProActive Team
 */
public class MatSciJobVolatileInfo<R> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 30L;

    protected JobStatus status;

    protected Request pendingWaitAllRequest;

    protected HashMap<String, Request> pendingWaitRequest = new HashMap<String, Request>();

    protected boolean jobFinished;

    protected HashMap<String, R> results = new HashMap<String, R>();

    protected HashSet<String> receivedTasks = new HashSet<String>();

    protected HashSet<String> servedTasks = new HashSet<String>();

    protected HashMap<String, Throwable> exceptions = new HashMap<String, Throwable>();

    protected HashMap<String, String> logs = new HashMap<String, String>();

    protected MatSciJobPermanentInfo info;

    public int getDepth() {
        return info.getDepth();
    }

    public boolean isDebugCurrentJob() {
        return info.getConf().isDebug();
    }

    public boolean isTimeStamp() {
        return info.getConf().isTimeStamp();
    }

    public boolean isJobFinished() {
        return jobFinished;
    }

    public void setJobFinished(boolean jobFinished) {
        this.jobFinished = jobFinished;
    }

    public MatSciJobVolatileInfo(MatSciJobPermanentInfo info) {
        // convenience notation, the job is probably not running yet, we are only interested in the other states not pending/running
        this.status = JobStatus.RUNNING;
        this.jobFinished = false;
        this.info = info;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public TreeSet<String> getFinalTasksNames() {
        return info.getFinalTaskNames();
    }

    public TreeSet<String> getTasksNames() {
        return info.getTaskNames();
    }

    public void setResult(String tname, R res) {
        this.results.put(tname, res);
    }

    public R getResult(String tname) {
        return this.results.get(tname);
    }

    public void addLogs(String tname, String log) {
        String logs = this.logs.get(tname);
        if (logs == null) {
            logs = "";
        }
        logs += log;
        this.logs.put(tname, logs);
    }

    public String getLogs(String tname) {
        return this.logs.get(tname);
    }

    public Throwable getException(String tname) {
        return this.exceptions.get(tname);
    }

    public void setException(String tname, Throwable t) {
        this.exceptions.put(tname, t);
    }

    public Request getPendingWaitAllRequest() {
        return pendingWaitAllRequest;
    }

    public void setPendingWaitAllRequest(Request req) {
        this.pendingWaitAllRequest = req;
    }

    public Request getPendingRequest(String tname) {
        return pendingWaitRequest.get(tname);
    }

    public void setPendingRequest(String tname, Request r) {
        pendingWaitRequest.put(tname, r);
    }

    public int nbResults() {
        return this.info.getNbres();
    }

    public void addReceivedTask(String tname) {
        this.receivedTasks.add(tname);
    }

    public boolean isReceivedResult(String tname) {
        return this.receivedTasks.contains(tname);
    }

    public void addServedTask(String tname) {
        this.servedTasks.add(tname);
    }

    public boolean isServedResult(String tname) {
        return this.servedTasks.contains(tname);
    }

    public boolean allServed() {
        return this.servedTasks.size() == nbResults();
    }

    public TreeSet<String> missingResults() {
        TreeSet<String> allTasks = (TreeSet<String>) info.getFinalTaskNames().clone();
        allTasks.removeAll(receivedTasks);
        return allTasks;
    }

}
