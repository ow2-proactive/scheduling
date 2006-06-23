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
package org.objectweb.proactive.ic2d.gui.components.dialog.model;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.text.Document;

import org.objectweb.fractal.gui.dialog.model.BasicDialogModel;
import org.objectweb.fractal.gui.model.Component;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveConfigurationListener;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveDialogModelImpl extends BasicDialogModel
    implements ProActiveDialogModel, ProActiveConfigurationListener {
    protected ProActiveTextFieldModel virtualNodeFieldModel;
    protected ProActiveTextFieldModel exportedVirtualNodeNameFieldModel;
    protected ProActiveTextFieldModel composingVirtualNodesNamesFieldModel;

    /**
     * The table model representing the exported virtual nodes.
     */
    protected ExportedVirtualNodesCompositionTableModel exportedVirtualNodesCompositionTableModel;

    /**
     * The table selection model representing the exported virtual nodes.
     */
    protected ListSelectionModel exportedVirtualNodesCompositionTableSelectionModel;

    /**
     *
     */
    public ProActiveDialogModelImpl() {
        super();
        exportedVirtualNodesCompositionTableModel = new ExportedVirtualNodesCompositionTableModel();
        exportedVirtualNodesCompositionTableSelectionModel = new DefaultListSelectionModel();
        exportedVirtualNodesCompositionTableSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        virtualNodeFieldModel = new ProActiveTextFieldModel(ProActiveTextFieldModel.VIRTUAL_NODE);
        exportedVirtualNodeNameFieldModel = new ProActiveTextFieldModel(ProActiveTextFieldModel.EXPORTED_VIRTUAL_NODE);
        composingVirtualNodesNamesFieldModel = new ProActiveTextFieldModel(ProActiveTextFieldModel.COMPOSING_VIRTUAL_NODES);
    }

    /**
     *
     */
    public void rootComponentChanged(Component arg0) {
        super.rootComponentChanged(arg0);
        final Component root = configuration.getRootComponent();
        exportedVirtualNodesCompositionTableModel.setComponentModel(root);
        virtualNodeFieldModel.setComponentModel(root);
        exportedVirtualNodeNameFieldModel.setComponentModel(root);
        composingVirtualNodesNamesFieldModel.setComponentModel(root);
    }

    public void virtualNodeChanged(final ProActiveComponent component,
        final String oldValue) {
        if (component == configuration.getRootComponent()) {
            //System.out.println("VIRTUAL NODE CHANGED : " + oldValue + " -->" + ((ProActiveComponent)component).getVirtualNode());
            virtualNodeFieldModel.componentTextChanged(((ProActiveComponent) component).getVirtualNode());
        }
    }

    public void exportedVirtualNodeChanged(final ProActiveComponent component,
        final String exportedVirtualNodeName, final String oldValue) {
        if (component == configuration.getRootComponent()) {
            exportedVirtualNodesCompositionTableModel.exportedVirtualNodeChanged(exportedVirtualNodeName,
                oldValue);
        }
    }

    public Document getVirtualNodeTextFieldModel() {
        return virtualNodeFieldModel;
    }

    /**
     *
     */
    public Document getComposingVirtualNodeNamesTextFieldModel() {
        return composingVirtualNodesNamesFieldModel;
    }

    /**
     *
     */
    public Document getExportedVirtualNodeNameTextFieldModel() {
        return exportedVirtualNodeNameFieldModel;
    }

    /**
     *
     */
    public TableModel getExportedVirtualNodesCompositionTableModel() {
        return exportedVirtualNodesCompositionTableModel;
    }

    /**
     *
     */
    public ListSelectionModel getExportedVirtualNodesCompositionTableSelectionModel() {
        return exportedVirtualNodesCompositionTableSelectionModel;
    }
}
