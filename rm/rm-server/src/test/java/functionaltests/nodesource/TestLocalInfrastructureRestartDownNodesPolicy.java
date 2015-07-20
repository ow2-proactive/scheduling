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
package functionaltests.nodesource;

import java.io.File;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;
import org.junit.Test;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;

import static org.junit.Assert.*;


/**
 * Test checks the correct behavior of node source consisted of Local infrastructure manager
 * and static acquisition policy.
 *
 */
public class TestLocalInfrastructureRestartDownNodesPolicy extends RMConsecutive {

    protected int defaultDescriptorNodesNb = 3;

    protected void createNodeSourceWithNodes(String sourceName, Object[] policyParameters) throws Exception {

        // creating node source
        // first parameter of im is empty default rmHelper url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        rmHelper.getResourceManager().createNodeSource(
                sourceName,
                LocalInfrastructure.class.getName(),
                new Object[] { creds, defaultDescriptorNodesNb, RMTHelper.defaultNodesTimeout,
                        CentralPAPropertyRepository.PA_RMI_PORT.getCmdLine() + RMTHelper.PA_PNP_PORT },
                RestartDownNodesPolicy.class.getName(), policyParameters);

        rmHelper.waitForNodeSourceCreation(sourceName, defaultDescriptorNodesNb);
    }

    @Test
    public void action() throws Exception {
        String source1 = "Node_source_1";

        ResourceManager resourceManager = rmHelper.getResourceManager();
        RMTHelper.log("Test 0 - create down nodes policy with null parameters (null is a valid input)");
        createNodeSourceWithNodes("Node_source_0", null);

        RMState stateTest0 = resourceManager.getState();
        assertEquals(defaultDescriptorNodesNb, stateTest0.getTotalNodesNumber());
        assertEquals(defaultDescriptorNodesNb, stateTest0.getFreeNodesNumber());

        resourceManager.removeNodeSource("Node_source_0", true);

        RMTHelper.log("Test 1 - restart down nodes policy");
        createNodeSourceWithNodes(source1, new Object[] { "ALL", "ALL", "10000" });

        RMState stateTest1 = resourceManager.getState();
        assertEquals(defaultDescriptorNodesNb, stateTest1.getTotalNodesNumber());
        assertEquals(defaultDescriptorNodesNb, stateTest1.getFreeNodesNumber());

        NodeSet ns = resourceManager.getNodes(new Criteria(defaultDescriptorNodesNb));

        for (Node n : ns) {
            rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        }

        String nodeUrl = ns.get(0).getNodeInformation().getURL();
        rmHelper.killNode(nodeUrl);

        RMNodeEvent ev = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, nodeUrl);

        assertEquals(NodeState.DOWN, ev.getNodeState());

        // one node is down - the policy should detect it and redeploy
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        assertEquals(defaultDescriptorNodesNb, stateTest1.getTotalNodesNumber());
        assertEquals(defaultDescriptorNodesNb, stateTest1.getTotalAliveNodesNumber());
    }
}
