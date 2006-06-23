package org.objectweb.proactive.ext.scilab;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

/**
 * SciDeployEngine contains all methods to deploy Scilab Engines from a file descriptor
 * @author amangin
 *
 */
public class SciDeployEngine {
	private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_DEPLOY);
	
	private static HashMap mapVirtualNode = new HashMap(); // List of deployed VNs 
	
	/**
	 * @param pathDescriptor
	 * @return list of virtual node contained in the descriptor
	 */
	public static String[] getListVirtualNode(String pathDescriptor){
		logger.debug("->SciDeployEngine In:getListVirtualNode:" + pathDescriptor);
		
		ProActiveDescriptor desc;
		VirtualNode arrayVn[];
		String arrayNameVn[];
		try {
			desc = ProActive.getProactiveDescriptor("file:" + pathDescriptor);
			arrayVn = desc.getVirtualNodes();
			arrayNameVn = new String[arrayVn.length];
			
			for(int i=0; i<arrayVn.length; i++){
				arrayNameVn[i] = arrayVn[i].getName();
			}
			
			return arrayNameVn;
		} catch (ProActiveException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * 
	 * @param idVirtualNode
	 * @param nameVirtualNode 
	 * @param pathDescriptor
	 * @param arrayIdEngine
	 * @return HashMap of deployed Scilab Engines
	 */
	public synchronized static HashMap deploy(String idVirtualNode, String nameVirtualNode, String pathDescriptor, String[] arrayIdEngine){
		logger.debug("->SciDeployEngine In:deploy:" + pathDescriptor);
		ProActiveDescriptor desc;
		VirtualNode vn;
		Node nodes[];
		SciEngineWorker sciEngine;
		HashMap mapEngine = new HashMap();
		
		try {
			desc = ProActive.getProactiveDescriptor("file:" + pathDescriptor);
			vn = desc.getVirtualNode(nameVirtualNode);
			vn.activate();
			//((VirtualNodeImpl)vn).addNodeCreationEventListener(this);
			nodes = vn.getNodes();
			
			mapVirtualNode.put(idVirtualNode, vn);
			int length = (nodes.length >arrayIdEngine.length)? arrayIdEngine.length:nodes.length;
			
			for(int i=0; i<length; i++){
				sciEngine = deploy(arrayIdEngine[i], nodes[i]);
				mapEngine.put(arrayIdEngine[i], sciEngine);
			}
		} catch (ProActiveException e) {
			e.printStackTrace();
		}
		return mapEngine;
	}
	
	/**
	 * 
	 * @param idEngine
	 * @param currentNode
	 * @return a Scilab Engine deployed on the current node
	 * @throws ActiveObjectCreationException
	 * @throws NodeException
	 */
	private synchronized static SciEngineWorker deploy(String idEngine, Node currentNode) throws ActiveObjectCreationException, NodeException {
		Object param[] = new Object[]{idEngine};
		SciEngineWorker sciEngine  = (SciEngineWorker) ProActive.newActive(SciEngineWorker.class.getName(), param, currentNode);
		
		try{
			ProActive.setImmediateService(sciEngine, "killTask");
			ProActive.setImmediateService(sciEngine, "exit");
		}catch(IOException e){
			e.printStackTrace();
		}
		return sciEngine;
	}
	
	
	/**
	 * 
	 * @param idEngine
	 * @return a local Scilab Engine
	 * @throws ActiveObjectCreationException
	 * @throws NodeException
	 */
	
	public synchronized static SciEngineWorker deploy(String idEngine) throws ActiveObjectCreationException, NodeException {
		logger.debug("->SciDeployEngine In:deploy");
		Object param[] = new Object[]{idEngine};
		SciEngineWorker sciEngine  =  (SciEngineWorker) ProActive.newActive(SciEngineWorker.class.getName(), param);
		try{
			ProActive.setImmediateService(sciEngine, "killTask");
			ProActive.setImmediateService(sciEngine, "exit");
		}catch(IOException e){
			e.printStackTrace();
		}
		return sciEngine;
	}
	
	/**
	 * Kill all resources used by a VN
	 * @param idVirtualNode
	 */
	public static synchronized void kill(String idVirtualNode){
		logger.debug("->SciDeployEngine In:kill");
		VirtualNode vn;
		vn = (VirtualNode) mapVirtualNode.remove(idVirtualNode);
		if(vn != null) vn.killAll(false);
	}	
	
	/**
	 * Kill all resources used
	 * @param idVirtualNode
	 */
	public static synchronized void killAll(){
		logger.debug("->SciDeployEngine In:killAll");
		VirtualNode vn;
		Object keys[] = mapVirtualNode.keySet().toArray();
		
		for(int i=0; i<keys.length; i++){
			vn = (VirtualNode) mapVirtualNode.remove(keys[i]);
			vn.killAll(false);
		}
	}	
}
