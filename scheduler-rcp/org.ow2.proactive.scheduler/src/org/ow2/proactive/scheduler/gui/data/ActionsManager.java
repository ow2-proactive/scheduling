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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.gui.data;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.gui.actions.SchedulerGUIAction;


/**
 *
 *
 * @author The ProActive Team
 */
public class ActionsManager {

    private static ActionsManager instance = null;
    private List<SchedulerGUIAction> actions = null;
    private SchedulerStatus schedulerStatus = null;
    private boolean connected = false;

    private ActionsManager() {
        actions = new ArrayList<SchedulerGUIAction>();
        schedulerStatus = SchedulerStatus.KILLED;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setSchedulerStatus(SchedulerStatus schedulerStatus) {
        this.schedulerStatus = schedulerStatus;
    }

    public boolean addAction(SchedulerGUIAction action) {
        return this.actions.add(action);
    }

    public void update() {
        boolean jobSelected = false;
        boolean owner = false;
        boolean jobInFinishQueue = false;

        if (connected) {
            List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
            if (jobsId.size() > 0) {
                List<JobState> jobs = JobsController.getLocalView().getJobsByIds(jobsId);
                if (jobs.size() > 0) {
                    JobState job = jobs.get(0);
                    jobSelected = true;
                    jobInFinishQueue = (job.getStatus() == JobStatus.CANCELED) ||
                        (job.getStatus() == JobStatus.FAILED) || (job.getStatus() == JobStatus.FINISHED) ||
                        (job.getStatus() == JobStatus.KILLED);
                    owner = SchedulerProxy.getInstance().isItHisJob(job.getOwner());
                    for (int i = 1; owner && (i < jobs.size()); i++) {
                        owner &= SchedulerProxy.getInstance().isItHisJob(jobs.get(i).getOwner());
                    }
                }
            }
        }

        for (SchedulerGUIAction action : actions) {
            action.setEnabled(connected, schedulerStatus, /*SchedulerProxy.getInstance().isAnAdmin()*/true,
                    jobSelected, owner, jobInFinishQueue);
        }
    }

    public static ActionsManager getInstance() {
        if (instance == null)
            instance = new ActionsManager();
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }
}
