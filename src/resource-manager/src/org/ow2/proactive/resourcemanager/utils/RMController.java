package org.ow2.proactive.resourcemanager.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.passwordhandler.PasswordField;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;


/**
 * Class with a main provides a way to list nodes, nodes sources,
 * add/remove nodes and nodes sources and shutdown Resource Manager.
 * 
 * 
 * @author ProActive team
 *
 */
public class RMController {

    /**
     * Log4j logger name.
     */
    public static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMLAUNCHER);

    /**
     * @param args
     */
    public static void main(String[] args) {

        Options options = new Options();

        Option urlOpt = new Option("u", "url", true, "Resource manager url");
        urlOpt.setArgName("rmURL");
        urlOpt.setRequired(false);
        urlOpt.setArgs(1);
        options.addOption(urlOpt);

        Option username = new Option("l", "login", true, "the login");
        username.setArgName("login");
        username.setRequired(false);
        options.addOption(username);

        OptionGroup actionGroup = new OptionGroup();
        actionGroup.setRequired(true);

        Option help = new Option("h", "help", false, "to display this help");
        help.setArgName("help");
        help.setRequired(false);
        actionGroup.addOption(help);

        Option addNodesOpt = new Option("a", "addNodes", true, "add nodes by their URLs");
        addNodesOpt.setArgName("nodes urls");
        addNodesOpt.setRequired(true);
        addNodesOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(addNodesOpt);

        Option gcmdOpt = new Option("gcmd", "gcmdeploy", true, "add nodes by GCM deployment descriptor files");
        gcmdOpt.setArgName("GCMD files");
        gcmdOpt.setRequired(true);
        gcmdOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(gcmdOpt);

        Option removeNodesOpt = new Option("d", "removeNodes", true, "remove nodes by their URLs");
        removeNodesOpt.setArgName("nodes URLs");
        removeNodesOpt.setRequired(true);
        removeNodesOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(removeNodesOpt);

        Option createNSOpt = new Option("c", "createNS", true, "create new node sources");
        createNSOpt.setArgName("name");
        createNSOpt.setRequired(true);
        createNSOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(createNSOpt);

        Option listNodesOpt = new Option("ln", "listNodes", false,
            "list nodes handled by Resource Manager. Display is :\nNODESOURCE HOSTNAME STATE NODE_URL");
        listNodesOpt.setRequired(true);
        actionGroup.addOption(listNodesOpt);

        Option listNSOpt = new Option("lns", "listNodesSource", false,
            "list node sources on Resource Manager. Display is :\nNODESOURCE TYPE");
        listNSOpt.setRequired(true);
        actionGroup.addOption(listNSOpt);

        Option removeNSOpt = new Option("r", "removeNS", true, "remove node sources");
        removeNSOpt.setArgName("names");
        removeNSOpt.setRequired(true);
        removeNSOpt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(removeNSOpt);

        Option shutdownOpt = new Option("s", "shutdown", false, "shutdown Resource Manager");
        shutdownOpt.setRequired(true);
        actionGroup.addOption(shutdownOpt);

        options.addOptionGroup(actionGroup);

        Option nodeSourceNameOpt = new Option("ns", "nodesource", true,
            "specify an existing node source name for "
                + "adding nodes and deploying GCMD actions (-a and -gcmd)");
        nodeSourceNameOpt.setArgName("nodes URLs");
        nodeSourceNameOpt.setRequired(false);
        nodeSourceNameOpt.setArgs(1);
        options.addOption(nodeSourceNameOpt);

        Option preeemptiveRemovalOpt = new Option("f", "force", false,
            "don't wait tasks end on busy nodes for "
                + "nodes removal, node source removal and shutdown actions (-d, -r and -s)");
        preeemptiveRemovalOpt.setRequired(false);
        options.addOption(preeemptiveRemovalOpt);

        Parser parser = new GnuParser();
        CommandLine cmd;
        String rmUrl = null;
        String rmUser = null;
        String rmPassword = null;

        try {

            RMAuthentication auth = null;

            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                System.out
                        .println("\n Add and removes nodes, create and removes nodes source, shutdown a Resource Manager.\n");
                new HelpFormatter().printHelp("adminRM", options, true);
                System.exit(0);
            } else if (!cmd.hasOption("u")) {
                auth = RMConnection.join(null);
            } else {
                rmUrl = cmd.getOptionValue("u") + "/";
                auth = RMConnection.join(rmUrl + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION);
            }

            String pwdMsg = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            if (cmd.hasOption("l")) {
                rmUser = cmd.getOptionValue("l");
                pwdMsg = rmUser + "'s password: ";
            } else {
                System.out.print("login: ");
                rmUser = reader.readLine();
                pwdMsg = "password: ";
            }

            //ask password to User         
            char password[] = null;
            try {
                password = PasswordField.getPassword(System.in, pwdMsg);
                rmPassword = (password == null) ? "" : String.valueOf(password);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            RMAdmin admin = auth.logAsAdmin(rmUser, rmPassword);

            if (cmd.hasOption("a")) {
                String[] nodesURls = cmd.getOptionValues("a");

                if (cmd.hasOption("ns")) {
                    String nsName = cmd.getOptionValue("ns");
                    for (String nUrl : nodesURls) {
                        admin.addNode(nUrl, nsName);
                    }
                } else {
                    for (String nUrl : nodesURls) {
                        admin.addNode(nUrl);
                    }
                }
                logger.info("Addning nodes request sent to Resource Manager " + rmUrl);

            } else if (cmd.hasOption("gcmd")) {
                String[] gcmdTab = cmd.getOptionValues("gcmd");

                for (String gcmdf : gcmdTab) {
                    File gcmdFile = new File(gcmdf);
                    if (!(gcmdFile.exists() && gcmdFile.isFile() && gcmdFile.canRead())) {
                        logger.error("cannot read GCMDeployment descriptor : " + gcmdf);
                        System.exit(0);
                    }
                }

                if (cmd.hasOption("ns")) {
                    String nsName = cmd.getOptionValue("ns");
                    for (String desc : gcmdTab) {
                        File gcmDeployFile = new File(desc);
                        admin.addNodes(FileToBytesConverter.convertFileToByteArray(gcmDeployFile), nsName);
                    }
                } else {
                    for (String desc : gcmdTab) {
                        File gcmDeployFile = new File(desc);
                        admin.addNodes(FileToBytesConverter.convertFileToByteArray(gcmDeployFile));
                    }
                }
                logger.info("GCM deployment request sent to Resource Manager " + rmUrl);
            } else if (cmd.hasOption("c")) {

                String[] nsNames = cmd.getOptionValues("c");
                ;
                for (String nsName : nsNames) {
                    admin.createGCMNodesource(null, nsName);
                }
                logger.info("Node source creation request sent to Resource Manager " + rmUrl);

            } else if (cmd.hasOption("d")) {

                String[] nodesURls = cmd.getOptionValues("d");
                boolean preempt = cmd.hasOption("f");
                for (String nUrl : nodesURls) {
                    admin.removeNode(nUrl, preempt);
                }
                logger.info("Nodes removal request sent to Resource Manager " + rmUrl);

            } else if (cmd.hasOption("r")) {

                String[] nsNames = cmd.getOptionValues("r");
                boolean preempt = cmd.hasOption("f");
                for (String nsName : nsNames) {
                    admin.removeSource(nsName, preempt);
                }
                logger.info("Node sources removal request sent to Resource Manager " + rmUrl);
            } else if (cmd.hasOption("s")) {

                boolean preempt = cmd.hasOption("f");
                admin.shutdown(preempt);
                logger.info("shutdown request sent to Resource Manager " + rmUrl);
            } else if (cmd.hasOption("ln")) {

                printNodesList(admin.getNodesList());

            } else if (cmd.hasOption("lns")) {

                printNodeSourcesList(admin.getNodeSourcesList());

            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        System.exit(0);
    }

    /**
     * Print on STDOUT a list of nodes 
     * @param list a list of RMNodeEvent objects representing a set of nodes to display.
     */
    public static void printNodesList(List<RMNodeEvent> list) {

        if (list.size() == 0) {
            System.out.println("No nodes handled by Resource Manager");
        } else {
            System.out.println("\n");
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
                System.out.println(evt.getNodeSource() + "\t" + evt.getHostName() + "\t" + state + "\t" +
                    evt.getNodeUrl());
            }
        }

    }

    /**
     * Print on STDOUT a list of node sources 
     * @param list a list of RMNodesourceEvent representing a set of nodes sources to display.
     */
    public static void printNodeSourcesList(List<RMNodeSourceEvent> list) {
        System.out.println("\n");
        for (RMNodeSourceEvent evt : list) {
            System.out.println(evt.getSourceName() + "\t" + evt.getSourceType());
        }
    }
}
