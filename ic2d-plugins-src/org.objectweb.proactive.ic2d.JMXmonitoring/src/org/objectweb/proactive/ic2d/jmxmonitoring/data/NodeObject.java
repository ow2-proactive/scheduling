package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.server.ProActiveServerImpl;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.extensions.jmx.util.JMXNotificationManager;

public class NodeObject extends AbstractData{

	private RuntimeObject parent;
	private VNObject vnParent;
	private String url;

	public NodeObject(RuntimeObject parent, String url, ObjectName objectName){
		super(objectName);
		this.parent = parent;
		
		String host = UrlBuilder.getHostNameFromUrl(url);
		String name = UrlBuilder.getNameFromUrl(url);
		String protocol = UrlBuilder.getProtocol(url);
		int port = UrlBuilder.getPortFromUrl(url);
		
		this.url = UrlBuilder.buildUrl(host, name, protocol, port);
		
		Comparator<String> comparator = new ActiveObject.ActiveObjectComparator();
		this.monitoredChildren = new TreeMap<String, AbstractData>(comparator); 
	}

	@SuppressWarnings("unchecked")
	@Override
	public RuntimeObject getParent() {
		return this.parent;
	}
	
	/**
	 * Sets the virtual node.
	 * @param vn the virtual node.
	 */
	public void setVirtualNode(VNObject vn){
		this.vnParent = vn;
	}
	
	/**
	 * Returns the virtual node.
	 * @return the virtual node.
	 */
	public VNObject getVirtualNode(){
		return this.vnParent;
	}
	
	@Override
	public void destroy(){
		this.vnParent.removeChild(this);
		super.destroy();
	}

	@Override
	public void explore() {
		findActiveObjects();
	}

	@Override
	public String getKey() {
		return this.url;
	}
	
	@Override
	public String getType() {
		return "node object";
	}
	
	/**
	 * Returns the url of this object.
	 * @return An url.
	 */
	public String getUrl(){
		return this.url;
	}

	/**
	 * Finds all active objects of this node.
	 */
	@SuppressWarnings("unchecked")
	private void findActiveObjects(){
		List<ObjectName> activeObjectNames = null;
		try {
			activeObjectNames = (List<ObjectName>) getConnection().getAttribute(getObjectName(),"ActiveObjects");
		} catch (AttributeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReflectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (ObjectName oname : activeObjectNames) {
			UniqueID id = null;
			String activeObjectName = null;
			try {
				id = (UniqueID) getConnection().getAttribute(oname, "ID");
				activeObjectName = (String) getConnection().getAttribute(oname, "Name");
				//System.out.println("NodeObject.findActiveObjects() ===>>>> name="+activeObjectName);
			} catch (AttributeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstanceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ActiveObject ao = new ActiveObject(this,id, activeObjectName, oname);
			addChild(ao);
		}
	}

	@Override
	public String getName(){
		return UrlBuilder.getNameFromUrl(getUrl());
	}

	@Override
	public String toString(){
		return "Node: "+getUrl();
	}
	
	public void addChild(ActiveObject child){		
		super.addChild(child);
		String name = child.getClassName();
		if((!name.equals(ProActiveConnection.class.getName())
				&&
				(!name.equals(ProActiveServerImpl.class.getName())))){
			ObjectName oname = child.getObjectName();
			
			JMXNotificationManager.getInstance().subscribe(oname, child.getListener(), this.getHostUrlServer(), this.getServerName());
			//subscribe(new NotificationSource(oname,getUrl()), child.getListener());
		}
	}

	/**
	 * Returns the virtual node name.
	 * @return the virtual node name.
	 */
	public String getVirtualNodeName() {
		try {
			return (String) getAttribute("VirtualNodeName");
		} catch (AttributeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReflectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns the Job Id.
	 * @return the Job Id.
	 */
	public String getJobId() {
		try {
			return (String) getAttribute("JobId");
		} catch (AttributeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReflectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Used to highlight this node, in a virtual node.
	 * @param highlighted true, or false
	 */
	public void setHighlight(boolean highlighted) {
		this.setChanged();
		if (highlighted)
			this.notifyObservers(State.HIGHLIGHTED);
		else
			this.notifyObservers(State.NOT_HIGHLIGHTED);
	}
}
