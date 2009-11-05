/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
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
import java.net.URI;
import java.rmi.AlreadyBoundException;

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
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scheduler.SchedulerFactory;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.exception.AdminSchedulerException;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive.utils.console.JVMPropertiesPreloader;


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
    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE);
    private static Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.SCHEDULER);

    public static final String defaultPolicy = PASchedulerProperties.SCHEDULER_DEFAULT_POLICY
            .getValueAsString();
    public static final String deploymentFile = PAResourceManagerProperties.RM_HOME.getValueAsString() +
        File.separator + "config/rm/deployment/Local4JVMDeployment.xml";

    /**
     * Start the scheduler creation process.
     *
     * @param args
     */
    public static void main(String[] args) {

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
            "the complete name of the scheduling policy to use (default org.ow2.proactive.scheduler.policy.PriorityPolicy)");
        policy.setArgName("policy");
        policy.setRequired(false);
        options.addOption(policy);

        boolean displayHelp = false;

        try {
            RMAuthentication rmAuth = null;
            //get the path of the file

            String rm = null;
            String policyFullName = defaultPolicy;

            Parser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                displayHelp = true;
            else {
                if (cmd.hasOption("p")) {
                    policyFullName = cmd.getOptionValue("p");
                    logger_dev.info("Used policy : " + policyFullName);
                }

                if (cmd.hasOption("u")) {
                    rm = cmd.getOptionValue("u");
                    logger_dev.info("RM URL : " + rm);
                }

                logger.info("Starting Scheduler, Please wait...");
                boolean onlySched = false;

                if (rm != null) {
                    try {
                        logger_dev.info("Trying to connect to Resource Manager on " + rm);
                        ResourceManagerProxy.getProxy(new URI(rm));
                        onlySched = true;
                    } catch (Exception e) {
                        logger.error("ERROR while connecting the RM on " + rm + ", no RM found !");
                        System.exit(2);
                    }
                } else {
                    rm = getLocalAdress();
                    URI uri = new URI(rm);
                    //trying to connect to a started local RM
                    logger_dev.info("Trying to connect to a started Resource Manager on " + uri);
                    try {
                        ResourceManagerProxy.getProxy(uri);
                        logger
                                .info("Resource Manager URL was not specified, connection made to the local Resource Manager at " +
                                    uri);
                    } catch (Exception e) {
                        logger.info("Resource Manager doesn't exist on the local host");
                        try {
                            //Starting a local RM using default deployment descriptor
                            RMFactory.setOsJavaProperty();
                            logger.info("Trying to start a local Resource Manager");
                            rmAuth = RMFactory.startLocal();

                            logger_dev
                                    .info("Trying to connect the local Resource Manager using Scheduler identity");

                            //creating default node source
                            RMAdmin rmAdmin = rmAuth.logAsAdmin(Credentials
                                    .getCredentials(PAResourceManagerProperties
                                            .getAbsolutePath(PAResourceManagerProperties.RM_CREDS
                                                    .getValueAsString())));
                            byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray(new File(
                                deploymentFile));
                            rmAdmin.createNodesource(NodeSource.GCM_LOCAL, GCMInfrastructure.class.getName(),
                                    new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), null);

                            logger.info("Resource Manager created on " + rmAuth.getHostURL());
                        } catch (AlreadyBoundException abe) {
                            logger.error("Resource Manager already exists on local host", abe);
                            System.exit(4);
                        } catch (ActiveObjectCreationException aoce) {
                            logger.error("Unable to create local Resource Manager", aoce);
                            System.exit(5);
                        }
                    }
                }

                try {
                    if (!onlySched) {
                        logger.info("Starting scheduler...");
                    }
                    SchedulerAuthenticationInterface sai = SchedulerFactory.startLocal(rm, policyFullName);
                    logger.info("Scheduler successfully created on " + sai.getHostURL());
                } catch (AdminSchedulerException e) {
                    logger.warn("", e);
                }
            }
        } catch (MissingArgumentException e) {
            logger_dev.error("", e);
            displayHelp = true;
        } catch (MissingOptionException e) {
            logger_dev.error("", e);
            displayHelp = true;
        } catch (UnrecognizedOptionException e) {
            logger_dev.error("", e);
            logger_dev.error("", e);
            displayHelp = true;
        } catch (AlreadySelectedException e) {
            logger_dev.error("", e);
            displayHelp = true;
        } catch (ParseException e) {
            logger_dev.error("", e);
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

    private static String getLocalAdress() {
        try {
            return AbstractRemoteObjectFactory.getDefaultRemoteObjectFactory().getBaseURI().toString();
        } catch (UnknownProtocolException e) {
            return "rmi://localhost/";
        }
    }
}
