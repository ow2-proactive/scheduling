/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package functionalTests.group.creation;

import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;
import functionalTests.group.A;


/**
 * create a group with 3 active objects
 *
 * @author The ProActive Team
 */
@GCMDeploymentReady
public class TestCreation extends GCMFunctionalTestDefaultNodes {
    private A typedGroup = null;
    Node node0;
    Node node1;
    Node node2;

    public TestCreation() throws NodeException {
        super(DeploymentType._2x1);

    }

    private A createGroup() throws Exception {
        node0 = NodeFactory.getDefaultNode();
        node1 = super.getANode();
        node2 = super.getANode();

        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { node0, node1, node2 };

        this.typedGroup = (A) PAGroup.newGroup(A.class.getName(), params, nodes);
        return this.typedGroup;
    }

    @org.junit.Test
    public void action() throws Exception {
        this.createGroup();

        // was the group created ?
        assertTrue(typedGroup != null);
        Group agentGroup = PAGroup.getGroup(this.typedGroup);

        // has the group the right size ?
        assertTrue(agentGroup.size() == 3);

        A agent0 = (A) agentGroup.get(0);
        A agent1 = (A) agentGroup.get(1);
        A agent2 = (A) agentGroup.get(2);

        Assert.assertEquals(node0.getNodeInformation().getURL().toLowerCase(), agent0.getNodeName()
                .toLowerCase());
        Assert.assertEquals(node1.getNodeInformation().getURL().toLowerCase(), agent1.getNodeName()
                .toLowerCase());
        Assert.assertEquals(node2.getNodeInformation().getURL().toLowerCase(), agent2.getNodeName()
                .toLowerCase());

        Assert.assertEquals("Agent0", agent0.getName());
        Assert.assertEquals("Agent1", agent1.getName());
        Assert.assertEquals("Agent2", agent2.getName());
    }
}
