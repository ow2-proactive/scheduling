/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.runtime;

import java.net.URI;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * The <code>RuntimeFactory</code> provides a generic way to create and lookup <code>ProActiveRuntime</code>
 * without protocol specific code (such as RMI, HTTP, IBIS, RMI/SSH).
 * </p><p>
 * <code>RuntimeFactory</code> provides a set of static methods to create and lookup <code>ProActiveRuntime</code>
 * and to associate protocol specific factory to concrete protocols. To create a proActiveRuntime it is only
 * necessary to associate the protocol as parameter. For instance :
 * </p>
 * <pre>
 *                 RuntimeFactory.getProtocolSpecificRuntime("rmi");
 *                 RuntimeFactory.getProtocolSpecificRuntime("http");
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
    private static java.util.HashMap<String, String> protocolFactoryMapping = new java.util.HashMap<String, String>();
    private static java.util.HashMap<String, RuntimeFactory> instanceFactoryMapping =
        new java.util.HashMap<String, RuntimeFactory>();

    static {
        ProActiveConfiguration.load();
        //createClassServer();
        //        registerProtocolFactories();
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
    protected static synchronized void setFactory(String protocol,
        String factoryClassName) {
        if (runtimeLogger.isDebugEnabled()) {
            runtimeLogger.debug("protocol2 =  " + protocol + " " +
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
    protected static synchronized void setFactory(String protocol,
        RuntimeFactory factoryObject) {
        protocolFactoryMapping.put(protocol, factoryObject.getClass().getName());
        instanceFactoryMapping.put(protocol, factoryObject);
    }

    /**
     * Returns true if the given proActiveRuntime belongs to this JVM false else.
     * @return true if the given proActiveRuntime belongs to this JVM false else
     */
    public static boolean isRuntimeLocal(ProActiveRuntime proActiveRuntime) {
        return proActiveRuntime.getVMInformation().getVMID()
                               .equals(UniqueID.getCurrentVMID());
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
            //            defaultRuntime = getProtocolSpecificRuntime(ProActiveConfiguration.getInstance()
            //                                                                              .getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL));
            defaultRuntime = getProtocolSpecificRuntime(PAProperties.PA_COMMUNICATION_PROTOCOL.getValue());
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
        ProActiveRuntimeImpl proActiveRuntime = ProActiveRuntimeImpl.getProActiveRuntime();

        RemoteRemoteObject rro = proActiveRuntime.getRemoteObjectExposer()
                                                 .getRemoteObject(protocol);

        if (rro == null) {
            URI url = RemoteObjectHelper.generateUrl(protocol,
                    URIBuilder.getNameFromURI(URI.create(
                            proActiveRuntime.getURL())));
            proActiveRuntime.getRemoteObjectExposer().activateProtocol(url);
            rro = proActiveRuntime.getRemoteObjectExposer()
                                  .getRemoteObject(protocol);

            //            throw new ProActiveException("Cannot create a ProActiveRuntime based on " + protocol);
        }

        return (ProActiveRuntime) RemoteObjectHelper.generatedObjectStub(new RemoteObjectAdapter(
                rro));
    }

    /**
     * Returns a reference to the ProActiveRuntime created with the given protocol and
     * located at the given url.This url can be either local or remote
     * @param proActiveRuntimeURL
     * @return ProActiveRuntime
     * @throws ProActiveException if the runtime cannot be found
     */
    public static ProActiveRuntime getRuntime(String proActiveRuntimeURL)
        throws ProActiveException {
        runtimeLogger.debug("proActiveRunTimeURL " + proActiveRuntimeURL);

        //do we have any association for this node?
        //String protocol = getProtocol(proActiveRuntimeURL);
        //        RuntimeFactory factory = getFactory(protocol);
        RemoteObject ro = RemoteObjectHelper.lookup(URI.create(
                    proActiveRuntimeURL));

        //proActiveRuntimeURL = removeProtocol(proActiveRuntimeURL, protocol);
        return (ProActiveRuntime) RemoteObjectHelper.generatedObjectStub(ro);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

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
}
