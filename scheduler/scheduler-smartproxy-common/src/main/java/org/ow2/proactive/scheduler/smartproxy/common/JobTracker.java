/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.smartproxy.common;

import org.apache.log4j.Logger;

import java.util.Set;


/**
 * JobTracker is in charge to track jobs and associated tasks. In summary, it
 * records jobs which have been submit to the scheduler for execution and allows
 * to manage these records. Most actions are synchronized with the scheduler to
 * retrieve and remove job or task outputs remotely.
 *
 * @author The ProActive Team
 */
public abstract class JobTracker {

    public static final Logger logger = Logger.getLogger(JobTracker.class);

    protected final JobDatabase jobDatabase;

    public JobTracker() {
        this.jobDatabase = new JobDatabase();
    }

    /**
     * Removes from the proxy knowledge all info related with the given job.
     * This will also delete every folder created by the job in the shared input and output spaces.
     *
     * @param id job ID
     */
    public abstract void removeAwaitedJob(String id);

    /**
     * Removes from the proxy knowledge all info related with the given task.
     * If all tasks of a job have been removed this way, the job itself will be removed.
     *
     * @param id job ID
     * @param taskName task name
     */
    public abstract void removeAwaitedTask(String id, String taskName);

    public void close() {
        jobDatabase.close();
    }

    public void setSessionName(String name) {
        jobDatabase.setSessionName(name);
    }

    public void loadJobs() {
        jobDatabase.loadJobs();
    }

    public void cleanDataBase() {
        jobDatabase.cleanDataBase();
    }

    public void discardAllJobs() {
        jobDatabase.discardAllJobs();
    }

    public void discardJob(String jobID) {
        jobDatabase.discardJob(jobID);
    }

    public Set<String> getAwaitedJobsIds() {
        return jobDatabase.getAwaitedJobsIds();
    }

    public AwaitedJob getAwaitedJob(String id) {
        return jobDatabase.getAwaitedJob(id);
    }

    public boolean isAwaitedJob(String id) {
        return getAwaitedJob(id) != null;
    }

    public void putAwaitedJob(String id, AwaitedJob aj) {
        jobDatabase.putAwaitedJob(id, aj);
    }

    public void setTaskTransferring(String id, String taskName, boolean transferring) {
        jobDatabase.setTaskTransferring(id, taskName, transferring);
    }

}
