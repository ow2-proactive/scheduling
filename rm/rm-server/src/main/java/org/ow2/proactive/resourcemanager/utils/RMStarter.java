/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.io.File;
import java.security.Policy;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.utils.JVMPropertiesPreloader;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * Class with main which instantiates a resource manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class RMStarter {

    private static Logger logger = Logger.getLogger(RMStarter.class);

    private static Options options = new Options();

    private static final int DEFAULT_NUMBER_OF_NODES = 4;
    private static final int DEFAULT_NODE_TIMEOUT = 30 * 1000;
    private static int nodeTimeout = DEFAULT_NODE_TIMEOUT;

    private static void initOptions() {
        Option help = new Option("h", "help", false, "to display this help");
        help.setArgName("help");
        help.setRequired(false);
        options.addOption(help);

        Option noDeploy = new Option("ln", "localNodes", false,
            "start the resource manager deploying default " + DEFAULT_NUMBER_OF_NODES + " local nodes");
        noDeploy.setArgName("localNodes");
        noDeploy.setRequired(false);
        options.addOption(noDeploy);

        Option nodeTimeout = new Option("t", "timeout", true,
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

        Parser parser = new GnuParser();
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

            if (localNodes) {
                ResourceManager resourceManager = auth.login(Credentials
                        .getCredentials(PAResourceManagerProperties
                                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
                String nodeSourceName = NodeSource.LOCAL_INFRASTRUCTURE_NAME;

                //first im parameter is default rm url
                byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(
                    PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS
                            .getValueAsString())));
                resourceManager.createNodeSource(nodeSourceName, LocalInfrastructure.class.getName(),
                        new Object[] { "", creds, DEFAULT_NUMBER_OF_NODES, nodeTimeout, "" },
                        RestartDownNodesPolicy.class.getName(), null);
                resourceManager.disconnect();
                logger.info("The resource manager with " + DEFAULT_NUMBER_OF_NODES +
                    " local nodes created on " + auth.getHostURL());
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
                "/config/proactive/ProActiveConfiguration.xml");
        }
    }

    private static void configureSecurityManager() {
        if (System.getProperty("java.security.policy") == null) {
            System.setProperty("java.security.policy", System.getProperty(
              PAResourceManagerProperties.RM_HOME.getKey()) + "/config/security.java.policy-server");
            Policy.getPolicy().refresh();
        }
    }

    private static void configureLogging() {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            String defaultLog4jConfig = System.getProperty(
              PAResourceManagerProperties.RM_HOME.getKey()) + "/config/log4j/rm-log4j-server";
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(),
              defaultLog4jConfig);
            PropertyConfigurator.configure(defaultLog4jConfig);
        }
    }

}
