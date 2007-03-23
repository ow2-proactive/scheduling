package org.objectweb.proactive.extra.infrastructuremanager.test.util;

import java.rmi.AlreadyBoundException;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.NodeInformation;


public class TestNodeInformation {

	public static void main(String[] args) {
		try {
			Node nodeIM   = NodeFactory.createNode("IM");
			NodeInformation nodeInfo = nodeIM.getNodeInformation();
			String mes = "## NodeInformation : \n";
			mes += "+--------------------------------------------------------------------\n";
			mes += "+--> getCreationProtocolID : " + nodeInfo.getCreationProtocolID() + "\n";
			mes += "+--> getDescriptorVMName   : " + nodeInfo.getDescriptorVMName()   + "\n";
			mes += "+--> getHostName           : " + nodeInfo.getHostName()           + "\n";
			mes += "+--> getJobID              : " + nodeInfo.getJobID()              + "\n";
			mes += "+--> getName               : " + nodeInfo.getName()               + "\n";
			mes += "+--> getProtocol           : " + nodeInfo.getProtocol()           + "\n";
			mes += "+--> getURL                : " + nodeInfo.getURL()                + "\n";
			mes += "+--------------------------------------------------------------------\n";
			
			System.out.println(mes);
			
		} 
		catch (NodeException e)         { e.printStackTrace(); } 
		catch (AlreadyBoundException e) { e.printStackTrace(); }
		
	}
}
