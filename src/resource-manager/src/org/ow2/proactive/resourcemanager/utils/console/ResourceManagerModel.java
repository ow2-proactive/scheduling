/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive.utils.console.Command;
import org.ow2.proactive.utils.console.ConsoleModel;
import org.ow2.proactive.utils.console.MBeanInfoViewer;


/**
 * Class represents underlying model of the resource manager when accessing from
 * the command line.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class ResourceManagerModel extends ConsoleModel {

    private static final String DEFAULT_INIT_JS = System.getProperty("user.home") + File.separator +
        ".proactive" + File.separator + "rm-client.js";
    private static final String JS_INIT_FILE = "ResourceManagerActions.js";
    protected static final int cmdHelpMaxCharLength = 28;
    private static final String YES = "yes";
    private static final String NO = "no";
    private static final String YES_NO = "(" + YES + "/" + NO + ")";

    protected ResourceManager rm;
    private ArrayList<Command> commands;

    private static int logsNbLines = 20;
    private static String logsDirectory = System.getProperty("pa.rm.home") + File.separator + ".logs";

    private static final String rmLogFile = "RM.log";

    protected MBeanInfoViewer jmxInfoViewer = null;

    private String initEnvFileName = null;

    /**
     * Get this model. Also specify if the exit command should do something or not
     *
     * @param allowExitCommand true if the exit command is part of the commands, false if exit command does not exist.
     * @return the current model associated to this class.
     */
    public static ResourceManagerModel getModel(boolean allowExitCommand) {
        if (model == null) {
            model = new ResourceManagerModel(allowExitCommand);
        }
        return (ResourceManagerModel) model;
    }

    private static ResourceManagerModel getModel() {
        return (ResourceManagerModel) model;
    }

    protected ResourceManagerModel(boolean allowExitCommand) {
        this.allowExitCommand = allowExitCommand;
        commands = new ArrayList<Command>();
        commands.add(new Command("addcandidate(str)",
            "Add a completion candidate to the current completion list "
                + "(str is a string representing the candidate to add)"));
        commands
                .add(new Command(
                    "exmode(display,onDemand)",
                    "Change the way exceptions are displayed (if display is true, stacks are displayed - if onDemand is true, prompt before displaying stacks)"));
        commands.add(new Command("addnode(nodeURL, nsName)",
            "Add node to the given node source (parameters is a string representing the node URL to add &"
                + " an optional string representing the node source in which to add the node)"));
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
        commands.add(new Command("listinfrastructures()", "List supported infrastructures"));
        commands.add(new Command("listpolicies()", "List available node sources policies"));
        commands.add(new Command("shutdown(preempt)",
            "Shutdown the Resource Manager (RM shutdown immediately if parameter is true)"));
        commands.add(new Command("stats()", "Display some statistics about the Resource Manager"));
        commands.add(new Command("myaccount()", "Display current user account information"));
        commands.add(new Command("account(username)", "Display account information by username"));
        commands.add(new Command("reloadpermissions()", "Reload the permission file"));
        commands.add(new Command("reconnect()", "Try to reconnect this console to the server"));
        commands
                .add(new Command("exec(scriptFilePath)",
                    "Execute the content of the given script file (parameter is a string representing a script-file path)"));
        commands.add(new Command("setlogsdir(logsDir)",
            "Set the directory where the log are located, (default is RM_HOME/.logs"));
        commands.add(new Command("viewlogs(nbLines)",
            "View the last nbLines lines of the logs file, (default nbLines is 20)"));
        if (allowExitCommand) {
            commands.add(new Command("exit()", "Exits RM controller"));
        }

    }

    /**
     * Retrieve a completion list from the list of commands
     *
     * @return a completion list as a string array
     */
    private String[] getCompletionList() {
        String[] ret = new String[commands.size()];
        for (int i = 0; i < commands.size(); i++) {
            String name = commands.get(i).getName();
            int lb = name.indexOf('(');
            ret[i] = name.substring(0, lb + 1);
            if (name.indexOf(')') - lb == 1) {
                ret[i] += ");";
            }
        }
        return ret;
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
        BufferedReader br = new BufferedReader(new InputStreamReader(ResourceManagerController.class
                .getResourceAsStream(JS_INIT_FILE)));
        eval(readFileContent(br));
        //read default js env file if exist
        if (new File(DEFAULT_INIT_JS).exists()) {
            console.print("! Loading environment from '" + DEFAULT_INIT_JS + "' !" + newline);
            this.exec_(DEFAULT_INIT_JS);
        }
        //read js env argument if any
        if (this.initEnvFileName != null) {
            console.print("! Loading environment from '" + this.initEnvFileName + "' !" + newline);
            this.exec_(this.initEnvFileName);
        }
    }

    void setInitEnv(String fileName) {
        this.initEnvFileName = fileName;
    }

    /**
     * @see org.ow2.proactive.utils.console.ConsoleModel#startModel()
     */
    @Override
    public void startModel() throws Exception {
        checkIsReady();
        console.start(" > ");
        console.addCompletion(getCompletionList());
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

    @Override
    public void handleExceptionDisplay(String msg, Throwable t) {
        if (t instanceof ProActiveRuntimeException) {
            String tmp = msg + " : ResourceManager server seems to be unreachable !";
            if (!displayOnStdStream) {
                console.error(tmp);
            } else {
                System.err.printf(tmp);
            }
        } else {
            super.handleExceptionDisplay(msg, t);
        }
    }

    //***************** COMMAND LISTENER *******************
    //note : method marked with a "_" are called from JS evaluation

    public void addCandidate_(String candidate) {
        if (candidate == null) {
            error("Candidate string cannot be null or empty");
        } else {
            console.addCompletion(candidate);
        }
    }

    public void shutdown_(boolean preempt) {
        try {
            boolean success = false;
            if (!displayOnStdStream) {
                String s = console.readStatement("Are you sure you want to shutdown the Resource Manager ? " +
                    YES_NO + " > ");
                success = s.equalsIgnoreCase(YES);
            }
            if (success || displayOnStdStream) {
                rm.shutdown(preempt);
                print("Shutdown request sent to Resource Manager, controller will shutdown !");
                terminated = true;
            } else {
                print("Shutdown aborted !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error while shutting down the RM", e);
        }
    }

    public void removens_(String nodeSourceName, boolean preempt) {
        try {
            BooleanWrapper res = rm.removeNodeSource(nodeSourceName, preempt);
            if (res.booleanValue()) {
                print("Node source '" + nodeSourceName + "' removal request sent to Resource Manager");
            } else {
                print("Cannot send '" + nodeSourceName + "' removal request to Resource Manager");
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error while removing node source '" + nodeSourceName, e);
        }
    }

    public void listns_() {
        try {
            List<String> list;
            List<RMNodeSourceEvent> listns = rm.getMonitoring().getState().getNodeSource();
            ObjectArrayFormatter oaf = new ObjectArrayFormatter();
            oaf.setMaxColumnLength(70);
            //space between column
            oaf.setSpace(3);
            //title line
            list = new ArrayList<String>();
            list.add("SOURCE NAME");
            list.add("DESCRIPTION");
            list.add("PROVIDER");
            oaf.setTitle(list);
            //separator
            oaf.addEmptyLine();
            for (RMNodeSourceEvent evt : listns) {
                list = new ArrayList<String>();
                list.add(evt.getSourceName());
                list.add(evt.getSourceDescription());
                list.add(evt.getNodeSourceProvider());
                oaf.addLine(list);
            }
            print(Tools.getStringAsArray(oaf));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retreiving nodeSources informations", e);
        }
    }

    public void listnodes_() {
        try {
            List<RMNodeEvent> listne = rm.getMonitoring().getState().getNodesEvents();
            if (listne.size() == 0) {
                print("No nodes handled by Resource Manager");
            } else {
                List<String> list;
                ObjectArrayFormatter oaf = new ObjectArrayFormatter();
                oaf.setMaxColumnLength(80);
                //space between column
                oaf.setSpace(2);
                //title line
                list = new ArrayList<String>();
                list.add("SOURCE NAME");
                list.add("HOSTNAME");
                list.add("STATE");
                list.add("SINCE");
                list.add("URL");
                list.add("PROVIDER");
                list.add("OWNER");
                oaf.setTitle(list);
                //separator
                oaf.addEmptyLine();
                for (RMNodeEvent evt : listne) {
                    list = new ArrayList<String>();
                    list.add(evt.getNodeSource());
                    list.add(evt.getHostName());
                    list.add(evt.getNodeState().toString());
                    list.add(evt.getTimeStampFormatted());
                    list.add(evt.getNodeUrl());
                    list.add(evt.getNodeProvider() == null ? "" : evt.getNodeProvider());
                    list.add(evt.getNodeOwner() == null ? "" : evt.getNodeOwner());
                    oaf.addLine(list);
                }
                print(Tools.getStringAsArray(oaf));
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error while retreiving nodes informations", e);
        }
    }

    private Object[] packPluginParameters(String[] params) throws RMException, ClassNotFoundException {
        // extracting plugin name from the first param
        if (params != null && params.length > 0) {
            String name = params[0];
            // shifting array of input params (plugin does not imply having its name in the first parameter)
            Object[] shiftedParams = new Object[params.length - 1];
            System.arraycopy(params, 1, shiftedParams, 0, params.length - 1);

            //TODO if we want to run the command line remotely this part has to be rewritten
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

    public boolean createns_(String nodeSourceName, String[] imInputParams, String[] policyInputParams) {

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

            BooleanWrapper result = rm.createNodeSource(nodeSourceName, imName, imPackedParams, policyName,
                    policyPackedParams);
            if (result.booleanValue()) {
                print("Node source '" + nodeSourceName + "' creation request sent to Resource Manager");
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error while creating node source '" + nodeSourceName, e);
            return false;
        }
        return true;
    }

    public void removenode_(String nodeURL, boolean preempt) {
        try {
            rm.removeNode(nodeURL, preempt);
            print("Nodes '" + nodeURL + "' removal request sent to Resource Manager");
        } catch (Exception e) {
            handleExceptionDisplay("Error while removing node", e);
        }
    }

    public void addnode_(String nodeName, String nodeSourceName) {
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
        } catch (Exception e) {
            handleExceptionDisplay("Error while adding node '" + nodeName + "'", e);
        }
    }

    public void listInfrastructures_() {
        try {
            Collection<PluginDescriptor> plugins = rm.getSupportedNodeSourceInfrastructures();
            print("Available node source infrastructures:");
            for (PluginDescriptor plugin : plugins) {
                print(plugin.toString());
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error while retreiving infrastructure informations", e);
        }
    }

    public void listPolicies_() {
        try {
            Collection<PluginDescriptor> plugins = rm.getSupportedNodeSourcePolicies();
            print("Available node source policies:");
            for (PluginDescriptor plugin : plugins) {
                print(plugin.toString());
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error while retreiving policies informations", e);
        }
    }

    public void reconnect_() {
        try {
            connectRM(authentication, credentials);
            print("Console has been successfully re-connected to the ResourceManager !");
        } catch (LoginException e) {
            //should not append in such a context !
        } catch (RuntimeException re) {
            try {
                authentication = RMConnection.join(this.serverURL);
                connectRM(authentication, credentials);
                print("Console has been successfully re-connected to the ResourceManager !");
            } catch (Exception e) {
                handleExceptionDisplay("*ERROR*", e);
            }
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
    }

    public void showRuntimeData_() {
        try {
            print(jmxInfoViewer.getInfo("ProActiveResourceManager:name=RuntimeData"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void showMyAccount_() {
        try {
            print(jmxInfoViewer.getInfo("ProActiveResourceManager:name=MyAccount"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void showAccount_(final String username) {
        try {
            jmxInfoViewer.setAttribute("ProActiveResourceManager:name=AllAccounts", "Username", username);
            print(jmxInfoViewer.getInfo("ProActiveResourceManager:name=AllAccounts"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void refreshPermissionPolicy_() {
        try {
            jmxInfoViewer.invoke("ProActiveResourceManager:name=Management", "refreshPermissionPolicy",
                    new Object[0]);
            print("\nThe permission file has been successfully reloaded.");
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void exec_(String commandFilePath) {
        try {
            File f = new File(commandFilePath.trim());
            BufferedReader br = new BufferedReader(new FileReader(f));
            eval(readFileContent(br));
            br.close();
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
    }

    public void setLogsDir_(String logsDir) {
        if (logsDir == null || "".equals(logsDir)) {
            error("Given logs directory is null or empty !");
            return;
        }
        File dir = new File(logsDir);
        if (!dir.exists()) {
            error("Given logs directory does not exist !");
            return;
        }
        if (!dir.isDirectory()) {
            error("Given logsDir is not a directory !");
            return;
        }
        dir = new File(logsDir + File.separator + rmLogFile);
        if (!dir.exists()) {
            error("Given logs directory does not contains Scheduler logs files !");
            return;
        }
        print("Logs Directory set to '" + logsDir + "' !");
        logsDirectory = logsDir;
    }

    public void viewlogs_(String nbLines) {
        if (!"".equals(nbLines)) {
            try {
                logsNbLines = Integer.parseInt(nbLines);
            } catch (NumberFormatException nfe) {
                //logsNbLines not set
            }
        }
        print(readLastNLines(rmLogFile));
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

    public void exit_() {
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

    public ResourceManager getResourceManager() {
        return rm;
    }

    //***************** HELP SCREEN *******************

    @Override
    protected String helpScreen() {
        StringBuilder out = new StringBuilder("Resource Manager controller commands are :" + newline +
            newline);

        out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s\t %2$s" + newline, commands.get(0)
                .getName(), commands.get(0).getDescription()));
        out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s\t %2$s" + newline + newline, commands
                .get(1).getName(), commands.get(1).getDescription()));

        for (int i = 2; i < commands.size(); i++) {
            out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s\t %2$s" + newline, commands.get(i)
                    .getName(), commands.get(i).getDescription()));
        }

        return out.toString();
    }

    //**************** GETTER / SETTER ******************

    protected String serverURL;
    protected RMAuthentication authentication;
    protected Credentials credentials;

    /**
     * Connect the RM using given authentication interface and credentials
     *
     * @param auth the authentication interface on which to connect
     * @param credentials the credentials to be used for the connection
     * @throws LoginException If bad credentials are provided
     */
    public void connectRM(RMAuthentication auth, Credentials credentials) throws LoginException {
        if (auth == null || credentials == null) {
            throw new NullPointerException("Given authentication part is null");
        }
        this.rm = auth.login(credentials);
        this.authentication = auth;
        this.credentials = credentials;
        this.serverURL = auth.getHostURL();
    }

    public void connectRM(ResourceManager rm) {
        if (rm == null) {
            throw new NullPointerException("Given RM must not be null");
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
