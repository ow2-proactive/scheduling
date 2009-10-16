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

import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.gui.Activator;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;


/**
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class StartStopSchedulerAction extends SchedulerGUIAction {
    private boolean started = false;

    public StartStopSchedulerAction() {
        this.setText("Start/Stop scheduler");
        this.setToolTipText("Start or stop the scheduler");
        this.setImageDescriptor(Activator.getImageDescriptor("icons/scheduler_start.png"));
        this.setEnabled(false);
    }

    @Override
    public void run() {
        if (started) {
            SchedulerProxy.getInstance().stop();
        } else {
            SchedulerProxy.getInstance().start();
        }
    }

    public void setStartMode() {
        started = false; // If I set the text to "start", so the scheduler is
        // stopped !

        this.setText("Start scheduler");
        this.setToolTipText("Start the scheduler (this will finish start or restart the scheduler)");
        this.setImageDescriptor(Activator.getImageDescriptor("icons/scheduler_start.png"));
    }

    public void setStopMode() {
        started = true; // If I set the text to "stop", so the scheduler is
        // started/running !

        this.setText("Stop scheduler");
        this.setToolTipText("Stop the scheduler (this will finish all pending and running jobs)");
        this.setImageDescriptor(Activator.getImageDescriptor("icons/scheduler_stop.png"));
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (connected && admin && (schedulerStatus == SchedulerStatus.STOPPED)) {
            setStartMode();
            setEnabled(true);
        } else if (connected && admin && (schedulerStatus != SchedulerStatus.KILLED) &&
            (schedulerStatus != SchedulerStatus.UNLINKED) &&
            (schedulerStatus != SchedulerStatus.SHUTTING_DOWN)) {
            setStopMode();
            setEnabled(true);
        } else
            setEnabled(false);
    }
}
