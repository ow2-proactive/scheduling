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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.nodesource.NodeSourceDescriptor;
import org.ow2.proactive.resourcemanager.utils.AddonClassUtils;


/**
 *
 * Provides a generic way to create an infrastructure manager.
 * Loads all supported infrastructure names from a config file. Checks that required
 * infrastructure is supported at creation time.
 *
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
            for (String supportedInfra : getSupportedInfrastructuresName()) {
                if (supportedInfra.equals(infrastructureType)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                throw new IllegalArgumentException(infrastructureType + " is not supported");
            }

            Class<?> imClass = AddonClassUtils.loadClass(infrastructureType, originalClassLoader);
            im = (InfrastructureManager) AddonClassUtils.instantiateAddon(imClass);
            im.internalConfigure(infrastructureParameters);
            im.setPersistedNodeSourceData(NodeSourceData.fromNodeSourceDescriptor(nodeSourceDescriptor));
        } catch (Exception e) {
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
     * Get a list of supported infrastructures name from a configuration file
     *
     * @return list of supported infrastructures name
     */
    public static Collection<String> getSupportedInfrastructuresName() {
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
        return properties.keySet().stream().map(Object::toString).collect(Collectors.toList());
    }

    /**
     * Load the list of supported infrastructures
     *
     * @return list of supported infrastructures class
     */
    public static Collection<Class<?>> getSupportedInfrastructures() {
        Collection<Class<?>> supportedInfastructures = new ArrayList<>();

        for (String infraClassName : getSupportedInfrastructuresName()) {
            try {
                supportedInfastructures.add(AddonClassUtils.loadClass(infraClassName, originalClassLoader));
            } catch (ClassNotFoundException e) {
                logger.error("Cannot find infrastructure class " + infraClassName);
            }
        }

        return supportedInfastructures;
    }
}
