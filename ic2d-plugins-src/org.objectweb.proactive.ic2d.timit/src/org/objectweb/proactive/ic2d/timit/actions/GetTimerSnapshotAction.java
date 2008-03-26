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
package org.objectweb.proactive.ic2d.timit.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.data.BasicChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.BasicChartObject;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


/**
 * This class defines an action that will be plugged to the
 * monitoring context menu.
 * After the user perform a click on an figure the associated model
 * reference will be provided to this action.
 * @author The ProActive Team
 *
 */
public class GetTimerSnapshotAction extends Action implements IActionExtPoint {
    public static final String GET_TIMER_SNAPSHOT = "Get timer snapshot";
    private AbstractData object;
    private BasicChartContainerObject container;

    public GetTimerSnapshotAction() {
        super.setId(GET_TIMER_SNAPSHOT);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/timer.gif"), null)));
        super.setToolTipText("Get timers snapshot from this object");
        super.setEnabled(false);
    }

    @Override
    public final void run() {
        ///IWorkbenchWindow currentWindow = null;
        IWorkbench iworkbench = PlatformUI.getWorkbench();
        IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = currentWindow.getActivePage();
        try {
            IViewPart part = page.showView("org.objectweb.proactive.ic2d.timit.views.TimItView");

            if (BasicChartObject.DEBUG) {
                new BasicChartObject(((TimItView) part).getChartContainer(), null, null);
                return;
            }

            // Pass the reference of the AbstractDataObject to the ChartContainerObject			
            if ((part != null) && part.getClass().equals(TimItView.class)) {
                if (this.container == null) {
                    this.container = ((TimItView) part).getChartContainer();
                }
                this.container.recognizeAndCreateChart(this.object);
            }

            this.object = null; // free the reference
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Implements IActionExtPoint setAbstractDataObject(AbstractDataObject) method
     */
    public final void setAbstractDataObject(final AbstractData object) {
        this.object = object;

        if (this.object instanceof WorldObject) {
            if (this.object.getMonitoredChildrenSize() != 0) {
                setText("Gather All Stats");
                setEnabled(true);
            }
        }
    }

    public void setActiveSelect(AbstractData ref) {
        if ((this.container != null) && (ref instanceof ActiveObject)) {
            BasicChartObject basicChartObject = this.container.getChartObjectById(((ActiveObject) ref)
                    .getUniqueID());
            if (basicChartObject != null) {
                basicChartObject.getEp().handleSelection(true);
            }
        }
    }
}
