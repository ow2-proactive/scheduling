/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.resourcemanager.utils.adminconsole;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.console.Command;
import org.ow2.proactive.utils.console.ConsoleModel;
import org.ow2.proactive.utils.console.MBeanInfoViewer;


/**
 * AdminRMModel is the class that drives the RM console in the admin view.
 * To use this class, get the model, connect a RM and a console, and just start this model.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class AdminRMModel extends ConsoleModel {

    private static final String JS_INIT_FILE = "AdminActions.js";
    protected static final int cmdHelpMaxCharLength = 28;
    protected RMAdmin rm;
    private ArrayList<Command> commands;

    private static int logsNbLines = 20;
    private static String logsDirectory = System.getProperty("pa.rm.home") + File.separator + ".logs";

    private static final String rmLogFile = "RM.log";

    protected MBeanInfoViewer jmxInfoViewer = null;

    /**
     * Get this model. Also specify if the exit command should do something or not
     *
     * @param allowExitCommand true if the exit command is part of the commands, false if exit command does not exist.
     * @return the current model associated to this class.
     */
    public static AdminRMModel getModel(boolean allowExitCommand) {
        if (model == null) {
            model = new AdminRMModel(allowExitCommand);
        }
        return (AdminRMModel) model;
    }

    private static AdminRMModel getModel() {
        return (AdminRMModel) model;
    }

    protected AdminRMModel(boolean allowExitCommand) {
        this.allowExitCommand = allowExitCommand;
        commands = new ArrayList<Command>();
        commands
                .add(new Command(
                    "exMode(display,onDemand)",
                    "Change the way exceptions are displayed (if display is true, stacks are displayed - if onDemand is true, prompt before displaying stacks)"));
        commands.add(new Command("addnode(nodeURL, nsName)",
            "Add node to the given node source (parameters is a string representing the node URL to add AND"
                + " a string representing the node source in which to add the node)"));
        commands.add(new Command("removenode(nodeURL,preempt)",
            "Remove the given node (parameter is a string representing the node URL,"
                + " node is removed immediately if second parameter is true)"));
        commands.add(new Command("gcmdeploy(gcmdFile,nsName)",
            "Add node(s) to the given node source (parameter is a string representing the a GCMD file AND"
                + " a string representing the node source in which to add the node(s) )"));
        commands.add(new Command("createns(nsName)",
            "Create a new node source (parameter is a string representing the node source name to create)"));
        commands.add(new Command("removens(nsName,preempt)",
            "Remove the given node source (parameter is a string representing the node source name to remove,"
                + " nodeSource is removed immediately if second parameter is true)"));
        commands.add(new Command("listnodes()", "List every handled nodes"));
        commands.add(new Command("listns()", "List every handled node sources"));
        commands.add(new Command("shutdown(preempt)",
            "Shutdown the Resource Manager (RM shutdown immediately if parameter is true)"));
        commands.add(new Command("jmxinfo()", "Display some statistics provided by the Scheduler MBean"));
        commands
                .add(new Command("exec(scriptFilePath)",
                    "Execute the content of the given script file (parameter is a string representing a script-file path)"));
        commands.add(new Command("setLogsDir(logsDir)",
            "Set the directory where the log are located, (default is RM_HOME/.logs"));
        commands.add(new Command("viewlogs(nbLines)",
            "View the last nbLines lines of the logs file, (default nbLines is 20)"));
        if (allowExitCommand) {
            commands.add(new Command("exit()", "Exits RM controller"));
        }

    }

    /**
     * @see org.ow2.proactive.utils.console.ConsoleModel#checkIsReady()
     */
    @Override
    protected void checkIsReady() {
        super.checkIsReady();
        if (rm == null) {
            throw new RuntimeException("RM is not set, it must be set before starting the model");
        }
    }

    /**
     * @see org.ow2.proactive.utils.console.ConsoleModel#initialize()
     */
    @Override
    protected void initialize() throws IOException {
        super.initialize();
        //read and launch Action.js
        BufferedReader br = new BufferedReader(new InputStreamReader(AdminController.class
                .getResourceAsStream(JS_INIT_FILE)));
        eval(readFileContent(br));
    }

    /**
     * @see org.ow2.proactive.utils.console.ConsoleModel#startModel()
     */
    public void startModel() throws Exception {
        checkIsReady();
        console.start(" > ");
        console.print("Type command here (type '?' or help() to see the list of commands)\n");
        initialize();
        String stmt;
        while (!terminated) {
            stmt = console.readStatement();
            if ("?".equals(stmt)) {
                console.print("\n" + helpScreen());
            } else {
                eval(stmt);
                console.print("");
            }
        }
        console.stop();
    }

    //***************** COMMAND LISTENER *******************

    public static void setExceptionMode(boolean displayStack, boolean displayOnDemand) {
        getModel().checkIsReady();
        getModel().setExceptionMode_(displayStack, displayOnDemand);
    }

    public static void help() {
        getModel().checkIsReady();
        getModel().help_();
    }

    public static void shutdown(boolean preempt) {
        getModel().checkIsReady();
        getModel().shutdown_(preempt);
    }

    private void shutdown_(boolean preempt) {
        try {
            rm.shutdown(preempt);
            print("Shutdown request sent to Resource Manager, controller will shutdown !");
            terminated = true;
        } catch (Exception e) {
            handleExceptionDisplay("Error while shutting down the RM", e);
        }
    }

    public static void removens(String nodeSourceName, boolean preempt) {
        getModel().checkIsReady();
        getModel().removens_(nodeSourceName, preempt);
    }

    private void removens_(String nodeSourceName, boolean preempt) {
        try {
            rm.removeSource(nodeSourceName, preempt);
            print("Node source '" + nodeSourceName + "' removal request sent to Resource Manager");
        } catch (Exception e) {
            handleExceptionDisplay("Error while removing node source '" + nodeSourceName, e);
        }
    }

    public static void listns() {
        getModel().checkIsReady();
        getModel().listns_();
    }

    private void listns_() {
        List<RMNodeSourceEvent> list = rm.getNodeSourcesList();
        int[] nameSize = new int[] { 11, 9 };
        for (RMNodeSourceEvent evt : list) {
            if (evt.getSourceName().length() > nameSize[0]) {
                nameSize[0] = evt.getSourceName().length();
            }
            if (evt.getSourceDescription().length() > nameSize[1]) {
                nameSize[1] = evt.getSourceDescription().length();
            }
        }
        nameSize[0] += 2;
        nameSize[1] += 2;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(" %1$-" + nameSize[0] + "s", "SOURCE NAME"));
        sb.append(String.format(" %1$-" + nameSize[1] + "s", "DESCRIPTION"));
        print(sb.toString());
        for (RMNodeSourceEvent evt : list) {
            sb = new StringBuilder();
            sb.append(String.format(" %1$-" + nameSize[0] + "s", evt.getSourceName()));
            sb.append(String.format(" %1$-" + nameSize[1] + "s", evt.getSourceDescription()));
            print(sb.toString());
        }
    }

    public static void listnodes() {
        getModel().checkIsReady();
        getModel().listnodes_();
    }

    private void listnodes_() {
        List<RMNodeEvent> list = rm.getNodesList();
        if (list.size() == 0) {
            print("No nodes handled by Resource Manager");
        } else {
            int[] nameSize = new int[] { 11, 8, 5, 8, 3 };
            for (RMNodeEvent evt : list) {
                if (evt.getNodeSource().length() > nameSize[0]) {
                    nameSize[0] = evt.getNodeSource().length();
                }
                if (evt.getHostName().length() > nameSize[1]) {
                    nameSize[1] = evt.getHostName().length();
                }
                if (evt.getNodeState().toString().length() > nameSize[2]) {
                    nameSize[2] = evt.getNodeState().toString().length();
                }
                if (evt.getStateChangeTime().toString().length() > nameSize[3]) {
                    nameSize[3] = evt.getStateChangeTime().toString().length();
                }
                if (evt.getNodeUrl().length() > nameSize[4]) {
                    nameSize[4] = evt.getNodeUrl().length();
                }
            }
            nameSize[0] += 2;
            nameSize[1] += 2;
            nameSize[2] += 2;
            nameSize[3] += 2;
            nameSize[4] += 2;
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(" %1$-" + nameSize[0] + "s", "SOURCE NAME"));
            sb.append(String.format(" %1$-" + nameSize[1] + "s", "HOSTNAME"));
            sb.append(String.format(" %1$-" + nameSize[2] + "s", "STATE"));
            sb.append(String.format(" %1$-" + nameSize[3] + "s", "SINCE"));
            sb.append(String.format(" %1$-" + nameSize[4] + "s", "URL"));
            print(sb.toString());
            for (RMNodeEvent evt : list) {
                sb = new StringBuilder();
                sb.append(String.format(" %1$-" + nameSize[0] + "s", evt.getNodeSource()));
                sb.append(String.format(" %1$-" + nameSize[1] + "s", evt.getHostName()));
                sb.append(String.format(" %1$-" + nameSize[2] + "s", evt.getNodeState()));
                sb.append(String.format(" %1$-" + nameSize[3] + "s", evt.getStateChangeTime()));
                sb.append(String.format(" %1$-" + nameSize[4] + "s", evt.getNodeUrl()));
                print(sb.toString());
            }
        }
    }

    public static void createns(String nodeSourceName) {
        getModel().checkIsReady();
        getModel().createns_(nodeSourceName);
    }

    private void createns_(String nodeSourceName) {
        try {
            rm.createNodesource(nodeSourceName, GCMInfrastructure.class.getName(), null, StaticPolicy.class
                    .getName(), null);
            print("Node source '" + nodeSourceName + "' creation request sent to Resource Manager");
        } catch (Exception e) {
            handleExceptionDisplay("Error while creating node source '" + nodeSourceName, e);
        }
    }

    public static void removenode(String nodeURL, boolean preempt) {
        getModel().checkIsReady();
        getModel().removenode_(nodeURL, preempt);
    }

    private void removenode_(String nodeURL, boolean preempt) {
        rm.removeNode(nodeURL, preempt);
        print("Nodes '" + nodeURL + "' removal request sent to Resource Manager");
    }

    public static void gcmdeploy(String fileName, String nodeSourceName) {
        getModel().checkIsReady();
        getModel().gcmdeploy_(fileName, nodeSourceName);
    }

    private void gcmdeploy_(String fileName, String nodeSourceName) {
        try {
            File gcmDeployFile = new File(fileName);
            BooleanWrapper result;
            if (nodeSourceName != null) {
                result = rm.addNodes(nodeSourceName, new Object[] { FileToBytesConverter
                        .convertFileToByteArray(gcmDeployFile) });
            } else {
                result = rm.addNodes(NodeSource.DEFAULT_NAME, new Object[] { FileToBytesConverter
                        .convertFileToByteArray(gcmDeployFile) });
            }

            if (result.booleanValue()) {
                print("GCM deployment '" + fileName + "' request sent to Resource Manager");
            }
        } catch (IOException e) {
            handleExceptionDisplay("Error while load GCMD file '" + fileName, e);
        } catch (AddingNodesException e) {
            handleExceptionDisplay(e.getMessage(), e);
        }
    }

    public static void addnode(String nodeName, String nodeSourceName) {
        getModel().checkIsReady();
        getModel().addnode_(nodeName, nodeSourceName);
    }

    private void addnode_(String nodeName, String nodeSourceName) {
        try {
            BooleanWrapper result;
            if (nodeSourceName != null) {
                result = rm.addNode(nodeName, nodeSourceName);
            } else {
                result = rm.addNode(nodeName);
            }

            if (result.booleanValue()) {
                print("Adding node '" + nodeName + "' request sent to Resource Manager");
            }
        } catch (AddingNodesException e) {
            handleExceptionDisplay("Error while adding node '" + nodeName + "'", e);
        }
    }

    public static void JMXinfo() {
        getModel().checkIsReady();
        getModel().JMXinfo_();
    }

    private void JMXinfo_() {
        try {
            print(jmxInfoViewer.getInfo());
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public static void exec(String commandFilePath) {
        getModel().checkIsReady();
        getModel().exec_(commandFilePath);
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

    public static void setLogsDir(String logsDir) {
        if (logsDir == null || "".equals(logsDir)) {
            getModel().error("Given logs directory is null or empty !");
            return;
        }
        File dir = new File(logsDir);
        if (!dir.exists()) {
            getModel().error("Given logs directory does not exist !");
            return;
        }
        if (!dir.isDirectory()) {
            getModel().error("Given logsDir is not a directory !");
            return;
        }
        dir = new File(logsDir + File.separator + rmLogFile);
        if (!dir.exists()) {
            getModel().error("Given logs directory does not contains Scheduler logs files !");
            return;
        }
        getModel().print("Logs Directory set to '" + logsDir + "' !");
        logsDirectory = logsDir;
    }

    public static void viewlogs(String nbLines) {
        if (!"".equals(nbLines)) {
            try {
                logsNbLines = Integer.parseInt(nbLines);
            } catch (NumberFormatException nfe) {
                //logsNbLines not set
            }
        }
        getModel().print(readLastNLines(rmLogFile));
    }

    /**
     * Return the logsNbLines last lines of the given file.
     *
     * @param fileName the file to be displayed
     * @return the N last lines of the given file
     */
    private static String readLastNLines(String fileName) {
        StringBuilder toret = new StringBuilder();
        File f = new File(logsDirectory + File.separator + fileName);
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            long cursor = raf.length() - 2;
            int nbLines = logsNbLines;
            byte b;
            raf.seek(cursor);
            while (nbLines > 0) {
                if ((b = raf.readByte()) == '\n') {
                    nbLines--;
                }
                cursor--;
                raf.seek(cursor);
                if (nbLines > 0) {
                    toret.insert(0, (char) b);
                }
            }
        } catch (Exception e) {
        }
        return toret.toString();
    }

    public static void exit() {
        getModel().checkIsReady();
        getModel().exit_();
    }

    private void exit_() {
        if (allowExitCommand) {
            console.print("Exiting controller.");
            try {
                rm.disconnect();
            } catch (Exception e) {
            }
            terminated = true;
        } else {
            console.print("Exit command has been disabled !");
        }
    }

    public static RMAdmin getAdminRM() {
        getModel().checkIsReady();
        return getModel().getAdminRM_();
    }

    private RMAdmin getAdminRM_() {
        return rm;
    }

    //***************** HELP SCREEN *******************

    protected String helpScreen() {
        StringBuilder out = new StringBuilder("Resource Manager controller commands are :" + newline +
            newline);

        out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s\t %2$s" + newline + newline, commands
                .get(0).getName(), commands.get(0).getDescription()));

        for (int i = 1; i < commands.size(); i++) {
            out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s\t %2$s" + newline, commands.get(i)
                    .getName(), commands.get(i).getDescription()));
        }

        return out.toString();
    }

    //**************** GETTER / SETTER ******************

    /**
     * Get the Resource manager
     *
     * @return the Resource manager
     */
    public RMAdmin getScheduler() {
        return rm;
    }

    /**
     * Connect the Resource manager value to the given rm value
     *
     * @param rm the Resource manager to connect
     */
    public void connectRM(RMAdmin rm) {
        if (rm == null) {
            throw new NullPointerException("Given Resource Manager is null");
        }
        this.rm = rm;
    }

    /**
     * Set the JMX information : it is not a mandatory option, if set, it will show informations, if not nothing will be displayed.
     * 
     * @param info the jmx information about the current connection
     */
    public void setJMXInfo(MBeanInfoViewer info) {
        jmxInfoViewer = info;
    }

}
