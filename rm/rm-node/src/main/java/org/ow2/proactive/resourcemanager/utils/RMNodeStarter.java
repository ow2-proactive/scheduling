/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyException;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
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
import org.ow2.proactive.utils.CookieBasedProcessTreeKiller;
import org.ow2.proactive.utils.Formatter;
import org.ow2.proactive.utils.Tools;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;

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
    protected String nodeSourceName = null;

    // While logger is not configured and it not set with sys properties, use Console logger
    static {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            Logger.getRootLogger().getLoggerRepository().resetConfiguration();
            BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%m%n")));
            Logger.getRootLogger().setLevel(Level.INFO);
        }
    }
    static final Logger logger = Logger.getLogger(RMNodeStarter.class);

    /** Prefix for temp files that store nodes URL */
    private static final String URL_TMPFILE_PREFIX = "PA-AGENT_URL";

    /** Name of the java property to set the rank */
    private final static String RANK_PROP_NAME = "proactive.agent.rank";

    /** Name of the java property to set the data spaces configuration status */
    public final static String DATASPACES_STATUS_PROP_NAME = "proactive.dataspaces.status";

    /** Name of the node property that stores the Sigar JMX connection URL*/
    public static final String JMX_URL = "proactive.node.jmx.sigar.";

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
    private static int WAIT_ON_JOIN_TIMEOUT_IN_MS = 60000;
    /** to inform that the user supplied a value from the command line for the join rm timeout */
    private static boolean WAIT_ON_JOIN_TIMEOUT_IN_MS_USER_SUPPLIED = false;
    /** Name of the java property to set the timeout value used to join the resource manager */
    private final static String WAIT_ON_JOIN_PROP_NAME = "proactive.node.joinrm.timeout";

    /**
     * The ping delay used in RMPinger that pings the RM and exists if the
     * Resource Manager is down
     */
    private static long PING_DELAY_IN_MS = 30000;
    /** to inform that the user supplied a value from the command line for the ping */
    private static boolean PING_DELAY_IN_MS_USER_SUPPLIED = false;
    /** Name of the java property to set the node -> rm ping frequency value */
    private final static String PING_DELAY_PROP_NAME = "proactive.node.ping.delay";

    /** The number of attempts to add the local node to the RM before quitting */
    private static int NB_OF_ADD_NODE_ATTEMPTS = 10;
    /** to inform that the user supplied a value from the command line for the number of "add" attempts */
    private static boolean NB_OF_ADD_NODE_ATTEMPTS_USER_SUPPLIED = false;
    /** Name of the java property to set the number of attempts performed to add a node to the resource manager */
    private final static String NB_OF_ADD_NODE_ATTEMPTS_PROP_NAME = "proactive.node.add.attempts";

    /** The number of attempts to reconnect the node to the RM before quitting (interval between each attempt is
     * given by {@link #PING_DELAY_IN_MS})
     */
    protected static int NB_OF_RECONNECTION_ATTEMPTS = 2 * 5; // so 5 minutes by default

    /** Name of the java property to set the number of attempts performed to add a node to the resource manager */
    private final static String NB_OF_RECONNECTION_ATTEMPTS_PROP_NAME = "proactive.node.reconnection.attempts";

    /** The delay, in millis, between two attempts to add a node */
    private static int ADD_NODE_ATTEMPTS_DELAY_IN_MS = 5000;
    /** to inform that the user supplied a value from the command line for the delay between two add attempts*/
    private static boolean ADD_NODE_ATTEMPTS_DELAY_IN_MS_USER_SUPPLIED = false;
    /** Name of the java property to set the delay between two attempts performed to add a node to the resource manager */
    private final static String ADD_NODE_ATTEMPTS_DELAY_PROP_NAME = "proactive.node.add.delay";
    /** Name of the java property to set the node source name */
    private final static String NODESOURCE_PROP_NAME = "proactive.node.nodesource";

    private int discoveryTimeoutInMs = 3 * 1000;
    private final static String DISCOVERY_TIMEOUT_IN_MS_NAME = "proactive.node.discovery.timeout";
    private int discoveryPort = 64739;
    private final static String DISCOVERY_PORT_NAME = "proactive.node.discovery.port";

    private int workers = 1;
    private final static String NUMBER_OF_WORKERS_PROPERTY_NAME = "proactive.node.workers";

    // the rank of this node
    private int rank;
    // if true, previous nodes with different URLs are removed from the RM
    private boolean removePrevious;

    private int numberOfReconnectionAttemptsLeft;

    private static final long DATASPACE_CLOSE_TIMEOUT = 3 * 1000; // seconds

    static final char OPTION_CREDENTIAL_FILE = 'f';
    static final char OPTION_CREDENTIAL_ENV = 'e';
    static final char OPTION_CREDENTIAL_VAL = 'v';
    static final char OPTION_RM_URL = 'r';
    static final char OPTION_NODE_NAME = 'n';
    static final char OPTION_SOURCE_NAME = 's';
    private static final char OPTION_PING_DELAY = 'p';
    private static final char OPTION_ADD_NODE_ATTEMPTS = 'a';
    private static final char OPTION_ADD_NODE_ATTEMPTS_DELAY = 'd';
    private static final String OPTION_WAIT_AND_JOIN_TIMEOUT = "wt";
    static final String OPTION_WORKERS = "w";
    private static final String OPTION_DISCOVERY_PORT = "dp";
    private static final String OPTION_DISCOVERY_TIMEOUT = "dt";
    private static final char OPTION_HELP = 'h';

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
        final Option waitOnJoinTimeout = new Option(OPTION_WAIT_AND_JOIN_TIMEOUT,
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
                PING_DELAY_IN_MS + "). A null or negative frequency means no ping at all.");
        pingDelay.setRequired(false);
        pingDelay.setArgName("millis");
        options.addOption(pingDelay);
        // The number of attempts option
        final Option addNodeAttempts = new Option(Character.toString(OPTION_ADD_NODE_ATTEMPTS),
            "addNodeAttempts", true,
            "number of attempts to add the node(s) to the resource manager. Default is " +
                NB_OF_ADD_NODE_ATTEMPTS + "). When 0 is specified node(s) remains alive without " +
                "trying to add itself to the RM. Otherwise the process is terminated when number " +
                "of attempts exceeded.");
        addNodeAttempts.setRequired(false);
        addNodeAttempts.setArgName("number");
        options.addOption(addNodeAttempts);
        // The delay between attempts option
        final Option addNodeAttemptsDelay = new Option(Character.toString(OPTION_ADD_NODE_ATTEMPTS_DELAY),
            "addNodeAttemptsDelay", true,
            "delay in millis between attempts to add the node(s) to the resource manager (default is " +
                ADD_NODE_ATTEMPTS_DELAY_IN_MS + ")");
        addNodeAttemptsDelay.setRequired(false);
        addNodeAttemptsDelay.setArgName("millis");
        options.addOption(addNodeAttemptsDelay);
        // The discovery port
        final Option discoveryPort = new Option(OPTION_DISCOVERY_PORT, "discoveryPort", true,
            "port to use for RM discovery (default is " + this.discoveryPort + ")");
        discoveryPort.setRequired(false);
        options.addOption(discoveryPort);
        // The discovery timeout
        final Option discoveryTimeout = new Option(OPTION_DISCOVERY_TIMEOUT, "discoveryTimeout", true,
            "timeout to use for RM discovery (default is " + discoveryTimeoutInMs + "ms)");
        discoveryTimeout.setRequired(false);
        options.addOption(discoveryTimeout);

        // The number of workers
        final Option workers = new Option(
            OPTION_WORKERS,
            "workers",
            true,
            "Number of workers, i.e number of tasks that can be executed in parallel on this node (default is 1). If no value specified, number of cores.");
        workers.setRequired(false);
        workers.setOptionalArg(true);
        options.addOption(workers);

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
            args = JVMPropertiesPreloader.overrideJVMProperties(args);
            CookieBasedProcessTreeKiller.registerKillChildProcessesOnShutdown("node");
            RMNodeStarter starter = new RMNodeStarter();
            starter.doMain(args);
        } catch (Throwable t) {
            System.err
                    .println("A major problem occurred when trying to start a node and register it into the Resource Manager, see the stacktrace below");
            // Fix for SCHEDULING-1588
            if (t instanceof java.lang.NoClassDefFoundError) {
                System.err
                        .println("Unable to load a class definition, maybe the classpath is not accessible");
            }
            t.printStackTrace(System.err);
            System.exit(-2);
        }
    }

    protected void doMain(final String args[]) {
        configureSecurityManager();
        configureRMAndProActiveHomes();
        configureProActiveDefaultConfigurationFile();
        loadSigarIfRunningWithOneJar();

        String nodeName = parseCommandLine(args);

        configureLogging(nodeName);

        selectNetworkInterface();

        readAndSetTheRank();

        List<Node> nodes = createNodes(nodeName);

        Tools.logAvailableScriptEngines(logger);

        if (nodeSourceName != null && nodeSourceName.length() > 0) {
            // setting system the property with node source name
            System.setProperty(NODESOURCE_PROP_NAME, nodeSourceName);
        }

        if (rmURL == null) {
            rmURL = tryBroadcastDiscoveryOrExit();
        }

        connectToResourceManager(nodeName, nodes);
    }

    private List<Node> createNodes(String nodeName) {
        List<Node> nodes = new ArrayList<>();
        for (int nodeIndex = 0; nodeIndex < workers; nodeIndex++) {
            String indexedNodeName = nodeName;
            if (workers > 1) {
                indexedNodeName += "_" + nodeIndex;
            }
            Node node = createLocalNode(indexedNodeName);
            configureForDataSpace(node);
            nodes.add(node);
            logger.debug("URL of node " + nodeIndex + " " + node.getNodeInformation().getURL());
        }
        return nodes;
    }

    private String tryBroadcastDiscoveryOrExit() {
        try {
            return new BroadcastDiscoveryClient(discoveryPort).discover(discoveryTimeoutInMs);
        } catch (IOException e) {
            logger
                    .info("No URL to connect to was specified and discovery failed, please specify a URL with -r parameter.");
            System.exit(ExitStatus.RM_NO_PING.exitCode);
            return null;
        }
    }

    private void connectToResourceManager(String nodeName, List<Node> nodes) {
        ResourceManager rm = this.registerInRM(credentials, rmURL, nodeName, nodes);
        resetReconnectionAttemptsLeft();
        pingAllNodes(nodes, rm);
    }

    private void pingAllNodes(List<Node> nodes, ResourceManager rm) {
        if (rm != null) {
            logger.info("Connected to the resource manager at " + rmURL);

            // NB_OF_ADD_NODE_ATTEMPTS is used here to disable pinging
            if (PING_DELAY_IN_MS > 0 && NB_OF_ADD_NODE_ATTEMPTS > 0) {

                while (numberOfReconnectionAttemptsLeft >= 0) {
                    try {
                        pingAllNodesIndefinitely(nodes, rm);
                    } catch (NotConnectedException e) {
                        rm = reconnectToResourceManager();
                    } catch (ProActiveRuntimeException e) {
                        rm = reconnectToResourceManager();
                    } catch (IllegalStateException e) {
                        logger.error(ExitStatus.RMNODE_ILLEGAL_STATE.description);
                        System.exit(ExitStatus.RMNODE_ILLEGAL_STATE.exitCode);
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
            RMAuthentication auth = RMConnection.waitAndJoin(rmURL, WAIT_ON_JOIN_TIMEOUT_IN_MS);
            return auth.login(credentials);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    private void pingAllNodesIndefinitely(List<Node> nodes, ResourceManager rm) {
        while (allNodesAreAvailable(nodes, rm)) {
            try {
                if (numberOfReconnectionAttemptsLeft < NB_OF_RECONNECTION_ATTEMPTS) {
                    logger.info("Node successfully reconnected to the resource manager");
                    resetReconnectionAttemptsLeft();
                }
                Thread.sleep(PING_DELAY_IN_MS);
            } catch (InterruptedException e) {
                logger.warn("Node ping activity is interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void resetReconnectionAttemptsLeft() {
        numberOfReconnectionAttemptsLeft = NB_OF_RECONNECTION_ATTEMPTS;
    }

    private boolean allNodesAreAvailable(List<Node> nodes, ResourceManager rm) {
        if (rm == null)
            throw new NotConnectedException("No connection to RM");

        for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            String url = node.getNodeInformation().getURL();
            if (!rm.setNodeAvailable(url).getBooleanValue()) {
                if (!rm.nodeIsAvailable(url).getBooleanValue()) {
                    logger.info("Node " + url + " removed");
                    iterator.remove(); // node removed manually by user
                } else {
                    return false;
                }
            }
        }

        if (nodes.size() == 0)
            throw new IllegalStateException("Cannot have zero nodes");

        return true;
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
            File defaultProActiveConfiguration = new File(System
                    .getProperty(PAResourceManagerProperties.RM_HOME.getKey()), "config/network/node.ini");
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
            boolean dataspaceConfigured = conf.configureNode();
            if (!dataspaceConfigured) {
                throw new NotConfiguredException(
                    "Failed to configure dataspaces, check the logs for more details");
            }
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

    protected String fillParameters(final CommandLine cl, final Options options) {
        boolean printHelp = false;

        try {

            // Optional rmURL option
            if (cl.hasOption(OPTION_RM_URL)) {
                rmURL = cl.getOptionValue(OPTION_RM_URL);
            }

            // if the user doesn't provide a rm URL, we don't care about the credentials
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

            String nodeName;
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

            // Discovery
            if (cl.hasOption(OPTION_DISCOVERY_PORT)) {
                discoveryPort = Integer.valueOf(cl.getOptionValue(OPTION_DISCOVERY_PORT));
            } else if (System.getProperty(DISCOVERY_PORT_NAME) != null) {
                discoveryPort = Integer.valueOf(System.getProperty(DISCOVERY_PORT_NAME));
            }

            if (cl.hasOption(OPTION_DISCOVERY_TIMEOUT)) {
                discoveryTimeoutInMs = Integer.valueOf(cl.getOptionValue(OPTION_DISCOVERY_TIMEOUT));
            } else if (System.getProperty(DISCOVERY_TIMEOUT_IN_MS_NAME) != null) {
                discoveryPort = Integer.valueOf(System.getProperty(DISCOVERY_TIMEOUT_IN_MS_NAME));
            }

            readWorkersOption(cl);

            // Optional help option
            if (cl.hasOption(OPTION_HELP)) {
                printHelp = true;
            }

            return nodeName;
        } catch (Throwable t) {
            printHelp = true;
            logger.info(t.getMessage());
            t.printStackTrace(System.err);
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
        return null;
    }

    // positive integer, empty (number of available cores or 1 (default if nothing specified)
    private void readWorkersOption(CommandLine cl) throws Exception {
        try {
            if (cl.hasOption(OPTION_WORKERS)) {
                if (cl.getOptionValue(OPTION_WORKERS) == null) {
                    workers = Runtime.getRuntime().availableProcessors();
                } else {
                    workers = Integer.valueOf(cl.getOptionValue(OPTION_WORKERS));
                }
            } else if (System.getProperty(NUMBER_OF_WORKERS_PROPERTY_NAME) != null) {
                if ("".equals(System.getProperty(NUMBER_OF_WORKERS_PROPERTY_NAME))) {
                    workers = Runtime.getRuntime().availableProcessors();
                } else {
                    workers = Integer.valueOf(System.getProperty(NUMBER_OF_WORKERS_PROPERTY_NAME));
                }
            } else {
                workers = 1;
            }
        } catch (NumberFormatException e) {
            throw new Exception("Number of workers should be a positive integer", e);
        }
        if (workers <= 0) {
            throw new Exception("Number of workers should be at least 1, was " + workers);
        }
    }

    private String getDefaultNodeName() {
        try {
            return InetAddress.getLocalHost().getHostName().replace('.', '_') + "_" + new Sigar().getPid();
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

    protected String parseCommandLine(String[] args) {
        final Options options = new Options();

        //we fill int the options object, child classes can override this method
        //to add new options...
        fillOptions(options);

        final CommandLineParser parser = new DefaultParser();

        CommandLine cl;
        try {
            cl = parser.parse(options, args);
            //now we update this object's fields given the options.
            String nodeName = fillParameters(cl, options);
            //check the user supplied values
            //performed after fillParameters to be able to override fillParameters in subclasses
            checkUserSuppliedParameters();
            return nodeName;
        } catch (ParseException pe) {
            pe.printStackTrace();
            System.exit(ExitStatus.RMNODE_PARSE_ERROR.exitCode);
        }

        return null;
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

    private RMAuthentication joinResourceManager(String rmURL) {
        // Create the full url to contact the Resource Manager
        final String fullUrl = rmURL.endsWith("/") ? rmURL + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION
                : rmURL + "/" + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION;
        // Try to join the Resource Manager with a specified timeout
        try {
            RMAuthentication auth = RMConnection.waitAndJoin(fullUrl, WAIT_ON_JOIN_TIMEOUT_IN_MS);
            if (auth == null) {
                logger.error(ExitStatus.RMAUTHENTICATION_NULL.description);
                System.exit(ExitStatus.RMAUTHENTICATION_NULL.exitCode);
            }
            return auth;
        } catch (Throwable t) {
            logger.error("Unable to join the Resource Manager at " + rmURL, t);
            System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }
        return null;
    }

    private ResourceManager loginToResourceManager(final Credentials credentials, final RMAuthentication auth) {
        try {
            ResourceManager rm = auth.login(credentials);
            if (rm == null) {
                logger.error(ExitStatus.RM_NULL.description);
                System.exit(ExitStatus.RM_NULL.exitCode);
            }
            return rm;
        } catch (Throwable t) {
            logger.error("Unable to log into the Resource Manager at " + rmURL, t);
            System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }
        return null;
    }

    /**
     * Tries to join to the Resource Manager with a specified timeout
     * at the given URL, logs with provided credentials and adds the local node to
     * the Resource Manager. Handles all errors/exceptions.
     */
    protected ResourceManager registerInRM(final Credentials credentials, final String rmURL,
            final String nodeName, final List<Node> nodes) {

        RMAuthentication auth = joinResourceManager(rmURL);
        final ResourceManager rm = loginToResourceManager(credentials, auth);

        // initializing JMX server with Sigar beans
        SigarExposer sigarExposer = new SigarExposer(nodeName);
        final RMAuthentication rmAuth = auth;
        sigarExposer.boot(auth, false, new PermissionChecker() {
            @Override
            public boolean checkPermission(Credentials cred) {
                ResourceManager rm = null;
                try {
                    rm = rmAuth.login(cred);
                    if (NB_OF_ADD_NODE_ATTEMPTS == 0)
                        return true;

                    boolean isAdmin = rm.isNodeAdmin(nodes.get(0).getNodeInformation().getURL())
                            .getBooleanValue();
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

        for (final Node node : nodes) {
            try {
                node.setProperty(JMX_URL + JMXTransportProtocol.RMI, sigarExposer.getAddress(
                        JMXTransportProtocol.RMI).toString());
                node.setProperty(JMX_URL + JMXTransportProtocol.RO, sigarExposer.getAddress(
                        JMXTransportProtocol.RO).toString());
            } catch (Exception e) {
                logger.error("", e);
            }
            addNodeToResourceManager(rmURL, node, rm);
        }

        return rm;
    }

    private void addNodeToResourceManager(String rmURL, Node node, ResourceManager rm) {
        // Add the created node to the Resource Manager with a specified
        // number of attempts and a timeout between each attempt
        boolean isNodeAdded = false;
        int attempts = 0;
        String nodeUrl = node.getNodeInformation().getURL();
        String nodeName = node.getNodeInformation().getName();

        while ((!isNodeAdded) && (attempts < NB_OF_ADD_NODE_ATTEMPTS)) {
            attempts++;
            try {
                if (this.nodeSourceName != null) {
                    isNodeAdded = rm.addNode(nodeUrl, this.nodeSourceName).getBooleanValue();
                } else {
                    isNodeAdded = rm.addNode(nodeUrl).getBooleanValue();
                }
            } catch (AddingNodesException addException) {
                addException.printStackTrace();
                System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
            }
            if (isNodeAdded) {
                if (removePrevious) {
                    // try to remove previous URL if different...
                    String previousURL = this.getAndDeleteNodeURL(nodeName, rank);
                    if (previousURL != null && !previousURL.equals(nodeUrl)) {
                        logger
                                .info("Different previous URL registered by this agent has been found. Remove previous registration.");
                        rm.removeNode(previousURL, true);
                    }
                    // store the node URL
                    this.storeNodeURL(nodeName, rank, nodeUrl);
                    logger.info("Node " + nodeUrl + " added. URL is stored in " +
                        getNodeURLFilename(nodeName, rank));
                } else {
                    logger.info("Node " + nodeUrl + " added.");
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
            logger.error("The Resource Manager was unable to add the local node " + nodeUrl + " after " +
                NB_OF_ADD_NODE_ATTEMPTS + " attempts. The application will exit.");
            System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }
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
            localNode = NodeFactory.createLocalNode(nodeName, false, null, nodeName + "vnname");
            if (localNode == null) {
                logger.error(ExitStatus.RMNODE_NULL.description);
                System.exit(ExitStatus.RMNODE_NULL.exitCode);
            }
            // setting system properties to node (they will be accessible remotely)
            for (Object key : System.getProperties().keySet()) {
                localNode.setProperty(key.toString(), System.getProperty(key.toString()));
            }
        } catch (Throwable t) {
            logger.error("Unable to create the local node " + nodeName, t);
            System.exit(ExitStatus.RMNODE_ADD_ERROR.exitCode);
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
                FileUtils.forceDelete(f);
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            out.write(nodeURL);
            out.write(System.lineSeparator());
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
                FileUtils.deleteQuietly(f);
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

    private enum ExitStatus {
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
                303, "Was not able to add RMNode to the Resource Manager."), RMNODE_PARSE_ERROR(304,
                "Problem encountered while parsing " + RMNodeStarter.class.getName() + " command line."), RMNODE_EXIT_FORCED(
                305,
                "Was not able to add RMNode to the Resource Manager. Force system to exit to bypass daemon threads."), RMNODE_ILLEGAL_STATE(
                306, "Illegal state of RMNode (no nodes left)."), FAILED_TO_LAUNCH(-1, RMNodeStarter.class
                .getSimpleName() +
            " process hasn't been started at all."), UNKNOWN(-2, "Cannot determine exit status.");
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

}
