package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;


public class IMUserImpl implements IMUser {	

	private static final Logger logger = ProActiveLogger
	.getLogger(Loggers.IM_USER);
	
	// Attributes
	private IMCore imcore;
	
	
	//----------------------------------------------------------------------//
	// CONSTRUCTORS
	
	/** ProActive compulsory no-args constructor */
	public IMUserImpl() {}
	
	
	public IMUserImpl(IMCore imcore) {
		if (logger.isInfoEnabled()) {
			logger.info("IMUser constructor");
		}
		this.imcore = imcore;
	}
	
	//=======================================================//
	public String echo() {
		return "Je suis le IMUser";	
	}
	//=======================================================//
	
	
	//----------------------------------------------------------------------//
	// METHODS
	
	
	public Node getNode() throws NodeException  {
		if (logger.isInfoEnabled()) {
			logger.info("getNode");
		}
		return imcore.getNode();
	}
	
	public Node[] getAtLeastNNodes(int nb) throws NodeException  {
		if (logger.isInfoEnabled()) {
			logger.info("getAtLeastNNodes, nb nodes : " + nb);
		}
		return imcore.getAtLeastNNodes(nb);
	}

	public void freeNode(Node node) throws NodeException {
		if (logger.isInfoEnabled()) {
			logger.info("freeNode : " + node.getNodeInformation().getName());
		}
		imcore.freeNode(node);
	}
	
	public void freeNodes(Node[] nodes) throws NodeException {
		if (logger.isInfoEnabled()) {
			String freeNodes = "";
			for(Node node : nodes) {
				freeNodes += node.getNodeInformation().getName() + " ";
			}
			logger.info("freeNode : " + freeNodes);
		}
		imcore.freeNodes(nodes);
	}
	

}
