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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.utils.TargetType;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
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
    protected static int cmdHelpMaxCharLength = 28;
    private static final String YES = "yes";
    private static final String NO = "no";
    private static final String YES_NO = "(" + YES + "/" + NO + ")";

    protected ResourceManager rm;

    protected MBeanInfoViewer jmxInfoViewer = null;

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
        commands.add(new Command("locknode(nodeURL)", "Locks the node"));
        commands.add(new Command("unlocknode(nodeURL)", "Unlocks the node"));
        commands.add(new Command("listnodes(nodeSourceName)",
            "List nodes for a particular or all node sources"));
        commands.add(new Command("listns()", "List every handled node sources"));
        commands.add(new Command("nodeinfo(nodeURL)", "Displays node informations"));
        commands.add(new Command("listinfrastructures()", "List supported infrastructures"));
        commands.add(new Command("listpolicies()", "List available node sources policies"));
        commands.add(new Command("topology()", "Displays nodes topology"));
        commands.add(new Command("shutdown(preempt)",
            "Shutdown the Resource Manager (RM shutdown immediately if parameter is true)"));
        commands.add(new Command("stats()", "Display some statistics about the Resource Manager"));
        commands.add(new Command("myaccount()", "Display current user account information"));
        commands.add(new Command("account(username)", "Display account information by username"));
        commands.add(new Command("reloadconfig()",
            "Reload the resource manager config, including permissions and log4j"));
        commands.add(new Command("reconnect()", "Try to reconnect this console to the server"));
        commands
                .add(new Command("exec(scriptFilePath)",
                    "Execute the content of the given script file (parameter is a string representing a script-file path)"));
        commands.add(new Command("execr(path,type,[targets],args)",
            "Execute remotely the specified script at 'path' on specified ['targets'] of 'type' " +
                Arrays.toString(TargetType.values()) +
                " with optional 'args'. For example execr('../../samples/scripts/misc/processBuilder.js','" +
                TargetType.NODESOURCE_NAME + "',['" + RMConstants.DEFAULT_STATIC_SOURCE_NAME +
                "'],'hostname');"));
        commands.add(new Command("cnslhelp() or ?c", "Displays help about the console functions itself"));
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
        BufferedReader br = new BufferedReader(new InputStreamReader(ResourceManagerController.class
                .getResourceAsStream(JS_INIT_FILE)));
        eval(readFileContent(br));
        //read default js env file if exist
        if (new File(DEFAULT_INIT_JS).exists()) {
            print("! Loading environment from '" + DEFAULT_INIT_JS + "' !" + newline);
            this.exec_(DEFAULT_INIT_JS);
        }
        //read js env argument if any
        if (this.initEnvFileName != null) {
            print("! Loading environment from '" + this.initEnvFileName + "' !" + newline);
            this.exec_(this.initEnvFileName);
        }
    }

    /**
     * @see org.ow2.proactive.utils.console.ConsoleModel#startModel()
     */
    @Override
    public void startModel() throws Exception {
        checkIsReady();
        Map<String, String> conf = new HashMap<String, String>();
        conf.put("history_filepath", System.getProperty("user.home") + File.separator + ".proactive" +
            File.separator + "rm.hist");
        conf.put("history_size", "" + 30);
        console.configure(conf);
        console.start(" > ");
        console.addCompletion(getCompletionList());
        console.print("Type command here (type '?' or help() to see the list of commands)\n");
        console.print("");
        initialize();
        console.print("");
        String stmt;
        while (!terminated) {
            stmt = console.readStatement();
            if ("?".equals(stmt)) {
                print(newline + helpScreen());
            } else if ("?c".equals(stmt)) {
                print(newline + helpScreenCnsl());
            } else {
                eval(stmt);
            }
            print("");
        }
        console.stop();
    }

    @Override
    public void handleExceptionDisplay(String msg, Throwable t) {
        if (t instanceof ProActiveRuntimeException) {
            String tmp = msg + " : ResourceManager server seems to be unreachable !";
            console.error(tmp);
        } else {
            super.handleExceptionDisplay(msg, t);
        }
    }

    //***************** COMMAND LISTENER *******************
    //note : method marked with a "_" are called from JS evaluation

    public void shutdown_(boolean preempt) {
        try {
            boolean success = false;
            String s = console.readStatement("Are you sure you want to shutdown the Resource Manager ? " +
                YES_NO + " > ");
            //s == null is true if console has no input
            success = (s == null || s.equalsIgnoreCase(YES));
            if (success) {
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
            if (res.getBooleanValue()) {
                print("Node source '" + nodeSourceName + "' removal request sent to Resource Manager");
            } else {
                print("Cannot send '" + nodeSourceName + "' removal request to Resource Manager");
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error while removing node source '" + nodeSourceName, e);
        }
    }

    public List<RMNodeSourceEvent> listns_() {
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
            list.add("ADMINISTRATOR");
            oaf.setTitle(list);
            //separator
            oaf.addEmptyLine();
            for (RMNodeSourceEvent evt : listns) {
                list = new ArrayList<String>();
                list.add(evt.getSourceName());
                list.add(evt.getSourceDescription());
                list.add(evt.getNodeSourceAdmin());
                oaf.addLine(list);
            }
            print(Tools.getStringAsArray(oaf));
            return listns;
        } catch (Exception e) {
            handleExceptionDisplay("Error while retreiving nodeSources informations", e);
            return null;
        }
    }

    public List<RMNodeEvent> listnodes_(String nodeSourceName) {
        try {
            boolean listAllNodes = nodeSourceName == null || nodeSourceName.length() == 0;
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
                list.add("USED BY");
                oaf.setTitle(list);
                //separator
                oaf.addEmptyLine();
                for (RMNodeEvent evt : listne) {

                    if (listAllNodes || evt.getNodeSource().equals(nodeSourceName)) {
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
                }
                print(Tools.getStringAsArray(oaf));
            }
            return listne;
        } catch (Exception e) {
            handleExceptionDisplay("Error while retreiving nodes informations", e);
            return null;
        }
    }

    public void topology_() {
        Topology topology = rm.getTopology();
        print("Hosts list(" + topology.getHosts().size() + "): ");
        for (InetAddress host : topology.getHosts()) {
            print(host.toString());
        }

        print("\nHosts topology: ");
        final int COLUMN_NUMBERS = 3;
        int count = 1;

        ObjectArrayFormatter oaf = new ObjectArrayFormatter();
        oaf.setMaxColumnLength(80);
        oaf.setSpace(5);
        ArrayList<String> line = new ArrayList<String>();

        for (int i = 0; i < COLUMN_NUMBERS; i++) {
            line.add("Host");
            line.add("Distance (µs)");
            line.add("Host");
        }

        oaf.setTitle(line);
        oaf.addEmptyLine();

        line = new ArrayList<String>();
        for (InetAddress host : topology.getHosts()) {
            HashMap<InetAddress, Long> hostTopology = topology.getHostTopology(host);
            if (hostTopology != null) {
                for (InetAddress anotherHost : hostTopology.keySet()) {
                    line.add(host.toString());
                    line.add(String.valueOf(hostTopology.get(anotherHost)));
                    line.add(anotherHost.toString());

                    if (count++ % COLUMN_NUMBERS == 0) {
                        oaf.addLine(line);
                        line = new ArrayList<String>();
                    }
                }
            }
        }

        if (line.size() > 0) {
            for (int i = line.size() / 3; i < COLUMN_NUMBERS; i++) {
                line.add("");
                line.add("");
                line.add("");
            }
            oaf.addLine(line);
        }
        print(Tools.getStringAsArray(oaf));
    }

    private Object[] packPluginParameters(String[] params, Map<String, String> defaultValues)
            throws RMException, ClassNotFoundException {
        // extracting plugin name from the first param
        if (params != null && params.length > 0) {
            String name = params[0];
            // shifting array of input params (plugin does not imply having its name in the first parameter)
            Object[] shiftedParams = new Object[params.length - 1];
            System.arraycopy(params, 1, shiftedParams, 0, params.length - 1);

            //TODO if we want to run the command line remotely this part has to be rewritten
            Class<?> cls = Class.forName(name);
            PluginDescriptor pd = new PluginDescriptor(cls, defaultValues);
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

            //we get the default values for the im plugin
            Collection<PluginDescriptor> imPlugins = rm.getSupportedNodeSourceInfrastructures();
            //we get the associated default values
            Map<String, String> imDefaultValues = new HashMap<String, String>();
            if (imName != null) {
                for (PluginDescriptor pd : imPlugins) {
                    if (imName.equals(pd.getPluginName())) {
                        imDefaultValues = pd.getDefaultValues();
                        break;
                    }
                }
            }
            //we do the same for the ns policy
            Collection<PluginDescriptor> nspPlugins = rm.getSupportedNodeSourcePolicies();
            Map<String, String> nspDefaultValues = new HashMap<String, String>();
            if (policyName != null) {
                for (PluginDescriptor pd : nspPlugins) {
                    if (policyName.equals(pd.getPluginName())) {
                        nspDefaultValues = pd.getDefaultValues();
                        break;
                    }

                }
            }
            //even if we provide coherent default values, user must supply his
            Object[] imPackedParams = packPluginParameters(imInputParams, imDefaultValues);
            Object[] policyPackedParams = packPluginParameters(policyInputParams, nspDefaultValues);

            BooleanWrapper result = rm.createNodeSource(nodeSourceName, imName, imPackedParams, policyName,
                    policyPackedParams);
            if (result.getBooleanValue()) {
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

            if (result.getBooleanValue()) {
                print("Adding node '" + nodeName + "' request sent to Resource Manager");
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error while adding node '" + nodeName + "'", e);
        }
    }

    public Collection<PluginDescriptor> listInfrastructures_() {
        try {
            Collection<PluginDescriptor> plugins = rm.getSupportedNodeSourceInfrastructures();
            print("Available node source infrastructures:");
            for (PluginDescriptor plugin : plugins) {
                print(plugin.toString());
            }
            return plugins;
        } catch (Exception e) {
            handleExceptionDisplay("Error while retreiving infrastructure informations", e);
            return null;
        }
    }

    public Collection<PluginDescriptor> listPolicies_() {
        try {
            Collection<PluginDescriptor> plugins = rm.getSupportedNodeSourcePolicies();
            print("Available node source policies:");
            for (PluginDescriptor plugin : plugins) {
                print(plugin.toString());
            }
            return plugins;
        } catch (Exception e) {
            handleExceptionDisplay("Error while retreiving policies informations", e);
            return null;
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
            printMap(jmxInfoViewer.getMappedInfo("ProActiveResourceManager:name=RuntimeData"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void showMyAccount_() {
        try {
            printMap(jmxInfoViewer.getMappedInfo("ProActiveResourceManager:name=MyAccount"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void showAccount_(final String username) {
        try {
            jmxInfoViewer.setAttribute("ProActiveResourceManager:name=AllAccounts", "Username", username);
            printMap(jmxInfoViewer.getMappedInfo("ProActiveResourceManager:name=AllAccounts"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void nodeinfo_(final String nodeURL) {
        try {
            List<RMNodeEvent> allnodes = this.rm.getMonitoring().getState().getNodesEvents();
            boolean found = false;
            for (RMNodeEvent node : allnodes) {
                if (node.getNodeUrl().equals(nodeURL)) {
                    print(node.getNodeInfo());
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Node with URL " + nodeURL + " has not been found.");
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving node informations", e);
        }
    }

    public void refreshConfiguration_() {
        try {
            jmxInfoViewer.invoke("ProActiveResourceManager:name=Management", "refreshConfiguration",
                    new Object[0]);
            print("\nThe configuration has been successfully reloaded.");
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

    /**
     * Execute a JS command file with arguments.
     * 
     * @param params command file path must be the first param
     */
    public void execWithParam_(String... params) {
        try {
            File f = new File(params[0].trim());
            //parse arguments
            Map<String, String> mArgs = new HashMap<String, String>();
            for (String p : params) {
                if (p.contains("=")) {
                    String[] argVal = p.split("=");
                    mArgs.put(argVal[0], argVal[1]);
                }
            }
            BufferedReader br = new BufferedReader(new FileReader(f));
            eval(readFileContent(br), mArgs);
            br.close();
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
    }

    public void execr_(String commandFilePath, String targetType, HashSet<String> targets, String[] args) {
        try {
            // Read the script from the file and check is selection script
            File scriptFile = new File(commandFilePath.trim());
            String scriptContent = null;
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(scriptFile));
                scriptContent = readFileContent(br);
            } finally {
                if (br != null) {
                    br.close();
                }
            }
            Script<?> script = null;
            if (scriptContent.contains(SelectionScript.RESULT_VARIABLE)) {
                script = new SelectionScript(scriptFile, args);
            } else {
                script = new SimpleScript(scriptFile, args);
            }
            print("Script " + scriptFile.getName() + " targets: " + targets);

            // Execute the script on the specified targets (don't know how to avoid cast warning)
            @SuppressWarnings("unchecked")
            List<ScriptResult<Object>> results = this.rm.executeScript((Script<Object>) script, targetType,
                    targets);
            if (results.size() == 0) {
                print("No scripts were executed, maybe no targets found or the Resource Manager has no nodes");
            }
            for (ScriptResult<Object> sr : results) {
                StringBuilder bld = new StringBuilder("Script");
                Object result = sr.getResult();
                if (result != null) {
                    bld.append(" result: ").append(sr.getResult());
                }
                String output = sr.getOutput();
                if (output != null) {
                    bld.append(" output: ").append(newline);
                    bld.append(output);
                }
                Throwable exception = sr.getException();
                if (exception != null) {
                    bld.append(" had an exception:");
                    handleExceptionDisplay(bld.toString(), exception);
                } else {
                    print(bld.toString());
                }
            }
        } catch (Throwable e) {
            handleExceptionDisplay("*ERROR*", e);
        }
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

        for (int i = 6; i < commands.size(); i++) {
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

    public void locknodes_(HashSet<String> urls) {
        try {
            this.rm.lockNodes(urls).getBooleanValue();
            print("Nodes '" + urls + "' successfully locked");
        } catch (Exception e) {
            handleExceptionDisplay("Error while locking nodes", e);
        }
    }

    public void unlocknodes_(HashSet<String> urls) {
        try {
            this.rm.unlockNodes(urls).getBooleanValue();
            print("Nodes '" + urls + "' successfully unlocked");
        } catch (Exception e) {
            handleExceptionDisplay("Error while unlocking nodes", e);
        }
    }

    /**
     * Print a map on two column
     * 
     * @param map the map to be printed
     */
    private void printMap(Map<String, String> map) {
        ObjectArrayFormatter oaf = new ObjectArrayFormatter();
        oaf.setMaxColumnLength(80);
        //space between column
        oaf.setSpace(2);
        //fake title line
        List<String> list = new ArrayList<String>();
        list.add("");
        list.add("");
        oaf.setTitle(list);
        //lines content
        for (Entry<String, String> e : map.entrySet()) {
            list = new ArrayList<String>();
            list.add(e.getKey());
            list.add(e.getValue());
            oaf.addLine(list);
        }
        print(Tools.getStringAsArray(oaf));
    }
}
