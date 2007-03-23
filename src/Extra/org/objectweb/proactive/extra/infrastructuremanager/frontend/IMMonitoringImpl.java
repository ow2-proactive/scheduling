/**
 * 
 */
package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;



/**
 * @author Ellendir
 *
 */
public class IMMonitoringImpl implements IMMonitoring {
	
	private static final Logger logger = ProActiveLogger
	.getLogger(Loggers.IM_USER);
	
	// Attributes
	private IMCore imcore;
	
	
	//----------------------------------------------------------------------//
	// CONSTRUTORS
	
	/** ProActive compulsory no-args constructor */
	public IMMonitoringImpl() {}
	
	public IMMonitoringImpl(IMCore imcore) {
		System.out.println("[IMMonitoring] constructor");
		this.imcore = imcore;
	}

	
	//=======================================================//
	public String echo() {
		return "Je suis le IMMonitoring";	
	}
	//=======================================================//
	
	
	

	/* 
	 * OK
	 */
	public HashMap<String,ProActiveDescriptor> getListDescriptor() {
		return imcore.getListPAD();
	}

	
	public HashMap<String,ArrayList<VirtualNode>> getDeployedVirtualNodeByPad() {
		return imcore.getDeployedVirtualNodeByPad();
	}
	
	
	// TODO
	public ArrayList<IMNode> getListAllIMNodes() {
		return imcore.getListAllNodes();
	}

	/* 
	 *  OK
	 */
	public ArrayList<IMNode> getListFreeIMNode() {
		return imcore.getListFreeIMNode();
	}

	/*  
	 *  OK
	 */
	public ArrayList<IMNode> getListBusyIMNode() {
		return imcore.getListBusyIMNode();
	}


	// TODO
	public int getNumberOfFreeResource() {
		return imcore.getSizeListFreeIMNode();
	}


	// TODO
	public int getNumberOfBusyResource() {
		return imcore.getSizeListBusyIMNode();
	}


}
