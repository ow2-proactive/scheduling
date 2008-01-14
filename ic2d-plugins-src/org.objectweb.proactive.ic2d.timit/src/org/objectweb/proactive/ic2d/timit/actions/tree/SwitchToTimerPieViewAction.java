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
package org.objectweb.proactive.ic2d.timit.actions.tree;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.timit.editors.PieChartEditor;
import org.objectweb.proactive.ic2d.timit.editparts.tree.TimerEditPart;


/**
 * This action is used to show an editor that contains a pie chart.
 * @author vbodnart
 *
 */
public class SwitchToTimerPieViewAction extends Action {
    public static final String SWITCH_TO_TIMER_PIE_VIEW = "Switch to Timer Pie View";
    private TimerEditPart target;

    public SwitchToTimerPieViewAction() {
        super.setId(SWITCH_TO_TIMER_PIE_VIEW);
        super.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "piechart.gif"));
        super.setToolTipText(SWITCH_TO_TIMER_PIE_VIEW);
        super.setEnabled(false);
    }

    public final void setTarget(TimerEditPart target) {
        if (target == null) {
            this.setEnabled(false);
            return;
        }
        this.target = target;
        super.setEnabled(true);
    }

    @Override
    public final void run() {
        try {
            IWorkbench iworkbench = PlatformUI.getWorkbench();
            IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
            currentWindow.getActivePage().openEditor(target, PieChartEditor.ID);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }
}
