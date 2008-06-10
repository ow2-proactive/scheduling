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
package functionalTests.group.result;

import static junit.framework.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;
import functionalTests.group.A;


/**
 * do a oneway call and an (a)synchronous call on a result group
 *
 * @author The ProActive Team
 */
@GCMDeploymentReady
public class TestResult extends GCMFunctionalTestDefaultNodes {
    private A resultTypedGroup = null;
    private A resultResultTypedGroup = null;

    public TestResult() {
        super(2, 1);
    }

    @org.junit.Test
    public void action() throws Exception {
        this.resultTypedGroup.onewayCall();
        this.resultResultTypedGroup = this.resultTypedGroup.asynchronousCall();

        Group<A> group = PAGroup.getGroup(this.resultTypedGroup);
        Group<A> groupResult = PAGroup.getGroup(this.resultResultTypedGroup);

        // was the oneway call on the result group ok ?
        boolean allOnewayCallDone = true;
        Iterator<A> it = group.iterator();
        while (it.hasNext()) {
            allOnewayCallDone &= ((A) it.next()).isOnewayCallReceived();
        }
        assertTrue(allOnewayCallDone);

        // has the result-result group the same size as the caller group (result group) ?
        assertTrue(groupResult.size() == group.size());

        // is the result of the n-th group member at the n-th position in the result-result group ?
        boolean rightRankingOfResults = true;
        for (int i = 0; i < group.size(); i++) {
            rightRankingOfResults &= ((A) groupResult.get(i)).getName().equals(
                    (((A) group.get(i)).asynchronousCall()).getName());
        }
        assertTrue(rightRankingOfResults);
    }

    @Before
    public void preConditions() throws Exception {
        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { NodeFactory.getDefaultNode(), super.getANode(), super.getANode() };
        A typedGroup = (A) PAGroup.newGroup(A.class.getName(), params, nodes);
        this.resultTypedGroup = typedGroup.asynchronousCall();

        boolean NoOnewayCallDone = true;
        Group<A> group = PAGroup.getGroup(this.resultTypedGroup);
        Iterator<A> it = group.iterator();
        while (it.hasNext()) {
            NoOnewayCallDone &= !((A) it.next()).isOnewayCallReceived();
        }
        assertTrue(NoOnewayCallDone && (this.resultResultTypedGroup == null));
    }
}
