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
package org.objectweb.proactive.ic2d.chartit.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.chartit.data.ChartModelContainer;
import org.objectweb.proactive.ic2d.chartit.data.resource.ResourceData;
import org.objectweb.proactive.ic2d.chartit.data.resource.ResourceDataBuilder;


/**
 * This class handles the all the life-cycle of a <code>ChronologEditor</code>
 * and some additional information.
 * <p>
 * Typically an editor input is associated to a single resource.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ChartItDataEditorInput implements IEditorInput {
    /**
     * The associated resource
     */
    protected final ResourceData resourceData;
    /**
     * The list of controls to be enabled/disabled when the user is running some
     * graphs
     */
    protected final List<Control> controlsToDisable;

    /**
     * Creates a new instance of <code>StatsDataEditorInput</code>.
     * 
     * @param ressourceData
     *            The resource associated to this editor input
     */
    public ChartItDataEditorInput(final ResourceData ressourceData) {
        this.resourceData = ressourceData;
        this.controlsToDisable = new ArrayList<Control>();
    }

    /**
     * Creates a new instance of <code>StatsDataEditorInput</code>. With a
     * default local runtime resource.
     */
    public ChartItDataEditorInput() {
        this(ResourceDataBuilder.buildResourceDataForLocalRuntime());
    }

    /**
     * Adds a control to the disable list.
     * 
     * @param control
     *            The control to add to disable list
     */
    public void addControlToDisable(final Control control) {
        this.controlsToDisable.add(control);
    }

    /**
     * Removes a control from the disable list.
     * 
     * @param control
     *            The control to remove from disable list
     */
    public void removeControlToDisable(final Control control) {
        this.controlsToDisable.remove(control);
    }

    /**
     * Removes all controls from the disable list.
     */
    public void removeAllControlsToDisable() {
        this.controlsToDisable.clear();
    }

    /**
     * Enables or disables or registered controls.
     * 
     * @param enabled
     *            Executes the <code>setEnabled</code> on all registred
     *            controls.
     */
    public void setEnabledControls(final boolean enabled) {
        for (final Control control : this.controlsToDisable) {
            control.setEnabled(enabled);
        }
    }

    /**
     * Returns the model container of the resource associated to this editor input. 
     * 
     * @return The models container
     */
    public ChartModelContainer getModelsContainer() {
        return this.resourceData.getModelsContainer();
    }

    /**
     * Returns the resource data associated to this editor input.
     * 
     * @return the resource data
     */
    public ResourceData getResourceData() {
        return resourceData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    public boolean exists() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        return this.resourceData.getResourceDescriptor().getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        return getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        return null;
    }
}