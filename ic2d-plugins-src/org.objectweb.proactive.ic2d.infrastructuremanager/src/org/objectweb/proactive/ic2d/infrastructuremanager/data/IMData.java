package org.objectweb.proactive.ic2d.infrastructuremanager.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;

public class IMData extends Observable {

	private String url;
	private IMAdmin admin;
	private IMMonitoring monitoring;
	
	private int availableNode, busyNode, downNode;
	private HashMap<String, HashMap<String, ArrayList<IMNode>>> infrastructure;

	
	public IMData() {
	}
	
	public IMData(String url) {
		try {
			this.url = url;
			admin = IMFactory.getAdmin(url);
			monitoring = IMFactory.getMonitoring(url);
			arrayListToHashMap(monitoring.getListAllIMNodes());
		}
		catch (ActiveObjectCreationException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}		
	}

	public String getURL() {
		return url;
	}
	
	public HashMap<String, HashMap<String,ArrayList<IMNode>>> getInfrastructure() {
		return infrastructure;
	}
	
	public IMAdmin getAdmin() {
		return admin;
	}
	
	public void arrayListToHashMap(ArrayList<IMNode> list) {
		infrastructure = new HashMap<String, HashMap<String, ArrayList<IMNode>>>();
		String descriptorName, vnodeName;
		for (IMNode node : list) {
			descriptorName = node.getPADName();
			vnodeName = node.getVNodeName();
			// Si le descriptor n'existe pas dans infrastructure
			if(! infrastructure.containsKey(descriptorName)) {
				infrastructure.put(descriptorName, new HashMap<String, ArrayList<IMNode>>());				
			}
			// Si le vnode n'existe pas dans infrastructure/descriptorName
			if(! infrastructure.get(descriptorName).containsKey(vnodeName)) {
				infrastructure.get(descriptorName).put(vnodeName, new ArrayList<IMNode>());
			}
			// on ajoute le IMNode dans infrastructure/descriptorName/vnodeName
			infrastructure.get(descriptorName).get(vnodeName).add(node);
		}
	}
	
	
	// TODO
	/*public HashMap<String, HashMap<String,ArrayList<IMNode>>> getInfrastructureTest() {
		ArrayList<IMNode> list = new ArrayList<IMNode>();
		
		list.add(new IMNodeImpl(new NodeImpl(), "Busy", "VNode1", "Descriptor1"));
		list.add(new IMNodeImpl(new NodeImpl(), "Available", "VNode1", "Descriptor1"));
		
		list.add(new IMNodeImpl(new NodeImpl(), "Down", "VNode2", "Descriptor1"));
		list.add(new IMNodeImpl(new NodeImpl(), "Busy", "VNode2", "Descriptor1"));
		list.add(new IMNodeImpl(new NodeImpl(), "Available", "VNode2", "Descriptor1"));
		
		list.add(new IMNodeImpl(new NodeImpl(), "Available", "VNode3", "Descriptor2"));
		list.add(new IMNodeImpl(new NodeImpl(), "Available", "VNode3", "Descriptor2"));
		list.add(new IMNodeImpl(new NodeImpl(), "Available", "VNode3", "Descriptor2"));
		list.add(new IMNodeImpl(new NodeImpl(), "Busy", "VNode3", "Descriptor2"));
		
		list.add(new IMNodeImpl(new NodeImpl(), "Busy", "VNode4", "Descriptor2"));
		
		list.add(new IMNodeImpl(new NodeImpl(), "Busy", "VNode5", "Descriptor3"));
		
		list.add(new IMNodeImpl(new NodeImpl(), "Busy", "VNode5", "Descriptor3"));
		
		arrayListToHashMap(list);
		return infrastructure;
	}*/
}
