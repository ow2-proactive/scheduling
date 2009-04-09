package org.ow2.proactive.resourcemanager.utils.adminconsole;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.client.ClientConnector;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.passwordhandler.PasswordField;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.console.Console;
import org.ow2.proactive.utils.console.MBeanInfoViewer;
import org.ow2.proactive.utils.console.SimpleConsole;


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
    private final String JS_INIT_FILE = "AdminActions.js";

    public static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMLAUNCHER);
    protected static final String control = "<ctl> ";

    private static final String ADDNODE_CMD = "addnode(nodeURL, nsName)";
    private static final String REMOVENODE_CMD = "removenode(nodeURL,preempt)";
    private static final String GCMDEPLOY_CMD = "gcmdeploy(gcmdFile,nsName)";
    private static final String CREATENS_CMD = "createns(nsName)";
    private static final String REMOVENS_CMD = "removens(nsName,preempt)";
    private static final String LISTNODES_CMD = "listnodes()";
    private static final String LISTNS_CMD = "listns()";
    private static final String SHUTDOWN_CMD = "shutdown(preempt)";
    private static final String EXIT_CMD = "exit()";
    private static final String JMXINFO_CMD = "jmxinfo()";
    private static final String EXEC_CMD = "exec(commandFilePath)";

    private String commandName = "adminRM";

    protected RMAdmin rm;
    protected boolean initialized = false;
    protected boolean terminated = false;
    protected boolean intercativeMode = false;
    protected ScriptEngine engine;
    protected Console console = new SimpleConsole();

    protected MBeanInfoViewer mbeanInfoViewer;

    protected RMAuthentication auth = null;
    protected CommandLine cmd = null;
    protected String user = null;
    protected String pwd = null;

    protected static AdminController shell;

    /**
     * @param args
     */
    public static void main(String[] args) {
        shell = new AdminController();
        shell.load(args);
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

                logger.info("\nConnecting admin to the RM");
                if (cmd.hasOption("l")) {
                    user = cmd.getOptionValue("l");
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

                //connect to the scheduler
                connect();
                //connect JMX service
                //connectJMXClient(URIBuilder.getHostNameFromUrl(url));
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
            logger.error("Error at connection : " + e.getMessage() + "\nShutdown the controller.\n");
            System.exit(1);
        } catch (LoginException e) {
            logger.error(e.getMessage() + "\nShutdown the controller.\n");
            System.exit(1);
        } catch (Exception e) {
            logger.error("An error has occurred : " + e.getMessage() + "\nShutdown the controller.\n", e);
            System.exit(1);
        }

        if (displayHelp) {
            logger.info("");
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(160);
            String note = "\nNOTE : if no " + control +
                " command is specified, the controller will start in interactive mode.";
            hf.printHelp(commandName + shellExtension(), "", options, note, true);
            System.exit(2);
        }

        // if execution reaches this point this means it must exit
        System.exit(0);
    }

    protected void connect() throws LoginException {
        rm = auth.logAsAdmin(user, pwd);
        logger.info("\t-> Admin '" + user + "' successfully connected\n");
    }

    private void connectJMXClient(String url) {
        if (!url.startsWith("//")) {
            url = "//" + url;
        }
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        //connect the JMX client
        ClientConnector connectorClient = new ClientConnector(url, "ServerMonitoring");
        try {
            connectorClient.connect();
            ProActiveConnection connection = connectorClient.getConnection();
            ObjectName mbeanName = new ObjectName("RMFrontend:name=RMBean");
            MBeanInfo info = connection.getMBeanInfo(mbeanName);
            mbeanInfoViewer = new MBeanInfoViewer(connection, mbeanName, info);
        } catch (Exception e) {
            logger.error("Scheduler MBean not found using : RMFrontend:name=RMBean");
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

        Option createNSOpt = new Option("c", "createns", true, control + "Create new node sources");
        createNSOpt.setArgName("names");
        createNSOpt.setRequired(false);
        createNSOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(createNSOpt);

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

        //        Option jmx = new Option("jmxinfo", false, control +
        //            "Display some statistics provided by the Scheduler MBean");
        //        jmx.setRequired(false);
        //        jmx.setArgs(0);
        //        actionGroup.addOption(jmx);

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

        return actionGroup;
    }

    private void startCommandListener() throws Exception {
        initialize();
        console.start(" > ");
        console.printf("Type command here (type '?' or help() to see the list of commands)\n");
        String stmt;
        while (!terminated) {
            stmt = console.readStatement();
            if (stmt.equals("?")) {
                console.printf("\n" + helpScreen());
            } else {
                eval(stmt);
                console.printf("");
            }
        }
        console.stop();
    }

    protected boolean startCommandLine(CommandLine cmd) {
        intercativeMode = false;
        if (cmd.hasOption("addnodes")) {
            String[] nodesURls = cmd.getOptionValues("addnodes");
            if (cmd.hasOption("ns")) {
                String nsName = cmd.getOptionValue("ns");
                for (String nUrl : nodesURls) {
                    addnode(nUrl, nsName);
                }
            } else {
                for (String nUrl : nodesURls) {
                    addnode(nUrl, null);
                }
            }
        } else if (cmd.hasOption("gcmdeploy")) {
            String[] gcmdTab = cmd.getOptionValues("gcmdeploy");

            for (String gcmdf : gcmdTab) {
                File gcmdFile = new File(gcmdf);
                if (!(gcmdFile.exists() && gcmdFile.isFile() && gcmdFile.canRead())) {
                    error("Cannot read GCMDeployment descriptor : " + gcmdf);
                }
            }
            if (cmd.hasOption("ns")) {
                String nsName = cmd.getOptionValue("ns");
                for (String desc : gcmdTab) {
                    gcmdeploy(desc, nsName);
                }
            } else {
                for (String desc : gcmdTab) {
                    gcmdeploy(desc, null);
                }
            }
        } else if (cmd.hasOption("removenodes")) {
            String[] nodesURls = cmd.getOptionValues("removenodes");
            boolean preempt = cmd.hasOption("f");
            for (String nUrl : nodesURls) {
                removenode(nUrl, preempt);
            }
        } else if (cmd.hasOption("createns")) {
            String[] nsNames = cmd.getOptionValues("createns");
            for (String nsName : nsNames) {
                createns(nsName);
            }
        } else if (cmd.hasOption("listnodes")) {
            listnodes();
        } else if (cmd.hasOption("listns")) {
            listns();
        } else if (cmd.hasOption("removens")) {
            String[] nsNames = cmd.getOptionValues("removens");
            boolean preempt = cmd.hasOption("f");
            for (String nsName : nsNames) {
                removens(nsName, preempt);
            }
        } else if (cmd.hasOption("shutdown")) {
            shutdown(cmd.hasOption("f"));
        }
        //        else if (cmd.hasOption("jmxinfo")) {
        //            JMXinfo();
        //        } 
        else {
            intercativeMode = true;
            return intercativeMode;
        }
        return false;
    }

    //***************** COMMAND LISTENER *******************

    protected void handleExceptionDisplay(String msg, Throwable t) {
        if (intercativeMode) {
            console.handleExceptionDisplay(msg, t);
        } else {
            System.err.printf(msg);
            t.printStackTrace();
        }
    }

    protected void printf(String format, Object... args) {
        if (intercativeMode) {
            console.printf(format, args);
        } else {
            System.out.printf(format, args);
        }
    }

    protected void error(String format, Object... args) {
        if (intercativeMode) {
            console.error(format, args);
        } else {
            System.err.printf(format, args);
        }
    }

    public static void help() {
        shell.help_();
    }

    private void help_() {
        printf("\n" + helpScreen());
    }

    public static void shutdown(boolean preempt) {
        shell.shutdown_(preempt);
    }

    private void shutdown_(boolean preempt) {
        try {
            rm.shutdown(preempt);
            printf("Shutdown request sent to Resource Manager, controller will shutdown !");
            terminated = true;
        } catch (Exception e) {
            handleExceptionDisplay("Error while shutting down the RM", e);
        }
    }

    public static void removens(String nodeSourceName, boolean preempt) {
        shell.removens_(nodeSourceName, preempt);
    }

    private void removens_(String nodeSourceName, boolean preempt) {
        try {
            rm.removeSource(nodeSourceName, preempt);
            printf("Node source '" + nodeSourceName + "' removal request sent to Resource Manager");
        } catch (Exception e) {
            handleExceptionDisplay("Error while removing node source '" + nodeSourceName, e);
        }
    }

    public static void listns() {
        shell.listns_();
    }

    private void listns_() {
        List<RMNodeSourceEvent> list = rm.getNodeSourcesList();
        for (RMNodeSourceEvent evt : list) {
            printf(evt.getSourceName() + "\t" + evt.getSourceDescription());
        }
    }

    public static void listnodes() {
        shell.listnodes_();
    }

    private void listnodes_() {
        List<RMNodeEvent> list = rm.getNodesList();
        if (list.size() == 0) {
            printf("No nodes handled by Resource Manager");
        } else {
            for (RMNodeEvent evt : list) {
                String state = null;
                switch (evt.getState()) {
                    case DOWN:
                        state = "DOWN";
                        break;
                    case FREE:
                        state = "FREE";
                        break;
                    case BUSY:
                        state = "BUSY";
                        break;
                    case TO_BE_RELEASED:
                        state = "TO_BE_RELEASED";
                }
                printf(evt.getNodeSource() + "\t" + evt.getHostName() + "\t" + state + "\t" +
                    evt.getNodeUrl());
            }
        }
    }

    public static void createns(String nodeSourceName) {
        shell.createns_(nodeSourceName);
    }

    private void createns_(String nodeSourceName) {
        try {
            rm.createNodesource(nodeSourceName, GCMInfrastructure.class.getName(), null, StaticPolicy.class
                    .getName(), null);
            printf("Node source '" + nodeSourceName + "' creation request sent to Resource Manager");
        } catch (Exception e) {
            handleExceptionDisplay("Error while creating node source '" + nodeSourceName, e);
        }
    }

    public static void removenode(String nodeURL, boolean preempt) {
        shell.removenode_(nodeURL, preempt);
    }

    private void removenode_(String nodeURL, boolean preempt) {
        rm.removeNode(nodeURL, preempt);
        printf("Nodes '" + nodeURL + "' removal request sent to Resource Manager");
    }

    public static void gcmdeploy(String fileName, String nodeSourceName) {
        shell.gcmdeploy_(fileName, nodeSourceName);
    }

    private void gcmdeploy_(String fileName, String nodeSourceName) {
        try {
            File gcmDeployFile = new File(fileName);
            if (nodeSourceName != null) {
                rm.addNodes(nodeSourceName, new Object[] { FileToBytesConverter
                        .convertFileToByteArray(gcmDeployFile) });
            } else {
                rm.addNodes(NodeSource.DEFAULT_NAME, new Object[] { FileToBytesConverter
                        .convertFileToByteArray(gcmDeployFile) });
            }
            printf("GCM deployment '" + fileName + "' request sent to Resource Manager");
        } catch (Exception e) {
            handleExceptionDisplay("Error while load GCMD file '" + fileName, e);
        }
    }

    public static void addnode(String nodeName, String nodeSourceName) {
        shell.addnode_(nodeName, nodeSourceName);
    }

    private void addnode_(String nodeName, String nodeSourceName) {
        try {
            if (nodeSourceName != null) {
                rm.addNode(nodeName, nodeSourceName);
            } else {
                rm.addNode(nodeName);
            }
            printf("Adding node '" + nodeName + "' request sent to Resource Manager");
        } catch (Exception e) {
            handleExceptionDisplay("Error while adding node '" + nodeName + "'", e);
        }
    }

    public static void JMXinfo() {
        shell.JMXinfo_();
    }

    private void JMXinfo_() {
        try {
            printf(mbeanInfoViewer.getInfo());
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public static void exec(String commandFilePath) {
        shell.exec_(commandFilePath);
    }

    private void exec_(String commandFilePath) {
        try {
            File f = new File(commandFilePath.trim());
            BufferedReader br = new BufferedReader(new FileReader(f));
            eval(readFileContent(br));
            br.close();
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
    }

    public static void exit() {
        shell.exit_();
    }

    private void exit_() {
        console.printf("Exiting controller.");
        terminated = true;
    }

    //***************** OTHER *******************

    protected void initialize() throws IOException {
        if (!initialized) {
            ScriptEngineManager manager = new ScriptEngineManager();
            // Engine selection
            engine = manager.getEngineByName("rhino");
            initialized = true;
            //read and launch Action.js
            BufferedReader br = new BufferedReader(new InputStreamReader(AdminController.class
                    .getResourceAsStream(JS_INIT_FILE)));
            eval(readFileContent(br));
        }
    }

    protected void eval(String cmd) {
        try {
            if (!initialized) {
                initialize();
            }
            //Evaluate the command
            engine.eval(cmd);
        } catch (ScriptException e) {
            console.error("*SYNTAX ERROR* - " + format(e.getMessage()));
        } catch (Exception e) {
            handleExceptionDisplay("Error while evaluating command", e);
        }
    }

    private static String format(String msg) {
        msg = msg.replaceFirst("[^:]+:", "");
        return msg.replaceFirst("[(]<.*", "").trim();
    }

    protected static String readFileContent(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String tmp;
        while ((tmp = reader.readLine()) != null) {
            sb.append(tmp);
        }
        return sb.toString();
    }

    //***************** HELP SCREEN *******************

    protected String helpScreen() {
        StringBuilder out = new StringBuilder("Resource Manager controller commands are :\n\n");
        out.append(String.format(
                " %1$-28s\t Add node to the given node source (parameters is a string representing the node URL to add AND"
                    + " a string representing the node source in which to add the node)\n", ADDNODE_CMD));
        out.append(String.format(
                " %1$-28s\t Remove the given node (parameter is a string representing the node URL,"
                    + " node is removed immediately if second parameter is true)\n", REMOVENODE_CMD));
        out.append(String
                .format(
                        " %1$-28s\t Add node(s) to the given node source (parameter is a string representing the a GCMD file AND"
                            + " a string representing the node source in which to add the node(s) )\n",
                        GCMDEPLOY_CMD));
        out
                .append(String
                        .format(
                                " %1$-28s\t Create a new node source (parameter is a string representing the node source name to create)\n",
                                CREATENS_CMD));
        out.append(String.format(
                " %1$-28s\t Remove the given node source (parameter is a string representing the node source name to remove,"
                    + " nodeSource is removed immediately if second parameter is true)\n", REMOVENS_CMD));
        out.append(String.format(" %1$-28s\t List every handled nodes\n", LISTNODES_CMD));
        out.append(String.format(" %1$-28s\t List every handled node sources\n", LISTNS_CMD));
        out.append(String.format(
                " %1$-28s\t Shutdown the Resource Manager (RM shutdown immediately if parameter is true)\n",
                SHUTDOWN_CMD));
        //        out.append(String.format(" %1$-28s\t Display some statistics provided by the Scheduler MBean\n",
        //                JMXINFO_CMD));
        out
                .append(String
                        .format(
                                " %1$-28s\t Execute the content of the given script file (parameter is a string representing a command-file path)\n",
                                EXEC_CMD));
        out.append(String.format(" %1$-28s\t Exits RM controller\n", EXIT_CMD));

        return out.toString();
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
            return ".sh";
        }
    }

    /**
     * Set the commandName value to the given commandName value
     *
     * @param commandName the commandName to set
     */
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
}
