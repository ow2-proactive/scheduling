/***
 * FractalGUI: a graphical tool to edit Fractal component configurations.
 * Copyright (C) 2003 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: fractal@objectweb.org
 *
 * Authors: Eric Bruneton, Patrice Fauvel
 */
package org.objectweb.proactive.ic2d.gui.components.history.model;

import org.objectweb.fractal.gui.history.model.BasicHistoryManager;

import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveConfigurationListener;


/**
 * Basic implementation of the {@link HistoryManager} interface.
 */
public class ProActiveHistoryManager extends BasicHistoryManager
    implements ProActiveConfigurationListener {

    /* (non-Javadoc)
     * @see org.objectweb.fractal.gui.model.ConfigurationListener#virtualNodeChanged(org.objectweb.fractal.gui.model.Component, java.lang.String)
     */
    public void virtualNodeChanged(ProActiveComponent component, String oldValue) {
        // does nothing
    }

    public void exportedVirtualNodeChanged(ProActiveComponent component,
        String virtualNodeName, String oldValue) {
        // does nothing
    }
}
