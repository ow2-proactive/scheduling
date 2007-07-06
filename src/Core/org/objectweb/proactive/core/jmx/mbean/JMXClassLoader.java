package org.objectweb.proactive.core.jmx.mbean;


/**
 * This class is used to add a class loader to the MBean Server repository.
 * See JMX Specification, version 1.4 ; Chap 8.4.1 : 'A class loader is added to the repository if it is registered as an MBean'.
 * @author ProActive Team
 */
public class JMXClassLoader extends ClassLoader implements JMXClassLoaderMBean {
    public JMXClassLoader() {

        /* Empty Constructor required by JMX */
    }

    /**
     * Creates a new IC2DClassLoader.
     * @param parent
     */
    public JMXClassLoader(ClassLoader parent) {
        super(parent);
    }
}
