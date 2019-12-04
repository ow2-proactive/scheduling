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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 *
 * ChildFirstClassLoader first trys to load the class from the specified jars then the parent class loader.
 *
 * @author ActiveEon Team
 * @since 28/11/19
 */
public class ChildFirstClassLoader extends URLClassLoader {

    private static final Logger logger = Logger.getLogger(ChildFirstClassLoader.class);

    public ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // check whether the class already loaded
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            logger.debug("loading ..." + name);
            try {
                // find the class from given jar urls as in first constructor parameter.
                loadedClass = findClass(name);
                logger.debug(name + " loaded from jar " + Arrays.toString(getURLs()));
            } catch (ClassNotFoundException e) {
                logger.debug(name + " not found in jar " + Arrays.toString(getURLs()));
                logger.debug(name + " loading from parent " + getParent());
                // class is not found in the given urls, try it in parent classloader.
                // If class is still not found, then this method will throw class not found ex.
                loadedClass = super.loadClass(name, resolve);
            }
        }
        if (resolve) {
            resolveClass(loadedClass);
        }
        return loadedClass;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> allRes = new LinkedList<>();
        // load resource from this classloader
        allRes.addAll(Collections.list(findResources(name)));
        // then try finding resources from parent classloaders
        allRes.addAll(Collections.list(super.getResources(name)));

        return Collections.enumeration(allRes);
    }

    @Override
    public URL getResource(String name) {
        URL res = findResource(name);
        if (res == null) {
            res = super.getResource(name);
        }
        return res;
    }
}
