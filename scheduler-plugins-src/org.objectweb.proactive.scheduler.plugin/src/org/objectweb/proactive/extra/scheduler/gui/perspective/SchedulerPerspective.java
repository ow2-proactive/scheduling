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
package org.objectweb.proactive.extra.scheduler.gui.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.objectweb.proactive.extra.scheduler.gui.views.JobInfo;
import org.objectweb.proactive.extra.scheduler.gui.views.ResultPreview;
import org.objectweb.proactive.extra.scheduler.gui.views.SeparatedJobView;
import org.objectweb.proactive.extra.scheduler.gui.views.TaskView;


/**
 * The scheduler perspective
 *
 * @author ProActive Team
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class SchedulerPerspective implements IPerspectiveFactory {

    /** the id */
    public static final String ID = "org.objectweb.proactive.extra.scheduler.gui.perspective.SchedulerPerspective";

    /** Top folder's id. */
    public static final String ID_TOP_FOLDER = ID + ".topFolder";

    /** Bottom folder's id. */
    public static final String ID_LEFT_FOLDER = ID + ".leftFolder";

    /** Right folder's id. */
    public static final String ID_BOTTOM_FOLDER = ID + ".bottomFolder";

    // -------------------------------------------------------------------- //
    // ----------------- implements IPerspectiveFactory ------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(false);

        IFolderLayout topFolder = layout.createFolder(ID_TOP_FOLDER,
                IPageLayout.TOP, 0.62f, editorArea);
        topFolder.addView(SeparatedJobView.ID);

        IFolderLayout leftFolder = layout.createFolder(ID_LEFT_FOLDER,
                IPageLayout.LEFT, 0.72f, editorArea);
        leftFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
        leftFolder.addView(TaskView.ID);

        IFolderLayout bottomFolder = layout.createFolder(ID_BOTTOM_FOLDER,
                IPageLayout.BOTTOM, 0.28f, editorArea);
        bottomFolder.addView(JobInfo.ID);
        bottomFolder.addView(ResultPreview.ID);
    }
}
// bottomFolder.addPlaceholder("org.objectweb.proactive.ic2d.monitoring.views.MonitoringView:org.objectweb.proactive.ic2d.monitoring.views.MonitoringView*");
// bottomFolder.addPlaceholder("org.objectweb.proactive.ic2d.dgc.*");
