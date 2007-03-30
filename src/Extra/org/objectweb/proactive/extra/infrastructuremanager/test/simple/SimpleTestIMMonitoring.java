package org.objectweb.proactive.extra.infrastructuremanager.test.simple;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;


public class SimpleTestIMMonitoring {
	
	private IMMonitoring imMonitoring;
	
	
	//----------------------------------------------------------------------//
	
	public SimpleTestIMMonitoring(IMMonitoring imMonitoring) {
		this.imMonitoring = imMonitoring;
	}
	

	//----------------------------------------------------------------------//
	
	
	public String descriptionIMNode(IMNode imnode) {
		String mes = "";
		mes += imnode.getPADName() + " - ";
		mes += imnode.getVNodeName() + " - ";
		mes += imnode.getHostName() + " - ";
		mes += imnode.getDescriptorVMName() + " - ";
		mes += imnode.getNodeName() + ".\n";
		return mes;
	}
	
	
	/*
	 * IMNode's ToString :
	 *  
	 *  | Name of this Node  :  Asterix-1623542303
	 *  +-----------------------------------------------+
	 *  | Node is free ?    : true
	 *  | Name of PAD               : 3VNodes-3Jvms-10Nodes19412.xml
	 *  | VNode                     : Asterix
	 *  | Host                      : r5p6
	 *  | Name of the VM    : Jvm1
	 *  +-----------------------------------------------+
     *
	 */
	public  void printAllIMNodes() {
		System.out.println("printAllIMNodes");
		ArrayList<IMNode> imNodes = imMonitoring.getListAllIMNodes();
		for(IMNode imNode : imNodes) {
			System.out.println(imNode);
		}
	}
	
	
	public void printDeployedVNodes() {
		System.out.println("printDeployedVNodes");
		HashMap<String,ArrayList<VirtualNode>> deployedVNodesByPad = imMonitoring.getDeployedVirtualNodeByPad();
		for(String padName :  deployedVNodesByPad.keySet()) {
			System.out.println("padName : " + padName);
			ArrayList<VirtualNode> deployedVnodes = deployedVNodesByPad.get(padName);
			System.out.println("Number of deployed vn : " + deployedVnodes.size());
			for(VirtualNode vn : deployedVnodes) {
				System.out.println("Name of deployed vnode : " + vn.getName());
			}
		}
	}
	


	/*
	 * PAD 
	 *   	+-- VirtualNode
	 *        		+-- Hostname
	 *             		   +-- VirtualMachine
	 *      			 		   +-- Node				    state
	 *       
	 * state : {"free","busy","down"}    
	 * 
	 *      
	 * example :
	 *      
	 * 3VNodes-3Jvms-10Nodes19412.xml
     *      +-- Asterix
     *              +-- r5p6
     *                     +-- Jvm1
     *                             +-- Asterix-1623542303	busy
     *                             +-- Asterix-951276804	free
     *                             +-- Asterix390993068		busy
     *     +-- Idefix
     *              +-- r5p6
     *                     +-- Jvm3
     *                             +-- Idefix-1717183521	down
     *                             +-- Idefix-347200095		free
     *                             +-- Idefix1522374025		free
     *                             +-- Idefix1823860328		busy
     *                             +-- Idefix485990567		free
     *                             
	 * For sorting a list or a table see :
	 * 		Arrays.sort(Object[], Comparator)
	 * or
	 * 		Collections.sort(List, Comparator)     
	 */
	public void printIMNodesByVNodeByPad() {
		ArrayList<IMNode> imNodes = imMonitoring.getListAllIMNodes();
		Object[] tableOfIMNodes =  imNodes.toArray();
		Arrays.sort(tableOfIMNodes, new ComparatorIMNode());
		/*
		for(int i = 0 ; i < tableOfIMNodes.length ; i ++ ) {
			System.out.println(i + ". " + descriptionIMNode((IMNode)tableOfIMNodes[i]));
		}*/
		System.out.println("printTree :  ");
		System.out.println("-----------\n");
		
		IMNode imnode = (IMNode) tableOfIMNodes[0];
		
		System.out.println(" " + imnode.getPADName());
		System.out.println("\t+-- " + imnode.getVNodeName());
		System.out.println("\t\t+-- " + imnode.getHostName());
		System.out.println("\t\t\t+-- " + imnode.getDescriptorVMName());
		System.out.print("\t\t\t\t+-- " + imnode.getNodeName());
		try {
			if( imnode.isFree() ) {
				System.out.println(" \tfree");
			}
			else {
				System.out.println(" \tbusy");
			}
		} catch (NodeException e) {
			System.out.println(" \tdown");
		}
		
		boolean change = false;
		for(int i = 1 ; i < tableOfIMNodes.length ; i++ ) {
			IMNode imnode1 = (IMNode) tableOfIMNodes[i-1];
			IMNode imnode2 = (IMNode) tableOfIMNodes[i];
			change = false;
			
			if(! imnode1.getPADName().equals(imnode2.getPADName())) {
				System.out.println(" " + imnode2.getPADName());
				change = true;
			}
			if( change | ! imnode1.getVNodeName().equals(imnode2.getVNodeName()) ) {
				System.out.println("\t+-- " + imnode2.getVNodeName());
				change = true;
			}
			if( change | ! imnode1.getHostName().equals(imnode2.getHostName()) ) {
				System.out.println("\t\t+-- " + imnode2.getHostName());
				change = true;
			}
			if( change | ! imnode1.getDescriptorVMName().equals(imnode2.getDescriptorVMName()) ) {
				System.out.println("\t\t\t+-- " + imnode2.getDescriptorVMName());
				change = true;
			}
			System.out.print("\t\t\t\t+-- " + imnode2.getNodeName());
			try {
				if( imnode2.isFree() ) {
					System.out.println(" \tfree");
				}
				else {
					System.out.println(" \tbusy");
				}
			} catch (NodeException e) {
				System.out.println(" \tdown");
			}
		}
	}
	
	
	
	
	public static void main(String[] args) {
		
		System.out.println("# --oOo-- Simple Test  Monitoring --oOo-- ");
		
		try {
			IMMonitoring imMonitoring = IMFactory.getMonitoring();
			System.out.println("#[SimpleTestIMMonitoring] Echo monitoring : " + imMonitoring.echo());
			
			SimpleTestIMMonitoring test = new SimpleTestIMMonitoring(imMonitoring);
			
			test.printAllIMNodes();
			test.printDeployedVNodes();
			test.printIMNodesByVNodeByPad();
			
		} catch (NodeException e) {
			e.printStackTrace();
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
