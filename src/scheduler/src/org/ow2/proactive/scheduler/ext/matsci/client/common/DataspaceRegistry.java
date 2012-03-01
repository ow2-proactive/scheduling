package org.ow2.proactive.scheduler.ext.matsci.client.common;

import org.ow2.proactive.scheduler.ext.matsci.client.common.data.Pair;
import org.ow2.proactive.scheduler.ext.matsci.client.common.data.UnReifiable;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * DataspaceRegistry the interface to the middleman Dataspace registry (it creates dataspaces on given directories)
 *
 * @author The ProActive Team
 */
public interface DataspaceRegistry extends Remote {

    /**
     * Creates a dataspace for the given path
     * @param path path where to create the dataspace
     * @return
     * @throws RemoteException
     */
    public UnReifiable<Pair<String, String>> createDataSpace(String path) throws RemoteException;

    /**
     * Initializes the registry by specifying debug mode, input and output spaces base names
     * @param inbasename base name of each input space created
     * @param outbasename base name of each output space created
     * @param debug debug mode
     * @throws RemoteException
     */
    public void init(String inbasename, String outbasename, boolean debug) throws RemoteException;
}
