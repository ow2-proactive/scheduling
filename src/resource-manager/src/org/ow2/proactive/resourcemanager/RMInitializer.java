/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 *  Initial developer(s): ActiveEon Team - www.activeeon.com
 *
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager;

import java.io.File;

import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


/**
 * RMInitializer is used to initialize property for the Resource Manager start up.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class RMInitializer {

    private String paConfiguration;
    private String security;
    private String log4j;
    private String rmHome;
    private String rmProperties;

    /**
     * Set the ProActive configuration file to be used.
     * This file is used to define the behavior of ProActive.
     *
     * @see org.objectweb.proactive.core.config.PAProperties for more details.
     *
     * @param filePath the absolute file path to the PA configuration file to use.
     */
    public void setProActiveConfiguration(String filePath) {
        if (filePath == null || !new File(filePath).exists()) {
            throw new RuntimeException("File " + filePath + " does not exist !");
        }
        paConfiguration = filePath;
    }

    /**
     * Set the java security policy file to be used.
     * This is exactly what you would put in the "java.security.policy" property.
     *
     * @param filePath the absolute file path to the java security policy file to use.
     */
    public void setJavaSecurityPolicy(String filePath) {
        if (filePath == null || !new File(filePath).exists()) {
            throw new RuntimeException("File " + filePath + " does not exist !");
        }
        System.setProperty("java.security.policy", filePath);
        security = filePath;
    }

    /**
     * Set the log4j configuration file to be used on this instance.
     *
     * @see org.objectweb.proactive.core.config.PAProperties for more details.
     *
     * @param filePath the absolute file path to the log4j configuration file to use.
     */
    public void setLog4jConfiguration(String filePath) {
        if (filePath == null || !new File(filePath).exists()) {
            throw new RuntimeException("File " + filePath + " does not exist !");
        }
        System.setProperty("log4j.configuration", filePath.startsWith("file:") ? filePath : "file:" +
            filePath);
        log4j = filePath;
    }

    /**
     * Set the home directory for the Resource Manager.
     * This property is used to resolve every relative paths in the configuration file.<br />
     * You can leave this property without value if every specified paths are absolute in the configuration file.
     * The default value is the current directory.
     *
     * @param homeDir the home directory of Resource Manager. (used to resolve relative path)
     */
    public void setRMHomePath(String homeDir) {
        if (homeDir == null || !new File(homeDir).exists()) {
            throw new RuntimeException("Directory " + homeDir + " does not exist !");
        }
        if (!new File(homeDir).isDirectory()) {
            throw new RuntimeException("RM Home path must be a directory !");
        }
        System.setProperty(PAResourceManagerProperties.RM_HOME.getKey(), homeDir);
        rmHome = homeDir;
    }

    /**
     * Set the RM Property configuration file.
     * This is the main file that contains every RM properties.
     * THIS PROPERTY MUST BE SET.
     *
     * @param filePath the absolute file path to Resource Manager properties file.
     */
    public void setResourceManagerPropertiesConfiguration(String filePath) {
        if (filePath == null || !new File(filePath).exists()) {
            throw new RuntimeException("File " + filePath + " does not exist !");
        }
        rmProperties = filePath;
    }

    /**
     * Get the ProActive configuration file
     *
     * @return the ProActive configuration file
     */
    public String getProActiveConfiguration() {
        return paConfiguration;
    }

    /**
     * Get the absolute file path to the java security policy file
     *
     * @return the absolute file path to the java security policy file
     */
    public String getJavaSecurityPolicy() {
        return security;
    }

    /**
     * Get the log4j configuration file
     *
     * @return the log4j configuration file
     */
    public String getLog4jConfiguration() {
        return log4j;
    }

    /**
     * Get the home directory of Resource Manager.
     *
     * @return the home directory of Resource Manager.
     */
    public String getRMHomePath() {
        return rmHome;
    }

    /**
     * Get the RM Property configuration file.
     *
     * @return the RM Property configuration file.
     */
    public String getResourceManagerPropertiesConfiguration() {
        return rmProperties;
    }

}
