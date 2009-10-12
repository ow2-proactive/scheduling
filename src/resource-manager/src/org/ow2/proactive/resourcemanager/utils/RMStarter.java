/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 * Class with main which instantiates a Resource Manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class RMStarter {

    /**
     * Log4j logger name.
     */
    private static Logger logger = ProActiveLogger.getLogger(RMLoggers.CONSOLE);

    private static Options options = new Options();

    private static void initOptions() {
        Option help = new Option("h", "help", false, "to display this help");
        help.setArgName("help");
        help.setRequired(false);
        options.addOption(help);

        Option deploy = new Option("d", "deploy", true, "list of GCM deployment descriptors files");
        deploy.setArgName("deploy");
        deploy.setRequired(false);
        deploy.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(deploy);

        Option noDeploy = new Option("ln", "localNodes", false,
            "start Resource Manager deploying default 4 local nodes");
        noDeploy.setArgName("localNodes");
        noDeploy.setRequired(false);
        options.addOption(noDeploy);
    }

    private static void displayHelp() {
        logger.info("");
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(120);
        hf.printHelp("rm-start", options, true);
        logger.info("\n Notice : Without argument, Resource Manager starts without any computing node.");
        System.exit(1);
    }

    /**
     * main function
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {

        initOptions();

        Parser parser = new GnuParser();
        CommandLine cmd;

        String[] gcmdList = null;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                displayHelp();
            } else if (cmd.hasOption("d") && cmd.hasOption("localNodes")) {
                logger
                        .warn("\nYou cannot specify a deployment (-d|--deploy) and ask to deploy nothing (-n|--nodeply) !\n");
                displayHelp();
            } else if (cmd.hasOption("d")) {
                // checking that all specified files are exist 
                gcmdList = cmd.getOptionValues("d");
                for (String gcmdPath : gcmdList) {
                    if (!(new File(gcmdPath)).exists()) {
                        logger.error("Cannot find GCM deployment file " + gcmdPath);
                        System.exit(2);
                    }
                }
            }

            logger.info("Starting Resource Manager, Please wait...");
            RMFactory.setOsJavaProperty();

            Collection<String> deploymentDescriptors = new LinkedList<String>();
            if (cmd.hasOption("localNodes")) {
                String gcmDeployFile = PAResourceManagerProperties.RM_HOME.getValueAsString() +
                    File.separator + "config/rm/deployment/Local4JVMDeployment.xml";
                deploymentDescriptors.add(gcmDeployFile);
            } else if (cmd.hasOption("d")) {
                for (String desc : gcmdList) {
                    deploymentDescriptors.add(desc);
                }
            }

            // starting clean resource manager
            RMFactory.startLocal();

            RMAuthentication auth = RMConnection.waitAndJoin(null);
            RMAdmin admin = auth.logAsAdmin(Credentials.getCredentials(PAResourceManagerProperties
                    .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
            String nodeSourceName = NodeSource.GCM_LOCAL;
            int counter = 2;

            for (String deploymentDescriptor : deploymentDescriptors) {
                byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray(new File(
                    deploymentDescriptor));

                admin.createNodesource(nodeSourceName, GCMInfrastructure.class.getName(),
                        new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), null);

                nodeSourceName = NodeSource.GCM_LOCAL + counter;
                counter++;
            }

            /*logger.info("(Once started, press 'e' to shutdown)");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (!br.readLine().equals("e"))
                ;

            logger.info("Shutting down the resource manager");
            System.exit(0);*/

        } catch (ParseException e1) {
            displayHelp();
        } catch (Exception e) {
            logger.error("", e);
            System.exit(3);
        }

    }

}
