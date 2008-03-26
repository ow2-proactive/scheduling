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
package functionalTests.group.scatter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.FunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;
import functionalTests.group.A;


/**
 * distributes the parameters of a method call to member
 *
 * @author The ProActive Team
 */
@GCMDeploymentReady
public class TestScatter extends FunctionalTestDefaultNodes {
    private A typedGroup = null;
    private A parameterGroup = null;
    private A resultTypedGroup = null;

    public TestScatter() {
        super(DeploymentType._4x1);
    }

    @org.junit.Test
    public void action() throws Exception {

        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { NodeFactory.getDefaultNode(), super.getANode(), super.getANode() };
        this.typedGroup = (A) PAGroup.newGroup(A.class.getName(), params, nodes);
        Object[][] paramsParameter = { { "AgentA" }, { "AgentB" }, { "AgentC" } };
        Node[] nodesParameter = { super.getANode(), NodeFactory.getDefaultNode(), super.getANode() };
        this.parameterGroup = (A) PAGroup.newGroup(A.class.getName(), paramsParameter, nodesParameter);

        PAGroup.setScatterGroup(this.parameterGroup);
        this.resultTypedGroup = this.typedGroup.asynchronousCall(this.parameterGroup);
        PAGroup.unsetScatterGroup(this.parameterGroup);

        // was the result group created ?
        assertTrue(this.resultTypedGroup != null);

        Group group = PAGroup.getGroup(this.typedGroup);
        Group groupResult = PAGroup.getGroup(this.resultTypedGroup);

        // has the result group the same size as the caller group ?
        assertTrue(groupResult.size() == group.size());

        Group groupParameter = PAGroup.getGroup(this.parameterGroup);
        boolean rightRankingAndCorrectnessOfResults = true;
        for (int i = 0; i < group.size(); i++) {
            // is the result of the n-th group member called with the n-th parameter at the n-th position in the result group ?
            assertEquals(((A) groupResult.get(i)).getName(), (((A) group.get(i))
                    .asynchronousCall((A) groupParameter.get(i))).getName());
        }

        // is the result of the n-th group member called with the n-th parameter at the n-th position in the result group ?
    }

    public static void main(String[] args) {
        try {
            System.setProperty("fractal.provider", "org.objectweb.proactive.core.component.Fractive");
            System.setProperty("java.security.policy", System.getProperty("user.dir") +
                "/compile/proactive.java.policy");
            System.setProperty("log4j.configuration", System.getProperty("user.dir") +
                "/compile/proactive-log4j");
            System.setProperty("log4j.configuration", "file:" + System.getProperty("user.dir") +
                "/compile/proactive-log4j");
            System.setProperty("functionalTests.descriptor.defaultnodes.file",
                    "/functionalTests/descriptor/defaultnodes/NodesLocal.xml");
            TestScatter test = new TestScatter();
            test.action();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
