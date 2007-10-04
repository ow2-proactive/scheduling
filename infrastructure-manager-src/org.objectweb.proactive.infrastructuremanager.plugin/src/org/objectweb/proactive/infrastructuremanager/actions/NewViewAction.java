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
package org.objectweb.proactive.infrastructuremanager.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.infrastructuremanager.views.IMViewInfrastructure;


public class NewViewAction extends Action
    implements IWorkbenchWindowActionDelegate {
    private static int index = 0;
    public static final String NEW_VIEW = "NewInfrastructureView";

    public NewViewAction() {
        this.setId(NEW_VIEW);
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "newview.gif"));
        this.setText("New Infrastructure View");
        this.setToolTipText("New Infrastructure View");
    }

    public void dispose() {
        // TODO Auto-generated method stub
    }

    public void init(IWorkbenchWindow window) {
        // TODO Auto-generated method stub
    }

    public void run(IAction action) {
        this.run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub
    }

    @Override
    public void run() {
        try {
            IMViewInfrastructure view = (IMViewInfrastructure) PlatformUI.getWorkbench()
                                                                         .getActiveWorkbenchWindow()
                                                                         .getActivePage()
                                                                         .showView(IMViewInfrastructure.ID,
                    IMViewInfrastructure.ID + "#" + (++index),
                    IWorkbenchPage.VIEW_ACTIVATE);
            if (view.getPartName().equals("Infrastructure")) {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                          .getActivePage().hideView(view);
            }
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
