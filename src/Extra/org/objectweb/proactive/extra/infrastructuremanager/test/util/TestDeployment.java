package org.objectweb.proactive.extra.infrastructuremanager.test.util;

import java.io.File;
import java.util.Map;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;

public class TestDeployment {

	
	public static String URL_PAD1  = "/home/ellendir/ProActive/infrastructuremanager/descriptors/3VNodes-3Jvms-10Nodes.xml";
	//public static String URL_PAD2  = "infrastructuremanager/descriptor/3VNodes-4Jvms-10Nodes.xml";
	
	
	
	public static void DeployPAD(String urlPAD) {
		try {
			
			ProActiveDescriptor pad = ProActive.getProactiveDescriptor(urlPAD);
			//pad.activateMappings();
			pad.activateMapping("Asterix");

			System.out.println("pad name : " + new File(pad.getUrl()).getName());
			
			VirtualNode[] vnodes = pad.getVirtualNodes();
			
			for(int i = 0 ; i < vnodes.length ; i++ ) {
	    		//vnodes[i].activate();
				System.out.println("VNode n�"+i+" : " + vnodes[i].getName() + " is deployed : " + vnodes[i].isActivated());
	    	}
			
			/*
			VirtualNode 	vnode;
			Node[] 			nodes;
			Node 			node;
			NodeInformation nodeInfo;
			
			
			System.out.println("-");
			System.out.println("+--> Nombre de VNodes : " + vnodes.length);
			for(int i = 0 ; i < vnodes.length ; i++ ) {
				vnode = vnodes[i];	
				nodes = vnode.getNodes();
				System.out.println("--");
				System.out.println("+----> " + nodes.length + " Nodes appartiennent au VNode " + vnode.getName());
				
				for(int j = 0 ; j < nodes.length ; j++ ) {
					node 	 = nodes[i];
					nodeInfo = node.getNodeInformation();
					
					String mes = "NodeInformation : \n";
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
			}
			*/
			System.out.println("Map map = pad.getVirtualNodeMapping();");
			
			Map map = pad.getVirtualNodeMapping();
			for(Object vnodeObject :  map.keySet()) {
				String vnodeName = (String) vnodeObject;
				System.out.print(vnodeName + " : ");
				VirtualNode mappingVNode = (VirtualNode) map.get(vnodeName);
				System.out.println(mappingVNode.getName());
			}
			
			
			
		} 
		catch (ProActiveException e) { e.printStackTrace(); }

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Descripteur de d�ploimenent n�1 : ");
		DeployPAD(URL_PAD1);
		/*
		System.out.println("Descripteur de d�ploimenent n�2 : ");
		DeployPAD(URL_PAD1);
		*/
	}

}
