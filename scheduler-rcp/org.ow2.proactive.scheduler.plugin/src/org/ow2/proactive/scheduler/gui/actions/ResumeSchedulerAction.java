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

import org.eclipse.jface.resource.ImageDescriptor;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;


/**
 * @author The ProActive Team
 */
public class ResumeSchedulerAction extends SchedulerGUIAction {

    public ResumeSchedulerAction() {
        this.setText("Resume scheduler");
        this.setToolTipText("Resume the scheduler");
        this
                .setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(),
                        "icons/scheduler_resume.png"));
        this.setEnabled(false);
    }

    @Override
    public void run() {
        SchedulerProxy.getInstance().resume();
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (connected && admin &&
            ((schedulerStatus == SchedulerStatus.PAUSED) || (schedulerStatus == SchedulerStatus.FROZEN)))
            setEnabled(true);
        else
            setEnabled(false);
    }
}
