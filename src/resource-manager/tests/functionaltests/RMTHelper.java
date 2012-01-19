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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.tests.ProActiveSetup;

import functionaltests.common.CommonTUtils;
import functionaltests.common.InputStreamReaderThread;
import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.monitor.RMMonitorsHandler;


/**
 *
 * Static helpers for Resource Manager functional tests.
 * It provides waiters methods that check correct event dispatching.
 *
 * @author ProActive team
 *
 */
public class RMTHelper {

    protected static final String VAR_OS = "os";

    /**
     * Number of nodes deployed with default deployment descriptor
     */
    public static int defaultNodesNumber = 5;
    /**
     * Timeout for local infrastructure
     */
    public static int defaultNodesTimeout = 20 * 1000; //20s

    private static URL functionalTestRMProperties = RMTHelper.class
            .getResource("/functionaltests/config/functionalTRMProperties.ini");

    protected static RMMonitorsHandler monitorsHandler;

    protected static RMMonitorEventReceiver eventReceiver;

    protected static ResourceManager resourceManager;

    protected static RMMonitoring monitor;
    protected static RMAuthentication auth;

    final protected static ProActiveSetup setup = new ProActiveSetup();

    /**
     * Default user name for RM's connection
     */
    public static String username = "demo";

    /**
     * Default password for RM's connection
     */
    public static String password = "demo";

    /**
     * Log a String for tests.
     *
     * @param s String to log
     */
    public static void log(String s) {
        System.out.println("------------------------------ " + s);
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
    public static Node createNode(String nodeName) throws IOException, NodeException {
        return createNode(nodeName, null);
    }

    /**
     * Creates a Default Infrastructure Manager with defaultNodesNumber nodes
     * @throws Exception
     */
    public static void createDefaultNodeSource() throws Exception {
        RMFactory.setOsJavaProperty();
        ResourceManager rm = getResourceManager();
        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            String nodeName = "default_nodermt_" + System.currentTimeMillis();
            Node node = createNode(nodeName);
            rm.addNode(node.getNodeInformation().getURL());
        }
    }

    /**
     * Creates a Local Infrastructure Manager with defaultNodesNumber nodes
     * @throws Exception
     */
    public static void createLocalNodeSource() throws Exception {
        RMFactory.setOsJavaProperty();
        ResourceManager rm = getResourceManager();
        //first emtpy im parameter is default rm url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        rm.createNodeSource(
                NodeSource.LOCAL_INFRASTRUCTURE_NAME,
                LocalInfrastructure.class.getName(),
                new Object[] { "", creds, RMTHelper.defaultNodesNumber, RMTHelper.defaultNodesTimeout,
                        setup.getJvmParameters() }, StaticPolicy.class.getName(), null);
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
    public static Node createNode(String nodeName, Map<String, String> vmParameters) throws IOException,
            NodeException {

        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setClassname("org.objectweb.proactive.core.node.StartNode");

        String jvmParameters = "";
        if (vmParameters != null) {
            for (Entry<String, String> entry : vmParameters.entrySet()) {
                if (!entry.getKey().equals("") && !entry.getValue().equals("")) {
                    jvmParameters += " -D" + entry.getKey() + "=" + entry.getValue();
                }
            }
        }

        jvmParameters += " " + setup.getJvmParameters();
        nodeProcess.setJvmOptions(jvmParameters);
        nodeProcess.setParameters(nodeName);
        nodeProcess.startProcess();
        try {
            Node newNode = null;
            Thread.sleep(5000);
            NodeException toThrow = null;
            for (int i = 0; i < 12; i++) {
                try {
                    newNode = NodeFactory.getNode("//" + ProActiveInet.getInstance().getHostname() + "/" +
                        nodeName);
                } catch (NodeException e) {
                    toThrow = e;
                    //nothing, wait another loop
                }
                if (newNode != null)
                    return newNode;
                else
                    Thread.sleep(1000);
            }
            throw toThrow == null ? new NodeException("unable to create the node " + nodeName) : toThrow;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Connect to an existing RM, without creating one, and init Monitor handler
     * Used by Scheduler's tests
     * @throws Exception
     */
    public static void connectToExistingRM() throws Exception {
        connectToExistingRM(null);
    }

    /**
     * Connect to an existing RM, without creating one, and init Monitor handler
     * @param URL existing Resource Manager's URL
     * @throws Exception
     */
    public static void connectToExistingRM(String URL) throws Exception {
        auth = RMConnection.waitAndJoin(URL);
        initEventReceiver(auth);
    }

    private static Process rmProcess;

    /**
     * Start the RM using a forked JVM and
     * deploys, with its associated Resource manager, 5 local ProActive nodes.
     *
     * @param configurationFile the RM's configuration file to use (default is functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception if an error occurs.
     */
    public static void startRM(String configurationFile) throws Exception {
        if (configurationFile == null) {
            configurationFile = new File(functionalTestRMProperties.toURI()).getAbsolutePath();
        }
        PAResourceManagerProperties.updateProperties(configurationFile);

        List<String> commandLine = new ArrayList<String>();
        commandLine.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        commandLine.add("-Djava.security.manager");
        commandLine.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() +
            CentralPAPropertyRepository.PA_HOME.getValue());
        commandLine.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
            CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getValue());
        commandLine.add(CentralPAPropertyRepository.LOG4J.getCmdLine() +
            CentralPAPropertyRepository.LOG4J.getValue());
        commandLine.add(PAResourceManagerProperties.RM_HOME.getCmdLine() +
            PAResourceManagerProperties.RM_HOME.getValueAsString());

        String home = PAResourceManagerProperties.RM_HOME.getValueAsString();
        StringBuilder classpath = new StringBuilder();
        classpath.append(home + File.separator + "dist" + File.separator + "lib" + File.separator +
            "ProActive_tests.jar");
        classpath.append(File.pathSeparator);
        classpath.append(home + File.separator + "dist" + File.separator + "lib" + File.separator +
            "ProActive_SRM-common.jar");
        classpath.append(File.pathSeparator);
        classpath.append(home + File.separator + "dist" + File.separator + "lib" + File.separator +
            "ProActive_ResourceManager.jar");
        classpath.append(File.pathSeparator);
        classpath.append(home + File.separator + "dist" + File.separator + "lib" + File.separator +
            "ProActive.jar");
        classpath.append(File.pathSeparator);
        classpath.append(home + File.separator + "dist" + File.separator + "lib" + File.separator +
            "script-js.jar");
        classpath.append(File.pathSeparator);
        classpath.append(home + File.separator + "dist" + File.separator + "lib" + File.separator +
            "jruby-engine.jar");
        classpath.append(File.pathSeparator);
        classpath.append(home + File.separator + "dist" + File.separator + "lib" + File.separator +
            "jython-engine.jar");
        classpath.append(File.pathSeparator);
        classpath.append(home + File.separator + "classes" + File.separator + "schedulerTests");
        classpath.append(File.pathSeparator);
        classpath.append(home + File.separator + "classes" + File.separator + "resource-managerTests");
        commandLine.add("-cp");
        commandLine.add(classpath.toString());
        commandLine.add(CentralPAPropertyRepository.PA_TEST.getCmdLine() + "true");
        commandLine.add(RMTStarter.class.getName());
        commandLine.add(configurationFile);

        System.out.println("Starting RM process: " + commandLine);

        ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.redirectErrorStream(true);
        rmProcess = processBuilder.start();

        InputStreamReaderThread outputReader = new InputStreamReaderThread(rmProcess.getInputStream(),
            "[RM VM output]: ");
        outputReader.start();

        String url = "//" + ProActiveInet.getInstance().getHostname();

        System.out.println("Waiting for the RM using URL: " + url);
        auth = RMConnection.waitAndJoin(url);

        initEventReceiver(auth);
    }

    /**
     * Return RM authentication interface. Start forked RM with default test
     * configuration file, if not yet started.
     * @return RMauthentication interface
     * @throws Exception
     */
    public static RMAuthentication getRMAuth() throws Exception {
        return getRMAuth(null);
    }

    /**
     * Same as getRMAuth but allows to specify a property file used to start the RM
     * @param propertyFile
     * @return
     * @throws Exception
     */
    public static RMAuthentication getRMAuth(String propertyFile) throws Exception {
        if (auth == null) {
            startRM(propertyFile);
        }
        return auth;
    }

    /**
     * Stop the Resource Manager if exists.
     * @throws Exception
     * @throws ProActiveException
     */
    public static void killRM() throws Exception {
        if (rmProcess != null) {
            rmProcess.destroy();
            rmProcess.waitFor();
            rmProcess = null;

            // sometimes RM_NODE object isn't removed from the RMI registry after JVM with RM is killed (SCHEDULING-1498)
            CommonTUtils.cleanupRMActiveObjectRegistry();
        }
        auth = null;
        monitor = null;
        resourceManager = null;
    }

    /**
     * Wait for an event regarding Scheduler state : started, resumed, stopped...
     * If a corresponding event has been already thrown by scheduler, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param event awaited event.
     */
    public static void waitForRMStateEvent(RMEventType event) {
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
    public static void waitForRMStateEvent(RMEventType eventType, long timeout)
            throws ProActiveTimeoutException {
        getMonitorsHandler().waitForRMStateEvent(eventType, timeout);
    }

    /**
     * Wait for an event regarding node sources: created, removed....
     * If a corresponding event has been already thrown by RM, returns immediately,
     * otherwise wait for reception of the corresponding event.
     * @param nodeSourceEvent awaited event.
     * @param nodeSourceName corresponding node source name for which an event is awaited.
     */
    public static void waitForNodeSourceEvent(RMEventType nodeSourceEvent, String nodeSourceName) {
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
    public static void waitForNodeSourceEvent(RMEventType eventType, String nodeSourceName, long timeout)
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
    public static RMNodeEvent waitForNodeEvent(RMEventType nodeEvent, String nodeUrl) {
        try {
            return waitForNodeEvent(nodeEvent, nodeUrl, 0);
        } catch (ProActiveTimeoutException e) {
            //unreachable block, 0 means infinite, no timeout
            //log sthing ?
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
    public static RMNodeEvent waitForNodeEvent(RMEventType eventType, String nodeUrl, long timeout)
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
    public static RMNodeEvent waitForAnyNodeEvent(RMEventType eventType) {
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
    public static void killNode(String url) throws NodeException {
        Node node = NodeFactory.getNode(url);
        try {
            node.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
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
    public static RMNodeEvent waitForAnyNodeEvent(RMEventType eventType, long timeout)
            throws ProActiveTimeoutException {
        return getMonitorsHandler().waitForAnyNodeEvent(eventType, timeout);
    }

    //-------------------------------------------------------------//
    //private methods
    //-------------------------------------------------------------//

    private static void initEventReceiver(RMAuthentication auth) throws Exception {
        RMMonitorsHandler mHandler = getMonitorsHandler();
        if (eventReceiver == null) {
            /** create event receiver then turnActive to avoid deepCopy of MonitorsHandler object
             * 	(shared instance between event receiver and static helpers).
            */
            RMMonitorEventReceiver passiveEventReceiver = new RMMonitorEventReceiver(mHandler);
            eventReceiver = (RMMonitorEventReceiver) PAActiveObject.turnActive(passiveEventReceiver);
        }
        PAFuture.waitFor(eventReceiver.init(auth));

        System.out.println("RMTHelper is connected");
    }

    /**
     * Connects to the resource manager
     */
    public static ResourceManager connect(String name, String pass) throws Exception {
        return connect(name, pass, null);
    }

    /**
     * Idem than connect but allows to specify a propertyFile used to start the RM
     */
    public static ResourceManager connect(String name, String pass, String propertyFile) throws Exception {
        RMAuthentication authInt = getRMAuth(propertyFile);
        Credentials cred = Credentials.createCredentials(
                new CredData(CredData.parseLogin(name), CredData.parseDomain(name), pass),
                authInt.getPublicKey());

        return authInt.login(cred);
    }

    /**
     * Joins to the resource manager.
     */
    public static ResourceManager join(String name, String pass) throws Exception {
        RMAuthentication authInt = getRMAuth();
        Credentials cred = Credentials.createCredentials(
                new CredData(CredData.parseLogin(name), CredData.parseDomain(name), pass),
                authInt.getPublicKey());

        while (true) {
            try {
                return authInt.login(cred);
            } catch (LoginException e) {
                Thread.sleep(100);
            }
        }
    }

    /**
     * Gets the connected ResourceManager interface.
     */
    public static ResourceManager getResourceManager() throws Exception {
        if (resourceManager == null) {
            resourceManager = connect(username, password);
        }
        return resourceManager;
    }

    /**
     * Idem than getResourceManager but allow to specify a propertyFile
     * @return the resource manager
     * @throws Exception
     */
    public static ResourceManager getResourceManager(String propertyFile) throws Exception {
        if (resourceManager == null) {
            resourceManager = connect(username, password, propertyFile);
        }
        return resourceManager;
    }

    private static RMMonitorsHandler getMonitorsHandler() {
        if (monitorsHandler == null) {
            monitorsHandler = new RMMonitorsHandler();
        }
        return monitorsHandler;
    }

}
