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

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;


/**
 *
 * Provides a generic way to create an infrastructure manager.
 * Loads all supported infrastructure names from a config file. Checks that required
 * infrastructure is supported at creation time.
 *
 */
public class InfrastructureManagerFactory {

    /** list of supported infrastructures */
    private static Collection<Class<?>> supportedInfrastructures;

    /**
     * Creates new infrastructure manager using reflection mechanism.
     *
     * @return new infrastructure manager
     */
    public static InfrastructureManager create(NodeSourceData nodeSourceData) {
        InfrastructureManager im = null;
        String infrastructureType = nodeSourceData.getInfrastructureType();
        Object[] infrastructureParameters = nodeSourceData.getInfrastructureParameters();
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

            Class<?> imClass = Class.forName(infrastructureType);
            im = (InfrastructureManager) imClass.newInstance();
            im.internalConfigure(infrastructureParameters);
            im.setPersistedNodeSourceData(nodeSourceData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return im;
    }

    /**
     * Creates a new infrastructure manager and recovers its state thanks to
     * the variables contained in {@param infrastructureVariables}.
     *
     * @param nodeSourceData the persisted information about the node source
     * @return recovered infrastructure manager
     */
    public static InfrastructureManager recover(NodeSourceData nodeSourceData) {
        InfrastructureManager infrastructure = create(nodeSourceData);
        infrastructure.recoverPersistedInfraVariables(nodeSourceData.getInfrastructureVariables());
        return infrastructure;
    }

    /**
     * Loads a list of supported infrastructures from a configuration file
     * @return list of supported infrastructures
     */
    public static Collection<Class<?>> getSupportedInfrastructures() {
        // reload file each time as it can be updated while the rm is running
        Properties properties = new Properties();
        try {
            String propFileName = PAResourceManagerProperties.RM_NODESOURCE_INFRASTRUCTURE_FILE.getValueAsString();
            if (!(new File(propFileName).isAbsolute())) {
                // file path is relative, so we complete the path with the prefix RM_Home constant
                propFileName = PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator + propFileName;
            }

            FileInputStream stream = new FileInputStream(propFileName);
            properties.load(stream);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        supportedInfrastructures = new ArrayList<>(properties.size());

        for (Object className : properties.keySet()) {
            try {
                Class<?> cls = Class.forName(className.toString());
                supportedInfrastructures.add(cls);
            } catch (ClassNotFoundException e) {
                Logger.getLogger(InfrastructureManagerFactory.class).warn("Cannot find class " + className.toString());
            }
        }

        return supportedInfrastructures;
    }
}
