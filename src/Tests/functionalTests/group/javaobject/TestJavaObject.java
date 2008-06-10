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
package functionalTests.group.javaobject;

import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;
import functionalTests.group.A;


/**
 * create a group with active nd non-ctive object then launch method calls
 *
 * @author The ProActive Team
 */
@GCMDeploymentReady
public class TestJavaObject extends GCMFunctionalTestDefaultNodes {
    private A typedGroup = null;
    private A resultTypedGroup = null;

    public TestJavaObject() {
        super(2, 1);
    }

    @org.junit.Test
    public void action() throws Exception {
        this.resultTypedGroup = this.typedGroup.asynchronousCall();
        this.resultTypedGroup.asynchronousCall();

        // was the result group created ?
        assertTrue(this.resultTypedGroup != null);

        Group<A> group = PAGroup.getGroup(this.typedGroup);
        Group<A> groupOfResult = PAGroup.getGroup(this.resultTypedGroup);

        // has the result group the same size as the caller group ?
        assertTrue(groupOfResult.size() == group.size());

        boolean rightRankingOfResults = true;
        for (int i = 0; i < group.size(); i++) {
            rightRankingOfResults &= ((A) groupOfResult.get(i)).getName().equals(
                    (((A) group.get(i)).asynchronousCall()).getName());
        }

        // is the result of the n-th group member at the n-th position in the result group ?
        assertTrue(rightRankingOfResults);
    }

    @Before
    public void preConditions() throws Exception {
        //@snippet-start get_group
        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { NodeFactory.getDefaultNode(), super.getANode(), super.getANode() };
        this.typedGroup = (A) PAGroup.newGroup(A.class.getName(), params, nodes);

        Group<A> g = PAGroup.getGroup(this.typedGroup);

        g.add(new A("Agent3"));
        g.add(new A("Agent4"));
        g.add(new A("Agent5"));
        //@snippet-end get_group

        g.setRatioMemberToThread(1);

        assertTrue(this.typedGroup != null);
    }
}
