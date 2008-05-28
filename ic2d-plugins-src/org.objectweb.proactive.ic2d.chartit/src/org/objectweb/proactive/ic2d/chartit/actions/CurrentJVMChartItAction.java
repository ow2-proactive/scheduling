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
import org.objectweb.proactive.ic2d.chartit.data.resource.ResourceDataBuilder;
import org.objectweb.proactive.ic2d.chartit.editor.ChartItDataEditor;
import org.objectweb.proactive.ic2d.chartit.editor.ChartItDataEditorInput;


/**
 * This action opens a ChartIt Editor using the current JVM <code>Runtime MXBean</code> as a resource.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class CurrentJVMChartItAction extends Action implements IWorkbenchWindowActionDelegate {

    public static final String CURRENT_JVM_CHARTIT_ACTION = "CurrentJVMChartItAction";

    /**
     * Creates a new instance of this class
     */
    public CurrentJVMChartItAction() {
        super.setId(CURRENT_JVM_CHARTIT_ACTION);
        super.setToolTipText(CURRENT_JVM_CHARTIT_ACTION);
        super.setEnabled(true);
    }

    @Override
    public final void run() {
        try {
            final IWorkbench iworkbench = PlatformUI.getWorkbench();
            final IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
            // Navigate through EditorReference->EditorInput then find the
            // Editor through ActivePage.findEditor(editorInputRef)
            // First list all EditorReferences
            for (final IEditorReference ref : currentWindow.getActivePage().getEditorReferences()) {
                if (ref.getEditorInput().getName().equals(ResourceDataBuilder.DEFAULT_RESOURCE_NAME)) {
                    // If the Editor input was found activate it
                    currentWindow.getActivePage().activate(
                            currentWindow.getActivePage().findEditor(ref.getEditorInput()));
                    return;
                }
            }
            currentWindow.getActivePage()
                    .openEditor(new ChartItDataEditorInput(), ChartItDataEditor.ID, true);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

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
