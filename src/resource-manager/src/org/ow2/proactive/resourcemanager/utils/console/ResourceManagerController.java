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
package org.ow2.proactive.resourcemanager.utils.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.security.auth.login.LoginException;

import jline.ConsoleReader;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.utils.JVMPropertiesPreloader;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.utils.console.Console;
import org.ow2.proactive.utils.console.JlineConsole;
import org.ow2.proactive.utils.console.MBeanInfoViewer;
import org.ow2.proactive.utils.console.StdOutConsole;
import org.ow2.proactive.utils.console.VisualConsole;


/**
 * Class with a main provides a way to list nodes, nodes sources,
 * add/remove nodes and nodes sources and shutdown Resource Manager.
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 *
 */
public class ResourceManagerController {

    protected static final String control = "<ctl> ";
    protected static final String newline = System.getProperty("line.separator");
    protected static Logger logger = Logger.getLogger(ResourceManagerController.class);
    protected static ResourceManagerController shell;

    private String commandName = "rm-client";

    protected CommandLine cmd = null;
    protected String user = null;
    protected String pwd = null;
    protected Credentials credentials = null;

    protected RMAuthentication auth = null;
    protected ResourceManagerModel model;

    protected String jsEnv = null;

    /**
     * Start the RM controller
     *
     * @param args the arguments to be passed
     */
    public static void main(String[] args) {
        args = JVMPropertiesPreloader.overrideJVMProperties(args);
        shell = new ResourceManagerController(null);
        shell.load(args);
    }

    /**
     * Create a new instance of ResourceManagerController
     */
    protected ResourceManagerController() {
    }

    /**
     * Create a new instance of ResourceManagerController
     *
     * Convenience constructor to let the default one do nothing
     */
    protected ResourceManagerController(Object o) {
        model = ResourceManagerModel.getModel(true);
    }

    public void load(String[] args) {

        Options options = new Options();

        Option help = new Option("h", "help", false, "Display this help");
        help.setRequired(false);
        options.addOption(help);

        Option username = new Option("l", "login", true, "The username to join the Resource Manager");
        username.setArgName("login");
        username.setArgs(1);
        username.setRequired(false);
        options.addOption(username);

        Option rmURL = new Option("u", "rmURL", true, "The Resource manager URL");
        rmURL.setArgName("rmURL");
        rmURL.setArgs(1);
        rmURL.setRequired(false);
        options.addOption(rmURL);

        Option visual = new Option("g", "gui", false, "Start the console in a graphical view");
        rmURL.setRequired(false);
        options.addOption(visual);

        addCommandLineOptions(options);

        boolean displayHelp = false;

        try {
            String pwdMsg = null;

            Parser parser = new GnuParser();
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                displayHelp = true;
            } else {
                if (cmd.hasOption("environment")) {
                    model.setInitEnv(cmd.getOptionValue("environment"));
                }
                String url;
                if (cmd.hasOption("u")) {
                    url = cmd.getOptionValue("u");
                } else {
                    url = null;
                }

                try {
                    logger.debug("Detecting a network interface to bind the client runtime");
                    String networkInterface = RMConnection.getNetworkInterfaceFor(url);
                    logger.debug("The runtime will be bounded to the following network interface " +
                        networkInterface);
                    CentralPAPropertyRepository.PA_NET_INTERFACE.setValue(networkInterface);
                } catch (Exception e) {
                    logger.debug("Unable to detect the network interface", e);
                }

                logger.info("Connecting to the RM on " + (url == null ? "localhost" : url));
                auth = RMConnection.join(url);
                logger.info("\t-> Connection established on " + (url == null ? "localhost" : url));

                if (cmd.hasOption("l")) {
                    user = cmd.getOptionValue("l");
                }
                if (cmd.hasOption("credentials")) {
                    if (cmd.getOptionValues("credentials").length == 1) {
                        System.setProperty(Credentials.credentialsPathProperty, cmd
                                .getOptionValue("credentials"));
                    }
                    try {
                        this.credentials = Credentials.getCredentials();
                    } catch (KeyException e) {
                        logger.error("Could not retreive credentials... Try to adjust the System property: " +
                            Credentials.credentialsPathProperty);
                        throw e;
                    }
                } else {
                    ConsoleReader console = new ConsoleReader(System.in, new PrintWriter(System.out));
                    if (cmd.hasOption("l")) {
                        pwdMsg = user + "'s password: ";
                    } else {
                        user = console.readLine("login: ");
                        pwdMsg = "password: ";
                    }

                    //ask password to User
                    try {
                        console.setDefaultPrompt(pwdMsg);
                        pwd = console.readLine('*');
                    } catch (IOException ioe) {
                        logger.error("" + ioe);
                        logger.debug("", ioe);
                    }

                    PublicKey pubKey = null;
                    try {
                        // first attempt at getting the pubkey : ask the RM
                        RMAuthentication auth = RMConnection.join(url);
                        pubKey = auth.getPublicKey();
                        logger.info("Retrieved public key from Resource Manager at " +
                            (url == null ? "localhost" : url));
                    } catch (Exception e) {
                        try {
                            // second attempt : try default location
                            pubKey = Credentials.getPublicKey(Credentials.getPubKeyPath());
                            logger.info("Using public key at " + Credentials.getPubKeyPath());
                        } catch (Exception exc) {
                            logger
                                    .error("Could not find a public key. Contact the administrator of the Resource Manager.");
                            logger.debug("", exc);
                            System.exit(1);
                        }
                    }
                    try {
                        this.credentials = Credentials.createCredentials(new CredData(CredData
                                .parseLogin(user), CredData.parseDomain(user), pwd), pubKey);
                    } catch (KeyException e) {
                        logger.error("Could not create credentials... " + e);
                        throw e;
                    }
                }

                //connect to the scheduler
                connect();
                //connect JMX service
                connectJMXClient();
                //start the command line or the interactive mode
                start();

            }
        } catch (MissingArgumentException e) {
            logger.error(e.getLocalizedMessage());
            logger.debug("", e);
            displayHelp = true;
        } catch (MissingOptionException e) {
            logger.error("Missing option: " + e.getLocalizedMessage());
            logger.debug("", e);
            displayHelp = true;
        } catch (UnrecognizedOptionException e) {
            logger.error(e.getLocalizedMessage());
            logger.debug("", e);
            displayHelp = true;
        } catch (AlreadySelectedException e) {
            logger.error(e.getClass().getSimpleName() + " : " + e.getLocalizedMessage());
            logger.debug("", e);
            displayHelp = true;
        } catch (ParseException e) {
            logger.debug("", e);
            displayHelp = true;
        } catch (RMException e) {
            logger.error("Error at connection : " + e.getMessage() + newline + "Shutdown the controller." +
                newline);
            logger.debug("", e);
            System.exit(2);
        } catch (LoginException e) {
            logger.error(e.getMessage() + newline + "Shutdown the controller." + newline);
            logger.debug("", e);
            System.exit(3);
        } catch (Exception e) {
            logger.error("An error has occurred : " + e.getMessage() + newline + "Shutdown the controller." +
                newline, e);
            logger.debug("", e);
            System.exit(4);
        }

        if (displayHelp) {
            logger.info("");
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(160);
            String note = newline + "NOTE : if no " + control +
                " command is specified, the controller will start in interactive mode.";
            hf.printHelp(commandName + shellExtension(), "", options, note, true);
            System.exit(5);
        }

        // if execution reaches this point this means it must exit
        System.exit(0);
    }

    protected void connect() throws LoginException {
        model.connectRM(auth, credentials);
        String userStr = (user != null) ? "'" + user + "' " : "";
        logger.info("\t-> Client " + userStr + " successfully connected" + newline);
    }

    protected void connectJMXClient() {
        final MBeanInfoViewer viewer = new MBeanInfoViewer(auth, user, credentials);
        this.model.setJMXInfo(viewer);
    }

    private void start() throws Exception {
        //start one of the two command behavior
        if (startCommandLine(cmd)) {
            startCommandListener();
        }
    }

    protected OptionGroup addCommandLineOptions(Options options) {
        OptionGroup actionGroup = new OptionGroup();

        Option addNodesOpt = new Option("a", "addnodes", true, control + "Add nodes by their URLs");
        addNodesOpt.setArgName("node URLs");
        addNodesOpt.setRequired(false);
        addNodesOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(addNodesOpt);

        Option removeNodesOpt = new Option("d", "removenodes", true, control + "Remove nodes by their URLs");
        removeNodesOpt.setArgName("node URLs");
        removeNodesOpt.setRequired(false);
        removeNodesOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(removeNodesOpt);

        Option lockNodesOpt = new Option("locknodes", true, control + "Lock nodes by their URLs");
        lockNodesOpt.setArgName("node URLs");
        lockNodesOpt.setRequired(false);
        lockNodesOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(lockNodesOpt);

        Option unlockNodesOpt = new Option("unlocknodes", true, control + "Unlock nodes by their URLs");
        unlockNodesOpt.setArgName("node URLs");
        unlockNodesOpt.setRequired(false);
        unlockNodesOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(unlockNodesOpt);

        Option createNSOpt = new Option("cn", "createns", true, control + "Create new node sources");
        createNSOpt.setArgName("names");
        createNSOpt.setRequired(false);
        createNSOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(createNSOpt);

        Option infrastuctureOpt = new Option("i", "infrastructure", true,
            "Specify an infrastructure when node source is created");
        infrastuctureOpt.setArgName("params");
        infrastuctureOpt.setRequired(false);
        infrastuctureOpt.setOptionalArg(true);
        infrastuctureOpt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(infrastuctureOpt);

        Option policyOpt = new Option("p", "policy", true, "Specify a policy when node source is created");
        policyOpt.setArgName("params");
        policyOpt.setOptionalArg(true);
        policyOpt.setRequired(false);
        policyOpt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(policyOpt);

        Option listNodesOpt = new Option("ln", "listnodes", true, control +
            "List nodes handled by Resource Manager. Display is : NODESOURCE HOSTNAME STATE NODE_URL");
        listNodesOpt.setRequired(false);
        listNodesOpt.setOptionalArg(true);
        listNodesOpt.setArgName("nodeSourceName");
        actionGroup.addOption(listNodesOpt);

        Option listNSOpt = new Option("lns", "listns", false, control +
            "List node sources on Resource Manager. Display is : NODESOURCE TYPE");
        listNSOpt.setRequired(false);
        actionGroup.addOption(listNSOpt);

        Option topologyOpt = new Option("t", "topology", false, control + "Displays nodes topology.");
        topologyOpt.setRequired(false);
        actionGroup.addOption(topologyOpt);

        Option removeNSOpt = new Option("r", "removens", true, control + "Remove given node sources");
        removeNSOpt.setArgName("names");
        removeNSOpt.setRequired(false);
        removeNSOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(removeNSOpt);

        Option shutdownOpt = new Option("s", "shutdown", false, control + "Shutdown Resource Manager");
        shutdownOpt.setRequired(false);
        actionGroup.addOption(shutdownOpt);

        Option acopt = new Option("stats", "statistics", false, control +
            "Display some statistics about the Resource Manager");
        acopt.setRequired(false);
        acopt.setArgs(0);
        actionGroup.addOption(acopt);

        acopt = new Option("ma", "myaccount", false, control + "Display current user account informations");
        acopt.setRequired(false);
        acopt.setArgs(0);
        actionGroup.addOption(acopt);

        acopt = new Option("ua", "useraccount", false, control + "Display account information by username");
        acopt.setRequired(false);
        acopt.setArgs(1);
        acopt.setArgName("username");
        actionGroup.addOption(acopt);

        acopt = new Option("ni", "nodeinfo", true, control + "Display node information");
        acopt.setRequired(false);
        acopt.setArgs(1);
        acopt.setArgName("nodeURL");
        actionGroup.addOption(acopt);

        acopt = new Option("rc", "reloadconfig", false, control +
            "Reloads the resource manager permission policy and log4j config");
        acopt.setRequired(false);
        acopt.setArgs(0);
        actionGroup.addOption(acopt);

        options.addOptionGroup(actionGroup);

        Option nodeSourceNameOpt = new Option("ns", "nodesource", true, control +
            "Specify an existing node source name for adding nodes");
        nodeSourceNameOpt.setArgName("nodes URLs");
        nodeSourceNameOpt.setRequired(false);
        nodeSourceNameOpt.setArgs(1);
        options.addOption(nodeSourceNameOpt);

        Option preeemptiveRemovalOpt = new Option("f", "force", false, control +
            "Do not wait for busy nodes to be freed before " +
            "nodes removal, node source removal and shutdown actions (-d, -r and -s)");
        preeemptiveRemovalOpt.setRequired(false);
        options.addOption(preeemptiveRemovalOpt);

        Option script = new Option("sf", "script", true, control +
            "Execute the given javascript file with optional arguments.");
        script.setArgName("filePath arg1=val1 arg2=val2 ...");
        script.setArgs(Option.UNLIMITED_VALUES);
        script.setOptionalArg(true);
        script.setRequired(false);
        options.addOption(script);

        script = new Option("env", "environment", true,
            "Execute the given script and go into interactive mode");
        script.setArgName("filePath");
        script.setRequired(false);
        script.setOptionalArg(true);
        options.addOption(script);

        Option opt = new Option("c", "credentials", true, "Path to the credentials (" +
            Credentials.getCredentialsPath() + ").");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        return actionGroup;
    }

    private void startCommandListener() throws Exception {
        Console console;
        if (cmd.hasOption("g")) {
            console = new VisualConsole();
        } else {
            console = new JlineConsole();
        }
        model.connectConsole(console);
        model.startModel();
    }

    protected boolean startCommandLine(CommandLine cmd) throws Exception {
        /* start stdConsole */
        model.connectConsole(new StdOutConsole());
        if (cmd.hasOption("addnodes")) {
            String[] nodesURls = cmd.getOptionValues("addnodes");
            if (cmd.hasOption("ns")) {
                String nsName = cmd.getOptionValue("ns");
                for (String nUrl : nodesURls) {
                    model.addnode_(nUrl, nsName);
                }
            } else {
                for (String nUrl : nodesURls) {
                    model.addnode_(nUrl, null);
                }
            }
        } else if (cmd.hasOption("removenodes")) {
            String[] nodesURls = cmd.getOptionValues("removenodes");
            boolean preempt = cmd.hasOption("f");
            for (String nUrl : nodesURls) {
                model.removenode_(nUrl, preempt);
            }
        } else if (cmd.hasOption("locknodes")) {
            String[] nodesURls = cmd.getOptionValues("locknodes");
            List<String> list = Arrays.asList(nodesURls);
            model.locknodes_(new HashSet<String>(list));
        } else if (cmd.hasOption("unlocknodes")) {
            String[] nodesURls = cmd.getOptionValues("unlocknodes");
            List<String> list = Arrays.asList(nodesURls);
            model.unlocknodes_(new HashSet<String>(list));
        } else if (cmd.hasOption("createns")) {

            String[] nsNames = cmd.getOptionValues("createns");

            boolean hasInfrastructure = cmd.hasOption("infrastructure");
            String[] imParams = null;
            if (hasInfrastructure) {
                imParams = cmd.getOptionValues("infrastructure");
                if (imParams == null) {
                    // list available infrastructures
                    model.listInfrastructures_();
                    return false;
                }
            }
            boolean hasPolicy = cmd.hasOption("policy");
            String[] policyParams = null;
            if (hasPolicy) {
                policyParams = cmd.getOptionValues("policy");
                if (policyParams == null) {
                    // list available policies
                    model.listPolicies_();
                    return false;
                }
            }

            for (String nsName : nsNames) {
                // if imParams is null or policyParams is null use default
                if (!model.createns_(nsName, imParams, policyParams)) {
                    break;
                }
            }
        } else if (cmd.hasOption("listnodes")) {
            model.listnodes_(cmd.getOptionValue("listnodes"));
        } else if (cmd.hasOption("topology")) {
            model.topology_();
        } else if (cmd.hasOption("listns")) {
            model.listns_();
        } else if (cmd.hasOption("removens")) {
            String[] nsNames = cmd.getOptionValues("removens");
            boolean preempt = cmd.hasOption("f");
            for (String nsName : nsNames) {
                model.removens_(nsName, preempt);
            }
        } else if (cmd.hasOption("ni")) {
            String[] nodesURls = cmd.getOptionValues("ni");
            for (String nUrl : nodesURls) {
                model.nodeinfo_(nUrl);
            }
        } else if (cmd.hasOption("shutdown")) {
            model.shutdown_(cmd.hasOption("f"));
        } else if (cmd.hasOption("stats")) {
            model.showRuntimeData_();
        } else if (cmd.hasOption("ma")) {
            model.showMyAccount_();
        } else if (cmd.hasOption("ua")) {
            model.showAccount_(cmd.getOptionValue("ua"));
        } else if (cmd.hasOption("rc")) {
            model.refreshConfiguration_();
        } else if (cmd.hasOption("script")) {
            model.execWithParam_(cmd.getOptionValues("script"));
        } else {
            return true;
        }
        return false;
    }

    /**
     * Return the extension of shell script depending the current OS
     *
     * @return the extension of shell script depending the current OS
     */
    public static String shellExtension() {
        if (System.getProperty("os.name").contains("Windows")) {
            return ".bat";
        } else {
            return "";
        }
    }

}
