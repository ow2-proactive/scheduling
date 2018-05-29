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
package org.ow2.proactive.boot.microservices.util;

import java.io.File;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;


public class IAMConfiguration {

    public static final String PROPERTIES_FILE = "iam.properties";

    public static final String ARCHIVE_NAME = "iam.archive.name";

    public static final String STARTUP_TIMEOUT = "iam.startup.timeout";

    public static final String READY_MARKER = "iam.ready.marker";

    public static final String JVM_ARGS = "iam.jvm.args";

    private static final Logger LOGGER = Logger.getLogger(IAMConfiguration.class);

    private IAMConfiguration() {

    }

    public static Configuration loadConfig(File configFile) throws ConfigurationException {

        Configuration config = new BaseConfiguration();

        ListDelimiterHandler delimiter = new DefaultListDelimiterHandler(',');

        PropertiesBuilderParameters propertyParameters = new Parameters().properties();
        propertyParameters.setFile(configFile);
        propertyParameters.setThrowExceptionOnMissing(true);
        propertyParameters.setListDelimiterHandler(delimiter);

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);

        builder.configure(propertyParameters);

        config = builder.getConfiguration();

        return config;
    }
}
