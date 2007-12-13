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

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.launcher.Activator;
import org.objectweb.proactive.ic2d.launcher.editors.PathEditorInput;
import org.objectweb.proactive.ic2d.launcher.files.XMLDescriptor;
import org.objectweb.proactive.ic2d.launcher.files.XMLDescriptorSet;
import org.objectweb.proactive.ic2d.launcher.perspectives.LauncherPerspective;


public class OpenFile implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow fWindow;

    //
    // -- PUBLIC METHODS ---------------------------------------------
    //
    public void dispose() {
        fWindow = null;
    }

    public void init(IWorkbenchWindow window) {
        fWindow = window;
    }

    /*
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        File file = queryFile();
        if (file != null) {
            IEditorInput input = createEditorInput(file);
            String editorId = getEditorId(file);
            IWorkbenchPage page = fWindow.getActivePage();
            try {
                page.openEditor(input, editorId);
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }
    }

    public void run(IAction action) {
        run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub
    }

    //
    // -- PRIVATE METHODS ---------------------------------------------
    //

    /**
     * Change the current perspective, to the Launcher perspective.
     */
    private void changePerspective() {
        IWorkbenchPage page = fWindow.getActivePage();
        IWorkbench workbench = fWindow.getWorkbench();
        IPerspectiveRegistry reg = workbench.getPerspectiveRegistry();
        page.setPerspective(reg.findPerspectiveWithId(LauncherPerspective.ID));
    }

    /**
     * Allows to the user of selectionner a file.
     * @return The selected file
     */
    private File queryFile() {
        FileDialog dialog = new FileDialog(fWindow.getShell(), SWT.OPEN);
        dialog.setText("Open File");
        String[] filterExt = { "*.xml", "*.*" };
        dialog.setFilterExtensions(filterExt);
        String path = dialog.open();
        if ((path != null) && (path.length() > 0)) {
            changePerspective();
            Console.getInstance(Activator.CONSOLE_NAME).log("File selected : " + path);
            XMLDescriptorSet.getInstance().addFile(new XMLDescriptor(path));
            return new File(path);
        } else {
            Console.getInstance(Activator.CONSOLE_NAME).warn("No file was selected");
        }
        return null;
    }

    private String getEditorId(File file) {
        IWorkbench workbench = fWindow.getWorkbench();
        IEditorRegistry editorRegistry = workbench.getEditorRegistry();
        IEditorDescriptor descriptor = editorRegistry.getDefaultEditor(file.getName());
        if (descriptor != null) {
            return descriptor.getId();
        }
        return "org.eclipse.ui.examples.rcp.texteditor.editors.SimpleEditor"; //$NON-NLS-1$
    }

    private IEditorInput createEditorInput(File file) {
        IPath location = new Path(file.getAbsolutePath());
        PathEditorInput input = new PathEditorInput(location);
        return input;
    }
}
