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

import org.junit.Before;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;
import functionalTests.descriptor.defaultnodes.TestNodes;
import functionalTests.group.A;
import static junit.framework.Assert.assertTrue;


/**
 * create a group with 3 active objects
 *
 * @author Laurent Baduel
 */
public class Test extends FunctionalTest {
    private A typedGroup = null;

    @Before
    public void before() throws Exception {
        new TestNodes().action();
    }

    private A createGroup() throws Exception {
        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { TestNodes.getSameVMNode(), TestNodes.getLocalVMNode(), TestNodes.getRemoteVMNode() };

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

        boolean rightLocations = (agent0.getNodeName().compareTo(
                TestNodes.getSameVMNode().getNodeInformation().getURL().toUpperCase()) == 0) &&
            (agent1.getNodeName().compareTo(
                    TestNodes.getLocalVMNode().getNodeInformation().getURL().toUpperCase()) == 0) &&
            (agent2.getNodeName().compareTo(
                    TestNodes.getRemoteVMNode().getNodeInformation().getURL().toUpperCase()) == 0);

        boolean rightNames = (agent0.getName().equals("Agent0")) && (agent1.getName().equals("Agent1")) &&
            (agent2.getName().equals("Agent2"));

        // are the agents at the correct location with the correct names ?
        assertTrue(rightLocations);
        assertTrue(rightNames);
    }
}
