package org.ow2.proactive.scheduler.ext.matsci.middleman;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.ext.matlab.middleman.AOMatlabEnvironment;
import org.ow2.proactive.scheduler.ext.matsci.client.common.DataspaceRegistry;
import org.ow2.proactive.scheduler.ext.matsci.client.common.MatSciEnvironment;
import org.ow2.proactive.scheduler.ext.matsci.client.common.MatSciJVMProcessInterface;
import org.ow2.proactive.scheduler.ext.scilab.middleman.AOScilabEnvironment;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


/**
 * MiddlemanDeployer a main class used to deploy the environment and dataspace registry in the middleman JVM
 *
 * @author The ProActive Team
 */
public class MiddlemanDeployer {

    private static AOScilabEnvironment paenv_scilab;
    private static AOMatlabEnvironment paenv_matlab;
    private static AODataspaceRegistry reg;
    private static MatSciJVMProcessInterfaceImpl itf;

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("rmi.port"));

        paenv_scilab = (AOScilabEnvironment) PAActiveObject.newActive(AOScilabEnvironment.class.getName(),
                new Object[0]);
        paenv_matlab = (AOMatlabEnvironment) PAActiveObject.newActive(AOMatlabEnvironment.class.getName(),
                new Object[0]);

        reg = (AODataspaceRegistry) PAActiveObject.newActive(AODataspaceRegistry.class.getName(),
                new Object[0]);

        itf = (MatSciJVMProcessInterfaceImpl) PAActiveObject.newActive(MatSciJVMProcessInterfaceImpl.class
                .getName(), new Object[] { paenv_scilab, paenv_matlab });

        MatSciEnvironment stubenv_sci = (MatSciEnvironment) UnicastRemoteObject.exportObject(paenv_scilab);
        MatSciEnvironment stubenv_mat = (MatSciEnvironment) UnicastRemoteObject.exportObject(paenv_matlab);
        DataspaceRegistry stubreg = (DataspaceRegistry) UnicastRemoteObject.exportObject(reg);
        MatSciJVMProcessInterface stubjvm = (MatSciJVMProcessInterface) UnicastRemoteObject.exportObject(itf);
        Registry registry = LocateRegistry.createRegistry(port);
        registry.rebind(AOScilabEnvironment.class.getName(), stubenv_sci);
        registry.rebind(AOMatlabEnvironment.class.getName(), stubenv_mat);
        registry.rebind(DataspaceRegistry.class.getName(), stubreg);
        registry.rebind(MatSciJVMProcessInterface.class.getName(), stubjvm);
    }
}
