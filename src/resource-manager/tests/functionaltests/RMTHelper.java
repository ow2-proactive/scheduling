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
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
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
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;

import functionalTests.FunctionalTest;
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

    protected static URL startForkedRMApplication = RMTHelper.class
            .getResource("/functionaltests/config/StartForkedRMApplication.xml");

    protected static VariableContractImpl vContract;
    protected static GCMApplication gcmad;

    /**
     * A default deployment descriptor
     */
    public static String defaultDescriptor = RMTHelper.class.getResource(
            "/functionaltests/config/GCMNodeSourceDeployment.xml").getPath();

    /**
     * Number of nodes deployed with default deployment descriptor
     */
    public static int defaultNodesNumber = 5;

    private static String functionalTestRMProperties = RMTHelper.class.getResource(
            "/functionaltests/config/functionalTRMProperties.ini").getPath();

    protected static RMMonitorsHandler monitorsHandler;

    protected static RMMonitorEventReceiver eventReceiver;

    protected static ResourceManager resourceManager;

    protected static RMMonitoring monitor;
    protected static RMAuthentication auth;

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

    public static void createGCMLocalNodeSource() throws Exception {
        RMFactory.setOsJavaProperty();
        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(defaultDescriptor)));
        ResourceManager rm = getResourceManager();
        rm.createNodeSource(NodeSource.GCM_LOCAL, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), null);
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

        jvmParameters += " " + FunctionalTest.getJvmParameters();
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

    /**
     * Start the scheduler using a forked JVM.
     * It uses Scheduler Properties file designed for tests
     * (database is recovered without jobs).
     *
     * @throws Exception
     */
    public static void startRM() throws Exception {
        startRM(functionalTestRMProperties);
    }

    /**
     * Start the RM using a forked JVM and
     * deploys, with its associated Resource manager, 5 local ProActive nodes.
     *
     * @param configurationFile the RM's configuration file to use (default is functionalTSchedulerProperties.ini)
     * 			null to use the default one.
     * @throws Exception if an error occurs.
     */
    public static void startRM(String configurationFile) throws Exception {
        PAResourceManagerProperties.updateProperties(configurationFile);
        deployRMGCMA();
        GCMVirtualNode vn = gcmad.getVirtualNode("VN");
        Node node = vn.getANode();
        RMLauncherAO launcherAO = (RMLauncherAO) PAActiveObject.newActive(RMLauncherAO.class.getName(), null,
                node);
        auth = launcherAO.createAndJoinForkedRM(configurationFile);
        initEventReceiver(auth);
    }

    /**
     * Return RM authentication interface. Start forked RM with default test
     * configuration file, if not yet started.
     * @return RMauthentication interface
     * @throws Exception
     */
    public static RMAuthentication getRMAuth() throws Exception {
        if (auth == null) {
            startRM();
        }
        return auth;
    }

    /**
     * Stop the Resource Manager if exists.
     * @throws Exception
     * @throws ProActiveException
     */
    public static void killRM() throws Exception {
        if (gcmad != null) {
            gcmad.kill();
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

    private static void deployRMGCMA() throws ProActiveException {

        vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(VAR_OS, OperatingSystem.getOperatingSystem().name(),
                VariableContractType.DescriptorDefaultVariable);
        StringBuilder properties = new StringBuilder("-Djava.security.manager");
        properties.append(" " + CentralPAPropertyRepository.PA_HOME.getCmdLine() +
            CentralPAPropertyRepository.PA_HOME.getValue());
        properties.append(" " + CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
            CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getValue());
        properties.append(" " + CentralPAPropertyRepository.LOG4J.getCmdLine() +
            CentralPAPropertyRepository.LOG4J.getValue());

        properties.append(" " + PAResourceManagerProperties.RM_HOME.getCmdLine() +
            PAResourceManagerProperties.RM_HOME.getValueAsString());
        vContract.setVariableFromProgram("jvmargDefinedByTest", properties.toString(),
                VariableContractType.DescriptorDefaultVariable);
        gcmad = PAGCMDeployment.loadApplicationDescriptor(startForkedRMApplication, vContract);
        gcmad.startDeployment();
    }

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
        RMAuthentication authInt = getRMAuth();
        Credentials cred = Credentials.createCredentials(new CredData(name, pass), authInt.getPublicKey());

        return authInt.login(cred);
    }

    /**
     * Joins to the resource manager.
     */
    public static ResourceManager join(String name, String pass) throws Exception {
        RMAuthentication authInt = getRMAuth();
        Credentials cred = Credentials.createCredentials(new CredData(name, pass), authInt.getPublicKey());

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

    private static RMMonitorsHandler getMonitorsHandler() {
        if (monitorsHandler == null) {
            monitorsHandler = new RMMonitorsHandler();
        }
        return monitorsHandler;
    }

}
