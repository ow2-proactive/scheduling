/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesTabView;
import org.ow2.proactive.resourcemanager.gui.views.StatisticsView;


/**
 * The Resource Manager perspective
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class ResourceManagerPerspective implements IPerspectiveFactory {

    /** the id */
    public static final String ID = "org.ow2.proactive.resourcemanager.gui.ResourceManagerPerspective";

    /** Top folder's id. */
    public static final String ID_TOP_FOLDER = ID + ".topFolder";

    /** Left folder's id. */
    public static final String ID_LEFT_FOLDER = ID + ".leftFolder";

    /** Bottom folder's id. */
    public static final String ID_BOTTOM_FOLDER = ID + ".bottomFolder";

    // -------------------------------------------------------------------- //
    // ----------------- implements IPerspectiveFactory ------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {

        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(false);

        IFolderLayout topFolderTab = layout.createFolder(ID_TOP_FOLDER, IPageLayout.TOP, 0.7f, editorArea);
        topFolderTab.addView(ResourcesTabView.ID);

        IFolderLayout topFolderTree = layout.createFolder(ID_TOP_FOLDER, IPageLayout.TOP, 0.7f, editorArea);
        topFolderTree.addView(ResourceExplorerView.ID);

        IFolderLayout subleftFolder = layout.createFolder(ID_LEFT_FOLDER, IPageLayout.BOTTOM, 0.3f,
                editorArea);
        subleftFolder.addView(StatisticsView.ID);
        //
        //        IFolderLayout subsubleftFolder = layout.createFolder(ID_BOTTOM_FOLDER, IPageLayout.RIGHT, 0.5f,
        //                ID_LEFT_FOLDER);
        //        subsubleftFolder.addView(NodeInfoView.ID);
    }
}
