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

import javax.swing.table.AbstractTableModel;

import org.objectweb.fractal.gui.model.Component;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;


/**
 * A {@link javax.swing.table.TableModel} based on a {@link Component} model
 * that represents interfaces names, signatures... This model makes a conversion
 * from a {@link Component} model to a {@link javax.swing.table.TableModel}.
 */
public class ExportedVirtualNodesCompositionTableModel
    extends AbstractTableModel {

    /**
     * The component model on which this model is based.
     */
    private ProActiveComponent model;

    /**
     * Sets the component model on which this model is based.
     *
     * @param model a component.
     */
    void setComponentModel(final Component model) {
        this.model = (ProActiveComponent) model;
        fireTableDataChanged();
    }

    /**
     * Notifies this model that an exported virtual node has changed in {@link #model}.
     *
     * @param virtualNodeNameName the exported virtual node virtuathat has changed.
     * @param oldValue the old value of the composing virtual nodes.
     */
    void exportedVirtualNodeChanged(final String virtualNodeName,
        final String oldValue) {
        fireTableDataChanged();
    }

    // -------------------------------------------------------------------------
    // Implementation of the TableModel interface
    // -------------------------------------------------------------------------
    public int getRowCount() {
        if (model == null) {
            return 0;
        }
        return ((ProActiveComponent) model).getExportedVirtualNodesNames().size();
    }

    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(final int column) {
        //return column == 0 ? "Exported Virtual Node" : "Composing Virtual Nodes (syntax : component1.vn1;component2.vn2)";
        switch (column) {
        case 0:
            return "ExportedVirtualNode";
        case 1:
            return "Composing VirtualNodes";
        case 2:
            return "Composition Result";
        default:
            return null;
        }
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        if (columnIndex == 2) {
            return false;
            // composition result is not editable
        }
        return true;
    }

    public Object getValueAt(final int rowIndex, final int columnIndex) {
        Object exportedVirtualNodeName = model.getExportedVirtualNodesNames()
                                              .get(rowIndex);
        switch (columnIndex) {
        case 0:
            return exportedVirtualNodeName;
        case 1:
            return model.getComposingVirtualNodesAsString((String) exportedVirtualNodeName);
        case 2:
            return model.getExportedVirtualNodeNameAfterComposition((String) exportedVirtualNodeName);
        default:
            return null;
        }
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex,
        final int columnIndex) {
        try {
            //Thread.dumpStack();
            String exportedVirtualNodeName = (String) model.getExportedVirtualNodesNames()
                                                           .get(rowIndex);
            switch (columnIndex) {
            case 0: // changing exported vn
                String composingVirtualNodes = model.getComposingVirtualNodesAsString(exportedVirtualNodeName);
                model.removeExportedVirtualNode(exportedVirtualNodeName);
                model.addExportedVirtualNode((String) aValue,
                    composingVirtualNodes);
                break;
            case 1: // changing composing vns
                model.setComposingVirtualNodes(exportedVirtualNodeName,
                    (String) aValue);
                break;
            default:
                break;
            }
        } catch (IllegalArgumentException e) {
        }
    }
}
