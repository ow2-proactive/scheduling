package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.extensions.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.listener.RuntimeObjectListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.finder.RemoteObjectHostRTFinder;
import org.objectweb.proactive.ic2d.jmxmonitoring.finder.RuntimeFinder;


/**
 * Holder class for the host data representation.
 */
public class HostObject extends AbstractData{

	private WorldObject parent;
	private String url;
	private String hostName;
	private int port;
	private String protocol;
	private int rank;

	private final static String DEFAULT_NAME = "undefined"; 
	
	//Warning: Don't use this variavle directly, use getOSName().
	private String osName;

	//Warning: Don't use this vaiable directly, use getOSVersion(). 
	private String osVersion;

	/**
	 * Creates a new HostObject. Use the method addHost(String url) of the WorldObject class instead.
	 * @param url du host
	 * @throws NullPointerException 
	 * @throws MalformedObjectNameException 
	 */
	protected HostObject(WorldObject parent, String url, int rank) throws MalformedObjectNameException, NullPointerException{
		super(new ObjectName(FactoryName.HOST));
		
		this.hostName = UrlBuilder.getHostNameFromUrl(url);
		String name = UrlBuilder.getNameFromUrl(url);
		this.protocol = UrlBuilder.getProtocol(url);
		this.port = UrlBuilder.getPortFromUrl(url);
		
		this.url = UrlBuilder.buildUrl(hostName, name, protocol, port);
		
		this.rank = rank;
		
		this.parent = parent;
	}

	@SuppressWarnings("unchecked")
	@Override
	public WorldObject getParent() {
		return this.parent;
	}

	@Override
	public String getKey() {
		return this.url;
	}

	@Override
	public String getType() {
		return "host";
	}
	
	@Override
	public String getName() {
		return getHostName();
	}
	
	/**
	 * Returns the url of this object.
	 * @return An url.
	 */
	public String getUrl(){
		return this.url;
	}

	public String getHostName(){
		return this.hostName;
	}

	public int getPort(){
		return this.port;
	}

	public String getProtocol(){
		return this.protocol;
	}

	/**
	 * Returns the operating system name.
	 * @return The operating system name.
	 */
	public String getOSName(){
		if(this.osName==null)
			return DEFAULT_NAME;
		else
			return this.osName;
	}

	/**
	 * Returns the operating sytem version.
	 * @return The operating sytem version.
	 */
	public String getOSVersion(){
		if(osVersion==null)
			return DEFAULT_NAME;
		else
			return this.osVersion;
	}

	@Override
	public void explore() {
		System.out.println(this);
		findRuntimes();
	}
	
	@Override
	public int getHostRank(){
		return this.rank;
	}

	public String toString(){
		String result = this.hostName+":"+this.port;
		if(!getOSName().equals(DEFAULT_NAME)){
			result+=":"+getOSName();
		}
		if(!getOSVersion().equals(DEFAULT_NAME)){
			result+="(OS version: "+getOSVersion()+")";
		}
		return result;
	}

	/**
	 * Propose à l'host de s'ajouter s'il y arrive le fils donné en paramètre.
	 */
	public void proposeChild(){
		
	}
	
	/**
	 * Find all the ProActive Runtimes of this host.
	 */
	@SuppressWarnings("unchecked")
	private void findRuntimes(){
		RuntimeFinder rfinder = new RemoteObjectHostRTFinder();
		Collection<RuntimeObject> runtimeObjects = rfinder.getRuntimeObjects(this);
		
		Iterator<RuntimeObject> it = runtimeObjects.iterator();
		while(it.hasNext()){			
			RuntimeObject runtimeObject = it.next();
			// If this child is not yet monitored.
			if(!(this.containsChild(runtimeObject.getKey()))){
				ObjectName oname = runtimeObject.getObjectName();		
				JMXNotificationManager.getInstance().subscribe(oname, new RuntimeObjectListener(runtimeObject), this.getUrl(), runtimeObject.getServerName());
				addChild(runtimeObject);
				updateOSNameAndVersion(runtimeObject.getConnection());
			}
		}
	}
	
	private void updateOSNameAndVersion(ProActiveConnection connection){
		if(this.osName==null){
			ObjectName osOName = null;
			try {
				osOName = new ObjectName(FactoryName.OS);
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				this.osName = (String)connection.getAttribute(osOName, "Name");
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
			setChanged();
			notifyObservers(this.toString());
		}
		if(this.osVersion==null){
			ObjectName versionOName = null;
			try {
				versionOName = new ObjectName(FactoryName.OS);
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				this.osVersion = (String) connection.getAttribute(versionOName, "Version");
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
			setChanged();
			notifyObservers(this.toString());
		}
	}
}