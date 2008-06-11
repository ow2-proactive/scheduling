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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chartit.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.chartit.data.resource.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chartit.editor.ChartItDataEditor;


/**
 * This action opens a ChartIt Editor using a resource provided by sub classes.
 * <p>
 * This action should be used properly inside an ActionSet. 
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public abstract class AbstractWorkbenchChartItAction extends Action implements IWorkbenchWindowActionDelegate {

    public static final String WORKBENCH_CHARTIT_ACTION = "WorkbenchChartItAction";

    /**
     * The instance of the resource descriptor created by sub classes
     */
    protected final IResourceDescriptor resourceDescriptor;

    /**
     * Creates a new instance of this class
     */
    public AbstractWorkbenchChartItAction() {
        super.setId(WORKBENCH_CHARTIT_ACTION);
        super.setToolTipText(WORKBENCH_CHARTIT_ACTION);
        super.setEnabled(true);
        this.resourceDescriptor = this.createResourceDescriptor();
    }

    /**
     * Subclasses provides an instance of a concrete class that implements a resource descriptor interface.
     * 
     * @return An instance of a concrete resource descriptor class
     */
    public abstract IResourceDescriptor createResourceDescriptor();

    @Override
    public final void run() {
        try {
            final IWorkbench iworkbench = PlatformUI.getWorkbench();
            final IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
            // Navigate through EditorReference->EditorInput then find the
            // Editor through ActivePage.findEditor(editorInputRef)
            // First list all EditorReferences
            for (final IEditorReference ref : currentWindow.getActivePage().getEditorReferences()) {
                if (ref.getEditorInput().getName().equals(resourceDescriptor.getName())) {
                    // If the Editor input was found activate it
                    currentWindow.getActivePage().activate(
                            currentWindow.getActivePage().findEditor(ref.getEditorInput()));
                    return;
                }
            }
            ChartItDataEditor.openNewFromResourceDescriptor(resourceDescriptor);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

    //
    // IWorkbenchWindowActionDelegate implementation
    //

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
    }

    public void run(IAction action) {
        this.run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

}
