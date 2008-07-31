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
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.utils.FileToBytesConverter;
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;


/**
 * LocalSchedulerExample can start a new scheduler.
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 */
public class LocalSchedulerExample {
    //shows how to run the scheduler
    /** Default Database configuration file. */
    public static final String defaultConfigFile = PASchedulerProperties.SCHEDULER_DEFAULT_DBCONFIG_FILE
            .getValueAsString();
    public static final String defaultPolicy = PASchedulerProperties.SCHEDULER_DEFAULT_POLICY
            .getValueAsString();
    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CORE);
    private static RMAdmin admin;

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

        Option auth = new Option("a", "auth", true, "path of authentication files directory (default '.')");
        auth.setArgName("auth");
        auth.setRequired(false);
        options.addOption(auth);

        Option rmURL = new Option("u", "rmURL", true, "the resource manager URL (default //localhost)");
        rmURL.setArgName("rmURL");
        rmURL.setRequired(false);
        options.addOption(rmURL);

        Option policy = new Option(
            "p",
            "policy",
            true,
            "the complete name of the scheduling policy to use (default org.ow2.proactive.scheduler.policy.PriorityPolicy)");
        rmURL.setArgName("policy");
        rmURL.setRequired(false);
        options.addOption(policy);

        Option configFileOption = new Option("c", "configFile", true,
            "the Scheduler database configuration file.");
        configFileOption.setArgName("configFile");
        configFileOption.setRequired(!new File(defaultConfigFile).exists());
        options.addOption(configFileOption);

        boolean displayHelp = false;

        try {
            //get the path of the file
            ResourceManagerProxy imp = null;

            String rm = null;
            String authPath = null;
            String configFile = defaultConfigFile;
            String policyFullName = defaultPolicy;

            Parser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                displayHelp = true;
            else {
                if (cmd.hasOption("c"))
                    configFile = cmd.getOptionValue("c");

                if (cmd.hasOption("p"))
                    policyFullName = cmd.getOptionValue("p");

                if (cmd.hasOption("u"))
                    rm = cmd.getOptionValue("u");

                if (cmd.hasOption("a"))
                    authPath = cmd.getOptionValue("a");
                else
                    authPath = ".";

                if (rm != null) {
                    try {
                        imp = ResourceManagerProxy.getProxy(new URI(rm));

                        logger.info("[SCHEDULER] Connect to Resource Manager on " + rm);
                    } catch (Exception e) {
                        throw new Exception("Resource Manager doesn't exist on " + rm);
                    }
                } else {
                    URI uri = new URI("rmi://localhost:" + PAProperties.PA_RMI_PORT.getValue() + "/");
                    //trying to connect to a started local RM
                    logger.info("[SCHEDULER] Trying to connect to a started local Resource Manager...");
                    try {
                        imp = ResourceManagerProxy.getProxy(uri);

                        logger.info("[SCHEDULER] Connected to the local Resource Manager");
                    } catch (Exception e) {
                        logger.info("[SCHEDULER] Resource Manager doesn't exist on localhost");

                        //Starting a local RM
                        RMFactory.startLocal();
                        admin = RMFactory.getAdmin();
                        logger.info("[SCHEDULER] Start local Resource Manager with 4 local nodes.");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }

                        //select the appropriate deployment descriptor regarding to the OS
                        if (System.getProperty("os.name").contains("Windows")) {
                            File GCMDeployFile = new File(PAResourceManagerProperties.RM_HOME
                                    .getValueAsString() +
                                File.separator + "config/deployment/Local4JVMDeploymentWindows.xml");
                            admin.addNodes(FileToBytesConverter.convertFileToByteArray(GCMDeployFile));
                        } else {
                            File GCMDeployFile = new File(PAResourceManagerProperties.RM_HOME
                                    .getValueAsString() +
                                File.separator + "config/deployment/Local4JVMDeploymentUnix.xml");
                            admin.addNodes(FileToBytesConverter.convertFileToByteArray(GCMDeployFile));
                        }

                        //admin.addNodes(appl);

                        //                Runtime.getRuntime().addShutdownHook(new Thread() {
                        //                        public void run() {
                        //                            try {
                        //                                admin.killAll();
                        //                            } catch (ProActiveException e) {
                        //                                e.printStackTrace();
                        //                            }
                        //                        }
                        //                    });
                        imp = ResourceManagerProxy.getProxy(uri);

                        logger.info("Resource Manager created on " +
                            PAActiveObject.getActiveObjectNodeUrl(imp));
                    }

                }

                AdminScheduler.createScheduler(configFile, authPath, imp, policyFullName);
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
            e.printStackTrace();
            System.exit(1);
        }

        if (displayHelp) {
            System.out.println();
            new HelpFormatter().printHelp("scheduler", options, true);
            System.exit(2);
        }

    }
}
