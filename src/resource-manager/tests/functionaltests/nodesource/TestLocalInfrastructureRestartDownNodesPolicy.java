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

import junit.framework.Assert;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;


/**
 *
 * Test checks the correct behavior of node source consisted of Local infrastructure manager
 * and static acquisition policy.
 *
 */
public class TestLocalInfrastructureRestartDownNodesPolicy extends RMConsecutive {

    protected int defaultDescriptorNodesNb = 3;

    protected void createNodeSourceWithNodes(String sourceName) throws Exception {
        RMTHelper helper = RMTHelper.getDefaultInstance();

        // creating node source
        // first parameter of im is empty default rm url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        helper.getResourceManager().createNodeSource(sourceName, LocalInfrastructure.class.getName(),
                new Object[] { "", creds, defaultDescriptorNodesNb, RMTHelper.defaultNodesTimeout, "" },
                RestartDownNodesPolicy.class.getName(), new Object[] { "ALL", "ALL", "10000" });

        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }
        // once added, wait for nodes to be configured
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }

    protected void init() throws Exception {
    }

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        init();
        String source1 = "Node_source_1";

        RMTHelper helper = RMTHelper.getDefaultInstance();
        ResourceManager resourceManager = helper.getResourceManager();
        RMTHelper.log("Test 1 - restart down nodes policy");
        createNodeSourceWithNodes(source1);

        RMState s = resourceManager.getState();
        Assert.assertEquals(defaultDescriptorNodesNb, s.getTotalNodesNumber());
        Assert.assertEquals(defaultDescriptorNodesNb, s.getFreeNodesNumber());

        NodeSet ns = resourceManager.getNodes(new Criteria(defaultDescriptorNodesNb));

        for (Node n : ns) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        String nodeUrl = ns.get(0).getNodeInformation().getURL();

        helper.killNode(nodeUrl);

        RMNodeEvent ev = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        Assert.assertEquals(NodeState.DOWN, ev.getNodeState());

        // one node is down - the policy should detect it and redeploy
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        Assert.assertEquals(defaultDescriptorNodesNb, s.getTotalNodesNumber());
        Assert.assertEquals(defaultDescriptorNodesNb, s.getTotalAliveNodesNumber());
    }
}
