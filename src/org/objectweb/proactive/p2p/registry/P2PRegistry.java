/*
 * Created on Jan 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.p2p.registry;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author jbustos
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface P2PRegistry extends Remote {
public void register(String key, Object o) throws RemoteException;
public void unregister(String key, Object o) throws RemoteException;

public void registerOverloaded(String key, Object o) throws RemoteException;
public void unregisterOverloaded(String key, Object o) throws RemoteException;

public void registerUnderloaded(String key, Object o) throws RemoteException;
public void unregisterUnderloaded(String key, Object o) throws RemoteException;

public Object getUnderloaded(String key) throws RemoteException;

public int getNumberOfAvailables() throws RemoteException;
public Object[] getAvailables(int n) throws RemoteException;
}
