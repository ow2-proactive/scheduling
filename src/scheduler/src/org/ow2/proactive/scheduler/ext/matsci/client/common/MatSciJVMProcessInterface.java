package org.ow2.proactive.scheduler.ext.matsci.client.common;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * JVMProcessInterface an interface to control the lifecycle of the middleman JVM
 *
 * @author The ProActive Team
 */
public interface MatSciJVMProcessInterface extends Remote {

    /**
     * Returns this JVM PID
     * @return
     * @throws RemoteException
     */
    public Integer getPID() throws RemoteException;

    /**
     * Shuts down this JVM
     * @return
     * @throws RemoteException
     */
    public boolean shutdown() throws RemoteException;
}
