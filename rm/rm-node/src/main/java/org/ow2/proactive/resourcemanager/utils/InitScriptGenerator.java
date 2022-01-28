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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.log4j.Logger;

import com.google.common.base.Joiner;

import lombok.Getter;


public class InitScriptGenerator {

    private static final String DETACHED_MODE_PREFIX_PROPERTY = "%detachedModePrefix%";

    private static final String JAVA_PATH_PROPERTY = "%javaPath%";

    private static final String SCHEDULING_PATH_PROPERTY = "%schedulingPath%";

    private static final String FILE_ENCODING = "%fileEncoding%";

    private static final String JAVA_OPTIONS_PROPERTY = "%javaOptions%";

    private static final String RM_URL_PROPERTY = "%rmUrl%";

    private static final String NODE_NAME_PROPERTY = "%nodeName%";

    private static final String NODE_SOURCE_NAME_PROPERTY = "%nodeSourceName%";

    private static final String CREDENTIALS_PROPERTY = "%credentials%";

    private static final String NUMBER_OF_NODES_PER_INSTANCE_PROPERTY = "%numberOfNodesPerInstance%";

    private static final String JAVA_URL_PROPERTY = "%javaUrl%";

    private static final String NODE_JAR_URL_PROPERTY = "%nodeJarUrl%";

    private static final String JYTHON_PATH_PROPERTY = "%jythonPath%";

    private static final String NODE_JAR_URL_PROTOCOL_PROPERTY = "%protocol%";

    private static final String NODE_JAR_URL_HOST_IP_PROPERTY = "%hostIp%";

    private static final String NODE_JAR_URL_PORT_PROPERTY = "%port%";

    private static final String NODE_JAR_URL_DIR_PROPERTY = "%nodeJarDir%";

    private static final String NODE_JAR_DIR = "rest/node.jar";

    private static final String DEFAULT_JAVA_URL = "https://s3.amazonaws.com/ci-materials/Latest_jre/jre-8u281-linux-x64.tar.gz";

    private static final String DEFAULT_JYTHON_PATH = "/tmp/node/lib/jython-standalone-2.7.0.jar/Lib";

    private static final Logger logger = Logger.getLogger(InitScriptGenerator.class);

    protected static Configuration nsConfig;

    protected static WebPropertiesLoader webConfig;

    static {
        try {
            //load configuration manager with the NodeSource properties file
            nsConfig = NodeCommandLineProperties.loadConfig();
            webConfig = new WebPropertiesLoader();
        } catch (ConfigurationException e) {
            logger.error("Exception when loading NodeSource properties", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a template of the Standard Linux startup script from {@link NodeCommandLineProperties}
     * @retun a string of the default Linux startup script
     **/
    @Getter
    public static String defaultLinuxStandardCommand = nsConfig.getString(NodeCommandLineProperties.LINUX_Standard_STARTUP_COMMAND);

    /**
     * Get a template of the NodeJar Linux startup script from {@link NodeCommandLineProperties}
     * @retun a string of the NodeJar Linux startup script
     **/
    @Getter
    public static String defaultLinuxNodeJarCommand = nsConfig.getString(NodeCommandLineProperties.LINUX_NODE_JAR_STARTUP_COMMAND);

    /**
     * Fills the properties of the default Linux startup script with variables collected by {@link CommandLineBuilder}.
     * @param startupScript the Linux startup script added by the user in the GUI.
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
    public static String fillInStandardScriptProperties(String startupScript, String javaPath, String schedulingPath,
            String rmUrl, String fileEncoding, String javaOptions, String nodeName, String nodeSourceName,
            String credentials, boolean detached, int numberOfNodesPerInstance) {
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

        startupScript = prepareStartupScript(startupScript);
        return startupScript;
    }

    /**
     * Fills the properties of the default Linux startup script with variables collected by {@link CommandLineBuilder}.
     * @param startupScript the Linux startup script added by the user in the GUI.
     * @param nodeJarUrl the url from which the node.jar file is downloaded.
     * @param rmUrl the Url of the RM (pamr://0/ by default).
     * @param javaOptions the options to be passed to the Java command.
     * @param nodeName the name of the node.
     * @param nodeSourceName the name of the node source.
     * @param credentials the RM credentials or an obfuscated credentials String .
     * @param detached if set to true, the node agent will ignore the RM termination signal.
     * @param numberOfNodesPerInstance number of ProActive nodes to start on the instance.
     * @retun a string of the default Linux startup script
     */

    public static String fillInNodeJarScriptProperties(String startupScript, String nodeJarUrl, String rmUrl,
            String javaOptions, String nodeName, String nodeSourceName, String credentials, boolean detached,
            int numberOfNodesPerInstance) {
        startupScript = startupScript.replace(DETACHED_MODE_PREFIX_PROPERTY, detached ? "nohup" : "");
        startupScript = startupScript.replace(JAVA_URL_PROPERTY, DEFAULT_JAVA_URL);
        startupScript = startupScript.replace(JYTHON_PATH_PROPERTY, DEFAULT_JYTHON_PATH);
        startupScript = startupScript.replace(NODE_JAR_URL_PROPERTY, nodeJarUrl);
        startupScript = startupScript.replace(NODE_SOURCE_NAME_PROPERTY, nodeSourceName);
        startupScript = startupScript.replace(RM_URL_PROPERTY, rmUrl);
        startupScript = startupScript.replace(NODE_NAME_PROPERTY, nodeName);
        startupScript = startupScript.replace(CREDENTIALS_PROPERTY, credentials);
        startupScript = startupScript.replace(NUMBER_OF_NODES_PER_INSTANCE_PROPERTY,
                                              String.valueOf(numberOfNodesPerInstance));
        startupScript = startupScript.replace(JAVA_OPTIONS_PROPERTY, javaOptions);
        startupScript = prepareStartupScript(startupScript);
        return startupScript;
    }

    private static String prepareStartupScript(String startupScript) {
        String preparedStartupScript;
        preparedStartupScript = startupScript.replace("\r\n", "\n");
        List<String> startupScriptList = Arrays.stream(preparedStartupScript.split("\n"))
                                               .filter(s -> !s.isEmpty())
                                               .collect(Collectors.toList());
        preparedStartupScript = Joiner.on("\n").join(startupScriptList);
        return preparedStartupScript;
    }

    public static String createNodeJarUrl() {
        String NodeJarUrl = nsConfig.getString(NodeCommandLineProperties.NODE_JAR_URL_TEMPLATE);
        String ip = getServerPublicIp();
        String protocol = webConfig.getHttpProtocol();
        String port = String.valueOf(webConfig.getRestPort());
        NodeJarUrl = NodeJarUrl.replace(NODE_JAR_URL_PROTOCOL_PROPERTY, protocol);
        NodeJarUrl = NodeJarUrl.replace(NODE_JAR_URL_HOST_IP_PROPERTY, ip);
        NodeJarUrl = NodeJarUrl.replace(NODE_JAR_URL_PORT_PROPERTY, port);
        NodeJarUrl = NodeJarUrl.replace(NODE_JAR_URL_DIR_PROPERTY, NODE_JAR_DIR);

        return NodeJarUrl;
    }

    private static String getServerPublicIp() {
        String ip = "localhost";
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

            String ipValue = in.readLine(); //you get the IP as a String
            if (ipV4Validator(ipValue)) {
                ip = ipValue;
            }
        } catch (Exception e) {
            logger.warn("unable to get the public IP address of the server", e);
            return "localhost";
        }

        return ip;
    }

    private static boolean ipV4Validator(String ip) {
        InetAddressValidator validator = InetAddressValidator.getInstance();
        return validator.isValidInet4Address(ip);
    }
}
