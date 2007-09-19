package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.jmx.mbean.NodeWrapperMBean;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.server.ProActiveServerImpl;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.util.UrlBuilder;

public class NodeObject extends AbstractData{

	private RuntimeObject parent;
	private VNObject vnParent;
	private String url;

 	//Warning: Don't use this variavle directly, use getProxyNodeMBean().
	private NodeWrapperMBean proxyNodeMBean;
	
	public NodeObject(RuntimeObject parent, String url, ObjectName objectName){
		super(objectName);
		this.parent = parent;
		
		this.url = FactoryName.getCompleteUrl(url);
		
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
	
	private NodeWrapperMBean getProxyNodeMBean(){
		if(proxyNodeMBean==null){
			proxyNodeMBean = (NodeWrapperMBean) MBeanServerInvocationHandler.newProxyInstance(getConnection(), getObjectName(), NodeWrapperMBean.class, false);
		}
		return proxyNodeMBean;
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
		List<ObjectName> activeObjectNames = getProxyNodeMBean().getActiveObjects();
		
		for (ObjectName oname : activeObjectNames) {
			BodyWrapperMBean proxyBodyMBean = (BodyWrapperMBean) MBeanServerInvocationHandler.newProxyInstance(getConnection(), oname, BodyWrapperMBean.class, false);
			UniqueID id = proxyBodyMBean.getID();
			String activeObjectName = proxyBodyMBean.getName();
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
		return getProxyNodeMBean().getVirtualNodeName();
	}
	
	/**
	 * Returns the Job Id.
	 * @return the Job Id.
	 */
	public String getJobId() {
		return getProxyNodeMBean().getJobId();
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
