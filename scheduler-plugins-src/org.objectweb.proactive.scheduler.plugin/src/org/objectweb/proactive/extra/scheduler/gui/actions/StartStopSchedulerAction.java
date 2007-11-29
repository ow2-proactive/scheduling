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
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.extra.scheduler.gui.data.SchedulerProxy;


/**
 * @author FRADJ Johann
 * @version 1.0, Aug 8, 2007
 * @since ProActive 3.2
 */
public class StartStopSchedulerAction extends Action {
    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static StartStopSchedulerAction instance = null;
    private boolean started = false;

    private StartStopSchedulerAction() {
        this.setText("Start/Stop scheduler");
        this.setToolTipText("To start or stop the scheduler");
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "icons/scheduler_start.png"));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
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
        this.setToolTipText(
            "To start the scheduler (this will finish start or restart the scheduler)");
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "icons/scheduler_start.png"));
    }

    public void setStopMode() {
        started = true; // If I set the text to "stop", so the scheduler is
                        // started/running !

        this.setText("Stop scheduler");
        this.setToolTipText(
            "To stop the scheduler (this will finish all pending and running jobs)");
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "icons/scheduler_stop.png"));
    }

    public static StartStopSchedulerAction newInstance() {
        instance = new StartStopSchedulerAction();
        return instance;
    }

    public static StartStopSchedulerAction getInstance() {
        return instance;
    }
}
