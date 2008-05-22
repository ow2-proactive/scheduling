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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.chartit.Activator;
import org.objectweb.proactive.ic2d.chartit.data.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chartit.data.ResourceData;
import org.objectweb.proactive.ic2d.chartit.data.ResourceDataBuilder;
import org.objectweb.proactive.ic2d.chartit.editors.ChartItDataEditor;
import org.objectweb.proactive.ic2d.chartit.editors.ChartItDataEditorInput;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;


/**
 * This action allows the user to open a ChartIt editor using as input a resource provided externally.  
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ChartItAction extends Action implements IActionExtPoint {

    /**
     * The text displayed by this action
     */
    public static final String SHOW_IN_CHARTIT_VIEW_ACTION = "Show in ChartIt View";

    /**
     * The target data
     */
    private AbstractData target;

    /**
     * Creates a new instance of <code>ChartItAction</code>.
     */
    public ChartItAction() {
        super.setId(SHOW_IN_CHARTIT_VIEW_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/graph.gif"), null)));
        super.setToolTipText(SHOW_IN_CHARTIT_VIEW_ACTION);
        super.setText(SHOW_IN_CHARTIT_VIEW_ACTION);
        super.setEnabled(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint#setAbstractDataObject(org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData)
     */
    public void setAbstractDataObject(final AbstractData object) {
        if (object.getClass() == WorldObject.class || object.getClass() == HostObject.class)
            return;
        this.target = object;
        super.setEnabled(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint#setActiveSelect(org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData)
     */
    public void setActiveSelect(final AbstractData ref) {
        this.activateIfFound(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), ref.getName());
    }

    /**
     * Activates an editor by name.
     * 
     * @param abstractDataRef
     * @return
     */
    private boolean activateIfFound(final IWorkbenchWindow currentWindow, final String name) {
        try {
            // Navigate through EditorReference->EditorInput then find the
            // Editor through ActivePage.findEditor(editorInputRef)
            // First list all EditorReferences
            for (final IEditorReference ref : currentWindow.getActivePage().getEditorReferences()) {
                if (ref.getEditorInput().getName().equals(name)) {
                    // If the Editor input was found activate it
                    currentWindow.getActivePage().activate(
                            currentWindow.getActivePage().findEditor(ref.getEditorInput()));
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        try {
            // Get the current window instance
            final IWorkbenchWindow currentWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (this.target != null && !activateIfFound(currentWindow, this.target.getName())) {
                // First build a ResourceDescriptor
                final IResourceDescriptor resourceDescriptor = new AbstractDataDescriptor(this.target);
                final ResourceData resourceData = ResourceDataBuilder
                        .buildResourceDataFromDescriptor(resourceDescriptor);
                currentWindow.getActivePage().openEditor(new ChartItDataEditorInput(resourceData),
                        ChartItDataEditor.ID, true);
            }
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Could not initiate the edit part : " + e.getMessage());
        }
    }

    /**
     * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
     * 
     */
    final class AbstractDataDescriptor implements IResourceDescriptor {
        final AbstractData ad;

        public AbstractDataDescriptor(final AbstractData ad) {
            this.ad = ad;
        }

        public String getHostUrlServer() {
            return this.ad.getHostUrlServer();
        }

        public MBeanServerConnection getMBeanServerConnection() {
            return this.ad.getMBeanServerConnection();
        }

        public String getName() {
            return this.ad.getName();
        }

        public ObjectName getObjectName() {
            return this.ad.getObjectName();
        }

    }
}