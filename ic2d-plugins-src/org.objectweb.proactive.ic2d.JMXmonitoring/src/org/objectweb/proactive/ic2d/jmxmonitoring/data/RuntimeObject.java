package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.io.IOException;
import java.util.List;

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.jmx.mbean.NodeWrapperMBean;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.util.UrlBuilder;

/**
 * Represents a Runtime in the IC2D model.
 * @author ProActive Team
 */
public class RuntimeObject extends AbstractData{

	/**
	 * All the method names used to notify the observers
	 */
	public enum methodName { RUNTIME_KILLED, RUNTIME_NOT_RESPONDING, RUNTIME_NOT_MONITORED };
	
	private HostObject parent;
	private String url;
	//private ProActiveConnection connection;
	private String hostUrlServer;
	private String serverName;
	
	private ProActiveRuntimeWrapperMBean proxyMBean;
	
	public RuntimeObject(HostObject parent, String url, ObjectName objectName, String hostUrl, String serverName) {
		super(objectName);
		this.parent = parent;
		
		this.url = FactoryName.getCompleteUrl(url);
		
		this.hostUrlServer = hostUrl;
		this.serverName = serverName;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public HostObject getParent() {
		return this.parent;
	}

	@Override
	public void explore() {
		findNodes();
	}

	@Override
	public String getKey() {
		return this.url;
	}
	
	@Override
	public String getType() {
		return "runtime object";
	}
	
	@Override
	protected String getHostUrlServer(){
		return this.hostUrlServer;
	}
	
	@Override
	protected String getServerName(){
		return this.serverName;
	}
	
	/**
	 * Returns the url of this object.
	 * @return An url.
	 */
	public String getUrl(){
		return this.url;
	}
	
	/**
	 * Kill this runtime.
	 */
	public void killRuntime(){
		new Thread(){
			public void run(){
				Object[] params = {};
				String[] signature = {};
				invokeAsynchronous("killRuntime", params, signature);
				runtimeKilled();
			}
		}.start();
	}

	
	public void runtimeKilled(){
		setChanged();
		notifyObservers(methodName.RUNTIME_KILLED);
		new Thread(){
			public void run(){
				try {
					sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				RuntimeObject.this.destroy();
			}
		}.start();
	}
	
	/**
	 * Finds all nodes of this Runtime.
	 */
	@SuppressWarnings("unchecked")
	private void findNodes(){
		proxyMBean = (ProActiveRuntimeWrapperMBean) MBeanServerInvocationHandler.newProxyInstance(getConnection(), getObjectName(), ProActiveRuntimeWrapperMBean.class, false);
		try {
			if(!(getConnection().isRegistered(getObjectName()))){
				return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<ObjectName> nodeNames = null;
		try {
			nodeNames = nodeNames = proxyMBean.getNodes();
		} catch (ProActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (ObjectName name : nodeNames) {
			NodeWrapperMBean proxyNodeMBean = (NodeWrapperMBean) MBeanServerInvocationHandler.newProxyInstance(getConnection(), name, NodeWrapperMBean.class, false);
			String url = proxyNodeMBean.getURL();
			
			// We need to have a complete url protocol://host:port/name
        	NodeObject child = new NodeObject(this,FactoryName.getCompleteUrl(url), name);
        	String virtualNodeName = child.getVirtualNodeName();
        	VNObject vn = getWorldObject().getVirtualNode(virtualNodeName);
        	// this virtual node is not monitored
        	if(vn==null){
        		vn = new VNObject(virtualNodeName, child.getJobId(), getWorldObject());
        		getWorldObject().addVirtualNode(vn);
        	}
        	// Set to the node the parent virtual node.
        	child.setVirtualNode(vn);
        	vn.addChild(child);
        	
			addChild(child);
		}
	}
	
	@Override
	public String getName(){
		return UrlBuilder.getNameFromUrl(getUrl());
	}
	
	public String toString(){
		return "Runtime: "+getUrl();
	}
}
