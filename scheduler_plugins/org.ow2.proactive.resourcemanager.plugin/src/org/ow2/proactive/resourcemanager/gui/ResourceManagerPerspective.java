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
package org.ow2.proactive.resourcemanager.gui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
import org.ow2.proactive.resourcemanager.gui.views.StatisticsView;


/**
 * The scheduler perspective
 *
 * @author The ProActive Team
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class ResourceManagerPerspective implements IPerspectiveFactory {

    /** the id */
    public static final String ID = "org.ow2.proactive.scheduler.gui.ResourceManagerPerspective";

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

        IFolderLayout topFolder = layout.createFolder(ID_TOP_FOLDER, IPageLayout.TOP, 0.7f, editorArea);
        topFolder.addView(ResourceExplorerView.ID);

        IFolderLayout subleftFolder = layout.createFolder(ID_LEFT_FOLDER, IPageLayout.BOTTOM, 0.3f,
                editorArea);
        subleftFolder.addView(StatisticsView.ID);
        //
        //        IFolderLayout subsubleftFolder = layout.createFolder(ID_BOTTOM_FOLDER, IPageLayout.RIGHT, 0.5f,
        //                ID_LEFT_FOLDER);
        //        subsubleftFolder.addView(NodeInfoView.ID);
    }
}
