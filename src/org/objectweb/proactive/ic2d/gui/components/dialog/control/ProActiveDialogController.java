package org.objectweb.proactive.ic2d.gui.components.dialog.control;

import org.objectweb.fractal.gui.dialog.control.BasicDialogController;
import org.objectweb.proactive.ic2d.gui.components.dialog.model.ProActiveDialogModel;
import org.objectweb.proactive.ic2d.gui.components.dialog.view.ProActiveDialogViewListener;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;
import org.objectweb.proactive.ic2d.gui.components.util.Verifier;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveDialogController extends BasicDialogController implements ProActiveDialogViewListener{
    
    
    
    public void addExportedVirtualNodesCompositionButtonClicked () {
        ProActiveComponent component = (ProActiveComponent)configuration.getRootComponent();
        //int index = component.getExportedVirtualNodesNames().size();
        //component.addExportedVirtualNode(LinkedVirtualNode.EMPTY_VIRTUAL_NODE_NAME, ExportedVirtualNodesList.EMPTY_COMPOSING_VIRTUAL_NODES);
        if ((component.getCurrentlyEditedExportedVirtualNodeName() != null) && (component.getCurrentlyEditedComposingVirtualNodesNames()!= null)) {
            component.addExportedVirtualNode(component.getCurrentlyEditedExportedVirtualNodeName(), component.getCurrentlyEditedComposingVirtualNodesNames());
            component.setCurrentlyEditedComposingVirtualNodesNames(null);
            component.setCurrentlyEditedExportedVirtualNodeName(null);
        }
      }

      public void removeExportedVirtualNodesCompositionButtonClicked () {
        ProActiveComponent component = (ProActiveComponent)configuration.getRootComponent();
        int index = ((ProActiveDialogModel)model).getExportedVirtualNodesCompositionTableSelectionModel().getMinSelectionIndex();
        String name = (String)component.getExportedVirtualNodesNames().get(index);
        component.removeExportedVirtualNode(name);
      }


      public void checkExportedVirtualNodesCompositionButtonClicked() {
          Verifier.checkConsistencyOfExportedVirtualNodes(true);
      }

}
