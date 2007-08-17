package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.io.IOException;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

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
	
	public RuntimeObject(HostObject parent, String url, ObjectName objectName, String hostUrl, String serverName) {
		super(objectName);
		this.parent = parent;
		
		String host = UrlBuilder.getHostNameFromUrl(url);
		String name = UrlBuilder.getNameFromUrl(url);
		String protocol = UrlBuilder.getProtocol(url);
		int port = UrlBuilder.getPortFromUrl(url);
		
		this.url = UrlBuilder.buildUrl(host, name, protocol, port);
		
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
	
	public void killRuntime(){
		new Thread(){
			public void run(){
				try {
					Object[] params = {};
					String[] signature = {};
					invoke("killRuntime", params, signature);
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
				runtimeKilled();
			}
		}.start();
	}
	
	public void runtimeKilled(){
		setChanged();
		notifyObservers(methodName.RUNTIME_KILLED);
		/*try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		destroy();*/
	}
	
	/**
	 * Finds all nodes of this Runtime.
	 */
	@SuppressWarnings("unchecked")
	private void findNodes(){
		List<ObjectName> nodeNames = null;
		try {
			if(!(getConnection().isRegistered(getObjectName()))){
				return;
			}
			nodeNames = (List<ObjectName>) getConnection().getAttribute(getObjectName(), "Nodes");
		} catch (AttributeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
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
		for (ObjectName name : nodeNames) {
			String url = null;
			try {
				url = (String) getConnection().getAttribute(name, "URL");
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
			// We need to have a complete url protocol://host:port/name
        	String hostInUrl = UrlBuilder.getHostNameFromUrl(url);
        	String nameInUrl = UrlBuilder.getNameFromUrl(url);
        	String protocolInUrl = UrlBuilder.getProtocol(url);
        	int portInUrl = UrlBuilder.getPortFromUrl(url);
        	NodeObject child = new NodeObject(this,UrlBuilder.buildUrl(hostInUrl, nameInUrl, protocolInUrl, portInUrl), name);
        	String virtualNodeName = child.getVirtualNodeName();
        	VNObject vn = getWorldObject().getVirtualNode(virtualNodeName);
        	// this virtual node is not monitored
        	if(vn==null){
        		vn = new VNObject(virtualNodeName, child.getJobId(), getWorldObject());
        		getWorldObject().addVirtualNode(vn);
        	}
        	// Set to the node the parent virtual node.
        	child.setVirtualNode(vn);
        	
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
