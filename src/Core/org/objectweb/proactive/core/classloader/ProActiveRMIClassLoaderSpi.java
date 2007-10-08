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
package org.objectweb.proactive.core.classloader;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;


/**
 * <p>This class allows dynamic remote downloading of classes from other proactive runtimes when using
 * RMI as the communication protocol, or when streams  are marshalled using the RMI mechanism.</p>
 *
 * <p>It is instantiated and invoked by the RMIClassLoader when loading classes while deserializing an incoming stream. It
 * delegates calls to the default provider instance of the RMIClassLoaderSpi</p>
 *
 * <p>If the System ClassLoader is the ProActiveClassLoader, then delegated calls to the default provider instance
 * of RMIClassLoader are given ProActiveClassLoader as the defaultLoader parameters. This ensures that
 * ProActiveClassLoader is used, otherwise, the sun.rmi.MarshalInputStream which deserializes the stream will use
 * "the first class loader on the execution stack", which can be different from ProActiveClassLoader. </p>
 *
 *<p><b>In the current implementation, there are no classes downloaded through http using the codebase path</b></p>
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveRMIClassLoaderSpi extends RMIClassLoaderSpi {
    private ClassLoader proActiveClassLoader = null;
    RMIClassLoaderSpi rmiClassLoaderSpi = RMIClassLoader.getDefaultProviderInstance();
    private boolean useProActiveClassLoader = ClassLoader.getSystemClassLoader()
                                                         .toString()
                                                         .startsWith("org.objectweb.proactive.core.classloader.ProActiveClassLoader");

    public ProActiveRMIClassLoaderSpi() {
        if (useProActiveClassLoader) {
            // enforce defaultLoader
            proActiveClassLoader = ClassLoader.getSystemClassLoader();
        }
    }

    /**
     * If the System ClassLoader is ProActiveClassLoader, this classLoader is given to the defaultProviderInstance as
     * the default loader for this method.
     * @see RMIClassLoaderSpi#loadClass(java.lang.String, java.lang.String, java.lang.ClassLoader)
     */
    @Override
    public Class<?> loadClass(String codebase, String name,
        ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException {
        return rmiClassLoaderSpi.loadClass(codebase, name,
            ((proActiveClassLoader != null) ? proActiveClassLoader : defaultLoader));
    }

    /**
     * If the System ClassLoader is ProActiveClassLoader, this classLoader is given to the defaultProviderInstance as
     * the default loader for this method.
     * @see RMIClassLoaderSpi#loadProxyClass(java.lang.String, java.lang.String[], java.lang.ClassLoader)
     */
    @Override
    public Class<?> loadProxyClass(String codebase, String[] interfaces,
        ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException {
        return rmiClassLoaderSpi.loadProxyClass(codebase, interfaces,
            ((proActiveClassLoader != null) ? proActiveClassLoader : defaultLoader));
    }

    /*
     * @see RMIClassLoaderSpi#getClassLoader(java.lang.String)
     */
    @Override
    public ClassLoader getClassLoader(String codebase)
        throws MalformedURLException {
        if (proActiveClassLoader != null) {
            return proActiveClassLoader;
        } else {
            return rmiClassLoaderSpi.getClassLoader(codebase);
        }
    }

    /*
     * @see RMIClassLoaderSpi#getClassAnnotation(java.lang.Class)
     */
    @Override
    public String getClassAnnotation(Class<?> arg0) {
        return rmiClassLoaderSpi.getClassAnnotation(arg0);
    }
}
