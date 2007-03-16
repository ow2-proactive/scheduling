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
package nonregressiontest.group.dynamicthreadpool;

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

    /**
     *
     */
    public Test() {
        super("dynamic threadpool",
            "add and remove member in a group to see the threadpool vary");
    }

    @Override
	public void action() throws Exception {
        Group g = ProActiveGroup.getGroup(this.typedGroup);

        this.typedGroup.onewayCall();

        for (int i = 0; i < 100; i++) {
            g.add(g.get(i % 3));
        }

        this.typedGroup.onewayCall();

        int i = 3;
        while (i < g.size()) {
            g.remove(g.size() - 1);
        }
        this.typedGroup.onewayCall();
    }

    @Override
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

        return (this.typedGroup != null);
    }

    @Override
	public void endTest() throws Exception {
        // nothing to do
    }

    @Override
	public void initTest() throws Exception {
        // nothing to do : ProActive methods can not be used here
    }
}
