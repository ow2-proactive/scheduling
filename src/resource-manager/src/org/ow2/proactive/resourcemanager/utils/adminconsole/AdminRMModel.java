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
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.console.Command;
import org.ow2.proactive.utils.console.ConsoleModel;


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

    public static AdminRMModel getModel() {
        if (model == null) {
            model = new AdminRMModel();
        }
        return (AdminRMModel) model;
    }

    protected AdminRMModel() {
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
        //commands.add(new Command("jmxinfo()","Display some statistics provided by the Scheduler MBean"));
        commands
                .add(new Command("exec(commandFilePath)",
                    "Execute the content of the given script file (parameter is a string representing a command-file path)"));
        commands.add(new Command("exit()", "Exits RM controller"));

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
        for (RMNodeSourceEvent evt : list) {
            print(evt.getSourceName() + "\t" + evt.getSourceDescription());
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
            for (RMNodeEvent evt : list) {
                String state = null;
                switch (evt.getNodeState()) {
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
                print(evt.getNodeSource() + "\t" + evt.getHostName() + "\t" + state + "\t" + evt.getNodeUrl());
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
            if (nodeSourceName != null) {
                rm.addNodes(nodeSourceName, new Object[] { FileToBytesConverter
                        .convertFileToByteArray(gcmDeployFile) });
            } else {
                rm.addNodes(NodeSource.DEFAULT_NAME, new Object[] { FileToBytesConverter
                        .convertFileToByteArray(gcmDeployFile) });
            }
            print("GCM deployment '" + fileName + "' request sent to Resource Manager");
        } catch (Exception e) {
            handleExceptionDisplay("Error while load GCMD file '" + fileName, e);
        }
    }

    public static void addnode(String nodeName, String nodeSourceName) {
        getModel().checkIsReady();
        getModel().addnode_(nodeName, nodeSourceName);
    }

    private void addnode_(String nodeName, String nodeSourceName) {
        try {
            if (nodeSourceName != null) {
                rm.addNode(nodeName, nodeSourceName);
            } else {
                rm.addNode(nodeName);
            }
            print("Adding node '" + nodeName + "' request sent to Resource Manager");
        } catch (Exception e) {
            handleExceptionDisplay("Error while adding node '" + nodeName + "'", e);
        }
    }

    //    public static void JMXinfo() {
    //    	getModel().checkIsReady();
    //    	getModel().JMXinfo_();
    //    }
    //
    //    private void JMXinfo_() {
    //        try {
    //            printf(mbeanInfoViewer.getInfo());
    //        } catch (Exception e) {
    //            handleExceptionDisplay("Error while retrieving JMX informations", e);
    //        }
    //    }

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

    public static void exit() {
        getModel().checkIsReady();
        getModel().exit_();
    }

    private void exit_() {
        console.print("Exiting controller.");
        try {
            rm.disconnect();
        } catch (Exception e) {
        }
        terminated = true;
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

}
