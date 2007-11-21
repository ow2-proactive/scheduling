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
package org.objectweb.proactive.ic2d.security.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.objectweb.proactive.ic2d.security.views.PolicyEditorView;


public class SecurityPerspective implements IPerspectiveFactory {
    public static final String ID = "org.objectweb.proactive.ic2d.security.perspectives.SecurityPerspective";

    /** Top folder's id. */
    public static final String FI_TOP = ID + ".topFolder";

    //
    // -- PUBLIC METHODS ----------------------------------------------
    //
    public void createInitialLayout(IPageLayout layout) {
        String editorAreaId = layout.getEditorArea();
        layout.setEditorAreaVisible(false);
        layout.setFixed(false);

        IFolderLayout topFolder = layout.createFolder(FI_TOP, IPageLayout.TOP,
                0.75f, editorAreaId);
        topFolder.addView(PolicyEditorView.ID);
        topFolder.addPlaceholder("org.objectweb.proactive.ic2d.security.*");
    }
}
