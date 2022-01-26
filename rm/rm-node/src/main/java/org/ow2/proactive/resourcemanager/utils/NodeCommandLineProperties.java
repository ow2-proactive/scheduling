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

import java.io.File;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;


public class NodeCommandLineProperties {

    private NodeCommandLineProperties() {
    }

    private static final Logger LOGGER = Logger.getLogger(NodeCommandLineProperties.class);

    public static final String PROPERTIES_FILE = "NodeCommandLine.properties";

    private static final ListDelimiterHandler DELIMITER = new DisabledListDelimiterHandler();

    public static final String LINUX_JAR_STARTUP_COMMAND = "ns.node.jar.command.line.linux";

    public static final String LINUX_AGENT_STARTUP_COMMAND = "ns.node.agent.command.line.linux";

    /**
     * loads NodeSource configuration.
     *
     * @return NodeSource configuration
     */
    public static Configuration loadConfig() throws ConfigurationException {

        Configuration config;
        File propertiesFile = new File(NodeCommandLineProperties.class.getClassLoader()
                                                                      .getResource(PROPERTIES_FILE)
                                                                      .getFile());
        PropertiesBuilderParameters propertyParameters = new Parameters().properties();
        propertyParameters.setFile(propertiesFile);
        propertyParameters.setThrowExceptionOnMissing(true);
        propertyParameters.setListDelimiterHandler(DELIMITER);

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
        builder.configure(propertyParameters);
        config = builder.getConfiguration();
        LOGGER.debug("NodeSources configuration loaded");

        return config;
    }
}
