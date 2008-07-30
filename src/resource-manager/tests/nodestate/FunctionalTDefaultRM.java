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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package nodestate;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.scripting.SelectionScript;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.utils.FileToBytesConverter;

import functionalTests.FunctionalTest;


/**
 * 
 * Basic class that perform before and after junit operations for
 * Resource manager functional tests. It provides some helpers methods too. 
 * 
 * @author ProActive team
 *
 */
public class FunctionalTDefaultRM extends FunctionalTest {

    protected RMUser user;
    protected RMAdmin admin;
    protected RMMonitoring monitor;

    private static String functionalTestRMProperties = FunctionalTDefaultRM.class.getResource(
            "/nodestate/functionalTRMProperties.ini").getPath();

    protected static String defaultDescriptor = FunctionalTDefaultRM.class.getResource(
            "/nodestate/GCMNodeSourceDeployment.xml").getPath();

    protected int defaultDescriptorNodesNb = 5;

    /**
     * preliminary action for testing the RM; 
     * Launching the RM, and lookup RM's main AO.
     * @throws Exception if one of these basic actions fails
     */
    @Before
    public void before() throws Exception {

        PAResourceManagerProperties.updateProperties(functionalTestRMProperties);
        RMFactory.startLocal();
        user = RMFactory.getUser();
        admin = RMFactory.getAdmin();
        monitor = RMFactory.getMonitoring();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deploy the default GCMD, and add nodes to the RM. 
     * @throws Exception if the deployment fails.
     */
    public void deployDefault() throws Exception {

        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(defaultDescriptor)));
        admin.createGCMNodesource(GCMDeploymentData, "GCM_Node_Source");
    }

    /**
     * Create a ProActive Node in a new JVM on the local host
     * This method can be used to test adding nodes mechanism
     * with already deploy ProActive nodes. 
     * @param nodeName node's name to create
     * @throws IOException if the external JVM cannot be created
     * @throws NodeException if lookup of the new node fails.
     */
    public void createNode(String nodeName) throws IOException, NodeException {

        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setClassname("org.objectweb.proactive.core.node.StartNode");
        nodeProcess.setJvmOptions(FunctionalTest.JVM_PARAMETERS);
        nodeProcess.setParameters(nodeName);
        nodeProcess.startProcess();
        try {
            Node newNode = null;
            Thread.sleep(1000);

            for (int i = 0; i < 5; i++) {
                try {
                    newNode = NodeFactory.getNode(nodeName);
                } catch (NodeException e) {
                    //nothing, wait another loop
                }
                if (newNode != null)
                    return;
                else
                    Thread.sleep(1000);
            }
            throw new NodeException("unable to create the node " + nodeName);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
     * @throws IOException if the external JVM cannot be created
     * @throws NodeException if lookup of the new node fails.
     */
    public void createNode(String nodeName, Map<String, String> vmParameters) throws IOException,
            NodeException {

        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setClassname("org.objectweb.proactive.core.node.StartNode");

        String jvmParameters = FunctionalTest.JVM_PARAMETERS;
        for (Entry<String, String> entry : vmParameters.entrySet()) {
            if (!entry.getKey().equals("") && !entry.getValue().equals("")) {
                jvmParameters += " -D" + entry.getKey() + "=" + entry.getValue();
            }
        }
        nodeProcess.setJvmOptions(jvmParameters);
        nodeProcess.setParameters(nodeName);
        nodeProcess.startProcess();
        try {
            Node newNode = null;
            Thread.sleep(1000);

            for (int i = 0; i < 5; i++) {
                try {
                    newNode = NodeFactory.getNode(nodeName);
                } catch (NodeException e) {
                    //nothing, wait another loop
                }
                if (newNode != null)
                    return;
                else
                    Thread.sleep(1000);
            }
            throw new NodeException("unable to create the node " + nodeName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
