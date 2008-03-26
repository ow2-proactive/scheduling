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
package org.objectweb.proactive.core.util;

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A cache for classes bytecode.
 * It also contains bytecodes of stubs generated
 * by the MOP.
 *
 * @author The ProActive Team
 *
 */
public class ClassDataCache {
    static Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);
    private static ClassDataCache classCache = null;
    private static Map<String, byte[]> classStorage;

    private ClassDataCache() {
        classStorage = new Hashtable<String, byte[]>();
    }

    public static ClassDataCache instance() {
        if (classCache == null) {
            return classCache = new ClassDataCache();
        } else {
            return classCache;
        }
    }

    /**
     * Indicates whether the bytecode for the given class is already in cache.
     * @param className name of the class
     * @return true if the class is in the cache
     */
    public boolean contains(String className) {
        return classStorage.containsKey(className);
    }

    /**
     * Associates classname and bytecode in the cache.
     * @param fullname name of the class
     * @param classData bytecode of the class
     */
    public void addClassData(String fullname, byte[] classData) {
        if (logger.isDebugEnabled()) {
            logger.debug(ProActiveRuntimeImpl.getProActiveRuntime().getURL() + " --> " +
                ("ClassDataCache caching class " + fullname));
        }
        classStorage.put(fullname, classData);
    }

    /**
     * Returns the bytecode for a given class name
     * @param fullname the name of the class
     */
    public byte[] getClassData(String fullname) {
        if (logger.isDebugEnabled()) {
            logger.debug(ProActiveRuntimeImpl.getProActiveRuntime().getURL() + " --> " +
                ("ClassDataCache was asked for class " + fullname));
        }
        return classStorage.get(fullname);
    }
}
