/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.ow2.proactive.scheduler.gui.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerState;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.data.TableManager;


/**
 * @author The ProActive Team
 */
public class PriorityJobAction extends SchedulerGUIAction {
    private static Map<JobPriority, PriorityJobAction> instances = new HashMap<JobPriority, PriorityJobAction>();
    private JobPriority priority = null;

    private PriorityJobAction(JobPriority priority) {
        this.priority = priority;
        this.setText(priority.toString());
        this.setToolTipText("To set the job priority to \"" + priority.toString().toLowerCase() + "\"");
        this.setEnabled(false);
    }

    @Override
    public void run() {
        List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
        for (JobId jobId : jobsId)
            SchedulerProxy.getInstance().changePriority(jobId, priority);
    }

    public static PriorityJobAction newInstance(JobPriority priority) {
        PriorityJobAction instance = new PriorityJobAction(priority);
        instances.put(priority, instance);
        return instance;
    }

    public static PriorityJobAction getInstance(JobPriority priority) {
        return instances.get(priority);
    }

    @Override
    public void setEnabled(boolean connected, SchedulerState schedulerState, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (connected && jobSelected && !jobInFinishQueue && (owner || admin))
            setEnabled(true);
        else
            setEnabled(false);
    }
}