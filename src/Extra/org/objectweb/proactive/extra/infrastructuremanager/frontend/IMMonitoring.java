package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;



public interface IMMonitoring {


	// pour tester
	public String echo();
	
	
    /**
     * @return all descriptors load by the IM
     */
    public HashMap<String,ProActiveDescriptor> getListDescriptor();

    public HashMap<String,ArrayList<VirtualNode>> getDeployedVirtualNodeByPad();
    
    
    /**
     * @return the ArrayList of IMNode know by the IM, it's a set of 
     *  free IMNodes and used IMNodes.
     *  @see org.objectweb.proactive.infrastructuremanager.dataresource.IMNode 
     */
    public ArrayList<IMNode> getListAllIMNodes();

	
    /**
     * @return the ArrayList of free Node
     * @see org.objectweb.proactive.infrastructuremanager.dataresource.IMNode
     */
    public ArrayList<IMNode> getListFreeIMNode();

	
    /**
     * @return the ArrayList of used Node
     *  @see org.objectweb.proactive.infrastructuremanager.dataresource.IMNode
     */
    public ArrayList<IMNode> getListBusyIMNode();

	
    /**
     * @return the number of free resource IMNode
     */
    public int getNumberOfFreeResource();

	
    /**
     * @return the number of used resource IMNode
     */
    public int getNumberOfBusyResource();
	
}
