/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.util.classloading;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * This classLoader is used on nodes provided by the resource manager to instantiate
 * the executable. If a class is not found locally, then this class is asked to the
 * taskClassCerver associated to this TaskClassLoader.
 *
 * @see TaskClassServer 
 * @author The ProActive team 
 *
 */
public class TaskClassLoader extends ClassLoader {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);

    /** The associated classserver on the scheduler core side */
    // Can be null if no classpath has been set for the job
    private TaskClassServer remoteServer;
    /** The directory containing extra classpath */
    private File extClasspathDir;
    /** Name of the property that set the extra classpath directory */
    public final static String EXT_CLASSPATH_PROPERTY = "pa.scheduler.extraclasspath.dir";

    /**
     * Create a new classloader.
     * @param parent the parent classloader.
     * @param remoteServer The associated classserver on the scheduler core side.
     */
    public TaskClassLoader(ClassLoader parent, TaskClassServer remoteServer) {
        super(parent);
        this.remoteServer = remoteServer;
        // look for the ext classpath dir if any
        try {
            String ecd = System.getProperty(EXT_CLASSPATH_PROPERTY);
            if (ecd != null && !"".equals(ecd)) {
                logger_dev.debug("Extra classpath directory is set to " + ecd);
                File extcp = new File(ecd);
                if (extcp.exists() && extcp.isDirectory() && extcp.canRead()) {
                    this.extClasspathDir = extcp;
                } else {
                    logger_dev.warn(extcp.getAbsolutePath() +
                        " is not a readable directory : cannot use extra classpath directory.");
                }
            } else {
                logger_dev.debug("Extra classpath directory is not set.");
            }
        } catch (SecurityException e) {
            logger_dev.warn("Extra classpath cannot be accessed.", e);
        }

    }

    /* (non-Javadoc)
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return this.findClass(className);
    }

    /* (non-Javadoc)
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    public Class<?> findClass(String className) throws ClassNotFoundException {
        logger_dev.debug("Looking for class " + className);
        Class<?> res = this.findLoadedClass(className);
        if (res != null) {
            // seeked class is already loaded. Return it.
            logger_dev.info("Class " + className + " was already loaded");
            return res;
        } else {
            // try parent
            try {
                res = this.getParent().loadClass(className);
                logger_dev.debug("Found class " + className + " locally");
                return res;
            } catch (ClassNotFoundException e) {
                if (this.extClasspathDir != null) {
                    try {
                        // tries extra classpath dir
                        logger_dev.debug("Look for class " + className + " to the extra classpath");
                        byte[] classBytes = TaskClassUtils.lookIntoDirectory(className, this.extClasspathDir);
                        if (classBytes != null && classBytes.length != 0) {
                            logger_dev.debug("Found " + className + " in extra classpath");
                            res = this.defineClass(className, classBytes, 0, classBytes.length);
                            return res;
                        }
                    } catch (IOException e1) {
                        logger_dev.warn("Cannot access to extra classpath directory.", e1);
                        // try remote server anyway...
                    }
                }
                // if class has not been found locally, tries remote TaskClassServer...
                if (remoteServer != null) {
                    logger_dev.debug("Ask for class " + className + " to the remote TaskClassServer");
                    byte[] classBytes = this.remoteServer.getClassBytes(className);
                    if (classBytes == null || classBytes.length == 0) {
                        logger_dev.debug("Did not find " + className);
                        throw new ClassNotFoundException(className);
                    } else {
                        logger_dev.debug("Found " + className);
                        res = this.defineClass(className, classBytes, 0, classBytes.length);
                        return res;
                    }
                } else {
                    // no remote classserver available...
                    logger_dev.debug("No TaskClassServer found when looking for " + className);
                    throw new ClassNotFoundException(className);
                }
            }
        }
    }

}
