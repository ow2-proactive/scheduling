package org.objectweb.proactive.ic2d.gui.components.dialog.view;

import org.objectweb.fractal.gui.dialog.view.DialogViewListener;

/**
 * @author Matthieu Morel
 *
 */
public interface ProActiveDialogViewListener extends DialogViewListener {
    
    void addExportedVirtualNodesCompositionButtonClicked();


    void removeExportedVirtualNodesCompositionButtonClicked();
    
    void checkExportedVirtualNodesCompositionButtonClicked();

}
