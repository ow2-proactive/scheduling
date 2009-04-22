/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;


/**
 *
 * Provides a generic way to create an infrastructure manager.
 * Loads all supported infrastructure names from a config file. Checks that required
 * infrastructure is supported at creation time.
 *
 */
public class InfrastructureManagerFactory {

    /** list of supported infrastructures */
    private static ArrayList<String> supportedInfrastructures;

    /**
     * Creates new infrastructure manager using reflection mechanism.
     *
     * @param infrastructureType a full class name of an infrastructure manager
     * @param infrastructureParameters parameters for nodes acquisition
     * @return new infrastructure manager
     * @throws RMException if any problems occurred
     */
    public static InfrastructureManager create(String infrastructureType, Object[] infrastructureParameters)
            throws RMException {

        InfrastructureManager im = null;
        try {
            if (!getSupportedInfrastructures().contains(infrastructureType)) {
                throw new RMException(infrastructureType + " is not supported");
            }

            Class<?> imClass = Class.forName(infrastructureType);
            im = (InfrastructureManager) imClass.newInstance();
            im.addNodesAcquisitionInfo(infrastructureParameters);
        } catch (ClassNotFoundException e) {
            throw new RMException(e);
        } catch (InstantiationException e) {
            throw new RMException(e);
        } catch (IllegalAccessException e) {
            throw new RMException(e);
        }
        return im;
    }

    /**
     * Loads a list of supported infrastructures from a configuration file
     * @return list of supported infrastructures
     */
    public static ArrayList<String> getSupportedInfrastructures() {
        if (supportedInfrastructures == null) {
            supportedInfrastructures = new ArrayList<String>();
            Properties properties = new Properties();
            try {
                String propFileName = PAResourceManagerProperties.RM_NODESOURCE_INFRASTRUCTURE_FILE
                        .getValueAsString();
                if (!(new File(propFileName).isAbsolute())) {
                    // file path is relative, so we complete the path with the prefix RM_Home constant
                    propFileName = PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator +
                        propFileName;
                }

                properties.load(new FileInputStream(propFileName));
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (Object className : properties.keySet()) {
                try {
                    Class<?> cls = Class.forName(className.toString());
                    supportedInfrastructures.add(cls.getName());
                } catch (ClassNotFoundException e) {
                }
            }
        }
        return supportedInfrastructures;
    }
}
