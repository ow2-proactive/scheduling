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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.utils.JVMPropertiesPreloader;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.SecurityPolicyLoader;


/**
 * Class with main which instantiates a resource manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class RMStarter {

    private static Logger logger = Logger.getLogger(RMStarter.class);

    private static Options options = new Options();

    public static final int DEFAULT_NODES_NUMBER = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);

    private static final int DEFAULT_NODE_TIMEOUT = 30 * 1000;

    private static int nodeTimeout = DEFAULT_NODE_TIMEOUT;

    private static void initOptions() {
        Option help = new Option("h", "help", false, "to display this help");
        help.setArgName("help");
        help.setRequired(false);
        options.addOption(help);

        Option noDeploy = new Option("ln",
                                     "localNodes",
                                     false,
                                     "start the resource manager deploying default 4 local nodes");
        noDeploy.setArgName("localNodes");
        noDeploy.setRequired(false);
        options.addOption(noDeploy);

        Option nodeTimeout = new Option("t",
                                        "timeout",
                                        true,
                                        "Timeout used to start the nodes (only useful with local nodes, default: " +
                                              DEFAULT_NODE_TIMEOUT + "ms)");
        nodeTimeout.setArgName("timeout");
        nodeTimeout.setRequired(false);
        options.addOption(nodeTimeout);
    }

    private static void displayHelp() {
        logger.info("");
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(120);
        hf.printHelp("rm-start", options, true);
        logger.info("\n Notice : Without argument, the resource manager starts without any computing node.");
        System.exit(1);
    }

    public static void main(String[] args) {
        configureRMHome();
        configureSecurityManager();
        configureLogging();

        args = JVMPropertiesPreloader.overrideJVMProperties(args);

        initOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                displayHelp();
            }

            logger.info("Starting the resource manager...");
            RMFactory.setOsJavaProperty();

            boolean localNodes = false;
            if (cmd.hasOption("localNodes")) {
                localNodes = true;
                if (cmd.hasOption("timeout")) {
                    String timeout = cmd.getOptionValue("t");
                    try {
                        nodeTimeout = Integer.parseInt(timeout);
                    } catch (Exception e) {
                        logger.error("Wrong value for timeout option: " + timeout, e);
                    }
                }
            }

            // starting clean resource manager
            RMAuthentication auth = RMFactory.startLocal();

            int defaultNodesNumber = PAResourceManagerProperties.RM_NB_LOCAL_NODES.getValueAsInt();

            // -1 means that the number of local nodes depends of the number of cores in the local machine
            if (defaultNodesNumber == -1) {
                defaultNodesNumber = DEFAULT_NODES_NUMBER;
            }

            if (localNodes && defaultNodesNumber > 0) {
                ResourceManager resourceManager = auth.login(Credentials.getCredentials(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
                String nodeSourceName = RMConstants.DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME;

                //first im parameter is default rm url
                byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
                resourceManager.createNodeSource(nodeSourceName,
                                                 LocalInfrastructure.class.getName(),
                                                 new Object[] { creds, defaultNodesNumber, nodeTimeout, "" },
                                                 RestartDownNodesPolicy.class.getName(),
                                                 null,
                                                 NodeSource.DEFAULT_LOCAL_NODES_NODE_SOURCE_RECOVERABLE);
                resourceManager.disconnect();
                logger.info("The resource manager with " + defaultNodesNumber + " local nodes created on " +
                            auth.getHostURL());
            } else {
                logger.info("The resource manager created on " + auth.getHostURL());
            }

        } catch (ParseException e1) {
            displayHelp();
        } catch (Exception e) {
            logger.error("", e);
            System.exit(3);
        }

    }

    private static void configureRMHome() {
        if (System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) == null) {
            System.setProperty(PAResourceManagerProperties.RM_HOME.getKey(), System.getProperty("user.dir"));
        }
        if (System.getProperty(CentralPAPropertyRepository.PA_HOME.getName()) == null) {
            System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(),
                               System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()));
        }
        if (System.getProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName()) == null) {
            System.setProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(),
                               System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) +
                                                                                            "/config/network/server.ini");
        }
    }

    private static void configureSecurityManager() {
        SecurityPolicyLoader.configureSecurityManager(System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) +
                                                      "/config/security.java.policy-server",
                                                      PAResourceManagerProperties.POLICY_RELOAD_FREQUENCY_IN_SECONDS.getValueAsLong());
    }

    private static void configureLogging() {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            String defaultLog4jConfig = System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) +
                                        "/config/log/server.properties";
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(), defaultLog4jConfig);
            PropertyConfigurator.configure(defaultLog4jConfig);
            overrideAppenders();

        }
    }

    /**
     * Wrap existing appenders configured for the root logger in AsyncAppenders
     */
    public static void overrideAppenders() {
        if (PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_ENABLED.getValueAsBoolean()) {
            Logger rootLogger = Logger.getRootLogger();
            Enumeration<?> en = rootLogger.getAllAppenders();
            if (en != null) {
                List<AsyncAppender> newAppenders = new ArrayList<>();
                List<String> appendersToRemove = new ArrayList<>();
                int index = 0;
                while (en.hasMoreElements()) {
                    Appender app = (Appender) en.nextElement();
                    if (app != null && !(app instanceof AsyncAppender)) {
                        AsyncAppender asyncAppender = new AsyncAppender();
                        asyncAppender.setName("MainAsyncAppender_" + index);
                        asyncAppender.setBufferSize(PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_BUFFER_SIZE.getValueAsInt());
                        asyncAppender.addAppender(app);
                        newAppenders.add(asyncAppender);
                        appendersToRemove.add(app.getName());
                        index++;
                    }
                }
                for (String appenderName : appendersToRemove) {
                    rootLogger.removeAppender(appenderName);
                }
                for (Appender newAppender : newAppenders) {
                    rootLogger.addAppender(newAppender);
                }
            }
        }
    }

}
