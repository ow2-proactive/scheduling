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
package org.ow2.proactive.resourcemanager.gui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.ow2.proactive.resourcemanager.gui.views.NodeInfoView;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesTabView;
import org.ow2.proactive.resourcemanager.gui.views.StatisticsView;


/**
 * The Resource Manager perspective
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public final class ResourceManagerPerspective implements IPerspectiveFactory {

    /** The perspective id */
    public static final String ID = "org.ow2.proactive.resourcemanager.gui.ResourceManagerPerspective";

    // -------------------------------------------------------------------- //
    // ----------------- implements IPerspectiveFactory ------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(final IPageLayout layout) {
        // Get identifier of the editor area of the page layout
        final String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(false);

        // The top folder will contain the table explorer view and the tree explorer view  
        final IFolderLayout topFolder = layout.createFolder("topLeft", IPageLayout.TOP, 0.3f, editorArea);
        topFolder.addView(ResourcesTabView.ID);
        topFolder.addView(ResourceExplorerView.ID);

        // The bottom left folder contains the statistics view
        final IFolderLayout bottomLeftFolder = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.7f,
                editorArea);
        bottomLeftFolder.addView(StatisticsView.ID);
        bottomLeftFolder.addView(NodeInfoView.ID);

        // The bottom left folder contains the compact view
        final IFolderLayout bottomRightFolder = layout.createFolder("bottomRight", IPageLayout.BOTTOM, 0.3f,
			editorArea);
        bottomRightFolder.addView(ResourcesCompactView.ID);

    }
}
