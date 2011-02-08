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
package org.ow2.proactive_grid_cloud_portal.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;


@XmlRootElement
public class MyJobState {

    private JobState js;

    public MyJobState() {
    }

    public MyJobState(JobState js) {
        this.js = js;

    }

    public long getFinishedTime() {
        return js.getFinishedTime();
    }

    public String getDescription() {
        return js.getDescription();
    }

    public JobId getId() {
        return js.getId();
    }

    public Map<String, String> getGenericInformations() {
        Map<String, String> gen = js.getGenericInformations();
        if (gen != null) {
            gen = new HashMap<String, String>(js.getGenericInformations());
        }

        return gen;
    }

    public int getNumberOfFinishedTasks() {
        return js.getJobInfo().getNumberOfFinishedTasks();
    }

    public int getNumberOfPendingTasks() {
        return js.getJobInfo().getNumberOfPendingTasks();
    }

    public int getNumberOfRunningTasks() {
        return js.getJobInfo().getNumberOfRunningTasks();
    }

    public JobStatus getStatus() {
        return PAFuture.getFutureValue(js.getJobInfo().getStatus());
    }

    public long getStartTime() {
        return js.getStartTime();
    }

    public int getTotalNumberOfTasks() {
        return js.getJobInfo().getTotalNumberOfTasks();
    }

    public long getSubmittedTime() {
        return js.getSubmittedTime();
    }

    public JobPriority getPriority() {
        return js.getPriority();
    }
}
