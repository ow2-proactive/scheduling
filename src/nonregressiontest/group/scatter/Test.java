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
package nonregressiontest.group.scatter;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import nonregressiontest.descriptor.defaultnodes.TestNodes;
import nonregressiontest.group.A;

import testsuite.test.Assertions;
import testsuite.test.FunctionalTest;


/**
 * @author Laurent Baduel
 */
public class Test extends FunctionalTest {
    private A typedGroup = null;
    private A parameterGroup = null;
    private A resultTypedGroup = null;

    public Test() {
        super("scatter parameters",
            "distributes the parameters of a method call to member");
    }

    public void action() throws Exception {
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
        Object[][] paramsParameter = {
                { "AgentA" },
                { "AgentB" },
                { "AgentC" }
            };
        Node[] nodesParameter = {
                TestNodes.getRemoteVMNode(), TestNodes.getSameVMNode(),
                TestNodes.getLocalVMNode()
            };
        this.parameterGroup = (A) ProActiveGroup.newGroup(A.class.getName(),
                paramsParameter, nodesParameter);

        ProActiveGroup.setScatterGroup(this.parameterGroup);
        this.resultTypedGroup = this.typedGroup.asynchronousCall(this.parameterGroup);
        ProActiveGroup.unsetScatterGroup(this.parameterGroup);
    }

    public void endTest() throws Exception {
        // nothing to do
    }

    public void initTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
        // was the result group created ?
        if (this.resultTypedGroup == null) {
            return false;
        }

        Group group = ProActiveGroup.getGroup(this.typedGroup);
        Group groupResult = ProActiveGroup.getGroup(this.resultTypedGroup);

        // has the result group the same size as the caller group ?
        if (groupResult.size() != group.size()) {
            return false;
        }

        Group groupParameter = ProActiveGroup.getGroup(this.parameterGroup);
        boolean rightRankingAndCorrectnessOfResults = true;
        for (int i = 0; i < group.size(); i++) {
            // is the result of the n-th group member called with the n-th parameter at the n-th position in the result group ?
            Assertions.assertEquals(((A) groupResult.get(i)).getName(),
                (((A) group.get(i)).asynchronousCall((A) groupParameter.get(i))).getName());
        }

        // is the result of the n-th group member called with the n-th parameter at the n-th position in the result group ?
        return true;
    }

    public static void main(String[] args) {
        try {
            System.setProperty("fractal.provider",
                "org.objectweb.proactive.core.component.Fractive");
            System.setProperty("java.security.policy",
                System.getProperty("user.dir") +
                "/compile/proactive.java.policy");
            System.setProperty("log4j.configuration",
                System.getProperty("user.dir") + "/compile/proactive-log4j");
            System.setProperty("log4j.configuration",
                "file:" + System.getProperty("user.dir") +
                "/compile/proactive-log4j");
            System.setProperty("nonregressiontest.descriptor.defaultnodes.file",
                "/nonregressiontest/descriptor/defaultnodes/NodesLocal.xml");
            Test test = new Test();
            test.initTest();
            test.action();
            boolean success = test.postConditions();
            if (success) {
                System.out.println("[SUCCESS] " + test.getName());
            } else {
                System.out.println("[FAILED] " + test.getName());
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
