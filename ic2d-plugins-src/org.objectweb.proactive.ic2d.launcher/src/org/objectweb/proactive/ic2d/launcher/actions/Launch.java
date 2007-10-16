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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.objectweb.proactive.core.descriptor.Launcher;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.launcher.Activator;
import org.objectweb.proactive.ic2d.launcher.editors.PathEditorInput;
import org.objectweb.proactive.ic2d.launcher.exceptions.TagMissingException;
import org.objectweb.proactive.ic2d.launcher.files.XMLDescriptor;
import org.objectweb.proactive.ic2d.launcher.files.XMLDescriptorSet;
import org.objectweb.proactive.ic2d.launcher.perspectives.LauncherPerspective;


public class Launch extends Action implements IWorkbenchWindowActionDelegate {
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
        if (page == null) {
            return;
        }
        IEditorPart editorPart = page.getActiveEditor();
        if (editorPart == null) {
            return;
        }
        String path = ((PathEditorInput) editorPart.getEditorInput()).getPath()
                       .toString();

        launch(window.getActivePage(), path);
    }

    /**
     * Launch an XML file. If the file is not saved, a pop-up will display.
     * @param page
     * @param path The file's path.
     */
    public static void launch(IWorkbenchPage page, final String path) {
        if (page == null) {
            return;
        }

        IEditorReference[] editorReference = page.getEditorReferences();
        IEditorReference editor = null;
        for (int i = 0, size = editorReference.length; i < size; i++) {
            if (editorReference[i].getName().compareTo(path) == 0) {
                editor = editorReference[i];
                break;
            }
        }

        //  If the file editor is open.(And maybe not saved)
        if (editor != null) {
            IEditorPart editorPart = editor.getEditor(false);

            boolean wasDirty = editorPart.isDirty();

            // Ask to the user, if he wants save his file (if it is not already saved).
            // If the user chooses 'cancel', we don't run the xml file
            boolean succeeded = page.saveEditor(editorPart, true);
            if (!succeeded) {
                return;
            }
            if (wasDirty && !editorPart.isDirty()) {
                Console.getInstance(Activator.CONSOLE_NAME).log("File saved");
            }
        }

        Launcher launcher = null;

        // creates the launcher
        try {
            launcher = new Launcher(path);
            XMLDescriptorSet.getInstance().getFile(path).setLauncher(launcher);
        } catch (TagMissingException e) {
            XMLDescriptorSet.getInstance().getFile(path)
                            .setState(XMLDescriptor.FileState.ERROR);
            Console.getInstance(Activator.CONSOLE_NAME).debug(e);
            return;
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME).logException(e);
        }

        // activate the launcher
        try {
            if (!launcher.isActivated()) {
                final Launcher l = launcher;
                Thread thread = new Thread(new Runnable() {
                            public void run() {
                                try {
                                    l.activate();
                                    Console.getInstance(Activator.CONSOLE_NAME)
                                           .log(path + " - activated");
                                    Console.getInstance(Activator.CONSOLE_NAME)
                                           .log("Open the log4j console for more details");
                                    Display.getDefault().asyncExec(new Runnable() {
                                            public void run() {
                                                XMLDescriptorSet.getInstance()
                                                                .getFile(path)
                                                                .setState(XMLDescriptor.FileState.LAUNCHED);
                                            }
                                        });
                                } catch (Exception e) {
                                    Console.getInstance(Activator.CONSOLE_NAME)
                                           .logException(e);
                                    XMLDescriptorSet.getInstance().getFile(path)
                                                    .setState(XMLDescriptor.FileState.ERROR);
                                }
                            }
                        });
                thread.start();
            }
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME).logException(e);
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
