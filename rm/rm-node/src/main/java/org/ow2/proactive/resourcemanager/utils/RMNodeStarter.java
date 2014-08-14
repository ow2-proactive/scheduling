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
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyException;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.xml.ProActiveConfigurationParser;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.utils.JVMPropertiesPreloader;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.PermissionChecker;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.exception.NotConnectedException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.node.jmx.SigarExposer;
import org.ow2.proactive.resourcemanager.nodesource.dataspace.DataSpaceNodeConfigurationAgent;
import org.ow2.proactive.rm.util.process.EnvironmentCookieBasedChildProcessKiller;
import org.ow2.proactive.utils.Formatter;
import org.ow2.proactive.utils.Tools;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.ow2.proactive.utils.ClasspathUtils.findSchedulerHome;


/**
 * This class is responsible for creating a local node. You can define different settings to
 * register the node to an appropriate Resource Manager, ping it...
 *
 * @author ProActive team
 */
public class RMNodeStarter {

    protected Credentials credentials = null;
    protected String rmURL = null;
    protected String nodeName;
    protected String nodeSourceName = null;
    protected Node node;

    // While logger is not configured and it not set with sys properties, use Console logger
    static {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            Logger.getRootLogger().getLoggerRepository().resetConfiguration();
            BasicConfigurator.configure(new ConsoleAppender(new PatternLayout(
              "%m%n")));
            Logger.getRootLogger().setLevel(Level.INFO);
        }
    }
    /** Class' logger */
    protected static final Logger logger = Logger.getLogger(RMNodeStarter.class);

    /** Prefix for temp files that store nodes URL */
    protected static final String URL_TMPFILE_PREFIX = "PA-AGENT_URL";

    /** Name of the java property to set the rank */
    protected final static String RANK_PROP_NAME = "proactive.agent.rank";

    /** Name of the java property to set the data spaces configuration status */
    public final static String DATASPACES_STATUS_PROP_NAME = "proactive.dataspaces.status";

    /** If this property is added to node properties then this
     *  node will be provides for 
     * computations only if criteria have the same access token.
     *
     */
    public static final String NODE_ACCESS_TOKEN = "proactive.node.access.token";

    /**
     * The starter will try to connect to the Resource Manager before killing
     * itself that means that it will try to connect during
     * WAIT_ON_JOIN_TIMEOUT_IN_MS milliseconds
     */
    protected static int WAIT_ON_JOIN_TIMEOUT_IN_MS = 60000;
    /** to inform that the user supplied a value from the command line for the join rm timeout */
    protected static boolean WAIT_ON_JOIN_TIMEOUT_IN_MS_USER_SUPPLIED = false;
    /** Name of the java property to set the timeout value used to join the resource manager */
    protected final static String WAIT_ON_JOIN_PROP_NAME = "proactive.node.joinrm.timeout";

    /**
     * The ping delay used in RMPinger that pings the RM and exists if the
     * Resource Manager is down
     */
    protected static long PING_DELAY_IN_MS = 30000;
    /** to inform that the user supplied a value from the command line for the ping */
    protected static boolean PING_DELAY_IN_MS_USER_SUPPLIED = false;
    /** Name of the java property to set the node -> rm ping frequency value */
    protected final static String PING_DELAY_PROP_NAME = "proactive.node.ping.delay";

    /** The number of attempts to add the local node to the RM before quitting */
    protected static int NB_OF_ADD_NODE_ATTEMPTS = 10;
    /** to inform that the user supplied a value from the command line for the number of "add" attempts */
    protected static boolean NB_OF_ADD_NODE_ATTEMPTS_USER_SUPPLIED = false;
    /** Name of the java property to set the number of attempts performed to add a node to the resource manager */
    protected final static String NB_OF_ADD_NODE_ATTEMPTS_PROP_NAME = "proactive.node.add.attempts";

    /** The number of attempts to reconnect the node to the RM before quitting */
    protected static int NB_OF_RECONNECTION_ATTEMPTS = 2 * 60 * 24; // to make it 24 hours by default

    /** Name of the java property to set the number of attempts performed to add a node to the resource manager */
    protected final static String NB_OF_RECONNECTION_ATTEMPTS_PROP_NAME = "proactive.node.reconnection.attempts";

    /** The delay, in millis, between two attempts to add a node */
    protected static int ADD_NODE_ATTEMPTS_DELAY_IN_MS = 5000;
    /** to inform that the user supplied a value from the command line for the delay between two add attempts*/
    protected static boolean ADD_NODE_ATTEMPTS_DELAY_IN_MS_USER_SUPPLIED = false;
    /** Name of the java property to set the delay between two attempts performed to add a node to the resource manager */
    protected final static String ADD_NODE_ATTEMPTS_DELAY_PROP_NAME = "proactive.node.add.delay";
    /** Name of the java property to set the node source name */
    protected final static String NODESOURCE_PROP_NAME = "proactive.node.nodesource";

    /** Name of the node property that stores the Sigar JMX connection URL*/
    public static final String JMX_URL = "proactive.node.jmx.sigar.";

    // The url of the created node
    protected String nodeURL = "Not defined";
    // the rank of this node
    protected int rank;
    // if true, previous nodes with different URLs are removed from the RM
    protected boolean removePrevious;

    protected int numberOfReconnectionAttemptsLeft = NB_OF_RECONNECTION_ATTEMPTS;

    private static final long DATASPACE_CLOSE_TIMEOUT = 3 * 1000; // seconds

    // Sigar JMX beans
    protected SigarExposer sigarExposer;

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
        final Option credentialFile = new Option(Character.toString(OPTION_CREDENTIAL_FILE),
          "credentialFile", true, "path to file that contains the credential");
        credentialFile.setRequired(false);
        credentialFile.setArgName("path");
        options.addOption(credentialFile);
        // The credential passed as environment variable
        final Option credentialEnv = new Option(Character.toString(OPTION_CREDENTIAL_ENV), "credentialEnv",
          true, "name of the environment variable that contains the credential");
        credentialEnv.setRequired(false);
        credentialEnv.setArgName("name");
        options.addOption(credentialEnv);
        // The credential passed as value
        final Option credVal = new Option(Character.toString(OPTION_CREDENTIAL_VAL), "credentialVal", true,
          "explicit value of the credential");
        credVal.setRequired(false);
        credVal.setArgName("credential");
        options.addOption(credVal);
        // The url of the resource manager
        final Option rmURL = new Option(Character.toString(OPTION_RM_URL), "rmURL", true,
          "URL of the resource manager. If no URL is provided, the node won't register.");
        rmURL.setRequired(false);
        rmURL.setArgName("url");
        options.addOption(rmURL);
        // The node name
        final Option nodeName = new Option(Character.toString(OPTION_NODE_NAME), "nodeName", true,
          "node name (default is hostname_pid)");
        nodeName.setRequired(false);
        nodeName.setArgName("name");
        options.addOption(nodeName);
        // The node source name
        final Option sourceName = new Option(Character.toString(OPTION_SOURCE_NAME), "sourceName", true,
          "node source name");
        sourceName.setRequired(false);
        sourceName.setArgName("name");
        options.addOption(sourceName);
        // The wait on join timeout in millis
        final Option waitOnJoinTimeout = new Option(Character.toString(OPTION_WAIT_AND_JOIN_TIMEOUT),
          "waitOnJoinTimeout", true, "wait on join the resource manager timeout in millis (default is " +
          WAIT_ON_JOIN_TIMEOUT_IN_MS + ")");
        waitOnJoinTimeout.setRequired(false);
        waitOnJoinTimeout.setArgName("millis");
        options.addOption(waitOnJoinTimeout);
        // The ping delay in millis
        final Option pingDelay = new Option(
          Character.toString(OPTION_PING_DELAY),
          "pingDelay",
          true,
          "ping delay in millis used by RMPinger thread that calls System.exit(1) if the resource manager is down (default is " +
            PING_DELAY_IN_MS + "). A nul or negative frequence means no ping at all.");
        pingDelay.setRequired(false);
        pingDelay.setArgName("millis");
        options.addOption(pingDelay);
        // The number of attempts option
        final Option addNodeAttempts = new Option(Character.toString(OPTION_ADD_NODE_ATTEMPTS),
          "addNodeAttempts", true,
          "number of attempts to add the local node to the resource manager. Default is " +
            NB_OF_ADD_NODE_ATTEMPTS + "). When 0 is specified node remains alive without " +
            "trying to add itself to the RM. Otherwise the process is terminated when number " +
            "of attempts exceeded.");
        addNodeAttempts.setRequired(false);
        addNodeAttempts.setArgName("number");
        options.addOption(addNodeAttempts);
        // The delay between attempts option
        final Option addNodeAttemptsDelay = new Option(Character.toString(OPTION_ADD_NODE_ATTEMPTS_DELAY),
          "addNodeAttemptsDelay", true,
          "delay in millis between attempts to add the local node to the resource manager (default is " +
            ADD_NODE_ATTEMPTS_DELAY_IN_MS + ")");
        addNodeAttemptsDelay.setRequired(false);
        addNodeAttemptsDelay.setArgName("millis");
        options.addOption(addNodeAttemptsDelay);
        // Displays the help
        final Option help = new Option(Character.toString(OPTION_HELP), "help", false, "to display this help");
        help.setRequired(false);
        options.addOption(help);
    }

    /**
     * Creates a new instance of this class and calls registersInRm method.
     * @param args The arguments needed to join the Resource Manager
     */
    public static void main(String[] args) {
        try {
            //this call takes JVM properties into account
            args = JVMPropertiesPreloader.overrideJVMProperties(args);
            EnvironmentCookieBasedChildProcessKiller.registerKillChildProcessesOnShutdown();
            RMNodeStarter starter = new RMNodeStarter();
            starter.doMain(args);
        } catch (Throwable t) {
            System.out
              .println("A major problem occured when trying to start a node and register it into the Resource Manager, see the stacktrace below");
            // Fix for SCHEDULING-1588            	
            if (t instanceof java.lang.NoClassDefFoundError) {
                System.out
                  .println("Unable to load a class definition, maybe the classpath is not accessible");
            }
            t.printStackTrace();
            // Do not load extra class definitions
            System.exit(/*ExitStatus.UNKNOWN.exitCode*/-2);
        }
    }

    protected void doMain(final String args[]) {
        configureSecurityManager();
        configureRMAndProActiveHomes();
        configureProActiveDefaultConfigurationFile();
        loadSigarIfRunningWithOneJar();

        this.parseCommandLine(args);

        configureLogging(nodeName);

        selectNetworkInterface();

        this.readAndSetTheRank();
        this.node = this.createLocalNode(nodeName);

        Tools.logAvailableScriptEngines(logger);

        this.nodeURL = node.getNodeInformation().getURL();
        logger.info("URL of this node " + this.nodeURL);

        configureForDataSpace(node);
        if (nodeSourceName != null && nodeSourceName.length() > 0) {
            // setting system the property with node source name 
            System.setProperty(NODESOURCE_PROP_NAME, nodeSourceName);
        }

        if (rmURL != null) {
            ResourceManager rm = this.registerInRM(credentials, rmURL, nodeName);

            if (rm != null) {
                logger.info("Connected to the resource manager at " + rmURL);

                // NB_OF_ADD_NODE_ATTEMPTS is used here to disable pinging
                if (PING_DELAY_IN_MS > 0 && NB_OF_ADD_NODE_ATTEMPTS > 0) {

                    while (numberOfReconnectionAttemptsLeft >= 0) {
                        try {
                            pingNodeIndefinitely(rm);
                        } catch (NotConnectedException e) {
                            rm = reconnectToResourceManager();
                        } catch (Throwable e) {
                            logger.error(ExitStatus.RM_NO_PING.description, e);
                        } finally {
                            try {
                                logger.warn("Disconnected from the resource manager");
                                logger.warn("Node will try to reconnect in " + PING_DELAY_IN_MS + " ms");
                                logger.warn("Number of attempts left " + numberOfReconnectionAttemptsLeft);

                                numberOfReconnectionAttemptsLeft--;
                                Thread.sleep(PING_DELAY_IN_MS);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }

                    // if we are here it means we lost the connection. just exit..
                    logger.error(ExitStatus.RM_IS_SHUTDOWN.description);
                    System.exit(ExitStatus.RM_IS_SHUTDOWN.exitCode);
                }
            } else {
                // Force system exit to bypass daemon threads
                logger.error(ExitStatus.RMNODE_EXIT_FORCED.description);
                System.exit(ExitStatus.RMNODE_EXIT_FORCED.exitCode);
            }
        }
    }

    private void selectNetworkInterface() {
        if (rmURL != null) {
            try {
                logger.debug("Detecting a network interface to bind the node");
                String networkInterface = RMConnection.getNetworkInterfaceFor(rmURL);
                logger.info("Node will be bounded to the following network interface " + networkInterface);
                CentralPAPropertyRepository.PA_NET_INTERFACE.setValue(networkInterface);
            } catch (Exception e) {
                logger.debug("Unable to detect the network interface", e);
            }
        }
    }

    private ResourceManager reconnectToResourceManager() {
        try {
            // trying to reconnect to the resource manager
            RMAuthentication auth = RMConnection.waitAndJoin(rmURL);
            return auth.login(credentials);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    private void pingNodeIndefinitely(ResourceManager rm) {
        while (rm != null && rm.setNodeAvailable(this.getNodeURL()).getBooleanValue()) {
            try {
                if (numberOfReconnectionAttemptsLeft < NB_OF_RECONNECTION_ATTEMPTS) {
                    logger.info("Node successfully reconnected to the resource manager");
                    numberOfReconnectionAttemptsLeft = NB_OF_RECONNECTION_ATTEMPTS;
                }
                Thread.sleep(PING_DELAY_IN_MS);
            } catch (InterruptedException e) {
                logger.warn("Node ping activity is interrupted", e);
            }
        }
    }

    private void configureRMAndProActiveHomes() {
        if (System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) == null) {
            System.setProperty(PAResourceManagerProperties.RM_HOME.getKey(), findSchedulerHome());
        }
        if (System.getProperty(CentralPAPropertyRepository.PA_HOME.getName()) == null) {
            System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(), System
              .getProperty(PAResourceManagerProperties.RM_HOME.getKey()));
        }
    }

    private void configureProActiveDefaultConfigurationFile() {
        if (System.getProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName()) == null) {
            File defaultProActiveConfiguration = new File(
              System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()),
              "config/network/node.ini");
            if (defaultProActiveConfiguration.exists()) {
                System.setProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(),
                  defaultProActiveConfiguration.getAbsolutePath());
            }
        }
    }

    private void loadSigarIfRunningWithOneJar() {
        if (OneJar.isRunningWithOneJar()) {
            String nativeLibraryName = SigarLoader.getNativeLibraryName();
            String nativeLibraryNameToLoad = nativeLibraryName.replace(SigarLoader.getLibraryExtension(), "")
              .replace(SigarLoader.getLibraryPrefix(), "");
            System.loadLibrary(nativeLibraryNameToLoad);
        }
    }

    private void configureSecurityManager() {
        if (System.getProperty("java.security.policy") == null) {
            System.setProperty("java.security.policy", RMNodeStarter.class.getResource(
              "/config/security.java.policy-client").toString());
            Policy.getPolicy().refresh();
        }
    }

    /*
     * Sets system properties "proactive.home" and "node.name" (used to parameterize the default
     * node.properties configuration file). Re-configures log4j for the new values of the properties to
     * take effect.
     */
    private static void configureLogging(String nodeName) {

        String proActiveHome = System.getProperty(CentralPAPropertyRepository.PA_HOME.getName());

        if (proActiveHome == null) {
            try {
                proActiveHome = ProActiveRuntimeImpl.getProActiveRuntime().getProActiveHome();
            } catch (ProActiveException e) {
                logger
                  .debug("Cannot find proactive home using ProActiveRuntime, will use RM home as ProActive home.");
                proActiveHome = PAResourceManagerProperties.RM_HOME.getValueAsString();
            }
            System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(), proActiveHome);
        }

        System.setProperty("node.name", nodeName);

        LogManager.resetConfiguration();

        String log4jConfigPropertyValue = System.getProperty(CentralPAPropertyRepository.LOG4J.getName());

        // (re-)configure log4j so that system properties set above take effect
        if (log4jConfigPropertyValue != null) {
            // log4j.configuration property is set (to a URL), use its value
            URL url;
            try {
                url = new URL(log4jConfigPropertyValue);
            } catch (MalformedURLException e) {
                throw new RuntimeException(
                  "Malformed log4j.configuration value: " + log4jConfigPropertyValue, e);
            }
            PropertyConfigurator.configure(url);
            logger.info("Reconfigured log4j using " + log4jConfigPropertyValue);
        } else {
            // log4j.configuration property is not set, use default log4j configuration for node
            String log4jConfig = proActiveHome + File.separator + "config" + File.separator + "log" +
              File.separator + "node.properties";
            // set log4j.configuration to stop ProActiveLogger#load from reconfiguring log4j once again
            if (new File(log4jConfig).exists()) {
                System.setProperty(CentralPAPropertyRepository.LOG4J.getName(), "file:" + log4jConfig);
                PropertyConfigurator.configure(log4jConfig);
                logger.info("Configured log4j using " + log4jConfig);
            } else {
                // use log4j config from JAR
                URL log4jConfigFromJar = RMNodeStarter.class.getResource("/config/log/node.properties");
                System
                  .setProperty(CentralPAPropertyRepository.LOG4J.getName(), log4jConfigFromJar
                    .toString());
                PropertyConfigurator.configure(log4jConfigFromJar);
                logger.info("Configured log4j using " + log4jConfigFromJar.toString());
            }
        }
    }

    /**
     * Configure node for dataSpaces
     *
     * @param node the node to be configured
     */
    private void configureForDataSpace(final Node node) {

        try {
            DataSpaceNodeConfigurationAgent conf = (DataSpaceNodeConfigurationAgent) PAActiveObject
              .newActive(DataSpaceNodeConfigurationAgent.class.getName(), null, node);
            conf.configureNode();
            closeDataSpaceOnShutdown(node);
            node.setProperty(DATASPACES_STATUS_PROP_NAME, Boolean.TRUE.toString());
        } catch (Throwable t) {
            logger.error("Cannot configure dataSpace", t);
            try {
                node.setProperty(DATASPACES_STATUS_PROP_NAME, Formatter.stackTraceToString(t));
            } catch (ProActiveException e) {
                logger.error("Cannot contact the node", e);
            }
        }
    }

    private void closeDataSpaceOnShutdown(final Node node) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataSpaceNodeConfigurationAgent conf = (DataSpaceNodeConfigurationAgent) PAActiveObject
                      .newActive(DataSpaceNodeConfigurationAgent.class.getName(), null, node);
                    BooleanWrapper closeNodeConfiguration = conf.closeNodeConfiguration();
                    PAFuture.waitFor(closeNodeConfiguration, DATASPACE_CLOSE_TIMEOUT);
                    if (closeNodeConfiguration.getBooleanValue()) {
                        logger.debug("Dataspaces are successfully closed for node " +
                          node.getNodeInformation().getURL());
                    }
                } catch (Throwable t) {
                    logger.debug("Cannot close data spaces configuration", t);
                }

            }
        }));
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
                        logger.error(ExitStatus.CRED_UNREADABLE.description, ke);
                        System.exit(ExitStatus.CRED_UNREADABLE.exitCode);
                    }
                    // The name of the env variable that contains
                } else if (cl.hasOption(OPTION_CREDENTIAL_ENV)) {
                    final String variableName = cl.getOptionValue(OPTION_CREDENTIAL_ENV);
                    final String value = System.getenv(variableName);
                    if (value == null) {
                        logger.error(ExitStatus.CRED_ENVIRONMENT.description);
                        System.exit(ExitStatus.CRED_ENVIRONMENT.exitCode);
                    }
                    try {
                        credentials = Credentials.getCredentialsBase64(value.getBytes());
                    } catch (KeyException ke) {
                        logger.error(ExitStatus.CRED_DECODE.description, ke);
                        System.exit(ExitStatus.CRED_DECODE.exitCode);
                    }
                    // Read the credentials directly from the command-line argument
                } else if (cl.hasOption(OPTION_CREDENTIAL_VAL)) {
                    final String str = cl.getOptionValue(OPTION_CREDENTIAL_VAL);
                    try {
                        credentials = Credentials.getCredentialsBase64(str.getBytes());
                    } catch (KeyException ke) {
                        logger.error(ExitStatus.CRED_DECODE.description, ke);
                        System.exit(ExitStatus.CRED_DECODE.exitCode);
                    }
                } else {
                    credentials = getDefaultCredentials();
                }
            }

            // Optional node name
            if (cl.hasOption(OPTION_NODE_NAME)) {
                nodeName = cl.getOptionValue(OPTION_NODE_NAME);
            } else {
                nodeName = getDefaultNodeName();
            }
            // Optional node source name
            if (cl.hasOption(OPTION_SOURCE_NAME)) {
                nodeSourceName = cl.getOptionValue(OPTION_SOURCE_NAME);
            }
            // Optional wait on join option
            if (cl.hasOption(OPTION_WAIT_AND_JOIN_TIMEOUT)) {
                RMNodeStarter.WAIT_ON_JOIN_TIMEOUT_IN_MS = Integer.valueOf(cl
                  .getOptionValue(OPTION_WAIT_AND_JOIN_TIMEOUT));
                RMNodeStarter.WAIT_ON_JOIN_TIMEOUT_IN_MS_USER_SUPPLIED = true;
            }
            // Optional ping delay
            if (cl.hasOption(OPTION_PING_DELAY)) {
                RMNodeStarter.PING_DELAY_IN_MS = Integer.valueOf(cl.getOptionValue(OPTION_PING_DELAY));
                RMNodeStarter.PING_DELAY_IN_MS_USER_SUPPLIED = true;
            }
            // Optional number of add node attempts before quitting
            if (cl.hasOption(OPTION_ADD_NODE_ATTEMPTS)) {
                RMNodeStarter.NB_OF_ADD_NODE_ATTEMPTS = Integer.valueOf(cl
                  .getOptionValue(OPTION_ADD_NODE_ATTEMPTS));
                RMNodeStarter.NB_OF_ADD_NODE_ATTEMPTS_USER_SUPPLIED = true;
            }
            // Optional delay between add node attempts
            if (cl.hasOption(OPTION_ADD_NODE_ATTEMPTS_DELAY)) {
                RMNodeStarter.ADD_NODE_ATTEMPTS_DELAY_IN_MS = Integer.valueOf(cl
                  .getOptionValue(OPTION_ADD_NODE_ATTEMPTS_DELAY));
                RMNodeStarter.ADD_NODE_ATTEMPTS_DELAY_IN_MS_USER_SUPPLIED = true;
            }
            // Optional help option
            if (cl.hasOption(OPTION_HELP)) {
                printHelp = true;
            }
        } catch (Throwable t) {
            printHelp = true;
            logger.info(t.getMessage());
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

    private String getDefaultNodeName() {
        try {
            return InetAddress.getLocalHost().getHostName() + "_" + new Sigar().getPid();
        } catch (Throwable error) {
            logger
              .warn(
                "Failed to retrieve hostname or pid to compute node name, will fallback to default value",
                error);
            return "PA-AGENT_NODE";
        }
    }

    private Credentials getDefaultCredentials() {
        try {
            return Credentials.getCredentials();
        } catch (KeyException fromDiskKeyException) {
            try {
                Credentials credentialsFromRMHome = Credentials.getCredentials(new File(
                  PAResourceManagerProperties.RM_HOME.getValueAsStringOrNull(),
                  "config/authentication/rm.cred").getAbsolutePath());
                logger.info("Using default credentials from ProActive home, authenticating as user rm");
                return credentialsFromRMHome;
            } catch (KeyException fromRMHomeKeyException) {
                try {
                    Credentials credentialsFromJar = Credentials.getCredentials(RMNodeStarter.class
                      .getResourceAsStream("/config/authentication/rm.cred"));
                    logger.info("Using default credentials from ProActive jars, authenticating as user rm");
                    return credentialsFromJar;
                } catch (Exception fromJarKeyException) {
                    logger
                      .error(
                        "Failed to read credentials, from location obtained using system property, RM home or ProActive jars",
                        fromJarKeyException);
                    System.exit(ExitStatus.CRED_UNREADABLE.exitCode);
                }
            }
        }
        return null;
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
            //check the user supplied values
            //performed after fillParameters to be able to override fillParameters in subclasses
            checkUserSuppliedParameters();
        } catch (ParseException pe) {
            pe.printStackTrace();
            System.exit(ExitStatus.RMNODE_PARSE_ERROR.exitCode);
        }

    }

    /**
     * Checks that user has supplied parameters or override them with java properties values...
     */
    private void checkUserSuppliedParameters() {
        //need an exhaustive list...
        //first, the number of add attempts
        if (!NB_OF_ADD_NODE_ATTEMPTS_USER_SUPPLIED) {
            String tmpNBAddString = System.getProperty(RMNodeStarter.NB_OF_ADD_NODE_ATTEMPTS_PROP_NAME);
            if (tmpNBAddString != null) {
                try {
                    RMNodeStarter.NB_OF_ADD_NODE_ATTEMPTS = Integer.parseInt(tmpNBAddString);
                    logger.debug("Number of add node attempts not supplied by user, using java property: " +
                      RMNodeStarter.NB_OF_ADD_NODE_ATTEMPTS);
                } catch (Exception e) {
                    logger.warn("Cannot use the value supplied by java property " +
                      RMNodeStarter.NB_OF_ADD_NODE_ATTEMPTS_PROP_NAME + " : " + tmpNBAddString +
                      ". Using default " + RMNodeStarter.NB_OF_ADD_NODE_ATTEMPTS);
                }
            } else {
                logger.debug("Using default value for the number of add node attempts: " +
                  RMNodeStarter.NB_OF_ADD_NODE_ATTEMPTS);
            }
        } else {
            logger.debug("Using value supplied by user for the number of add node attempts: " +
              RMNodeStarter.NB_OF_ADD_NODE_ATTEMPTS);
        }

        String numberOfReconnection = System.getProperty(RMNodeStarter.NB_OF_RECONNECTION_ATTEMPTS_PROP_NAME);
        if (numberOfReconnection != null) {
            try {
                RMNodeStarter.NB_OF_RECONNECTION_ATTEMPTS = Integer.parseInt(numberOfReconnection);
                logger
                  .debug("Number of attempts to reconnect a node to the resource manager when connection is lost: " +
                    RMNodeStarter.NB_OF_RECONNECTION_ATTEMPTS);
            } catch (Exception e) {
                logger.warn("Cannot use the value supplied by java property " +
                  RMNodeStarter.NB_OF_RECONNECTION_ATTEMPTS_PROP_NAME + " : " + numberOfReconnection +
                  ". Using default " + RMNodeStarter.NB_OF_RECONNECTION_ATTEMPTS);
            }
        } else {
            logger.debug("Using default value for the number of reconnection attempts: " +
              RMNodeStarter.NB_OF_RECONNECTION_ATTEMPTS);
        }

        //the delay between two add node attempts
        if (!ADD_NODE_ATTEMPTS_DELAY_IN_MS_USER_SUPPLIED) {
            String tmpADDNodeDelay = System.getProperty(RMNodeStarter.ADD_NODE_ATTEMPTS_DELAY_PROP_NAME);
            if (tmpADDNodeDelay != null) {
                try {
                    RMNodeStarter.ADD_NODE_ATTEMPTS_DELAY_IN_MS = Integer.parseInt(tmpADDNodeDelay);
                    logger.debug("Add node attempts delay not supplied by user, using java property: " +
                      RMNodeStarter.ADD_NODE_ATTEMPTS_DELAY_IN_MS);
                } catch (Exception e) {
                    logger.warn("Cannot use the value supplied by java property " +
                      RMNodeStarter.ADD_NODE_ATTEMPTS_DELAY_PROP_NAME + " : " + tmpADDNodeDelay +
                      ". Using default " + RMNodeStarter.ADD_NODE_ATTEMPTS_DELAY_IN_MS);
                }
            } else {
                logger.debug("Using default value for the add node attempts delay: " +
                  RMNodeStarter.ADD_NODE_ATTEMPTS_DELAY_IN_MS);
            }
        } else {
            logger.debug("Using value supplied by user for the number the add node attempts delay: " +
              RMNodeStarter.ADD_NODE_ATTEMPTS_DELAY_IN_MS);
        }

        //the delay of the node -> rm ping
        if (!PING_DELAY_IN_MS_USER_SUPPLIED) {
            String tmpPingDelay = System.getProperty(RMNodeStarter.PING_DELAY_PROP_NAME);
            if (tmpPingDelay != null) {
                try {
                    RMNodeStarter.PING_DELAY_IN_MS = Integer.parseInt(tmpPingDelay);
                    logger.debug("RM Ping delay not supplied by user, using java property: " +
                      RMNodeStarter.PING_DELAY_IN_MS);
                } catch (Exception e) {
                    logger.warn("Cannot use the value supplied by java property " +
                      RMNodeStarter.PING_DELAY_PROP_NAME + " : " + tmpPingDelay + ". Using default " +
                      RMNodeStarter.PING_DELAY_IN_MS);
                }
            } else {
                logger.debug("Using default value for the rm ping delay: " + RMNodeStarter.PING_DELAY_IN_MS);
            }
        } else {
            logger.debug("Using value supplied by user for the rm ping delay: " +
              RMNodeStarter.PING_DELAY_IN_MS);
        }

        //the "joinRM" timeout
        if (!WAIT_ON_JOIN_TIMEOUT_IN_MS_USER_SUPPLIED) {
            String tmpWait = System.getProperty(RMNodeStarter.WAIT_ON_JOIN_PROP_NAME);
            if (tmpWait != null) {
                try {
                    RMNodeStarter.WAIT_ON_JOIN_TIMEOUT_IN_MS = Integer.parseInt(tmpWait);
                    logger.debug("Wait on join not supplied by user, using java property: " +
                      RMNodeStarter.WAIT_ON_JOIN_TIMEOUT_IN_MS);
                } catch (Exception e) {
                    logger.warn("Cannot use the value supplied by java property " +
                      RMNodeStarter.WAIT_ON_JOIN_PROP_NAME + " : " + tmpWait + ". Using default " +
                      RMNodeStarter.WAIT_ON_JOIN_TIMEOUT_IN_MS);
                }
            } else {
                logger.debug("Using default value for the wait on join: " +
                  RMNodeStarter.WAIT_ON_JOIN_TIMEOUT_IN_MS);
            }
        } else {
            logger.debug("Using value supplied by user for the wait on join timeout: " +
              RMNodeStarter.WAIT_ON_JOIN_TIMEOUT_IN_MS);
        }
    }

    /**
     * Tries to join to the Resource Manager with a specified timeout
     * at the given URL, logs with provided credentials and adds the local node to
     * the Resource Manager. Handles all errors/exceptions.
     */
    protected ResourceManager registerInRM(final Credentials credentials, final String rmURL,
      final String nodeName) {

        // Create the full url to contact the Resource Manager
        final String fullUrl = rmURL.endsWith("/") ? rmURL + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION
          : rmURL + "/" + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION;
        // Try to join the Resource Manager with a specified timeout
        RMAuthentication auth = null;
        try {
            auth = RMConnection.waitAndJoin(fullUrl, WAIT_ON_JOIN_TIMEOUT_IN_MS);
            if (auth == null) {
                logger.error(ExitStatus.RMAUTHENTICATION_NULL.description);
                System.exit(ExitStatus.RMAUTHENTICATION_NULL.exitCode);
            }
        } catch (Throwable t) {
            logger.error("Unable to join the Resource Manager at " + rmURL, t);
            System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }

        ResourceManager rm = null;
        // 3 - Log using credential
        try {
            rm = auth.login(credentials);
            if (rm == null) {
                logger.error(ExitStatus.RM_NULL.description);
                System.exit(ExitStatus.RM_NULL.exitCode);
            }
        } catch (Throwable t) {
            logger.error("Unable to log into the Resource Manager at " + rmURL, t);
            System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }

        // initializing JMX server with Sigar beans
        sigarExposer = new SigarExposer(nodeName);
        final RMAuthentication rmAuth = auth;
        sigarExposer.boot(auth, false, new PermissionChecker() {
            public boolean checkPermission(Credentials cred) {
                ResourceManager rm = null;
                try {
                    rm = rmAuth.login(cred);
                    if (NB_OF_ADD_NODE_ATTEMPTS == 0)
                        return true;

                    boolean isAdmin = rm.isNodeAdmin(node.getNodeInformation().getURL()).getBooleanValue();
                    if (!isAdmin) {
                        throw new SecurityException("Permission denied");
                    }
                    return true;
                } catch (LoginException e) {
                    throw new SecurityException(e);
                } finally {
                    if (rm != null) {
                        rm.disconnect();
                    }
                }
            }
        });
        try {
            node.setProperty(JMX_URL + JMXTransportProtocol.RMI, sigarExposer.getAddress(
              JMXTransportProtocol.RMI).toString());
            node.setProperty(JMX_URL + JMXTransportProtocol.RO, sigarExposer.getAddress(
              JMXTransportProtocol.RO).toString());
        } catch (Exception e) {
            logger.error("", e);
        }

        if (NB_OF_ADD_NODE_ATTEMPTS == 0) {
            // no need to add the node to the resource manager
            return rm;
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
                        logger
                          .info("Different previous URL registered by this agent has been found. Remove previous registration.");
                        rm.removeNode(previousURL, true);
                    }
                    // store the node URL
                    this.storeNodeURL(nodeName, rank, this.nodeURL);
                    logger.info("Node " + this.nodeURL + " added. URL is stored in " +
                      getNodeURLFilename(nodeName, rank));
                } else {
                    logger.info("Node " + this.nodeURL + " added.");
                }
            } else { // not yet registered
                logger.info("Attempt number " + attempts + " out of " + NB_OF_ADD_NODE_ATTEMPTS +
                  " to add the local node to the Resource Manager at " + rmURL + " has failed.");
                try {
                    Thread.sleep(ADD_NODE_ATTEMPTS_DELAY_IN_MS);
                } catch (InterruptedException e) {
                    logger.info("Interrupted", e);
                }
            }
        }// while

        if (!isNodeAdded) {
            // if not registered
            logger.error("The Resource Manager was unable to add the local node " + this.nodeURL + " after " +
              NB_OF_ADD_NODE_ATTEMPTS + " attempts. The application will exit.");
            System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }
        return rm;
    }

    protected void readAndSetTheRank() {
        String rankAsString = System.getProperty(RANK_PROP_NAME);
        if (rankAsString == null) {
            logger.debug("Rank is not set. Previous URLs will not be stored");
            this.removePrevious = false;
        } else {
            try {
                this.rank = Integer.parseInt(rankAsString);
                this.removePrevious = true;
                logger.info("Rank is " + this.rank);
            } catch (Throwable e) {
                logger.warn("Rank cannot be read due to " + e.getMessage() +
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
                logger.error(RMNodeStarter.ExitStatus.RMNODE_NULL.description);
                System.exit(RMNodeStarter.ExitStatus.RMNODE_NULL.exitCode);
            }
            // setting system properties to node (they will be accessible remotely) 
            for (Object key : System.getProperties().keySet()) {
                localNode.setProperty(key.toString(), System.getProperty(key.toString()));
            }
        } catch (Throwable t) {
            logger.error("Unable to create the local node " + nodeName, t);
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
                logger.warn("NodeURL file already exists ; delete it.");
                f.delete();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            out.write(nodeURL);
            out.write(System.getProperty("line.separator"));
            out.close();
        } catch (IOException e) {
            logger.warn("NodeURL cannot be created.", e);
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
        return new File(tmpDir, URL_TMPFILE_PREFIX + "_" + nodeName + "-" + rank).getAbsolutePath();
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
          "Cannot read the submitted credential's key."), CRED_DECODE(201,
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

    }

    /**
     * CommandLineBuilder is an utility class that provide users with the capability to automatise
     * the RMNodeStarter command line building. We encourage Infrastructure Manager providers to
     * use this class as it is used as central point for applying changes to the RMNodeStarter
     * properties, for instance, if the classpath needs to bu updated, a call to
     *  will reflect the change.
     *
     */
    public static final class CommandLineBuilder implements Cloneable {
        public static final String OBFUSC = "[OBFUSCATED_CRED]";
        private String nodeName, sourceName, javaPath, rmURL, credentialsFile, credentialsValue,
          credentialsEnv, rmHome;
        private long pingDelay = 30000;
        private Properties paPropProperties;
        private List<String> paPropList;
        private int addAttempts = -1, addAttemptsDelay = -1;
        private final String addonsDir = "addons";

        private OperatingSystem targetOS = OperatingSystem.UNIX;

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
                    String rmHome;
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
            return null;
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
         * @deprecated Please use {@link #setPaProperties(List)}
         */
        public void setPaProperties(String paProp) {
            this.setPaProperties(Arrays.asList(paProp.split(" ")));
        }

        /**
         * To set a list of String standing for the ProActive Properties, appended to the built command line without any modification.
         * If a call to {@link #setPaProperties(byte[])} or {@link #setPaProperties(File)} of {@link #setPaProperties(Map)} has already been made, the PAProperties structure will be cleaned up...
         */
        public void setPaProperties(List<String> paPropList) {
            if (this.paPropProperties != null) {
                this.paPropProperties = null;
            }
            this.paPropList = paPropList;
        }

        /**
         * To set the PAproperties of the node. If a previous call to
         * {@link #setPaProperties(byte[])} or {@link #setPaProperties(File)} of {@link #setPaProperties(Map)} has already been made,
         * this one will override the previous call. This file must be a valid ProActive XML configuration file.
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
         * @return the ping delay used to maintain connectivity with the RM. -1 means no ping
         */
        public long getPingDelay() {
            return pingDelay;
        }

        /**
         * @return the number of attempts made to add the node to the core
         */
        public int getAddAttempts() {
            return addAttempts;
        }

        /**
         * @return the delay between two add attempts
         */
        public int getAddAttemptsDelay() {
            return addAttemptsDelay;
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
         */
        public Properties getPaProperties() {
            return paPropProperties;
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
            List<String> command = this.buildCommandLineAsList(displayCreds);
            return Tools.join(command, " ");
        }

        /**
         * Same as {@link RMNodeStarter.CommandLineBuilder#buildCommandLine(boolean)} but the command is a list of String.
         * @param displayCreds if true displays the credentials in the command line if false, obfuscates them
         * @return The RMNodeStarter command line as a list of String.
         * @throws IOException if you supplied a ProActive Configuration file that doesn't exist.
         */
        public List<String> buildCommandLineAsList(boolean displayCreds) throws IOException {
            final ArrayList<String> command = new ArrayList<String>();
            final OperatingSystem os = this.getTargetOS();
            final Properties paProp = this.getPaProperties();

            String rmHome = this.getRmHome();
            if (rmHome != null) {
                if (!rmHome.endsWith(os.fs)) {
                    rmHome = rmHome + os.fs;
                }
            } else {
                rmHome = "";
            }

            final String libRoot = rmHome + "dist" + os.fs + "lib" + os.fs;
            String javaPath = this.getJavaPath();
            if (javaPath != null) {
                command.add(javaPath);
            } else {
                logger.warn("java path isn't set in RMNodeStarter configuration.");
                command.add("java");
            }

            //building configuration
            if (paProp != null) {
                Set<Object> keys = paProp.keySet();
                for (Object key : keys) {
                    command.add("-D" + key + "=" + paProp.get(key));
                }
            } else {
                if (this.paPropList != null) {
                    command.addAll(this.paPropList);
                }
            }
            //building classpath
            command.add("-cp");
            final StringBuilder classpath = new StringBuilder(".");

            // add the content of addons dir on the classpath
            classpath.append(os.ps).append(rmHome).append(this.addonsDir);
            classpath.append(os.ps).append(libRoot).append("*");

            // add jars inside the addons directory
            File addonsAbsolute = new File(rmHome + this.addonsDir);
            addonsAbsolute.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.getName().matches(".*[.]jar")) {
                        classpath.append(os.ps).append(pathname.getAbsolutePath());
                    }
                    return false;
                }
            });
            command.add(classpath.toString());
            command.add(RMNodeStarter.class.getName());

            //appending options
            int addAttempts = this.getAddAttempts();
            if (addAttempts != -1) {
                command.add("-" + OPTION_ADD_NODE_ATTEMPTS);
                command.add(Integer.toString(addAttempts));
            }
            int addAttemptsDelay = this.getAddAttemptsDelay();
            if (addAttemptsDelay != -1) {
                command.add("-" + OPTION_ADD_NODE_ATTEMPTS_DELAY);
                command.add(Integer.toString(addAttemptsDelay));
            }
            String credsEnv = this.getCredentialsEnv();
            if (credsEnv != null) {
                command.add("-" + OPTION_CREDENTIAL_ENV);
                command.add(credsEnv);
            }
            String credsFile = this.getCredentialsFile();
            if (credsFile != null) {
                command.add("-" + OPTION_CREDENTIAL_FILE);
                command.add(credsFile);
            }
            String credsValue = this.getCredentialsValue();
            if (credsValue != null) {
                command.add("-" + OPTION_CREDENTIAL_VAL);
                command.add(displayCreds ? credsValue : OBFUSC);
            }
            String nodename = this.getNodeName();
            if (nodename != null) {
                command.add("-" + OPTION_NODE_NAME);
                command.add(nodename);
            }
            String nodesource = this.getSourceName();
            if (nodesource != null) {
                command.add("-" + OPTION_SOURCE_NAME);
                command.add(nodesource);
            }
            String rmurl = this.getRmURL();
            if (rmurl != null) {
                command.add("-" + OPTION_RM_URL);
                command.add(rmurl);
                //if the rm url != null we can specify a ping delay
                //it is not relevant if the rm url == null
                command.add("-" + OPTION_PING_DELAY);
                command.add(Long.toString(this.getPingDelay()));
            }
            return command;
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
