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
package org.objectweb.proactive.extra.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extra.scheduler.gui.data.SchedulerProxy;
import org.objectweb.proactive.extra.scheduler.gui.data.TableManager;


public class PriorityIdleJobAction extends Action {
    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static PriorityIdleJobAction instance = null;

    private PriorityIdleJobAction() {
        this.setText("Idle");
        this.setToolTipText("To set the job priority to \"idle\"");
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    @Override
    public void run() {
        JobId jobId = TableManager.getInstance().getLastJobIdOfLastSelectedItem();
        if (jobId != null) {
            SchedulerProxy.getInstance().changePriority(jobId, JobPriority.IDLE);
        }
    }

    public static PriorityIdleJobAction newInstance() {
        instance = new PriorityIdleJobAction();
        return instance;
    }

    public static PriorityIdleJobAction getInstance() {
        return instance;
    }
}
