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
                                                         .toString().startsWith("org.objectweb.proactive.core.classloader.ProActiveClassLoader");

    public ProActiveRMIClassLoaderSpi() {
        if (useProActiveClassLoader) {
            // enforce defaultLoader
            proActiveClassLoader = ClassLoader.getSystemClassLoader();
        }
    }

    /**
     * see {@link RMIClassLoaderSpi#loadClass(java.lang.String, java.lang.String, java.lang.ClassLoader)}
     * If the System ClassLoader is ProActiveClassLoader, this classLoader is given to the defaultProviderInstance as
     * the default loader for this method.
     */
    public Class loadClass(String codebase, String name,
        ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException {
        return rmiClassLoaderSpi.loadClass(codebase, name,
            ((proActiveClassLoader != null) ? proActiveClassLoader : defaultLoader));
    }

    /**
     * see {@link RMIClassLoaderSpi#loadProxyClass(java.lang.String, java.lang.String[], java.lang.ClassLoader)}
     * If the System ClassLoader is ProActiveClassLoader, this classLoader is given to the defaultProviderInstance as
     * the default loader for this method.
     */
    public Class loadProxyClass(String codebase, String[] interfaces,
        ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException {
        return rmiClassLoaderSpi.loadProxyClass(codebase, interfaces,
            ((proActiveClassLoader != null) ? proActiveClassLoader : defaultLoader));
    }

    /*
     * see {@link RMIClassLoaderSpi#getClassLoader(java.lang.String)}
     */
    public ClassLoader getClassLoader(String codebase)
        throws MalformedURLException {
        if (proActiveClassLoader != null) {
            return proActiveClassLoader;
        } else {
            return rmiClassLoaderSpi.getClassLoader(codebase);
        }
    }

    /*
     * see {@link RMIClassLoaderSpi#getClassAnnotation(java.lang.Class)}
     */
    public String getClassAnnotation(Class arg0) {
        return rmiClassLoaderSpi.getClassAnnotation(arg0);
    }
}
