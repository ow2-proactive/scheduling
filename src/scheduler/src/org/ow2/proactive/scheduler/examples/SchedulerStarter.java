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
package org.ow2.proactive.scheduler.examples;

import java.io.File;
import java.net.URI;
import java.rmi.AlreadyBoundException;

import javax.security.auth.login.LoginException;

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
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.exception.AdminSchedulerException;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;


/**
 * SchedulerStarter can start a new scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SchedulerStarter {
    //shows how to run the scheduler

    public static final String defaultPolicy = PASchedulerProperties.SCHEDULER_DEFAULT_POLICY
            .getValueAsString();
    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CORE);

    public static void cleanNode() {
        try {
            AdminScheduler.destroyLocalScheduler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                if (cmd.hasOption("p"))
                    policyFullName = cmd.getOptionValue("p");

                if (cmd.hasOption("u"))
                    rm = cmd.getOptionValue("u");

                logger.info("STARTING SCHEDULER : Press 'e' to shutdown.");

                if (rm != null) {
                    try {
                        imp = ResourceManagerProxy.getProxy(new URI(rm));
                        logger.info("Connect to Resource Manager on " + rm);
                    } catch (LoginException e) {
                        logger.info("Unable to authenticate user to Resource Manager (" + rm + ")", e);
                        System.exit(1);
                    } catch (Exception e) {
                        logger.info(e);
                        System.exit(2);
                    }
                } else {
                    URI uri = new URI("rmi://localhost/");
                    //trying to connect to a started local RM
                    logger.info("Trying to connect to a started local Resource Manager...");
                    try {
                        imp = ResourceManagerProxy.getProxy(uri);
                        logger.info("Connected to the local Resource Manager");
                    } catch (LoginException e) {
                        logger.info("Unable to authenticate user to Resource Manager (" + rm + ")", e);
                        System.exit(1);
                    } catch (Exception e) {
                        logger.info("Resource Manager doesn't exist on localhost");
                        try {
                            //Starting a local RM using default deployment descriptor
                            RMFactory.setOsJavaProperty();
                            String deploymentDescriptor = PAResourceManagerProperties.RM_HOME
                                    .getValueAsString() +
                                File.separator + "config/deployment/Local4JVMDeployment.xml";
                            RMFactory.startLocal(java.util.Collections.singletonList(deploymentDescriptor));

                            //wait for the RM to be created
                            RMConnection.waitAndJoin(null);

                            //get the proxy on the Resource Manager
                            imp = ResourceManagerProxy.getProxy(uri);

                            logger.info("Resource Manager created on " +
                                PAActiveObject.getActiveObjectNodeUrl(imp));
                        } catch (LoginException le) {
                            logger.info("Unable to authenticate user to local Resource Manager", le);
                            System.exit(1);
                        } catch (AlreadyBoundException abe) {
                            logger.info("Resource Manager already exists on local host", abe);
                            System.exit(4);
                        } catch (ActiveObjectCreationException aoce) {
                            logger.info("Unable to create local Resource Manager", aoce);
                            System.exit(5);
                        }
                    }
                }

                try {
                    AdminScheduler.createScheduler(imp, policyFullName);
                } catch (AdminSchedulerException e) {
                    logger.info(e);
                }

                @SuppressWarnings("unused")
                char typed;
                while (System.in.read() != 'e')
                    ;
                //shutdown scheduler if 'e' is pressed
                cleanNode();
                //and terminate scheduler JVM
                System.exit(0);
            }
        } catch (MissingArgumentException e) {
            System.out.println(e.getLocalizedMessage());
            displayHelp = true;
        } catch (MissingOptionException e) {
            System.out.println("Missing option: " + e.getLocalizedMessage());
            displayHelp = true;
        } catch (UnrecognizedOptionException e) {
            System.out.println(e.getLocalizedMessage());
            displayHelp = true;
        } catch (AlreadySelectedException e) {
            System.out.println(e.getClass().getSimpleName() + " : " + e.getLocalizedMessage());
            displayHelp = true;
        } catch (ParseException e) {
            displayHelp = true;
        } catch (Exception e) {
            logger.info(e);
            System.exit(6);
        }

        if (displayHelp) {
            System.out.println();
            new HelpFormatter().printHelp("scheduler", options, true);
            System.exit(2);
        }

    }
}
