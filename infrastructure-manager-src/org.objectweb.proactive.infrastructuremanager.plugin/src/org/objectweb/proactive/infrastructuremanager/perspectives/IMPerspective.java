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
package org.objectweb.proactive.infrastructuremanager.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.objectweb.proactive.infrastructuremanager.views.IMViewAdministration;
import org.objectweb.proactive.infrastructuremanager.views.IMViewLegend;


public class IMPerspective implements IPerspectiveFactory {
    public static final String ID = "org.objectweb.proactive.infrastructuremanager.gui.perspectives.IMPerspective";

    /** Left folder's id. */
    public static final String FI_LEFT_ADMIN = ID + ".leftAdminFolder";
    public static final String FI_LEFT_LEGEND = FI_LEFT_ADMIN +
        ".leftLegendFolder";

    /** Top folder's id. */
    public static final String FI_TOP = ID + ".topFolder";

    /** Bottom folder's id. */
    public static final String FI_BOTTOM = ID + ".bottomFolder";

    public void createInitialLayout(IPageLayout layout) {
        String editorAreaId = layout.getEditorArea();
        layout.setEditorAreaVisible(false);
        layout.setFixed(false);

        IFolderLayout leftAdminFolder = layout.createFolder(FI_LEFT_ADMIN,
                IPageLayout.LEFT, 0.30f, editorAreaId);
        leftAdminFolder.addView(IMViewAdministration.ID);

        IFolderLayout leftLegendFolder = layout.createFolder(FI_LEFT_LEGEND,
                IPageLayout.BOTTOM, 0.50f, FI_LEFT_ADMIN);
        leftLegendFolder.addView(IMViewLegend.ID);

        IFolderLayout bottomFolder = layout.createFolder(FI_BOTTOM,
                IPageLayout.BOTTOM, 0.75f, editorAreaId);
        bottomFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
    }
}
