package org.objectweb.proactive.ic2d.gui.components.model;

import java.util.List;

import org.objectweb.fractal.gui.model.Component;

/**
 * @author Matthieu Morel
 *
 */
public interface ProActiveComponent extends Component {
    
    public static final long VIRTUAL_NODE_INCORRECT_SYNTAX = 1 << 8; 

    public String getVirtualNode();
    
    public void setVirtualNode(String virtualNode);
    
    public String getExportedVirtualNodesAfterComposition();
    
    public String getExportedVirtualNodesBeforeComposition();
    
    public List getExportedVirtualNodesNames();
    
    public List getComposingVirtualNodes(String exportedVirtualNodeName);
    
    public String getComposingVirtualNodesAsString(String exportedVirtualNodeName);
    
    public void setComposingVirtualNodes(String virtualNodeName, String composingVirtualNodes);
    
    public void addExportedVirtualNode(String virtualNodeName, String composingVirtualNodes);
    
    public String getExportedVirtualNodeNameAfterComposition(String exportedVNName);
    
    public void removeExportedVirtualNode(String virtualNodeName);
    
    public boolean isParallel();
    
    public void setParallel();
    
    public void setCurrentlyEditedExportedVirtualNodeName(String exportedVirtualNodeName);
    
    public String getCurrentlyEditedExportedVirtualNodeName();
   
    public void setCurrentlyEditedComposingVirtualNodesNames(String composingVirtualNodesNames);
    
    public String getCurrentlyEditedComposingVirtualNodesNames();
    
   
    
    
}
