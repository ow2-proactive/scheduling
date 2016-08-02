/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package functionaltests.utils;

import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.monitor.RMMonitorsHandler;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.StartNode;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.tests.ProActiveSetup;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.rmi.AlreadyBoundException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;


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
     * Timeout for local infrastructure
     */
    public static final int DEFAULT_NODES_TIMEOUT = 60 * 1000; //60s

    /**
     * Number of nodes deployed with default deployment descriptor
     */
    public static final int DEFAULT_NODES_NUMBER = 2;

    public final static ProActiveSetup setup = new ProActiveSetup();

    private static TestRM rm = new TestRM();
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    rm.kill();
                } catch (Exception ignored) {
                }
            }
        }));
    }

    private String currentTestConfiguration;

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
     * Creates a Local node source with specified name and default number of nodes
     * @throws Exception
     * @return expected number of nodes
     */
    public int createNodeSource(String name) throws Exception {
        createNodeSource(name, RMTHelper.DEFAULT_NODES_NUMBER);
        return RMTHelper.DEFAULT_NODES_NUMBER;
    }


    public void removeNodeSource(String name) throws Exception {
        try {
            getResourceManager().removeNodeSource(name, true).getBooleanValue();
        } catch (Throwable ignored) {
        }
    }

    /**
     * Creates a Local node source with specified name and number of nodes
     *
     * @param name
     * @param nodeNumber
     * @throws Exception
     */
    public void createNodeSource(String name, int nodeNumber) throws Exception {
        createNodeSource(name, nodeNumber, getResourceManager(), getMonitorsHandler());
    }

    public static void createNodeSource(String name, int nodeNumber, List<String> vmOptions, ResourceManager rm,
                                        RMMonitorsHandler monitor) throws Exception {
        RMFactory.setOsJavaProperty();
        log("Creating a node source " + name);
        //first emtpy im parameter is default rm url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        rm.createNodeSource(name, LocalInfrastructure.class.getName(),
                new Object[]{
                        creds,
                        nodeNumber,
                        RMTHelper.DEFAULT_NODES_TIMEOUT,
                        vmOptions != null ? setup.listToString(vmOptions) : setup.getJvmParameters()},
                StaticPolicy.class.getName(), null);
        rm.setNodeSourcePingFrequency(5000, name);

        waitForNodeSourceCreation(name, nodeNumber, monitor);
    }

    /**
     * Creates a Local node source with specified name
     */
    public static void createNodeSource(String name, int nodeNumber, ResourceManager rm,
                                        RMMonitorsHandler monitor) throws Exception {
        createNodeSource(name, nodeNumber, setup.getJvmParametersAsList(), rm, monitor);
    }

    public List<TestNode> addNodesToDefaultNodeSource(int nodesNumber) throws Exception {
        return addNodesToDefaultNodeSource(nodesNumber, new ArrayList<String>());
    }

    public List<TestNode> addNodesToDefaultNodeSource(int nodesNumber, List<String> vmOptions) throws Exception {
        return addNodesToDefaultNodeSource(nodesNumber, vmOptions != null ? vmOptions : setup.getJvmParametersAsList(), getResourceManager(), getMonitorsHandler());
    }

    public static List<TestNode> addNodesToDefaultNodeSource(int nodesNumber, List<String> vmOptions,
                                                             ResourceManager resourceManager, RMMonitorsHandler monitor) throws Exception {

        Map<String, String> map = new HashMap<>();
        List<TestNode> nodes = new ArrayList<>();
        map.put(CentralPAPropertyRepository.PA_HOME.getName(), CentralPAPropertyRepository.PA_HOME.getValue());
        for (int i = 0; i < nodesNumber; i++) {
            String nodeName = "node-" + i;
            TestNode node = createNode(nodeName, map, vmOptions != null ? vmOptions : setup.getJvmParametersAsList());
            nodes.add(node);
            resourceManager.addNode(node.getNode().getNodeInformation().getURL());
        }
        waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.DEFAULT, monitor);
        for (int i = 0; i < nodesNumber; i++) {
            waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, monitor);
        }
        return nodes;
    }

    /** Wait for the node source to be created when the node source is empty */
    public void waitForNodeSourceCreation(String name) {
        waitForNodeSourceCreation(name, 0);
    }

    public static void waitForNodeSourceCreation(String name, int nodeNumber, RMMonitorsHandler monitor) {
        waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, name, monitor);
        // this sequence of events matches what's happening on the resource manager when a node source is created
        waitForAnyMultipleNodeEvent(RMEventType.NODE_ADDED, nodeNumber, monitor);
        for (int i = 0; i < nodeNumber; i++) {
            waitForAnyNodeEvent(RMEventType.NODE_REMOVED, monitor);
            waitForAnyNodeEvent(RMEventType.NODE_ADDED, monitor);
            waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, monitor);
        }
    }

    /** Wait for the node source to be created and the nodes to be connected */
    @Deprecated
    public void waitForNodeSourceCreation(String name, int nodeNumber) {
        waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, name);
        // this sequence of events matches what's happening on the resource manager when a node source is created
        waitForAnyMultipleNodeEvent(RMEventType.NODE_ADDED, nodeNumber);
        for (int i = 0; i < nodeNumber; i++) {
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
     * @return created node URL
     * @throws IOException if the external JVM cannot be created
     * @throws NodeException if lookup of the new node fails.
     */
    public static TestNode createNode(String nodeName) throws IOException, NodeException,
            InterruptedException {
        return createNode(nodeName, new HashMap<String, String>());
    }

    public static TestNode createNode(String nodeName, Map<String, String> vmParameters) throws IOException,
            NodeException, InterruptedException {
        return createNode(nodeName, vmParameters, null);
    }

    public static TestNode createNode(String nodeName, int pnpPort) throws IOException, NodeException,
            InterruptedException {
        return createNode(nodeName, new HashMap<String, String>(), new ArrayList<String>(), pnpPort);
    }

    /**
     * Create several nodes on the same JVMProcess
     */
    public List<TestNode> createNodes(final String nodeName, int number) throws IOException, NodeException,
            ExecutionException, InterruptedException, AlreadyBoundException {
        if (number == 0) {
            throw new IllegalArgumentException("" + number);
        }

        ArrayList<TestNode> nodes = new ArrayList<>(number);
        // Start the JVMProcess and create the first node
        TestNode node0 = createNode(nodeName + 0, findFreePort());
        nodes.add(0, node0);

        // create all subsequent nodes remotely
        for (int i = 1; i < number; i++) {
            Node nodei = node0.getNode().getProActiveRuntime().createLocalNode(nodeName + i, false, null);
            nodes.add(new TestNode(node0.getNodeProcess(), nodei));
        }

        return nodes;
    }




    static int findFreePort() throws IOException {
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        return port;
    }

    private static TestNode createNode(String nodeName, Map<String, String> vmParameters,
            List<String> vmOptions) throws IOException, NodeException, InterruptedException {
        return createNode(nodeName, vmParameters, vmOptions, 0);
    }

    /**
     * Create a ProActive Node in a new JVM on the local host
     * with specific java parameters.
     * This method can be used to test adding nodes mechanism
     * with already deploy ProActive nodes.
     * @param nodeName node's name to create
     * @param vmParameters an HashMap containing key and value String
     * of type :-Dkey=value
     * @return created node URL
     * @throws IOException if the external JVM cannot be created
     * @throws NodeException if lookup of the new node fails.
     */
    private static TestNode createNode(String nodeName, Map<String, String> vmParameters,
            List<String> vmOptions, int pnpPort) throws IOException, NodeException, InterruptedException {

        if (pnpPort <= 0) {
            pnpPort = findFreePort();
        }
        String nodeUrl = "pnp://localhost:" + pnpPort + "/" + nodeName;
        vmParameters.put(PNPConfig.PA_PNP_PORT.getName(), Integer.toString(pnpPort));
        JVMProcessImpl nodeProcess = createJvmProcess(StartNode.class.getName(),
                Collections.singletonList(nodeName), vmParameters, vmOptions);
        return createNode(nodeName, nodeUrl, nodeProcess);

    }

    public static TestNode createNode(String nodeName, String expectedUrl, JVMProcess nodeProcess)
            throws IOException, NodeException, InterruptedException {
        Node newNode = null;

        long startTimeStamp = System.currentTimeMillis();

        NodeException toThrow = null;
        while ((System.currentTimeMillis() - startTimeStamp) < DEFAULT_NODES_TIMEOUT) {
            try {
                newNode = NodeFactory.getNode(expectedUrl);
            } catch (NodeException e) {
                toThrow = e;
                //nothing, wait another loop
            }
            if (newNode != null) {
                return new TestNode(nodeProcess, newNode);
            } else {
                Thread.sleep(100);
            }
        }
        throw toThrow == null ? new NodeException("unable to create the node " + nodeName) : toThrow;
    }

    public static JVMProcessImpl createJvmProcess(String className, List<String> parameters,
            Map<String, String> vmParameters, List<String> vmOptions) throws IOException {
        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setClassname(className);

        ArrayList<String> jvmParameters = new ArrayList<>();

        if (vmParameters == null) {
            vmParameters = new HashMap<>();
        }

        vmParameters.put(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getName(), "pnp");
        if (!vmParameters.containsKey(CentralPAPropertyRepository.PA_HOME.getName())) {
            vmParameters.put(CentralPAPropertyRepository.PA_HOME.getName(),
                    CentralPAPropertyRepository.PA_HOME.getValue());
        }
        if (!vmParameters.containsKey(PAResourceManagerProperties.RM_HOME.getKey())) {
            vmParameters.put(PAResourceManagerProperties.RM_HOME.getKey(),
                    PAResourceManagerProperties.RM_HOME.getValueAsString());
        }

        if (!vmParameters.containsKey(CentralPAPropertyRepository.LOG4J.getName())) {
            try {
                File log4jFile = new File(CentralPAPropertyRepository.PA_HOME.getValue(), "config/log/node.properties");
                vmParameters.put(CentralPAPropertyRepository.LOG4J.getName(), log4jFile.toURI().toURL().toString());
            } catch (MalformedURLException ignore) {

            }
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
     * Returns the list of alive  Nodes
     * @return list of ProActive Nodes urls
     */
    public Set<String> listAliveNodesUrls() throws Exception {
        return getResourceManager().listAliveNodeUrls();
    }

    /**
     * Kills the rm process
     *
     * @throws Exception
     */
    public void killRM() throws Exception {
        RMTestUser.getInstance().disconnectFromRM();
        rm.kill();
    }

    /**
     * Shutdowns the RM (takes longer than killing the process)
     *
     * @throws Exception
     */
    public void shutdownRM() throws Exception {
        getResourceManager().shutdown(true);
        killRM();
    }

    /**
     * Wait for an event regarding node sources: created, removed....
     * If a corresponding event has been already thrown by RM, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param nodeSourceEvent awaited event.
     * @param nodeSourceName corresponding node source name for which an event is awaited.
     */
    public void waitForNodeSourceEvent(RMEventType nodeSourceEvent, String nodeSourceName) throws ProActiveTimeoutException {
        waitForNodeSourceEvent(nodeSourceEvent, nodeSourceName, getMonitorsHandler());
    }

    public static void waitForNodeSourceEvent(RMEventType nodeSourceEvent, String nodeSourceName,
            RMMonitorsHandler monitorsHandler) {
        try {
            waitForNodeSourceEvent(nodeSourceEvent, nodeSourceName, 0, monitorsHandler);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log something ?
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
        waitForNodeSourceEvent(eventType, nodeSourceName, timeout, getMonitorsHandler());
    }

    public static void waitForNodeSourceEvent(RMEventType eventType, String nodeSourceName, long timeout,
            RMMonitorsHandler monitorsHandler) throws ProActiveTimeoutException {
        monitorsHandler.waitForNodesourceEvent(eventType, nodeSourceName, timeout);
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
        return waitForNodeEvent(eventType, nodeUrl, timeout, getMonitorsHandler());
    }

    public static RMNodeEvent waitForNodeEvent(RMEventType eventType, String nodeUrl, long timeout,
            RMMonitorsHandler monitorsHandler) throws ProActiveTimeoutException {
        return monitorsHandler.waitForNodeEvent(eventType, nodeUrl, timeout);
    }

    /**
     * Wait for an event on any node: added, removed....
     * If a corresponding event has been already thrown by RM, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param eventType awaited event.
     * @return RMNodeEvent object received by event receiver.
     */
    public RMNodeEvent waitForAnyNodeEvent(RMEventType eventType) {
        return waitForAnyNodeEvent(eventType, getMonitorsHandler());
    }

    /**
     * Wait for a given number of events of the same type on any node: added, removed....
     *
     * @param eventType awaited event.
     * @return RMNodeEvent object received by event receiver.
     */
    public List<RMNodeEvent> waitForAnyMultipleNodeEvent(RMEventType eventType, int nbTimes) {
        List<RMNodeEvent> answer = new ArrayList<>(nbTimes);
        for (int i = 0; i < nbTimes; i++) {
            answer.add(waitForAnyNodeEvent(eventType, getMonitorsHandler()));
        }
        return answer;
    }

    public static RMNodeEvent waitForAnyNodeEvent(RMEventType eventType, RMMonitorsHandler monitorsHandler) {
        try {
            return waitForAnyNodeEvent(eventType, 0, monitorsHandler);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
            return null;
        }
    }

    public static List<RMNodeEvent> waitForAnyMultipleNodeEvent(RMEventType eventType, int nbTimes, RMMonitorsHandler monitorsHandler) {
        try {
            List<RMNodeEvent> answer = new ArrayList<>(nbTimes);
            for (int i = 0; i < nbTimes; i++) {
                answer.add(waitForAnyNodeEvent(eventType, 0, monitorsHandler));
            }
            return answer;
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
    public static void killNode(String url) throws NodeException {
        Node node = NodeFactory.getNode(url);
        try {
            ProActiveRuntime rt = node.getProActiveRuntime();
            rt.killNode(node.getNodeInformation().getName());
        } catch (Exception ignored) {
        }
    }

    /**
     * Kills the runtime associated with specified node url
     *
     * @param url of the node
     * @throws NodeException if node cannot be looked up
     */
    public static void killRuntime(String url) throws NodeException {
        try {
            Node node = NodeFactory.getNode(url);
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
        return waitForAnyNodeEvent(eventType, timeout, getMonitorsHandler());
    }

    public static RMNodeEvent waitForAnyNodeEvent(RMEventType eventType, long timeout,
            RMMonitorsHandler monitorsHandler) throws ProActiveTimeoutException {
        return monitorsHandler.waitForAnyNodeEvent(eventType, timeout);
    }

    public void startRM(String configurationFile) throws Exception {
        startRM(configurationFile, TestRM.PA_PNP_PORT);
    }

    /**
     * Start the RM using a forked JVM
     *
     * @param configurationFile the RM's configuration file to use (default is functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception if an error occurs.
     */
    public String startRM(String configurationFile, int pnpPort, String... jvmArgs) throws Exception {
        if (!rm.isStartedWithSameConfiguration(configurationFile)) {
            killRM();
            log("Starting RM");
            rm.start(configurationFile, pnpPort, jvmArgs);
            currentTestConfiguration = configurationFile;
        }
        return rm.getUrl();
    }

    public ResourceManager getResourceManager() throws Exception {
        return getResourceManager(TestUsers.TEST);
    }

    /**
     * Idem than getResourceManager but allow to specify a propertyFile
     * @return the resource manager
     * @throws Exception
     */
    public ResourceManager getResourceManager(TestUsers user, String... jvmArgs) throws Exception {
        startRM(currentTestConfiguration, TestRM.PA_PNP_PORT, jvmArgs);

        if (!RMTestUser.getInstance().is(user)) { // changing user on the fly
            RMTestUser.getInstance().connect(user, rm.getUrl());
        }

        if (!RMTestUser.getInstance().isConnected()) {
            RMTestUser.getInstance().connect(user, rm.getUrl());
        }

        return RMTestUser.getInstance().getResourceManager();
    }

    public static String getLocalUrl() {
        return rm.getUrl();
    }

    public RMMonitorsHandler getMonitorsHandler() throws ProActiveTimeoutException {
        return RMTestUser.getInstance().getMonitorsHandler();
    }

    public RMMonitorEventReceiver getEventReceiver() {
        return RMTestUser.getInstance().getEventReceiver();
    }

    public RMAuthentication getRMAuth() throws Exception {
        startRM(currentTestConfiguration, TestRM.PA_PNP_PORT);
        return rm.getAuth();
    }

    public void disconnect() throws Exception {
        RMTestUser.getInstance().disconnectFromRM();
    }

    public boolean isRMStarted() {
        return rm.isStarted();
    }
}
