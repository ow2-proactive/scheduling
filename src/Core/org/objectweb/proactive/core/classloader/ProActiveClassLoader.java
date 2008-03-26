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
 * tries to load the class by using:
 * <ol>
 *         <li>the URLClassLoader (classpath)</li>
 *         <li>the SystemClassLoader (JDK)</li>
 *         <li>the ProActiveClassLoader (Dynamic downloading)</li>
 * <ol>
 *
 * The ProActiveClassLoaderHelper looks for the given class in
 * other runtimes.
 *
 * @author The ProActive Team
 * @author The ProActive Team
 * @author The ProActive Team
 */
public class ProActiveClassLoader extends URLClassLoader {

    /** The ProActive Runtime class */
    final private static String RUNTIME_CLASSNAME = "org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl";

    /**
     * A ProActiveClassLoaderHelper
     *
     * Need to be an Object otherwise it will be loaded by the System
     * Classloader
     */
    private Object helper;

    /** The method to be used to get class data */
    private Method helper_getClassData;

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
            Class proActiveClassLoaderHelper = loadClass("org.objectweb.proactive.core.classloader.ProActiveClassLoaderHelper");
            helper = proActiveClassLoaderHelper.newInstance();
            helper_getClassData = proActiveClassLoaderHelper.getDeclaredMethod("getClassData",
                    new Class[] { String.class });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> c = null;

        // Check if the class has been already loaded by this ClassLoader
        if ((c = findLoadedClass(name)) != null) {
            return c;
        }

        // We don't want to find some classes but don't know really why ! 
        if (name.equals("org.objectweb.proactive.core.ssh.http.Handler")) {
            throw new ClassNotFoundException(name);
        }

        // do not attempt to download any 1.2- rmi skeleton
        if (name.endsWith("_Skel")) {
            throw new ClassNotFoundException(name);
        }

        c = findClass(name);
        if (c == null) {
            throw new ClassNotFoundException(name);
        }

        return c;
    }

    /**
     * Looks for the given class using three different classloaders
     *
     * Three classloaders are used in the following order:
     *  <ol>
     *          <li>URLClassLoader</li>
     *          <li>SystemClassLoader</li>
     *          <li>ProActiveClassLoaderHelper</li>
     *  </ol>
     *
     * @see ClassLoader#findClass(java.lang.String)
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        //                 System.out.println("Trying to find " + name);
        Class<?> c = null;
        try {
            /* 1- URLClassLoader: find classes inside the classpath */
            c = super.findClass(name);
            //             System.out.println("Loaded " + name + " using the URLClassLoader");
        } catch (ClassNotFoundException e) {
            try {
                /* 2- SystemClassLoader: find classes inside the JDK */
                c = getParent().loadClass(name);
                //                 System.out.println("Loaded " + name + " using the SystemClassLoader");
            } catch (ClassNotFoundException ex) {

                /* 3- ProActiveClassLoaderHelper: find classes from Parent Runtime */
                byte[] class_data = null;
                try {
                    class_data = (byte[]) helper_getClassData.invoke(helper, new Object[] { name });
                    if (class_data != null) {
                        c = defineClass(name, class_data, 0, class_data.length, getClass()
                                .getProtectionDomain());
                    }

                    //                     System.out.println("Loaded " + name + " using the PAClassLoader");
                } catch (Exception e1) {
                    throw new ClassNotFoundException(name, e1);
                }
            }
        }

        if (c == null) {
            throw new ClassNotFoundException(name);
        }

        return c;
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
                urlArray[i] = (new File(pathList.get(i))).toURI().toURL();
                count++;
            } catch (MalformedURLException e) {
                System.out.println("MalformedURLException occured for " + urlArray[i].toString() +
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
