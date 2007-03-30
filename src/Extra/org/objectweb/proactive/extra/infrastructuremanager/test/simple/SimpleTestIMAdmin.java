package org.objectweb.proactive.extra.infrastructuremanager.test.simple;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;


public class SimpleTestIMAdmin {
	
	//public static String URL_PAD_LOCAL  = "/home/ellendir/ProActive/infrastructuremanager/descriptors/3VNodes-3Jvms-10Nodes.xml";
	public static String URL_PAD_LOCAL  = "/workspace/ProActive-New2/infrastructuremanager/descriptors/3VNodes-3Jvms-10Nodes.xml";
	public static String[] vnodesName   = new String[]{"Idefix", "Asterix"}; 
	
	public static void main(String[] args) {
		
		System.out.println("# --oOo-- Simple Test  Admin --oOo-- ");
	
		
		try {
			
			URI uriIM   = new URI("rmi://localhost:1099/");
			IMAdmin admin = IMFactory.getAdmin(uriIM);
			// OR
			// IMAdmin admin = IMFactory.getAdmin();
			// to get admin from the local IM
			
			System.out.println("#[SimpleTestIMAdmin] Echo admin : " + admin.echo());
			
			System.out.println("#[SimpleTestIMAdmin] deployAllVirtualNodes : " + URL_PAD_LOCAL);
			admin.deployVirtualNodes(new File(URL_PAD_LOCAL), NodeFactory.getDefaultNode(), vnodesName);
			
			
			System.out.println("Sleep 12s");
			Thread.sleep(12000);
			
			HashMap<String,ArrayList<VirtualNode>> deployedVNodesByPad = admin.getDeployedVirtualNodeByPad();
			//System.out.println("hashNext : " + deployedVNodesByPad.keySet().iterator().hasNext());
			String padName = deployedVNodesByPad.keySet().iterator().next();
			System.out.println("padName : " + padName);
			
			System.out.println("#[SimpleTestIMAdmin] killPAD : vnode Idefix");
			admin.killPAD(padName);
		
			
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		
		System.out.println("##[TestIMAdmin] END TEST");

	}

}
