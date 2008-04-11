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
package org.objectweb.proactive.ic2d.chronolog.actions;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.chronolog.Activator;
import org.objectweb.proactive.ic2d.chronolog.data.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chronolog.data.ResourceData;
import org.objectweb.proactive.ic2d.chronolog.data.ResourceDataBuilder;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditor;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditorInput;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;


/**
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ChronologAction extends Action implements IActionExtPoint {

    public static final String SHOW_IN_CHRONOLOG_VIEW_ACTION = "Show in Chronolog View";
    private AbstractData target;
    private final IWorkbenchWindow currentWindow;

    /**
     * Creats a new instance of <code>ChronologAction</code>.
     */
    public ChronologAction() {
        super.setId(SHOW_IN_CHRONOLOG_VIEW_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/graph.gif"), null)));
        super.setToolTipText(SHOW_IN_CHRONOLOG_VIEW_ACTION);
        super.setText(SHOW_IN_CHRONOLOG_VIEW_ACTION);
        super.setEnabled(false);
        this.currentWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
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
        this.activateIfFound(ref);
    }

    /**
     * @param abstractDataRef
     * @return
     */
    private boolean activateIfFound(final AbstractData abstractDataRef) {
        try {
            // Navigate through EditorReference->EditorInput then find the
            // Editor through ActivePage.findEditor(editorInputRef)
            // First list all EditorReferences
            for (final IEditorReference ref : currentWindow.getActivePage().getEditorReferences()) {
                if (ref.getEditorInput().getName().equals(abstractDataRef.getName())) {
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
            if (this.target != null && !activateIfFound(this.target)) {
                // First build a ResourceDescriptor
                final IResourceDescriptor resourceDescriptor = new AbstractDataDescriptor(this.target);
                final ResourceData resourceData = ResourceDataBuilder
                        .buildResourceDataFromDescriptor(resourceDescriptor);
                currentWindow.getActivePage().openEditor(new ChronologDataEditorInput(resourceData),
                        ChronologDataEditor.ID, true);
            }
        } catch (PartInitException e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Could not initiate the edit part : PartInitException");
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