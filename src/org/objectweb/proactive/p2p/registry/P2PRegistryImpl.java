/*
 * Created on Jan 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.p2p.registry;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
// import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.rmi.*;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
// import org.objectweb.proactive.core.node.*;


import net.jini.discovery.LookupDiscovery;

/**
 * @author jbustos
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
// public class P2PRegisterImpl extends UnicastRemoteObject implements P2PRegister {
public class P2PRegistryImpl implements Serializable, P2PRegistry, RunActive {

	private Map register;
	private Map underloaded;
		
	public P2PRegistryImpl()  throws RemoteException {
		register = new HashMap();
		underloaded = new HashMap();
	}

/****************** External Messages **************/

	public void  register(String key, Object o) throws RemoteException {
		
		register.put(key,o);
		System.out.println("[P2PRegistry] Register: "+key);
	}

	public void unregister(String key, Object o) throws RemoteException {
		register.remove(key);			
		System.out.println("[P2PRegistry] Unregister: "+key);
	}

	public void registerOverloaded(String key, Object o) throws RemoteException {
	}

	public void unregisterOverloaded(String key, Object o) throws RemoteException {
	}

	public void registerUnderloaded(String key, Object o) throws RemoteException {
			underloaded.put(key, o);
		System.out.println("[P2PRegistry] Underload Register: "+key);
	}

	public void unregisterUnderloaded(String key, Object o) throws RemoteException {
			underloaded.remove(key);
		System.out.println("[P2PRegistry] Underload Unregister: "+key);
	}

	public Object getUnderloaded(String key) throws RemoteException {
		
		Object o = null;
		
			if (underloaded.size() < 1) return o;

			Object[] keys = underloaded.keySet().toArray();			
			return underloaded.get(keys[0]);
	}
/******************* Internal use *********************************/

public static void main(String[] args) throws Exception {

		
	P2PRegistryImpl Areg = (P2PRegistryImpl) ProActive.newActive(P2PRegistryImpl.class.getName(),new Object[]{});
	System.out.println("[P2PRegistry] Started.");
	
	}
/************** Job Submitter use *****************************/

public int getNumberOfAvailables() throws RemoteException {
	return underloaded.size();
	}

public Object[] getFullyAvailables(int n) throws RemoteException {
        if (n > underloaded.size()) n = underloaded.size();
        List l = new LinkedList(underloaded.values());
        return l.subList(0,n-1).toArray();
        }

public Object[] getAvailables(int n) throws RemoteException {
	if (n > register.size()) n = register.size();
	List l = new LinkedList(register.values());
	return l.subList(0,n-1).toArray();
	}

/*************** Active object methods ***************************/

private void registerJini(RemoteBodyAdapter body) throws IOException {

	System.setSecurityManager(new RMISecurityManager());
	String[] groups = new String[] {""};
	LookupDiscovery ld = new LookupDiscovery(groups);
	Object reg = body.getRemoteAdapter();
	// Debug
	
	// end Debug
	ServerListener sl = new ServerListener(ld,reg);
	ld.addDiscoveryListener(sl);
	}

	public void register () {
			try {
					ProActive.register(ProActive.getStubOnThis(), "//localhost/P2PRegistry"); 
					}
			catch (IOException e) { 
					System.err.println("[Robin Hood] Cannot register!");
					e.printStackTrace();
			} 
	}

	public void unregister () {
			try {
					ProActive.unregister("//localhost/P2PRegistry"); 
					}
			catch (IOException e) { e.printStackTrace(); }
	}

	public void runActivity(Body body) {
		
		ProActiveConfiguration.load();
		Service service = new Service(body);
		register();
					
		while (body.isActive()) {
				service.blockingServeOldest();
			};
			
		unregister();
	}


}
