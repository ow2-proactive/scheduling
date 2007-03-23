package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;




public interface IMAdmin extends Serializable {
	
    // FOR TESTING
    //-------------------------
    public StringWrapper echo();
    //-------------------------

    
    
    //----------------------------------------------------------------------//	
    // DEPLOY	
    public void deployAllVirtualNodes(File xmlDescriptor, Node remoteNode) throws Exception;
    public void deployVirtualNode(File xmlDescriptor, Node remoteNode, String vnName) throws Exception;
    public void deployVirtualNodes(File xmlDescriptor, Node remoteNode, String[] vnNames) throws Exception;
    
    
    //----------------------------------------------------------------------//	
    // GET THE DEPLOYED VNODES BY PAD
    
    /**
     * @return
     */
    public HashMap<String,ArrayList<VirtualNode>> getDeployedVirtualNodeByPad();
    


    //----------------------------------------------------------------------//	
    // REDEPLOY
    
    public void redeploy(String padName);
    public void redeploy(String padName, String vnName);
    public void redeploy(String padName, String[] vnNames);
    
    
    //----------------------------------------------------------------------//	
    // KILL
    
    public void killAll() throws ProActiveException;
    public void killPAD(String padName) throws ProActiveException;                   
    public void killPAD(String padName, String vnName);    
    public void killPAD(String padName, String[] vnNames); 
	

	
	//----------------------------------------------------------------------//
    // SHUTDOWN

    /**
     * Kill all ProActiveDescriptor and ResourceManager
     */
    public void shutdown();
}
