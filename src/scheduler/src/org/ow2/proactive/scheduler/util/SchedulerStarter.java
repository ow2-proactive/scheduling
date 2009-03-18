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
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.common.util.Tools;
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.exception.AdminSchedulerException;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;


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
        File.separator + "config/deployment/Local4JVMDeployment.xml";

    /**
     * Start the scheduler creation process.
     *
     * @param args
     */
    public static void main(String[] args) {

        Options options = new Options();

        Option help = new Option("h", "help", false, "to display this help");
        help.setArgName("help");
        help.setRequired(false);
        options.addOption(help);

        Option rmURL = new Option("u", "rmURL", true, "the resource manager URL (default //localhost)");
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
            //get the path of the file
            ResourceManagerProxy imp = null;

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

                if (rm != null) {
                    try {
                        logger_dev.info("Trying to connect to Resource Manager on " + rm);
                        imp = ResourceManagerProxy.getProxy(new URI(rm));
                    } catch (Exception e) {
                        logger.error("Error while connecting the RM on " + rm, e);
                        System.exit(2);
                    }
                } else {
                    URI uri = new URI("rmi://localhost/");
                    //trying to connect to a started local RM
                    logger_dev.info("Trying to connect to a started Resource Manager on " + uri);
                    try {
                        imp = ResourceManagerProxy.getProxy(uri);
                        logger
                                .info("Resource Manager URL was not specified, connection made to the local Resource Manager at " +
                                    uri);
                    } catch (Exception e) {
                        logger_dev.info("Resource Manager doesn't exist on " + uri);
                        try {
                            //Starting a local RM using default deployment descriptor
                            RMFactory.setOsJavaProperty();
                            logger_dev.info("Trying to start a local Resource Manager");
                            RMFactory.startLocal(java.util.Collections.singletonList(deploymentFile));

                            logger_dev.info("Trying to join the local Resource Manager");
                            //wait for the RM to be created
                            RMConnection.waitAndJoin(null);

                            logger_dev
                                    .info("Trying to connect the local Resource Manager using Scheduler identity");
                            //get the proxy on the Resource Manager
                            imp = ResourceManagerProxy.getProxy(uri);

                            logger.warn("Resource Manager created on " +
                                Tools.getHostURL(PAActiveObject.getActiveObjectNodeUrl(imp)) +
                                " as it does not exist locally");
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
                    logger_dev.info("Creating scheduler...");
                    AdminScheduler.createScheduler(imp, policyFullName);
                } catch (AdminSchedulerException e) {
                    logger.warn(e);
                }

                logger.info("(Once started, press 'e' to shutdown)");
                while (System.in.read() != 'e')
                    ;
                //and terminate scheduler JVM
                System.exit(0);
            }
        } catch (MissingArgumentException e) {
            logger_dev.error(e.getLocalizedMessage());
            displayHelp = true;
        } catch (MissingOptionException e) {
            logger_dev.error(e.getLocalizedMessage());
            displayHelp = true;
        } catch (UnrecognizedOptionException e) {
            logger_dev.error(e.getLocalizedMessage());
            displayHelp = true;
        } catch (AlreadySelectedException e) {
            logger_dev.error(e.getLocalizedMessage());
            displayHelp = true;
        } catch (ParseException e) {
            logger_dev.error(e.getLocalizedMessage());
            displayHelp = true;
        } catch (Exception e) {
            logger.error(e);
            System.exit(6);
        }

        if (displayHelp) {
            logger.info("");
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(120);
            hf.printHelp("startScheduler" + Tools.shellExtension(), options, true);
            System.exit(10);
        }

    }
}
