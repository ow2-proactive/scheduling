/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.utils.ChildFirstClassLoader;


/**
 * This class helps the addon classes to use a child-first class loading mechanism for class loading and creating instances.
 * Loading the addon class with a child-first delegation mechanims allows addons to use their specific version of dependent library.
 *
 * @author ActiveEon Team
 * @since 03/12/19
 */
public class AddonClassUtils {
    // store the corresponding class loader for each addon (key is the addon class name)
    private static Map<String, ClassLoader> addonClassLoaderMap = new HashMap<>();

    /**
     * Check whether the addon class should be loaded from addons jars.
     * The class is considered to be loaded from addons jar, iff server addons directory contains a directory named as addonName (addon simple class name in minuscule).
     *
     * @param addonFullClassName The complete class name of the addon
     * @return whether the addon class should be loaded from addons jar
     */
    public static boolean isAddon(String addonFullClassName) {
        return AddonPathUtils.existDirectoryInAddons(getAddonsDirectoryName(addonFullClassName));
    }

    /**
     * Get the proper class loader for the class which could be an addon.
     * If the class is an addon, it should use its proper child-first class loader.
     * Otherwise, the originalClassLoader is used.
     *
     * The created classloaders are stored and reused when asked for the same class next time.
     *
     * @param addonClassName The complete class name of the addon
     * @param originalClassLoader The parent class loader of the addon class loader
     * @return the proper class loader for the class
     */
    public static ClassLoader getAddonClassLoader(String addonClassName, ClassLoader originalClassLoader) {
        ClassLoader addonClassLoader = addonClassLoaderMap.get(addonClassName);
        if (addonClassLoader == null) {
            if (isAddon(addonClassName)) {
                URL[] addonJarsUrl = findAddonJars(addonClassName);
                addonClassLoader = new ChildFirstClassLoader(addonJarsUrl, originalClassLoader);
                addonClassLoaderMap.put(addonClassName, addonClassLoader);
            } else {
                addonClassLoader = originalClassLoader;
            }
        }
        return addonClassLoader;
    }

    /**
     * Load the class which could be an addon with the proper class loader.
     * If the class is an addon, the class loader is set as thread context class loader during class initialization.
     *
     * @param addonClassName the name of class to be loaded.
     * @param originalClassLoader the original class loader used when the class is not an addon.
     * @return the class object of given class name
     * @throws ClassNotFoundException if the class can't be located
     */
    public static Class<?> loadClass(String addonClassName, ClassLoader originalClassLoader)
            throws ClassNotFoundException {
        // when the class is an addon, load it with a specific class loader, and inject thread context class loader during initializing class.
        if (AddonClassUtils.isAddon(addonClassName)) {
            ClassLoader classLoader = getAddonClassLoader(addonClassName, originalClassLoader);
            try {
                return ContextClassLoaderInjector.switchContextClassLoader(classLoader,
                                                                           () -> Class.forName(addonClassName,
                                                                                               true,
                                                                                               classLoader));
            } catch (ClassNotFoundException e) {
                throw e;
            } catch (Exception ex) {
                throw new RuntimeException("Error when loading addon class " + addonClassName, ex);
            }
        } else {
            return Class.forName(addonClassName);
        }

    }

    /**
     * Create a new instance of the given class which could be an addon.
     * If the class is an addon, the class loader of addon class are injected as thread context class loader to the instance methods.
     *
     * @param addonClass the class object which could be an addon class
     * @return a newly allocated instance of the given class
     */
    public static Object instantiateAddon(Class<?> addonClass) {
        try {
            // when the class is an addon (which is loaded by a specific class loader), its methods need to inject setting of thread context class loader.
            if (AddonClassUtils.isAddon(addonClass.getName())) {
                return ContextClassLoaderInjector.createInjectedObject(addonClass, addonClass.getClassLoader());
            } else {
                return addonClass.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error when instantiating the addon " + addonClass, e);
        }
    }

    /**
     * Get the addon jars URL which should located in the sub-directory (named as addon simple class name in minuscule) of dir addons.
     *
     * @param addonClassName the complete class name of the addon
     * @return URL array where locate the addon jars
     */
    private static URL[] findAddonJars(String addonClassName) {
        String addonDirName = getAddonsDirectoryName(addonClassName);
        return AddonPathUtils.findJarsInPath(AddonPathUtils.getPathInAddons(addonDirName));
    }

    /**
     * Get the addon directory name from its complete class name, which is its simple class name in minuscule.
     *
     * @param fullClassName complete class name of addon
     * @return the directory name of the addon jar
     */
    private static String getAddonsDirectoryName(String fullClassName) {
        return getSimpleClassName(fullClassName).toLowerCase();
    }

    private static String getSimpleClassName(String fullClassName) {
        int simpleNameBeginIndex = fullClassName.lastIndexOf(".") + 1;
        if (simpleNameBeginIndex >= fullClassName.length()) {
            throw new IllegalArgumentException(fullClassName + " is not a valid class name.");
        }
        return fullClassName.substring(simpleNameBeginIndex);
    }
}
