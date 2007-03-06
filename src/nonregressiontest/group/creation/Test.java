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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package nonregressiontest.group.creation;

import nonregressiontest.descriptor.defaultnodes.TestNodes;
import nonregressiontest.group.A;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.FunctionalTest;


/**
 * @author Laurent Baduel
 */
public class Test extends FunctionalTest {
    private A typedGroup = null;

    public Test() {
        super("group creation", "create a group with 3 active objects");
    }

    private A createGroup() throws Exception {
        Object[][] params = {
                { "Agent0" },
                { "Agent1" },
                { "Agent2" }
            };
        Node[] nodes = {
                TestNodes.getSameVMNode(), TestNodes.getLocalVMNode(),
                TestNodes.getRemoteVMNode()
            };

        this.typedGroup = (A) ProActiveGroup.newGroup(A.class.getName(),
                params, nodes);
        return this.typedGroup;
    }

    public void action() throws Exception {
        this.createGroup();
    }

    public A action(Object o) throws Exception {
        return this.createGroup();
    }

    public void initTest() throws Exception {
        // nothing to do
    }

    public void endTest() throws Exception {
        // nothing to do
    }

    public boolean postConditions() throws Exception {
        // was the group created ?
        if (this.typedGroup == null) {
            return false;
        }
        Group agentGroup = ProActiveGroup.getGroup(this.typedGroup);

        // has the group the right size ?
        if (agentGroup.size() != 3) {
            return false;
        }

        A agent0 = (A) agentGroup.get(0);
        A agent1 = (A) agentGroup.get(1);
        A agent2 = (A) agentGroup.get(2);

        boolean rightLocations = (agent0.getNodeName().compareTo(TestNodes.getSameVMNode()
                                                                          .getNodeInformation()
                                                                          .getURL()
                                                                          .toUpperCase()) == 0) &&
            (agent1.getNodeName().compareTo(TestNodes.getLocalVMNode()
                                                     .getNodeInformation()
                                                     .getURL().toUpperCase()) == 0) &&
            (agent2.getNodeName().compareTo(TestNodes.getRemoteVMNode()
                                                     .getNodeInformation()
                                                     .getURL().toUpperCase()) == 0);

        boolean rightNames = (agent0.getName().equals("Agent0")) &&
            (agent1.getName().equals("Agent1")) &&
            (agent2.getName().equals("Agent2"));

        // are the agents at the correct location with the correct names ?
        return (rightLocations && rightNames);
    }
    
    
 public static void main(String[] args) {
        
        Test test = new Test();
        try {
            test.action();
            if (test.postConditions()) {
                System.out.println("TEST SUCCEEDED");
            } else {
                System.out.println("TEST FAILED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
