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
 * $ACTIVEEON_INITIAL_DEV$
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

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive.utils.Tools;
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
        commands
                .add(new Command(
                    "createns(nsName,infr,pol)",
                    "Create a new node source with specified name, infrastructure and policy (e.g. createns('myname', ['infrastrucure', 'param1', ...], ['policy', 'param1', ...]))"));
        commands.add(new Command("removens(nsName,preempt)",
            "Remove the given node source (parameter is a string representing the node source name to remove,"
                + " nodeSource is removed immediately if second parameter is true)"));
        commands.add(new Command("listnodes()", "List every handled nodes"));
        commands.add(new Command("listns()", "List every handled node sources"));
        commands.add(new Command("listInfrastructures()", "List supported infrastructures"));
        commands.add(new Command("listPolicies()", "List available node sources policies"));
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
        List<String> list;
        List<RMNodeSourceEvent> listns = rm.getNodeSourcesList();
        ObjectArrayFormatter oaf = new ObjectArrayFormatter();
        oaf.setMaxColumnLength(70);
        //space between column
        oaf.setSpace(3);
        //title line
        list = new ArrayList<String>();
        list.add("SOURCE NAME");
        list.add("DESCRIPTION");
        oaf.setTitle(list);
        //separator
        oaf.addEmptyLine();
        for (RMNodeSourceEvent evt : listns) {
            list = new ArrayList<String>();
            list.add(evt.getSourceName());
            list.add(evt.getSourceDescription());
            oaf.addLine(list);
        }
        print(Tools.getStringAsArray(oaf));
    }

    public static void listnodes() {
        getModel().checkIsReady();
        getModel().listnodes_();
    }

    private void listnodes_() {
        List<RMNodeEvent> listne = rm.getNodesList();
        if (listne.size() == 0) {
            print("No nodes handled by Resource Manager");
        } else {
            List<String> list;
            ObjectArrayFormatter oaf = new ObjectArrayFormatter();
            oaf.setMaxColumnLength(50);
            //space between column
            oaf.setSpace(2);
            //title line
            list = new ArrayList<String>();
            list.add("SOURCE NAME");
            list.add("HOSTNAME");
            list.add("STATE");
            list.add("SINCE");
            list.add("URL");
            oaf.setTitle(list);
            //separator
            oaf.addEmptyLine();
            for (RMNodeEvent evt : listne) {
                list = new ArrayList<String>();
                list.add(evt.getNodeSource());
                list.add(evt.getHostName());
                list.add(evt.getNodeState().toString());
                list.add(evt.getStateChangeTime().toString());
                list.add(evt.getNodeUrl());
                oaf.addLine(list);
            }
            print(Tools.getStringAsArray(oaf));
        }
    }

    public static boolean createns(String nodeSourceName, String[] imParams, String[] policyParams) {
        getModel().checkIsReady();
        return getModel().createns_(nodeSourceName, imParams, policyParams);
    }

    private Object[] packPluginParameters(String[] params) throws RMException, ClassNotFoundException {
        // extracting plugin name from the first param
        if (params != null && params.length > 0) {
            String name = params[0];
            // shifting array of input params (plugin does not imply having its name in the first parameter)
            Object[] shiftedParams = new Object[params.length - 1];
            System.arraycopy(params, 1, shiftedParams, 0, params.length - 1);

            //TODO if we want to run rm-admin remotely this part has to be rewritten
            Class<?> cls = Class.forName(name);
            PluginDescriptor pd = new PluginDescriptor(cls);
            // packing parameters (reading files on client side, processing login information, etc)
            try {
                return pd.packParameters(shiftedParams);
            } catch (RMException ex) {
                getModel().print(pd.toString());
                throw ex;
            }
        }

        return null;
    }

    private boolean createns_(String nodeSourceName, String[] imInputParams, String[] policyInputParams) {

        try {
            String imName = DefaultInfrastructureManager.class.getName();
            if (imInputParams != null && imInputParams.length > 1) {
                imName = imInputParams[0];
            }

            String policyName = StaticPolicy.class.getName();
            if (policyInputParams != null && policyInputParams.length > 1) {
                policyName = policyInputParams[0];
            }

            Object[] imPackedParams = packPluginParameters(imInputParams);
            Object[] policyPackedParams = packPluginParameters(policyInputParams);

            rm.createNodesource(nodeSourceName, imName, imPackedParams, policyName, policyPackedParams);
            print("Node source '" + nodeSourceName + "' creation request sent to Resource Manager");
        } catch (Exception e) {
            handleExceptionDisplay("Error while creating node source '" + nodeSourceName, e);
            return false;
        }
        return true;
    }

    public static void removenode(String nodeURL, boolean preempt) {
        getModel().checkIsReady();
        getModel().removenode_(nodeURL, preempt);
    }

    private void removenode_(String nodeURL, boolean preempt) {
        rm.removeNode(nodeURL, preempt);
        print("Nodes '" + nodeURL + "' removal request sent to Resource Manager");
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

    public static void listInfrastructures() {
        getModel().checkIsReady();
        getModel().print("Available node source infrastructures:");
        for (PluginDescriptor plugin : getModel().rm.getSupportedNodeSourceInfrastructures()) {
            getModel().print(plugin.toString());
        }
    }

    public static void listPolicies() {
        getModel().checkIsReady();
        getModel().print("Available node source policies:");
        for (PluginDescriptor plugin : getModel().rm.getSupportedNodeSourcePolicies()) {
            getModel().print(plugin.toString());
        }
    }
}
