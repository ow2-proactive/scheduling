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
package org.objectweb.proactive.ic2d.launcher.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.launcher.Activator;
import org.objectweb.proactive.ic2d.launcher.perspectives.LauncherPerspective;


public class SaveFile extends Action implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;

    //
    // -- PUBLIC METHODS ---------------------------------------------
    //
    public void dispose() {
        window = null;
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
        this.setEnabled(false);
    }

    public void run(IAction action) {
        IWorkbenchPage page = window.getActivePage();
        IEditorPart editor = page.getActiveEditor();
        boolean dirty = editor.isDirty();
        page.saveEditor(editor, false);
        if (dirty) {
            Console.getInstance(Activator.CONSOLE_NAME).log("File saved");
        } else {
            Console.getInstance(Activator.CONSOLE_NAME).log("File already saved");
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(goodContext());
    }

    //
    // -- PRIVATE METHODS ---------------------------------------------
    //

    /**
     * @return True if the Launcher perspective is open, and if a file is selected, false otherwise.
     */
    private boolean goodContext() {
        boolean goodPerspective;
        IWorkbenchPage page = window.getActivePage();
        IWorkbench workbench = window.getWorkbench();
        IPerspectiveRegistry reg = workbench.getPerspectiveRegistry();
        goodPerspective = page.getPerspective()
                              .equals(reg.findPerspectiveWithId(
                    LauncherPerspective.ID));

        return (goodPerspective && (page.getEditorReferences().length > 0));
    }
}
