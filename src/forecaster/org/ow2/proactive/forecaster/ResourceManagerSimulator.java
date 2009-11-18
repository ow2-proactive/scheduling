package org.ow2.proactive.forecaster;

import java.util.List;

import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * Should manage FakeNodes objects as implementation of the Node interface
 * @author esalagea
 *
 */
public class ResourceManagerSimulator {

	/**
	 * TODO: implement this method
	 * @param nodes
	 */
	public void freeNodes(NodeSet nodes)
	{
		//TODO: implement ... 
	}
	
	
	/**
	 * TODO: implement this
	 * @return
	 */
	public RMState getRMState()
	{
		return null;
	}


	/**
	 * TODO: implement this
	 *    in the first implementation do not take in to account the selection scripts
	 * 
	 * @param neededResourcesNumber
	 * @param selectionScripts
	 * @return
	 */
	public NodeSet getAtMostNodes(int neededResourcesNumber,
			List<SelectionScript> selectionScripts) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
	
}
