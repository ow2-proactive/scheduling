package org.objectweb.proactive.core.classloader;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

import sun.misc.URLClassPath;


/**
 * This class defines a class loader that can fetch classes from other referenced ProActive runtimes.
 * It is able to look for classes in the classpath and, according to the classloader delegation model, and because
 * it is the system classloader, it will load *all* application classes except the system classes.
 * When asked to load a class, this classloader successively tries to :
 * 1. check if the class is a system class, in that case delegates to the parent classloader (primordial class loader)
 * 2. delegate the loading to the super class URLClassLoader, which looks into the classpath.
 * 3. delegate the search of the class data to a ProActiveClassLoaderHelper, then defines the class from
 * the retreived data (bytecode) (if the local ProActiveRuntime has been created)
 * The ProActiveClassLoaderHelper looks for the given class in other runtimes.
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveClassLoader extends URLClassLoader {
    boolean runtimeReady = false;
    private Object helper;
    private Method helper_getClassData;

    public ProActiveClassLoader() {
        super(URLClassPath.pathToURLs(System.getProperty("java.class.path")));
    }

    /**
     * see {@link ClassLoader#ClassLoader(java.lang.ClassLoader)} and
     * {@link ClassLoader#getSystemClassLoader()}
     */
    public ProActiveClassLoader(ClassLoader parent) {
        super(URLClassPath.pathToURLs(System.getProperty("java.class.path")),
            parent);
        try {
            // use a helper class so that the current class does not include any other proactive or application type 
            Class proActiveClassLoaderHelper = loadClass(
                    "org.objectweb.proactive.core.classloader.ProActiveClassLoaderHelper");
            helper = proActiveClassLoaderHelper.newInstance();
            helper_getClassData = proActiveClassLoaderHelper.getDeclaredMethod("getClassData",
                    new Class[] { String.class });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * see {@link ClassLoader#findClass(java.lang.String)}
     * Looks for the given class in parents, classpath, and if not found delegates
     * the search to a ProActiveClassLoaderHelper
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        Class c = null;
        try {
            // 1. look in parents and classpath
            c = super.findClass(name);
        } catch (ClassNotFoundException e) {
            if (runtimeReady) {
                byte[] class_data = null;
                try {
                    // 2. search for class data using helper
                    class_data = (byte[]) helper_getClassData.invoke(helper,
                            new Object[] { name });
                    if (class_data != null) {
                        c = defineClass(name, class_data, 0, class_data.length,
                                getClass().getProtectionDomain());
                    }
                } catch (Exception e1) {
                    throw new ClassNotFoundException(name, e1);
                }
            } else {
                throw e;
            }
        }
        if (c != null) {
            return c;
        } else {
            throw new ClassNotFoundException(name);
        }
    }

    /*
     * see {@link ClassLoader#loadClass(java.lang.String)}
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        Class c = null;
        if ((c = findLoadedClass(name)) != null) {
            return c;
        }
        if (name.startsWith("java.") || name.startsWith("javax.") ||
                name.startsWith("sun.") || name.startsWith("com.sun.") ||
                name.startsWith("org.xml.sax") || name.startsWith("org.omg") ||
                name.startsWith("org.ietf.jgss") ||
                name.startsWith("org.w3c.dom")) {
            return getParent().loadClass(name);
        }
        if (name.equals("org.objectweb.proactive.core.ssh.http.Handler")) {
            // class does not exist
            throw new ClassNotFoundException(name);
        }
        // FIXME temporary walkaround
        if (name.endsWith("_Skel")) {
            // do not attempt to download any 1.2- rmi skeleton
            throw new ClassNotFoundException(name);
        }
        c = findClass(name);
        if (name.equals(
                    "org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl")) {
            runtimeReady = true;
        }
        if (c != null) {
            // System.out.println("ProActiveClassloader loaded class : " + name);
        } else {
            throw new ClassNotFoundException(name);
        }
        return c;
    }
}
