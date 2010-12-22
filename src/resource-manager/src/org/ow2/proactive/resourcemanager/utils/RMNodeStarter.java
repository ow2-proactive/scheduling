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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.config.xml.ProActiveConfigurationParser;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.utils.console.JVMPropertiesPreloader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This class is responsible for creating a local node. You can define different settings to
 * register the node to an apropriate Resource Manager, ping it...
 *
 * @author ProActive team
 */
public class RMNodeStarter {

    protected Credentials credentials = null;
    protected String rmURL = null;
    protected String nodeName = RMNodeStarter.PAAGENT_DEFAULT_NODE_NAME;
    protected String nodeSourceName = null;

    /** Class' logger */
    protected static final Logger logger = ProActiveLogger.getLogger(RMLoggers.RMNODE);

    /** The default name of the node */
    protected static final String PAAGENT_DEFAULT_NODE_NAME = "PA-AGENT_NODE";

    /** Prefix for temp files that store nodes URL */
    protected static final String URL_TMPFILE_PREFIX = "PA-AGENT_URL";

    /** Name of the java property to set the rank */
    protected final static String RANK_PROP_NAME = "proactive.agent.rank";

    /**
     * The starter will try to connect to the Resource Manager before killing
     * itself that means that it will try to connect during
     * WAIT_ON_JOIN_TIMEOUT_IN_MS milliseconds
     */
    protected static int WAIT_ON_JOIN_TIMEOUT_IN_MS = 60000;
    /**
     * The ping delay used in RMPinger that pings the RM and exists if the
     * Resource Manager is down
     */
    protected static long PING_DELAY_IN_MS = 30000;

    /** The number of attempts to add the local node to the RM before quitting */
    protected static int NB_OF_ADD_NODE_ATTEMPTS = 10;

    /** The delay, in millis, between two attempts to add a node */
    protected static int ADD_NODE_ATTEMPTS_DELAY_IN_MS = 5000;

    // The url of the created node
    protected String nodeURL = "Not defined";
    // the rank of this node
    protected int rank;
    // if true, previous nodes with different URLs are removed from the RM
    protected boolean removePrevious;

    public static final char OPTION_CREDENTIAL_FILE = 'f';
    public static final char OPTION_CREDENTIAL_ENV = 'e';
    public static final char OPTION_CREDENTIAL_VAL = 'v';
    public static final char OPTION_RM_URL = 'r';
    public static final char OPTION_NODE_NAME = 'n';
    public static final char OPTION_SOURCE_NAME = 's';
    public static final char OPTION_WAIT_AND_JOIN_TIMEOUT = 'w';
    public static final char OPTION_PING_DELAY = 'p';
    public static final char OPTION_ADD_NODE_ATTEMPTS = 'a';
    public static final char OPTION_ADD_NODE_ATTEMPTS_DELAY = 'd';
    public static final char OPTION_HELP = 'h';

    public RMNodeStarter() {
    }

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
     * Fills the command line options.
     * @param options the options to fill
     */
    protected void fillOptions(final Options options) {
        // The path to the file that contains the credential
        final Option credentialFile = new Option(new Character(OPTION_CREDENTIAL_FILE).toString(),
            "credentialFile", true, "path to file that contains the credential");
        credentialFile.setRequired(false);
        credentialFile.setArgName("path");
        options.addOption(credentialFile);
        // The credential passed as environment variable
        final Option credentialEnv = new Option(new Character(OPTION_CREDENTIAL_ENV).toString(),
            "credentialEnv", true, "name of the environment variable that contains the credential");
        credentialEnv.setRequired(false);
        credentialEnv.setArgName("name");
        options.addOption(credentialEnv);
        // The credential passed as value
        final Option credVal = new Option(new Character(OPTION_CREDENTIAL_VAL).toString(), "credentialVal",
            true, "explicit value of the credential");
        credVal.setRequired(false);
        credVal.setArgName("credential");
        options.addOption(credVal);
        // The url of the resource manager
        final Option rmURL = new Option(new Character(OPTION_RM_URL).toString(), "rmURL", true,
            "URL of the resource manager. If no URL is provided, the node won't register.");
        rmURL.setRequired(false);
        rmURL.setArgName("url");
        options.addOption(rmURL);
        // The node name
        final Option nodeName = new Option(new Character(OPTION_NODE_NAME).toString(), "nodeName", true,
            "node name (default is " + PAAGENT_DEFAULT_NODE_NAME + ")");
        nodeName.setRequired(false);
        nodeName.setArgName("name");
        options.addOption(nodeName);
        // The node source name
        final Option sourceName = new Option(new Character(OPTION_SOURCE_NAME).toString(), "sourceName",
            true, "node source name");
        sourceName.setRequired(false);
        sourceName.setArgName("name");
        options.addOption(sourceName);
        // The wait on join timeout in millis
        final Option waitOnJoinTimeout = new Option(new Character(OPTION_WAIT_AND_JOIN_TIMEOUT).toString(),
            "waitOnJoinTimeout", true, "wait on join the resource manager timeout in millis (default is " +
                WAIT_ON_JOIN_TIMEOUT_IN_MS + ")");
        waitOnJoinTimeout.setRequired(false);
        waitOnJoinTimeout.setArgName("millis");
        options.addOption(waitOnJoinTimeout);
        // The ping delay in millis
        final Option pingDelay = new Option(
            new Character(OPTION_PING_DELAY).toString(),
            "pingDelay",
            true,
            "ping delay in millis used by RMPinger thread that calls System.exit(1) if the resource manager is down (default is " +
                PING_DELAY_IN_MS + "). A nul or negative frequence means no ping at all.");
        pingDelay.setRequired(false);
        pingDelay.setArgName("millis");
        options.addOption(pingDelay);
        // The number of attempts option
        final Option addNodeAttempts = new Option(new Character(OPTION_ADD_NODE_ATTEMPTS).toString(),
            "addNodeAttempts", true,
            "number of attempts to add the local node to the resource manager before quitting (default is " +
                NB_OF_ADD_NODE_ATTEMPTS + ")");
        addNodeAttempts.setRequired(false);
        addNodeAttempts.setArgName("number");
        options.addOption(addNodeAttempts);
        // The delay between attempts option
        final Option addNodeAttemptsDelay = new Option(new Character(OPTION_ADD_NODE_ATTEMPTS_DELAY)
                .toString(), "addNodeAttemptsDelay", true,
            "delay in millis between attempts to add the local node to the resource manager (default is " +
                ADD_NODE_ATTEMPTS_DELAY_IN_MS + ")");
        addNodeAttemptsDelay.setRequired(false);
        addNodeAttemptsDelay.setArgName("millis");
        options.addOption(addNodeAttemptsDelay);
        // Displays the help
        final Option help = new Option(new Character(OPTION_HELP).toString(), "help", false,
            "to display this help");
        help.setRequired(false);
        options.addOption(help);
    }

    /**
     * Creates a new instance of this class and calls registersInRm method.
     * @param args The arguments needed to join the Resource Manager
     */
    public static void main(String[] args) {
        //this call takes JVM properties into account
        args = JVMPropertiesPreloader.overrideJVMProperties(args);
        checkLog4jConfiguration();
        RMNodeStarter starter = new RMNodeStarter();
        starter.doMain(args);
    }

    protected void doMain(final String args[]) {
        this.parseCommandLine(args);
        this.readAndSetTheRank();
        Node node = this.createLocalNode(nodeName);
        this.nodeURL = node.getNodeInformation().getURL();
        System.out.println(this.nodeURL);

        if (rmURL != null) {
            ResourceManager rm = this.registerInRM(credentials, rmURL, nodeName, nodeSourceName);

            if (rm != null) {
                System.out.println("Connected to the Resource Manager at " + rmURL +
                    System.getProperty("line.separator"));
                // start pinging...
                // ping the im to see if we are still connected
                // if not connected just exit
                // isActive throws an exception is not connected
                // ping is optional, that allows other implementation
                // of RMNodeStarter to be cached for futur use by the
                // infrastructure manager
                if (PING_DELAY_IN_MS > 0) {
                    try {
                        while (rm.nodeIsAvailable(this.getNodeURL()).getBooleanValue()) {
                            try {
                                Thread.sleep(PING_DELAY_IN_MS);
                            } catch (InterruptedException e) {
                            }
                        }// while connected
                    } catch (Throwable e) {
                        // no more connected to the RM
                        System.out
                                .println("The connection to the Resource Manager has been lost. The application will exit.");
                        e.printStackTrace();
                        System.exit(ExitStatus.RM_NO_PING.exitCode);
                    }

                    // if we are here it means we lost the connection. just exit..
                    System.out.println("The Resource Manager has been shutdown. The application will exit. ");
                    System.err.println(ExitStatus.RM_IS_SHUTDOWN.description);
                    System.exit(ExitStatus.RM_IS_SHUTDOWN.exitCode);
                }
            } else {
                // Force system exit to bypass daemon threads
                System.err.println(ExitStatus.RMNODE_EXIT_FORCED.description);
                System.exit(ExitStatus.RMNODE_EXIT_FORCED.exitCode);
            }
        }
    }

    protected void fillParameters(final CommandLine cl, final Options options) {
        boolean printHelp = false;

        try {

            // Optional rmURL option
            if (cl.hasOption(OPTION_RM_URL)) {
                rmURL = cl.getOptionValue(OPTION_RM_URL);
            }

            // if the user doesn't provide a rm URL, we don't care about the credentials
            if (rmURL != null) {
                // The path to the file that contains the credential
                if (cl.hasOption(OPTION_CREDENTIAL_FILE)) {
                    try {
                        credentials = Credentials.getCredentials(cl.getOptionValue(OPTION_CREDENTIAL_FILE));
                    } catch (KeyException ke) {
                        System.err.println(ExitStatus.CRED_UNREADABLE.description);
                        System.exit(ExitStatus.CRED_UNREADABLE.exitCode);
                    }
                    // The name of the env variable that contains
                } else if (cl.hasOption(OPTION_CREDENTIAL_ENV)) {
                    final String variableName = cl.getOptionValue(OPTION_CREDENTIAL_ENV);
                    final String value = System.getenv(variableName);
                    if (value == null) {
                        System.err.println(ExitStatus.CRED_ENVIRONMENT.description);
                        System.exit(ExitStatus.CRED_ENVIRONMENT.exitCode);
                    }
                    try {
                        credentials = Credentials.getCredentialsBase64(value.getBytes());
                    } catch (KeyException ke) {
                        ke.printStackTrace();
                        System.exit(ExitStatus.CRED_DECODE.exitCode);
                    }
                    // Read the credentials directly from the command-line argument
                } else if (cl.hasOption(OPTION_CREDENTIAL_VAL)) {
                    final String str = cl.getOptionValue(OPTION_CREDENTIAL_VAL);
                    try {
                        credentials = Credentials.getCredentialsBase64(str.getBytes());
                    } catch (KeyException ke) {
                        ke.printStackTrace();
                        System.exit(ExitStatus.CRED_DECODE.exitCode);
                    }
                } else {
                    try {
                        credentials = Credentials.getCredentials();
                    } catch (KeyException ke) {
                        ke.printStackTrace();
                        System.exit(ExitStatus.CRED_UNREADABLE.exitCode);
                    }
                }
            }

            // Optional node name
            if (cl.hasOption(OPTION_NODE_NAME)) {
                nodeName = cl.getOptionValue(OPTION_NODE_NAME);
            }
            // Optional node source name
            if (cl.hasOption(OPTION_SOURCE_NAME)) {
                nodeSourceName = cl.getOptionValue(OPTION_SOURCE_NAME);
            }
            // Optional wait on join option
            if (cl.hasOption(OPTION_WAIT_AND_JOIN_TIMEOUT)) {
                RMNodeStarter.WAIT_ON_JOIN_TIMEOUT_IN_MS = Integer.valueOf(cl
                        .getOptionValue(OPTION_WAIT_AND_JOIN_TIMEOUT));
            }
            // Optional ping delay
            if (cl.hasOption(OPTION_PING_DELAY)) {
                RMNodeStarter.PING_DELAY_IN_MS = Integer.valueOf(cl.getOptionValue(OPTION_PING_DELAY));
            }
            // Optional number of add node attempts before quitting
            if (cl.hasOption(OPTION_ADD_NODE_ATTEMPTS)) {
                RMNodeStarter.NB_OF_ADD_NODE_ATTEMPTS = Integer.valueOf(cl
                        .getOptionValue(OPTION_ADD_NODE_ATTEMPTS));
            }
            // Optional delay between add node attempts
            if (cl.hasOption(OPTION_ADD_NODE_ATTEMPTS_DELAY)) {
                RMNodeStarter.ADD_NODE_ATTEMPTS_DELAY_IN_MS = Integer.valueOf(cl
                        .getOptionValue(OPTION_ADD_NODE_ATTEMPTS_DELAY));
            }
            // Optional help option
            if (cl.hasOption(OPTION_HELP)) {
                printHelp = true;
            }
        } catch (Throwable t) {
            printHelp = true;
            System.out.println(t.getMessage());
            t.printStackTrace();
            System.exit(ExitStatus.FAILED_TO_LAUNCH.exitCode);
        } finally {
            if (printHelp) {
                // Automatically generate the help statement
                HelpFormatter formatter = new HelpFormatter();
                // Prints usage
                formatter.printHelp("java " + RMNodeStarter.class.getName(), options);
                System.exit(ExitStatus.OK.exitCode);
            }
        }
    }

    protected void parseCommandLine(String[] args) {
        final Options options = new Options();

        //we fill int the options object, child classes can override this method
        //to add new options...
        fillOptions(options);

        final Parser parser = new GnuParser();

        CommandLine cl;
        try {
            cl = parser.parse(options, args);
            //now we update this object's fields given the options.
            fillParameters(cl, options);
        } catch (ParseException pe) {
            pe.printStackTrace();
            System.exit(ExitStatus.RMNODE_PARSE_ERROR.exitCode);
        }

    }

    /**
     * To define the default log4j configuration if log4j.configuration property has not been set.
     */
    public static void checkLog4jConfiguration() {
        try {
            String log4jPath = System.getProperty("log4j.configuration");
            if (log4jPath == null) {
                //either sched-home/dist/lib/PA-RM.jar or sched-home/classes/resource-manager/
                File origin = new File(RMNodeStarter.class.getProtectionDomain().getCodeSource()
                        .getLocation().getFile());
                File parent = origin.getParentFile();
                configuration: {
                    while (parent != null && parent.isDirectory()) {
                        File[] childs = parent.listFiles();
                        for (File child : childs) {
                            if ("config".equals(child.getName())) {
                                //we have found the sched-home/config/ directory!
                                log4jPath = child.getAbsolutePath() + File.separator + "log4j" +
                                    File.separator + "log4j-defaultNode";
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
     * Tries to join to the Resource Manager with a specified timeout
     * at the given URL, logs with provided credentials and adds the local node to
     * the Resource Manager. Handles all errors/exceptions.
     */
    protected ResourceManager registerInRM(final Credentials credentials, final String rmURL,
            final String nodeName, final String nodeSourceName) {

        // Create the full url to contact the Resource Manager
        final String fullUrl = rmURL.endsWith("/") ? rmURL + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION
                : rmURL + "/" + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION;
        // Try to join the Resource Manager with a specified timeout
        RMAuthentication auth = null;
        try {
            auth = RMConnection.waitAndJoin(fullUrl, WAIT_ON_JOIN_TIMEOUT_IN_MS);
            if (auth == null) {
                System.out.println(ExitStatus.RMAUTHENTICATION_NULL.description);
                System.err.println(ExitStatus.RMAUTHENTICATION_NULL.description);
                System.exit(ExitStatus.RMAUTHENTICATION_NULL.exitCode);
            }
        } catch (Throwable t) {
            System.out.println("Unable to join the Resource Manager at " + rmURL);
            t.printStackTrace();
            System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }

        ResourceManager rm = null;
        // 3 - Log using credential
        try {
            rm = auth.login(credentials);
            if (rm == null) {
                System.out.println(ExitStatus.RM_NULL.description);
                System.err.println(ExitStatus.RM_NULL.description);
                System.exit(ExitStatus.RM_NULL.exitCode);
            }
        } catch (Throwable t) {
            System.out.println("Unable to log into the Resource Manager at " + rmURL);
            t.printStackTrace();
            System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }

        // 4 - Add the created node to the Resource Manager with a specified
        // number of attempts and a timeout between each attempt
        boolean isNodeAdded = false;
        int attempts = 0;

        while ((!isNodeAdded) && (attempts < NB_OF_ADD_NODE_ATTEMPTS)) {
            attempts++;
            try {
                if (this.nodeSourceName != null) {
                    isNodeAdded = rm.addNode(this.nodeURL, this.nodeSourceName).getBooleanValue();
                } else {
                    isNodeAdded = rm.addNode(this.nodeURL).getBooleanValue();
                }
            } catch (AddingNodesException addException) {
                addException.printStackTrace();
                System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
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
            System.err.println(ExitStatus.RMNODE_ADD_ERROR.description);
            System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }// if not registered

        return rm;
    }

    protected void readAndSetTheRank() {
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
    }

    /**
     * Creates the node with the name given as parameter and returns it.
     * @param nodeName The expected name of the node
     * @return the newly created node.
     */
    protected Node createLocalNode(String nodeName) {
        Node localNode = null;
        try {
            localNode = NodeFactory.createLocalNode(nodeName, false, null, null);
            if (localNode == null) {
                System.out.println("The node returned by the NodeFactory is null");
                System.err.println(RMNodeStarter.ExitStatus.RMNODE_NULL.description);
                System.exit(RMNodeStarter.ExitStatus.RMNODE_NULL.exitCode);
            }
        } catch (Throwable t) {
            System.out.println("Unable to create the local node " + nodeName);
            t.printStackTrace();
            System.exit(RMNodeStarter.ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }
        return localNode;
    }

    /**
     * Store in a temp file the current URL of the node started by the agent
     * @param nodeName the name of the node
     * @param rank the rank of the node
     * @param nodeURL the URL of the node
     */
    protected void storeNodeURL(String nodeName, int rank, String nodeURL) {
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
    protected String getAndDeleteNodeURL(String nodeName, int rank) {
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
        final String tmpFile = new File(tmpDir, URL_TMPFILE_PREFIX + "_" + nodeName + "-" + rank)
                .getAbsolutePath();
        return tmpFile;
    }

    /**
     * This enum stands for the entire set of possible exit values of RMNodeStarter
     */
    public enum ExitStatus {
        OK(0, "Exit success."),
        //mustn't be changed, return value set in the JVM itself
        JVM_ERROR(1, "Problem with the Java process itself ( classpath, main method... )."), RM_NO_PING(100,
                "Cannot ping the Resource Manager because of a Throwable."), RM_IS_SHUTDOWN(101,
                "The Resource Manager has been shutdown."), CRED_UNREADABLE(200,
                "Cannot read the submited credential's key."), CRED_DECODE(201,
                "Cannot decode credential's key from base64."), CRED_ENVIRONMENT(202,
                "Environment variable not set for credential but it should be."), RMNODE_NULL(300,
                "NodeFactory returned null as RMNode."), RMAUTHENTICATION_NULL(301,
                "RMAuthentication instance is null."), RM_NULL(302, "Resource Manager instance is null."), RMNODE_ADD_ERROR(
                303, "Was not able to add RMNode the the Resource Manager."), RMNODE_PARSE_ERROR(304,
                "Problem encountered while parsing " + RMNodeStarter.class.getName() + " command line."), RMNODE_EXIT_FORCED(
                305,
                "Was not able to add RMNode to the Resource Manager. Force system to exit to bypass daemon threads."), FAILED_TO_LAUNCH(
                -1, RMNodeStarter.class.getSimpleName() + " process hasn't been started at all."), UNKNOWN(
                -2, "Cannot determine exit status.");
        public final int exitCode;
        public final String description;

        private ExitStatus(int exitCode, String description) {
            this.exitCode = exitCode;
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }

        public int getExitCode() {
            return this.exitCode;
        }
    }

    /**
     * CommandLineBuilder is an utility class that provide users with the capability to automatise
     * the RMNodeStarter command line building. We encourage Infrastructure Manager providers to
     * use this class as it is used as central point for applying changes to the RMNodeStarter
     * properties, for instance, if the classpath needs to bu updated, a call to
     * {@link #getRequiredJARs()} will reflect the change.
     *
     */
    public static final class CommandLineBuilder implements Cloneable {
        private String nodeName, sourceName, javaPath, rmURL, credentialsFile, credentialsValue,
                credentialsEnv, rmHome;
        private long pingDelay = 30000;
        private Properties paPropProperties;
        private String paPropString;
        private int addAttempts = -1, addAttemptsDelay = -1;
        private final String[] requiredJARs = { "script-js.jar", "jruby-engine.jar", "jython-engine.jar",
                "commons-logging-1.1.1.jar", "ProActive_SRM-common.jar", "ProActive_ResourceManager.jar",
                "ProActive_Scheduler-worker.jar", "commons-httpclient-3.1.jar", "commons-codec-1.3.jar",
                "ProActive.jar" };

        private OperatingSystem targetOS = OperatingSystem.UNIX;

        /**
         * Returns the jars required by RMNodeStarter in the right order.
         * @return Returns the jars required by RMNodeStarter in the right order.
         */
        public String[] getRequiredJARs() {
            return requiredJARs;
        }

        /**
         * To get the RMHome from a previous call to the method {@link #setRmHome(String)}. If such a call has not been made,
         * one manages to retrieve it from the PAProperties set thanks to a previous call to the method {@link #setPaProperties(byte[])} or {@link #setPaProperties(File)} of {@link #setPaProperties(Map)}.
         * @return the RMHome which will be used to build the command line.
         */
        public String getRmHome() {
            if (this.rmHome != null) {
                return rmHome;
            } else {
                if (paPropProperties != null) {
                    String rmHome = null;
                    if (paPropProperties.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) != null &&
                        !paPropProperties.getProperty(PAResourceManagerProperties.RM_HOME.getKey())
                                .equals("")) {
                        rmHome = paPropProperties.getProperty(PAResourceManagerProperties.RM_HOME.getKey());
                        if (!rmHome.endsWith(String.valueOf(this.targetOS.fs))) {
                            rmHome += String.valueOf(this.targetOS.fs);
                        }
                    } else {
                        if (PAResourceManagerProperties.RM_HOME.isSet()) {
                            rmHome = PAResourceManagerProperties.RM_HOME.getValueAsString();
                            if (!rmHome.endsWith(String.valueOf(this.targetOS.fs))) {
                                rmHome += String.valueOf(this.targetOS.fs);
                            }
                        } else {
                            logger
                                    .warn("No RM Home property found in the supplied configuration. You have to launch RMNodeStarter at the root of the RM Home by yourself.");
                            rmHome = "";
                        }
                    }
                    return rmHome;
                }
            }
            return rmHome;
        }

        /**
         * @param rmHome the resource manager's home
         */
        public void setRmHome(String rmHome) {
            this.rmHome = rmHome;
        }

        /**
         * @param nodeName the node's name
         */
        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        /**
         * @param sourceName the node source's name to which one the node will be added
         */
        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        /**
         * @param javaPath the path to the java executable used to launch the node
         */
        public void setJavaPath(String javaPath) {
            this.javaPath = javaPath;
        }

        /**
         * @param targetOS the operating system on which one the node will run
         */
        public void setTargetOS(OperatingSystem targetOS) {
            this.targetOS = targetOS;
        }

        /**
         * @param rmURL the url of the resource manager to which one the node must be added
         */
        public void setRmURL(String rmURL) {
            this.rmURL = rmURL;
        }

        /**
         * To set a String standing for the ProActive Properties, appended to the built command line without any modification.
         * If a call to {@link #setPaProperties(byte[])} or {@link #setPaProperties(File)} of {@link #setPaProperties(Map)} has already been made, the PAProperties structure will be cleaned up...
         * @param paProp A String standing for the PAProperties ( for instance -Dlog4j.configuration=... or -Dproactive.net.netmask=... )
         */
        public void setPaProperties(String paProp) {
            if (this.paPropProperties != null) {
                this.paPropProperties = null;
            }
            this.paPropString = paProp;
        }

        /**
         * To set the PAproperties of the node. If a previsous call to
         * {@link #setPaProperties(byte[])} or {@link #setPaProperties(File)} of {@link #setPaProperties(Map)} has already been made,
         * this one will overide the previous call. This file must be a valid ProActive XML configuration file.
         * @param paPropertiesFile the ProActive configuration file
         * @throws IOException if the file is not a ProActive regular file
         */
        public void setPaProperties(File paPropertiesFile) throws IOException {
            this.paPropProperties = new Properties();
            if (paPropertiesFile != null) {
                if (paPropertiesFile.exists() && paPropertiesFile.isFile()) {
                    this.paPropProperties = ProActiveConfigurationParser.parse(paPropertiesFile
                            .getAbsolutePath(), paPropProperties);
                } else {
                    throw new IOException("The supplied file is not a regular file: " +
                        paPropertiesFile.getAbsolutePath());
                }
            }
        }

        /**
         * To set the PAproperties of the node. If a previsous call to
         * {@link #setPaProperties(byte[])} or {@link #setPaProperties(File)} of {@link #setPaProperties(Map)} has already been made,
         * this one will overide the previous call.
         * Every properties will be appended to the command line this way: -Dkey=value
         * @param paProp a map containing valid java properties
         */
        public void setPaProperties(Map<String, String> paProp) {
            this.paPropProperties = new Properties();
            for (String key : paProp.keySet()) {
                this.paPropProperties.put(key, paProp.get(key));
            }
        }

        /**
         * To set the PAproperties of the node. If a previsous call to
         * {@link #setPaProperties(byte[])} or {@link #setPaProperties(File)} of {@link #setPaProperties(Map)} has already been made,
         * this one will overide the previous call.
         * @param ba the content of a valid ProActive xml configuration file.
         */
        public void setPaProperties(byte[] ba) {
            this.paPropProperties = new Properties();
            if (ba == null) {
                return;
            }
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(ba);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(is);
                NodeList props = doc.getElementsByTagName("prop");
                for (int i = 0; i < props.getLength(); i++) {
                    Element prop = (Element) props.item(i);
                    String key = prop.getAttribute("key");
                    String value = prop.getAttribute("value");
                    paPropProperties.put(key, value);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot read ProActive configuration from supplied file.");
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    logger.warn(e);
                }
            }
        }

        /**
         * To retrieve the credentials file from a previous {@link #setCredentialsFileAndNullOthers(String)}. If no such call has already been made, will try to retrieve the credentials file path
         * from the PAProperties set thanks to the methods: {@link #setPaProperties(byte[])} or {@link #setPaProperties(File)} of {@link #setPaProperties(Map)}
         * @return The credentials file used to build the command line
         */
        public String getCredentialsFile() {
            if (this.credentialsFile != null) {
                logger.trace("Credentials file retrieved from previously set value.");
                return credentialsFile;
            } else {
                if (this.credentialsEnv == null && this.credentialsValue == null) {
                    String paRMKey = PAResourceManagerProperties.RM_CREDS.getKey();
                    if (paPropProperties != null && paPropProperties.getProperty(paRMKey) != null &&
                        !paPropProperties.getProperty(paRMKey).equals("")) {
                        logger.trace(paRMKey + " property retrieved from PA properties supplied by " +
                            RMNodeStarter.CommandLineBuilder.class.getName());
                        return paPropProperties.getProperty(paRMKey);
                    } else {
                        if (PAResourceManagerProperties.RM_CREDS.isSet()) {
                            logger.trace(paRMKey +
                                " property retrieved from PA Properties of parent Resource Manager");
                            return PAResourceManagerProperties.RM_CREDS.getValueAsString();
                        }
                    }
                }
            }
            return credentialsFile;
        }

        /**
         * Sets the credentials file field to the supplied parameter and set
         * the other field related to credentials setup to null;
         * @param credentialsEnv
         */
        public void setCredentialsFileAndNullOthers(String credentialsFile) {
            this.credentialsFile = credentialsFile;
            this.credentialsEnv = null;
            this.credentialsValue = null;
        }

        /**
         * @return the value of the credentials used to connect as a string
         */
        public String getCredentialsValue() {
            return credentialsValue;
        }

        /**
         * Sets the credentials value field to the supplied parameter and set
         * the other field related to credentials setup to null;
         * @param credentialsEnv
         */
        public void setCredentialsValueAndNullOthers(String credentialsValue) {
            this.credentialsValue = credentialsValue;
            this.credentialsEnv = null;
            this.credentialsFile = null;
        }

        /**
         * @return the value of the credentials used to connect as an environment variable
         */
        public String getCredentialsEnv() {
            return credentialsEnv;
        }

        /**
         * Sets the credentials environment field to the supplied parameter and set
         * the other field related to credentials setup to null;
         * @param credentialsEnv
         */
        public void setCredentialsEnvAndNullOthers(String credentialsEnv) {
            this.credentialsEnv = credentialsEnv;
            this.credentialsFile = null;
            this.credentialsValue = null;
        }

        /**
         * @return the ping delay used to maintain connectivity with the RM. -1 means no ping
         */
        public long getPingDelay() {
            return pingDelay;
        }

        /**
         * @param pingDelay the ping delay used to detect rm shutdown. -1 means no ping
         */
        public void setPingDelay(long pingDelay) {
            this.pingDelay = pingDelay;
        }

        /**
         * @return the number of attempts made to add the node to the core
         */
        public int getAddAttempts() {
            return addAttempts;
        }

        /**
         * @param addAttempts the number of attempts made to add the node to the core
         */
        public void setAddAttempts(int addAttempts) {
            this.addAttempts = addAttempts;
        }

        /**
         * @return the delay between two add attempts
         */
        public int getAddAttemptsDelay() {
            return addAttemptsDelay;
        }

        /**
         *
         * @param addAttemptsDelay the delay between two add attempts
         */
        public void setAddAttemptsDelay(int addAttemptsDelay) {
            this.addAttemptsDelay = addAttemptsDelay;
        }

        /**
         * @return the name of the node
         */
        public String getNodeName() {
            return nodeName;
        }

        /**
         * @return the name of the node source to which one the node will be added
         */
        public String getSourceName() {
            return sourceName;
        }

        /**
         * @return the path of the java executable used to launch the node
         */
        public String getJavaPath() {
            return javaPath;
        }

        /**
         * @return the url of the resource manager to which one the node will be added
         */
        public String getRmURL() {
            return rmURL;
        }

        /**
         * @return the operating system on which one the node will run
         */
        public OperatingSystem getTargetOS() {
            return targetOS;
        }

        /**
         * To retrieve the PaProperties set thanks to a call to {@link #setPaProperties(byte[])} or {@link #setPaProperties(File)} of {@link #setPaProperties(Map)}
         * @return
         */
        public Properties getPaProperties() {
            return paPropProperties;
        }

        /**
         * To retrieved the PAProperties set thanks to the method {@link #setPaProperties(String)};
         * @return
         */
        public String getPaPropertiesString() {
            return paPropString;
        }

        /**
         * Build the command to launch the RMNode.
         * The required pieces of information that need to be set in order to allow the RMNode to start properly are:<br />
         * <ul><li>{@link RMNodeStarter.CommandLineBuilder#rmURL}</li><li>{@link RMNodeStarter.CommandLineBuilder#nodeName}</li>
         * <li>one of {@link RMNodeStarter.CommandLineBuilder#credentialsEnv}, {@link RMNodeStarter.CommandLineBuilder#credentialsFile} or {@link RMNodeStarter.CommandLineBuilder#credentialsValue}</li></ul>
         * @param displayCreds if true displays the credentials in the command line if false, obfuscates them
         * @return The RMNodeStarter command line.
         * @throws IOException if you supplied a ProActive Configuration file that doesn't exist.
         */
        public String buildCommandLine(boolean displayCreds) throws IOException {
            Properties paProp = this.getPaProperties();
            String rmHome = this.getRmHome();
            if (rmHome != null) {
                if (!rmHome.endsWith(this.targetOS.fs)) {
                    rmHome = rmHome + this.targetOS.fs;
                }
            } else {
                rmHome = "";
            }
            String libRoot = rmHome + "dist" + this.targetOS.fs + "lib" + this.targetOS.fs;
            StringBuilder sb = new StringBuilder();
            if (this.getJavaPath() != null) {
                sb.append(this.getJavaPath());
            } else {
                logger.warn("java path isn't set in RMNodeStarter configuration.");
                sb.append("java");
            }

            //building configuration
            if (paProp != null) {
                Set<Object> keys = paProp.keySet();
                for (Object key : keys) {
                    sb.append(" -D");
                    sb.append(key.toString());
                    sb.append("=");
                    sb.append(paProp.get(key).toString());
                }
            } else {
                if (this.getPaPropertiesString() != null) {
                    sb.append(" ");
                    sb.append(this.getPaPropertiesString());
                    sb.append(" ");
                }
            }
            //building classpath
            sb.append(" -cp ");
            if (this.getTargetOS().equals(OperatingSystem.CYGWIN) ||
                this.getTargetOS().equals(OperatingSystem.WINDOWS)) {
                sb.append("\"");//especially on cygwin, we need to quote the cp
            }
            sb.append(".");
            for (String jar : this.requiredJARs) {
                sb.append(this.targetOS.ps);
                sb.append(libRoot);
                sb.append(jar);
            }
            if (this.getTargetOS().equals(OperatingSystem.CYGWIN) ||
                this.getTargetOS().equals(OperatingSystem.WINDOWS)) {
                sb.append("\"");//especially on cygwin, we need to quote the cp
            }
            sb.append(" ");
            sb.append(RMNodeStarter.class.getName());

            //appending options
            if (this.getAddAttempts() != -1) {
                sb.append(" -");
                sb.append(OPTION_ADD_NODE_ATTEMPTS);
                sb.append(" ");
                sb.append(this.getAddAttempts());
            }
            if (this.getAddAttemptsDelay() != -1) {
                sb.append(" -");
                sb.append(OPTION_ADD_NODE_ATTEMPTS_DELAY);
                sb.append(" ");
                sb.append(this.getAddAttemptsDelay());
            }
            if (this.getCredentialsEnv() != null) {
                sb.append(" -");
                sb.append(OPTION_CREDENTIAL_ENV);
                sb.append(" ");
                sb.append(this.getCredentialsEnv());
            }
            if (this.getCredentialsFile() != null) {
                sb.append(" -");
                sb.append(OPTION_CREDENTIAL_FILE);
                sb.append(" ");
                sb.append(this.getCredentialsFile());
            }
            if (this.getCredentialsValue() != null) {
                sb.append(" -");
                sb.append(OPTION_CREDENTIAL_VAL);
                sb.append(" ");
                sb.append(displayCreds ? this.getCredentialsValue() : "[OBFUSCATED_CRED]");
            }
            if (this.getNodeName() != null) {
                sb.append(" -");
                sb.append(OPTION_NODE_NAME);
                sb.append(" ");
                sb.append(this.getNodeName());
            }
            if (this.getSourceName() != null) {
                sb.append(" -");
                sb.append(OPTION_SOURCE_NAME);
                sb.append(" ");
                sb.append(this.getSourceName());
            }
            if (this.getRmURL() != null) {
                //if the rm url != null we can specify a ping delay
                //it is not relevant if the rm url == null
                sb.append(" -");
                sb.append(OPTION_PING_DELAY);
                sb.append(" ");
                sb.append(this.getPingDelay());
                sb.append(" -");
                sb.append(OPTION_RM_URL);
                sb.append(" ");
                sb.append(this.getRmURL());
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            try {
                return buildCommandLine(false);
            } catch (IOException e) {
                return RMNodeStarter.CommandLineBuilder.class.getName() + " with invalid configuration";
            }
        }
    }

    /*####################################
     * Helpers
     *###################################*/
    /**
     * Private inner enum which represents supported operating systems
     */
    public enum OperatingSystem {
        WINDOWS(";", "\\"), UNIX(":", "/"), CYGWIN(";", "/");
        /** the path separator, ie. ";" on windows systems and ":" on unix systems */
        public final String ps;
        /** the file path separator, ie. "/" on unix systems and "\" on windows systems */
        public final String fs;

        private OperatingSystem(String ps, String fs) {
            this.fs = fs;
            this.ps = ps;
        }

        /**
         * Returns the operating system corresponding to the provided String parameter: 'LINUX', 'WINDOWS' or 'CYGWIN'
         * @param desc one of 'LINUX', 'WINDOWS' or 'CYGWIN'
         * @return the appropriate Operating System of null if no system is found
         */
        public static OperatingSystem getOperatingSystem(String desc) {
            if (desc == null) {
                throw new IllegalArgumentException("String description of operating system cannot be null");
            }
            desc = desc.toUpperCase();
            if ("LINUX".equals(desc) || "UNIX".equals(desc)) {
                return OperatingSystem.UNIX;
            }
            if ("WINDOWS".equals(desc)) {
                return OperatingSystem.WINDOWS;
            }
            if ("CYGWIN".equals(desc)) {
                return OperatingSystem.CYGWIN;
            }
            return null;
        }
    }
}