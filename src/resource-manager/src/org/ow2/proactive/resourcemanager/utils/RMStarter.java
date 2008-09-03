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
package org.ow2.proactive.resourcemanager.utils;

import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;


/**
 * Class with main which instantiates a Resource Manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class RMStarter {

    public static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMLAUNCHER);

    /**
     * main function
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {

        Options options = new Options();

        Option help = new Option("h", "help", false, "to display this help");
        help.setArgName("help");
        help.setRequired(false);
        options.addOption(help);

        Option deploy = new Option("d", "deploy", true, "list of GCM deployment descriptors files");
        deploy.setArgName("deploy");
        deploy.setRequired(false);
        deploy.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(deploy);

        Option noDeploy = new Option("n", "nodeploy", false,
            "start Resource Manager without deploying default 4 local nodes");
        noDeploy.setArgName("nodeploy");
        noDeploy.setRequired(false);
        options.addOption(noDeploy);

        Parser parser = new GnuParser();
        CommandLine cmd;

        String[] gcmdList = null;
        boolean displayHelp = false;
        boolean launchRM = true;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                displayHelp = true;
                launchRM = false;
            } else if (cmd.hasOption("d") && cmd.hasOption("n")) {
                displayHelp = true;
                launchRM = false;
                System.out
                        .println("\nError, you cannot specify a deployment (-d|--deploy) and ask to deploy nothing (-n|--nodeply) !");
            } else {
                if (cmd.hasOption("d")) {
                    gcmdList = cmd.getOptionValues("d");
                    for (String gcmdPath : gcmdList) {
                        if (!(new File(gcmdPath)).exists()) {
                            System.out.println("Error, cannot find GCM deployment file " + gcmdPath);
                            launchRM = false;
                        }
                    }
                }
            }

            if (launchRM) {
                logger.info("STARTING RESOURCE MANAGER: Press 'e' to shutdown.");
                RMFactory.startLocal();
                Thread.sleep(2000);
                RMAdmin admin = RMFactory.getAdmin();

                if (cmd.hasOption("d")) {
                    for (String desc : gcmdList) {
                        File gcmDeployFile = new File(desc);
                        admin.addNodes(FileToBytesConverter.convertFileToByteArray(gcmDeployFile));
                    }
                } else if (!cmd.hasOption("n")) {
                    //select the appropriate deployment descriptor regarding to the OS
                    if (System.getProperty("os.name").contains("Windows")) {
                        File gcmDeployFile = new File(PAResourceManagerProperties.RM_HOME.getValueAsString() +
                            File.separator + "config/deployment/Local4JVMDeploymentWindows.xml");
                        admin.addNodes(FileToBytesConverter.convertFileToByteArray(gcmDeployFile));
                    } else {
                        File gcmDeployFile = new File(PAResourceManagerProperties.RM_HOME.getValueAsString() +
                            File.separator + "config/deployment/Local4JVMDeploymentUnix.xml");
                        admin.addNodes(FileToBytesConverter.convertFileToByteArray(gcmDeployFile));
                    }
                }

                //        Vector<String> v = new Vector<String>();
                //        v.add("//localhost:6444");
                //        admin.createDynamicNodeSource("P2P", 3, 10000, 50000, v);

                @SuppressWarnings("unused")
                char typed = 'x';
                while ((typed = (char) System.in.read()) != 'e') {
                }
                try {
                    RMFactory.getAdmin().shutdown(false);
                } catch (Exception e) {
                    e.printStackTrace();
                    PALifeCycle.exitFailure();
                }
            }
        } catch (ParseException e1) {
            displayHelp = true;
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RMException e) {
            e.printStackTrace();
        }

        if (displayHelp) {
            System.out.println("\nLaunch ProActive Resource Manager.");
            System.out.println("Without arguments, Resource Manager is launched with 4 "
                + "computing nodes on local machine.\n");
            new HelpFormatter().printHelp("scheduler", options, true);
            System.exit(2);
        }
    }
}
