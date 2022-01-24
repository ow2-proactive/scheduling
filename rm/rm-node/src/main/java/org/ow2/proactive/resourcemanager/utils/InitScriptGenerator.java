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

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;

import lombok.Getter;


public class InitScriptGenerator {

    public static final String DETACHED_MODE_PREFIX_PROPERTY = "%detachedModePrefix%";

    public static final String JAVA_PATH_PROPERTY = "%javaPath%";

    public static final String SCHEDULING_PATH_PROPERTY = "%schedulingPath%";

    public static final String FILE_ENCODING = "%fileEncoding%";

    public static final String JAVA_OPTIONS_PROPERTY = "%javaOptions%";

    public static final String RM_URL_PROPERTY = "%rmUrl%";

    public static final String NODE_NAME_PROPERTY = "%nodeName%";

    public static final String NODE_SOURCE_NAME_PROPERTY = "%nodeSourceName%";

    public static final String CREDENTIALS_PROPERTY = "%credentials%";

    public static final String NUMBER_OF_NODES_PER_INSTANCE_PROPERTY = "%numberOfNodesPerInstance%";

    private static final Logger logger = Logger.getLogger(InitScriptGenerator.class);

    protected static Configuration nsConfig;

    static {
        try {
            //load configuration manager with the NodeSource properties file
            nsConfig = NSProperties.loadConfig();
        } catch (ConfigurationException e) {
            logger.error("Exception when loading NodeSource properties", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a template of the default Linux startup script from {@link NSProperties}
     * @retun a string of the default Linux startup script
     */
    @Getter
    protected String defaultLinuxStartupScript = nsConfig.getString(NSProperties.LINUX_AGENT_STARTUP_SCRIPT);

    /**
     * Fills the properties of the default Linux startup script with variables collected by {@link CommandLineBuilder}.
     * @param startupScriptTemplate the Linux startup script with commands added by the user in the GUI.
     * @param javaPath the path to the bin java.
     * @param schedulingPath the path to the rm home.
     * @param rmUrl the Url of the RM (pamr://0/ by default).
     * @param fileEncoding the file encoding (UTF-8 by default).
     * @param javaOptions the options to be passed to the Java command.
     * @param nodeName the name of the node.
     * @param nodeSourceName the name of the node source.
     * @param credentials the RM credentials or an obfuscated credentials String .
     * @param detached if set to true, the node agent will ignore the RM termination signal.
     * @param numberOfNodesPerInstance number of ProActive nodes to start on the instance.
     * @retun a string of the default Linux startup script
     */
    public static String fillInAgentScriptProperties(String startupScriptTemplate, String javaPath,
            String schedulingPath, String rmUrl, String fileEncoding, String javaOptions, String nodeName,
            String nodeSourceName, String credentials, boolean detached, int numberOfNodesPerInstance) {
        String startupScript = startupScriptTemplate;
        startupScript = startupScript.replace(DETACHED_MODE_PREFIX_PROPERTY, detached ? "nohup" : "");
        startupScript = startupScript.replace(JAVA_PATH_PROPERTY, javaPath);
        startupScript = startupScript.replace(SCHEDULING_PATH_PROPERTY, schedulingPath);
        startupScript = startupScript.replace(FILE_ENCODING, fileEncoding);
        startupScript = startupScript.replace(JAVA_OPTIONS_PROPERTY, javaOptions);
        startupScript = startupScript.replace(NODE_NAME_PROPERTY, nodeName);
        startupScript = startupScript.replace(RM_URL_PROPERTY, rmUrl);
        startupScript = startupScript.replace(NODE_SOURCE_NAME_PROPERTY, nodeSourceName);
        startupScript = startupScript.replace(CREDENTIALS_PROPERTY, credentials);
        startupScript = startupScript.replace(NUMBER_OF_NODES_PER_INSTANCE_PROPERTY,
                                              String.valueOf(numberOfNodesPerInstance));

        return startupScript;
    }

}
