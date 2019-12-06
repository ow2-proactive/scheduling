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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.nodesource.NodeSourceDescriptor;
import org.ow2.proactive.resourcemanager.utils.AddonClassUtils;
import org.ow2.proactive.resourcemanager.utils.ChildFirstClassLoader;


/**
 * Provides a generic way to create an infrastructure manager.
 * Loads all supported infrastructure names from a config file. Checks that required
 * infrastructure is supported at creation time.
 */
public class InfrastructureManagerFactory {

    private static final Logger logger = Logger.getLogger(InfrastructureManagerFactory.class);

    // the class loader of this class (usually it is the system class loader)
    private static ClassLoader originalClassLoader = InfrastructureManagerFactory.class.getClassLoader();

    /**
     * Creates new infrastructure manager using reflection mechanism.
     *
     * @return new infrastructure manager
     */
    public static InfrastructureManager create(NodeSourceDescriptor nodeSourceDescriptor) {
        InfrastructureManager im = null;
        String infrastructureType = nodeSourceDescriptor.getInfrastructureType();
        Object[] infrastructureParameters = nodeSourceDescriptor.getInfrastructureParameters();
        try {

            boolean supported = false;
            for (Class<?> cls : getSupportedInfrastructures()) {
                if (cls.getName().equals(infrastructureType)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                throw new IllegalArgumentException(infrastructureType + " is not supported");
            }
            Class<?> imClass = loadInfrastructureClass(infrastructureType);
            Thread.currentThread().setContextClassLoader(imClass.getClassLoader());
            im = (InfrastructureManager) imClass.newInstance();
            im.internalConfigure(infrastructureParameters);
            im.setPersistedNodeSourceData(NodeSourceData.fromNodeSourceDescriptor(nodeSourceDescriptor));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return im;
    }

    /**
     * Creates a new infrastructure manager and recovers its state thanks to
     * the variables contained in the nodeSourceDescriptor.
     *
     * @param nodeSourceDescriptor the persisted information about the node source
     * @return recovered infrastructure manager
     */
    public static InfrastructureManager recover(NodeSourceDescriptor nodeSourceDescriptor) {
        InfrastructureManager infrastructure = create(nodeSourceDescriptor);
        infrastructure.recoverPersistedInfraVariables(nodeSourceDescriptor.getLastRecoveredInfrastructureVariables());
        return infrastructure;
    }

    /**
     * Loads a list of supported infrastructures from a configuration file
     *
     * @return list of supported infrastructures
     */
    public static Collection<Class<?>> getSupportedInfrastructures() {
        // reload file each time as it can be updated while the rm is running
        Properties properties = new Properties();
        String propFileName = null;
        try {
            propFileName = PAResourceManagerProperties.RM_NODESOURCE_INFRASTRUCTURE_FILE.getValueAsString();
            if (!(new File(propFileName).isAbsolute())) {
                // file path is relative, so we complete the path with the prefix RM_Home constant
                propFileName = PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator + propFileName;
            }

            try (FileInputStream stream = new FileInputStream(propFileName)) {
                properties.load(stream);
            }
        } catch (Exception e) {
            logger.error("Error when loading infrastructure definition file : " + propFileName, e);
        }

        Collection<Class<?>> supportedInfrastructures = new ArrayList<>(properties.size());

        for (Object className : properties.keySet()) {
            try {
                Class<?> cls = loadInfrastructureClass(className.toString());
                supportedInfrastructures.add(cls);
            } catch (ClassNotFoundException e) {
                logger.error("Cannot find infrastructure class " + className.toString());
            } catch (Exception e) {
                logger.error("Error when getSupportedInfrastructures : ", e);
                e.printStackTrace();
            }
        }

        return supportedInfrastructures;
    }

    /**
     * Load the infrastructure class with a child-first delegation mechanims class loader.
     * This child-first class loader allows the different infrastructures use their specific version of dependent library.
     * To ensure the infrastructure class always use the correct class loader, both its class loader and thread context class loader are specified.
     *
     * @param infraClassName the complete class name of the infrastructure
     * @return the loaded class of the infrastructure
     * @throws ClassNotFoundException
     */
    private static Class<?> loadInfrastructureClass(String infraClassName) throws ClassNotFoundException {
        ClassLoader classLoader = AddonClassUtils.getAddonClassLoader(infraClassName, originalClassLoader);
        Class<?> imClass = Class.forName(infraClassName, true, classLoader);
        logger.debug(imClass.getName() + " use class loader: " + imClass.getClassLoader());
        if (imClass.getClassLoader() instanceof URLClassLoader) {
            logger.debug("class loader url:" + Arrays.toString(((URLClassLoader) imClass.getClassLoader()).getURLs()));
        }
        return imClass;
    }
}
