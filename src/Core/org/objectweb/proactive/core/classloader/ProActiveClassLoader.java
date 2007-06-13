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

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * This class defines a class loader that can fetch classes from other
 * referenced ProActive runtimes. It is able to look for classes in the
 * classpath and, according to the classloader delegation model, and because it
 * is the system classloader, it will load *all* application classes except the
 * system classes. When asked to load a class, this classloader successively
 * tries to : 1. check if the class is a system class, in that case delegates to
 * the parent classloader (primordial class loader) 2. delegate the loading to
 * the super class URLClassLoader, which looks into the classpath. 3. delegate
 * the search of the class data to a ProActiveClassLoaderHelper, then defines
 * the class from the retreived data (bytecode) (if the local ProActiveRuntime
 * has been created) The ProActiveClassLoaderHelper looks for the given class in
 * other runtimes.
 *
 * @author cmathieu
 * @author acontes
 * @author mmorel
 */
public class ProActiveClassLoader extends URLClassLoader {

    /** The ProActive Runtime class */
    final private static String RUNTIME_CLASSNAME = "org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl";

    /** Is the Runtime class loaded ? */
    private boolean runtimeReady = false;

    /**
     * A ProActiveClassLoaderHelper
     *
     * Need to be an Object otherwise it will be
     * loaded by the System Classloader
     */
    private Object helper;

    /** The method to be used to get class data */
    private Method helper_getClassData;

    /** prefix of classes that must to be loaded by the System classloader */
    final private static String[] JDKPackages = {
            "java", "javax", "sun", "com.sun", "org.xml.sax", "org.omg",
            "org.ietf.jgss", "org.w3c.dom", "com.ibm", "org.jcp", "org.apache"
        };

    public ProActiveClassLoader() {
        this(null);
    }

    /*
     * @see ClassLoader#ClassLoader(java.lang.ClassLoader)
     * @see ClassLoader#getSystemClassLoader()
     */
    @SuppressWarnings("unchecked")
    public ProActiveClassLoader(ClassLoader parent) {
        super(pathToURLs(System.getProperty("java.class.path")), parent);
        try {
            // use a helper class so that the current class does not include any
            // other proactive or application type
            Class proActiveClassLoaderHelper = loadClass(
                    "org.objectweb.proactive.core.classloader.ProActiveClassLoaderHelper");
            helper = proActiveClassLoaderHelper.newInstance();
            helper_getClassData = proActiveClassLoaderHelper.getDeclaredMethod("getClassData",
                    new Class[] { String.class });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // System.out.println("Trying to load " + name);
        Class<?> c = null;

        // Check if the class has been already loaded by this ClassLoader
        if ((c = findLoadedClass(name)) != null) {
            return c;
        }

        if (isJDKClass(name)) {
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
        if (c == null) {
            throw new ClassNotFoundException(name);
        }

        // System.out.println("ProActiveClassloader loaded class : " + name);
        return c;
    }

    /**
     * Looks for the given class in parents, classpath, and if not found
     * delegates the search to a ProActiveClassLoaderHelper
     *
     * @see ClassLoader#findClass(java.lang.String)
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        //		System.out.println("Trying to find " + name);
        Class c = null;
        try {
            // 1. look in classpath (not from the parent classloader)
            c = super.findClass(name);

            if (!runtimeReady && name.equals(RUNTIME_CLASSNAME)) {
                runtimeReady = true;
            }
        } catch (ClassNotFoundException e) {
            // 2. Try to load the class from parent runtime

            // 2.1 ProActive is not ready, load it
            if (!runtimeReady) {
                System.err.println(
                    "Trying to use the ProActiveClassLoading but the Runtime is not ready");
                System.err.println(
                    "Please fill a bug report, this should not happen");

                /* Use super.findClass to load RUNTIME_CLASSNAME if needed */
            }

            //			System.out.println("Using PACLHelper to find : " + name);
            byte[] class_data = null;
            try {
                class_data = (byte[]) helper_getClassData.invoke(helper,
                        new Object[] { name });
                if (class_data != null) {
                    c = defineClass(name, class_data, 0, class_data.length,
                            getClass().getProtectionDomain());
                }
            } catch (Exception e1) {
                throw new ClassNotFoundException(name, e1);
            }
        }

        if (c == null) {
            throw new ClassNotFoundException(name);
        }

        return c;
    }

    /**
     * Does this class belong to the JDK or not ?
     *
     * JDK classes need to be loaded by the System classloader not the ProActive
     * classloader.
     *
     * @param name
     *            class name
     * @return true if the class should be loaded by the System classloader
     */
    protected boolean isJDKClass(String name) {
        if (name == null) {
            return false;
        }

        // Looking up the package
        String packageName = null;
        int pos = name.lastIndexOf('.');
        if (pos != -1) {
            packageName = name.substring(0, pos);
        } else {
            return false;
        }

        for (int i = 0; i < JDKPackages.length; i++) {
            if (packageName.startsWith(JDKPackages[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     * Transforms the string classpath to and URL array based classpath.
     *
     * The classpath string must be separated with the filesystem path
     * separator.
     *
     * @param _classpath
     *            a classpath string
     * @return URL[] array of wellformed URL's
     * @throws MalformedURLException
     *             if a malformed URL has occurred in the classpath string.
     */
    public static URL[] pathToURLs(String _classpath) {
        StringTokenizer tok = new StringTokenizer(_classpath, File.pathSeparator);
        ArrayList<String> pathList = new ArrayList<String>();

        while (tok.hasMoreTokens()) {
            pathList.add(tok.nextToken());
        }

        URL[] urlArray = new URL[pathList.size()];

        int count = 0;
        for (int i = 0; i < pathList.size(); i++) {
            try {
                urlArray[i] = (new File((String) pathList.get(i))).toURI()
                               .toURL();
                count++;
            } catch (MalformedURLException e) {
                System.out.println("MalformedURLException occured for " +
                    urlArray[i].toString() +
                    " during the ProActiveClassLoader creation");
            }
        }

        if (count != pathList.size()) {
            // A MalformedURLException occured
            URL[] tmpUrlArray = new URL[count];
            System.arraycopy(urlArray, 0, tmpUrlArray, 0, count);
        }

        return urlArray;
    }
}
