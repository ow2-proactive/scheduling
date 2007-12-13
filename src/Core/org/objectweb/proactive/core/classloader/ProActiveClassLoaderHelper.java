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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.ClassDataCache;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>Instances of this class are created and used from the ProActiveClassLoader by reflection,
 * in order to work in a dedicated namespace.</p>
 *
 * <p>This class provides a method for getting the bytecode of a given class, from the cache, from other
 * runtimes, or by generating it.</p>
 *
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveClassLoaderHelper {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);
    private ClassDataCache classCache;

    public ProActiveClassLoaderHelper() {
        classCache = ClassDataCache.instance();
    }

    /**
     * Looks for the bytecode of the given class in different places :<ul>
     * <li>1. cache</li>
     * <li>2. runtime parents</li>
     * <li>3. tries to generate it (stub, component interface representative, or component interface metaobject)</li></ul>
     */
    public synchronized byte[] getClassData(String className) throws ClassNotFoundException {
        byte[] class_data = null;

        // 1. look in class cache
        debug("looking for " + className + "  in class data cache");
        class_data = classCache.getClassData(className);
        if (class_data != null) {
            debug("found " + className + " in class data cache");
            return class_data;
        }

        // 2. look in runtime parents
        try {
            debug("looking for " + className + " in parent runtimes");

            class_data = ProActiveRuntimeImpl.getProActiveRuntime().getClassDataFromParentRuntime(className);
            if (class_data != null) {
                debug("found " + className + " in ancestor runtime");
                return class_data;
            }
        } catch (NullPointerException e) {
            // The PART is probably not ready 
            // class downloading is not yet available

            //System.out.println("XXX name=" + className);
            //e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. standard proactive stub?
        if (Utils.isStubClassName(className)) {
            // do not use directly MOP methods
            logger.info("Generating class : " + className);
            //    e.printStackTrace();
            String classname = Utils.convertStubClassNameToClassName(className);
            class_data = JavassistByteCodeStubBuilder.create(classname, null);

            if (class_data != null) {
                classCache.addClassData(className, class_data);
                return class_data;
            }
        }

        if (class_data != null) {
            return class_data;
        }

        // component-generated?
        class_data = org.objectweb.proactive.core.component.gen.Utils.getClassData(className);

        if (class_data != null) {
            classCache.addClassData(className, class_data);
            return class_data;
        }

        throw new ClassNotFoundException(className);
    }

    private void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(ProActiveRuntimeImpl.getProActiveRuntime().getURL() + " --> " + message);
        }
    }
}
