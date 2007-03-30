package org.objectweb.proactive.extra.infrastructuremanager.test.simple;

import java.io.IOException;
import java.util.Date;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;


public class SimpleTestIMUser {

	
	public static void afficheNodeInfo(Node node) {
		if( node != null ) {
			NodeInformation nodeInfo = node.getNodeInformation();
			String mes = "#[SimpleTestIMUser] NodeInformation : \n";
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
		else {
			System.out.println("##[TestIMUser] Aucun node disponible");
		}
	}
	
	

	public static void main(String[] args) throws ActiveObjectCreationException, IOException {

		System.out.println("# --oOo-- Simple Test User --oOo-- ");

		try {
			IMUser user = IMFactory.getUser();
			System.out.println("#[SimpleTestIMUser] Echo user : " + user.echo());
			
			// GET NODE(S)
			System.out.println("#[SimpleTestIMUser] User ask to the IM One Node");
			Node node = user.getNode();
			afficheNodeInfo(node);
			
			
			int nbAskedNodes = 2; 
			System.out.println("#[SimpleTestIMUser] User ask to the IM " + nbAskedNodes + " Nodes");
			Node[] nodes = user.getAtLeastNNodes(nbAskedNodes);
			for(Node aNode : nodes) {
				afficheNodeInfo(aNode);
			}
			
			
			
			// FREE NODE(S)
			
			/*
			System.out.println("#[SimpleTestIMUser] User free the node");
			user.freeNode(node);
			user.freeNodes(nodes);
			*/
			
		}
		catch(Exception e) { System.out.println("##[TestIMUser-catch] Pas de node dispo"); }
		
		System.out.println("##[SimpleTestIMUser] END TEST");
		

	}

}
