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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils.adminconsole;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyException;
import java.security.PublicKey;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXProviderException;
import javax.security.auth.login.LoginException;

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
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.passwordhandler.PasswordField;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.connector.PAAuthenticationConnectorClient;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.utils.console.Console;
import org.ow2.proactive.utils.console.MBeanInfoViewer;
import org.ow2.proactive.utils.console.SimpleConsole;
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
public class AdminController {

    private static final String RM_DEFAULT_URL = getHostURL("//localhost/");

    protected static final String control = "<ctl> ";
    protected static final String newline = System.getProperty("line.separator");
    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMLAUNCHER);
    protected static AdminController shell;

    private String commandName = "rm-admin";

    protected CommandLine cmd = null;
    protected String user = null;
    protected String pwd = null;
    protected Credentials credentials = null;

    protected RMAuthentication auth = null;
    protected AdminRMModel model;

    /**
     * Start the RM controller
     *
     * @param args the arguments to be passed
     */
    public static void main(String[] args) {
        shell = new AdminController(null);
        shell.load(args);
    }

    /**
     * Create a new instance of AdminController
     */
    protected AdminController() {
    }

    /**
     * Create a new instance of AdminController
     *
     * Convenience constructor to let the default one do nothing
     */
    protected AdminController(Object o) {
        model = AdminRMModel.getModel(true);
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

        Option rmURL = new Option("u", "rmURL", true, "The Resource manager URL (default " + RM_DEFAULT_URL +
            ")");
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
                String url;
                if (cmd.hasOption("u")) {
                    url = cmd.getOptionValue("u");
                } else {
                    url = RM_DEFAULT_URL;
                }

                logger.info("Trying to connect RM on " + url);
                auth = RMConnection.join(url);
                logger.info("\t-> Connection established on " + url);

                logger.info(newline + "Connecting admin to the RM");

                if (cmd.hasOption("l")) {
                    user = cmd.getOptionValue("l");
                }
                if (cmd.hasOption("uc")) {
                    if (cmd.hasOption("c")) {
                        System.setProperty(Credentials.credentialsPathProperty, cmd.getOptionValue("c"));
                    }
                    try {
                        this.credentials = Credentials.getCredentials();
                    } catch (KeyException e) {
                        logger.error("Could not retreive credentials... Try to adjust the System property: " +
                            Credentials.credentialsPathProperty);
                        throw e;
                    }
                } else {
                    if (cmd.hasOption("l")) {
                        pwdMsg = user + "'s password: ";
                    } else {
                        System.out.print("login: ");
                        BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
                        user = buf.readLine();
                        pwdMsg = "password: ";
                    }

                    //ask password to User
                    char password[] = null;
                    try {
                        password = PasswordField.getPassword(System.in, pwdMsg);
                        if (password == null) {
                            pwd = "";
                        } else {
                            pwd = String.valueOf(password);
                        }
                    } catch (IOException ioe) {
                        logger.error("", ioe);
                    }

                    PublicKey pubKey = null;
                    try {
                        // first attempt at getting the pubkey : ask the RM
                        RMAuthentication auth = RMConnection.join(url);
                        pubKey = auth.getPublicKey();
                        System.out.println("Retrieved public key from Resource Manager at " + url);
                    } catch (Exception e) {
                        try {
                            // second attempt : try default location
                            pubKey = Credentials.getPublicKey(Credentials.getPubKeyPath());
                            System.out.println("Using public key at " + Credentials.getPubKeyPath());
                        } catch (Exception exc) {
                            System.out
                                    .println("Could not find a public key. Contact the administrator of the Resource Manager.");
                            exc.printStackTrace();
                            System.exit(0);
                        }
                    }
                    try {
                        this.credentials = Credentials.createCredentials(user, pwd, pubKey);
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
            displayHelp = true;
        } catch (MissingOptionException e) {
            logger.error("Missing option: " + e.getLocalizedMessage());
            displayHelp = true;
        } catch (UnrecognizedOptionException e) {
            logger.error(e.getLocalizedMessage());
            displayHelp = true;
        } catch (AlreadySelectedException e) {
            logger.error(e.getClass().getSimpleName() + " : " + e.getLocalizedMessage());
            displayHelp = true;
        } catch (ParseException e) {
            displayHelp = true;
        } catch (RMException e) {
            logger.error("Error at connection : " + e.getMessage() + newline + "Shutdown the controller." +
                newline);
            System.exit(1);
        } catch (LoginException e) {
            logger.error(e.getMessage() + newline + "Shutdown the controller." + newline);
            System.exit(1);
        } catch (Exception e) {
            logger.error("An error has occurred : " + e.getMessage() + newline + "Shutdown the controller." +
                newline, e);
            System.exit(1);
        }

        if (displayHelp) {
            logger.info("");
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(160);
            String note = newline + "NOTE : if no " + control +
                " command is specified, the controller will start in interactive mode.";
            hf.printHelp(commandName + shellExtension(), "", options, note, true);
            System.exit(2);
        }

        // if execution reaches this point this means it must exit
        System.exit(0);
    }

    protected void connect() throws LoginException {
        RMAdmin rm = auth.logAsAdmin(credentials);
        model.connectRM(rm);
        String userStr = (user != null) ? "'" + user + "' " : "";
        logger.info("\t-> Admin " + userStr + " successfully connected" + newline);
    }

    protected void connectJMXClient() throws JMXProviderException {
        try {
            PAAuthenticationConnectorClient cli = new PAAuthenticationConnectorClient(auth
                    .getJMXConnectorURL() +
                "_admin");
            cli.connect(credentials, user);
            MBeanServerConnection conn = cli.getConnection();
            ObjectName on = new ObjectName("RMFrontend:name=RMBean_admin");
            model.setJMXInfo(new MBeanInfoViewer(conn, on));
        } catch (Exception e) {
            logger.error("Error while connecting JMX : ", e);
        }
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

        Option gcmdOpt = new Option("gcmd", "gcmdeploy", true, control +
            "Add nodes by GCM deployment descriptor files");
        gcmdOpt.setArgName("GCMD files");
        gcmdOpt.setRequired(false);
        gcmdOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(gcmdOpt);

        Option removeNodesOpt = new Option("d", "removenodes", true, control + "Remove nodes by their URLs");
        removeNodesOpt.setArgName("node URLs");
        removeNodesOpt.setRequired(false);
        removeNodesOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(removeNodesOpt);

        Option createNSOpt = new Option("cn", "createns", true, control + "Create new node sources");
        createNSOpt.setArgName("names");
        createNSOpt.setRequired(false);
        createNSOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(createNSOpt);

        Option infrastuctureOpt = new Option("infrastructure", true,
            "Specify an infrastructure when node source is created");
        infrastuctureOpt.setArgName("params");
        infrastuctureOpt.setRequired(false);
        infrastuctureOpt.setOptionalArg(true);
        infrastuctureOpt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(infrastuctureOpt);

        Option policyOpt = new Option("policy", true, "Specify a policy when node source is created");
        policyOpt.setArgName("params");
        policyOpt.setOptionalArg(true);
        policyOpt.setRequired(false);
        policyOpt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(policyOpt);

        Option listNodesOpt = new Option("ln", "listnodes", false, control +
            "List nodes handled by Resource Manager. Display is : NODESOURCE HOSTNAME STATE NODE_URL");
        listNodesOpt.setRequired(false);
        actionGroup.addOption(listNodesOpt);

        Option listNSOpt = new Option("lns", "listns", false, control +
            "List node sources on Resource Manager. Display is : NODESOURCE TYPE");
        listNSOpt.setRequired(false);
        actionGroup.addOption(listNSOpt);

        Option removeNSOpt = new Option("r", "removens", true, control + "Remove given node sources");
        removeNSOpt.setArgName("names");
        removeNSOpt.setRequired(false);
        removeNSOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(removeNSOpt);

        Option shutdownOpt = new Option("s", "shutdown", false, control + "Shutdown Resource Manager");
        shutdownOpt.setRequired(false);
        actionGroup.addOption(shutdownOpt);

        Option jmx = new Option("jmxinfo", false, control +
            "Display some statistics provided by the Scheduler MBean");
        jmx.setRequired(false);
        jmx.setArgs(0);
        actionGroup.addOption(jmx);

        options.addOptionGroup(actionGroup);

        Option nodeSourceNameOpt = new Option("ns", "nodesource", true, control +
            "Specify an existing node source name for " +
            "adding nodes on and deploying GCMD actions (-a and -gcmd)");
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
            "Execute the given script (javascript is supported)");
        script.setArgName("filePath");
        script.setArgs(1);
        script.setRequired(false);
        options.addOption(script);

        Option useCreds = new Option("uc", "use-creds", false, "Use credentials retreived from disk");
        useCreds.setRequired(false);
        useCreds.setArgs(0);
        options.addOption(useCreds);

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
            console = new SimpleConsole();
        }
        model.connectConsole(console);
        model.startModel();
    }

    protected boolean startCommandLine(CommandLine cmd) {
        model.setDisplayOnStdStream(true);
        if (cmd.hasOption("addnodes")) {
            String[] nodesURls = cmd.getOptionValues("addnodes");
            if (cmd.hasOption("ns")) {
                String nsName = cmd.getOptionValue("ns");
                for (String nUrl : nodesURls) {
                    AdminRMModel.addnode(nUrl, nsName);
                }
            } else {
                for (String nUrl : nodesURls) {
                    AdminRMModel.addnode(nUrl, null);
                }
            }
        } else if (cmd.hasOption("gcmdeploy")) {
            String[] gcmdTab = cmd.getOptionValues("gcmdeploy");

            for (String gcmdf : gcmdTab) {
                File gcmdFile = new File(gcmdf);
                if (!(gcmdFile.exists() && gcmdFile.isFile() && gcmdFile.canRead())) {
                    model.error("Cannot read GCMDeployment descriptor : " + gcmdf);
                }
            }
            if (cmd.hasOption("ns")) {
                String nsName = cmd.getOptionValue("ns");
                for (String desc : gcmdTab) {
                    AdminRMModel.gcmdeploy(desc, nsName);
                }
            } else {
                for (String desc : gcmdTab) {
                    AdminRMModel.gcmdeploy(desc, null);
                }
            }
        } else if (cmd.hasOption("removenodes")) {
            String[] nodesURls = cmd.getOptionValues("removenodes");
            boolean preempt = cmd.hasOption("f");
            for (String nUrl : nodesURls) {
                AdminRMModel.removenode(nUrl, preempt);
            }
        } else if (cmd.hasOption("createns")) {

            String[] nsNames = cmd.getOptionValues("createns");

            boolean hasInfrastructure = cmd.hasOption("infrastructure");
            String[] imParams = null;
            if (hasInfrastructure) {
                imParams = cmd.getOptionValues("infrastructure");
                if (imParams == null) {
                    // list available infrastructures
                    AdminRMModel.listInfrastructures();
                    return false;
                }
            }
            boolean hasPolicy = cmd.hasOption("policy");
            String[] policyParams = null;
            if (hasPolicy) {
                policyParams = cmd.getOptionValues("policy");
                if (policyParams == null) {
                    // list available policies
                    AdminRMModel.listPolicies();
                    return false;
                }
            }

            for (String nsName : nsNames) {
                // if imParams is null or policyParams is null use default
                if (!AdminRMModel.createns(nsName, imParams, policyParams)) {
                    break;
                }
            }
        } else if (cmd.hasOption("listnodes")) {
            AdminRMModel.listnodes();
        } else if (cmd.hasOption("listns")) {
            AdminRMModel.listns();
        } else if (cmd.hasOption("removens")) {
            String[] nsNames = cmd.getOptionValues("removens");
            boolean preempt = cmd.hasOption("f");
            for (String nsName : nsNames) {
                AdminRMModel.removens(nsName, preempt);
            }
        } else if (cmd.hasOption("shutdown")) {
            AdminRMModel.shutdown(cmd.hasOption("f"));
        } else if (cmd.hasOption("jmxinfo")) {
            AdminRMModel.JMXinfo();
        } else if (cmd.hasOption("script")) {
            AdminRMModel.exec(cmd.getOptionValue("script"));
        } else {
            model.setDisplayOnStdStream(false);
            return true;
        }
        return false;
    }

    /**
     * Normalize the given URL into an URL that only contains protocol://host:port/
     *
     * @param url the url to transform
     * @return an URL that only contains protocol://host:port/
     */
    public static String getHostURL(String url) {
        URI uri = URI.create(url);
        String scheme = (uri.getScheme() == null) ? "rmi" : uri.getScheme();
        String host = (uri.getHost() == null) ? "localhost" : uri.getHost();
        int port = (uri.getPort() == -1) ? PAProperties.PA_RMI_PORT.getValueAsInt() : uri.getPort();
        return scheme + "://" + host + ":" + port + "/";
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
