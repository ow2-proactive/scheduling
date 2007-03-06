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
package nonregressiontest.group.barrier;

import java.util.Iterator;

import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.node.Node;

import nonregressiontest.descriptor.defaultnodes.TestNodes;

import testsuite.test.FunctionalTest;


/**
 * @author Laurent Baduel
 */
public class Test extends FunctionalTest {
    private A spmdgroup = null;

    public Test() {
        super("barrier", "perform a barrier call on an SPMD group");
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
        this.spmdgroup = (A) ProSPMD.newSPMDGroup(A.class.getName(), params,
                nodes);

        return ((this.spmdgroup != null) &&
        (ProActiveGroup.size(this.spmdgroup) == 3));
    }

    public void action() throws Exception {
        this.spmdgroup.start();
    }

    public boolean postConditions() throws Exception {
        String errors = "";
        Iterator it = ProActiveGroup.getGroup(this.spmdgroup).iterator();
        while (it.hasNext()) {
            errors += ((A) it.next()).getErrors();
        }
        System.err.print(errors);
        return "".equals(errors);
    }

    public void endTest() throws Exception {
        // nothing to do
    }

    public void initTest() throws Exception {
        // nothing to do : ProActive methods can not be used here
    }
}
