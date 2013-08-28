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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;


public class SchedulerSession {

    protected String sessionId;
    protected SchedulerProxyUserInterface scheduler;
    protected String userName;
    protected Map<String, JobOutputAppender> jobOutputAppenders = new HashMap<String, JobOutputAppender>();

    public SchedulerProxyUserInterface getScheduler() {
        return scheduler;
    }

    public void setScheduler(SchedulerProxyUserInterface scheduler) {
        this.scheduler = scheduler;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public JobOutputAppender getJobOutputAppender(String jobId) {
        return jobOutputAppenders.get(jobId);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        for (JobOutputAppender jobOutputAppender : jobOutputAppenders.values()) {
            jobOutputAppender.terminate();
        }
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void addJobOutputAppender(String jobId, JobOutputAppender joa) {
        jobOutputAppenders.put(jobId, joa);
    }

    public void removeJobOutAppender(String jobId) {
        if (jobOutputAppenders.containsKey(jobId)) {
            jobOutputAppenders.remove(jobId).terminate();
        }
    }
}
