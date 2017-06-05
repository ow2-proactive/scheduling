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
package org.ow2.proactive.scheduler.common.util.logforwarder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.DefaultRepositorySelector;


/**
 * @author ActiveEon Team
 * @since 01/06/2017
 */
public class Log4JRemover {

    private static final Logger logger = Logger.getLogger(Log4JRemover.class);

    public static void removeLogger(String name) {
        removeLogger(name, null);
    }

    public static void removeLogger(String name, Hierarchy hierarchy) {
        try {

            Hierarchy usedHierarchy;
            if (hierarchy == null) {
                Field repositorySelectorField = LogManager.class.getDeclaredField("repositorySelector");
                repositorySelectorField.setAccessible(true);
                DefaultRepositorySelector selector = (DefaultRepositorySelector) repositorySelectorField.get(null);

                Field repositoryField = DefaultRepositorySelector.class.getDeclaredField("repository");

                repositoryField.setAccessible(true);
                usedHierarchy = (Hierarchy) repositoryField.get(selector);
            } else {
                usedHierarchy = hierarchy;
            }

            Field htField = Hierarchy.class.getDeclaredField("ht");
            htField.setAccessible(true);

            Hashtable ht = (Hashtable) htField.get(usedHierarchy);

            Class categoryKeyclazz = Class.forName("org.apache.log4j.CategoryKey");
            Constructor<?> categoryKeyConstructor = categoryKeyclazz.getDeclaredConstructor(String.class);

            categoryKeyConstructor.setAccessible(true);

            Object key = categoryKeyConstructor.newInstance(name);

            Logger logger;
            synchronized (ht) {
                // remove the logger entry and inside all its parents reference
                logger = (Logger) ht.remove(key);
                if (logger != null) {
                    String parentLogger = null;
                    while ((parentLogger = getParentLogger(name)) != null) {
                        key = categoryKeyConstructor.newInstance(parentLogger);
                        Vector categoryEntries = (Vector) ht.get(key);
                        categoryEntries.remove(logger);
                        if (categoryEntries.isEmpty()) {
                            // remove the parent category if empty
                            ht.remove(key);
                        }
                        name = parentLogger;
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Unable to access Log4J logger table, logger removal will be disabled", e);
        }
    }

    private static String getParentLogger(String loggerName) {
        if (loggerName.indexOf(".") >= 0) {
            return loggerName.substring(0, loggerName.lastIndexOf("."));
        } else {
            return null;
        }

    }
}
