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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;


/**
 * MatSciJobVolatileInfo infos of a job seen by the middleman JVM
 *
 * @author The ProActive Team
 */
public class MatSciJobVolatileInfo<R> implements Serializable {

    /**
     * Status of the job (status seen by the scheduler)
     */
    protected JobStatus status;

    /**
     * A boolean, true if the job is finished
     */
    protected boolean jobFinished;

    /**
     * results of the job
     */
    protected HashMap<String, R> results = new HashMap<String, R>();

    /**
     * Names of tasks already received by the middleman JVM
     */
    protected HashSet<String> receivedTasks = new HashSet<String>();

    /**
     * Names of tasks already given by the middleman JVM to the Matlab or Scilab client
     */
    protected HashSet<String> servedTasks = new HashSet<String>();

    /**
     * Exceptions which occurred in the job
     */
    protected HashMap<String, Throwable> exceptions = new HashMap<String, Throwable>();

    /**
     * logs of tasks in the job
     */
    protected HashMap<String, String> logs = new HashMap<String, String>();

    /**
     * permanent info to be stored
     */
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

        Pair<Integer, Integer> idpair = computeIdsFromTName(tname);
        int i = idpair.getX();
        int j = idpair.getY();
        String tname_t;
        // We add logs to each succeeding task (this way, we ensure that if a task fails, the logs from all previous tasks will appear).
        do {
            tname_t = "" + i + "_" + j;
            String logs = this.logs.get(tname_t);
            if (logs == null) {
                logs = "";
            }
            logs += log;
            this.logs.put(tname_t, logs);
            j = j + 1;
        } while (!info.getFinalTaskNames().contains(tname_t));
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

    public int nbResults() {
        return this.info.getNbres();
    }

    public void addReceivedTask(String tname) {
        this.receivedTasks.add(tname);
    }

    public boolean isReceivedResult(String tname) {
        return this.receivedTasks.contains(tname);
    }

    public int isAnyReceivedResult(ArrayList<String> tnames) {
        for (int i = 0; i < tnames.size(); i++) {
            if (this.receivedTasks.contains(tnames.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public boolean areAllReceived(ArrayList<String> tnames) {
        for (int i = 0; i < tnames.size(); i++) {
            if (!this.receivedTasks.contains(tnames.get(i))) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Boolean> areAwaited(ArrayList<String> tnames) {
        ArrayList<Boolean> answer = new ArrayList<Boolean>();
        for (int i = 0; i < tnames.size(); i++) {
            if (this.receivedTasks.contains(tnames.get(i))) {
                answer.add(false);
            } else {
                answer.add(true);
            }
        }
        return answer;
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

    private Pair<Integer, Integer> computeIdsFromTName(String tname) {
        String[] ids = tname.split("_");
        return new Pair<Integer, Integer>(Integer.parseInt(ids[0]), Integer.parseInt(ids[1]));
    }

}
