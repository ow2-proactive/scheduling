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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.StartNode;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.tests.ProActiveSetup;

import functionaltests.common.CommonTUtils;
import functionaltests.common.InputStreamReaderThread;
import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.monitor.RMMonitorsHandler;
import org.ow2.tests.ProcessCleaner;


/**
 *
 * Static helpers for Resource Manager functional tests.
 * It provides waiters methods that check correct event dispatching.
 *
 * @author ProActive team
 *
 */
public class RMTHelper {

    /**
     * Number of nodes deployed with default deployment descriptor
     */
    private static final int defaultNodesNumber = 2;

    // default RMI port
    // do not use the one from proactive config to be able to
    // keep the RM running after the test with rmi registry is killed
    public static final int PA_RMI_PORT = 1199;

    /**
     * Timeout for local infrastructure
     */
    public static final int defaultNodesTimeout = 60 * 1000; //60s

    public static final URL functionalTestRMProperties = RMTHelper.class
            .getResource("/functionaltests/config/functionalTRMProperties.ini");

    protected RMMonitorsHandler monitorsHandler;

    protected RMMonitorEventReceiver eventReceiver;

    protected ResourceManager resourceManager;

    protected RMAuthentication auth;

    private Process rmProcess;

    final protected static ProActiveSetup setup = new ProActiveSetup();

    /**
     * Default user name for RM's connection
     */
    public static String defaultUserName = "test_executor";

    /**
     * Default password for RM's connection
     */
    public static String defaultUserPassword = "pwd";

    /**
     * Currently connected user name for RM's connection
     */
    public static String connectedUserName = null;

    /**
     * Currently connected password for RM's connection
     */
    public static String connectedUserPassword = null;

    public static Credentials connectedUserCreds = null;

    private static RMTHelper defaultInstance = new RMTHelper();

    public static RMTHelper getDefaultInstance() {
        return defaultInstance;
    }

    /**
     * Log a String for tests.
     *
     * @param s String to log
     */
    public static void log(String s) {
        System.out.println("------------------------------ " + s);
    }

    /**
     * Creates a Local node source
     * @throws Exception
     */
    public void createNodeSource() throws Exception {
        createNodeSource(this.getClass().getSimpleName());
    }

    /**
     * Creates a Local node source with specified name
     * @throws Exception
     * @return expected number of nodes
     */
    public int createNodeSource(String name) throws Exception {
        createNodeSource(name, RMTHelper.defaultNodesNumber);
        return RMTHelper.defaultNodesNumber;
    }

    /**
     * Creates a Local node source with specified name
     */
    public void createNodeSource(String name, int nodeNumber) throws Exception {
        RMFactory.setOsJavaProperty();
        ResourceManager rm = getResourceManager();
        System.out.println("Creating a node source " + name);
        //first emtpy im parameter is default rm url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        rm
                .createNodeSource(name, LocalInfrastructure.class.getName(), new Object[] { creds,
                        nodeNumber, RMTHelper.defaultNodesTimeout, setup.getJvmParameters() + " " +
                        CentralPAPropertyRepository.PA_RMI_PORT.getCmdLine()+RMTHelper.PA_RMI_PORT
                },
                        StaticPolicy.class.getName(), null);
        rm.setNodeSourcePingFrequency(5000, name);

        waitForNodeSourceCreation(name, nodeNumber);
    }

    /** Wait for the node source to be created when the node source is empty */
    public void waitForNodeSourceCreation(String name) {
        waitForNodeSourceCreation(name, 0);
    }

    /** Wait for the node source to be created and the nodes to be connected */
    public void waitForNodeSourceCreation(String name, int nodeNumber) {
        waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, name);
        for (int i = 0; i < nodeNumber; i++) {
            waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }

    /**
     * Create a ProActive Node in a new JVM on the local host
     * This method can be used to test adding nodes mechanism
     * with already deploy ProActive nodes.
     * @param nodeName node's name to create
     * @return created node object
     * @throws IOException if the external JVM cannot be created
     * @throws NodeException if lookup of the new node fails.
     */
    public TNode createNode(String nodeName) throws IOException, NodeException {
        return createNode(nodeName, null);
    }

    public TNode createNode(String nodeName, Map<String, String> vmParameters) throws IOException,
            NodeException {
        return createNode(nodeName, null, vmParameters, null);
    }

    public List<TNode> createNodes(final String nodeName, int number) throws IOException, NodeException, ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(number);
        ArrayList<Future<TNode>> futureNodes = new ArrayList<Future<TNode>>(number);
        for (int i=0; i< number; i++) {
            final int index = i;
            futureNodes.add(executorService.submit(new Callable<TNode>() {
                @Override
                public TNode call() {
                    try {
                        return createNode(nodeName+index, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }));
        }

        ArrayList nodes = new ArrayList(number);
        for (int i=0; i< number; i++) {
            nodes.add(futureNodes.get(i).get());
        }

        return nodes;
    }

    // TODO REMOVE THIS METHOD
    public void createNodeSource(int rmiPort, int nodesNumber) throws Exception {
        createNodeSource(rmiPort, nodesNumber, null);
    }

    // TODO REMOVE THIS METHOD
    public void createNodeSource(int rmiPort, int nodesNumber, List<String> vmOptions) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put(CentralPAPropertyRepository.PA_RMI_PORT.getName(), String.valueOf(rmiPort));
        map
                .put(CentralPAPropertyRepository.PA_HOME.getName(), CentralPAPropertyRepository.PA_HOME
                        .getValue());
        for (int i = 0; i < nodesNumber; i++) {
            String nodeName = "node-" + i;
            String nodeUrl = "rmi://localhost:" + rmiPort + "/" + nodeName;
            createNode(nodeName, nodeUrl, map, vmOptions);
            getResourceManager().addNode(nodeUrl);
        }
        waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.DEFAULT);
        for (int i = 0; i < nodesNumber; i++) {
            waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }

    /**
     * Create a ProActive Node in a new JVM on the local host
     * with specific java parameters.
     * This method can be used to test adding nodes mechanism
     * with already deploy ProActive nodes.
     * @param nodeName node's name to create
     * @param vmParameters an HashMap containing key and value String
     * of type :-Dkey=value
     * @return created node object
     * @throws IOException if the external JVM cannot be created
     * @throws NodeException if lookup of the new node fails.
     */
    public TNode createNode(String nodeName, String expectedUrl, Map<String, String> vmParameters,
            List<String> vmOptions) throws IOException, NodeException {

        JVMProcessImpl nodeProcess = createJvmProcess(StartNode.class.getName(), Arrays.asList(nodeName),
                vmParameters, vmOptions);
        return createNode(nodeName, expectedUrl, nodeProcess);

    }

    public static TNode createRMNodeStarterNode(String nodeName) throws IOException, NodeException {

        JVMProcessImpl nodeProcess = createJvmProcess(RMNodeStarter.class.getName(), Arrays.asList("-n", nodeName),
                null, null);
        return createNode(nodeName, null, nodeProcess);

    }

    public static TNode createNode(String nodeName, String expectedUrl, JVMProcess nodeProcess) throws IOException, NodeException {

        if (expectedUrl == null) {
            expectedUrl = "rmi://" + ProActiveInet.getInstance().getHostname() + ":" + PA_RMI_PORT + "/" + nodeName;
        }

        try {
            Node newNode = null;

            final long NODE_START_TIMEOUT_IN_MS = 60000;
            long startTimeStamp = System.currentTimeMillis();

            NodeException toThrow = null;
            while ((System.currentTimeMillis()-startTimeStamp) < NODE_START_TIMEOUT_IN_MS) {
                try {
                    newNode = NodeFactory.getNode(expectedUrl);
                } catch (NodeException e) {
                    toThrow = e;
                    //nothing, wait another loop
                }
                if (newNode != null) {
                    return new TNode(nodeProcess, newNode);
                } else {
                    Thread.sleep(100);
                }
            }
            throw toThrow == null ? new NodeException("unable to create the node " + nodeName) : toThrow;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JVMProcessImpl createJvmProcess(String className, List<String> parameters, Map<String, String> vmParameters, List<String> vmOptions) throws IOException {
        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setClassname(className);

        ArrayList<String> jvmParameters = new ArrayList<String>();

        if (vmParameters == null) {
            vmParameters = new HashMap<String, String>();
        }

        if (!vmParameters.containsKey(CentralPAPropertyRepository.PA_RMI_PORT.getName())) {
            vmParameters.put(CentralPAPropertyRepository.PA_RMI_PORT.getName(), String.valueOf(PA_RMI_PORT));
        }
        if (!vmParameters.containsKey(CentralPAPropertyRepository.PA_HOME.getName())) {
            vmParameters.put(CentralPAPropertyRepository.PA_HOME.getName(), CentralPAPropertyRepository.PA_HOME
                    .getValue());
        }
        if (!vmParameters.containsKey(PAResourceManagerProperties.RM_HOME.getKey())) {
            vmParameters.put(PAResourceManagerProperties.RM_HOME.getKey(), PAResourceManagerProperties.RM_HOME
              .getValueAsString());
        }

        for (Entry<String, String> entry : vmParameters.entrySet()) {
            if (!entry.getKey().equals("") && !entry.getValue().equals("")) {
                jvmParameters.add("-D" + entry.getKey() + "=" + entry.getValue());
            }
        }

        if (vmOptions != null) {
            jvmParameters.addAll(vmOptions);
        }
        jvmParameters.addAll(setup.getJvmParametersAsList());
        nodeProcess.setJvmOptions(jvmParameters);
        nodeProcess.setParameters(parameters);
        nodeProcess.startProcess();
        return nodeProcess;
    }

    /**
     * Start the RM using a forked JVM
     *
     * @param configurationFile the RM's configuration file to use (default is functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception if an error occurs.
     */
    public String startRM(String configurationFile, int rmiPort, String... jvmArgs) throws Exception {
        if (configurationFile == null) {
            configurationFile = new File(functionalTestRMProperties.toURI()).getAbsolutePath();
        }
        PAResourceManagerProperties.updateProperties(configurationFile);

        List<String> commandLine = new ArrayList<String>();
        commandLine.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        commandLine.add("-Djava.security.manager");

        String proactiveHome = CentralPAPropertyRepository.PA_HOME.getValue();
        if (!CentralPAPropertyRepository.PA_HOME.isSet()) {
            proactiveHome = PAResourceManagerProperties.RM_HOME.getValueAsString();
        }
        commandLine.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() +
          proactiveHome);

        String securityPolicy = CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getValue();
        if (!CentralPAPropertyRepository.JAVA_SECURITY_POLICY.isSet()) {
            securityPolicy = PAResourceManagerProperties.RM_HOME.getValueAsString() + "/config/security.java.policy-server";
        }
        commandLine.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
          securityPolicy);

        String log4jConfiguration = CentralPAPropertyRepository.LOG4J.getValue();
        if (!CentralPAPropertyRepository.LOG4J.isSet()) {
            log4jConfiguration = "file:" + PAResourceManagerProperties.RM_HOME.getValueAsString() + "/config/log4j/log4j-junit";
        }
        commandLine.add(CentralPAPropertyRepository.LOG4J.getCmdLine() +
          log4jConfiguration);

        commandLine.add(PAResourceManagerProperties.RM_HOME.getCmdLine() +
            PAResourceManagerProperties.RM_HOME.getValueAsString());
        commandLine.add(CentralPAPropertyRepository.PA_RUNTIME_PING.getCmdLine() +
                false);

        commandLine.add("-cp");
        commandLine.add(testClasspath());
        commandLine.add(CentralPAPropertyRepository.PA_TEST.getCmdLine() + "true");
        commandLine.add("-Djava.awt.headless=true"); // For Mac builds
        commandLine.add(CentralPAPropertyRepository.PA_RMI_PORT.getCmdLine() + rmiPort);
        Collections.addAll(commandLine, jvmArgs);
        commandLine.add(RMTStarter.class.getName());
        commandLine.add(configurationFile);
        System.out.println("Starting RM process: " + commandLine);

        ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.redirectErrorStream(true);
        rmProcess = processBuilder.start();

        InputStreamReaderThread outputReader = new InputStreamReaderThread(rmProcess.getInputStream(),
            "[RM VM output]: ");
        outputReader.start();

        String url = getLocalUrl(rmiPort);

        System.out.println("Waiting for the RM using URL: " + url);
        auth = RMConnection.waitAndJoin(url);
        return url;
    }

    public static String testClasspath() {
        String home = PAResourceManagerProperties.RM_HOME.getValueAsString();
        String classpathToLibFolderWithWildcard = home + File.separator + "dist" + File.separator + "lib" + File.separator + "*";
        if (OperatingSystem.getOperatingSystem().equals(OperatingSystem.windows)) {
            // required by windows otherwise wildcard is expanded
            classpathToLibFolderWithWildcard = "\"" + classpathToLibFolderWithWildcard + "\"";
        }
        return classpathToLibFolderWithWildcard;
    }

    /**
     * Returns the alive Nodes accessible by the RM
     * @return list of ProActive Nodes
     */
    public List<Node> listAliveNodes() throws Exception {
        ArrayList<Node> nodes = new ArrayList<Node>();
        Set<String> urls = getResourceManager().listAliveNodeUrls();
        for (String url : urls) {
            nodes.add(NodeFactory.getNode(url));
        }
        return nodes;
    }

    /**
     * Returns the alive Nodes accessible by the RM in the given node sources
     * @param  nodeSourceNames
     * @return list of ProActive Nodes
     */
    public List<Node> listAliveNodes(Set<String> nodeSourceNames) throws Exception {
        ArrayList<Node> nodes = new ArrayList<Node>();
        Set<String> urls = getResourceManager().listAliveNodeUrls(nodeSourceNames);
        for (String url : urls) {
            nodes.add(NodeFactory.getNode(url));
        }
        return nodes;
    }

    /**
     * Returns the list of alive  Nodes
     * @return list of ProActive Nodes urls
     */
    public Set<String> listAliveNodesUrls() throws Exception {
        return getResourceManager().listAliveNodeUrls();
    }

    /**
     * Returns the list of alive Nodes in the given nodeSources
     * @param nodeSourceNames
     * @return list of ProActive Nodes urls
     */
    public Set<String> listAliveNodesUrls(Set<String> nodeSourceNames) throws Exception {
        return getResourceManager().listAliveNodeUrls(nodeSourceNames);
    }

    /**
     * Stop the Resource Manager if exists.
     * @throws Exception
     * @throws ProActiveException
     */
    public void killRM() throws Exception {
        if (rmProcess != null) {
            rmProcess.destroy();
            rmProcess.waitFor();
            rmProcess = null;

            // killing all rm nodes
            ProcessCleaner cleaner = new ProcessCleaner(".*RMNodeStarter.*");
            cleaner.killAliveProcesses();

            // sometimes RM_NODE object isn't removed from the RMI registry after JVM with RM is killed (SCHEDULING-1498)
            CommonTUtils.cleanupRMActiveObjectRegistry();
        }
        reset();
    }

    /**
     * Resets the RMTHelper
     */
    public void reset() throws Exception {
        auth = null;
        resourceManager = null;
        eventReceiver = null;
    }

    /**
     * Wait for an event regarding Scheduler state : started, resumed, stopped...
     * If a corresponding event has been already thrown by scheduler, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param event awaited event.
     */
    public void waitForRMStateEvent(RMEventType event) {
        try {
            waitForRMStateEvent(event, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
        }
    }

    /**
     * Wait for an event regarding RM state : started, resumed, stopped...
     * If a corresponding event has been already thrown by scheduler, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param eventType awaited event.
     * @param timeout in milliseconds
     * @throws ProActiveTimeoutException if timeout is reached
     */
    public void waitForRMStateEvent(RMEventType eventType, long timeout) throws ProActiveTimeoutException {
        getMonitorsHandler().waitForRMStateEvent(eventType, timeout);
    }

    /**
     * Wait for an event regarding node sources: created, removed....
     * If a corresponding event has been already thrown by RM, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param nodeSourceEvent awaited event.
     * @param nodeSourceName corresponding node source name for which an event is awaited.
     */
    public void waitForNodeSourceEvent(RMEventType nodeSourceEvent, String nodeSourceName) {
        try {
            waitForNodeSourceEvent(nodeSourceEvent, nodeSourceName, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
        }
    }

    /**
     * Wait for an event regarding node sources: created, removed....
     * If a corresponding event has been already thrown by RM, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param eventType awaited event.
     * @param nodeSourceName corresponding node source name for which an event is awaited.
     * @param timeout in milliseconds
     * @throws ProActiveTimeoutException if timeout is reached
     */
    public void waitForNodeSourceEvent(RMEventType eventType, String nodeSourceName, long timeout)
            throws ProActiveTimeoutException {
        getMonitorsHandler().waitForNodesourceEvent(eventType, nodeSourceName, timeout);
    }

    /**
     * Wait for an event on a specific node : created, removed....
     * If a corresponding event has been already thrown by RM, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param nodeEvent awaited event.
     * @param nodeUrl Url's of the node for which a new state is awaited.
     * @return RMNodeEvent object received by event receiver.
     */
    public RMNodeEvent waitForNodeEvent(RMEventType nodeEvent, String nodeUrl) {
        try {
            return waitForNodeEvent(nodeEvent, nodeUrl, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log string ?
            return null;
        }
    }

    /**
     * Wait for an event on a specific node : created, removed....
     * If a corresponding event has been already thrown by RM, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param eventType awaited event.
     * @param nodeUrl Url's of the node for which a new state is awaited
     * @param timeout in milliseconds
     * @return RMNodeEvent object received by event receiver.
     * @throws ProActiveTimeoutException if timeout is reached
     */
    public RMNodeEvent waitForNodeEvent(RMEventType eventType, String nodeUrl, long timeout)
            throws ProActiveTimeoutException {
        return getMonitorsHandler().waitForNodeEvent(eventType, nodeUrl, timeout);
    }

    /**
     * Wait for an event on any node: added, removed....
     * If a corresponding event has been already thrown by RM, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param eventType awaited event.
     * @return RMNodeEvent object received by event receiver.
     */
    public RMNodeEvent waitForAnyNodeEvent(RMEventType eventType) {
        try {
            return waitForAnyNodeEvent(eventType, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
            return null;
        }
    }

    /**
     * Kills the node with specified url
     * @param url of the node
     * @throws NodeException if node cannot be looked up
     */
    public void killNode(String url) throws NodeException {
        Node node = NodeFactory.getNode(url);
        try {
            node.getProActiveRuntime().killRT(false);
        } catch (Exception ignored) {
        }
    }

    /**
     * Wait for an event on any node: added, removed....
     * If a corresponding event has been already thrown by RM, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param eventType awaited event.
     * @param timeout in milliseconds
     * @return RMNodeEvent object received by event receiver.
     * @throws ProActiveTimeoutException if timeout is reached
     */
    public RMNodeEvent waitForAnyNodeEvent(RMEventType eventType, long timeout)
            throws ProActiveTimeoutException {
        return getMonitorsHandler().waitForAnyNodeEvent(eventType, timeout);
    }

    //-------------------------------------------------------------//
    //private methods
    //-------------------------------------------------------------//

    private void initEventReceiver() throws Exception {
        RMMonitorsHandler mHandler = getMonitorsHandler();
        if (eventReceiver == null) {
            /** create event receiver then turnActive to avoid deepCopy of MonitorsHandler object
             * 	(shared instance between event receiver and static helpers).
            */
            System.out.println("Initializing new event receiver");
            RMMonitorEventReceiver passiveEventReceiver = new RMMonitorEventReceiver(mHandler);
            eventReceiver = (RMMonitorEventReceiver) PAActiveObject.turnActive(passiveEventReceiver);
            PAFuture.waitFor(resourceManager.getMonitoring().addRMEventListener(eventReceiver));
        }
    }

    /**
     * Gets the connected ResourceManager interface.
     */
    public ResourceManager getResourceManager() throws Exception {
        return getResourceManager(null, defaultUserName, defaultUserPassword);
    }

    /**
     * Idem than getResourceManager but allow to specify a propertyFile
     * @return the resource manager
     * @throws Exception
     */
    public ResourceManager getResourceManager(String propertyFile, String user, String pass) throws Exception {

        if (user == null)
            user = defaultUserName;
        if (pass == null)
            pass = defaultUserPassword;

        if (resourceManager == null || !user.equals(connectedUserName)) {

            if (resourceManager != null) {
                System.out.println("Disconnecting user " + connectedUserName + " from the resource manager");
                try {
                    resourceManager.getMonitoring().removeRMEventListener();
                    resourceManager.disconnect().getBooleanValue();

                    eventReceiver = null;
                    resourceManager = null;
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
            }

            if (auth == null) {
                try {
                    // trying to connect to the existing RM first
                    auth = RMConnection.waitAndJoin(getLocalUrl(PA_RMI_PORT), 1);
                    System.out.println("Connected to the RM on " + getLocalUrl(PA_RMI_PORT));
                } catch (Exception e) {
                    // creating a new RM and default node source
                    startRM(propertyFile, PA_RMI_PORT);
                }
            }
            authentificate(user, pass);
            initEventReceiver();
            System.out.println("RMTHelper is connected");
        }
        return resourceManager;
    }

    public static String getLocalUrl(int rmiPort) {
        return "rmi://localhost:" + rmiPort + "/";
    }

    public static String getLocalUrl() {
        return getLocalUrl(PA_RMI_PORT);
    }

    private void authentificate(String user, String pass) throws Exception {
        connectedUserName = user;
        connectedUserPassword = pass;
        connectedUserCreds = Credentials.createCredentials(new CredData(CredData.parseLogin(user), CredData
                .parseDomain(connectedUserName), pass), auth.getPublicKey());

        System.out.println("Authentificating as user " + user);
        resourceManager = auth.login(connectedUserCreds);
    }

    public RMMonitorsHandler getMonitorsHandler() {
        if (monitorsHandler == null) {
            monitorsHandler = new RMMonitorsHandler();
        }
        return monitorsHandler;
    }

    public RMMonitorEventReceiver getEventReceiver() {
        return eventReceiver;
    }

    public RMAuthentication getRMAuth() throws Exception {
        if (auth == null) {
            getResourceManager();
        }
        return auth;
    }
}
