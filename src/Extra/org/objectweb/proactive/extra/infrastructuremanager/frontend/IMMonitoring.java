package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;

/**
 * An interface Front-End for the Monitoring to communicate with
 * the Infrastructure Manager 
 */

public interface IMMonitoring {
    // pour tester
    public String echo();

	/**
	 * This method serve to know all the deployed proactive descriptor, 
	 * and get the virtual nodes for redeploying or killing nodes.<BR/>
	 * For getting the virtual nodes of a pad call this method :<BR/>
	 * VirtualNode[] vnodes = pad.getVirtualNodes(); 
	 * @return hashmap < String padName, ProActiveDescriptor pad >
	 */
    public HashMap<String,ProActiveDescriptor> getListDescriptor();

    
    /**
     * This method serve to get all deployed virtualnodes by proactive descriptor.
     * @return hashmap < String padName, ArrayList<VirtualNode> list of deployed virtualnodes >
     */
    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad();

    /**
     * @return the ArrayList of IMNode know by the IM, it's a set of
     *  free IMNodes and busy IMNodes.
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
     * @see org.objectweb.proactive.infrastructuremanager.dataresource.IMNode
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
    
    /**
     * @return the number of down resource IMNode
     */
    public int getNumberOfDownResource();
    
    /**
     * @return the number of all resource IMNode
     */
	public int getNumberOfAllResources();
	

}
