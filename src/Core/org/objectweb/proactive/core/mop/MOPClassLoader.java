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
package org.objectweb.proactive.core.mop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class MOPClassLoader extends URLClassLoader {
    static Logger logger = ProActiveLogger.getLogger(Loggers.MOP);

    // retreives the optionnal byteCodeManipulator JVM arg
    // javassist is used by default
    public static String BYTE_CODE_MANIPULATOR = PAProperties.PA_BYTECODEMANIPULATOR.isSet()
        ? PAProperties.PA_BYTECODEMANIPULATOR.getValue() : "javassist";
    protected static Hashtable<String, byte[]> classDataCache = new Hashtable<String, byte[]>();
    protected static MOPClassLoader mopCl = null;

    /**
     * Return the unique MOPClassLoader for the current JVM
     * Create it if it does not exist
     */
    public static synchronized MOPClassLoader getMOPClassLoader() {
        if (MOPClassLoader.mopCl == null) {
            MOPClassLoader.mopCl = MOPClassLoader.createMOPClassLoader();
        }
        return MOPClassLoader.mopCl;
    }

    public MOPClassLoader() {
        super(new URL[] {  });
    }

    /**
     * Get the bytecode of a stub given its name. If the stub can not be found
     * the cache, the MOPClassLoader tries to generate it.
     * @param classname The name of the stub class
     * @return An array representing the bytecode of the stub, null if the
     *  stub could not be found or created
     */
    public byte[] getClassData(String classname) {
        byte[] cb = null;
        cb = classDataCache.get(classname);
        if (cb == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("MOPClassLoader: class " + classname +
                    " not found, trying to generate it");
            }
            try {
                this.loadClass(classname);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            cb = classDataCache.get(classname);
        }
        return cb;
    }

    private MOPClassLoader(ClassLoader parent, URL[] urls) {
        super(urls, parent);
    }

    public void launchMain(String[] args) throws Throwable {
        try {
            // Looks up the class that contains main
            Class<?> cl = Class.forName(args[0], true, this);

            // Looks up method main
            Class<?>[] argTypes = { args.getClass() };
            Method mainMethod = cl.getMethod("main", argTypes);

            // And calls it
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);

            Object[] mainArgs = { newArgs };
            mainMethod.invoke(null, mainArgs);
        } catch (ClassNotFoundException e) {
            logger.error("Launcher: cannot find class " + args[0]);
        } catch (NoSuchMethodException e) {
            logger.error("Launcher: class " + args[0] +
                " does not contain have method void 'public void main (String[])'");
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        return;
    }

    protected static MOPClassLoader createMOPClassLoader() {
        // Gets the current classloader
        ClassLoader currentClassLoader = null;

        try {
            Class<?> c = Class.forName(
                    "org.objectweb.proactive.core.mop.MOPClassLoader");
            currentClassLoader = c.getClassLoader();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        URL[] urls = null;

        // Checks if the current classloader is actually an instance of
        // java.net.URLClassLoader, or of one of its subclasses.
        if (currentClassLoader instanceof java.net.URLClassLoader) {
            // Retrieves the set of URLs from the current classloader     
            urls = ((URLClassLoader) currentClassLoader).getURLs();
        } else {
            urls = new URL[0];
        }

        // Creates a new MOPClassLoader
        return new MOPClassLoader(currentClassLoader, urls);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, null, null, false);
    }

    public Class<?> loadClass(String name, Class<?>[] genericParameters)
        throws ClassNotFoundException {
        return this.loadClass(name, genericParameters, null, false);
    }

    public Class<?> loadClass(String name, Class<?>[] genericParameters,
        ClassLoader cl) throws ClassNotFoundException {
        return this.loadClass(name, genericParameters, cl, false);
    }

    protected synchronized Class<?> loadClass(String name,
        Class<?>[] genericParameters, ClassLoader cl, boolean resolve)
        throws ClassNotFoundException {
        if (this.getParent() != null) {
            try {
                return this.getParent().loadClass(name);
            } catch (ClassNotFoundException e) {
                // proceeding
            }
        } else {
            //ok, we don't have any parent, so maybe we previously
            //defined the stub class using the context class loader
            //we check here
            try {
                return Thread.currentThread().getContextClassLoader()
                             .loadClass(name);
            } catch (ClassNotFoundException e) {
                //no luck, proceed
            }
        }
        try {
            if (cl != null) {
                return cl.loadClass(name);
            } else {
                return Class.forName(name);
            }
        } catch (ClassNotFoundException e) {
            // Test if the name of the class is actually a request for
            // a stub class to be created
            if (Utils.isStubClassName(name)) {
                logger.info("Generating class : " + name);
                //    e.printStackTrace();
                String classname = Utils.convertStubClassNameToClassName(name);

                byte[] data = null;

                //                if (BYTE_CODE_MANIPULATOR.equals("ASM")) {
                //                    ASMBytecodeStubBuilder bsb = new ASMBytecodeStubBuilder(classname);
                //                    data = bsb.create();
                //                    MOPClassLoader.classDataCache.put(name, data);
                //                } else 
                if (BYTE_CODE_MANIPULATOR.equals("javassist")) {
                    data = JavassistByteCodeStubBuilder.create(classname,
                            genericParameters);
                    MOPClassLoader.classDataCache.put(name, data);
                } else {
                    // that shouldn't happen, unless someone manually sets the BYTE_CODE_MANIPULATOR static variable
                    logger.error(
                        "byteCodeManipulator argument is optionnal. If specified, it can only be set to javassist (ASM is no longer supported).");
                    logger.error(
                        "Any other setting will result in the use of javassist, the default bytecode manipulator framework");
                }

                // We use introspection to invoke the defineClass method to avoid the normal 
                // class Access checking. This method is supposed to be protected which means 
                // we should not be accessing it but the access policy file allows us to access it freely.
                try {
                    Class<?> clc = Class.forName("java.lang.ClassLoader");
                    Class<?>[] argumentTypes = new Class<?>[5];
                    argumentTypes[0] = name.getClass();
                    argumentTypes[1] = data.getClass();
                    argumentTypes[2] = Integer.TYPE;
                    argumentTypes[3] = Integer.TYPE;
                    argumentTypes[4] = Class.forName(
                            "java.security.ProtectionDomain");

                    Method m = clc.getDeclaredMethod("defineClass",
                            argumentTypes);
                    m.setAccessible(true);

                    Object[] effectiveArguments = new Object[5];
                    effectiveArguments[0] = name;
                    effectiveArguments[1] = data;
                    effectiveArguments[2] = new Integer(0);
                    effectiveArguments[3] = new Integer(data.length);
                    effectiveArguments[4] = this.getClass().getProtectionDomain();

                    //  we have been loaded through the bootclasspath
                    // so we use the context classloader
                    if (this.getParent() == null) {
                        return (Class<?>) m.invoke(Thread.currentThread()
                                                         .getContextClassLoader(),
                            effectiveArguments);
                    } else {
                        return (Class<?>) m.invoke(this.getParent(),
                            effectiveArguments);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new ClassNotFoundException(ex.getMessage());
                }
            } else {
                logger.debug("Cannot generate class " + name +
                    " as a stub class");
                throw e;
            }
        }
    }
}
