/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.util;

import static org.ow2.proactive.utils.ClasspathUtils.findSchedulerHome;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.security.KeyException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.router.Router;
import org.objectweb.proactive.extensions.pamr.router.RouterConfig;
import org.objectweb.proactive.utils.JVMPropertiesPreloader;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.resourcemanager.utils.RMStarter;
import org.ow2.proactive.scheduler.SchedulerFactory;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.JettyStarter;
import org.ow2.proactive.utils.PAMRRouterStarter;
import org.ow2.proactive.utils.SecurityPolicyLoader;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive.web.WebProperties;


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

    private static Logger logger = Logger.getLogger(SchedulerStarter.class);

    private static final int DEFAULT_NODES_TIMEOUT = 120 * 1000;

    private static final int DISCOVERY_DEFAULT_PORT = 64739;

    private static BroadcastDiscovery discoveryService;

    private static SchedulerHsqldbStarter hsqldbServer;

    protected static SchedulerAuthenticationInterface schedAuthInter;

    protected static String rmURL;

    protected static byte[] credentials;

    /**
     * Start the scheduler creation process.
     */
    public static void main(String[] args) {
        configureSchedulerAndRMAndPAHomes();
        configureSecurityManager();
        configureLogging();
        configureDerby();

        args = JVMPropertiesPreloader.overrideJVMProperties(args);

        Options options = getOptions();

        try {
            CommandLine commandLine = getCommandLine(args, options);
            if (commandLine.hasOption("h")) {
                displayHelp(options);
            } else {
                start(commandLine);
            }
        } catch (Exception e) {
            logger.error("Error when starting the scheduler", e);
            displayHelp(options);
            System.exit(6);
        }
    }

    protected static CommandLine getCommandLine(String[] args, Options options) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    protected static void start(CommandLine commandLine) throws Exception {
        ProActiveConfiguration.load(); // force properties loading to find out if PAMR router should be started

        if (!commandLine.hasOption("no-router")) {
            startRouter();
        }

        hsqldbServer = new SchedulerHsqldbStarter();
        hsqldbServer.startIfNeeded();

        String rmUrl = getRmUrl(commandLine);
        setCleanDatabaseProperties(commandLine);
        setCleanNodesourcesProperty(commandLine);

        rmUrl = connectToOrStartResourceManager(commandLine, rmUrl);

        if (commandLine.hasOption("rm-only")) {
            return;
        }

        SchedulerAuthenticationInterface schedulerAuthenticationInterface = startScheduler(commandLine, rmUrl);

        schedAuthInter = schedulerAuthenticationInterface;
        rmURL = rmUrl;

        if (!commandLine.hasOption("no-rest")) {
            List<String> applicationUrls = (new JettyStarter().deployWebApplications(rmUrl,
                                                                                     schedulerAuthenticationInterface.getHostURL()));
            if (applicationUrls != null) {
                for (String applicationUrl : applicationUrls) {
                    if (applicationUrl.endsWith("/rest")) {
                        if (!PASchedulerProperties.SCHEDULER_REST_URL.isSet()) {
                            PASchedulerProperties.SCHEDULER_REST_URL.updateProperty(applicationUrl);
                        }

                    }
                    if (applicationUrl.endsWith("/catalog")) {
                        if (!PASchedulerProperties.CATALOG_REST_URL.isSet()) {
                            PASchedulerProperties.CATALOG_REST_URL.updateProperty(applicationUrl);
                        }

                    }
                }
            }
        }

        addShutdownMessageHook();

        executeStartScripts();
    }

    private static void startRouter() throws Exception {
        if (needToStartRouter()) {
            RouterConfig config = new RouterConfig();
            int routerPort = PAMRConfig.PA_NET_ROUTER_PORT.getValue();
            config.setPort(routerPort);
            config.setNbWorkerThreads(Runtime.getRuntime().availableProcessors());
            config.setReservedAgentConfigFile(new File(System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey()) +
                                                       PAMRRouterStarter.PATH_TO_ROUTER_CONFIG_FILE));
            Router.createAndStart(config);
            logger.info("The router created on " + ProActiveInet.getInstance().getHostname() + ":" + routerPort);
        }
    }

    private static boolean needToStartRouter() {
        return isPamrProtocolUsed() && isPamrHostLocalhost();
    }

    private static boolean isPamrHostLocalhost() {
        try {
            return isThisMyIpAddress(InetAddress.getByName(PAMRConfig.PA_NET_ROUTER_ADDRESS.getValue()));
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static boolean isThisMyIpAddress(InetAddress addr) {
        // Check if the address is a valid special local or loop back
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
            return true;

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            return false;
        }
    }

    private static boolean isPamrProtocolUsed() {
        return CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue().contains("pamr") ||
               CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.getValue().contains("pamr");
    }

    private static SchedulerAuthenticationInterface startScheduler(CommandLine commandLine, String rmUrl)
            throws Exception {
        String policyFullName = getPolicyFullName(commandLine);
        logger.info("Starting the scheduler...");
        SchedulerAuthenticationInterface sai = SchedulerFactory.startLocal(new URI(rmUrl), policyFullName);

        startDiscovery(commandLine, rmUrl);

        logger.info("The scheduler created on " + sai.getHostURL());
        return sai;
    }

    private static void startDiscovery(CommandLine commandLine, String urlToDiscover)
            throws ParseException, SocketException, UnknownHostException {
        if (!commandLine.hasOption("no-discovery")) {
            int discoveryPort = readIntOption(commandLine, "discovery-port", DISCOVERY_DEFAULT_PORT);
            discoveryService = new BroadcastDiscovery(discoveryPort, urlToDiscover);
            discoveryService.start();
        }
    }

    private static String connectToOrStartResourceManager(CommandLine commandLine, String rmUrl)
            throws ProActiveException, URISyntaxException, ParseException {
        if (rmUrl != null) {
            try {
                logger.info("Connecting to the resource manager on " + rmUrl);
                int rmConnectionTimeout = PASchedulerProperties.RESOURCE_MANAGER_CONNECTION_TIMEOUT.getValueAsInt();
                SchedulerFactory.waitAndJoinRM(new URI(rmUrl), rmConnectionTimeout);
            } catch (Exception e) {
                logger.error("ERROR while connecting to the RM on " + rmUrl + ", no RM found !");
                System.exit(2);
            }
        } else {
            rmUrl = getLocalAdress();
            URI uri = new URI(rmUrl);
            //trying to connect to a started local RM
            try {
                SchedulerFactory.tryJoinRM(uri);
                logger.info("Connected to the existing resource manager at " + uri);
            } catch (Exception e) {
                int defaultNodesNumber = PAResourceManagerProperties.RM_NB_LOCAL_NODES.getValueAsInt();

                // -1 means that the number of local nodes depends of the number of cores in the local machine
                if (defaultNodesNumber == -1) {
                    defaultNodesNumber = RMStarter.DEFAULT_NODES_NUMBER;
                }

                int numberLocalNodes = readIntOption(commandLine, "localNodes", defaultNodesNumber);
                int nodeTimeoutValue = readIntOption(commandLine, "timeout", DEFAULT_NODES_TIMEOUT);

                startResourceManager(numberLocalNodes, nodeTimeoutValue);
            }
        }
        return rmUrl;
    }

    private static void setCleanDatabaseProperties(CommandLine commandLine) {
        if (commandLine.hasOption("c")) {
            PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.updateProperty("true");
            PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB.updateProperty("true");
        }
    }

    private static void setCleanNodesourcesProperty(CommandLine commandLine) {
        if (commandLine.hasOption("clean-nodesources")) {
            PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB_NODESOURCES.updateProperty("true");
        }
    }

    private static String getRmUrl(CommandLine commandLine) {
        String rmUrl = null;

        if (commandLine.hasOption("u")) {
            rmUrl = commandLine.getOptionValue("u");
            logger.info("RM URL : " + rmUrl);
        }
        return rmUrl;
    }

    private static String getPolicyFullName(CommandLine commandLine) {
        String policyFullName = PASchedulerProperties.SCHEDULER_DEFAULT_POLICY.getValueAsString();

        if (commandLine.hasOption("p")) {
            policyFullName = commandLine.getOptionValue("p");
            logger.info("Used policy : " + policyFullName);
        }
        return policyFullName;
    }

    private static void addShutdownMessageHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Shutting down...");

                if (discoveryService != null) {
                    discoveryService.stop();
                }

                // WARNING: do not close embedded HSQLDB server in a shutdown hook.
                //
                // Multiple shutdown hooks are defined. Some are used for instance
                // by the Resource Manager to remove node sources before termination.
                // If the database is closed before completing the last operation, then
                // some errors will occur (see logs).
                //
                // Besides, checking the state of the Scheduler and RM by registering
                // an event listener or even by polling with periodic method calls will
                // not work since Scheduler and RM are Active Objects and these are
                // terminated by other shutdown hooks executed before the current one.
                //
                // The database connection should be closed by Hibernate when
                // the datasource is closed. If not, the HSQLDB server will handle
                // in the worst case the JVM shutdown as an accidental machine failure.
            }
        }));
    }

    private static void displayHelp(Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(120);
        hf.printHelp("proactive-server" + Tools.shellExtension(), options, true);
    }

    protected static Options getOptions() {
        Options options = new Options();

        Option help = new Option("h", "help", false, "to display this help");
        help.setArgName("help");
        help.setRequired(false);
        options.addOption(help);

        Option rmURL = new Option("u", "rmURL", true, "the resource manager URL (default: localhost)");
        rmURL.setArgName("rmURL");
        rmURL.setRequired(false);
        options.addOption(rmURL);

        Option policy = new Option("p",
                                   "policy",
                                   true,
                                   "the complete name of the scheduling policy to use (default: org.ow2.proactive.scheduler.policy.DefaultPolicy)");
        policy.setArgName("policy");
        policy.setRequired(false);
        options.addOption(policy);

        Option noDeploy = new Option("ln",
                                     "localNodes",
                                     true,
                                     "the number of local nodes to start (can be 0; default: " +
                                           RMStarter.DEFAULT_NODES_NUMBER + ")");
        noDeploy.setArgName("localNodes");
        noDeploy.setRequired(false);
        options.addOption(noDeploy);

        Option nodeTimeout = new Option("t",
                                        "timeout",
                                        true,
                                        "timeout used to start the nodes (only useful with local nodes; default: " +
                                              DEFAULT_NODES_TIMEOUT + "ms)");
        nodeTimeout.setArgName("timeout");
        nodeTimeout.setRequired(false);
        options.addOption(nodeTimeout);

        options.addOption(new Option("c",
                                     "clean",
                                     false,
                                     "clean scheduler and resource manager databases (default: false)"));

        options.addOption(Option.builder()
                                .longOpt("clean-nodesources")
                                .desc("drop all previously created nodesources from resource manager database (default: false)")
                                .build());

        options.addOption(Option.builder()
                                .longOpt("rm-only")
                                .desc("start only resource manager (implies --no-rest; default: false)")
                                .build());

        options.addOption(Option.builder()
                                .longOpt("no-rest")
                                .desc("do not deploy REST server and wars from dist/war (default: false)")
                                .build());

        options.addOption(Option.builder()
                                .longOpt("no-router")
                                .desc("do not deploy PAMR Router (default: false)")
                                .build());

        options.addOption(Option.builder()
                                .longOpt("no-discovery")
                                .desc("do not run discovery service for nodes (default: false)")
                                .build());
        options.addOption(Option.builder("dp")
                                .longOpt("discovery-port")
                                .desc("discovery service port for nodes (default: " + DISCOVERY_DEFAULT_PORT + ")")
                                .hasArg()
                                .argName("port")
                                .build());

        return options;
    }

    private static int readIntOption(CommandLine cmd, String optionName, int defaultValue) throws ParseException {
        int value = defaultValue;
        if (cmd.hasOption(optionName)) {
            try {
                value = Integer.parseInt(cmd.getOptionValue(optionName));
            } catch (Exception nfe) {
                throw new ParseException("Wrong value for " + optionName + " option: " + cmd.getOptionValue("t"));
            }
        }
        return value;
    }

    private static void startResourceManager(final int numberLocalNodes, final int nodeTimeoutValue) {
        final Thread rmStarter = new Thread() {
            public void run() {
                try {
                    //Starting a local RM using default deployment descriptor
                    RMFactory.setOsJavaProperty();
                    logger.info("Starting the resource manager...");
                    RMAuthentication rmAuth = RMFactory.startLocal();

                    if (numberLocalNodes > 0) {
                        addLocalNodes(rmAuth, numberLocalNodes, nodeTimeoutValue);
                    }

                    logger.info("The resource manager with " + numberLocalNodes + " local nodes created on " +
                                rmAuth.getHostURL());
                } catch (AlreadyBoundException abe) {
                    logger.error("The resource manager already exists on local host", abe);
                    System.exit(4);
                } catch (Exception aoce) {
                    logger.error("Unable to create local resource manager", aoce);
                    System.exit(5);
                }
            }
        };

        rmStarter.start();
    }

    private static void addLocalNodes(RMAuthentication rmAuth, int numberLocalNodes, int nodeTimeoutValue)
            throws LoginException, KeyException, IOException {
        //creating default node source
        ResourceManager rman = rmAuth.login(Credentials.getCredentials(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        //first im parameter is default rm url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        rman.createNodeSource(NodeSource.DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME,
                              LocalInfrastructure.class.getName(),
                              new Object[] { creds, numberLocalNodes, nodeTimeoutValue, "" },
                              RestartDownNodesPolicy.class.getName(),
                              new Object[] { "ALL", "ALL", "10000" });

        credentials = creds;
    }

    private static String getLocalAdress() throws ProActiveException {
        RemoteObjectFactory rof = AbstractRemoteObjectFactory.getDefaultRemoteObjectFactory();
        return rof.getBaseURI().toString();
    }

    private static void configureSchedulerAndRMAndPAHomes() {
        setPropIfNotAlreadySet(PASchedulerProperties.SCHEDULER_HOME.getKey(), findSchedulerHome());
        String schedHome = System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey());
        setPropIfNotAlreadySet(PAResourceManagerProperties.RM_HOME.getKey(), schedHome);
        setPropIfNotAlreadySet(CentralPAPropertyRepository.PA_HOME.getName(), schedHome);
        setPropIfNotAlreadySet(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(),
                               schedHome + "/config/network/server.ini");
    }

    protected static void configureSecurityManager() {
        SecurityPolicyLoader.configureSecurityManager(System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey()) +
                                                      "/config/security.java.policy-server",
                                                      PASchedulerProperties.POLICY_RELOAD_FREQUENCY_IN_SECONDS.getValueAsLong());

    }

    protected static void configureLogging() {
        String schedHome = System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey());
        String defaultLog4jConfig = schedHome + "/config/log/server.properties";
        if (setPropIfNotAlreadySet(CentralPAPropertyRepository.LOG4J.getName(), defaultLog4jConfig))
            PropertyConfigurator.configure(defaultLog4jConfig);
        setPropIfNotAlreadySet("java.util.logging.config.file", defaultLog4jConfig);
        setPropIfNotAlreadySet("derby.stream.error.file", schedHome + "/logs/Database.log");
    }

    protected static void configureDerby() {
        setPropIfNotAlreadySet("derby.locks.deadlockTimeout", "1");
    }

    protected static boolean setPropIfNotAlreadySet(String name, Object value) {
        boolean notSet = System.getProperty(name) == null;
        if (notSet)
            System.setProperty(name, value.toString());
        return notSet;
    }

    private static void executeStartScripts() throws InvalidScriptException, IOException {

        // Nothing to do if no script path is specified
        if (!PASchedulerProperties.SCHEDULER_STARTSCRIPTS_PATHS.isSet())
            return;

        // Retrieve the start scripts paths
        List<String> scriptsPaths = PASchedulerProperties.SCHEDULER_STARTSCRIPTS_PATHS.getValueAsList(";");

        // Scripts binding
        ScriptHandler scriptHandler = ScriptLoader.createLocalHandler();
        scriptHandler.addBindings(PASchedulerProperties.getPropertiesAsHashMap());
        scriptHandler.addBindings(PAResourceManagerProperties.getPropertiesAsHashMap());
        scriptHandler.addBindings(WebProperties.getPropertiesAsHashMap());

        // Execute all the listed scripts
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os, true);
        ScriptResult scriptResult;
        File scriptFile;

        for (String scriptPath : scriptsPaths) {

            scriptFile = new File(PASchedulerProperties.getAbsolutePath(scriptPath));
            if (scriptFile.exists()) {
                logger.info("Executing " + scriptPath);
                scriptResult = scriptHandler.handle(new SimpleScript(scriptFile, new String[0]), ps, ps);
                if (scriptResult.errorOccured()) {

                    // Close streams before throwing
                    os.close();
                    ps.close();
                    throw new InvalidScriptException("Failed to execute script: " +
                                                     scriptResult.getException().getMessage(),
                                                     scriptResult.getException());
                }
                logger.info(os.toString());

                os.reset();
            } else
                logger.warn("Start script " + scriptPath + " not found");
        }
        // Close streams
        os.close();
        ps.close();
    }
}
