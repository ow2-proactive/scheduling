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
package org.objectweb.proactive.ic2d.p2PMonitoring.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.objectweb.proactive.ic2d.p2PMonitoring.dialog.DumpP2PNetworkDialog;
import org.objectweb.proactive.ic2d.p2PMonitoring.views.P2PView;


public class DumpP2PNetworkAction implements IWorkbenchWindowActionDelegate {
    private Display display;

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
        this.display = window.getShell().getDisplay();
    }

    public void run(IAction action) {
        DumpP2PNetworkDialog d = new DumpP2PNetworkDialog(display.getActiveShell());
        System.out.println("DumpP2PNetworkAction.run() I should monitor " + d.getUrl());
        System.out.println("DumpP2PNetworkAction.run() display is " + display);
        P2PView.dumpP2PNetwork(d.getUrl());
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub
    }
}
