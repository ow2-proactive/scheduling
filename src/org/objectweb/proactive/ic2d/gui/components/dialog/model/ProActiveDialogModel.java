package org.objectweb.proactive.ic2d.gui.components.dialog.model;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.text.Document;

import org.objectweb.fractal.gui.dialog.model.DialogModel;

/**
 * @author Matthieu Morel
 *
 */
public interface ProActiveDialogModel extends DialogModel {
    
    public Document getVirtualNodeTextFieldModel();
    
    Document getExportedVirtualNodeNameTextFieldModel();
    
    Document getComposingVirtualNodeNamesTextFieldModel();
    
    TableModel getExportedVirtualNodesCompositionTableModel();

    ListSelectionModel getExportedVirtualNodesCompositionTableSelectionModel();


}
