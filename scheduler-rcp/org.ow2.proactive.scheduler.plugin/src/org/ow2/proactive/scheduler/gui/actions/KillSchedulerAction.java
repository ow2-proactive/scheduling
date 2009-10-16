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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.gui.Activator;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;


/**
 * @author The ProActive Team
 */
public class KillSchedulerAction extends SchedulerGUIAction {
    private Shell shell = null;

    public KillSchedulerAction(Shell shell) {
        this.shell = shell;
        this.setText("Kill scheduler");
        this.setToolTipText("Kill the scheduler (this kill immediately the scheduler)");
        this.setImageDescriptor(Activator.getImageDescriptor("icons/scheduler_kill.png"));
        this.setEnabled(false);
    }

    @Override
    public void run() {
        if (MessageDialog.openConfirm(shell, "Confirm please",
                "Are you sure you want to Kill the scheduler ?")) {
            SchedulerProxy.getInstance().kill();
        }
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        if (connected && admin && (schedulerStatus != SchedulerStatus.UNLINKED))
            setEnabled(true);
        else
            setEnabled(false);
    }
}
