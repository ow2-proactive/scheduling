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
import org.objectweb.proactive.ic2d.timit.views.TimeLineView;


/**
 * This action is used when the user stops the record. The time line view is then showed. 
 * @author vbodnart
 *
 */
public class StopRecordingTimeLineAction extends Action {
    public static final String STOP_RECORDING_TIMELINE_ACTION = "Stop Recording Time Line";
    private TimeLineView timeLineView;
    StartRecordingTimeLineAction startRecordingTimeLineAction;

    public StopRecordingTimeLineAction(StartRecordingTimeLineAction startRecordingTimeLineAction) {
        super.setId(STOP_RECORDING_TIMELINE_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/duration.gif"), null)));
        super.setToolTipText(STOP_RECORDING_TIMELINE_ACTION);
        super.setEnabled(false);
        this.startRecordingTimeLineAction = startRecordingTimeLineAction;
    }

    // @Override
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
        this.setEnabled(false);
        this.timeLineView.getContainer().stopRecordAndBuildChart();
        this.startRecordingTimeLineAction.setEnabled(true);
    }
}
