/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.runtime;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.rmi.ClassServerHelper;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * The <code>RuntimeFactory</code> provides a generic way to create and lookup <code>ProActiveRuntime</code>
 * without protocol specific code (such as RMI, HTTP, IBIS, RMI/SSH, JINI).
 * </p><p>
 * <code>RuntimeFactory</code> provides a set of static methods to create and lookup <code>ProActiveRuntime</code>
 * and to associate protocol specific factory to concrete protocols. To create a proActiveRuntime it is only
 * necessary to associate the protocol as parameter. For instance :
 * </p>
 * <pre>
 *                 RuntimeFactory.getProtocolSpecificRuntime("rmi");
 *                 RuntimeFactory.getProtocolSpecificRuntime("jini");
 * </pre>
 * <p>
 * As long as a protocol specific factory has been registered to this <code>RuntimeFactory</code> for the
 * given protocol, the creation of the ProActiveRuntime will be delegated to the right factory.
 * </p><p>
 * This class also provide the concept of default protocol and default ProActiveRuntime. When an active object is created
 * in the local JVM but without being attached to any node , a default node is created in the default ProActiveRuntime(with the default protocol)
 * associated with the JVM to hold that active object.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/08/28
 * @since   ProActive 0.9
 *
 */
public abstract class RuntimeFactory {
    public static Logger runtimeLogger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    // test with class loader
    //private static final ClassLoader myClassLoader = new NodeClassLoader();

    /** the table where associations Protocol - Factory are kept */
    private static java.util.HashMap protocolFactoryMapping = new java.util.HashMap();
    private static java.util.HashMap instanceFactoryMapping = new java.util.HashMap();

    //private static ProActiveRuntime defaultRuntime = null;
    //private static RuntimeFactory defaultRuntimeFactory;
    //    public static boolean JINI_ENABLED;
    //    public static boolean IBIS_ENABLED;
    public static boolean XMLHTTP_ENABLED;

    static {
        ProActiveConfiguration.load();
        createClassServer();
        //        JINI_ENABLED = isJiniEnabled();
        //        IBIS_ENABLED = isIbisEnabled();
        XMLHTTP_ENABLED = isXmlHttpEnabled();
        registerProtocolFactories();
        //getLocalRuntime();
    }

    //
    // -- PUBLIC METHODS - STATIC -----------------------------------------------
    //

    /**
     * Associates the factory of class <code>factoryClassName</code> as the factory to create
     * proactiveRuntime for the given protocol. Replaces any previous association.
     * @param protocol the protocol to associate the factory to
     * @param factoryClassName the fully qualified name of the class of the factory
     * responsible of creating the proActiveRuntime for that protocol
     */
    public static synchronized void setFactory(String protocol,
        String factoryClassName) {
        if (runtimeLogger.isDebugEnabled()) {
            runtimeLogger.debug("protocol =  " + protocol + " " +
                factoryClassName);
        }
        protocolFactoryMapping.put(protocol, factoryClassName);
    }

    /**
     * Associates the factory of class <code>factoryClassName</code> as the factory to create
     * proactiveRuntime for the given protocol. Replaces any previous association.
     * @param protocol the protocol to associate the factory to
     * @param factoryObject the class of the factory
     * responsible of creating the proactiveRuntime for that protocol
     */
    public static synchronized void setFactory(String protocol,
        RuntimeFactory factoryObject) {
        protocolFactoryMapping.put(protocol, factoryObject.getClass().getName());
        instanceFactoryMapping.put(protocol, factoryObject);
    }

    /**
     * Returns true if the given proActiveRuntime belongs to this JVM false else.
     * @return true if the given proActiveRuntime belongs to this JVM false else
     */
    public static boolean isRuntimeLocal(ProActiveRuntime proActiveRuntime) {
        return proActiveRuntime.getVMInformation().getVMID().equals(UniqueID.getCurrentVMID());
    }

    /**
     * Returns the reference of the only one instance of the default ProActiveRuntime associated with the local JVM.
     * If this runtime does not yet exist, it creates it with the default protocol.
     * @return The only one ProActiveRuntime associated with the local JVM
     * @throws ProActiveException if the default runtime cannot be created
     */
    public static synchronized ProActiveRuntime getDefaultRuntime()
        throws ProActiveException {
        ProActiveRuntime defaultRuntime = null;
        try {
            //defaultRuntime = getProtocolSpecificRuntime(Constants.DEFAULT_PROTOCOL_IDENTIFIER);
            defaultRuntime = getProtocolSpecificRuntime(System.getProperty(
                        "proactive.communication.protocol") + ":");
            if (runtimeLogger.isDebugEnabled()) {
                runtimeLogger.debug("default runtime = " +
                    defaultRuntime.getURL());
            }
        } catch (ProActiveException e) {
            //e.printStackTrace();
            if (runtimeLogger.isDebugEnabled()) {
                runtimeLogger.debug("Error with the default ProActiveRuntime");
            }
            throw new ProActiveException("Error when getting the default ProActiveRuntime",
                e);
        }
        return defaultRuntime;
    }

    /**
     * Returns the reference of the only one instance of the ProActiveRuntime
     * created with the given protocol, associated with the local JVM.
     * If this runtime does not yet exist, it creates it with the given protocol.
     * @param protocol
     * @return ProActiveRuntime
     * @throws ProActiveException if this ProActiveRuntime cannot be created
     */
    public static ProActiveRuntime getProtocolSpecificRuntime(String protocol)
        throws ProActiveException {
        RuntimeFactory factory = getFactory(protocol);
        ProActiveRuntime proActiveRuntime = factory.getProtocolSpecificRuntimeImpl();
        if (proActiveRuntime == null) {
            throw new ProActiveException(
                "Cannot create a ProActiveRuntime based on " + protocol);
        }
        return proActiveRuntime;
    }

    /**
     * Returns a reference to the ProActiveRuntime created with the given protocol and
     * located at the given url.This url can be either local or remote
     * @param proActiveRuntimeURL
     * @param protocol
     * @return ProActiveRuntime
     * @throws ProActiveException if the runtime cannot be found
     */
    public static ProActiveRuntime getRuntime(String proActiveRuntimeURL,
        String protocol) throws ProActiveException {
        runtimeLogger.debug("proActiveRunTimeURL " + proActiveRuntimeURL + " " +
            protocol);

        //do we have any association for this node?
        //String protocol = getProtocol(proActiveRuntimeURL);
        RuntimeFactory factory = getFactory(protocol);

        //proActiveRuntimeURL = removeProtocol(proActiveRuntimeURL, protocol);
        if (runtimeLogger.isDebugEnabled()) {
            runtimeLogger.debug("factory = " + factory);
        }
        return factory.getRemoteRuntimeImpl(proActiveRuntimeURL);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Creates an Adapter for the given RemoteProActiveRuntime
     * @param remoteProActiveRuntime object we will create an Adapter for
     * @return the newly created Adpater for the given RemoteProActiveRuntime
     * @throws ProActiveException if a pb occurs during the creation
     */
    protected ProActiveRuntimeAdapterImpl createRuntimeAdapter(
        RemoteProActiveRuntime remoteProActiveRuntime)
        throws ProActiveException {
        return new ProActiveRuntimeAdapterImpl(remoteProActiveRuntime);
    }

    /**
     * Returns the reference of the only one instance of the ProActiveRuntime
     * associated with the local JVM.
     * If this runtime does not yet exist, it creates it with the associated protocol.
     * @return ProActiveRuntime
     * @throws ProActiveException if this ProActiveRuntime cannot be created
     */
    protected abstract ProActiveRuntime getProtocolSpecificRuntimeImpl()
        throws ProActiveException;

    /**
     * Returns the reference to the proActiveRuntime located at s
     */
    protected abstract ProActiveRuntime getRemoteRuntimeImpl(String s)
        throws ProActiveException;

    /**
     * Creates a new Adapter
     * @return the newly created Adapter
     * @throws ProActiveException if a pb occurs during the creation
     */
    protected abstract ProActiveRuntimeAdapterImpl createRuntimeAdapter()
        throws ProActiveException;

    //
    // -- PRIVATE METHODS - STATIC -----------------------------------------------
    //
    private static void createClassServer() {
        try {
            new ClassServerHelper().initializeClassServer();
        } catch (Exception e) {
            if (runtimeLogger.isInfoEnabled()) {
                runtimeLogger.info("Error with the ClassServer : " +
                    e.getMessage());
            }
        }
    }

    private static void registerProtocolFactories() {
        setFactory(Constants.JINI_PROTOCOL_IDENTIFIER,
            "org.objectweb.proactive.core.runtime.jini.JiniRuntimeFactory");

        setFactory(Constants.IBIS_PROTOCOL_IDENTIFIER,
            "org.objectweb.proactive.core.runtime.ibis.IbisRuntimeFactory");

        setFactory(Constants.XMLHTTP_PROTOCOL_IDENTIFIER,
            "org.objectweb.proactive.core.runtime.http.HttpRuntimeFactory");

        setFactory(Constants.RMISSH_PROTOCOL_IDENTIFIER,
            "org.objectweb.proactive.core.runtime.rmi.SshRmiRuntimeFactory");

        setFactory(Constants.RMI_PROTOCOL_IDENTIFIER,
            "org.objectweb.proactive.core.runtime.rmi.RmiRuntimeFactory");
    }

    //    private static boolean isJiniEnabled() {
    //        try {
    //            // test if Jini is available
    //            Class.forName("net.jini.discovery.DiscoveryManagement");
    //            if (runtimeLogger.isInfoEnabled()) {
    //                runtimeLogger.info("Jini enabled");
    //            }
    //            return true;
    //        } catch (ClassNotFoundException e) {
    //            if (runtimeLogger.isInfoEnabled()) {
    //                runtimeLogger.info("Jini disabled");
    //            }
    //            return false;
    //        }
    //    }
    //    private static boolean isIbisEnabled() {
    //        try {
    //            // test if Ibis is available
    //            Class.forName("ibis.rmi.server.UnicastRemoteObject");
    //            if (runtimeLogger.isInfoEnabled()) {
    //                runtimeLogger.info("Ibis enabled");
    //            }
    //            return true;
    //        } catch (ClassNotFoundException e) {
    //            if (runtimeLogger.isInfoEnabled()) {
    //                runtimeLogger.info("Ibis disabled");
    //            }
    //            return false;
    //        }
    //    }
    private static boolean isXmlHttpEnabled() {
        return true;
    }

    private static RuntimeFactory createRuntimeFactory(Class factoryClass,
        String protocol) throws ProActiveException {
        try {
            RuntimeFactory nf = (RuntimeFactory) factoryClass.newInstance();
            instanceFactoryMapping.put(protocol, nf);
            return nf;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProActiveException("Error while creating the factory " +
                factoryClass.getName() + " for the protocol " + protocol);
        }
    }

    private static RuntimeFactory createRuntimeFactory(
        String factoryClassName, String protocol) throws ProActiveException {
        Class factoryClass = null;
        if (runtimeLogger.isDebugEnabled()) {
            runtimeLogger.debug("factoryClassName " + factoryClassName);
        }
        try {
            factoryClass = Class.forName(factoryClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ProActiveException(
                "Error while getting the class of the factory " +
                factoryClassName + " for the protocol " + protocol);
        }
        return createRuntimeFactory(factoryClass, protocol);
    }

    private static synchronized RuntimeFactory getFactory(String protocol)
        throws ProActiveException {
        if (runtimeLogger.isDebugEnabled()) {
            runtimeLogger.debug("protocol = " + protocol);
        }

        RuntimeFactory factory = (RuntimeFactory) instanceFactoryMapping.get(protocol);
        if (factory != null) {
            return factory;
        }

        String factoryClassName = (String) protocolFactoryMapping.get(protocol);
        if (runtimeLogger.isDebugEnabled()) {
            runtimeLogger.debug("factoryClassName  = " + factoryClassName);
        }
        if (factoryClassName != null) {
            return createRuntimeFactory(factoryClassName, protocol);
        }
        throw new ProActiveException(
            "No RuntimeFactory is registered for the protocol " + protocol);
    }
}
