package org.objectweb.proactive.ic2d.gui.components.model;

import org.objectweb.fractal.gui.model.BasicComponent;
import org.objectweb.fractal.gui.model.SharedComponent;
import org.objectweb.proactive.core.ProActiveRuntimeException;

/**
 * @author Matthieu Morel
 *
 */
public class ProActiveSharedComponent extends SharedComponent {
    
    protected String virtualNode;
    
    /**
     * @param arg0
     */
    public ProActiveSharedComponent(BasicComponent arg0) {
        super(arg0);
    }

    public String getVirtualNode() {
  	  return ((ProActiveComponent)masterComponent).getVirtualNode();
    }
    
    public void setVirtualNode(String virtualNode) {
        ((ProActiveComponent)masterComponent).setVirtualNode(virtualNode);
    }
    
    
    public void setExportedVirtualNodes(String exportedVirtualNodes) {
        throw new ProActiveRuntimeException("not yet implemented");
        //((ProActiveComponent)masterComponent).setExportedVirtualNodes(null);
    }

    public String getExportedVirtualNodes() {
        return ((ProActiveComponent)masterComponent).getExportedVirtualNodesAfterComposition();
    }



}
