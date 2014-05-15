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
package org.ow2.proactive.scheduler.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.rmi.AlreadyBoundException;
import java.security.KeyException;
import java.security.Policy;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.utils.JVMPropertiesPreloader;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.scheduler.SchedulerFactory;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.exception.AdminSchedulerException;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.JettyStarter;
import org.ow2.proactive.utils.Tools;
import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * SchedulerStarter will start a new Scheduler on the local host connected to the given Resource Manager.<br>
 * If no Resource Manager is specified, it will try first to connect a local one. If not succeed, it will create one on
 * the localHost started with 4 local nodes.<br>
 * The scheduling policy can be specified at startup. If not given, it will use the default one.<br>
 * Start with -h option for more help.<br>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SchedulerStarter {
    //shows how to run the scheduler
    private static Logger logger = Logger.getLogger(SchedulerStarter.class);

    public static final int DEFAULT_NODES_NUMBER = 4;
    public static final int DEFAULT_NODES_TIMEOUT = 30 * 1000;

    /**
     * Start the scheduler creation process.
     */
    public static void main(String[] args) {
        configureSchedulerAndRMAndPAHomes();
        configureSecurityManager();
        configureLogging();

        args = JVMPropertiesPreloader.overrideJVMProperties(args);

        Options options = new Options();

        Option help = new Option("h", "help", false, "to display this help");
        help.setArgName("help");
        help.setRequired(false);
        options.addOption(help);

        Option rmURL = new Option("u", "rmURL", true, "the resource manager URL (default localhost)");
        rmURL.setArgName("rmURL");
        rmURL.setRequired(false);
        options.addOption(rmURL);

        Option policy = new Option(
            "p",
            "policy",
            true,
            "the complete name of the scheduling policy to use (default org.ow2.proactive.scheduler.policy.DefaultPolicy)");
        policy.setArgName("policy");
        policy.setRequired(false);
        options.addOption(policy);

        Option noDeploy = new Option("ln", "localNodes", true,
          "the number of local nodes to start (can be 0), default value is " + DEFAULT_NODES_NUMBER);
        noDeploy.setArgName("localNodes");
        noDeploy.setRequired(false);
        options.addOption(noDeploy);

        Option nodeTimeout = new Option("t", "timeout", true,
          "Timeout used to start the nodes (only useful with local nodes, default: " +
            DEFAULT_NODES_TIMEOUT + "ms)");
        nodeTimeout.setArgName("timeout");
        nodeTimeout.setRequired(false);
        options.addOption(nodeTimeout);

        boolean displayHelp = false;

        try {
            //get the path of the file

            String rm = null;
            String policyFullName = PASchedulerProperties.SCHEDULER_DEFAULT_POLICY.getValueAsString();

            Parser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                displayHelp = true;
            else {
                if (cmd.hasOption("p")) {
                    policyFullName = cmd.getOptionValue("p");
                    logger.info("Used policy : " + policyFullName);
                }

                if (cmd.hasOption("u")) {
                    rm = cmd.getOptionValue("u");
                    logger.info("RM URL : " + rm);
                }

                logger.info("Starting the scheduler...");

                if (rm != null) {
                    try {
                        logger.info("Connecting to the resource manager on " + rm);
                        int rmConnectionTimeout = PASchedulerProperties.RESOURCE_MANAGER_CONNECTION_TIMEOUT
                                .getValueAsInt();
                        SchedulerFactory.waitAndJoinRM(new URI(rm), rmConnectionTimeout);
                    } catch (Exception e) {
                        logger.error("ERROR while connecting to the RM on " + rm + ", no RM found !");
                        System.exit(2);
                    }
                } else {
                    rm = getLocalAdress();
                    URI uri = new URI(rm);
                    //trying to connect to a started local RM
                    try {
                        SchedulerFactory.tryJoinRM(uri);
                        logger.info("Connected to the existing resource manager at " + uri);
                    } catch (Exception e) {
                        int numberLocalNodes = readIntOption(cmd, "localNodes", DEFAULT_NODES_NUMBER);
                        int nodeTimeoutValue = readIntOption(cmd, "timeout", DEFAULT_NODES_TIMEOUT);
                        startResourceManager(numberLocalNodes, nodeTimeoutValue);
                    }
                }

                try {
                    SchedulerAuthenticationInterface sai = SchedulerFactory.startLocal(new URI(rm),
                            policyFullName);

                    JettyStarter.runWars(rm, sai.getHostURL());
                    logger.info("The scheduler created on " + sai.getHostURL());

                } catch (AdminSchedulerException e) {
                    logger.warn("", e);
                }
            }
        } catch (MissingArgumentException e) {
            logger.error("", e);
            displayHelp = true;
        } catch (MissingOptionException e) {
            logger.error("", e);
            displayHelp = true;
        } catch (UnrecognizedOptionException e) {
            logger.error("", e);
            logger.error("", e);
            displayHelp = true;
        } catch (AlreadySelectedException e) {
            logger.error("", e);
            displayHelp = true;
        } catch (ParseException e) {
            logger.error("", e);
            displayHelp = true;
        } catch (Exception e) {
            logger.error("", e);
            System.exit(6);
        }

        if (displayHelp) {
            logger.info("");
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(120);
            hf.printHelp("scheduler-start" + Tools.shellExtension(), options, true);
            System.exit(10);
        }

    }

    private static int readIntOption(CommandLine cmd, String optionName,
      int defaultValue) throws ParseException {
        int value = defaultValue;
        if (cmd.hasOption(optionName)) {
            try {
                value = Integer.parseInt(cmd.getOptionValue(optionName));
            } catch (Exception nfe) {
                throw new ParseException(
                  "Wrong value for " + optionName + " option: " + cmd.getOptionValue("t"));
            }
        }
        return value;
    }

    private static void startResourceManager(final int numberLocalNodes, final int nodeTimeoutValue) {
        final Thread rmStarter = new Thread() {
            public void run() {
                try {
                    //Starting a local RM using default deployment descriptor
                    RMFactory.setOsJavaProperty();
                    logger.info("Starting the resource manager...");
                    RMAuthentication rmAuth = RMFactory.startLocal();

                    if (numberLocalNodes > 0) {
                        addLocalNodes(rmAuth, numberLocalNodes, nodeTimeoutValue);
                    }

                    logger.info("The resource manager with " + numberLocalNodes +
                      " local nodes created on " + rmAuth.getHostURL());
                } catch (AlreadyBoundException abe) {
                    logger.error("The resource manager already exists on local host", abe);
                    System.exit(4);
                } catch (Exception aoce) {
                    logger.error("Unable to create local resource manager", aoce);
                    System.exit(5);
                }
            }
        };

        rmStarter.start();
    }

    private static void addLocalNodes(RMAuthentication rmAuth, int numberLocalNodes,
      int nodeTimeoutValue) throws LoginException, KeyException, IOException {
        //creating default node source
        ResourceManager rman = rmAuth.login(Credentials
          .getCredentials(PAResourceManagerProperties
            .getAbsolutePath(PAResourceManagerProperties.RM_CREDS
              .getValueAsString())));
        //first im parameter is default rm url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(
          PAResourceManagerProperties
            .getAbsolutePath(PAResourceManagerProperties.RM_CREDS
              .getValueAsString())
        ));
        rman.createNodeSource(NodeSource.LOCAL_INFRASTRUCTURE_NAME,
          LocalInfrastructure.class.getName(), new Object[] { creds,
            numberLocalNodes, nodeTimeoutValue,
            CentralPAPropertyRepository.PA_HOME.getCmdLine() + CentralPAPropertyRepository.PA_HOME.getValue()
          },
          RestartDownNodesPolicy.class.getName(), new Object[] { "ALL", "ALL",
            "10000" }
        );
    }

    private static String getLocalAdress() throws ProActiveException {
        RemoteObjectFactory rof = AbstractRemoteObjectFactory.getDefaultRemoteObjectFactory();
        return rof.getBaseURI().toString();
    }

    private static void configureSchedulerAndRMAndPAHomes() {
        if (System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey()) == null) {
            System.setProperty(PASchedulerProperties.SCHEDULER_HOME.getKey(), System.getProperty("user.dir"));
        }
        if (System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) == null) {
            System.setProperty(PAResourceManagerProperties.RM_HOME.getKey(),
              System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey()));
        }
        if (System.getProperty(CentralPAPropertyRepository.PA_HOME.getName()) == null) {
            System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(),
              System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey()));
        }

        if (System.getProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName()) == null) {
            System.setProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(),
              System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey()) +
                "/config/proactive/ProActiveConfiguration.xml");
        }
    }

    private static void configureSecurityManager() {
        if (System.getProperty("java.security.policy") == null) {
            System.setProperty("java.security.policy", System.getProperty(
              PASchedulerProperties.SCHEDULER_HOME.getKey()) + "/config/security.java.policy-server");
            Policy.getPolicy().refresh();
        }
    }

    private static void configureLogging() {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            String defaultLog4jConfig = System.getProperty(
              PASchedulerProperties.SCHEDULER_HOME.getKey()) + "/config/log4j/scheduler-log4j-server";
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(),
              defaultLog4jConfig);
            PropertyConfigurator.configure(defaultLog4jConfig);
        }
    }
}
