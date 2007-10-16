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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.objectweb.proactive.ic2d.p2PMonitoring.views.P2PView;


public class DumpFileChooserAction implements IWorkbenchWindowActionDelegate {
    private Display display;

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
        this.display = window.getShell().getDisplay();
    }

    public void run(IAction action) {
        //	new MonitorNewHostDialog(display.getActiveShell(), Protocol.RMI);
        //new LoadFileDialog(display.getActiveShell(), Protocol.RMI);
        FileDialog fd = new FileDialog(display.getActiveShell());
        fd.setText("Open P2P dump file");
        String filename = fd.open();
        if (filename != null) {
            // loadImage(filename);
            //         String currentDir = fd.getFilterPath();
            System.out.println("Opening " + filename);
            P2PView.loadDumpFile(filename);
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }
}
