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
package org.objectweb.proactive.ic2d.jmxmonitoring.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.Legend;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public class MonitoringPerspective implements IPerspectiveFactory {
    public static final String ID = "org.objectweb.proactive.ic2d.jmxmonitoring.perspective.MonitoringPerspective";

    /** Top folder's id. */
    public static final String FI_TOP = ID + ".topFolder";

    /** Bottom folder's id. */
    public static final String FI_BOTTOM = ID + ".bottomFolder";

    /** Right folder's id. */
    public static final String FI_RIGHT = ID + ".rightFolder";

    //
    // -- PUBLIC METHODS ----------------------------------------------
    //
    public void createInitialLayout(IPageLayout layout) {
        String editorAreaId = layout.getEditorArea();
        layout.setEditorAreaVisible(false);
        layout.setFixed(false);

        IFolderLayout rightFolder = layout.createFolder(FI_RIGHT,
                IPageLayout.RIGHT, 0.80f, editorAreaId);
        //rightFolder.addPlaceholder(Legend.ID);
        rightFolder.addView(Legend.ID);

        IFolderLayout topFolder = layout.createFolder(FI_TOP, IPageLayout.TOP,
                0.75f, editorAreaId);
        //topFolder.addPlaceholder(MonitoringView.ID);
        topFolder.addView(MonitoringView.ID);
        topFolder.addPlaceholder(
            "org.objectweb.proactive.ic2d.jmxmonitoring.views.MonitoringView:org.objectweb.proactive.ic2d.jmxmonitoring.views.MonitoringView*");
        topFolder.addPlaceholder("org.objectweb.proactive.ic2d.dgc.*");

        IFolderLayout bottomFolder = layout.createFolder(FI_BOTTOM,
                IPageLayout.BOTTOM, 0.20f, editorAreaId);
        //bottomFolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
        bottomFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
    }
}
