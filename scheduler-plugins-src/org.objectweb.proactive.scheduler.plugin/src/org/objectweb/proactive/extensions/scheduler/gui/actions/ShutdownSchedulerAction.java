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
package org.objectweb.proactive.extensions.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.extensions.scheduler.gui.data.SchedulerProxy;


/**
 * @author The ProActive Team
 */
public class ShutdownSchedulerAction extends Action {
    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static ShutdownSchedulerAction instance = null;
    private Shell shell = null;

    private ShutdownSchedulerAction(Shell shell) {
        this.shell = shell;
        this.setText("Shutdown scheduler");
        this
                .setToolTipText("To shutdown the scheduler (This will finish all running and pending jobs before shutdown)");
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(),
                "icons/scheduler_shutdown.png"));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    @Override
    public void run() {
        if (MessageDialog.openConfirm(shell, "Confirm please",
                "Are you sure you want to shutting down the scheduler ?")) {
            SchedulerProxy.getInstance().shutdown();
        }
    }

    public static ShutdownSchedulerAction newInstance(Shell shell) {
        instance = new ShutdownSchedulerAction(shell);
        return instance;
    }

    public static ShutdownSchedulerAction getInstance() {
        return instance;
    }
}
