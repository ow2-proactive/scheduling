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
package org.ow2.proactive.scheduler.util.classloading;

/**
 * This classloader is used on nodes provided by the resource manager to instanciate
 * the executable. If a class is not found locally, then this class is asked to the
 * taskClassCerver associated to this TaskClassLoader.
 * @see TaskClassServer 
 * @author The ProActive team 
 *
 */
public class TaskClassLoader extends ClassLoader {

    /** The associated classserver on the scheduler core side */
    // Can be null if no classpath has been set for the job
    private TaskClassServer remoteServer;

    /**
     * Create a new classloader.
     * @param parent the parent classloader.
     * @param remoteServer The associated classserver on the scheduler core side.
     */
    public TaskClassLoader(ClassLoader parent, TaskClassServer remoteServer) {
        super(parent);
        this.remoteServer = remoteServer;
    }

    /* (non-Javadoc)
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    public Class loadClass(String className) throws ClassNotFoundException {
        return this.findClass(className);
    }

    /* (non-Javadoc)
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    public Class findClass(String className) throws ClassNotFoundException {
        Class res = null;
        // try parent
        try {
            res = this.getParent().loadClass(className);
        } catch (ClassNotFoundException e) {
            if (remoteServer != null) {
                // tries remote TaskClassServer...
                byte[] classBytes = this.remoteServer.getClassBytes(className);
                if (classBytes == null || classBytes.length == 0) {
                    throw new ClassNotFoundException(className);
                } else {
                    res = this.defineClass(className, classBytes, 0, classBytes.length);
                }
            } else {
                // no remote classserver available...
                throw e;
            }
        }
        return res;
    }

}
