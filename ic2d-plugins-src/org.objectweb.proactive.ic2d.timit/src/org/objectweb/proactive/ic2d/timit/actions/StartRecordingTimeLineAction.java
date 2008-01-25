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
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.data.BasicChartContainerObject;
import org.objectweb.proactive.ic2d.timit.views.TimeLineView;


/**
 * This action is used when the user starts the recording events in order to see them in the time line view. 
 * @author vbodnart
 *
 */
public class StartRecordingTimeLineAction extends Action {
    public static final String START_RECORDING_TIMELINE_ACTION = "Start Recording Time Line";
    private StopRecordingTimeLineAction stopRecordingTimeLineAction;
    private BasicChartContainerObject sourceContainer;
    private TimeLineView timeLineView;

    public StartRecordingTimeLineAction() {
        super.setId(START_RECORDING_TIMELINE_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/start_rec.gif"), null)));
        super.setToolTipText(START_RECORDING_TIMELINE_ACTION);
        super.setEnabled(false);
    }

    public final void setTarget(final BasicChartContainerObject container) {
        super.setEnabled(true);
        this.sourceContainer = container;
    }

    public void setStopRecordingTimeLineAction(StopRecordingTimeLineAction stopRecordingTimeLineAction) {
        this.stopRecordingTimeLineAction = stopRecordingTimeLineAction;
    }

    @Override
    public final void run() {
        if (this.timeLineView == null) {
            IWorkbench iworkbench = PlatformUI.getWorkbench();
            IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
            IWorkbenchPage page = currentWindow.getActivePage();
            try {
                IViewPart part = page.showView("org.objectweb.proactive.ic2d.timit.views.TimeLineView");
                this.timeLineView = (TimeLineView) part;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.timeLineView.getContainer().provideSourceContainer(this.sourceContainer);
        this.setEnabled(false);
        this.stopRecordingTimeLineAction.setEnabled(true);
    }
}
