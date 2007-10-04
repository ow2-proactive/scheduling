/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.launcher.perspectives;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPerspectiveListener4;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.objectweb.proactive.ic2d.launcher.editors.xml.XMLEditor;
import org.objectweb.proactive.ic2d.launcher.views.InfoView;


public class LauncherPerspective implements IPerspectiveFactory,
    IPerspectiveListener4 {
    public static final String ID = "org.objectweb.proactive.ic2d.launcher.perspectives.LauncherPerspective";

    /** Bottom folder's id. */
    public static final String FI_BOTTOM = ID + ".bottomFolder";

    /** Right folder's id. */
    public static final String FI_RIGHT = ID + ".rightFolder";
    private IEditorDescriptor oldDefaultEditor;

    //
    // -- PUBLIC METHODS ----------------------------------------------
    //
    public void createInitialLayout(IPageLayout layout) {
        String editorAreaId = layout.getEditorArea();
        layout.setEditorAreaVisible(true);
        layout.setFixed(false);

        IFolderLayout rightFolder = layout.createFolder(FI_RIGHT,
                IPageLayout.RIGHT, 0.80f, editorAreaId);
        rightFolder.addView(InfoView.ID);

        IFolderLayout bottomFolder = layout.createFolder(FI_BOTTOM,
                IPageLayout.BOTTOM, 0.75f, editorAreaId);
        bottomFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);

        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                  .addPerspectiveListener(this);
    }

    public void perspectivePreDeactivate(IWorkbenchPage page,
        IPerspectiveDescriptor perspective) {
        // TODO Auto-generated method stub
    }

    public void perspectiveClosed(IWorkbenchPage page,
        IPerspectiveDescriptor perspective) {
        //If the closed perspective is the Launcher perspective
        if ((perspective.getId().compareTo(LauncherPerspective.ID) == 0) &&
                (this.oldDefaultEditor != null)) {
            restoreDefaultEditor();
        }
    }

    public void perspectiveDeactivated(IWorkbenchPage page,
        IPerspectiveDescriptor perspective) {
        if (perspective.getId().compareTo(LauncherPerspective.ID) == 0) {
            restoreDefaultEditor();
        }
    }

    public void perspectiveOpened(IWorkbenchPage page,
        IPerspectiveDescriptor perspective) {
        //If the opened perspective is the Launcher perspective
        if (perspective.getId().compareTo(LauncherPerspective.ID) == 0) {
            saveDefaultEditor();
        }
    }

    public void perspectiveSavedAs(IWorkbenchPage page,
        IPerspectiveDescriptor oldPerspective,
        IPerspectiveDescriptor newPerspective) {
        // TODO Auto-generated method stub
    }

    public void perspectiveChanged(IWorkbenchPage page,
        IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef,
        String changeId) {
        // TODO Auto-generated method stub
    }

    public void perspectiveActivated(IWorkbenchPage page,
        IPerspectiveDescriptor perspective) {
        if (perspective.getId().compareTo(LauncherPerspective.ID) == 0) {
            saveDefaultEditor();
        }
    }

    public void perspectiveChanged(IWorkbenchPage page,
        IPerspectiveDescriptor perspective, String changeId) {
        // TODO Auto-generated method stub
    }

    //
    // -- PRIVATE METHODS ---------------------------------------------
    //

    /**
     * Save the default XML editor,
     * and install the IC2D XML editor as default editor.
     */
    private synchronized void saveDefaultEditor() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return;
        }
        IEditorRegistry regEdit = workbench.getEditorRegistry();
        if (regEdit == null) {
            return;
        }
        if (this.oldDefaultEditor == null) {
            this.oldDefaultEditor = regEdit.getDefaultEditor("foo.xml");
        }
        System.out.println("Activator.start() " + workbench + " " +
            PlatformUI.getWorkbench());
        regEdit.setDefaultEditor("*.xml", XMLEditor.ID);
        System.out.println("Activator.start() " +
            regEdit.getDefaultEditor("toto.xml").getId());
    }

    /**
     * Restore the default XML editor
     */
    private synchronized void restoreDefaultEditor() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return;
        }
        IEditorRegistry regEdit = workbench.getEditorRegistry();
        if (regEdit == null) {
            return;
        }
        regEdit.setDefaultEditor("*.xml", this.oldDefaultEditor.getId());
        this.oldDefaultEditor = null;
    }
}
