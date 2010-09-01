/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;


/**
 * This class is responsible for creating a local node and add it
 * to the Resource Manager.
 * 
 * @author ProActive team
 */
public final class PAAgentServiceRMStarter {

    /** The default url of the Resource Manager */
    private static final String DEFAULT_RM_URL = "rmi://localhost:1099/";

    /** The default name of the node */
    private static final String PAAGENT_DEFAULT_NODE_NAME = "PA-AGENT_NODE";

    /** Prefix for temp files that store nodes URL */
    private static final String URL_TMPFILE_PREFIX = "PA-AGENT_URL";

    /** Name of the java property to set the rank */
    private final static String RANK_PROP_NAME = "proactive.agent.rank";

    /** class' logger */
    private static final Logger logger = Logger.getLogger(RMLoggers.RMNODE);

    /**
     * The starter will try to connect to the Resource Manager before killing
     * itself that means that it will try to connect during
     * WAIT_ON_JOIN_TIMEOUT_IN_MS milliseconds
     */
    private static int WAIT_ON_JOIN_TIMEOUT_IN_MS = 60000;
    /**
     * The ping delay used in RMPinger that pings the RM and exists if the
     * Resource Manager is down
     */
    private static long PING_DELAY_IN_MS = 30000;

    /** The number of attempts to add the local node to the RM before quitting */
    private static int NB_OF_ADD_NODE_ATTEMPTS = 10;

    /** The delay, in millis, between two attempts to add a node */
    private static int ADD_NODE_ATTEMPTS_DELAY_IN_MS = 5000;

    // The url of the created node
    private String nodeURL = "Not defined";
    // the rank of this node
    private int rank;
    // if true, previous nodes with different URLs are removed from the RM
    private boolean removePrevious;

    /**
     * Returns the rank of this node
     * @return the rank of this node
     */
    public int getRank() {
        return rank;
    }

    /**
     * Returns the URL of the node handled by this starter.
     * @return the URL of the node handled by this starter.
     */
    public String getNodeURL() {
        return this.nodeURL;
    }

    /**
     * To define the default log4j configuration if log4j.configuration property has not been set.
     */
    public static void checkLog4jConfiguration() {
        try {
            String log4jPath = System.getProperty("log4j.configuration");
            if (log4jPath == null) {
                //either sched-home/dist/lib/PA-RM.jar or sched-home/classes/resource-manager/
                File origin = new File(PAAgentServiceRMStarter.class.getProtectionDomain().getCodeSource()
                        .getLocation().getFile());
                File parent = origin.getParentFile();
                configuration: {
                    while (parent != null && parent.isDirectory()) {
                        File[] childs = parent.listFiles();
                        for (File child : childs) {
                            if ("config".equals(child.getName())) {
                                //we have found the sched-home/config/ directory!
                                log4jPath = child.getAbsolutePath() + File.separator + "log4j" +
                                    File.separator + "rm-log4j-server";
                                File log4j = new File(log4jPath);
                                if (log4j.exists()) {
                                    URL log4jURL;
                                    try {
                                        log4jURL = log4j.toURL();
                                        PropertyConfigurator.configure(log4jURL);
                                        System.setProperty("log4j.configuration", log4jPath);
                                        logger.trace("log4j.configuration not set, " + log4jPath +
                                            " defiined as default log4j configuration.");
                                    } catch (MalformedURLException e) {
                                        logger.trace("Cannot configure log4j", e);
                                    }
                                } else {
                                    logger.trace("Log4J configuration not found. Cannot configure log4j");
                                }
                                break configuration;
                            }
                        }
                        parent = parent.getParentFile();
                    }
                }
            } else {
                logger.trace("Does not override log4j.configuration");
            }
        } catch (Exception ex) {
            logger.trace("Cannot set log4j Configuration", ex);
        }
    }

    /**
     * Fills the command line options.
     * @param options the options to fill 
     */
    public static void fillOptions(final Options options) {
        // The path to the file that contains the credential
        final Option credentialFile = new Option("f", "credentialFile", true,
            "path to file that contains the credential");
        credentialFile.setRequired(false);
        credentialFile.setArgName("path");
        options.addOption(credentialFile);
        // The credential passed as environment variable
        final Option credentialEnv = new Option("e", "credentialEnv", true,
            "name of the environment variable that contains the credential");
        credentialEnv.setRequired(false);
        credentialEnv.setArgName("name");
        options.addOption(credentialEnv);
        // The credential passed as value
        final Option credVal = new Option("v", "credentialVal", true, "explicit value of the credential");
        credVal.setRequired(false);
        credVal.setArgName("credential");
        options.addOption(credVal);
        // The url of the resource manager
        final Option rmURL = new Option("r", "rmURL", true, "URL of the resource manager");
        rmURL.setRequired(false);
        rmURL.setArgName("url");
        options.addOption(rmURL);
        // The node name
        final Option nodeName = new Option("n", "nodeName", true, "node name (default is " +
            PAAGENT_DEFAULT_NODE_NAME + ")");
        nodeName.setRequired(false);
        nodeName.setArgName("name");
        options.addOption(nodeName);
        // The node source name 
        final Option sourceName = new Option("s", "sourceName", true, "node source name");
        sourceName.setRequired(false);
        sourceName.setArgName("name");
        options.addOption(sourceName);
        // The wait on join timeout in millis
        final Option waitOnJoinTimeout = new Option("w", "waitOnJoinTimeout", true,
            "wait on join the resource manager timeout in millis (default is " + WAIT_ON_JOIN_TIMEOUT_IN_MS +
                ")");
        waitOnJoinTimeout.setRequired(false);
        waitOnJoinTimeout.setArgName("millis");
        options.addOption(waitOnJoinTimeout);
        // The ping delay in millis
        final Option pingDelay = new Option(
            "p",
            "pingDelay",
            true,
            "ping delay in millis used by RMPinger thread that calls System.exit(1) if the resource manager is down (default is " +
                PING_DELAY_IN_MS + ")");
        pingDelay.setRequired(false);
        pingDelay.setArgName("millis");
        options.addOption(pingDelay);
        // The number of attempts option
        final Option addNodeAttempts = new Option("a", "addNodeAttempts", true,
            "number of attempts to add the local node to the resource manager before quitting (default is " +
                NB_OF_ADD_NODE_ATTEMPTS + ")");
        addNodeAttempts.setRequired(false);
        addNodeAttempts.setArgName("number");
        options.addOption(addNodeAttempts);
        // The delay between attempts option
        final Option addNodeAttemptsDelay = new Option("d", "addNodeAttemptsDelay", true,
            "delay in millis between attempts to add the local node to the resource manager (default is " +
                ADD_NODE_ATTEMPTS_DELAY_IN_MS + ")");
        addNodeAttemptsDelay.setRequired(false);
        addNodeAttemptsDelay.setArgName("millis");
        options.addOption(addNodeAttemptsDelay);
        // Displays the help
        final Option help = new Option("h", "help", false, "to display this help");
        help.setRequired(false);
        options.addOption(help);
    }

    /**
     * Creates a new instance of this class and calls registersInRm method.
     * 
     * @param args
     *            The arguments needed to join the Resource Manager
     */
    public static void main(final String args[]) {
        checkLog4jConfiguration();
        Credentials credentials = null;
        String rmURL = PAAgentServiceRMStarter.DEFAULT_RM_URL;
        String nodeName = PAAgentServiceRMStarter.PAAGENT_DEFAULT_NODE_NAME;
        String nodeSourceName = null;
        boolean printHelp = false;

        final Parser parser = new GnuParser();
        final Options options = new Options();
        PAAgentServiceRMStarter.fillOptions(options);
        try {
            // Parse the command line           
            final CommandLine cl = parser.parse(options, args);

            // The path to the file that contains the credential
            if (cl.hasOption('f')) {
                credentials = Credentials.getCredentials(cl.getOptionValue('f'));
                // The name of the env variable that contains     
            } else if (cl.hasOption('e')) {
                final String variableName = cl.getOptionValue('e');
                final String value = System.getenv(variableName);
                if (value == null) {
                    throw new IllegalArgumentException("Unable to read the value of the " + variableName);
                }
                credentials = Credentials.getCredentialsBase64(value.getBytes());
                // Read the credentials directly from the command-line argument	
            } else if (cl.hasOption('v')) {
                final String str = cl.getOptionValue('v');
                credentials = Credentials.getCredentialsBase64(str.getBytes());
            } else {
                credentials = Credentials.getCredentials();
            }
            // Mandatory rmURL option                                
            if (cl.hasOption('r')) {
                rmURL = cl.getOptionValue('r');
            }
            // Optional node name
            if (cl.hasOption('n')) {
                nodeName = cl.getOptionValue('n');
            }
            // Optional node source name
            if (cl.hasOption('s')) {
                nodeSourceName = cl.getOptionValue('s');
            }
            // Optional wait on join option
            if (cl.hasOption('w')) {
                PAAgentServiceRMStarter.WAIT_ON_JOIN_TIMEOUT_IN_MS = Integer.valueOf(cl.getOptionValue('w'));
            }
            // Optional ping delay
            if (cl.hasOption('p')) {
                PAAgentServiceRMStarter.PING_DELAY_IN_MS = Integer.valueOf(cl.getOptionValue('p'));
            }
            // Optional number of add node attempts before quitting
            if (cl.hasOption('a')) {
                PAAgentServiceRMStarter.NB_OF_ADD_NODE_ATTEMPTS = Integer.valueOf(cl.getOptionValue('a'));
            }
            // Optional delay between add node attempts
            if (cl.hasOption('d')) {
                PAAgentServiceRMStarter.ADD_NODE_ATTEMPTS_DELAY_IN_MS = Integer.valueOf(cl
                        .getOptionValue('d'));
            }
            // Optional help option
            if (cl.hasOption('h')) {
                printHelp = true;
            }
        } catch (Throwable t) {
            printHelp = true;
            System.out.println(t.getMessage());
            return;
        } finally {
            if (printHelp) {
                // Automatically generate the help statement
                HelpFormatter formatter = new HelpFormatter();
                // Prints usage
                formatter.printHelp("java " + PAAgentServiceRMStarter.class.getName(), options);
            }
        }

        final PAAgentServiceRMStarter starter = new PAAgentServiceRMStarter();
        ResourceManager rm = starter.registerInRM(credentials, rmURL, nodeName, nodeSourceName);
        if (rm != null) {
            System.out.println("Connected to the Resource Manager at " + rmURL + "\n");
            // start pinging...
            // ping the rm to see if we are still connected
            // if not connected just exit
            // isActive throws an exception is not connected
            try {
                while (rm.nodeIsAvailable(starter.getNodeURL()).booleanValue()) {
                    try {
                        Thread.sleep(PING_DELAY_IN_MS);
                    } catch (InterruptedException e) {
                    }
                }// while connected
            } catch (Throwable e) {
                // no more connected to the RM
                System.out
                        .println("The connection to the Resource Manager has been lost. The application will exit.");
                System.exit(1);
            }

            // if we are here it means we lost the connection. just exit..
            System.out.println("The Resource Manager has been shutdown. The application will exit. ");
            System.exit(2);
        } else {
            // Force system exit to bypass daemon threads
            System.exit(3);
        }
    }

    /**
     * Creates a local node, tries to join to the Resource Manager with a specified timeout
     * at the given URL, logs with provided credentials and adds the local node to
     * the Resource Manager. Handles all errors/exceptions.
     */
    private ResourceManager registerInRM(final Credentials credentials, final String rmURL,
            final String nodeName, final String nodeSourceName) {

        // 0 - read and set the rank
        String rankAsString = System.getProperty(RANK_PROP_NAME);
        if (rankAsString == null) {
            System.out.println("[WARNING] Rank is not set. Previous URLs will not be stored");
            this.removePrevious = false;
        } else {
            try {
                this.rank = Integer.parseInt(rankAsString);
                this.removePrevious = true;
                System.out.println("Rank is " + this.rank);
            } catch (Throwable e) {
                System.out.println("[WARNING] Rank cannot be read due to " + e.getMessage() +
                    ". Previous URLs will not be stored");
                this.removePrevious = false;
            }
        }

        // 1 - Create the local node that will be registered in RM
        Node localNode = null;
        try {
            localNode = NodeFactory.createLocalNode(nodeName, false, null, null, null);
            if (localNode == null) {
                throw new RuntimeException("The node returned by the NodeFactory is null");
            }
            this.nodeURL = localNode.getNodeInformation().getURL();
        } catch (Throwable t) {
            System.out.println("Unable to create the local node " + nodeName);
            t.printStackTrace();
            return null;
        }

        // Create the full url to contact the Resource Manager
        final String fullUrl = rmURL.endsWith("/") ? rmURL + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION
                : rmURL + "/" + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION;
        // 2 - Try to join the Resource Manager with a specified timeout
        RMAuthentication auth = null;
        try {
            auth = RMConnection.waitAndJoin(fullUrl, WAIT_ON_JOIN_TIMEOUT_IN_MS);
            if (auth == null) {
                throw new RuntimeException("The RMAuthentication instance is null");
            }
        } catch (Throwable t) {
            System.out.println("Unable to join the Resource Manager at " + rmURL);
            t.printStackTrace();
            return null;
        }

        ResourceManager rm = null;
        // 3 - Log using credential        
        try {
            rm = auth.login(credentials);
            if (rm == null) {
                throw new RuntimeException("The ResourceManager instance is null");
            }
        } catch (Throwable t) {
            System.out.println("Unable to log into the Resource Manager at " + rmURL);
            t.printStackTrace();
            return null;
        }

        // 4 - Add the created node to the Resource Manager with a specified 
        // number of attempts and a timeout between each attempt            
        boolean isNodeAdded = false;
        int attempts = 0;

        while ((!isNodeAdded) && (attempts < NB_OF_ADD_NODE_ATTEMPTS)) {
            attempts++;
            try {
                if (nodeSourceName == null) {
                    isNodeAdded = rm.addNode(this.nodeURL).booleanValue();
                } else {
                    isNodeAdded = rm.addNode(this.nodeURL, nodeSourceName).booleanValue();
                }
            } catch (AddingNodesException ex) {
                System.out.println("Unable to add the local node to the Resource Manager at " + rmURL);
                ex.printStackTrace();
                isNodeAdded = false;
            } catch (SecurityException ex) {
                System.out.println("Unable to add the local node to the Resource Manager at " + rmURL);
                ex.printStackTrace();
                isNodeAdded = false;
            }

            if (isNodeAdded) {
                if (removePrevious) {
                    // try to remove previous URL if different...
                    String previousURL = this.getAndDeleteNodeURL(nodeName, rank);
                    if (previousURL != null && !previousURL.equals(this.nodeURL)) {
                        System.out
                                .println("Different previous URL registered by this agent has been found. Remove previous registration.");
                        rm.removeNode(previousURL, true);
                    }
                    // store the node URL
                    this.storeNodeURL(nodeName, rank, this.nodeURL);
                    System.out.println("Node " + this.nodeURL + " added. URL is stored in " +
                        getNodeURLFilename(nodeName, rank));
                } else {
                    System.out.println("Node " + this.nodeURL + " added.");
                }
            } else { // not yet registered
                System.out.println("Attempt number " + attempts + " out of " + NB_OF_ADD_NODE_ATTEMPTS +
                    " to add the local node to the Resource Manager at " + rmURL + " has failed.");
                try {
                    Thread.sleep(ADD_NODE_ATTEMPTS_DELAY_IN_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }// while

        if (!isNodeAdded) {
            System.out.println("The Resource Manager was unable to add the local node " + this.nodeURL +
                " after " + NB_OF_ADD_NODE_ATTEMPTS + " attempts. The application will exit.");
            return null;
        }// if not registered

        return rm;
    }

    /**
     * Store in a temp file the current URL of the node started by the agent
     * @param nodeName the name of the node
     * @param rank the rank of the node
     * @param nodeURL the URL of the node
     */
    private void storeNodeURL(String nodeName, int rank, String nodeURL) {
        try {
            File f = new File(getNodeURLFilename(nodeName, rank));
            if (f.exists()) {
                System.out.println("[WARNING] NodeURL file already exists ; delete it.");
                f.delete();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            out.write(nodeURL);
            out.write(System.getProperty("line.separator"));
            out.close();
        } catch (IOException e) {
            System.out.println("[WARNING] NodeURL cannot be created.");
            e.printStackTrace();
        }
    }

    /**
     * Return the previous URL of this node
     * @param nodeName the name of the node started by the Agent
     * @param rank the rank of the node
     * @return the previous URL of this node, null if none can be found
     */
    private String getAndDeleteNodeURL(String nodeName, int rank) {
        try {
            File f = new File(getNodeURLFilename(nodeName, rank));
            if (f.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(f));
                String read = in.readLine();
                in.close();
                f.delete();
                return read;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create the name of the temp file for storing node URL.
     */
    private String getNodeURLFilename(String nodeName, int rank) {
        final String tmpDir = System.getProperty("java.io.tmpdir");
        final String tmpFile = tmpDir + "_" + URL_TMPFILE_PREFIX + "_" + nodeName + "-" + rank;
        return tmpFile;
    }

}