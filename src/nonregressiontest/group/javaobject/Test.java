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
package nonregressiontest.group.javaobject;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import nonregressiontest.descriptor.defaultnodes.TestNodes;
import nonregressiontest.group.A;

import testsuite.test.FunctionalTest;


/**
 * @author Laurent Baduel
 */
public class Test extends FunctionalTest {
    private A typedGroup = null;
    private A resultTypedGroup = null;

    /**
     *
     */
    public Test() {
        super("standard Java object in a typed group",
            "create a group with active nd non-ctive object then launch method calls");
    }

    public void action() throws Exception {
        this.resultTypedGroup = this.typedGroup.asynchronousCall();
        this.resultTypedGroup.asynchronousCall();
    }

    public void endTest() throws Exception {
        // nothing to do
    }

    public boolean preConditions() throws Exception {
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

        Group g = ProActiveGroup.getGroup(this.typedGroup);

        g.add(new A("Agent3"));
        g.add(new A("Agent4"));
        g.add(new A("Agent5"));

        g.setRatioMemberToThread(1);

        return (this.typedGroup != null);
    }

    public boolean postConditions() throws Exception {
        // was the result group created ?
        if (this.resultTypedGroup == null) {
            return false;
        }

        Group group = ProActiveGroup.getGroup(this.typedGroup);
        Group groupOfResult = ProActiveGroup.getGroup(this.resultTypedGroup);

        // has the result group the same size as the caller group ?
        if (groupOfResult.size() != group.size()) {
            return false;
        }

        boolean rightRankingOfResults = true;
        for (int i = 0; i < group.size(); i++) {
            rightRankingOfResults &= ((A) groupOfResult.get(i)).getName()
                                      .equals((((A) group.get(i)).asynchronousCall()).getName());
        }

        // is the result of the n-th group member at the n-th position in the result group ?
        return rightRankingOfResults;
    }

    public void initTest() throws Exception {
        // nothing to do : ProActive methods can not be used here
    }
}
