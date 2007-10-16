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
package org.objectweb.proactive.ic2d.launcher.editors.xml;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.launcher.Activator;
import org.objectweb.proactive.ic2d.launcher.files.XMLDescriptor;
import org.objectweb.proactive.ic2d.launcher.files.XMLDescriptorSet;
import org.objectweb.proactive.ic2d.launcher.perspectives.LauncherPerspective;


public class XMLEditor extends TextEditor {
    public static final String ID = "org.objectweb.proactive.ic2d.launcher.editors.xml.XMLEditor";
    private ColorManager colorManager;

    //
    // -- CONSTRUCTORS ---------------------------------------------
    //
    public XMLEditor() {
        super();
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new XMLConfiguration(colorManager));
        setDocumentProvider(new XMLDocumentProvider());
    }

    //
    // -- PUBLIC METHODS ---------------------------------------------
    //
    public void dispose() {
        colorManager.dispose();
        super.dispose();
    }

    //
    // -- PROTECTED METHODS ---------------------------------------------
    //
    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        super.doSetInput(input);
        String path = null;
        if (input instanceof IPathEditorInput) {
            path = ((IPathEditorInput) input).getPath().toOSString();
            changePerspective();
            XMLDescriptorSet.getInstance().addFile(new XMLDescriptor(path));
            Console.getInstance(Activator.CONSOLE_NAME)
                   .log("File selected : " + path);
            System.out.println("XMLEditor.doSetInput() " +
                this.getEditorSite().getPage());
        }
    }

    //
    // -- PRIVATE METHODS ---------------------------------------------
    //

    /**
     * Change the current perspective, to the Launcher perspective.
     */
    private void changePerspective() {
        IEditorSite site = getEditorSite();
        if (site == null) {
            return;
        }
        IWorkbenchPage page = site.getPage();
        if (page == null) {
            return;
        }
        IWorkbenchWindow workbenchWindow = page.getWorkbenchWindow();
        if (workbenchWindow == null) {
            return;
        }
        IWorkbench workbench = workbenchWindow.getWorkbench();
        if (workbench == null) {
            return;
        }
        IPerspectiveRegistry reg = workbench.getPerspectiveRegistry();
        if (reg == null) {
            return;
        }
        page.setPerspective(reg.findPerspectiveWithId(LauncherPerspective.ID));
    }
}
