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
package nonregressiontest.group.oneserialization;

import java.util.Iterator;

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
    /**
	 * 
	 */
	private static final long serialVersionUID = -8599180613841630776L;
	private A typedGroup = null;

    public Test() {
        super("one serialization of the methodcall object in a group communication",
            "do only serialization of the MethodCall object (in broadcast call only)");
    }

    @Override
	public void action() throws Exception {
        ProActiveGroup.setUniqueSerialization(this.typedGroup);
        this.typedGroup.onewayCall();
        ProActiveGroup.unsetUniqueSerialization(this.typedGroup);
    }

    @Override
	public void endTest() throws Exception {
        // nothing to do
    }

    @Override
	public void initTest() throws Exception {
        // nothing to do : ProActive methods can be used here
    }

    @Override
	public boolean postConditions() throws Exception {
        boolean allOnewayCallDone = true;
        Group group = ProActiveGroup.getGroup(this.typedGroup);
        Iterator it = group.iterator();
        while (it.hasNext()) {
            allOnewayCallDone &= ((A) it.next()).isOnewayCallReceived();
        }
        return allOnewayCallDone;
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
        ProActiveGroup.getGroup(this.typedGroup).setRatioMemberToThread(1);

        boolean NoOnewayCallDone = true;
        Group group = ProActiveGroup.getGroup(this.typedGroup);
        Iterator it = group.iterator();
        while (it.hasNext()) {
            NoOnewayCallDone &= !((A) it.next()).isOnewayCallReceived();
        }
        return NoOnewayCallDone;
    }
}
