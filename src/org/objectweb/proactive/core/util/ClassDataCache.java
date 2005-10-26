/*
 * Created on 2 oct. 2004
 *
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
 * @author Matthieu Morel
 *
 */
public class ClassDataCache {
    static Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);
    private static ClassDataCache classCache = null;
    private static Map classStorage;
    private static String runtimeURL = null;

    private ClassDataCache() {
        classStorage = new Hashtable();
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
            logger.debug(ProActiveRuntimeImpl.getProActiveRuntime().getURL() +
                " --> " + ("ClassDataCache caching class " + fullname));
        }
        classStorage.put(fullname, classData);
    }

    /**
     * Returns the bytecode for a given class name
     * @param fullname the name of the class
     */
    public byte[] getClassData(String fullname) {
        if (logger.isDebugEnabled()) {
            logger.debug(ProActiveRuntimeImpl.getProActiveRuntime().getURL() +
                " --> " + ("ClassDataCache was asked for class " + fullname));
        }
        return (byte[]) classStorage.get(fullname);
    }
}
