/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *  
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s): 
 * 
 * ################################################################
 */ 
package org.objectweb.proactive.ic2d.gui.components.admin.model;

import javax.swing.JOptionPane;

import org.objectweb.fractal.gui.admin.model.BasicAdminModel;
import org.objectweb.fractal.gui.model.Component;
import org.objectweb.fractal.gui.model.IllegalOperationException;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveConfigurationListener;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveVetoableConfigurationListener;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveAdminModel extends BasicAdminModel
    implements ProActiveConfigurationListener,
        ProActiveVetoableConfigurationListener {
    public void virtualNodeChanged(ProActiveComponent component, String oldValue) {
        // nothing to do (should never be called, because of veto)
    }

    public void exportedVirtualNodeChanged(ProActiveComponent component,
        String virtualNodeName, String oldValue) {
        // nothing to do (should never be called, because of veto)
    }

    // --------------------------
    public void canChangeVirtualNode(Component component, String virtualNode) {
        org.objectweb.fractal.api.Component ci = getInstance(component);
        if (ci != null) {
            //throw new IllegalOperationException(
            avert("Cannot change the virtual node of an instantiated component");
        }
    }

    public void canChangeExportedVirtualNode(ProActiveComponent component,
        String exportedVirtualNodes) {
        //avert("Sorry, in the current version, you have to change the export setting manually by editing the ADL files.");
        org.objectweb.fractal.api.Component ci = getInstance(component);

        if (ci != null) {
            //throw new IllegalOperationException(
            avert(
                "Cannot change and exported virtual node of an instantiated component");
        }
    }

    private void avert(String motif) {
        JOptionPane.showMessageDialog(null, motif, "Error",
            JOptionPane.ERROR_MESSAGE);
        throw new IllegalOperationException(motif);
    }
}
