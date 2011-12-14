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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.composite.StatusLabel;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.views.SeparatedJobView;


public class DisconnectAction extends SchedulerGUIAction {

    public DisconnectAction() {
        this.setText("&Disconnect");
        this.setToolTipText("Disconnect from the ProActive Scheduler");
        this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_DISCONNECT));
    }

    @Override
    public void run() {
        if (MessageDialog.openConfirm(getParent().getShell(), "Confirm disconnection",
                "Are you sure you want to disconnect from the ProActive Scheduler ?")) {
            disconnection(false);
        }
    }

    public static void disconnection(boolean serverIsDown) {
        StatusLabel.getInstance().disconnect();
        // stop log server
        try {
            Activator.terminateLoggerServer();
        } catch (LogForwardingException e) {
            e.printStackTrace();
        }

        try {
            // Disconnect the JMX client of ChartIt
            JMXActionsManager.getInstance().disconnectJMXClient();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        try {
            SchedulerProxy.getInstance().disconnect(serverIsDown);
        } catch (Throwable th) {
        	th.printStackTrace();
        }
        SchedulerProxy.clearInstance();

        SeparatedJobView.clearOnDisconnection();
        ActionsManager.getInstance().update();
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus chedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        super.setEnabled(connected);
    }
}
